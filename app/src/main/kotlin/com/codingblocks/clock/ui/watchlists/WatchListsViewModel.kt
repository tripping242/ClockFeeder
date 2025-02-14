package com.codingblocks.clock.ui.watchlists

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.model.clock.StatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchListViewModel(
    private val dataRepo: DataRepo
) : ControllerViewModel<WatchListViewModel.Action, WatchListViewModel.State>() {

    sealed class Action {
        data object GetClockStatus : Action()
    }

    sealed class Mutation {
        data class StatusChanged(val statusResponse: StatusResponse) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
    }

    data class State(
        val status: StatusResponse? = null,
        val error: String? = null,
    )

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(),
            mutator = { action ->
                when (action) {
                    Action.GetClockStatus -> flow {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                emit(Mutation.ErrorChanged(null))
                                dataRepo.getClockStatus()
                                    .onSuccess { emit(Mutation.StatusChanged(it)) }.onFailure {
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
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.StatusChanged -> previousState.copy(status = mutation.statusResponse)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                }
            }
        )
}