/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.backend.js.declarations

import org.jetbrains.kotlin.backend.js.context.IrTranslationContext
import org.jetbrains.kotlin.backend.js.expression.IrExpressionTranslationVisitor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.js.backend.ast.*

class IrDeclarationTranslationVisitor(private val context: IrTranslationContext) : IrElementVisitorVoid {
    override fun visitElement(element: IrElement) {
    }

    override fun visitFunction(declaration: IrFunction) {
        val jsFunction = JsFunction(context.scope, JsBlock(), "")
        context.declarationStatements += JsExpressionStatement(jsFunction)

        context.nestedDeclaration(declaration.descriptor) {
            jsFunction.name = context.naming.innerNames[declaration.descriptor]
            for (parameter in declaration.valueParameters) {
                jsFunction.parameters += JsParameter(context.naming.innerNames[parameter.descriptor])
            }

            declaration.body?.let { body ->
                context.withStatements(jsFunction.body.statements) {
                    body.acceptVoid(this)
                }
            }
        }
    }

    override fun visitBlockBody(body: IrBlockBody) {
        val innerVisitor = IrExpressionTranslationVisitor(context)
        for (statement in body.statements) {
            statement.accept(innerVisitor, Unit)?.let { (jsExpression) ->
                context.addStatement(JsExpressionStatement(jsExpression))
            }
        }
    }
}