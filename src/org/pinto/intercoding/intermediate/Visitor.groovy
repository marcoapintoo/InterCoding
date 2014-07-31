/*
 *  
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *  Author: Marco Antonio Pinto O. <pinto.marco@live.com>
 *  URL: https://github.com/marcoapintoo
 *  License: LGPL
 */


package org.pinto.intercoding.intermediate

import groovy.util.logging.Log4j2

interface ModelVisitor<T, R> {
    R visit(ArrayModel node, T argument);

    R visit(ArrayAccessModel node, T argument);

    R visit(AssertModel node, T argument);

    R visit(AssignmentModel node, T argument);

    R visit(BlockModel node, T argument);

    R visit(BreakModel node, T argument);

    R visit(CaseSwitchModel node, T argument);

    R visit(CastModel node, T argument);

    R visit(ClassModel node, T argument);

    R visit(CommentModel node, T argument);

    R visit(ConditionalModel node, T argument);

    //R visit(Context node, T argument);

    R visit(ContinueModel node, T argument);

    R visit(DoModel node, T argument);

    R visit(TypeElementModel node, T argument);

    R visit(EmptyModel node, T argument);

    R visit(EnumerationModel node, T argument);

    R visit(EnumFieldModel node, T argument);

    R visit(ExpressionModel node, T argument);

    R visit(FieldModel node, T argument);

    R visit(FieldAccessModel node, T argument);

    R visit(ModelFlag node, T argument);

    R visit(ForModel node, T argument);

    R visit(ForeachModel node, T argument);

    R visit(GenericArgumentModel node, T argument);

    R visit(BaseModel node, T argument);

    R visit(GenericParameterModel node, T argument);

    R visit(IfModel node, T argument);

    R visit(ImportModel node, T argument);

    R visit(InfixOperationModel node, T argument);

    R visit(IsInstanceModel node, T argument);

    R visit(InterfaceModel node, T argument);

    R visit(LabeledModel node, T argument);

    R visit(LiteralModel node, T argument);

    R visit(MethodModel node, T argument);

    R visit(MethodCallModel node, T argument);

    R visit(MethodParameterModel node, T argument);

    R visit(NameModel node, T argument);

    R visit(NamespaceModel node, T argument);

    R visit(ObjectCreationModel node, T argument);

    R visit(ParenthesizedModel node, T argument);

    R visit(PostfixOperationModel node, T argument);

    R visit(PrefixOperationModel node, T argument);

    R visit(ReturnModel node, T argument);

    R visit(StatementModel node, T argument);

    R visit(SuperThisModel node, T argument);

    R visit(SwitchModel node, T argument);

    R visit(ThisModel node, T argument);

    R visit(ThrowModel node, T argument);

    R visit(TryModel node, T argument);

    R visit(TryCatchModel node, T argument);

    R visit(TypeModel node, T argument);

    R visit(TypeDeclarationModel node, T argument);

    R visit(TypeReferenceModel node, T argument);

    R visit(VariableDeclaration node, T argument);

    R visit(WhileModel node, T argument);

    R visit(WithResourceModel node, T argument);

    R visit(AssignmentModel.Operators node, T argument);

    R visit(PrefixOperationModel.Operators node, T argument);

    R visit(PostfixOperationModel.Operators node, T argument);

    R visit(InfixOperationModel.Operators node, T argument);
}

class GenericModelVisitor<T, R> implements ModelVisitor<T, R> {
    @Override
    R visit(ArrayModel node, T argument) {
        return null
    }

    @Override
    R visit(ArrayAccessModel node, T argument) {
        return null
    }

    @Override
    R visit(AssertModel node, T argument) {
        return null
    }

    @Override
    R visit(AssignmentModel node, T argument) {
        return null
    }

    @Override
    R visit(BlockModel node, T argument) {
        return null
    }

    @Override
    R visit(BreakModel node, T argument) {
        return null
    }

    @Override
    R visit(CaseSwitchModel node, T argument) {
        return null
    }

    @Override
    R visit(CastModel node, T argument) {
        return null
    }

    @Override
    R visit(ClassModel node, T argument) {
        return null
    }

    @Override
    R visit(CommentModel node, T argument) {
        return null
    }

    @Override
    R visit(ConditionalModel node, T argument) {
        return null
    }
/*
    @Override
    R visit(Context node, T argument) {
        return null
    }
*/

    @Override
    R visit(ContinueModel node, T argument) {
        return null
    }

    @Override
    R visit(DoModel node, T argument) {
        return null
    }

    @Override
    R visit(TypeElementModel node, T argument) {
        return null
    }

    @Override
    R visit(EmptyModel node, T argument) {
        return null
    }

    @Override
    R visit(EnumerationModel node, T argument) {
        return null
    }

    @Override
    R visit(EnumFieldModel node, T argument) {
        return null
    }

    @Override
    R visit(ExpressionModel node, T argument) {
        return null
    }

    @Override
    R visit(FieldModel node, T argument) {
        return null
    }

    @Override
    R visit(FieldAccessModel node, T argument) {
        return null
    }

    @Override
    R visit(ModelFlag node, T argument) {
        return null
    }

    @Override
    R visit(ForModel node, T argument) {
        return null
    }

    @Override
    R visit(ForeachModel node, T argument) {
        return null
    }

    @Override
    R visit(GenericArgumentModel node, T argument) {
        return null
    }

    @Override
    R visit(BaseModel node, T argument) {
        return null
    }

    @Override
    R visit(GenericParameterModel node, T argument) {
        return null
    }

    @Override
    R visit(IfModel node, T argument) {
        return null
    }

    @Override
    R visit(ImportModel node, T argument) {
        return null
    }

    @Override
    R visit(InfixOperationModel node, T argument) {
        return null
    }

    @Override
    R visit(IsInstanceModel node, T argument) {
        return null
    }

    @Override
    R visit(InterfaceModel node, T argument) {
        return null
    }

    @Override
    R visit(LabeledModel node, T argument) {
        return null
    }

    @Override
    R visit(LiteralModel node, T argument) {
        return null
    }

    @Override
    R visit(MethodModel node, T argument) {
        return null
    }

    @Override
    R visit(MethodCallModel node, T argument) {
        return null
    }

    @Override
    R visit(MethodParameterModel node, T argument) {
        return null
    }

    @Override
    R visit(NameModel node, T argument) {
        return null
    }

    @Override
    R visit(NamespaceModel node, T argument) {
        return null
    }

    @Override
    R visit(ObjectCreationModel node, T argument) {
        return null
    }

    @Override
    R visit(ParenthesizedModel node, T argument) {
        return null
    }

    @Override
    R visit(PostfixOperationModel node, T argument) {
        return null
    }

    @Override
    R visit(PrefixOperationModel node, T argument) {
        return null
    }

    @Override
    R visit(ReturnModel node, T argument) {
        return null
    }

    @Override
    R visit(StatementModel node, T argument) {
        return null
    }

    @Override
    R visit(SuperThisModel node, T argument) {
        return null
    }

    @Override
    R visit(SwitchModel node, T argument) {
        return null
    }

    @Override
    R visit(ThisModel node, T argument) {
        return null
    }

    @Override
    R visit(ThrowModel node, T argument) {
        return null
    }

    @Override
    R visit(TryModel node, T argument) {
        return null
    }

    @Override
    R visit(TryCatchModel node, T argument) {
        return null
    }

    @Override
    R visit(TypeModel node, T argument) {
        return null
    }

    @Override
    R visit(TypeDeclarationModel node, T argument) {
        return null
    }

    @Override
    R visit(TypeReferenceModel node, T argument) {
        return null
    }

    @Override
    R visit(VariableDeclaration node, T argument) {
        return null
    }

    @Override
    R visit(WhileModel node, T argument) {
        return null
    }

    @Override
    R visit(WithResourceModel node, T argument) {
        return null
    }

    @Override
    R visit(AssignmentModel.Operators node, T argument) {
        return null
    }

    @Override
    R visit(PrefixOperationModel.Operators node, T argument) {
        return null
    }

    @Override
    R visit(PostfixOperationModel.Operators node, T argument) {
        return null
    }

    @Override
    R visit(InfixOperationModel.Operators node, T argument) {
        return null
    }
}

@Log4j2
class AutomaticModelVisitor<T, R> implements ModelVisitor<T, R> {
    protected static long indent = 0
    boolean avoidStackOverflow = true
    final stackMaxLength = 1509
    ArrayList<Object> stackCache = new ArrayList<>(stackMaxLength)

    R defaultVisit(BaseModel node, T argument) {
        stackCache.add(node)
        if (stackCache.size() >= stackMaxLength) stackCache.remove(0)
        log.info "  " * indent + "Default visiting " + node
        log.info "  " * indent + ClassInfo.of(node).beanProperties.keySet().toString()

        /*def atomVerify = { obj->
            //if(obj!=null&&obj!=node&&GenericModel.isAssignableFrom(obj.class))
            if(obj!=null&&(!avoidStackOverflow || !stackCache.contains(obj))&&GenericModel.isAssignableFrom(obj.class))
                (obj as GenericModel).accept(this, argument)
        }*/
        def atomVerify = { obj ->
            (obj != null && (!avoidStackOverflow || !stackCache.contains(obj)) && BaseModel.isAssignableFrom(obj.class))
        }
        ClassInfo.of(node).beanProperties.each {
            def obj = it.value.applyTo(node)
            indent++
            log.info "  " * indent + "PROPERTY " + it.key
            if (Collection.isAssignableFrom(it.value.type)) {
                //obj.value.findAll{ atomVerify(it) }.each {def obj = it; (obj as GenericModel).accept(this, argument)}
                obj.value.findAll { atomVerify(it) }.each { (it as BaseModel).accept(this, argument) }
            } else {
                if (atomVerify(obj.value)) {
                    (obj.value as BaseModel).accept(this, argument)
                }
            }
            indent--
        }
        return null
    }

    @Override
    R visit(ArrayModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ArrayAccessModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(AssertModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(AssignmentModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(BlockModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(BreakModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(CaseSwitchModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(CastModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ClassModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(CommentModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ConditionalModel node, T argument) {
        return defaultVisit(node, argument)
    }
/*
    @Override
    R visit(Context node, T argument) {
        return defaultVisit(node, argument)
    }
*/

    @Override
    R visit(ContinueModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(DoModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeElementModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(EmptyModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(EnumerationModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(EnumFieldModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ExpressionModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(FieldModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(FieldAccessModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ModelFlag node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ForModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ForeachModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(GenericArgumentModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(BaseModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(GenericParameterModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(IfModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ImportModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(InfixOperationModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(IsInstanceModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(InterfaceModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(LabeledModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(LiteralModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(MethodModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(MethodCallModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(MethodParameterModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(NameModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(NamespaceModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ObjectCreationModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ParenthesizedModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PostfixOperationModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PrefixOperationModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ReturnModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(StatementModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(SuperThisModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(SwitchModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ThisModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ThrowModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TryModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TryCatchModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeDeclarationModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeReferenceModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(VariableDeclaration node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(WhileModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(WithResourceModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(AssignmentModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PrefixOperationModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PostfixOperationModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(InfixOperationModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }
}


@Log4j2
class DefaultModelVisitor<T, R> implements ModelVisitor<T, R> {
    R defaultVisit(Object node, T argument) {
        //log.info "Default visiting " + node
        return null
    }
    /*R defaultVisit(GenericModel node, T argument) {
        log.info "Default visiting " + node
        return null
    }*/

    public <V extends BaseModel> void iterate(List<V> list, T argument) {
        for (c in list.toList())
            c?.accept(this, argument)
        /*def iterator = newList.iterator()
        while (iterator.hasNext()) {
            //argument.setIterator(iterator)
            iterator.next().accept(this, argument)
            //argument.setIterator(null)
        }*/
    }

    @Override
    R visit(ArrayModel node, T argument) {
        iterate(node.dimensions, argument)
        iterate(node.elements, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ArrayAccessModel node, T argument) {
        node.array.accept(this, argument)
        node.index.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(AssertModel node, T argument) {
        node.expression.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(AssignmentModel node, T argument) {
        node.left.accept(this, argument)
        node.operator.accept(this, argument)
        node.right.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(BlockModel node, T argument) {
        iterate(node.statements, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(BreakModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(CaseSwitchModel node, T argument) {
        node.label.accept(this, argument)
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(CastModel node, T argument) {
        node.expression.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ClassModel node, T argument) {
        iterate(node.elements, argument)
        iterate(node.parents, argument)
        node.genericParameters?.each { it.accept(this, argument) }
        return defaultVisit(node, argument)
    }

    @Override
    R visit(CommentModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ConditionalModel node, T argument) {
        node.condition.accept(this, argument)
        node.trueAction.accept(this, argument)
        node.falseAction.accept(this, argument)
        return defaultVisit(node, argument)
    }
/*
    @Override
    R visit(Context node, T argument) {
        return defaultVisit(node, argument)
    }
*/

    @Override
    R visit(ContinueModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(DoModel node, T argument) {
        node.condition.accept(this, argument)
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeElementModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(EmptyModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(EnumerationModel node, T argument) {
        iterate(node.values, argument)
        iterate(node.elements, argument)
        iterate(node.parents, argument)
        node.genericParameters?.each { it.accept(this, argument) }
        return defaultVisit(node, argument)
    }

    @Override
    R visit(EnumFieldModel node, T argument) {
        iterate(node.arguments, argument)
        node.type?.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ExpressionModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(FieldModel node, T argument) {
        node.defaultValue.accept(this, argument)
        node.type.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(FieldAccessModel node, T argument) {
        node.expression.accept(this, argument)
        iterate(node.genericArguments, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ModelFlag node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ForModel node, T argument) {
        iterate(node.initializers, argument)
        node.condition.accept(this, argument)
        iterate(node.updaters, argument)
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ForeachModel node, T argument) {
        iterate(node.variables, argument)
        node.generator.accept(this, argument)
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(GenericArgumentModel node, T argument) {
        iterate(node.childRequests ?: [], argument)
        iterate(node.parentRequests ?: [], argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(BaseModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(GenericParameterModel node, T argument) {
        iterate(node.childRequests ?: [], argument)
        iterate(node.parentRequests ?: [], argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(IfModel node, T argument) {
        node.condition.accept(this, argument)
        node.trueAction.accept(this, argument)
        node.falseAction.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ImportModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(InfixOperationModel node, T argument) {
        node.left.accept(this, argument)
        node.operator.accept(this, argument)
        node.right.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(IsInstanceModel node, T argument) {
        node.expression.accept(this, argument)
        node.type.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(InterfaceModel node, T argument) {
        iterate(node.elements, argument)
        iterate(node.parents, argument)
        iterate(node.genericParameters, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(LabeledModel node, T argument) {
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(LiteralModel node, T argument) {
        if (node.value instanceof BaseModel) {
            (node.value as BaseModel)?.accept(this, argument)
        }
        return defaultVisit(node, argument)
    }

    @Override
    R visit(MethodModel node, T argument) {
        iterate( (List<MethodParameterModel>) node.parameters, argument)
        node.action.accept(this, argument)
        iterate(node.genericParameters, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(MethodCallModel node, T argument) {
        node.expression.accept(this, argument)
        iterate(node.arguments, argument)
        iterate(node.genericArguments, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(MethodParameterModel node, T argument) {
        node.defaultValue?.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(NameModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(NamespaceModel node, T argument) {
        iterate(node.types, argument)
        iterate(node.namespaces, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ObjectCreationModel node, T argument) {
        iterate(node.arguments, argument)
        node.type.accept(this, argument)
        iterate(node.genericArguments, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ParenthesizedModel node, T argument) {
        node.expression.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PostfixOperationModel node, T argument) {
        node.operand.accept(this, argument)
        node.operator.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PrefixOperationModel node, T argument) {
        node.operator.accept(this, argument)
        node.operand.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ReturnModel node, T argument) {
        node.returnValue?.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(StatementModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(SuperThisModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(SwitchModel node, T argument) {
        node.condition.accept(this, argument)
        iterate(node.statements, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ThisModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(ThrowModel node, T argument) {
        node.expression.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TryModel node, T argument) {
        node.verifiedAction.accept(this, argument)
        iterate(node.catchErrors, argument)
        node.finallyAction.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TryCatchModel node, T argument) {
        iterate(node.errors, argument)
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeModel node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeDeclarationModel node, T argument) {
        node.declaredType.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(TypeReferenceModel node, T argument) {
        iterate(node.genericArguments, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(VariableDeclaration node, T argument) {
        node.defaultValue.accept(this, argument)
        node.type.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(WhileModel node, T argument) {
        node.condition.accept(this, argument)
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(WithResourceModel node, T argument) {
        iterate(node.resources, argument)
        node.action.accept(this, argument)
        return defaultVisit(node, argument)
    }

    @Override
    R visit(AssignmentModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PrefixOperationModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(PostfixOperationModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }

    @Override
    R visit(InfixOperationModel.Operators node, T argument) {
        return defaultVisit(node, argument)
    }
}
