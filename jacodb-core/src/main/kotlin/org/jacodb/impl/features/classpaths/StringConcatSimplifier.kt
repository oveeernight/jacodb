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

package org.jacodb.impl.features.classpaths

import org.jacodb.api.jvm.JcInstExtFeature
import org.jacodb.api.jvm.JcMethod
import org.jacodb.api.jvm.JcMethodExtFeature
import org.jacodb.api.jvm.cfg.JcInst
import org.jacodb.api.core.cfg.InstList
import org.jacodb.impl.analysis.impl.StringConcatSimplifierTransformer

object StringConcatSimplifier : JcInstExtFeature, JcMethodExtFeature {

    override fun transformInstList(method: JcMethod, list: InstList<JcInst>): InstList<JcInst> {
        return StringConcatSimplifierTransformer(method.enclosingClass.classpath, list).transform()
    }

}