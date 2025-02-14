package com.codingblocks.clock.ui.settings

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.local.data.PositionFT
import com.codingblocks.clock.core.local.data.PositionNFT
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class SettingsViewModel(
    private val dataRepo: DataRepo
) : ControllerViewModel<SettingsViewModel.Action, SettingsViewModel.State>() {

    sealed class Action {
        data object GetPositionsFT : Action()
        data object GetPositionsNFT : Action()
    }

    sealed class Mutation {
        data class PositionsFTChanged(val positions: List<PositionFT>) : Mutation()
        data class PositionsNFTChanged(val positions: List<PositionNFT>) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
        data class SetShowFT(val show: Boolean) : Mutation()
    }

    data class State(
        val positionsFT: List<PositionFT> = emptyList(),
        val positionsNFT: List<PositionNFT> = emptyList(),
        val showFT: Boolean = true,
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
                            emit(Mutation.SetShowFT(true))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not retrieve positions ${e.message}"))
                            Timber.d("could not retreive Positions $e")
                        }
                    }
                    is Action.GetPositionsNFT -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.getNFTPositionsForWatchlist().let {
                                Timber.tag("wims").i("got ${it.size} NFTS")
                                emit(Mutation.PositionsNFTChanged(it)) }
                            emit(Mutation.SetShowFT(false))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not retrieve positions ${e.message}"))
                            Timber.d("could not retreive Positions $e")
                        }
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.PositionsFTChanged -> previousState.copy(positionsFT = mutation.positions)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                    is Mutation.PositionsNFTChanged -> previousState.copy(positionsNFT = mutation.positions)
                    is Mutation.SetShowFT -> previousState.copy(showFT = mutation.show)
                }
            }
        )
}