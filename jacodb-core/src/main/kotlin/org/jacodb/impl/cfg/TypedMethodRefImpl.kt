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

package org.jacodb.impl.cfg

import org.jacodb.api.*
import org.jacodb.api.jvm.cfg.*
import org.jacodb.api.jvm.MethodNotFoundException
import org.jacodb.api.jvm.cfg.JcRawCallExpr
import org.jacodb.api.jvm.cfg.JcRawInstanceExpr
import org.jacodb.api.jvm.cfg.JcRawLocal
import org.jacodb.api.jvm.cfg.JcRawSpecialCallExpr
import org.jacodb.api.jvm.cfg.JcRawStaticCallExpr
import org.jacodb.api.jvm.ext.findType
import org.jacodb.api.jvm.ext.jvmName
import org.jacodb.impl.cfg.util.typeName
import org.jacodb.impl.softLazy
import org.jacodb.impl.weakLazy
import org.jacodb.api.jvm.JcClassType
import org.jacodb.api.jvm.JcProject
import org.jacodb.api.jvm.JcType
import org.jacodb.api.jvm.JcTypedMethod
import org.jacodb.api.core.TypeName
import org.jacodb.api.jvm.JcMethod
import org.jacodb.api.jvm.cfg.JcInstLocation
import org.jacodb.api.jvm.cfg.TypedMethodRef
import org.jacodb.api.jvm.cfg.VirtualTypedMethodRef
import org.objectweb.asm.Type

abstract class MethodSignatureRef(
    val type: JcClassType,
    override val name: String,
    argTypes: List<TypeName>,
    returnType: TypeName,
) : TypedMethodRef {

    protected val description: String = buildString {
        append("(")
        argTypes.forEach {
            append(it.typeName.jvmName())
        }
        append(")")
        append(returnType.typeName.jvmName())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MethodSignatureRef) return false

        if (type != other.type) return false
        if (name != other.name) return false
        return description == other.description
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    protected val methodNotFoundMessage: String
        get() {
            return type.methodNotFoundMessage
        }

    protected val JcType.methodNotFoundMessage: String
        get() {
            val argumentTypes = Type.getArgumentTypes(description).map { it.descriptor.typeName() }
            return buildString {
                append("Can't find method '")
                append(typeName)
                append("#")
                append(name)
                append("(")
                argumentTypes.joinToString(", ") { it.typeName }
                append(")'")
            }
        }

    fun JcType.throwNotFoundException(): Nothing {
        throw MethodNotFoundException(this.methodNotFoundMessage)
    }

}

class TypedStaticMethodRefImpl(
    type: JcClassType,
    name: String,
    argTypes: List<TypeName>,
    returnType: TypeName
) : MethodSignatureRef(type, name, argTypes, returnType) {

    constructor(classpath: JcProject, raw: JcRawStaticCallExpr) : this(
            classpath.findType(raw.declaringClass.typeName) as JcClassType,
            raw.methodName,
            raw.argumentTypes,
            raw.returnType
    )

    override val method: JcTypedMethod by weakLazy {
        type.lookup.staticMethod(name, description) ?: type.throwNotFoundException()
    }
}

class TypedSpecialMethodRefImpl(
    type: JcClassType,
    name: String,
    argTypes: List<TypeName>,
    returnType: TypeName
) : MethodSignatureRef(type, name, argTypes, returnType) {

    constructor(classpath: JcProject, raw: JcRawSpecialCallExpr) : this(
            classpath.findType(raw.declaringClass.typeName) as JcClassType,
            raw.methodName,
            raw.argumentTypes,
            raw.returnType
    )

    override val method: JcTypedMethod by weakLazy {
        type.lookup.specialMethod(name, description) ?: type.throwNotFoundException()
    }

}

class VirtualMethodRefImpl(
    type: JcClassType,
    private val actualType: JcClassType,
    name: String,
    argTypes: List<TypeName>,
    returnType: TypeName
) : MethodSignatureRef(type, name, argTypes, returnType), VirtualTypedMethodRef {

    companion object {
        private fun JcRawCallExpr.resolvedType(classpath: JcProject): Pair<JcClassType, JcClassType> {
            val declared = classpath.findType(declaringClass.typeName) as JcClassType
            if (this is JcRawInstanceExpr) {
                val instance = instance
                if (instance is JcRawLocal) {
                    val actualType = classpath.findTypeOrNull(instance.typeName.typeName)
                    if (actualType is JcClassType) {
                        return declared to actualType
                    }
                }
            }
            return declared to declared
        }

        fun of(classpath: JcProject, raw: JcRawCallExpr): VirtualMethodRefImpl {
            val (declared, actual) = raw.resolvedType(classpath)
            return VirtualMethodRefImpl(
                    declared,
                    actual,
                    raw.methodName,
                    raw.argumentTypes,
                    raw.returnType
            )
        }

        fun of(type: JcClassType, method: JcTypedMethod): VirtualMethodRefImpl {
            return VirtualMethodRefImpl(
                    type, type,
                    method.name,
                    method.method.parameters.map { it.type },
                    method.method.returnType
            )
        }
    }

    override val method: JcTypedMethod by softLazy {
        actualType.lookup.method(name, description) ?: declaredMethod
    }

    override val declaredMethod: JcTypedMethod by softLazy {
        type.lookup.method(name, description) ?: type.throwNotFoundException()
    }
}


class TypedMethodRefImpl(
    type: JcClassType,
    name: String,
    argTypes: List<TypeName>,
    returnType: TypeName
) : MethodSignatureRef(type, name, argTypes, returnType) {

    constructor(classpath: JcProject, raw: JcRawCallExpr) : this(
            classpath.findType(raw.declaringClass.typeName) as JcClassType,
            raw.methodName,
            raw.argumentTypes,
            raw.returnType
    )

    override val method: JcTypedMethod by softLazy {
        type.lookup.method(name, description) ?: type.throwNotFoundException()
    }

}

fun JcProject.methodRef(expr: JcRawCallExpr): TypedMethodRef {
    return when (expr) {
        is JcRawStaticCallExpr -> TypedStaticMethodRefImpl(this, expr)
        is JcRawSpecialCallExpr -> TypedSpecialMethodRefImpl(this, expr)
        else -> TypedMethodRefImpl(this, expr)
    }
}

fun JcTypedMethod.methodRef(): TypedMethodRef {
    return TypedMethodRefImpl(
            enclosingType as JcClassType,
            method.name,
            method.parameters.map { it.type },
            method.returnType
    )
}

class JcInstLocationImpl(
    override val method: JcMethod,
    override val index: Int,
    override val lineNumber: Int
) : JcInstLocation {

    override fun toString(): String {
        return "${method.enclosingClass.name}#${method.name}:$lineNumber"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JcInstLocationImpl

        if (index != other.index) return false
        return method == other.method
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + method.hashCode()
        return result
    }


}