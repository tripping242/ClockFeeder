/*
 * Copyright 2020 Tailored Media GmbH.
 * Created by Stefan Geyer.
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

package com.codingblocks.clock.base.control

import at.florianschuster.control.EffectController
import kotlinx.coroutines.flow.Flow

abstract class EffectControllerViewModel<Action, State, Effect> :
    ControllerViewModel<Action, State>() {
    abstract override val controller: EffectController<Action, State, Effect>

    val effects: Flow<Effect> get() = controller.effects
}
