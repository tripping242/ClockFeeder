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
        data class AutoFeedChanged(val bool: Boolean) : Action()
        data class AutoReloadPositionsChanged(val bool: Boolean) : Action()
        data class SmallTrendChanged(val percent: Double) : Action()
        data class HighTrendChanged(val percent: Double) : Action()
    }

    sealed class Mutation {
        data class FeedToClockItemsChanged(val items: List<FeedToClockItem>) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
        data class AutoFeedChanged(val bool: Boolean) : Mutation()
        data class AutoReloadPositionsChanged(val bool: Boolean) : Mutation()
        data class SmallTrendChanged(val percent: Double) : Mutation()
        data class HighTrendChanged(val percent: Double) : Mutation()
        data class ChangeCyclingState(val bool: Boolean) : Mutation()
    }

    data class State(
        val feedToClockItems: List<FeedToClockItem> = emptyList(),
        val error: String? = null,
        val autoFeed: Boolean,
        val autoReloadPositions: Boolean,
        val smallTrendPercent: Double,
        val highTrendPercent: Double,
        val isCycling: Boolean,
    )

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(
                autoFeed = dataRepo.autoFeed,
                autoReloadPositions = dataRepo.autoReloadPositions,
                smallTrendPercent = dataRepo.smallTrendPercent,
                highTrendPercent = dataRepo.highTrendPercent,
                isCycling = feedCycler.isCycling()
            ),
            mutator = { action ->
                when (action) {
                    is Action.LoadAndUpdateFeedFTToClockItems -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            dataRepo.loadAndUpdateFeedFTToClockItems()
                            dataRepo.loadAndUpdateFeedNFTToClockItems()
                            val updated = dataRepo.getAllFeedToClockItems()
                            emit(Mutation.FeedToClockItemsChanged(updated))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not load and update FeedToClock items"))
                            Timber.d("could not load Positions $e")
                        }
                    }

                    is Action.LoadAndUpdateFeedNFTToClockItems -> flow {
                        try {
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
                            emit(Mutation.ChangeCyclingState(true))
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not start Cycling the clockFeed"))
                            Timber.d("could not start Cycling $e")
                        }
                    }

                    is Action.PauseClockFeedCycler -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            feedCycler.stopCycling()
                            emit(Mutation.ChangeCyclingState(false))
                            dataRepo.resumeBlockClock()
                        } catch (e: Exception) {
                            emit(Mutation.ErrorChanged("could not pause Cycling the clockFeed"))
                            Timber.d("could not pause Cycling $e")
                        }
                    }

                    is Action.AutoFeedChanged -> flow {
                        dataRepo.setAutoFeed(action.bool)
                        emit(Mutation.AutoFeedChanged(action.bool))
                    }

                    is Action.AutoReloadPositionsChanged -> flow {
                        dataRepo.setAutoReloadPositions(action.bool)
                        emit(Mutation.AutoReloadPositionsChanged(action.bool))
                    }

                    is Action.HighTrendChanged -> flow {
                        dataRepo.setHighTrendPercent(action.percent)
                        emit(Mutation.HighTrendChanged(action.percent))
                    }
                    is Action.SmallTrendChanged -> flow {
                        dataRepo.setSmallTrendPercent(action.percent)
                        emit(Mutation.SmallTrendChanged(action.percent))
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.FeedToClockItemsChanged -> previousState.copy(feedToClockItems = mutation.items)
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                    is Mutation.AutoFeedChanged -> previousState.copy(autoFeed = mutation.bool)
                    is Mutation.AutoReloadPositionsChanged -> previousState.copy(autoReloadPositions = mutation.bool)
                    is Mutation.HighTrendChanged -> previousState.copy(highTrendPercent = mutation.percent)
                    is Mutation.SmallTrendChanged -> previousState.copy(smallTrendPercent = mutation.percent)
                    is Mutation.ChangeCyclingState -> previousState.copy(isCycling = mutation.bool)
                }
            }
        )
}