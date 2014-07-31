/*
 *  Copyleft (c) 2014 InterCoding Project
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

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j2
import sun.org.mozilla.javascript.ast.Block

class ProcessingInfo {
    //Iterator<? extends GenericModel> iterator
    int modifications = 0
}


class ForToWhileConversion {
    BlockModel convert(ForModel model) {
        def whileActions = [model.action]
        whileActions.addAll(model.updaters)
        ArrayList<StatementModel> stmtActions = model.initializers.collect() ?: []
        stmtActions.add(
                new WhileModel(
                        condition: model.condition,
                        action: new BlockModel(
                                newScope: true,
                                statements: whileActions
                        )
                )
        )
        def whileStmt = new BlockModel(
                newScope: false,
                statements: stmtActions
        )
        return whileStmt
    }
}

class SwitchToWhileConversion {
    def counter = 0

    BlockModel convert(SwitchModel model) {
        def varname = "switchCondition" + counter
        def blockStmt = new BlockModel(
                newScope: true,
                statements: [assignCondition(varname, model.condition)]
        )
        def whileStmt = new WhileModel(
                condition: new NameModel(name: varname),
                action: null
        )
        blockStmt.statements.add(whileStmt)
        def ifStatement = null;
        def lastIfStatement = null;
        for (int i = 0; i < model.statements.size(); i++) {
            lastIfStatement = switchCaseToIf varname, lastIfStatement, model.statements[i], i < model.statements.size() - 1 ? model.statements[i + 1] : null
            if (ifStatement == null) {
                ifStatement = lastIfStatement
            }
        }
        whileStmt.action = ifStatement
        counter++
        return blockStmt
    }

    protected def compareCondition(name, expression) {
        new InfixOperationModel(
                left: new NameModel(name: name),
                operator: InfixOperationModel.Operators.Equals,
                right: expression
        )
    }

    protected def assignCondition(name, expression) {
        new AssignmentModel(
                left: new NameModel(name: name),
                operator: AssignmentModel.Operators.Assign,
                right: expression
        )
    }

    protected def endOfCaseStatement(BlockModel trueBlock, name, nextSwitchCase) {
        def length = trueBlock.statements.size()
        if (length > 0 && trueBlock.statements[length - 1] instanceof BreakModel) {
            trueBlock.statements = length > 1 ? trueBlock.statements[0..-2] : []
        } else if (nextSwitchCase != null) {
            trueBlock.statements.add(assignCondition(name, nextSwitchCase.label))
        }

    }

    protected def switchCaseToIf(name, IfModel lastIf, CaseSwitchModel switchCase, CaseSwitchModel nextSwitchCase) {
        def trueBlock = switchCase.action
        if (!(switchCase.action instanceof BlockModel)) {
            trueBlock = new BlockModel(
                    newScope: true,
                    statements: switchCase.action instanceof BlockModel ? (switchCase.action as BlockModel).statements : [switchCase.action],
            )
        }
        trueBlock = (trueBlock as BlockModel)
        endOfCaseStatement trueBlock, name, nextSwitchCase
        if (switchCase.label == null) {
            lastIf.falseAction = trueBlock
            return lastIf
        }
        def ifStatement = new IfModel(
                condition: compareCondition(name, switchCase.label),
                trueAction: trueBlock,
                falseAction: null,
        )
        if (lastIf != null) {
            lastIf.falseAction = ifStatement
        }
        return ifStatement
    }
}

class FieldToConstructor {
    private MethodModel createConstructor(TypeModel type, boolean isStatic = false) {
        def constructor = new MethodModel(
                returnType: null,
                action: [],
                parameters: [],
                genericParameters: [],
                documentation: new CommentModel(
                        multiline: true,
                        content: "\n Auto generated constructor\n"
                ),
                flags: EnumSet.of(ModelFlag.Public),
                name: MethodModel.ConstructorName
        )
        if (isStatic) constructor.flags.add(ModelFlag.Static)
        type.elements.add(0, constructor)
        return constructor
    }

    private AssignmentModel transformfield(FieldModel field) {
        def assignment = new AssignmentModel(
                left: new NameModel(name: field.name),
                operator: AssignmentModel.Operators.Assign,
                right: field.defaultValue instanceof EmptyModel ? LiteralModel.None : field.defaultValue
        )
        field.defaultValue = LiteralModel.None
        return assignment
    }

    private BlockModel insertIntoConstructor(MethodModel constructor, List<AssignmentModel> fields) {
        def index = 0;
        BlockModel block
        if (!(constructor.action instanceof BlockModel)) {
            block = new BlockModel(
                    newScope: true
            )
            if (!(constructor.action instanceof EmptyModel)) {
                block.statements = [constructor.action]
            }
            constructor.action = block
        } else {
            block = constructor.action as BlockModel
        }
        //BEGIN: Unknown issue. I do not know why they appears :S
        def invalidStatements = block.statements.findAll { it.class == StatementModel.class }
        block.statements.removeAll(invalidStatements)
        //END
        if (block.statements.size() > 0 && block.statements[0] instanceof MethodCallModel) {
            def firstStatement = block.statements[0] as MethodCallModel
            if (firstStatement.expression instanceof ThisModel || firstStatement.expression instanceof SuperThisModel) {
                index = 1
            }
        }
        block.statements.addAll(index, fields)
        if (block.statements.size() == 0) {
            block.statements.add(new EmptyModel())
        }
        return block
    }

    void applyTo(TypeModel classType) {
        MethodModel staticConstructor = classType.elements.find {
            it instanceof MethodModel && it.constructor && it.flags.contains(ModelFlag.Static)
        }
        staticConstructor = staticConstructor ?: createConstructor(classType, true)
        //def staticFieldsInitiated = classType.elements.findAll {it instanceof Field && it.defaultValue != null && !(it.defaultValue instanceof Empty) && it.flags.contains(Flag.Static)   }
        def staticFieldsInitiated = classType.elements.findAll {
            it instanceof FieldModel && it.defaultValue != null && it.defaultValue != LiteralModel.None && it.flags.contains(ModelFlag.Static)
        }
        List<MethodModel> objectConstructors = classType.elements.findAll {
            it instanceof MethodModel && it.constructor && (it.parameters == null || it.parameters.size() == 0) && !it.flags.contains(ModelFlag.Static)
        }
        // Common constructor and fields
        def objectConstructor
        if (objectConstructors.size() > 0) {
            objectConstructor = objectConstructors.find { it.parameters == null }
            objectConstructor = objectConstructor ?: objectConstructors.min { it.parameters.size() }
        } else {
            objectConstructor = createConstructor(classType, false)
        }
        //def objectFieldsInitiated = classType.elements.findAll {it instanceof Field && it.defaultValue != null && !it.flags.contains(Flag.Static)   }
        //def objectFieldsInitiated = classType.elements.findAll {it instanceof Field && it.defaultValue != null && !(it.defaultValue instanceof Empty) && !it.flags.contains(Flag.Static)}
        def objectFieldsInitiated = classType.elements.findAll {
            it instanceof FieldModel && it.defaultValue != null && !LiteralModel.None.compare(it.defaultValue) && !it.flags.contains(ModelFlag.Static)
        }
        List<AssignmentModel> staticAssignments = staticFieldsInitiated.collectAll { transformfield(it) }
        insertIntoConstructor(staticConstructor, staticAssignments)
        List<AssignmentModel> objectAssignments = objectFieldsInitiated.collectAll { transformfield(it) }
        insertIntoConstructor(objectConstructor, objectAssignments)
    }
}

@CompileStatic
class AvoidExpressionAssignments {
    private StatementModel adaptAssignment(StatementModel parent, AssignmentModel assignmentModel){
        def left = (StatementModel) assignmentModel.left.clone()
        def accessor = parent.accessor
        if(accessor instanceof CollectionPropertyModelAccesor){
            def collectionAccessor = accessor as CollectionPropertyModelAccesor
            collectionAccessor.provider.add(collectionAccessor.index, assignmentModel)
        }else{
            def block =new BlockModel(
                    newScope: false
            )
            block.setStatements([assignmentModel, parent])
            accessor.value=block
        }
        assignmentModel.accessor.value = left
        return parent
    }
    private StatementModel adaptPreAssignment(WhileModel parent, AssignmentModel assignmentModel){
        def block = new BlockModel(
                newScope: false
        )
        block.setStatements([assignmentModel, parent])
        parent.accessor.value = block
        return block
    }
    private StatementModel adaptWhile(WhileModel parent, AssignmentModel assignmentModel){
        def left = (StatementModel) assignmentModel.left.clone()
        assignmentModel.accessor.value = left
        if(parent.action instanceof EmptyModel){
            parent.action=(StatementModel) assignmentModel.clone()
        }else if(parent.action instanceof BlockModel){
            (parent.action as BlockModel).statements.add((StatementModel) assignmentModel.clone())
        }else{
            def block = new BlockModel(
                    newScope: true
            )
            block.setStatements([parent.action, (StatementModel) assignmentModel.clone()],)
            parent.action = block
        }
        return parent
    }
    StatementModel convert(AssignmentModel assignmentModel) {
        StatementModel parent = PrePostfixConversion.findStatementParent(assignmentModel)[0]
        StatementModel subparent = PrePostfixConversion.findStatementParent(assignmentModel)[1]
        if (parent == assignmentModel.accessor.owner) {
            //It's a direct child
            return assignmentModel
        }
        //TODO: Finish
        if(subparent instanceof ExpressionModel){
            if(parent instanceof ForModel){
                println "unknown for"
            }else if(parent instanceof DoModel){
                return adaptWhile(parent, assignmentModel)
            }else if(parent instanceof WhileModel){
                adaptWhile(parent, assignmentModel)
                return adaptPreAssignment(parent, assignmentModel)
            }else if(parent instanceof IfModel){
                return adaptAssignment(parent, assignmentModel)
            }else if(parent instanceof BlockModel){
                return adaptAssignment(parent, assignmentModel)
            }else{
                println "unknown " + parent.class.toString()
                return assignmentModel
            }
        }
        return assignmentModel
    }
}
@CompileStatic
class PrePostfixConversion {
    private static int varCounter = 0

    private String createVarName(boolean create = true) {
        if (create) varCounter++
        "inc${varCounter}"
    }

    static List<StatementModel> findStatementParent(ExpressionModel model) {
        def isStatement = { BaseModel m -> m instanceof StatementModel && !(m instanceof ExpressionModel) }
        def parent = model.accessor.owner
        def lastParent = parent
        def lastParents = [(StatementModel) model]
        //Avoiding recursivity:
        //return (isStatement(parent))? parent: findStatementParent(parent);
        while (parent != null && !isStatement(parent)) {
            lastParents.add((StatementModel) parent)
            lastParent = parent
            parent = (parent as StatementModel).accessor.owner
        }
        return [(StatementModel) parent, (StatementModel) lastParent]
    }

    AssignmentModel createPreffixOperator(ExpressionModel name, int increment) {
        new AssignmentModel(
                left: name, //new Name(name: name),
                operator: increment == 1 ? AssignmentModel.Operators.PlusAssign : AssignmentModel.Operators.MinusAssign,
                right: LiteralModel.One,
        )
    }

    //model instanceof PostfixOperation || model instanceof PrefixOperation && (model.operator== PrefixOperation.Operators.AutoIncrement||model.operator== PrefixOperation.Operators.AutoDecrement
    int isValid(PostfixOperationModel model) {
        if (model.operator == PostfixOperationModel.Operators.AutoIncrement) {
            return 1
        } else if (model.operator == PostfixOperationModel.Operators.AutoDecrement) {
            return -1
        }
        return 0
    }

    int isValid(PrefixOperationModel model) {
        if (model.operator == PrefixOperationModel.Operators.AutoIncrement) {
            return 1
        } else if (model.operator == PrefixOperationModel.Operators.AutoDecrement) {
            return -1
        }
        return 0
    }

    void convert(ExpressionModel model) {
        def parents
        try {
            parents = findStatementParent(model)
        } catch (e) {
            e.printStackTrace()
            return
        }
        StatementModel parent = parents[0]
        int selfModification
        AssignmentModel selfModificationOperation
        if (model instanceof PrefixOperationModel) {
            def pmodel = model as PrefixOperationModel
            selfModification = isValid(pmodel)
            selfModificationOperation = createPreffixOperator(pmodel.operand, selfModification)
        } else {
            def pmodel = model as PostfixOperationModel
            selfModification = isValid(pmodel)
            selfModificationOperation = createPreffixOperator(pmodel.operand, selfModification)
        }
        if (parent == model.accessor.owner) {
            //Direct child
            model.accessor.value = selfModificationOperation
        } else {
            //Subchild
            String varName = createVarName()
            List<StatementModel> statements
            def index
            if (BlockModel.class.isAssignableFrom(parent.class)) {
                statements = (parent as BlockModel).statements.toList()
                index = statements.findIndexOf { it == parents[1] }
            } else {
                index = 0
                statements = [parent]
            }
            VariableDeclaration declaration = new VariableDeclaration(
                    defaultValue: selfModificationOperation.left,
                    type: new TypeReferenceModel(typeName: "Object"), //TODO: Infer type of selfModificationOperation.left
                    name: varName
            )
            if (model instanceof PrefixOperationModel) {
                statements.add(index, declaration)
                statements.add(index, selfModificationOperation)
            } else {
                statements.add(index, selfModificationOperation)
                statements.add(index, declaration)
            }
            def accessor = parent.accessor
            //def b = parent.accessor.owner
            BlockModel block = new BlockModel(
                    newScope: true
            )
            block.setStatements(statements)
            //def c = parent.accessor.owner
            model.accessor.value = new NameModel(name: varName)
            accessor.value = block
            /*def d = parent.accessor.owner
            if(parent.accessor instanceof CollectionPropertyModelAccesor){
                if((parent.accessor as CollectionPropertyModelAccesor).owner.is(block)){
                    println block
                    println b
                    println 111
                    println c
                    println 111
                    println d
                    println 111
                    println parent.accessor.owner
                    println block
                    throw new Exception("PRE-AAAAAAAAA")
                }
            }
            parent.accessor.value =  block
            */
        }
    }
}

@Log4j2
class PythonCodeProcessing extends DefaultModelVisitor<ProcessingInfo, Void> {
    def process(NamespaceModel root) {
        def info
        info = new ProcessingInfo()
        root.accept(this, info)
        for (; info.modifications != 0; info = new ProcessingInfo())
            root.accept(this, info)
    }

    @Override
    Void visit(ArrayModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ArrayAccessModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(AssertModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }


    @Override
    Void visit(AssignmentModel node, ProcessingInfo argument) {
        if(node.accessor?.owner instanceof ExpressionModel){
            def processor = new AvoidExpressionAssignments()
            def converted = processor.convert(node)
            if(converted!=null){
                return super.visit(converted, argument)
            }
        }
        return super.visit(node, argument)
    }

    @Override
    Void visit(BlockModel node, ProcessingInfo argument) {
        if (!node.newScope || (node.accessor.owner instanceof BlockModel)) {
            argument.modifications++
            //node.accessor.value =
            if (node.accessor instanceof CollectionPropertyModelAccesor) {
                def accessor = node.accessor as CollectionPropertyModelAccesor
                def index = accessor.index
                accessor.provider.remove(node)
                accessor.provider.addAll(index, node.statements)
                /*if(node.statementOwnerList!=null){
                    int index = node.statementOwnerList.indexOf(node)
                    node.statementOwnerList.remove(node)
                    node.statementOwnerList.addAll(index, node.statements)*/
            } else {//TODO:Verify
                node.newScope = true
            }
            //return null
        }
        return super.visit(node, argument)
    }

    @Override
    Void visit(BreakModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(CaseSwitchModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(CastModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ClassModel node, ProcessingInfo argument) {
        new FieldToConstructor().applyTo(node)
        return super.visit(node, argument)
    }

    @Override
    Void visit(CommentModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ConditionalModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ContinueModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(DoModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(TypeElementModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(EmptyModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(EnumerationModel node, ProcessingInfo argument) {
        new FieldToConstructor().applyTo(node)
        return super.visit(node, argument)
    }

    @Override
    Void visit(EnumFieldModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ExpressionModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(FieldModel node, ProcessingInfo argument) {
        if (node.defaultValue != null && !LiteralModel.None.compare(node.defaultValue)) {
            argument.modifications++
        }
        return super.visit(node, argument)
    }

    @Override
    Void visit(FieldAccessModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ModelFlag node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ForModel node, ProcessingInfo argument) {
        argument.modifications++
        log.info "Modifying for statement " + node.toString()
        def converter = new ForToWhileConversion()
        node.accessor.value = converter.convert(node)
        return super.visit(node, argument)
    }

    @Override
    Void visit(ForeachModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(GenericArgumentModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(BaseModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(GenericParameterModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(IfModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ImportModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(InfixOperationModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(IsInstanceModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(InterfaceModel node, ProcessingInfo argument) {
        new FieldToConstructor().applyTo(node)
        return super.visit(node, argument)
    }

    @Override
    Void visit(LabeledModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(LiteralModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(MethodModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(MethodCallModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(MethodParameterModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(NameModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(NamespaceModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ObjectCreationModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ParenthesizedModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(PostfixOperationModel node, ProcessingInfo argument) {
        argument.modifications++
        new PrePostfixConversion().convert(node)
        return super.visit(node, argument)
    }

    @Override
    Void visit(PrefixOperationModel node, ProcessingInfo argument) {
        if (node.operator == PrefixOperationModel.Operators.AutoIncrement || node.operator == PrefixOperationModel.Operators.AutoDecrement) {
            argument.modifications++
            new PrePostfixConversion().convert(node)
        }
        return super.visit(node, argument)
    }

    @Override
    Void visit(ReturnModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(StatementModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(SuperThisModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(SwitchModel node, ProcessingInfo argument) {
        argument.modifications++
        log.info "Modifying switch statement " + node.toString()
        def converter = new SwitchToWhileConversion()
        node.accessor.value = converter.convert(node)
        return super.visit(node, argument)
    }

    @Override
    Void visit(ThisModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(ThrowModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(TryModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(TryCatchModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(TypeModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(TypeDeclarationModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(TypeReferenceModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(VariableDeclaration node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(WhileModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(WithResourceModel node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(AssignmentModel.Operators node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(PrefixOperationModel.Operators node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(PostfixOperationModel.Operators node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }

    @Override
    Void visit(InfixOperationModel.Operators node, ProcessingInfo argument) {
        return super.visit(node, argument)
    }
}

@Log4j2
class TypeFieldNameResolution extends DefaultModelVisitor<ProcessingInfo, Void> {
    HashMap<String, HashMap<String, TypeElementModel>> names = new HashMap<String, HashMap<String, TypeElementModel>>();
    HashMap<String, HashMap<String, TypeModel>> types = new HashMap<String, HashMap<String, TypeModel>>();
    HashMap<String, Set<String>> notImportedTypes = new HashMap<String, Set<String>>();
    Stack<NamespaceModel> namespaces = new Stack<NamespaceModel>()
    Stack<TypeModel> classTypes = new Stack<TypeModel>()
    NamespaceModel coreNamespace

    NamespaceModel getCurrentNamespace() {
        namespaces.peek()
    }

    TypeModel getCurrentType() {
        classTypes.peek()
    }

    def process(NamespaceModel root) {
        def last = 0

        def info
        while (true) {
            log.info "==================================================="
            log.info "Step processing names (" + last + " modifications)"
            log.info "==================================================="
            log.error "==================================================="
            log.error "Step processing names (" + last + " modifications)"
            log.error "==================================================="
            info = new ProcessingInfo()
            root.accept(this, info)
            names.clear()
            types.clear()
            if (last == info.modifications) break
            last = info.modifications
        }
        log.info "Finishing processing with " + info.modifications + " modifications."
        //info = new ProcessingInfo()
        //root.accept(this, info)
    }

    void processType(TypeModel node) {
        log.info "Processing type " + node?.name + " " + node.toString()
        if (types.containsKey(node.uniqueID.toString())) return
        //if(names.containsKey(types.get(node.uniqueID.toString()))) return
        //names.put(node.uniqueID.toString(), new HashMap<String, Type>())
        HashMap<String, TypeModel> importedTypes = types.get(node.uniqueID.toString(), new HashMap<String, TypeModel>())
        //for(type in currentNamespace.types){ <-- This won't work correctly because node calls to its parent, and it maybe its not in the same namespace XD
        for (type in node.rootTypeOwner?.namespaceOwner?.types) {
            //if(type==null) continue
            importedTypes.put(type.name, type)
        }
        //Add owner classes
        for (TypeModel root = node; root.typeOwner != null; root = root.typeOwner)
            importedTypes.put(root.typeOwner.name, root.typeOwner)

        for (imprt in node.rootTypeOwner.imports) {
            if (imprt.wildcard) {
                //if(imprt.namespace.root!=null) coreNamespace = imprt.namespace.root
                for (type in imprt.namespace.types) {
                    importedTypes.put(type.name, type)
                }
            } else {
                def type = imprt.namespace?.findType(imprt.name, false)
                if (type != null) {
                    importedTypes.put(type.name, type)
                } else {
                    log.error "Type is not available: " + imprt.namespace?.fullname?:"" + "." + imprt.name
                }
            }
        }
        HashMap<String, TypeElementModel> classNames = names.get(node.uniqueID.toString(), new HashMap<String, TypeElementModel>())
        for (parent in node.parents) {
            def type = getType(node, parent.typeName)
            if (type == null) {
                log.error "Type is not available: " + parent.typeName
                continue
            }
            processType(type)
            classNames.putAll(names.get(type.uniqueID.toString()).findAll {
                !it.value.flags.contains(ModelFlag.Private)
            })
        }
        //TODO:Connect with parents
        for (element in node.elements) {
            classNames.put(element.name, element)
        }
    }

    TypeModel findInnerType(TypeModel node, String name) {
        if (node == null) return null
        def subtype = null
        for (subname in name.split("\\.")) {
            subtype = node.innerTypes?.find { it.name == subname }
            if (subtype == null) return null
            node = subtype
        }
        return subtype
    }

    TypeModel getType(TypeModel node, String name) {
        HashMap<String, TypeModel> importedTypes = types.get(node.uniqueID.toString(), new HashMap<String, TypeModel>())
        String[] names = name.split("\\.", 2)
        TypeModel basetype
        if (importedTypes.containsKey(names[0])) {
            basetype = importedTypes.get(names[0])
        } else {
            Set<String> notImported = notImportedTypes.get(node.uniqueID.toString(), new HashSet<String>())

            //basetype = coreNamespace.findType(names[0], false)
            NamespaceModel base = coreNamespace
            NamespaceModel current = base
            //for(subname in name.split("\\.")){
            String[] parts = name.split("\\.")
            int i = 0
            for (; i < parts.length - 1; i++) {
                base = current.findNamespace(parts[i], current)
                if (base == null) {
                    break
                }
                current = base
            }
            if (i < parts.length) {
                names = parts[i..-1].join(".").split("\\.")
            } else {
                names = [parts[parts.length - 1]]
            }
            basetype = current.findType(names[0], false, current)

            def importNamespace = parts[0..i-1].join(".")
            if((names.size()==1||basetype==null) && !notImported.contains(importNamespace)){
                node.imports.add(new ImportModel(name: importNamespace))
                notImported.add(importNamespace)
            }else{
                notImported.add(name)
            }

        }
        if (names.size() > 1) {
            basetype = findInnerType(basetype, names[1])
        } else if (basetype == null && names.size() == 1) {
            basetype = findInnerType(node, names[0])
        }
        return basetype
    }

    @Override
    Void visit(NamespaceModel node, ProcessingInfo argument) {
        namespaces.push(node)
        try {
            return super.visit(node, argument)
        } finally {
            namespaces.pop()
        }
    }

    Void visitType(TypeModel node, ProcessingInfo argument) {
        int errors = argument.modifications
        classTypes.push(node)
        try {
            processType(node)
            return super.visit(node, argument)
        } finally {
            if (errors != argument.modifications) {
                log.error "ERROR LINKING TYPE: " + node.namespaceOwner?.fullname + "." + node.name
            }
            classTypes.pop()
        }
    }

    @Override
    Void visit(ClassModel node, ProcessingInfo argument) {
        return visitType(node, argument)
        /*
        classTypes.push(node)
        try {
            processType(node)
            return super.visit(node, argument)
        } finally {
            classTypes.pop()
        }
        */
    }

    @Override
    Void visit(EnumerationModel node, ProcessingInfo argument) {
        return visitType(node, argument)
        /*
        classTypes.push(node)
        try {
            processType(node)
            return super.visit(node, argument)
        } finally {
            classTypes.pop()
        }
        */
    }

    @Override
    Void visit(InterfaceModel node, ProcessingInfo argument) {
        return visitType(node, argument)
        /*
        classTypes.push(node)
        try {
            processType(node)
            return super.visit(node, argument)
        } finally {
            classTypes.pop()
        }
        */
    }

    @Override
    Void visit(MethodModel node, ProcessingInfo argument) {
        try {
            names.put(node.uniqueID.toString(), [:])
            return super.visit(node, argument)
        }finally{
            names.remove(node.uniqueID.toString())
        }
    }

    @Override
    Void visit(VariableDeclaration node, ProcessingInfo argument) {
        def values = names.get(node.elementOwner.uniqueID.toString(), [:])
        values.put(node.name, null)
        return super.visit(node, argument)
    }

    @Override
    Void visit(MethodParameterModel node, ProcessingInfo argument) {
        def values = names.get(node.elementOwner.uniqueID.toString(), [:])
        values.put(node.name, null)
        return super.visit(node, argument)
    }

    @Override
    Void visit(NameModel node, ProcessingInfo argument) {
        if (node.namespace == null) {
            def values = names.get(node.elementOwner.typeOwner.uniqueID.toString(), [:])
            def localMethod = [:]
            if(node.elementOwner instanceof MethodModel){
                localMethod = names.get(node.elementOwner.uniqueID.toString(), [:])
            }
            if (values.containsKey(node.name) && !localMethod.containsKey(node.name)) {
                node.accessor.value = new FieldAccessModel(
                        field: node.name,
                        expression: new ThisModel(
                                objectReference: !values.get(node.name).flags.contains(ModelFlag.Static)
                        )
                )
            }/*else {
                def types = types.get(node.elementOwner.typeOwner.uniqueID.toString(), [:])
                if (types.containsKey(node.name)) {
                    node.accessor.value = $#
                }
            }*/
        }
        return null
        //return super.visit(node, argument)
    }

    @Override
    Void visit(MethodCallModel node, ProcessingInfo argument) {
        def values = names.get(node.elementOwner.typeOwner.uniqueID.toString(), [:])
        if (node.expression == null && values.containsKey(node.methodName))
            node.expression = new ThisModel(
                    objectReference: !values.get(node.methodName).flags.contains(ModelFlag.Static)
            )
        return super.visit(node, argument)
    }

    @Override
    Void visit(TypeReferenceModel node, ProcessingInfo argument) {
        //if(["T", "V", "K", "A", "S", "E"].contains(node.typeName))return super.visit(node, argument)
        if (node.typeName.length() == 1) return super.visit(node, argument)
        node.type = getType(currentType, node.typeName)
        if (node.type == null) {
            log.error "Type reference is not available: " + node.typeName
            argument.modifications++
            //getType(currentType, node.typeName)
        }
        return super.visit(node, argument)
    }

}

