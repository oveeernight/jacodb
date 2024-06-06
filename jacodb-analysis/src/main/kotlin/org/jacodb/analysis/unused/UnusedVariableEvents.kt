/*
 *  Copyright 2022 UnitTestBot contributors (utbot.org)
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jacodb.analysis.unused

import org.jacodb.analysis.ifds.Edge
import org.jacodb.api.common.CommonMethod
import org.jacodb.api.common.cfg.CommonInst

sealed interface UnusedVariableEvent<Method, Statement>
    where Method : CommonMethod,
          Statement : CommonInst

data class NewSummaryEdge<Method, Statement>(
    val edge: Edge<UnusedVariableDomainFact, Statement>,
) : UnusedVariableEvent<Method, Statement>
    where Method : CommonMethod,
          Statement : CommonInst
