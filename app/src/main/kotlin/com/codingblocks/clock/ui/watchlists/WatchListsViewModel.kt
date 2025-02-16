package com.codingblocks.clock.ui.watchlists

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import com.codingblocks.clock.core.local.data.WatchListConfig
import com.codingblocks.clock.core.local.data.WatchlistWithPositions
import com.codingblocks.clock.core.model.clock.StatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchListViewModel(
    private val dataRepo: DataRepo
) : ControllerViewModel<WatchListViewModel.Action, WatchListViewModel.State>() {

    enum class ShowType {
        FT, NFT, LP, FT_LP
    }
    sealed class PositionItem {
        data class FT(val positionFT: PositionFTLocal) : PositionItem()
        data class NFT(val positionNFT: PositionNFTLocal) : PositionItem()
        data class LP(val positionLP: PositionLPLocal) : PositionItem()
    }

    sealed class Action {
        data class OnResolveClick(val address: String) : Action()
        data class AddNewWatchlist(val config: WatchListConfig) : Action()
        data object GetPositions : Action()
        data class SelectListToShow(val showType: ShowType) : Action()
        data object GetClockStatus : Action()
    }

    sealed class Mutation {
       /* data class PositionsFTChanged(val positions: List<PositionFTLocal>) : Mutation()
        data class PositionsNFTChanged(val positions: List<PositionNFTLocal>) : Mutation()
        data class PositionsLPChanged(val positions: List<PositionLPLocal>) : Mutation()
        data class PositionsFTIncludingLP(val positions: List<PositionFTLocal>) : Mutation()*/
        data class WatchlistsWithPositionsChanged(val watchlistsWithPositions: List<WatchlistWithPositions>) : Mutation()
        data class ClockStatusChanged(val statusResponse: StatusResponse) : Mutation()
        data class ShowAddWatchlistDialogChanged(val show: Boolean) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
        data class ErrorAddWatchListChanged(val errorMessage: String?) : Mutation()
        data class ShowTypeChanged(val showType: ShowType) : Mutation()
    }

    data class State(
        val status: StatusResponse? = null,
        val watchlistsWithPositions: List<WatchlistWithPositions> = emptyList(),
        val enteredAddress: String = "",
        val resolvedAddress: String? = null,
        val resolveError: String? = null,
        /*val positionsNFT: List<PositionNFTLocal> = emptyList(),
        val positionsLP: List<PositionLPLocal> = emptyList(),
        val positionsFTIncludingLP: List<PositionFTLocal> = emptyList(),*/
        val errorAddWatchlist: String? = null,
        val showType: ShowType = ShowType.FT,
        val error: String? = null,
        val showAddWatchListDialog: Boolean = false,
    ) {
        val currentFirstWatchListWithPositions: WatchlistWithPositions?
            get() = watchlistsWithPositions.firstOrNull()
    }

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(
                // todo get async as in
                // employee = dataRepo.myProfile.current.getOrNull().asBasicEmployee!!
                watchlistsWithPositions = dataRepo.watchlistsWithPositions,
                /*positionsFT = dataRepo.positionsFT,
                positionsNFT = dataRepo.positionsNFT,
                positionsLP = dataRepo.positionsLP,
                positionsFTIncludingLP = dataRepo.positionsFTIncludingLP,*/
            ),
            mutator = { action ->
                when (action) {
                    Action.GetClockStatus -> flow {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                emit(Mutation.ErrorChanged(null))
                                dataRepo.getClockStatus()
                                    .onSuccess { emit(Mutation.ClockStatusChanged(it)) }.onFailure {
                                        emit(
                                            Mutation.ErrorChanged(
                                                it.message
                                                    ?: "could not retreive Clock Status:\n$it"
                                            )
                                        )
                                    }
                            } catch (e: Exception) {
                                Timber.d("could not retreive Clock Status $e")
                            }
                        }
                    }

                    Action.GetPositions -> flow {
                        // obsolete, remove when aadnewlist tested
                        try {
                            emit(Mutation.ErrorChanged(null))
                            // todo on add watchlist check if on with walletDress already exists!
                            val watchlistNumber = dataRepo.addWatchlist(
                                "\$statti",
                                includeNFT = true,
                                includeLPinFT = true,
                                showLPTab = true,
                                walletAddress = "stake1uy368slqls4u66g0502krmyasfdke3elfh0z6qgczmylx7ce6frnl"
                            )
                            dataRepo.loadPositionsForAddress("stake1uy368slqls4u66g0502krmyasfdke3elfh0z6qgczmylx7ce6frnl")
                                .onSuccess {
                                    dataRepo.updateOrInsertPositions(watchlistNumber, it)
                                    val updatedWatchlistsWithPositions = dataRepo.watchlistsWithPositions
                                    emit(Mutation.WatchlistsWithPositionsChanged(updatedWatchlistsWithPositions))  }
                                .onFailure { emit(Mutation.ErrorChanged("could not retreive Positions:\n$it")) }
                        } catch (e: Exception) {
                            Timber.d("could not retreive Positions $e")
                        }
                    }
                    is Action.AddNewWatchlist -> flow {
                        emit(Mutation.ErrorAddWatchListChanged(null))
                        val newWatchlistConfig = action.config
                        Timber.tag("wims").i("revieved wathclist ${newWatchlistConfig}")
                        // check if name already taken or watchlist with this stakeAddress already exists
                        val existingWatchlistConfig = dataRepo.findWatchlistWithAddressOrName(newWatchlistConfig.walletAddress,newWatchlistConfig.name)
                        if (existingWatchlistConfig != null) {
                            if (existingWatchlistConfig.walletAddress == newWatchlistConfig.walletAddress) emit(Mutation.ErrorAddWatchListChanged("You already have a watchlist with this stakeAddress: ${existingWatchlistConfig.name}"))
                            if (existingWatchlistConfig.name == newWatchlistConfig.name) emit(Mutation.ErrorAddWatchListChanged("This name is already used for one of your other watchlists"))
                            // todo add state nameError
                        } else {
                            Timber.tag("wims").i("adding the new watchlist}")
                            try {
                                emit(Mutation.ErrorChanged(null))
                                val watchlistNumber = with (newWatchlistConfig) {
                                    dataRepo.addWatchlist(
                                        name = name,
                                        includeNFT = includeNFT,
                                        includeLPinFT = includeLPinFT,
                                        showLPTab = showLPTab,
                                        walletAddress = walletAddress
                                    )
                                }
                                emit(Mutation.ShowAddWatchlistDialogChanged(false))
                                Timber.tag("wims").i("       new watchlist added: $watchlistNumber}")
                                newWatchlistConfig.walletAddress?.let { walletAddress ->
                                    Timber.tag("wims").i("getting positions for new watchlist with $walletAddress}")
                                    dataRepo.loadPositionsForAddress(walletAddress)
                                        .onSuccess {
                                            Timber.tag("wims").i("loading success}")
                                            dataRepo.updateOrInsertPositions(watchlistNumber, it)
                                            Timber.tag("wims").i("inserting in db success}")
                                            val updatedWatchlistsWithPositions =
                                                dataRepo.watchlistsWithPositions
                                            emit(
                                                Mutation.WatchlistsWithPositionsChanged(
                                                    updatedWatchlistsWithPositions
                                                )
                                            )
                                            Timber.tag("wims").i("update state with all watchlists done}")
                                        }
                                        .onFailure {
                                            // how do we handle an added watchlist ADDED, but no positions loaded for it ? how do we recover from this?
                                            // reload button in the card , plus maybe a warning trianlge next to it )on click show the info not loaded, lets try...
                                            emit(Mutation.ErrorChanged("could not retrieve Positions:\n$it")) }
                                }
                            } catch (e: Exception) {
                                Timber.d("could not retrieve Positions $e")
                                emit(Mutation.ErrorChanged("Something went wrong adding the new watchlist ${e.message}"))
                            }
                        }
                    }
                    is Action.SelectListToShow -> flow {
                        emit(Mutation.ShowTypeChanged(action.showType))
                    }

                    is Action.OnResolveClick -> flow {
                        if (action.address.startsWith("$")) {
                            // resolve with handle
                        } else {
                            // resolve with stake
                        }
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.ClockStatusChanged -> previousState.copy(status = mutation.statusResponse)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                  /*  is Mutation.PositionsFTChanged -> previousState.copy(positionsFT = mutation.positions)
                    is Mutation.PositionsLPChanged -> previousState.copy(positionsLP = mutation.positions)
                    is Mutation.PositionsNFTChanged -> previousState.copy(positionsNFT = mutation.positions)
                    is Mutation.PositionsFTIncludingLP -> previousState.copy(positionsFTIncludingLP = mutation.positions)*/
                    is Mutation.ShowTypeChanged -> previousState.copy(showType = mutation.showType)
                    is Mutation.WatchlistsWithPositionsChanged -> previousState.copy(watchlistsWithPositions = mutation.watchlistsWithPositions)
                    is Mutation.ErrorAddWatchListChanged -> previousState.copy(errorAddWatchlist = mutation.errorMessage)
                    is Mutation.ShowAddWatchlistDialogChanged -> previousState.copy(showAddWatchListDialog = mutation.show)
                }
            }
        )
}