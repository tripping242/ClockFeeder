package com.codingblocks.clock.ui.watchlists

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.local.data.FeedFT
import com.codingblocks.clock.core.local.data.FeedNFT
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
import java.time.ZonedDateTime

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
        data class EnteredAddressChanged(val address: String) : Action()
        data class AddNewWatchlist(val config: WatchListConfig) : Action()
        data class SelectListToShow(val showType: ShowType) : Action()
        data object GetClockStatus : Action()
        data class ShowAddWatchListDialogChanged(val show: Boolean) : Action()
        data class SettingsChanged(val config: WatchListConfig) : Action()
        data class ReloadPositions(val watchListNumber: Int, val walletAdress: String?) : Action()
        data class DeleteWatchList(val watchListNumber: Int) : Action()
        data object ResetError : Action()
        data object ResetAddError : Action()
        data class FTALertChanged(val unit: String, val watchList: Int) : Action()
        data class NFTALertChanged(val policy: String, val watchList: Int) : Action()
        data class LPALertChanged(val ticker: String, val watchList: Int) : Action()
    }

    sealed class Mutation {
        data class WatchlistsWithPositionsChanged(val watchlistsWithPositions: List<WatchlistWithPositions>) :
            Mutation()

        data class ClockStatusChanged(val statusResponse: StatusResponse) : Mutation()
        data class ShowAddWatchlistDialogChanged(val show: Boolean) : Mutation()
        data class ShowLoading(val show: Boolean) : Mutation()
        data class ShowReloading(val watchListNumber: Int) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
        data class ResolvedAddressChanged(val address: String?) : Mutation()
        data class ResolveErrorChanged(val errorMessage: String?) : Mutation()
        data class ErrorAddWatchListDuplicateAddress(val errorMessage: String?) : Mutation()
        data class ErrorAddWatchListDuplicateName(val errorMessage: String?) : Mutation()
        data class ShowTypeChanged(val showType: ShowType) : Mutation()
        data class EnteredAddressChanged(val address: String) : Mutation()
    }

    data class State(
        val status: StatusResponse? = null,
        val watchlistsWithPositions: List<WatchlistWithPositions> = emptyList(),
        val enteredAddress: String = "",
        val resolvedAddress: String? = null,
        val resolveError: String? = null,
        val errorAddWatchlist: String? = null,
        val showType: ShowType = ShowType.FT,
        val error: String? = null,
        val showAddWatchListDialog: Boolean = false,
        val errorDuplicateAddress: Boolean = false,
        val errorDuplicateName: Boolean = false,
        val isLoading: Boolean = false,
        val reloadingWatchListNumber: Int = -1,
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

                    is Action.AddNewWatchlist -> flow {
                        emit(Mutation.ErrorAddWatchListDuplicateAddress(null))
                        emit(Mutation.ErrorAddWatchListDuplicateName(null))
                        emit(Mutation.ErrorAddWatchListDuplicateName(null))
                        val newWatchlistConfig = action.config
                        Timber.tag("wims").i("revieved wathclist ${newWatchlistConfig}")
                        // check if name already taken or watchlist with this stakeAddress already exists
                        val existingWatchlistConfig = dataRepo.findWatchlistWithAddressOrName(
                            newWatchlistConfig.walletAddress,
                            newWatchlistConfig.name
                        )
                        if (existingWatchlistConfig != null) {
                            Timber.tag("wims").i("already exists")
                            if (existingWatchlistConfig.walletAddress == newWatchlistConfig.walletAddress) {
                                emit(Mutation.ResolvedAddressChanged(null))
                                emit(Mutation.ErrorAddWatchListDuplicateAddress("You already have a watchlist with this stakeAddress: ${existingWatchlistConfig.name}"))
                            }
                            if (existingWatchlistConfig.name == newWatchlistConfig.name) emit(
                                Mutation.ErrorAddWatchListDuplicateName("This name is already used for one of your other watchlists")
                            )
                        } else {
                            try {
                                emit(Mutation.ErrorChanged(null))
                                val watchlistNumber = with(newWatchlistConfig) {
                                    dataRepo.addWatchlist(
                                        name = name,
                                        includeNFT = includeNFT,
                                        includeLPinFT = includeLPinFT,
                                        showLPTab = showLPTab,
                                        walletAddress = walletAddress
                                    )
                                }
                                Timber.tag("wims").i("added config")
                                emit(Mutation.ShowAddWatchlistDialogChanged(false))
                                emit(Mutation.EnteredAddressChanged(""))
                                emit(Mutation.ResolvedAddressChanged(null))
                                newWatchlistConfig.walletAddress?.let { walletAddress ->
                                    emit(Mutation.ShowLoading(true))
                                    dataRepo.loadPositionsForAddress(walletAddress)
                                        .onSuccess {
                                            dataRepo.updateOrInsertPositions(watchlistNumber, it)
                                            val updatedWatchlistsWithPositions =
                                                dataRepo.watchlistsWithPositions
                                            emit(
                                                Mutation.WatchlistsWithPositionsChanged(
                                                    updatedWatchlistsWithPositions
                                                )
                                            )
                                        }
                                        .onFailure {
                                            emit(Mutation.ShowAddWatchlistDialogChanged(false))
                                            emit(Mutation.ErrorChanged("could not retrieve Positions:\n$it"))
                                        }
                                    emit(Mutation.ShowLoading(false))
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
                        emit(Mutation.ResolveErrorChanged(null))
                        if (action.address.startsWith("$")) {
                            // resolve with handle
                            dataRepo.resolveAdaHandle(action.address.removePrefix("$"))
                                .onSuccess { emit(Mutation.ResolvedAddressChanged(it)) }
                                .onFailure { emit(Mutation.ResolveErrorChanged("Could not resolve handle")) }
                        } else {
                            dataRepo.getStakeAddress(action.address)
                                .onSuccess { emit(Mutation.ResolvedAddressChanged(it)) }
                                .onFailure { emit(Mutation.ResolveErrorChanged("Could not find Address")) }
                        }
                    }

                    is Action.SettingsChanged -> flow {
                        dataRepo.updateWatchlistSettings(action.config)
                        val updatedWatchlistsWithPositions =
                            dataRepo.watchlistsWithPositions
                        emit(
                            Mutation.WatchlistsWithPositionsChanged(
                                updatedWatchlistsWithPositions
                            )
                        )
                    }

                    is Action.ReloadPositions -> flow {
                        // to let reload button change in crcular progress indicator
                        action.walletAdress?.let { walletAddress ->
                            emit(Mutation.ShowReloading(action.watchListNumber))
                            dataRepo.loadPositionsForAddress(walletAddress).onSuccess {
                                dataRepo.updateOrInsertPositions(action.watchListNumber, it)
                                val updatedWatchlistsWithPositions =
                                    dataRepo.watchlistsWithPositions
                                emit(
                                    Mutation.WatchlistsWithPositionsChanged(
                                        updatedWatchlistsWithPositions
                                    )
                                )
                            }.onFailure {
                                emit(Mutation.ErrorChanged("could not retrieve Positions:\n$it"))
                            }
                            emit(Mutation.ShowReloading(-1))
                        }

                    }

                    is Action.DeleteWatchList -> flow {
                        try {
                            dataRepo.deleteWatchlist(action.watchListNumber)
                            Timber.tag("wims").i("deleted, now updating")
                            emit(Mutation.ShowLoading(true))
                            val updatedWatchlistsWithPositions =
                                dataRepo.watchlistsWithPositions
                            emit(
                                Mutation.WatchlistsWithPositionsChanged(
                                    updatedWatchlistsWithPositions
                                )
                            )
                        } catch (e: Exception) {
                            Timber.d("Could not remove watchlist")
                        } finally {
                            emit(Mutation.ShowLoading(false))
                        }
                    }

                    is Action.ResetError -> flow {
                        emit(Mutation.ErrorChanged(null))
                    }

                    is Action.ResetAddError -> flow {
                        emit(Mutation.ErrorAddWatchListDuplicateName(null))
                        emit(Mutation.ErrorAddWatchListDuplicateAddress(null))
                    }

                    is Action.ShowAddWatchListDialogChanged -> flow {
                        emit(Mutation.ShowAddWatchlistDialogChanged(action.show))
                    }

                    is Action.EnteredAddressChanged -> flow {
                        emit(Mutation.EnteredAddressChanged(action.address))
                    }

                    is Action.FTALertChanged -> flow {
                        val updatePosition = dataRepo.getFTPositionBy(action.unit, action.watchList)
                        updatePosition?.let {
                            var showInfeed = true
                            if (!updatePosition.showInFeed) {
                                Timber.tag("wims").i("should add feed")
                                val feedAdded = dataRepo.addFeedFT(
                                    FeedFT(
                                        updatePosition.unit,
                                        ZonedDateTime.now(),
                                        ZonedDateTime.now(),
                                        true,
                                        false,
                                    )
                                )
                                showInfeed = feedAdded
                            }
                            if (showInfeed) {
                                Timber.tag("wims").i("added, now size is ${dataRepo.getAllFeedFT().size}")
                                dataRepo.updatePosition(
                                    it.copy(showInFeed = !it.showInFeed)
                                )
                            }
                        }
                        if (updatePosition == null) {
                            val updatePositionFtFromLP =
                                dataRepo.getLPPositionByUnit(action.unit, action.watchList)
                            updatePositionFtFromLP?.let {
                                dataRepo.updatePosition(
                                    it.copy(showInFeed = !it.showInFeed)
                                )

                            }
                        }
                        val updatedWatchlistsWithPositions =
                            dataRepo.watchlistsWithPositions
                        emit(
                            Mutation.WatchlistsWithPositionsChanged(
                                updatedWatchlistsWithPositions
                            )
                        )
                    }

                    is Action.NFTALertChanged -> flow {
                        dataRepo.getNFTPositionBy(action.policy, action.watchList)
                            ?.let { updatePosition ->
                                var showInfeed = true
                                if (!updatePosition.showInFeed) {
                                    val feedAdded = dataRepo.addFeedNFT(
                                        FeedNFT(
                                            updatePosition.policy,
                                            ZonedDateTime.now(),
                                            ZonedDateTime.now(),
                                            true,
                                            false,
                                        )
                                    )
                                    showInfeed = feedAdded
                                }
                                if (showInfeed) {
                                    dataRepo.updatePosition(
                                        updatePosition.copy(showInFeed = !updatePosition.showInFeed)
                                    )
                                }
                            }
                        val updatedWatchlistsWithPositions =
                            dataRepo.watchlistsWithPositions
                        emit(
                            Mutation.WatchlistsWithPositionsChanged(
                                updatedWatchlistsWithPositions
                            )
                        )
                    }

                    is Action.LPALertChanged -> flow {
                        dataRepo.getLPPositionByTicker(action.ticker, action.watchList)
                            ?.let {
                                dataRepo.updatePosition(
                                    it.copy(showInFeed = !it.showInFeed)
                                )
                            }
                        val updatedWatchlistsWithPositions =
                            dataRepo.watchlistsWithPositions
                        emit(
                            Mutation.WatchlistsWithPositionsChanged(
                                updatedWatchlistsWithPositions
                            )
                        )
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.ClockStatusChanged -> previousState.copy(status = mutation.statusResponse)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                    is Mutation.ShowTypeChanged -> previousState.copy(showType = mutation.showType)
                    is Mutation.WatchlistsWithPositionsChanged -> previousState.copy(
                        watchlistsWithPositions = mutation.watchlistsWithPositions
                    )

                    is Mutation.ErrorAddWatchListDuplicateName -> {
                        previousState.copy(
                            errorAddWatchlist = mutation.errorMessage,
                            errorDuplicateName = mutation.errorMessage != null
                        )
                    }

                    is Mutation.ErrorAddWatchListDuplicateAddress -> previousState.copy(
                        errorAddWatchlist = mutation.errorMessage,
                        errorDuplicateAddress = mutation.errorMessage != null
                    )

                    is Mutation.ShowAddWatchlistDialogChanged -> previousState.copy(
                        showAddWatchListDialog = mutation.show
                    )

                    is Mutation.ShowLoading -> previousState.copy(isLoading = mutation.show)
                    is Mutation.ShowReloading -> previousState.copy(reloadingWatchListNumber = mutation.watchListNumber)
                    is Mutation.ResolveErrorChanged -> previousState.copy(resolveError = mutation.errorMessage)
                    is Mutation.ResolvedAddressChanged -> previousState.copy(resolvedAddress = mutation.address)
                    is Mutation.EnteredAddressChanged -> previousState.copy(enteredAddress = mutation.address)
                }
            }
        )
}