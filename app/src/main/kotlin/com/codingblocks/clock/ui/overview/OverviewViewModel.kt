/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Florian Schuster.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codingblocks.clock.ui.overview

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.codingblocks.clock.base.control.ControllerViewModel
import com.codingblocks.clock.core.DataRepo
import com.codingblocks.clock.core.FeedCycler
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class OverviewViewModel(
    private val dataRepo: DataRepo,
    private val feedCycler: FeedCycler,
) : ControllerViewModel<OverviewViewModel.Action, OverviewViewModel.State>() {

    sealed class Action {
        data object Initialize : Action()
    }

    sealed class Mutation {
        data class IsLoadingChanged(val isLoading: Boolean) : Mutation()
    }

    data class State(
        val isLoading: Boolean = false
    )

    override val controller: Controller<Action, State> =
        viewModelScope.createController<Action, Mutation, State>(
            initialState = State(),
            mutator = { action ->
                when (action) {
                    Action.Initialize -> flow {
                        emit(Mutation.IsLoadingChanged(true))
                        try {
                            if (dataRepo.autoReloadPositions) dataRepo.loadPositionsForAllWatchlists()
                            dataRepo.schedulePeriodicFetching()
                            dataRepo.scheduleNFTAlertWorker()
                            dataRepo.scheduleFTAlertWorker()
                            if (dataRepo.autoFeed) feedCycler.startCycling(dataRepo.cyclingDelayMiliseconds)

                        } catch (e: Exception) {
                            Timber.d("could not start workers feeds $e")
                        } finally {
                            emit(Mutation.IsLoadingChanged(false))
                        }
                    }
                }
            },
            reducer = { mutation, previousState ->
                when (mutation) {
                    is Mutation.IsLoadingChanged -> previousState.copy(isLoading = mutation.isLoading)
                }
            }
        )
}
