package com.codingblocks.clock.ui.feeds

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.local.data.CustomFTAlert
import com.codingblocks.clock.core.local.data.CustomNFTAlert
import com.codingblocks.clock.core.local.data.FeedFT
import com.codingblocks.clock.core.local.data.FeedFTWithAlerts
import com.codingblocks.clock.core.local.data.FeedNFT
import com.codingblocks.clock.core.local.data.FeedNFTWithAlerts
import com.codingblocks.clock.core.model.taptools.PositionsResponse
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class FeedsViewModel(
    private val dataRepo: DataRepo
) : ControllerViewModel<FeedsViewModel.Action, FeedsViewModel.State>() {

    enum class ShowType {
        FT, NFT
    }
    enum class AlertValue(val label: String) {
        Price("PRICE"),
        Volume("VOLUME")
    }
    enum class AlertTrigger(val label: String) {
        Once("Only once"),
        Volume("Every time")
    }
    enum class AlertType(val label: String) {
        Push("Push notification"),
        Clock("Alert to BLockClock")
    }

    sealed class FeedItem {
        data class FT(val feedFT: FeedFT)
        data class NFT(val feedNFT: FeedNFT)
    }

    sealed class Action {
        data object Initialize : Action()
        data class DeleteFeedFTItem(val item: FeedFTWithAlerts) : Action()
        data class FeedClockPriceFTChanged(val item: FeedFTWithAlerts) : Action()
        data class FeedClockVolumeFTChanged(val item: FeedFTWithAlerts) : Action()
        data class DeleteFeedNFTItem(val item: FeedNFTWithAlerts) : Action()
        data class FeedClockPriceNFTChanged(val item: FeedNFTWithAlerts) : Action()
        data class FeedClockVolumeNFTChanged(val item: FeedNFTWithAlerts) : Action()
        data class ShowAddAlertDialog(val show: Boolean) : Action()
        data class AddFTAlert(val alert: CustomFTAlert) : Action()
        data class DeleteFTAlert(val alert: CustomFTAlert) : Action()
        data class AddNFTAlert(val alert: CustomNFTAlert) : Action()
        data class DeleteNFTAlert(val alert: CustomNFTAlert) : Action()
    }

    sealed class Mutation {
        data class FeedFTWithAlertsChanged(val feedsWithAlerts: List<FeedFTWithAlerts>) : Mutation()
        data class FeedNFTWithAlertsChanged(val feedsWithAlerts: List<FeedNFTWithAlerts>) : Mutation()
        data class ShowAddAlertDialog(val show: Boolean) : Mutation()
        data class ErrorChanged(val errorMessage: String?) : Mutation()
    }

    data class State(
        val feedFTWithAlerts: List<FeedFTWithAlerts> = emptyList(),
        val feedNFTWithAlerts: List<FeedNFTWithAlerts> = emptyList(),
        val positions: PositionsResponse? = null,
        val showAddAlertDialog: Boolean = false,
        val error: String? = null,
    )

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(),
            mutator = { action ->
                when (action) {
                    Action.Initialize -> flow {
                        try {
                            emit(Mutation.ErrorChanged(null))
                            val feedFTWithAlert = dataRepo.feedFTWithAlerts
                            val feedNFTWithAlerts = dataRepo.feedsNFTWithAlerts
                            emit(Mutation.FeedFTWithAlertsChanged(feedFTWithAlert))
                            emit(Mutation.FeedNFTWithAlertsChanged(feedNFTWithAlerts))
                        } catch (e: Exception) {
                            Timber.d("could not retreive feeds $e")
                        }
                    }

                    is Action.DeleteFeedFTItem -> flow {
                        try {
                            val feedWithAlerts = action.item
                            dataRepo.deleteAlertsForFeedWithUnit(feedWithAlerts.feedFT)
                            dataRepo.deleteFeedFT(feedWithAlerts.feedFT)
                        } catch (e: Exception) {
                            Timber.d("could not delete feed with alerts $e")
                        }
                    }
                    is Action.DeleteFeedNFTItem -> flow {
                        try {
                            val feedWithAlerts = action.item
                            dataRepo.deleteAlertsForFeedWithPolicy(feedWithAlerts.feedNFT.positionPolicy)
                            dataRepo.deleteFeedNFT(feedWithAlerts.feedNFT)
                        } catch (e: Exception) {
                            Timber.d("could not delete feed with alerts $e")
                        }
                    }
                    is Action.FeedClockPriceFTChanged -> flow {
                        try {
                            val feedFT = action.item.feedFT
                            dataRepo.updateFeedFT(
                                feedFT.copy(feedClockPrice = !feedFT.feedClockPrice)
                            )
                        } catch (e: Exception) {
                            Timber.d("could not change feedClockPrice $e")
                        }
                    }
                    is Action.FeedClockPriceNFTChanged -> flow {
                        try {
                            val feedNFT = action.item.feedNFT
                            dataRepo.updateFeedNFT(
                                feedNFT.copy(feedClockPrice = !feedNFT.feedClockPrice)
                            )
                        } catch (e: Exception) {
                            Timber.d("could not change feedClockPrice $e")
                        }
                    }
                    is Action.FeedClockVolumeFTChanged -> flow {
                        try {
                            val feedFT = action.item.feedFT
                            dataRepo.updateFeedFT(
                                feedFT.copy(feedClockVolume = !feedFT.feedClockVolume)
                            )
                        } catch (e: Exception) {
                            Timber.d("could not change feedClockVolume $e")
                        }
                    }
                    is Action.FeedClockVolumeNFTChanged -> flow {
                        try {
                            val feedNFT = action.item.feedNFT
                            dataRepo.updateFeedNFT(
                                feedNFT.copy(feedClockVolume = !feedNFT.feedClockVolume)
                            )
                        } catch (e: Exception) {
                            Timber.d("could not change feedClockVolume $e")
                        }
                    }
                    is Action.ShowAddAlertDialog -> flow {
                        emit(Mutation.ShowAddAlertDialog(action.show))
                    }

                    is Action.AddFTAlert -> flow {
                        try {
                            val alert = action.alert
                            dataRepo.addAlertForUnit(alert)
                            val feedFTWithAlert = dataRepo.feedFTWithAlerts
                            emit(Mutation.FeedFTWithAlertsChanged(feedFTWithAlert))
                        } catch (e: Exception) {
                            Timber.d("could not add alert $e")
                        }
                    }
                    is Action.DeleteFTAlert -> flow {
                        try {
                            val alert = action.alert
                            dataRepo.deleteAlert(alert)
                            val feedFTWithAlerts = dataRepo.feedFTWithAlerts
                            emit(Mutation.FeedFTWithAlertsChanged(feedFTWithAlerts))
                        } catch (e: Exception) {
                            Timber.d("could not delete alert $e")
                        }
                    }
                    is Action.AddNFTAlert -> flow {
                        try {
                            val alert = action.alert
                            dataRepo.addAlertForPolicy(alert)
                            val feedNFTWithAlerts = dataRepo.feedsNFTWithAlerts
                            emit(Mutation.FeedNFTWithAlertsChanged(feedNFTWithAlerts))
                        } catch (e: Exception) {
                            Timber.d("could not add alert $e")
                        }
                    }

                    is Action.DeleteNFTAlert -> flow {
                        try {
                            val alert = action.alert
                            dataRepo.deleteAlert(alert)
                            val feedNFTWithAlerts = dataRepo.feedsNFTWithAlerts
                            emit(Mutation.FeedNFTWithAlertsChanged(feedNFTWithAlerts))
                        } catch (e: Exception) {
                            Timber.d("could not delete alert $e")
                        }
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.ErrorChanged -> previousState.copy(error = mutation.errorMessage)
                    is Mutation.ShowAddAlertDialog -> previousState.copy(
                        showAddAlertDialog = mutation.show
                    )
                    is Mutation.FeedFTWithAlertsChanged -> previousState.copy(feedFTWithAlerts = mutation.feedsWithAlerts)
                    is Mutation.FeedNFTWithAlertsChanged -> previousState.copy(feedNFTWithAlerts = mutation.feedsWithAlerts)
                }
            }
        )
}