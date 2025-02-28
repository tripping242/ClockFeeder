package com.codingblocks.clock.ui.settings

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.FeedCycler
import com.codingblocks.clock.core.local.data.FeedToClockItem
import com.codingblocks.clock.core.local.data.PositionFTLocal
import com.codingblocks.clock.core.local.data.PositionLPLocal
import com.codingblocks.clock.core.local.data.PositionNFTLocal
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class SettingsViewModel(
    private val dataRepo: DataRepo,
    private val feedCycler: FeedCycler,
) : ControllerViewModel<SettingsViewModel.Action, SettingsViewModel.State>() {

    sealed class Action {
        data object LoadAndUpdateFeedFTToClockItems : Action()
        data object LoadAndUpdateFeedNFTToClockItems : Action()
        data object StartClockFeedCycler : Action()
        data object PauseClockFeedCycler : Action()
    }

    sealed class Mutation {
        data class FeedToClockItemsChanged(val items: List<FeedToClockItem>) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
    }

    data class State(
        val feedToClockItems: List<FeedToClockItem> = emptyList(),
        val error: String? = null,
    )

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(),
            mutator = { action ->
                when (action) {
                    is Action.LoadAndUpdateFeedFTToClockItems -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            Timber.tag("wims").i("before")
                            dataRepo.loadAndUpdateFeedFTToClockItems()
                            Timber.tag("wims").i("after")
                            val updated = dataRepo.getAllFeedToClockItems()
                            Timber.tag("wims").i("after updated ${updated.size}")
                            emit(Mutation.FeedToClockItemsChanged(updated))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not load and update FeedFTToClock"))
                            Timber.d("could not load Positions $e")
                        }
                    }
                    is Action.LoadAndUpdateFeedNFTToClockItems -> flow {
                        try {
                            Timber.tag("wims").i("LoadAndUpdateFeedNFTToClockItems")
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.loadAndUpdateFeedNFTToClockItems()
                            val updated = dataRepo.getAllFeedToClockItems()
                            emit(Mutation.FeedToClockItemsChanged(updated))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not load and update FeedNFTToClock"))
                            Timber.d("could not load Positions $e")
                        }
                    }
                    is Action.StartClockFeedCycler -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.pauseBlockClock()
                            feedCycler.startCycling(dataRepo.cyclingDelayMiliseconds)
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not start Cycling the clockFeed"))
                            Timber.d("could not start Cycling $e")
                        }
                    }
                    is Action.PauseClockFeedCycler -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            feedCycler.stopCycling()
                            dataRepo.resumeBlockClock()
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not pause Cycling the clockFeed"))
                            Timber.d("could not pause Cycling $e")
                        }
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.FeedToClockItemsChanged -> previousState.copy(feedToClockItems = mutation.items)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                }
            }
        )
}