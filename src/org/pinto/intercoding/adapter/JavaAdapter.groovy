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
package org.pinto.intercoding.adapter

import groovy.util.logging.Log4j2
import japa.parser.JavaParser
import japa.parser.ast.CompilationUnit
import japa.parser.ast.ImportDeclaration
import japa.parser.ast.PackageDeclaration
import japa.parser.ast.TypeParameter
import japa.parser.ast.body.*
import japa.parser.ast.comments.BlockComment
import japa.parser.ast.comments.JavadocComment
import japa.parser.ast.comments.LineComment
import japa.parser.ast.expr.*
import japa.parser.ast.stmt.*
import japa.parser.ast.type.*
import japa.parser.ast.visitor.GenericVisitorAdapter
import org.pinto.intercoding.intermediate.*

class JavaAdapterInfo {
    Stack<TypeModel> workingTypes = new Stack<TypeModel>()
    def coreNamespace = new NamespaceModel()
    def _workingNamespace = null
    List<TypeModel> currentTypes = []

    TypeModel getWorkingType() { workingTypes.peek() }

    NamespaceModel getWorkingNamespace() {
        return _workingNamespace == null ? coreNamespace : _workingNamespace
    }

    def setWorkingNamespace(NamespaceModel namespace) {
        _workingNamespace = namespace
    }
}

@Log4j2
class JavaAdapter implements AdapterModel {
    def visitor = new JavaVisitor()
    def options = new JavaAdapterInfo()

    NamespaceModel getCoreNamespace() { options.coreNamespace }

    NamespaceModel process(String path) {
        def file = new File(path)
        if (file.isDirectory()) {
            file.list().each { process path + "/" + it }
        } else {
            if (!path.endsWith(".java")) {
                //println "File ${path} ommited!"
            } else {
                log.info "Processing ${path}..."
                //def parser = JavaParser.parse file
                def parser = JavaParser.parse file, null, true
                parser.accept visitor, this.options
            }
        }
        return options.coreNamespace
    }
}
//@CompileStatic
@Log4j2
class JavaVisitor extends GenericVisitorAdapter<Object, JavaAdapterInfo> {
    def convertModifier(int modifier) {
        def modifiers = [
                (ModifierSet.PUBLIC)      : ModelFlag.Public,
                (ModifierSet.PRIVATE)     : ModelFlag.Private,
                (ModifierSet.PROTECTED)   : ModelFlag.Protected,
                (ModifierSet.STATIC)      : ModelFlag.Static,
                (ModifierSet.FINAL)       : ModelFlag.Final,
                (ModifierSet.SYNCHRONIZED): ModelFlag.ThreadSafe,
                (ModifierSet.VOLATILE)    : null,
                (ModifierSet.TRANSIENT)   : null,
                (ModifierSet.NATIVE)      : ModelFlag.Native,
                (ModifierSet.ABSTRACT)    : ModelFlag.Abstract,
                (ModifierSet.STRICTFP)    : null,

        ]
        def flags = EnumSet.noneOf(ModelFlag.class)
        for (Integer key in modifiers.keySet()) {
            if ((key & modifier) == key && modifiers[key] != null) {
                flags.add(modifiers[key])
            }
        }
        return flags;
    }

    def adaptDimensionType(TypeReferenceModel type, VariableDeclaratorId id) {
        return new TypeReferenceModel(
                typeName: type.typeName,
                //generic: type.generic,
                arrayDimensions: id.arrayCount,
                //!context: type.context,
                genericArguments: type.genericArguments,
        )
    }
    //Root
    @Override
    Object visit(CompilationUnit n, JavaAdapterInfo arg) {
        List<ImportModel> imports = []
        List<TypeModel> types = []
        def pakage = n.package?.accept(this, arg)
        //FIXING java.lang
        if(arg.workingNamespace.fullname != "java.lang"){
            imports.add(new ImportModel(
                    namespace: arg.coreNamespace.create("java.lang"),
                    wildcard: true,
            ))
        }
        imports.addAll(n.imports?.collect { (ImportModel) it.accept(this, arg) } ?: [])
        n.types?.collect { (TypeModel) it.accept(this, arg) }
        //!types.addAll(n.types?.collect { (TypeModel) it.accept(this, arg) } ?: [])
        //BUG:!!!types = types.findAll {it!=null && it instanceof Type} //Duckpatching
        arg.currentTypes.remove(null)
        //!types.remove(null)
        //types.each { it?.imports?.addAll(imports) } // it can be null if associated type was an annotation
        arg.currentTypes.each { it?.imports?.addAll(imports) } // it can be null if associated type was an annotation
        arg.workingNamespace.types.addAll(arg.currentTypes)
        arg.currentTypes.clear()
    }

    @Override
    Object visit(AnnotationDeclaration n, JavaAdapterInfo arg) {
        log.error "Annotations are not supported!"
        return null
    }

    @Override
    Object visit(AnnotationMemberDeclaration n, JavaAdapterInfo arg) {
        log.error "Annotations are not supported!"
        return null
    }

    @Override
    Object visit(ArrayAccessExpr n, JavaAdapterInfo arg) {
        new ArrayAccessModel(
                array: (ExpressionModel) n.name?.accept(this, arg),
                index: (ExpressionModel) n.index?.accept(this, arg)
        )
    }

    @Override
    Object visit(ArrayCreationExpr n, JavaAdapterInfo arg) {
        def array = (ArrayModel) n.initializer?.accept(this, arg) ?: new ArrayModel()
        array.arrayType = (TypeReferenceModel) n.type?.accept(this, arg)
        array.dimensions = n.dimensions?.collect { (ExpressionModel) it.accept(this, arg) } ?: []
        return array
    }

    @Override
    Object visit(ArrayInitializerExpr n, JavaAdapterInfo arg) {
        new ArrayModel(
                elements: n.values?.collect { (ExpressionModel) it.accept(this, arg) } ?: []
        )
    }

    @Override
    Object visit(AssertStmt n, JavaAdapterInfo arg) {
        new AssertModel(
                expression: (ExpressionModel) n.check?.accept(this, arg),
                message: n.message?.accept(this, arg).toString()
        )
    }

    @Override
    Object visit(AssignExpr n, JavaAdapterInfo arg) {
        def operators = [
                (AssignExpr.Operator.assign)        : AssignmentModel.Operators.Assign, // =
                (AssignExpr.Operator.plus)          : AssignmentModel.Operators.PlusAssign, // +=
                (AssignExpr.Operator.minus)         : AssignmentModel.Operators.MinusAssign, // -=
                (AssignExpr.Operator.star)          : AssignmentModel.Operators.TimesAssign, // *=
                (AssignExpr.Operator.slash)         : AssignmentModel.Operators.DivideAssign, // /=
                (AssignExpr.Operator.and)           : AssignmentModel.Operators.BinaryAndAssign, // &=
                (AssignExpr.Operator.or)            : AssignmentModel.Operators.BinaryOrAssign, // |=
                (AssignExpr.Operator.xor)           : AssignmentModel.Operators.BinaryXorAssign, // ^=
                (AssignExpr.Operator.rem)           : AssignmentModel.Operators.RemainderAssign, // %=
                (AssignExpr.Operator.lShift)        : AssignmentModel.Operators.LeftShiftAssign, // <<=
                (AssignExpr.Operator.rSignedShift)  : AssignmentModel.Operators.RightShiftAssign, // >>=
                (AssignExpr.Operator.rUnsignedShift): AssignmentModel.Operators.TripleRightShiftAssign, // >>>=
        ]
        new AssignmentModel(
                operator: operators[n.operator],
                left: (ExpressionModel) n.target?.accept(this, arg),
                right: (ExpressionModel) n.value?.accept(this, arg)
        )
    }

    @Override
    Object visit(BinaryExpr n, JavaAdapterInfo arg) {
        def operators = [
                (BinaryExpr.Operator.or)            : InfixOperationModel.Operators.Or, // ||
                (BinaryExpr.Operator.and)           : InfixOperationModel.Operators.And, // &&
                (BinaryExpr.Operator.binOr)         : InfixOperationModel.Operators.BinaryOr, // |
                (BinaryExpr.Operator.binAnd)        : InfixOperationModel.Operators.BinaryAnd, // &
                (BinaryExpr.Operator.xor)           : InfixOperationModel.Operators.BinaryXor, // ^
                (BinaryExpr.Operator.equals)        : InfixOperationModel.Operators.Equals, // ==
                (BinaryExpr.Operator.notEquals)     : InfixOperationModel.Operators.NotEquals, // !=
                (BinaryExpr.Operator.less)          : InfixOperationModel.Operators.Less, // <
                (BinaryExpr.Operator.greater)       : InfixOperationModel.Operators.Greater, // >
                (BinaryExpr.Operator.lessEquals)    : InfixOperationModel.Operators.LessOrEquals, // <=
                (BinaryExpr.Operator.greaterEquals) : InfixOperationModel.Operators.GreaterOrEquals, // >=
                (BinaryExpr.Operator.lShift)        : InfixOperationModel.Operators.LeftShift, // <<
                (BinaryExpr.Operator.rSignedShift)  : InfixOperationModel.Operators.RightShift, // >>
                (BinaryExpr.Operator.rUnsignedShift): InfixOperationModel.Operators.TripleGreaterShift, // >>>
                (BinaryExpr.Operator.plus)          : InfixOperationModel.Operators.Plus, // +
                (BinaryExpr.Operator.minus)         : InfixOperationModel.Operators.Minus, // -
                (BinaryExpr.Operator.times)         : InfixOperationModel.Operators.Times, // *
                (BinaryExpr.Operator.divide)        : InfixOperationModel.Operators.Divide, // /
                (BinaryExpr.Operator.remainder)     : InfixOperationModel.Operators.Remainder, // %
        ]
        new InfixOperationModel(
                operator: operators[n.operator],
                left: (ExpressionModel) n.left?.accept(this, arg),
                right: (ExpressionModel) n.right?.accept(this, arg)
        )
    }

    @Override
    Object visit(BlockStmt n, JavaAdapterInfo arg) {
        new BlockModel(
                newScope: true,
                statements: n.stmts?.collect { (StatementModel) it.accept(this, arg) } ?: []
        )
    }

    @Override
    Object visit(BooleanLiteralExpr n, JavaAdapterInfo arg) {
        return [(true): LiteralModel.True, (false): LiteralModel.False][n.value]
    }

    @Override
    Object visit(BreakStmt n, JavaAdapterInfo arg) {
        new BreakModel(
                label: n.id
        )
    }

    @Override
    Object visit(CastExpr n, JavaAdapterInfo arg) {
        new CastModel(
                expression: (ExpressionModel) n.expr?.accept(this, arg),
                targetType: (TypeReferenceModel) n.type?.accept(this, arg)
        )
    }

    @Override
    Object visit(CatchClause n, JavaAdapterInfo arg) {
        new TryCatchModel(
                errors: n.except?.types?.collect { (TypeReferenceModel) it.accept(this, arg) } ?: [],
                variableName: n.except?.id?.name ?: "",
                action: (StatementModel) n.catchBlock?.accept(this, arg)
        )
    }

    String toPythonString(String value, String enclosingChar = '"') {
        (value.contains("\\u") ? "u" : "") + enclosingChar + value + enclosingChar
    }

    @Override
    Object visit(CharLiteralExpr n, JavaAdapterInfo arg) {
        //groovy.json.StringEscapeUtils.escapeJava
        new LiteralModel(
                value: toPythonString(n.value, "'")
        )
    }

    @Override
    Object visit(ClassExpr n, JavaAdapterInfo arg) {
        new LiteralModel(
                value: n.type?.accept(this, arg)
        )
    }

    //List<TypeReference> createParentReferences(JavaAdapterInfo arg, List<ClassOrInterfaceDeclaration>... types) {
    List<TypeReferenceModel> createParentReferences(JavaAdapterInfo arg, List<List<ClassOrInterfaceDeclaration>> types) {
        List<TypeReferenceModel> references = []
        for (type in types) {
            if (type == null) continue
            references.addAll(type?.collect { (TypeReferenceModel) it.accept(this, arg) } ?: [])
        }
        if (references.size() == 0) {
            references.add(new TypeReferenceModel(
                    typeName: "java.lang.Object",
                    arrayDimensions: 0,
                    genericArguments: []
            ))
        }
        return references
    }

    GenericArgumentModel toGenericArgument(Object reference) {
        def argument = new GenericArgumentModel()
        if (reference instanceof TypeReferenceModel) {
            def typeReference = reference as TypeReferenceModel
            argument.name = typeReference.typeName
            argument.type = typeReference
        } else if (reference instanceof GenericParameterModel) { //Wildcard
            def wildcard = reference as GenericParameterModel
            argument.name = GenericParameterModel.WildcardSymbol
            argument.childRequests = wildcard.childRequests
            argument.parentRequests = wildcard.parentRequests
        } else {
            log.error "Generic argument with " + reference.toString()
            return null
        }
        return argument
    }

    @Override
    Object visit(ClassOrInterfaceDeclaration n, JavaAdapterInfo arg) {
        def type
        if (n.interface) {
            type = new InterfaceModel(
                    name: n.name,
            )
        } else {
            type = new ClassModel(
                    name: n.name,
            )
        }
        if (arg.workingTypes.size() > 0) {
            arg.workingType.innerTypes.add(type)
        }
        arg.workingTypes.push(type)
        arg.currentTypes.add(type)
        //arg.workingNamespace.types.add(type)
        //type.elements = n.members?.collect { it.accept(this, arg) } ?: []
        type.setElements([])
        n.members?.each { it.accept(this, arg) }
        type.genericParameters = n.typeParameters?.collect { (GenericParameterModel) it.accept(this, arg) } ?: []
        type.parents = createParentReferences(arg, [n.extends, n.implements])
        if(type.name=="Object" && arg.workingNamespace.fullname=="java.lang"){
            type.parents = type.parents.size()==1?[]:type.parents[0..-2]
        }
        type.documentation = (CommentModel) n.comment?.accept(this, arg)
        type.flags = convertModifier(n.modifiers)
        arg.workingTypes.pop()
        return type
    }

    @Override
    Object visit(ClassOrInterfaceType n, JavaAdapterInfo arg) {
        def name = ""
        for (def scope = n.scope; scope != null; scope = scope.scope) {
            name = scope.name + "." + name;
        }
        name += n.name
        new TypeReferenceModel(
                typeName: name,
                genericArguments: n.typeArgs?.collect { toGenericArgument(it.accept(this, arg)) } ?: [],
                arrayDimensions: 0
        )
    }


    @Override
    Object visit(ConditionalExpr n, JavaAdapterInfo arg) {
        new ConditionalModel(
                condition: (ExpressionModel) n.condition?.accept(this, arg),
                trueAction: (ExpressionModel) n.thenExpr?.accept(this, arg),
                falseAction: (ExpressionModel) n.elseExpr?.accept(this, arg)
        )
    }

    @Override
    Object visit(ConstructorDeclaration n, JavaAdapterInfo arg) {
        arg.workingType.elements.add(new MethodModel(
                returnType: null,
                action: (StatementModel) n.block?.accept(this, arg),
                parameters: n.parameters?.collect { (MethodParameterModel) it.accept(this, arg) } ?: [],
                genericParameters: n.typeParameters?.collect { (GenericParameterModel) it.accept(this, arg) } ?: [],
                //documentation: n.javaDoc?.accept(this, arg),
                documentation: (CommentModel) n.comment?.accept(this, arg),
                flags: convertModifier(n.modifiers),
                name: MethodModel.ConstructorName
        ))
    }

    @Override
    Object visit(ContinueStmt n, JavaAdapterInfo arg) {
        new ContinueModel(
                label: n.id
        )
    }

    @Override
    Object visit(DoStmt n, JavaAdapterInfo arg) {
        new DoModel(
                condition: (ExpressionModel) n.condition?.accept(this, arg),
                action: (StatementModel) n.body?.accept(this, arg)
        )
    }

    @Override
    Object visit(DoubleLiteralExpr n, JavaAdapterInfo arg) {
        new LiteralModel(
                value: n.value
        )
    }

    @Override
    Object visit(EmptyMemberDeclaration n, JavaAdapterInfo arg) {
        new EmptyModel()
    }

    @Override
    Object visit(EmptyStmt n, JavaAdapterInfo arg) {
        new EmptyModel()
    }

    @Override
    Object visit(EmptyTypeDeclaration n, JavaAdapterInfo arg) {
        new EmptyModel()
        return null
    }

    @Override
    Object visit(EnclosedExpr n, JavaAdapterInfo arg) {
        new ParenthesizedModel(
                expression: (ExpressionModel) n.inner?.accept(this, arg)
        )
    }

    @Override
    Object visit(EnumConstantDeclaration n, JavaAdapterInfo arg) {
        ClassOrInterfaceType enumType = new ClassOrInterfaceType()
        enumType.name = arg.workingType.name
        /*new EnumFieldModel(
                name: n.name,
                arguments: n.args?.collect { (ExpressionModel) it.accept(this, arg) } ?: [],
                documentation: (CommentModel) n.comment?.accept(this, arg),
                //type: (n.classBody == null) ? null : getTypeOrAnonymousType(enumType, n.classBody, arg)
                type: (n.classBody == null) ? new TypeReferenceModel(typeName: arg.workingType.name) : getTypeOrAnonymousType(enumType, n.classBody, arg)
        )*/
        //POTENTIAL BUGS FOR THIS:
        def type = (n.classBody == null) ? new TypeReferenceModel(typeName: arg.workingType.name) : getTypeOrAnonymousType(enumType, n.classBody, arg)
        def value = new FieldModel(
                name: n.name,
                type: type,
                flags: EnumSet.of(ModelFlag.Public, ModelFlag.Static),
                documentation: (CommentModel) n.comment?.accept(this, arg),
                defaultValue: new ObjectCreationModel(
                        type: type,
                        arguments: n.args?:[]
                )
        )
        arg.workingType.elements.add(value)
    }

    @Override
    Object visit(EnumDeclaration n, JavaAdapterInfo arg) {
        def type = new EnumerationModel(
                name: n.name
        )
        if (arg.workingTypes.size() > 0) {
            arg.workingType.innerTypes.add(type)
        }
        arg.workingTypes.push(type)
        arg.currentTypes.add(type)
        //arg.workingNamespace.types.add(type)
        //type.elements= n.members?.collect { it.accept(this, arg) }//?:[]
        n.members?.each { it.accept(this, arg) }//?:[]
        //type.values = n.entries?.collect { (EnumFieldModel) it.accept(this, arg) } ?: []
        n.entries?.collect { it.accept(this, arg) } ?: []
        type.documentation = (CommentModel) n.comment?.accept(this, arg)
        type.parents = createParentReferences(arg, [n.implements])
        type.flags = convertModifier(n.modifiers)
        arg.workingTypes.pop()
        return type
    }

    @Override
    Object visit(ExplicitConstructorInvocationStmt n, JavaAdapterInfo arg) {
        new MethodCallModel(
                methodName: "<<constructor>>",
                expression: n.this ? new ThisModel() : new SuperThisModel(), //Grammar issue with "super". Does it allow (expression).super()!!!
                arguments: n.args?.collect { (ExpressionModel) it.accept(this, arg) } ?: [],
                genericArguments: n.typeArgs?.collect { toGenericArgument(it.accept(this, arg)) } ?: [],
        )
    }

    @Override
    Object visit(ExpressionStmt n, JavaAdapterInfo arg) {
        n.expression?.accept(this, arg)
    }

    @Override
    Object visit(FieldAccessExpr n, JavaAdapterInfo arg) {
        new FieldAccessModel(
                field: n.field,
                expression: (ExpressionModel) n.scope?.accept(this, arg),
                genericArguments: n.typeArgs?.collect { toGenericArgument(it.accept(this, arg)) } ?: [],
        )
    }

    @Override
    Object visit(FieldDeclaration n, JavaAdapterInfo arg) {
        def fields = []
        //!def fields = new Block(newScope: false)
        def flags = convertModifier(n.modifiers)
        //def doc = n.javaDoc?.accept(this, arg)
        def doc = (CommentModel) n.comment?.accept(this, arg)
        def type_ = (TypeReferenceModel) n.type?.accept(this, arg)
        for (field in n.variables) {
            if (field.id.name == "BLOCKS") {
                int i = 0
            }
            //fields.statements.add(new Field(
            fields.add(new FieldModel(
                    varDeclaration: (VariableDeclaration) field.accept(this, arg),
                    documentation: doc,
                    type: adaptDimensionType(type_, field.id),
                    flags: flags,
            ))
        }
        arg.workingType.elements.addAll(fields)
        return []//fields.size() == 1 ? fields[0] : fields
    }

    @Override
    Object visit(ForeachStmt n, JavaAdapterInfo arg) {
        def var = (StatementModel) n.variable?.accept(this, arg)
        def vars
        if (var instanceof BlockModel)
            vars = var.statements.toList()
        else
            vars = [var]
        new ForeachModel(
                variables: vars,
                generator: (ExpressionModel) n.iterable?.accept(this, arg),
                action: (StatementModel) n.body?.accept(this, arg),
        )
    }

    @Override
    Object visit(ForStmt n, JavaAdapterInfo arg) {
        def statement = new ForModel(
                //initializers: n.init?.collect { (Expression)it.accept(this, arg) }?:[],
                updaters: n.update?.collect { (ExpressionModel) it.accept(this, arg) } ?: [],
                condition: (ExpressionModel) n.compare?.accept(this, arg),
                action: (StatementModel) n.body?.accept(this, arg),
        )
        statement.initializers = []
        for (initializer in n.init) {
            StatementModel modelled = (StatementModel) initializer.accept(this, arg)
            if (modelled instanceof BlockModel) {
                statement.initializers.addAll((modelled as BlockModel).statements.collect { (ExpressionModel) it })
            } else {
                statement.initializers.add((ExpressionModel) modelled)
            }
        }
        return statement
    }

    @Override
    Object visit(IfStmt n, JavaAdapterInfo arg) {
        new IfModel(
                condition: (ExpressionModel) n.condition?.accept(this, arg),
                trueAction: (StatementModel) n.thenStmt?.accept(this, arg),
                falseAction: (StatementModel) n.elseStmt?.accept(this, arg),
        )
    }

    @Override
    Object visit(ImportDeclaration n, JavaAdapterInfo arg) {
        ImportModel anImport = arg.coreNamespace.createImport n.name?.toString() + (n.asterisk ? ".*" : ""), false
        return anImport
    }

    @Override
    Object visit(InitializerDeclaration n, JavaAdapterInfo arg) {
        arg.workingType.elements.add(new MethodModel(
                name: MethodModel.ConstructorName,
                flags: EnumSet.of(n.static ? ModelFlag.Static : ModelFlag.Private),
                returnType: null,
                action: (StatementModel) n.block?.accept(this, arg),
                parameters: [],
                genericParameters: [],
                //documentation: n.javaDoc?.accept(this, arg),
                documentation: (CommentModel) n.comment?.accept(this, arg),
        )
        )
    }

    @Override
    Object visit(InstanceOfExpr n, JavaAdapterInfo arg) {
        new IsInstanceModel(
                type: (TypeReferenceModel) n.type?.accept(this, arg),
                expression: (ExpressionModel) n.expr?.accept(this, arg),
        )
    }

    @Override
    Object visit(IntegerLiteralExpr n, JavaAdapterInfo arg) {
        new LiteralModel(
                value: n.value
        )
    }

    @Override
    Object visit(IntegerLiteralMinValueExpr n, JavaAdapterInfo arg) {
        new LiteralModel(
                value: n.value
        )
    }

    @Override
    Object visit(JavadocComment n, JavaAdapterInfo arg) {
        new CommentModel(
                multiline: true,
                content: n.content.split("\n").collect { (it as String).trim().replaceFirst("^\\*\\s*", "") }.join("\n")
        )
    }

    @Override
    Object visit(LabeledStmt n, JavaAdapterInfo arg) {
        new LabeledModel(
                label: n.label,
                action: (StatementModel) n.stmt?.accept(this, arg),
        )
    }

    @Override
    Object visit(LongLiteralExpr n, JavaAdapterInfo arg) {
        new LiteralModel(
                value: n.value
        )
    }

    @Override
    Object visit(LongLiteralMinValueExpr n, JavaAdapterInfo arg) {
        new LiteralModel(
                value: n.value
        )
    }

    @Override
    Object visit(MarkerAnnotationExpr n, JavaAdapterInfo arg) {
        log.error "Annotations are not supported!"
        return null
    }

    @Override
    Object visit(MemberValuePair n, JavaAdapterInfo arg) {
        new AssignmentModel(
                left: new NameModel(name: n.name),
                right: (ExpressionModel) n.value?.accept(this, arg),
                operator: AssignmentModel.Operators.Assign
        )
    }

    @Override
    Object visit(MethodCallExpr n, JavaAdapterInfo arg) {
        new MethodCallModel(
                methodName: n.name,
                expression: (ExpressionModel) n.scope?.accept(this, arg),
                arguments: n.args?.collect { (ExpressionModel) it.accept(this, arg) } ?: [],
                genericArguments: n.typeArgs?.collect { toGenericArgument(it.accept(this, arg)) } ?: [],
        )
    }

    @Override
    Object visit(MethodDeclaration n, JavaAdapterInfo arg) {
        arg.workingType.elements.add(new MethodModel(
                name: n.name == "" && ModifierSet.isStatic(n.modifiers) ? MethodModel.ConstructorName : n.name,
                flags: convertModifier(n.modifiers),
                returnType: (TypeReferenceModel) n.type?.accept(this, arg),
                action: n.body == null ? new EmptyModel() : (StatementModel) n.body?.accept(this, arg),
                parameters: n.parameters?.collect { (MethodParameterModel) it.accept(this, arg) } ?: [],
                genericParameters: n.typeParameters?.collect { (GenericParameterModel) it.accept(this, arg) } ?: [],
                //documentation: n.javaDoc?.accept(this, arg),
                documentation: (CommentModel) n.comment?.accept(this, arg),
        ))
    }

    @Override
    Object visit(NameExpr n, JavaAdapterInfo arg) {
        return new NameModel(name: n.name)
        //return arg.workingNamespace.createName(n.name)
    }

    @Override
    Object visit(NormalAnnotationExpr n, JavaAdapterInfo arg) {
        log.error "Annotations are not supported!"
        return null
    }

    @Override
    Object visit(NullLiteralExpr n, JavaAdapterInfo arg) {
        LiteralModel.None
    }

    TypeReferenceModel getTypeOrAnonymousType(ClassOrInterfaceType parsedType, List<BodyDeclaration> anonymousClassBody, JavaAdapterInfo arg) {
        //TODO: Unify parents and implementations
        def type = (TypeReferenceModel) parsedType?.accept(this, arg)
        if (anonymousClassBody != null) {
            def anonymousClassName = ClassModel.anonymousName arg.workingNamespace, arg.workingType
            def anonymousClass = new ClassModel(
                    ///elements: anonymousClassBody?.collect { it.accept(this, arg) }?:[],
                    parents: [type],
                    genericParameters: [],
                    //documentation: null,
                    name: anonymousClassName,
                    flags: EnumSet.of(ModelFlag.Protected),
            )
            anonymousClass.typeOwner = arg.workingType
            arg.workingTypes.push(anonymousClass)
            arg.currentTypes.add(anonymousClass)
            //anonymousClass.elements= anonymousClassBody?.collect { it.accept(this, arg) }?:[]
            anonymousClassBody?.each { it.accept(this, arg) } ?: []
            arg.workingNamespace.types.add(anonymousClass)
            type = new TypeReferenceModel(
                    typeName: anonymousClassName,
                    arrayDimensions: 0,
                    genericArguments: []
            )
            arg.workingTypes.pop()
        }
        return type
    }

    @Override
    Object visit(ObjectCreationExpr n, JavaAdapterInfo arg) {
        new ObjectCreationModel(
                type: getTypeOrAnonymousType(n.type, n.anonymousClassBody, arg),
                arguments: n.args?.collect { (ExpressionModel) it.accept(this, arg) } ?: [],
                genericArguments: n.typeArgs?.collect { toGenericArgument(it.accept(this, arg)) } ?: [],
        )
    }

    @Override
    Object visit(PackageDeclaration n, JavaAdapterInfo arg) {
        arg.workingNamespace = arg.coreNamespace.create(n.name?.toString())
    }

    @Override
    Object visit(Parameter n, JavaAdapterInfo arg) {
        new MethodParameterModel(
                name: n.id?.name,
                type: (TypeReferenceModel) n.type?.accept(this, arg),
                //defaultValue: n. . .accept(this, arg),
                flags: convertModifier(n.modifiers),
                variadic: n.varArgs
        )
    }

    @Override
    Object visit(MultiTypeParameter n, JavaAdapterInfo arg) {
        log.error "MultiTypeParameter is not supported!"
        return null
    }

    @Override
    Object visit(PrimitiveType n, JavaAdapterInfo arg) {
        [
                (PrimitiveType.Primitive.Boolean): new TypeReferenceModel(typeName: "java.lang.Boolean"),
                (PrimitiveType.Primitive.Byte)   : new TypeReferenceModel(typeName: "java.lang.Byte"),
                (PrimitiveType.Primitive.Char)   : new TypeReferenceModel(typeName: "java.lang.Character"),
                (PrimitiveType.Primitive.Double) : new TypeReferenceModel(typeName: "java.lang.Double"),
                (PrimitiveType.Primitive.Float)  : new TypeReferenceModel(typeName: "java.lang.Float"),
                (PrimitiveType.Primitive.Int)    : new TypeReferenceModel(typeName: "java.lang.Integer"),
                (PrimitiveType.Primitive.Long)   : new TypeReferenceModel(typeName: "java.lang.Long"),
                (PrimitiveType.Primitive.Short)  : new TypeReferenceModel(typeName: "java.lang.Short"),
        ][n.type]
    }

    @Override
    Object visit(QualifiedNameExpr n, JavaAdapterInfo arg) {
        //arg.workingNamespace.createName n.qualifier?.toString()
        new NameModel(name: n.qualifier?.toString())
    }

    @Override
    Object visit(ReferenceType n, JavaAdapterInfo arg) {
        def type = (TypeReferenceModel) n.type.accept(this, arg)
        type.arrayDimensions = n.arrayCount
        return type
    }

    @Override
    Object visit(ReturnStmt n, JavaAdapterInfo arg) {
        new ReturnModel(
                returnValue: (ExpressionModel) n.expr?.accept(this, arg)
        )
    }

    @Override
    Object visit(SingleMemberAnnotationExpr n, JavaAdapterInfo arg) {
        log.error "Annotations are not supported!"
        return null
    }

    @Override
    Object visit(StringLiteralExpr n, JavaAdapterInfo arg) {
        new LiteralModel(
                value: toPythonString(n.value, '"')
        )
    }

    @Override
    Object visit(SuperExpr n, JavaAdapterInfo arg) {
        new SuperThisModel(
                namespace: expressionToNamespace((ExpressionModel) n.classExpr?.accept(this, arg), arg)
        )
    }

    @Override
    Object visit(SwitchEntryStmt n, JavaAdapterInfo arg) {
        new CaseSwitchModel(
                label: (ExpressionModel) n.label?.accept(this, arg),
                action: new BlockModel(
                        //I'm not sure about this
                        newScope: true,
                        statements: n.stmts?.collect { (StatementModel) it.accept(this, arg) } ?: [],
                )
        )
    }

    @Override
    Object visit(SwitchStmt n, JavaAdapterInfo arg) {
        new SwitchModel(
                condition: (ExpressionModel) n.selector?.accept(this, arg),
                statements: n.entries?.collect { (CaseSwitchModel) it.accept(this, arg) } ?: []
        )
    }

    @Override
    Object visit(SynchronizedStmt n, JavaAdapterInfo arg) {
        def block = (BlockModel) n.block?.accept(this, arg) ?: new BlockModel()
        block.threadSafeGuard = n.expr?.accept(this, arg)
        block.newScope = true
        return block
    }

    NamespaceModel expressionToNamespace(ExpressionModel expression, JavaAdapterInfo arg) {
        if (expression instanceof NameModel) {
            return arg.coreNamespace.create((expression as NameModel).fullname)
        } else if (expression != null) {
            log.error "I cannot handle " + expression.toString() + " with 'This' expression."
            return null
        }
        return null
    }

    @Override
    Object visit(ThisExpr n, JavaAdapterInfo arg) {
        new ThisModel(
                namespace: expressionToNamespace((ExpressionModel) n.classExpr?.accept(this, arg), arg)
        )
    }

    @Override
    Object visit(ThrowStmt n, JavaAdapterInfo arg) {
        new ThrowModel(
                expression: (ExpressionModel) n.expr?.accept(this, arg)
        )
    }

    @Override
    Object visit(TryStmt n, JavaAdapterInfo arg) {
        def tryStatement = n.tryBlock?.accept(this, arg)
        if (n.resources != null && !n.resources.empty) {
            if (n.finallyBlock == null && n.catchs == null) {
                return new WithResourceModel(
                        resources: n.resources?.collect { (StatementModel) it.accept(this, arg) } ?: [],
                        action: (StatementModel) tryStatement
                )
            } else {
                return new WithResourceModel(
                        resources: n.resources?.collect { (StatementModel) it.accept(this, arg) } ?: [],
                        action: new TryModel(
                                verifiedAction: (StatementModel) tryStatement,
                                finallyAction: (StatementModel) n.finallyBlock?.accept(this, arg),
                                catchErrors: n.catchs?.collect { (TryCatchModel) it.accept(this, arg) } ?: []
                        )
                )
            }
        }
        new TryModel(
                verifiedAction: (StatementModel) tryStatement,
                finallyAction: (StatementModel) n.finallyBlock?.accept(this, arg),
                catchErrors: n.catchs?.collect { (TryCatchModel) it.accept(this, arg) } ?: []
        )
    }

    @Override
    Object visit(TypeDeclarationStmt n, JavaAdapterInfo arg) {
        n.typeDeclaration.accept(this, arg)
        log.warn "Local classes are converted to inner classes"
        return null
        //return super.visit(n, arg)
    }


    @Override
    Object visit(TypeParameter n, JavaAdapterInfo arg) {
        new GenericParameterModel(
                name: n.name,
                instantiable: true,
                parentRequests: n.typeBound?.collect { (TypeReferenceModel) it.accept(this, arg) } ?: [],
                childRequests: []
        )
    }

    @Override
    Object visit(UnaryExpr n, JavaAdapterInfo arg) {
        def preOperators = [
                (UnaryExpr.Operator.positive)    : PrefixOperationModel.Operators.Plus, // +
                (UnaryExpr.Operator.negative)    : PrefixOperationModel.Operators.Minus, // -
                (UnaryExpr.Operator.preIncrement): PrefixOperationModel.Operators.AutoIncrement, // ++
                (UnaryExpr.Operator.preDecrement): PrefixOperationModel.Operators.AutoDecrement, // --
                (UnaryExpr.Operator.not)         : PrefixOperationModel.Operators.Not, // !
                (UnaryExpr.Operator.inverse)     : PrefixOperationModel.Operators.Complement, // ~
        ]
        def postOperators = [
                (UnaryExpr.Operator.posIncrement): PostfixOperationModel.Operators.AutoIncrement, // ++
                (UnaryExpr.Operator.posDecrement): PostfixOperationModel.Operators.AutoDecrement, // --
        ]
        if (preOperators.containsKey(n.operator))
            new PrefixOperationModel(
                    operator: preOperators[n.operator],
                    operand: (ExpressionModel) n.expr?.accept(this, arg)
            )
        else
            new PostfixOperationModel(
                    operator: postOperators[n.operator],
                    operand: (ExpressionModel) n.expr?.accept(this, arg)
            )
    }

    @Override
    Object visit(VariableDeclarationExpr n, JavaAdapterInfo arg) {
        def vars = new BlockModel(newScope: false)
        def flags = convertModifier(n.modifiers)
        def type_ = (TypeReferenceModel) n.type?.accept(this, arg)
        for (field in n.vars) {
            def var = (VariableDeclaration) field.accept(this, arg)
            var.type = adaptDimensionType(type_, field.id)
            var.flags = flags
            vars.statements.add(var)
        }
        return vars.statements.size() == 1 ? vars.statements[0] : vars
    }

    @Override
    Object visit(VariableDeclarator n, JavaAdapterInfo arg) {
        new VariableDeclaration(
                name: n.id?.name,
                type: null,
                defaultValue: (ExpressionModel) n.init?.accept(this, arg),
                flags: null,
        )
    }

    @Override
    Object visit(VariableDeclaratorId n, JavaAdapterInfo arg) {
        return n.name
    }

    @Override
    Object visit(VoidType n, JavaAdapterInfo arg) {
        return TypeReferenceModel.Void
    }

    @Override
    Object visit(WhileStmt n, JavaAdapterInfo arg) {
        new WhileModel(
                condition: (ExpressionModel) n.condition?.accept(this, arg),
                action: (StatementModel) n.body?.accept(this, arg)
        )
    }

    @Override
    Object visit(WildcardType n, JavaAdapterInfo arg) {
        new GenericParameterModel(
                name: GenericParameterModel.WildcardSymbol,
                instantiable: true,
                parentRequests: [(TypeReferenceModel) n.extends?.accept(this, arg)],
                childRequests: [(TypeReferenceModel) n.super?.accept(this, arg)]
        )
    }

    @Override
    Object visit(BlockComment n, JavaAdapterInfo arg) {
        new CommentModel(
                content: n.content,
                multiline: true,
        )
    }

    @Override
    Object visit(LineComment n, JavaAdapterInfo arg) {
        new CommentModel(
                content: n.content,
                multiline: false,
        )
    }
}
