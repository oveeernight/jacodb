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

package org.jacodb.panda.staticvm.cfg

import org.jacodb.panda.staticvm.classpath.PandaMethod
import org.jacodb.panda.staticvm.classpath.PandaType
import org.jacodb.panda.staticvm.ir.*

sealed interface LocalVarNode {
    val name: String
}
open class LeafVarNode(override val name: String, val type: PandaType) : LocalVarNode

class DependentVarNode(override val name: String, val bounds: List<String>) : LocalVarNode

class LoadArrayNode(override val name: String, val array: String) : LocalVarNode

class ThisNode(name: String, type: PandaType) : LeafVarNode(name, type)

class OutputVarBuilder(private val method: PandaMethod) : PandaInstIrVisitor<LocalVarNode?> {
    private val project = method.enclosingClass.project

    private fun default(inst: PandaInstIr) = LeafVarNode(inst.id, project.findType(inst.type))

    override fun visitPandaConstantInstInfo(inst: PandaConstantInstIr): LocalVarNode =
        LeafVarNode(inst.id, project.findType(inst.type))

    override fun visitPandaSafePointInstInfo(inst: PandaSafePointInstIr): LocalVarNode? = null

    override fun visitPandaSaveStateInstInfo(inst: PandaSaveStateInstIr): LocalVarNode? = null

    override fun visitPandaNewObjectInstInfo(inst: PandaNewObjectInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.findClass(inst.objectClass).type)

    override fun visitPandaNewArrayInstInfo(inst: PandaNewArrayInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.findType(inst.arrayType))

    override fun visitPandaCallStaticInstInfo(inst: PandaCallStaticInstIr): LocalVarNode? {
        val returnType = requireNotNull(project.findMethod(inst.method)).returnType
        return LeafVarNode(inst.id, returnType)
    }

    override fun visitPandaNullCheckInstInfo(inst: PandaNullCheckInstIr): LocalVarNode? =
        DependentVarNode(inst.id, inst.inputs.dropLast(1))

    override fun visitPandaZeroCheckInstInfo(inst: PandaZeroCheckInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.findType(inst.type))

    override fun visitPandaLoadStringInstInfo(inst: PandaLoadStringInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.stringClass.type)

    override fun visitPandaCallVirtualInstInfo(inst: PandaCallVirtualInstIr): LocalVarNode? {
        val returnType = requireNotNull(project.findMethod(inst.method)).returnType
        return LeafVarNode(inst.id, returnType)
    }

    override fun visitPandaLoadAndInitClassInstInfo(inst: PandaLoadAndInitClassInstIr): LocalVarNode? = null

    override fun visitPandaLoadClassInstInfo(inst: PandaLoadClassInstIr): LocalVarNode? = null

    override fun visitPandaInitClassInstInfo(inst: PandaInitClassInstIr): LocalVarNode? = null

    override fun visitPandaReturnVoidInstInfo(inst: PandaReturnVoidInstIr): LocalVarNode? = null

    override fun visitPandaReturnInstInfo(inst: PandaReturnInstIr): LocalVarNode? = null

    override fun visitPandaParameterInstInfo(inst: PandaParameterInstIr): LocalVarNode? =
        if (inst.index == 0 && !method.flags.isStatic)
            ThisNode(inst.id, method.parameterTypes[inst.index])
        else
            LeafVarNode(inst.id, method.parameterTypes[inst.index])

    override fun visitPandaLoadStaticInstInfo(inst: PandaLoadStaticInstIr): LocalVarNode? {
        val enclosingClass = project.findClass(inst.enclosingClass)
        val field = requireNotNull(enclosingClass.findFieldOrNull(inst.field))
        return LeafVarNode(inst.id, field.type)
    }

    override fun visitPandaLoadObjectInstInfo(inst: PandaLoadObjectInstIr): LocalVarNode? {
        val enclosingClass = project.findClass(inst.enclosingClass)
        val field = requireNotNull(enclosingClass.findFieldOrNull(inst.field))
        return LeafVarNode(inst.id, field.type)
    }

    override fun visitPandaStoreStaticInstInfo(inst: PandaStoreStaticInstIr): LocalVarNode? = null

    override fun visitPandaStoreObjectInstInfo(inst: PandaStoreObjectInstIr): LocalVarNode? = null

    override fun visitPandaLoadArrayInstInfo(inst: PandaLoadArrayInstIr): LocalVarNode? =
        LoadArrayNode(inst.id, inst.inputs.first())

    override fun visitPandaStoreArrayInstInfo(inst: PandaStoreArrayInstIr): LocalVarNode? = null

    override fun visitPandaCastInstInfo(inst: PandaCastInstIr): LocalVarNode? = default(inst)

    override fun visitPandaIsInstanceInstInfo(inst: PandaIsInstanceInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.findType("u1"))
    // TODO: refactor

    override fun visitPandaCheckCastInstInfo(inst: PandaCheckCastInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.findType(inst.candidateType))

    override fun visitPandaIfImmInstInfo(inst: PandaIfImmInstIr): LocalVarNode? = null

    override fun visitPandaCompareInstInfo(inst: PandaCompareInstIr): LocalVarNode? = default(inst)

    override fun visitPandaPhiInstInfo(inst: PandaPhiInstIr): LocalVarNode? =
        DependentVarNode(inst.id, inst.inputs).takeIf { inst.users.isNotEmpty() }

    override fun visitPandaAddInstInfo(inst: PandaAddInstIr): LocalVarNode? = default(inst)

    override fun visitPandaSubInstInfo(inst: PandaSubInstIr): LocalVarNode? = default(inst)

    override fun visitPandaMulInstInfo(inst: PandaMulInstIr): LocalVarNode? = default(inst)

    override fun visitPandaDivInstInfo(inst: PandaDivInstIr): LocalVarNode? = default(inst)

    override fun visitPandaModInstInfo(inst: PandaModInstIr): LocalVarNode? = default(inst)

    override fun visitPandaAndInstInfo(inst: PandaAndInstIr): LocalVarNode? = default(inst)

    override fun visitPandaOrInstInfo(inst: PandaOrInstIr): LocalVarNode? = default(inst)

    override fun visitPandaXorInstInfo(inst: PandaXorInstIr): LocalVarNode? = default(inst)

    override fun visitPandaShlInstInfo(inst: PandaShlInstIr): LocalVarNode? = default(inst)

    override fun visitPandaShrInstInfo(inst: PandaShrInstIr): LocalVarNode? = default(inst)

    override fun visitPandaAShlInstInfo(inst: PandaAShlInstIr): LocalVarNode? = default(inst)

    override fun visitPandaAShrInstInfo(inst: PandaAShrInstIr): LocalVarNode? = default(inst)

    override fun visitPandaCmpInstInfo(inst: PandaCmpInstIr): LocalVarNode? = default(inst)

    override fun visitPandaThrowInstInfo(inst: PandaThrowInstIr): LocalVarNode? = null

    override fun visitPandaNegativeCheckInstInfo(inst: PandaNegativeCheckInstIr): LocalVarNode? = default(inst)

    override fun visitPandaSaveStateDeoptimizeInstInfo(inst: PandaSaveStateDeoptimizeInstIr): LocalVarNode? = null

    override fun visitPandaNegInstInfo(inst: PandaNegInstIr): LocalVarNode? = default(inst)

    override fun visitPandaNotInstInfo(inst: PandaNotInstIr): LocalVarNode? = default(inst)

    override fun visitPandaLenArrayInstInfo(inst: PandaLenArrayInstIr): LocalVarNode? = default(inst)

    override fun visitPandaBoundsCheckInstInfo(inst: PandaBoundsCheckInstIr): LocalVarNode? = default(inst)

    override fun visitPandaNullPtrInstInfo(inst: PandaNullPtrInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.findType("std.core.Object"))
    // TODO: refactor

    override fun visitPandaLoadUndefinedInstInfo(inst: PandaLoadUndefinedInstIr): LocalVarNode? =
        LeafVarNode(inst.id, project.findType("std.core.UndefinedType"))
    // TODO: refactor

    override fun visitPandaRefTypeCheckInstInfo(inst: PandaRefTypeCheckInstIr): LocalVarNode? =
        DependentVarNode(inst.id, listOf(inst.inputs.first()))

    override fun visitPandaTryInstInfo(inst: PandaTryInstIr): LocalVarNode? = null

    override fun visitPandaCatchPhiInstInfo(inst: PandaCatchPhiInstIr): LocalVarNode? =
        DependentVarNode(inst.id, inst.inputs).takeIf { inst.users.isNotEmpty() }
}