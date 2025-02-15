package com.codingblocks.clock.ui.settings

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class SettingsViewModel(
    private val dataRepo: DataRepo
) : ControllerViewModel<SettingsViewModel.Action, SettingsViewModel.State>() {

    enum class ShowType {
        FT, NFT, LP
    }

    sealed class Action {
        data object GetPositionsFT : Action()
        data object GetPositionsNFT : Action()
        data object GetPositionsLP : Action()
    }

    sealed class Mutation {
        data class PositionsFTChanged(val positions: List<PositionFTLocal>) : Mutation()
        data class PositionsNFTChanged(val positions: List<PositionNFTLocal>) : Mutation()
        data class PositionsLPChanged(val positions: List<PositionLPLocal>) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
        data class ShowTypeChanged(val showType: ShowType) : Mutation()
    }

    data class State(
        val positionsFT: List<PositionFTLocal> = emptyList(),
        val positionsNFT: List<PositionNFTLocal> = emptyList(),
        val positionsLP: List<PositionLPLocal> = emptyList(),
        val showType: ShowType = ShowType.FT,
        val error: String? = null,
    )

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(),
            mutator = { action ->
                when (action) {
                    is Action.GetPositionsFT -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.getFTPositionsForWatchlist().let { emit(Mutation.PositionsFTChanged(it)) }
                            emit(Mutation.ShowTypeChanged(ShowType.FT))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not retrieve positions ${e.message}"))
                            Timber.d("could not retrieve Positions $e")
                        }
                    }
                    is Action.GetPositionsNFT -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.getNFTPositionsForWatchlist().let {
                                emit(Mutation.PositionsNFTChanged(it)) }
                            emit(Mutation.ShowTypeChanged(ShowType.NFT))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not retrieve positions ${e.message}"))
                            Timber.d("could not retrieve Positions $e")
                        }
                    }
                    is Action.GetPositionsLP -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.getLPPositionsForWatchlist().let {
                                emit(Mutation.PositionsLPChanged(it)) }
                            emit(Mutation.ShowTypeChanged(ShowType.LP))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not retrieve positions ${e.message}"))
                            Timber.d("could not retrieve Positions $e")
                        }
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.PositionsFTChanged -> previousState.copy(positionsFT = mutation.positions)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                    is Mutation.PositionsNFTChanged -> previousState.copy(positionsNFT = mutation.positions)
                    is Mutation.PositionsLPChanged -> previousState.copy(positionsLP = mutation.positions)
                    is Mutation.ShowTypeChanged -> previousState.copy(showType = mutation.showType)
                }
            }
        )
}