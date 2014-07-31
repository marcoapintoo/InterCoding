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
package org.pinto.intercoding.format

import groovy.util.logging.Log4j2
import org.pinto.intercoding.intermediate.*

class PythonFormatOption {
    def tabCharacter = "\t"

    def indent(content) {
        tabCharacter + (content ?: "").trim().replace("\n", "\n" + tabCharacter)
    }
}

@Log4j2
class PythonFormat extends GenericModelVisitor<PythonFormatOption, String> implements FormatterModel {
    NamespaceModel coreNamespace
    private def currentNamespace
    def commonMessage = ""
    def rootDirectory = "."
    def options = new PythonFormatOption()

    private def getFileMessage() {
        new CommentModel(
                content: commonMessage,
                multiline: true
        ).accept(this, options)
    }

    /*private def walkNamespaces(Namespace node, PythonFormatOption options){
        def codes = []
        currentNamespace = node
        codes.add()
        for(namespace in node?.namespaces){
            codes.addAll(walkNamespaces(namespace, options))
            currentNamespace = node
        }
        return codes
    }*/

    String format() {
        coreNamespace?.accept(this, options)
    }

    private def indent(argument, element) {
        def result = element?.accept(this, argument)
        if (element instanceof BlockModel && element.newScope) {
            if (result.trim() == "") {
                return argument.indent(new EmptyModel().accept(this, argument))
            }
            return result //Why??
        }
        if (result == "") {
            return argument.indent(new EmptyModel().accept(this, argument))
        }
        return argument.indent(result)
    }

    String defaultImports() {
        "from pinto import *"
    }

    def writeCode(NamespaceModel node, String code) {
        //def fullname = (node.name == "" ? "__root__" : node.name) + ".py"
        //def rootPath = rootDirectory + "/" + (node.parent?.fullname ?: "").replace(".", "/")
        def rootPath = rootDirectory + "/" + (node?.fullname ?: "").replace(".", "/")
        new File(rootPath).mkdirs()
        if ((code ?: "").trim() != "") {
            code = defaultImports() + "\n" + code
        }
        new File(rootPath + "/__init__.py").write("${fileMessage}\n${code}")
        //new File(rootPath + "/__init__.py").write(fileMessage)
        //new File(rootPath + "/" + fullname).write("${fileMessage}\n${code}")
    }

    @Override
    String visit(NamespaceModel node, PythonFormatOption argument) {
        def formatted
        def fullname = node.fullname
        log.info "Analyzing package: " + fullname
        currentNamespace = node
        writeCode(node, node.types.collect { it?.accept(this, argument) ?: "" }.join("\n"))
        for (namespace in node.namespaces) {
            namespace?.accept(this, argument)
            currentNamespace = node
        }
        /*
        println "Analyzing: " + fullname
        for(type in node.types){
            println type?.accept(this, argument)
            currentNamespace = node
        }*/
        return formatted
    }


    @Override
    String visit(ArrayModel node, PythonFormatOption argument) {
        def formatted
        // We omitted dimensions in array
        // def dimension = node.dimensions?.collectAll{it?.accept(this, argument)}
        // dimension = dimension?: ""
        def nodes = node.elements?.collectAll { it?.accept(this, argument) } ?: []
        formatted = "[${nodes.join(', ')}]"
        return formatted
    }

    @Override
    String visit(ArrayAccessModel node, PythonFormatOption argument) {
        def formatted
        def array = node.array?.accept(this, argument) ?: ""
        def index = node.index?.accept(this, argument) ?: ""
        formatted = "(${array})[$index]"
        return formatted
    }

    @Override
    String visit(AssertModel node, PythonFormatOption argument) {
        def formatted
        def expression = node.expression?.accept(this, argument) ?: ""
        def message = node.message//?.accept(this, argument) ?: ""
        formatted = "if ${expression}:\n"
        formatted += argument.indent("print ${message}\nexit(1)")
        formatted += "\n"
        return formatted
    }

    @Override
    String visit(AssignmentModel node, PythonFormatOption argument) {
        def formatted
        def left = node.left?.accept(this, argument) ?: ""
        def right = node.right?.accept(this, argument) ?: ""
        def operator = node.operator?.accept(this, argument) ?: ""
        formatted = "${left} ${operator} ${right}"
        return formatted
    }

    @Override
    String visit(BlockModel node, PythonFormatOption argument) {
        def formatted
        def statements = applyAndJoin node.statements, argument, "\n"
        //TODO: node.threadSafe is ignored!
        formatted = node.newScope ? argument.indent(statements) : statements
        return formatted
    }

    @Override
    String visit(BreakModel node, PythonFormatOption argument) {
        //TODO: breaks with label in python
        def formatted
        def label = node.label ?: ""
        formatted = "break ${label}"
        return formatted
    }

    @Override
    String visit(CaseSwitchModel node, PythonFormatOption argument) {
        log.error "Switch not supported yet"
        return null
    }

    @Override
    String visit(CastModel node, PythonFormatOption argument) {
        node.expression?.accept(this, argument)
        //def formatted
        //return formatted
    }

    //!private def long anonymousCounter = 1

    //private def applyAndJoin(subnode, PythonFormatOption argument, separator = ", ", defaultList = []) {
    private def applyAndJoin(subnode, argument, separator = ", ", defaultList = []) {
        (subnode?.collect { it?.accept(this, argument) } ?: defaultList).join(separator)
    }


    class ClassFormatter {
        def visitor = null
        def node = null
        def argument = null
        def name = null
        def useDocs = true
        def useParents = true
        def useImports = true
        def useElements = true
        def useValues = false

        def format() {
            def formatted
            def name = name ?: node.name
            def documentation = useDocs ? (node.documentation?.accept(visitor, argument) ?: "" ?: "") : ""
            def parents = useParents ? (visitor.applyAndJoin(node.parents, argument, ", ", ["object"])) : ""
            //def implementations = useImplementations ? (visitor.applyAndJoin(node.implementations, argument, ", ")) : ""
            //implementations = implementations != "" ? ", " + implementations : implementations
            def imports = useImports ? (visitor.applyAndJoin(node.imports, argument, "\n")) : ""
            def elements = useElements ? (visitor.applyAndJoin(node.elements, argument, "\n")) : ""
            def values = useValues ? (visitor.applyAndJoin(node.values, argument, "\n")) : ""
            formatted = "${documentation}\n${imports}\nclass ${name}(${parents}):\n"
            if (values != "") {
                formatted += argument.indent(values)
                formatted += "\n"
            }
            if (elements != "") {
                formatted += argument.indent(elements)
                formatted += "\n"
            }
            if (elements == "" && values == "") {
                formatted += argument.indent(new EmptyModel().accept(visitor, argument))
                formatted += "\n"
            }
            formatted += "\n${node.name}.__class_init__()"
            formatted += "\n"
            //TODO: Flags ignored
            //def flags = node.flags
            //TODO: Elements ignored
            //def genericParameters = node.genericParameters
            return formatted
        }
    }
    /*
    String visitClass(varargs, node=null, argument=null, name = null, useDocs = true, useParents = true, useImplementations = true, useImports=true, useElements=true, useValues=false){
        name = name?: node.name
        def documentation = useDocs?(node.documentation?.accept(this, argument)?:""?:""):""
        def parents = useParents? (applyAndJoin(node.parents, argument, ", ", ["object"])): ""
        def implementations = useImplementations? (applyAndJoin(node.implementations, argument, ", ")): ""
        implementations = implementations!=""?", " + implementations: implementations
        def imports = useImports?(applyAndJoin(node.imports, argument, "\n")): ""
        def elements = useElements?(applyAndJoin(node.elements, argument, "\n")):""
        def values = useValues?(applyAndJoin(node.values, argument, "\n")):""
        formatted = "${documentation}\n${imports}\nclass ${name}(${parents}${implementations}):"
        formatted += argument.indent(values)
        formatted += argument.indent(elements)
        //TODO: Flags ignored
        //def flags = node.flags
        //TODO: Elements ignored
        //def genericParameters = node.genericParameters
        return formatted
    }*/

    //!private String getAnonymousClassName() {
    //!    "anonymousClass${anonymousCounter}"
    //!}

    @Override
    String visit(ClassModel node, PythonFormatOption argument) {
        log.info "Formatting... ${node.name}"
        def formatted
        //def name = getAnonymousClassName()
        /*if (node.anonymous) {
            anonymousCounter++
        } else {*/
        //def name = node.name
        //}
        formatted = new ClassFormatter(
                visitor: this,
                node: node,
                argument: argument,
                useImports: true,
                useDocs: true,
                name: node.name,
                useParents: true,
                useElements: true,
                useValues: false
        ).format()
        return formatted
    }

    @Override
    String visit(CommentModel node, PythonFormatOption argument) {
        def formatted
        def content = node.content
        if (node.multiline) {
            formatted = "\"\"\"${content}\"\"\"\n"
        } else {
            content = content.trim().replace("\n", "\n# ")
            formatted = "# ${content}\n"
        }
        return formatted
    }

    @Override
    String visit(ConditionalModel node, PythonFormatOption argument) {
        def formatted
        def trueCond = node.condition?.accept(this, argument)
        def ifCond = node.condition?.accept(this, argument)
        def elseCond = node.condition?.accept(this, argument)
        formatted = "( (${trueCond}) if (${ifCond}) else (${elseCond}) )"
        return formatted
    }
/*
    @Override
    String visit(Context node, PythonFormatOption argument) {
        def formatted
        return formatted
    }
*/

    @Override
    String visit(ContinueModel node, PythonFormatOption argument) {
        def formatted = "continue"
        return formatted
    }

    @Override
    String visit(DoModel node, PythonFormatOption argument) {
        def formatted
        def condition = node.condition?.accept(this, argument)
        //def action = node.action?.accept(this, argument)
        formatted = "while ${condition}:\n"
        formatted += indent(argument, node.action)
        return formatted
    }
/*
    @Override
    String visit(Element node, PythonFormatOption argument) {
        def formatted
        return formatted
    }
*/

    @Override
    String visit(EmptyModel node, PythonFormatOption argument) {
        def formatted = "pass"
        return formatted
    }

    @Override
    String visit(EnumerationModel node, PythonFormatOption argument) {
        def formatted
        formatted = new ClassFormatter(
                visitor: this,
                node: node,
                argument: argument,
                useImports: true,
                useDocs: true,
                name: node.name,
                useParents: true,
                useElements: true,
                useValues: true
        ).format()
        node.values?.each {
            def enumReference = new TypeReferenceModel(
                    typeName: node.name,
                    arrayDimensions: 0
            )
            def objectInvocation = new ObjectCreationModel(
                    type: it.type != null ? it.type : enumReference,
                    arguments: it.arguments
            )
            def varInit = new AssignmentModel(
                    left: new FieldAccessModel(
                            //expression: currentNamespace.createName(node.name),
                            expression: new NameModel(name: node.name),
                            field: it.name
                    ),
                    operator: AssignmentModel.Operators.Assign,
                    right: objectInvocation
            )
            formatted += varInit.accept(this, argument)
            formatted += "\n"
        }
        return formatted
    }

    @Override
    String visit(EnumFieldModel node, PythonFormatOption argument) {
        def formatted
        //TODO: Flags ignored
        def name = node.name
        //def args = node.arguments?.collect { it?.accept(this, argument) } ?: []
        //args = args.join(", ")
        formatted = "${name} = None"
        //formatted = "${name} = ${args}"
        return formatted
    }
/*
    @Override
    String visit(Expression node, PythonFormatOption argument) {
        def formatted
        return formatted
    }
*/

    @Override
    String visit(FieldModel node, PythonFormatOption argument) {
        def formatted
        def defaultValue = node.defaultValue?.accept(this, argument) ?: LiteralModel.None.accept(this, argument)
        //def type=node.type?.accept(this, argument)
        def name = node.name
        //TODO: Ignore flags
        //def flags =node.flags
        def documentation = node.documentation?.accept(this, argument) ?: ""
        formatted = "${documentation}${name} = ${defaultValue}\n"
        return formatted
    }

    @Override
    String visit(FieldAccessModel node, PythonFormatOption argument) {
        def formatted
        def expression = node.expression?.accept(this, argument)
        def field = node.field//?.accept(this, argument)
        formatted = "${expression}.${field}"
        return formatted
    }
/*
    @Override
    String visit(Flag node, PythonFormatOption argument) {
        def formatted
        return formatted
    }
*/

    @Override
    String visit(ForModel node, PythonFormatOption argument) {
        log.error "For action must be converted into While!"
        return null
    }

    @Override
    String visit(ForeachModel node, PythonFormatOption argument) {
        def formatted
        def variables = node.variables.collect { it.name }.join(", ")
        //def action = node.action?.accept(this, argument)
        def generator = node.generator?.accept(this, argument)
        formatted = "for ${variables} in ${generator}:\n"
        formatted += indent(argument, node.action)
        return formatted
    }

    @Override
    String visit(GenericArgumentModel node, PythonFormatOption argument) {
        return ""
    }
/*
    @Override
    String visit(GenericModel node, PythonFormatOption argument) {
        def formatted
        return formatted
    }
*/

    @Override
    String visit(GenericParameterModel node, PythonFormatOption argument) {
        return ""
    }

    @Override
    String visit(IfModel node, PythonFormatOption argument) {
        def formatted
        def condition = node.condition?.accept(this, argument)
        //def trueAction=node.trueAction?.accept(this, argument)
        //def falseAction=node.falseAction?.accept(this, argument)
        formatted = "if ${condition}:\n"
        formatted += indent(argument, node.trueAction)
        formatted += "\n"
        if (node.falseAction != null && !(node.falseAction instanceof EmptyModel)) {
            if (node.falseAction instanceof IfModel) {
                formatted += "el" + node.falseAction.accept(this, argument)
            } else {
                formatted += "else:\n"
                formatted += indent(argument, node.falseAction)
            }
            formatted += "\n"
        }
        return formatted
    }

    @Override
    String visit(ImportModel node, PythonFormatOption argument) {
        def formatted
        if (node.namespace == null) {
            //String name = node.name.substring(0, node.name.indexOf(".")>0?node.name.indexOf("."):node.name.length())
            formatted = "import ${node.name}"//TODO:FIX
        } else {
            def name = node.namespace.fullname
            if (node.wildcard) {
                formatted = "from ${name} import *"
            } else {
                formatted = "from ${name} import ${node.name}"
            }
        }
        return formatted
    }

    @Override
    String visit(InfixOperationModel node, PythonFormatOption argument) {
        def formatted
        def left = node.left?.accept(this, argument)
        def right = node.right?.accept(this, argument)
        def operator = node.operator?.accept(this, argument)
        formatted = "${left} ${operator} ${right}"
        return formatted
    }

    @Override
    String visit(LabeledModel node, PythonFormatOption argument) {
        log.error "Labels are not supported yet"
        return null
    }

    @Override
    String visit(IsInstanceModel node, PythonFormatOption argument) {
        def formatted
        def expression = node.expression?.accept(this, argument)
        def type = node.type?.accept(this, argument)
        formatted = "isinstance(${expression}, ${type})"
        return formatted
    }

    @Override
    String visit(InterfaceModel node, PythonFormatOption argument) {
        def formatted
        formatted = new ClassFormatter(
                visitor: this,
                node: node,
                argument: argument,
                useImports: true,
                useDocs: true,
                name: node.name,
                useParents: true,
                useElements: true,
                useValues: false
        ).format()
        return formatted
    }

    @Override
    String visit(LiteralModel node, PythonFormatOption argument) {
        def value
        if (node.value instanceof BaseModel) {
            value = (node.value as BaseModel)?.accept(this, argument)
        } else if(node.value instanceof String){
            String val = node.value as String
            if (val.isBigDecimal()) {
                value = val.toBigDecimal().toString()
            } else if (val.isBigInteger()) {
                value = val.toBigInteger().toString()
            } else if (val.isDouble()) {
                value = val.toDouble().toString()
            } else if (val.isFloat()) {
                value = val.toFloat().toString()
            } else if (val.isInteger()) {
                value = val.toInteger().toString()
            } else if (val.isLong()) {
                value = val.toLong().toString()
            }else{
                value = val
            }
            //println node.value.toString() + " <---> " + value.toString()
        }else {
            value = (node.value ?: "None").toString()
        }
        //println node.value.toString() + "(" + node.value.class.toString() + ") <---> " + value.toString()
        def formatted = "${value}"
        return formatted
    }

    @Override
    String visit(MethodModel node, PythonFormatOption argument) {
        def formatted = ""
        def name = node.constructor ? (node.flags.contains(ModelFlag.Static) ? "__class_init__" : "__init__") : node.name
        //def action = node.action?.accept(this, argument)
        def documentation = node.documentation == null ? "" : (node.documentation?.accept(this, argument))
        def rettype = node.returnType?.accept(this, argument)
        def parameters = applyAndJoin node.parameters, argument
        def multimethodParameters = (node.parameters?.findAll { it.type != null }?.collect {
            it.name + " = " + it.type?.accept(this, argument)
        } ?: []).join(", ")
        def methodself
        formatted += documentation
        if (node.flags.contains(ModelFlag.Static)) {
            formatted += "@classmethod\n"
            methodself = "cls"
        } else {
            methodself = "self"
        }
        methodself += parameters == "" ? "" : (", ")
        formatted += "@multimethod(${multimethodParameters})\n"
        formatted += "def ${name}(${methodself}${parameters}):\n"
        if (node.flags.contains(ModelFlag.Native)) {
            formatted += argument.indent("raise new Exception('Native method')\n")
        } else {
            formatted += indent(argument, node.action)
        }
        formatted += "\n"
        return formatted
    }

    @Override
    String visit(MethodCallModel node, PythonFormatOption argument) {
        def formatted
        def expression = node.expression?.accept(this, argument) ?: "self"
        expression = expression == "None" ? "self" : expression //Duck patching
        def nodeArguments = node.arguments.toList()
        if(node.expression instanceof SuperThisModel){
            expression = node.elementOwner?.typeOwner?.parents.find {it.type instanceof ClassModel}?.typeName?:"object"
            nodeArguments.add(0, new ThisModel())
        }else if(!(node.expression instanceof FieldAccessModel)&&!(node.expression instanceof NameModel)&&!(node.expression instanceof ThisModel) ){
            expression = "(" + expression + ")"
        }
        def arguments = applyAndJoin nodeArguments, argument
        //node.genericArguments
        def methodName = node.methodName
        if (methodName == MethodModel.ConstructorName) {
            methodName = "__init__"
        }
        formatted = "${expression}.${methodName}(${arguments})"
        return formatted
    }

    @Override
    String visit(MethodParameterModel node, PythonFormatOption argument) {
        def formatted
        def variadic = node.variadic
        def defaultValue = node.defaultValue?.accept(this, argument)
        //def type = node.type?.accept(this, argument)
        def name = node.name
        formatted = (variadic ? "*" : "") + name + (defaultValue == null ? "" : " = " + defaultValue)
        //TODO: Flags are not supported!
        //def flags = node.flags
        return formatted
    }

    @Override
    String visit(NameModel node, PythonFormatOption argument) {
        def formatted
        def fullname = node.namespace?.fullname
        fullname = (fullname == null || fullname == "") ? "" : (fullname + ".")
        formatted = fullname + node.name
        return formatted
    }

    @Override
    String visit(ObjectCreationModel node, PythonFormatOption argument) {
        def formatted
        //node.genericArguments
        def typeName = node.type?.accept(this, argument)
        /*//if (node.type!=null) {
        if (!node.anonymous) {
            typeName = node.type?.accept(this, argument)
        } else {
            typeName = anonymousClassName
        }*/
        def arguments = applyAndJoin node.arguments, argument
        //formatted = !node.anonymous ? "" : (node.anonymousType?.accept(this, argument) + "\n")
        formatted = "${typeName}(${arguments})"
        return formatted
    }

    @Override
    String visit(ParenthesizedModel node, PythonFormatOption argument) {
        def formatted
        def expression = node.expression?.accept(this, argument)
        formatted = "(${expression})"
        return formatted
    }

    @Override
    String visit(PostfixOperationModel node, PythonFormatOption argument) {
        log.error "Postfix Operation are not allowed"
        return null
        //def formatted
        //return formatted
    }

    @Override
    String visit(PrefixOperationModel node, PythonFormatOption argument) {
        if (node.operator == PrefixOperationModel.Operators.AutoDecrement || node.operator == PrefixOperationModel.Operators.AutoIncrement) {
            log.error "Prefix Operation are not allowed"
            return null
        }
        return node.operator.accept(this, argument) + node.operand.accept(this, argument)
    }

    @Override
    String visit(ReturnModel node, PythonFormatOption argument) {
        def formatted
        def expression = node.returnValue?.accept(this, argument)
        formatted = "return ${expression}"
        return formatted
    }
/*
    @Override
    String visit(Statement node, PythonFormatOption argument) {
        def formatted
        return formatted
    }
*/

    @Override
    String visit(SuperThisModel node, PythonFormatOption argument) {
        //TODO: Namespaces are not supported
        //TODO: Support super keyword
        /*TypeModel classModel = node.elementOwner?.typeOwner
        TypeModel parentModel = node.elementOwner?.typeOwner?.parents.find {it.type instanceof ClassModel}
        def thisclass = node.elementOwner?.typeOwner?.name?: "object"
        */
        def formatted = "super"
        return formatted
    }

    @Override
    String visit(SwitchModel node, PythonFormatOption argument) {
        log.error "Switch not supported yet"
        return null
        //def formatted
        //return formatted
    }

    @Override
    String visit(ThisModel node, PythonFormatOption argument) {
        //TODO: Namespaces are not supported
        //def formatted = (node.objectReference||!node.elementOwner.flags.contains(Flag.Static))?"self":"cls"
        def formatted = (node.objectReference) ? "self" : node.elementOwner.typeOwner.name
        return formatted
    }

    @Override
    String visit(ThrowModel node, PythonFormatOption argument) {
        def formatted
        def expression = node.expression?.accept(this, argument)
        formatted = "raise ${expression}"
        return formatted
    }

    @Override
    String visit(TryModel node, PythonFormatOption argument) {
        def formatted
        //def verifiedAction = node.verifiedAction?.accept(this, argument)
        def catchErrors = applyAndJoin node.catchErrors, argument, "\n"
        //def finallyAction=node.finallyAction?.accept(this, argument)
        formatted = "try:\n"
        formatted += indent(argument, node.verifiedAction)
        formatted += "\n"
        if (node.catchErrors != null)
            formatted += catchErrors
        if (node.finallyAction != null) {
            formatted += "finally:\n"
            formatted += indent(argument, node.finallyAction)
            formatted += "\n"
        }
        return formatted
    }

    @Override
    String visit(TryCatchModel node, PythonFormatOption argument) {
        def formatted
        //def action = node.action?.accept(this, argument)
        def errors = applyAndJoin node.errors, argument, ", "
        //= node.errors.collect {it?.accept(this, argument)}
        def variableName = node.variableName
        formatted = "except (${errors}) as ${variableName}:\n"
        formatted += indent(argument, node.action)
        formatted += "\n"
        return formatted
    }
/*
    @Override
    String visit(Type node, PythonFormatOption argument) {
        def formatted
        node.documentation
        node.elements
        node.flags
        node.genericParameters
        node.imports
        node.name
        return formatted
    }
*/

    @Override
    String visit(TypeDeclarationModel node, PythonFormatOption argument) {
        def formatted = node.declaredType?.accept(this, argument)
        return formatted
    }

    @Override
    String visit(TypeReferenceModel node, PythonFormatOption argument) {
        def formatted = node.array ? "list" : node.typeName
        //node.arrayDimensions
        //node.genericArguments
        //node.type
        //node.typeName
        return formatted
    }

    @Override
    String visit(VariableDeclaration node, PythonFormatOption argument) {
        def formatted
        //def type =node.type?.accept(this, argument)
        def name = node.name
        //node.flags
        def defaultValue = node.defaultValue?.accept(this, argument) ?: "None"
        formatted = "${name} = ${defaultValue}"
        return formatted
    }

    @Override
    String visit(WhileModel node, PythonFormatOption argument) {
        def formatted
        //def action =node.action?.accept(this, argument)
        def condition = node.condition?.accept(this, argument)
        formatted = "while ${condition}:\n"
        formatted += indent(argument, node.action)
        formatted += "\n"
        return formatted
    }

    @Override
    String visit(WithResourceModel node, PythonFormatOption argument) {
        def formatted
        //def resources = applyAndJoin node.resources argument "\n"
        def resources = node.resources?.collect {
            def t = it.type?.accept(this, argument)
            def n = it.name
            "{t} as ${n}"
        }?.join(", ")
        //def action =node.action?.accept(this, argument)
        formatted = "with ${resources}:\n"
        formatted += indent(argument, node.action)
        formatted += "\n"
        return formatted
    }

    @Override
    String visit(AssignmentModel.Operators node, PythonFormatOption argument) {
        node.commonSymbol
    }

    @Override
    String visit(PrefixOperationModel.Operators node, PythonFormatOption argument) {
        if (node == PrefixOperationModel.Operators.Not) {
            return "not "
        } else {
            return node.commonSymbol
        }
    }

    @Override
    String visit(PostfixOperationModel.Operators node, PythonFormatOption argument) {
        node.commonSymbol
    }

    @Override
    String visit(InfixOperationModel.Operators node, PythonFormatOption argument) {
        if (node == InfixOperationModel.Operators.And) {
            return "and"
        } else if (node == InfixOperationModel.Operators.Or) {
            return "or"
        }
        node.commonSymbol
    }

}
