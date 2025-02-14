package com.codingblocks.clock.ui.feeds

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.model.taptools.PositionsResponse
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class FeedsViewModel(
    private val dataRepo: DataRepo
) : ControllerViewModel<FeedsViewModel.Action, FeedsViewModel.State>() {

    sealed class Action {
        data object GetPositions : Action()
    }

    sealed class Mutation {
        data class PositionsChanged(val positionsResponse: PositionsResponse) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
    }

    data class State(
        val positions: PositionsResponse? = null,
        val error: String? = null,
    )

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(),
            mutator = { action ->
                when (action) {
                    Action.GetPositions -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.getPositionsForAddress("stake1uy368slqls4u66g0502krmyasfdke3elfh0z6qgczmylx7ce6frnl")
                                .onSuccess { emit(Mutation.PositionsChanged(it))  }
                                .onFailure { emit(Mutation.ErrorChanged("could not retreive Positions:\n$it")) }
                        } catch (e: Exception) {
                            Timber.d("could not retreive Positions $e")
                        }
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.PositionsChanged -> previousState.copy(positions = mutation.positionsResponse)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                }
            }
        )
}