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

package com.tailoredapps.androidapptemplate.ui.overview

import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import com.tailoredapps.androidapptemplate.base.control.ControllerViewModel
import com.tailoredapps.androidapptemplate.core.DataRepo

class OverviewViewModel(
    private val dataRepo: DataRepo
) : ControllerViewModel<OverviewViewModel.Action, OverviewViewModel.State>() {

    sealed class Action

    sealed class Mutation

    object State

    override val controller: Controller<Action, State> = viewModelScope.createController<Action, Mutation, State>(
        initialState = State
    )
}