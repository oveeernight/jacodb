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

package org.jacodb.api.net.core

import org.jacodb.api.net.ilinstances.*

interface IlConstVisitor<out T> {
    fun visitIlNullConst(const: IlNull): T
    fun visitIlBoolConst(const: IlBoolConst): T
    fun visitIlStringConst(const: IlStringConst): T

    fun visitIlInt8Const(const: IlInt8Const): T
    fun visitIlInt16Const(const: IlInt16Const): T
    fun visitIlInt32Const(const: IlInt32Const): T
    fun visitIlInt64Const(const: IlInt64Const): T
    fun visitIlUInt32Const(const: IlUint8Const): T
    fun visitIlUInt32Const(const: IlUint16Const): T
    fun visitIlUInt32Const(const: IlUint32Const): T
    fun visitIlUInt32Const(const: IlInt64Const): T

    fun visitIlFloatConst(const: IlFloatConst): T
    fun visitIlDoubleConst(const: IlDoubleConst): T
    fun visitIlTypeRefConst(const: IlTypeRef): T
    fun visitIlMethodRefConst(const: IlMethodRef): T
    fun visitIlFieldRefConst(const: IlFieldRef): T
    fun visitIlArrayConst(const: IlArrayConst): T
    
    interface Default<out T> : IlConstVisitor<T> {
        fun visitDefault(value: IlValue): T

        override fun visitIlNullConst(const: IlNull): T = visitDefault(const)
        override fun visitIlBoolConst(const: IlBoolConst): T = visitDefault(const)
        override fun visitIlStringConst(const: IlStringConst): T = visitDefault(const)
        override fun visitIlInt8Const(const: IlInt8Const): T = visitDefault(const)
        override fun visitIlInt16Const(const: IlInt16Const): T = visitDefault(const)
        override fun visitIlInt32Const(const: IlInt32Const): T = visitDefault(const)
        override fun visitIlInt64Const(const: IlInt64Const): T = visitDefault(const)
        override fun visitIlUInt32Const(const: IlUint8Const): T = visitDefault(const)
        override fun visitIlUInt32Const(const: IlUint16Const): T = visitDefault(const)
        override fun visitIlUInt32Const(const: IlUint32Const): T = visitDefault(const)
        override fun visitIlUInt32Const(const: IlInt64Const): T = visitDefault(const)
        override fun visitIlFloatConst(const: IlFloatConst): T = visitDefault(const)
        override fun visitIlDoubleConst(const: IlDoubleConst): T = visitDefault(const)
        override fun visitIlTypeRefConst(const: IlTypeRef): T = visitDefault(const)
        override fun visitIlMethodRefConst(const: IlMethodRef): T = visitDefault(const)
        override fun visitIlFieldRefConst(const: IlFieldRef): T = visitDefault(const)
        override fun visitIlArrayConst(const: IlArrayConst): T = visitDefault(const)
    }
}

interface IlExprVisitor<out T> : IlConstVisitor<T> {
    fun visitIlUnaryOp(expr: IlUnaryOp): T
    fun visitIlBinaryOp(expr: IlBinaryOp): T
    fun visitIlArrayLength(expr: IlArrayLengthExpr): T
    fun visitIlCall(expr: IlCall): T
    fun visitIlInitExpr(expr: IlInitExpr): T
    fun visitIlNewArrayExpr(expr: IlNewArrayExpr): T
    fun visitIlNewExpr(expr: IlNewExpr): T
    fun visitIlSizeOfExpr(expr: IlSizeOfExpr): T
    fun visitIlStackAllocExpr(expr: IlStackAllocExpr): T
    fun visitIlManagedRefExpr(expr: IlManagedRefExpr): T
    fun visitIlUnmanagedRefExpr(expr: IlUnmanagedRefExpr): T
    fun visitIlManagedDerefExpr(expr: IlManagedDerefExpr): T
    fun visitIlUnmanagedDerefExpr(expr: IlUnmanagedDerefExpr): T
    fun visitIlConvExpr(expr: IlConvExpr): T
    fun visitIlBoxExpr(expr: IlBoxExpr): T
    fun visitIlUnboxExpr(expr: IlUnboxExpr): T
    fun visitIlCastClassExpr(expr: IlCastClassExpr): T
    fun visitIlIsInstExpr(expr: IlIsInstExpr): T
    fun visitIlFieldAccess(expr: IlFieldAccess): T
    fun visitIlArrayAccess(expr: IlArrayAccess): T
    fun visitIlLocalVar(expr: IlLocalVar): T
    fun visitIlTempVar(expr: IlTempVar): T
    fun visitErrVar(expr: IlErrVar): T
    fun visitIlArg(expr: IlArgument): T


    interface Default<out T> : IlExprVisitor<T>, IlConstVisitor<T> {
        fun visitDefault(expr: IlExpr): T

        override fun visitIlUnaryOp(expr: IlUnaryOp): T = visitDefault(expr)
        override fun visitIlBinaryOp(expr: IlBinaryOp): T = visitDefault(expr)
        override fun visitIlArrayLength(expr: IlArrayLengthExpr): T = visitDefault(expr)
        override fun visitIlCall(expr: IlCall): T = visitDefault(expr)
        override fun visitIlInitExpr(expr: IlInitExpr): T = visitDefault(expr)
        override fun visitIlNewArrayExpr(expr: IlNewArrayExpr): T = visitDefault(expr)
        override fun visitIlNewExpr(expr: IlNewExpr): T = visitDefault(expr)
        override fun visitIlSizeOfExpr(expr: IlSizeOfExpr): T = visitDefault(expr)
        override fun visitIlStackAllocExpr(expr: IlStackAllocExpr): T = visitDefault(expr)
        override fun visitIlManagedRefExpr(expr: IlManagedRefExpr): T = visitDefault(expr)
        override fun visitIlUnmanagedRefExpr(expr: IlUnmanagedRefExpr): T = visitDefault(expr)
        override fun visitIlManagedDerefExpr(expr: IlManagedDerefExpr): T = visitDefault(expr)
        override fun visitIlUnmanagedDerefExpr(expr: IlUnmanagedDerefExpr): T = visitDefault(expr)
        override fun visitIlConvExpr(expr: IlConvExpr): T = visitDefault(expr)
        override fun visitIlBoxExpr(expr: IlBoxExpr): T = visitDefault(expr)
        override fun visitIlUnboxExpr(expr: IlUnboxExpr): T = visitDefault(expr)
        override fun visitIlCastClassExpr(expr: IlCastClassExpr): T = visitDefault(expr)
        override fun visitIlIsInstExpr(expr: IlIsInstExpr): T = visitDefault(expr)
        override fun visitIlFieldAccess(expr: IlFieldAccess): T = visitDefault(expr)
        override fun visitIlArrayAccess(expr: IlArrayAccess): T = visitDefault(expr)
        override fun visitIlLocalVar(expr: IlLocalVar): T
        override fun visitIlTempVar(expr: IlTempVar): T
        override fun visitErrVar(expr: IlErrVar): T
        override fun visitIlArg(expr: IlArgument): T
    }
}
