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

/**
 * ValueAccessor encapsulates the owner, setter, and getter from an object.
 * The idea behind this is to encapsulate the container of an object.
 * Also, it is possible to REPLACE the current object with other.
 * @param < T >
 */
interface ValueAccessor<T extends BaseModel> extends Serializable {
    /**
     * Obtains the owner who contains the current object.
     * @return an object with type BaseModel.
     */
    BaseModel getOwner();

    /**
     * Returns the contained object
     * @return
     */
    T getValue();

    /**
     * Replace the contained object with other value
     * @param value
     */
    void setValue(T value);
}

/**
 * Accessor for simple properties
 * @param < T >
 */
class SimplePropertyModelAccesor<T extends BaseModel> implements ValueAccessor<T> {
    Object target
    String getterName
    transient java.lang.reflect.Method _getter

    /**
     * Obtains a getter reflected method
     * @return
     */
    java.lang.reflect.Method getGetter() {
        if (_getter == null) {
            _getter = target.class.methods.find { it.name == getterName }
        }
        return _getter
    }

    String setterName
    transient java.lang.reflect.Method _setter

    /**
     * Obtains a setter reflected method
     * @return
     */
    java.lang.reflect.Method getSetter() {
        if (_setter == null) {
            _setter = target.class.methods.find { it.name == setterName }
        }
        return _setter
    }

    /**
     * Return the value of accessed object
     * @return
     */
    T getValue() {
        (T) getter.invoke(target)
    }

    BaseModel getOwner() { target as BaseModel }

    /**
     * Set the value of accessed object
     * @param value
     */
    void setValue(T value) {
        setter.invoke(target, value)
    }

    /**
     * Obtains a property of a target object.
     * @param target Object which owns a property.
     * @param name Name of a getter/setter property.
     * @return
     */
    static <V extends BaseModel> SimplePropertyModelAccesor<V> of(Object target, String name) {
        def capitalized = name[0].toUpperCase() + name[1..-1]
        new SimplePropertyModelAccesor<V>(
                target: target,
                getterName: ("get" + capitalized),
                setterName: ("set" + capitalized),
        )
    }
}

/**
 * Accessor for properties in collection
 * @param < T >
 */
class CollectionPropertyModelAccesor<T extends BaseModel> implements ValueAccessor<T> {

    List<T> provider
    T containedObject
    BaseModel owner

    /**
     * Gets the index of the contained object
     * @return
     */
    int getIndex() {
        this.provider.indexOf(this.containedObject)
    }

    /**
     * Returns the contained object
     * @return
     */
    T getValue() {
        this.containedObject
    }

    /**
     * Replace the contained object with other.
     * @param value
     */
    void setValue(T value) {
        int index = this.index
        provider.remove(index)
        provider.add(index, value)
        this.containedObject = value
    }
}

/**
 * Base model for representations in a meta-language
  */
abstract class BaseModel implements Serializable {
    UUID uniqueID = UUID.randomUUID();

    /**
     * Visitor pattern accepter
     * @param visitor
     * @param data
     * @return
     */
    def <R, T> R accept(ModelVisitor<T, R> visitor, T data) {
        visitor.visit(this, data)
    }
}

//
// Expression classes
//

/**
 * Base model for expressions
 */
abstract class ExpressionModel extends StatementModel {
}

//! @CompileStatic 
class ArrayAccessModel extends ExpressionModel {
    ExpressionModel array

    void setArray(ExpressionModel value) { this.array = defaultCommonValue((ExpressionModel) value, "array") }
    ExpressionModel index

    void setIndex(ExpressionModel value) { this.index = defaultCommonValue((ExpressionModel) value, "index") }
}

// new Integer[
//! @CompileStatic 
class ArrayModel extends ExpressionModel {
    ExpressionList dimensions = new ExpressionList(this)

    void setDimensions(List<ExpressionModel> value) { this.dimensions = new ExpressionList(this, value) }
    TypeReferenceModel arrayType = null
    ExpressionList elements = new ExpressionList(this)

    void setElements(List<ExpressionModel> value) { this.elements = new ExpressionList(this, value) }
}

//! @CompileStatic 
class AssignmentModel extends ExpressionModel {
    enum Operators {
        Assign("="),
        PlusAssign("+="),
        MinusAssign("-="),
        TimesAssign("*="),
        DivideAssign("/="),
        BinaryAndAssign("&="),
        BinaryOrAssign("|="),
        BinaryXorAssign("^="),
        LeftShiftAssign("<<="),
        RemainderAssign("%="),
        RightShiftAssign(">>="),
        TripleRightShiftAssign(">>>=")
        String commonSymbol

        private Operators(String symbol) {
            this.commonSymbol = symbol
        }

        def <R, T> R accept(ModelVisitor<T, R> visitor, T data) {
            visitor.visit(this, data)
        }
    }
    ExpressionModel left

    void setLeft(ExpressionModel value) { this.left = defaultCommonValue((ExpressionModel) value, "left") }
    ExpressionModel right

    void setRight(ExpressionModel value) { this.right = defaultCommonValue((ExpressionModel) value, "right") }
    Operators operator
}

//! @CompileStatic 
class CastModel extends ExpressionModel {
    TypeReferenceModel targetType
    ExpressionModel expression

    void setExpression(ExpressionModel value) {
        this.expression = defaultCommonValue((ExpressionModel) value, "expression")
    }
}

//! @CompileStatic 
class ConditionalModel extends ExpressionModel {
    ExpressionModel condition

    void setCondition(ExpressionModel value) {
        this.condition = defaultCommonValue((ExpressionModel) value, "condition")
    }
    ExpressionModel trueAction

    void setTrueAction(ExpressionModel value) {
        this.trueAction = defaultCommonValue((ExpressionModel) value, "trueAction")
    }
    ExpressionModel falseAction

    void setFalseAction(ExpressionModel value) {
        this.falseAction = defaultCommonValue((ExpressionModel) value, "falseAction")
    }
}

//! @CompileStatic 
class FieldAccessModel extends ExpressionModel {
    String field = ""
    ExpressionModel expression

    void setExpression(ExpressionModel value) {
        this.expression = defaultCommonValue((ExpressionModel) value, "expression")
    }
    ArrayList<GenericArgumentModel> genericArguments = []
}

//! @CompileStatic 
class InfixOperationModel extends ExpressionModel {
    enum Operators {
        Plus("+"),
        Minus("-"),
        Times("*"),
        Divide("/"),
        Remainder("%"),
        LeftShift("<<"),
        RightShift(">>"),
        TripleLessThan("<<<"),
        TripleGreaterShift(">>>"),  // -> Default Unsigned shift XD"
        Less("<"),
        Greater(">"),
        LessOrEquals("<="),
        GreaterOrEquals(">="),
        Equals("=="),
        NotEquals("!="),
        BinaryXor("^"),
        BinaryOr("|"),
        BinaryAnd("&"),
        Or("||"),
        And("&&")

        String commonSymbol

        private Operators(String symbol) {
            this.commonSymbol = symbol
        }

        def <R, T> R accept(ModelVisitor<T, R> visitor, T data) {
            visitor.visit(this, data)
        }
    }
    Operators operator
    ExpressionModel left

    void setLeft(ExpressionModel value) { this.left = defaultCommonValue((ExpressionModel) value, "left") }
    ExpressionModel right

    void setRight(ExpressionModel value) { this.right = defaultCommonValue((ExpressionModel) value, "right") }
}

//! @CompileStatic 
class IsInstanceModel extends ExpressionModel {
    TypeReferenceModel type
    ExpressionModel expression

    void setExpression(ExpressionModel value) {
        this.expression = defaultCommonValue((ExpressionModel) value, "expression")
    }
}

//! @CompileStatic 
class LiteralModel extends ExpressionModel {
    def value

    LiteralModel copy() {
        new LiteralModel(value: this.value)
    }

    boolean compare(ExpressionModel expression) {
        if (!(expression instanceof LiteralModel)) return false
        return (expression as LiteralModel).value == value
    }
    static LiteralModel True = new LiteralModel(value: true)
    static LiteralModel False = new LiteralModel(value: false)
    static LiteralModel Zero = new LiteralModel(value: 0)
    static LiteralModel One = new LiteralModel(value: 1)
    static LiteralModel None = new LiteralModel(value: null)
}
//Class literals: //Array.class

//! @CompileStatic 
class ObjectCreationModel extends ExpressionModel {
    TypeReferenceModel type = null
    ExpressionList arguments = new ExpressionList(this)

    void setArguments(List<ExpressionModel> value) { this.arguments = new ExpressionList(this, value) }
    ArrayList<GenericArgumentModel> genericArguments = []
}

//! @CompileStatic 
class MethodCallModel extends ExpressionModel {
    String methodName = ""
    ExpressionModel expression

    void setExpression(ExpressionModel value) {
        this.expression = defaultCommonValue((ExpressionModel) value, "expression")
    }
    ExpressionList arguments = new ExpressionList(this)

    void setArguments(List<ExpressionModel> value) { this.arguments = new ExpressionList(this, value) }
    ArrayList<GenericArgumentModel> genericArguments = []
}

//! @CompileStatic 
class MethodParameterModel extends VariableDeclaration {
    boolean variadic = false
}

//! @CompileStatic 
class NameModel extends ExpressionModel {
    String name = ""
    NamespaceModel namespace

    String getFullname() {
        if (namespace == null) return name
        return namespace.fullname + "." + name
    }
}

//class NoneValue extends Expression {}

//! @CompileStatic 
class ParenthesizedModel extends ExpressionModel {
    ExpressionModel expression

    void setExpression(ExpressionModel value) {
        this.expression = defaultCommonValue((ExpressionModel) value, "expression")
    }
}

//! @CompileStatic 
class PostfixOperationModel extends ExpressionModel {
    enum Operators {
        AutoDecrement("--"),
        AutoIncrement("++");

        String commonSymbol

        private Operators(String symbol) {
            this.commonSymbol = symbol
        }

        def <R, T> R accept(ModelVisitor<T, R> visitor, T data) {
            visitor.visit(this, data)
        }
    }
    Operators operator
    ExpressionModel operand

    void setOperand(ExpressionModel value) { this.operand = defaultCommonValue((ExpressionModel) value, "operand") }
}

//! @CompileStatic 
class PrefixOperationModel extends ExpressionModel {
    enum Operators {
        AutoDecrement("--"),
        AutoIncrement("++"),
        Plus("+"),
        Minus("-"),
        Complement("~"),
        Not("!")

        String commonSymbol

        private Operators(String symbol) {
            this.commonSymbol = symbol
        }

        def <R, T> R accept(ModelVisitor<T, R> visitor, T data) {
            visitor.visit(this, data)
        }
    }
    Operators operator
    ExpressionModel operand

    void setOperand(ExpressionModel value) { this.operand = defaultCommonValue((ExpressionModel) value, "operand") }
}

//! @CompileStatic 
class ThisModel extends ExpressionModel {
    boolean objectReference = true
    NamespaceModel namespace
}

//! @CompileStatic
//Old name: Super
class SuperThisModel extends ExpressionModel {
    NamespaceModel namespace
}

//! @CompileStatic 
class VariableDeclaration extends ExpressionModel {
    ExpressionModel defaultValue

    void setDefaultValue(ExpressionModel value) {
        this.defaultValue = defaultCommonValue((ExpressionModel) value, "defaultValue")
    }
    TypeReferenceModel type
    String name = ""
    Set<ModelFlag> flags = EnumSet.noneOf(ModelFlag.class)
}
//
// Statement classes
//

/**
 * Base model for statements
 */
class StatementModel extends BaseModel {
    TypeElementModel getElementOwner() {
        if(accessor?.owner instanceof TypeElementModel) return (TypeElementModel) accessor?.owner
        for (StatementModel st = accessor?.owner as StatementModel; st != null; st = st.accessor?.owner as StatementModel) {
            if (st.accessor?.owner instanceof TypeElementModel) {
                return (TypeElementModel) st.accessor?.owner
            }
        }
        return null
    }
    ValueAccessor<StatementModel> accessor

    ExpressionModel defaultCommonValue(ExpressionModel value, String name) {
        value = value ?: LiteralModel.None.copy();
        value.accessor = (ValueAccessor<StatementModel>) SimplePropertyModelAccesor.of(this, name)
        return value
    }

    StatementModel defaultCommonValue(StatementModel value, String name) {
        value = value ?: new EmptyModel();
        value.accessor = (ValueAccessor<StatementModel>) SimplePropertyModelAccesor.of(this, name)
        return value
    }
}

//! @CompileStatic 
class AssertModel extends StatementModel {
    ExpressionModel expression

    void setExpression(ExpressionModel value) {
        this.expression = defaultCommonValue((ExpressionModel) value, "expression")
    }
    String message = ""
}

//! @CompileStatic 
class BlockModel extends StatementModel {
    def threadSafeGuard = null
    boolean newScope = true
    StatementList statements = new StatementList(this)

    void setStatements(List<StatementModel> values) { this.statements = new StatementList(this, values) }

    boolean isThreadSafe() { threadSafeGuard != null }
}

//! @CompileStatic 
class BreakModel extends StatementModel {
    String label = ""
}

//! @CompileStatic 
class ContinueModel extends StatementModel {
    String label = ""
}

//! @CompileStatic 
class WhileModel extends StatementModel {
    ExpressionModel condition

    void setCondition(ExpressionModel value) {
        this.condition = defaultCommonValue((ExpressionModel) value, "condition")
    }
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
}

//! @CompileStatic 
class DoModel extends WhileModel {}

//! @CompileStatic 
class EmptyModel extends StatementModel {}

//! @CompileStatic 
class ForeachModel extends StatementModel {
    StatementList variables = new StatementList(this)

    void setVariables(List<StatementModel> values) { this.variables = new StatementList(this, values) }
    ExpressionModel generator

    void setGenerator(ExpressionModel value) {
        this.generator = defaultCommonValue((ExpressionModel) value, "generator")
    }
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
}

//! @CompileStatic 
class ForModel extends StatementModel {
    ExpressionList initializers = new ExpressionList(this)

    void setInitializers(List<ExpressionModel> value) { this.initializers = new ExpressionList(this, value) }
    ExpressionList updaters = new ExpressionList(this)

    void setUpdaters(List<ExpressionModel> value) { this.updaters = new ExpressionList(this, value) }
    ExpressionModel condition

    void setCondition(ExpressionModel value) {
        this.condition = defaultCommonValue((ExpressionModel) value, "condition")
    }
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
}

//! @CompileStatic 
class IfModel extends StatementModel {
    ExpressionModel condition

    void setCondition(ExpressionModel value) {
        this.condition = defaultCommonValue((ExpressionModel) value, "condition")
    }
    StatementModel trueAction

    void setTrueAction(StatementModel value) {
        this.trueAction = defaultCommonValue((StatementModel) value, "trueAction")
    }
    StatementModel falseAction

    void setFalseAction(StatementModel value) {
        this.falseAction = defaultCommonValue((StatementModel) value, "falseAction")
    }
}

//! @CompileStatic 
class ImportModel extends NameModel {
    static String WildcardMark = "*"

    boolean isWildcard() {
        name == WildcardMark
    }

    void setWildcard(boolean value) {
        if (value) {
            name = WildcardMark
        }
    }
}

//! @CompileStatic 
class LabeledModel extends StatementModel {
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
    String label = ""
}

//! @CompileStatic 
class ReturnModel extends StatementModel {
    ExpressionModel returnValue

    void setReturnValue(ExpressionModel value) {
        this.returnValue = defaultCommonValue((ExpressionModel) value, "returnValue")
    }
}

//! @CompileStatic 
class SwitchModel extends StatementModel {
    ExpressionModel condition

    void setCondition(ExpressionModel value) {
        this.condition = defaultCommonValue((ExpressionModel) value, "condition")
    }

    private StatementList _statements = new StatementList(this)

    void setStatements(ArrayList<CaseSwitchModel> values) { this._statements = new StatementList(this, values) }

    void setStatements(StatementList values) { this._statements = new StatementList(this, values) }

    List<CaseSwitchModel> getStatements() { this._statements?.collectAll { (CaseSwitchModel) it } }
}

//! @CompileStatic 
class CaseSwitchModel extends StatementModel {
//class CaseSwitch extends Labeled {
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
    ExpressionModel label

    void setLabel(ExpressionModel value) { this.label = defaultCommonValue((ExpressionModel) value, "label") }
}

//! @CompileStatic 
class ThrowModel extends StatementModel {
    ExpressionModel expression

    void setExpression(ExpressionModel value) {
        this.expression = defaultCommonValue((ExpressionModel) value, "expression")
    }
}

//! @CompileStatic 
class TryCatchModel extends StatementModel {
    ArrayList<TypeReferenceModel> errors = []
    String variableName = ""
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
}

//! @CompileStatic 
class TryModel extends StatementModel {
    StatementModel verifiedAction

    void setVerifiedAction(StatementModel value) {
        this.verifiedAction = defaultCommonValue((StatementModel) value, "verifiedAction")
    }
    StatementModel finallyAction

    void setFinallyAction(StatementModel value) {
        this.finallyAction = defaultCommonValue((StatementModel) value, "finallyAction")
    }
    //ArrayList<TryCatch> catchErrors = []
    StatementList catchErrors = new StatementList(this)

    void setCatchErrors(List<TryCatchModel> values) { this.catchErrors = new StatementList(this, values) }
}

//! @CompileStatic 
class TypeDeclarationModel extends StatementModel {
    TypeModel declaredType
}

//! @CompileStatic 
class WithResourceModel extends StatementModel {
    StatementList resources = new StatementList(this)

    void setResources(List<StatementModel> values) { this.resources = new StatementList(this, values) }
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
}

//
// Structure classes
//

//! @CompileStatic 
class TypeModel extends BaseModel {
    NamespaceModel namespaceOwner = null
    TypeModel typeOwner = null
    /***
     * Type with no owner. It can be this objects by itself.
     * @return
     */
    TypeModel getRootTypeOwner() {
        TypeModel root
        for (root = this; root.typeOwner != null; root = root.typeOwner);
        return root
    }
    TypeList innerTypes = new TypeList(this)

    void setInnerTypes(List<TypeModel> values) { innerTypes = new TypeList(this, values) }
    ArrayList<ImportModel> imports = []
    ElementList elements = new ElementList(this)

    void setElements(List<TypeElementModel> values) { elements = new ElementList(this, values) }
    ArrayList<TypeReferenceModel> parents = []
    Set<ModelFlag> flags = EnumSet.noneOf(ModelFlag.class)
    String name = ""
    ArrayList<GenericParameterModel> genericParameters = []
    CommentModel documentation
}

//! @CompileStatic 
class TypeReferenceModel extends BaseModel {
    String typeName = ""
    int arrayDimensions = 0
    ArrayList<GenericArgumentModel> genericArguments = []
    TypeModel type;

    boolean isGeneric() {
        genericArguments == null || genericArguments == []
    }

    TypeModel obtainType(NamespaceModel currentNamespace) {
        currentNamespace?.root?.findType typeName, true
    }

    boolean isArray() {
        return arrayDimensions != 0
    }

    static def Boolean = new TypeReferenceModel(typeName: "bool")
    static def Char = new TypeReferenceModel(typeName: "char")
    static def Byte = new TypeReferenceModel(typeName: "byte")
    static def Short = new TypeReferenceModel(typeName: "short")
    static def Int = new TypeReferenceModel(typeName: "int")
    static def Long = new TypeReferenceModel(typeName: "long")
    static def Float = new TypeReferenceModel(typeName: "float")
    static def Double = new TypeReferenceModel(typeName: "double")
    static def Void = new TypeReferenceModel(typeName: "void")
}

//! @CompileStatic 
class ClassModel extends TypeModel {

    private static HashMap<String, Integer> anonymousNameCache = new HashMap<String, Integer>()

    static String anonymousName(NamespaceModel current, TypeModel type, boolean createNewName = true) {
        int currentValue = anonymousNameCache.get(current.fullname + "." + type.name, Integer.valueOf(0))
        if (createNewName) {
            currentValue++
            anonymousNameCache.put(current.fullname + "." + type.name, Integer.valueOf(currentValue))
        }
        return "_Helper_" + type.name + "_" + currentValue
        //return "__HelperClass_${currentValue}__"
    }
}

//! @CompileStatic 
class InterfaceModel extends TypeModel {
}

//! @CompileStatic 
class EnumerationModel extends TypeModel {
    ArrayList<EnumFieldModel> values = []
}

//
// Element classes
//

//! @CompileStatic 
class TypeElementModel extends BaseModel {
    String name = ""
    Set<ModelFlag> flags = EnumSet.noneOf(ModelFlag.class)
    CommentModel documentation

    TypeModel typeOwner

    ExpressionModel defaultCommonValue(ExpressionModel value, String name) {
        value = value ?: LiteralModel.None.copy();
        value.accessor = (ValueAccessor<StatementModel>) SimplePropertyModelAccesor.of(this, name)
        //value.elementOwner = this;
        return value
    }

    StatementModel defaultCommonValue(StatementModel value, String name) {
        value = value ?: new EmptyModel();
        value.accessor = (ValueAccessor<StatementModel>) SimplePropertyModelAccesor.of(this, name)
        return value
    }
}

//! @CompileStatic 
class EnumFieldModel extends TypeElementModel {
    ExpressionList arguments = new ExpressionList(this)

    void setArguments(List<ExpressionModel> value) { this.arguments = new ExpressionList(this, value) }
    TypeReferenceModel type = null //null means its type is default
}

//! @CompileStatic 
class FieldModel extends TypeElementModel {
    TypeReferenceModel type
    ExpressionModel defaultValue

    void setDefaultValue(ExpressionModel value) {
        this.defaultValue = defaultCommonValue((ExpressionModel) value, "defaultValue")
    }

    def setVarDeclaration(VariableDeclaration variableDeclaration) {
        defaultValue = variableDeclaration?.defaultValue ?: defaultValue
        type = variableDeclaration?.type ?: type
        name = variableDeclaration?.name ?: name
        flags = variableDeclaration?.flags ?: flags
        //context = variableDeclaration?.context ?: context
        //documentation = variableDeclaration.documentation
    }
}

//! @CompileStatic 
class MethodModel extends TypeElementModel {
    static String ConstructorName = "<<constructor>>"
    TypeReferenceModel returnType
    StatementModel action

    void setAction(StatementModel value) { this.action = defaultCommonValue((StatementModel) value, "action") }
    StatementList parameters = new StatementList(this)
    void setParameters(List<MethodParameterModel> values) { this.parameters = new StatementList(this, values) }
    ArrayList<GenericParameterModel> genericParameters = []

    boolean isConstructor() {
        name == ConstructorName
    }
}

//
// Structure fragment classes
//

//! @CompileStatic 
class CommentModel extends BaseModel {
    String content = ""
    boolean multiline = false
}

//! @CompileStatic 
class GenericArgumentModel extends BaseModel {
    TypeReferenceModel type
    String name = ""
    ArrayList<TypeReferenceModel> childRequests = []
    ArrayList<TypeReferenceModel> parentRequests = []

    boolean isWildcard() {
        name == GenericParameterModel.WildcardSymbol
    }
}

//! @CompileStatic 
class GenericParameterModel extends BaseModel {
    static String WildcardSymbol = "?"
    TypeReferenceModel type
    String name = ""
    boolean instantiable = false  // Inherited from new(), in c#|fields with no abstract flag
    ArrayList<TypeReferenceModel> childRequests = []
    ArrayList<TypeReferenceModel> parentRequests = []

    boolean isWildcard() {
        name == WildcardSymbol
    }
}
/*
//! @CompileStatic 
class Context extends GenericModel {
    def core  // Reference to this module. To create all elements
    def currentNamespace
    def currentType
    def currentElement
    def currentStatement
}
*/

//! @CompileStatic 
class NamespaceModel extends BaseModel {
    private NamespaceModel __parent
    String name = ""
    TypeList types = new TypeList(this)

    void setTypes(List<TypeModel> values) { types = new TypeList(this, values) }
    ArrayList<NamespaceModel> namespaces = []

    String getFullname() {
        def parentName = (__parent?.fullname) ?: ""
        parentName = parentName == "" ? parentName : (parentName + ".")
        return parentName + name
    }

    NamespaceModel getRoot() {
        def root = this
        while (root.parent != null)
            root = root.parent
        return root
    }

    NamespaceModel getParent() {
        __parent
    }

    void setParent(NamespaceModel value) {
        __parent = value
        if (value != null)
            __parent.namespaces.add(this)
    }

    NamespaceModel findNamespace(String name, NamespaceModel root = null) {
        root = root != null ? root : this
        //if(root.namespaces==null) return null
        def parts = [name]
        if (name.contains(".")) {
            parts = name.split("\\.")
        }
        for (subname in parts) {
            root = root.namespaces.find { it.name == subname }
            if (root == null) return null
        }
        return root/*
        for (namespace in root.namespaces) {
            if (namespace.name == name)
                return namespace
        }
        return null*/
    }

    TypeModel findType(String name, boolean fullName = true, NamespaceModel root = null) {
        root = root ?: this
        if (fullName && name.contains(".")) {
            def parts = name.split("\\.")
            //root = findNamespace root, parts[0..parts.size() - 2].join(".")
            root = findNamespace parts[0..parts.size() - 2].join("."), root
            if (root == null)
                return null
            name = parts[-1]
        }
        for (type in (List<TypeModel>) root.types) {
            if (type.name == name) {
                return type
            }
        }
        return null
    }

    NamespaceModel create(String qualifiedName, boolean useRoot = false) {
        def currentParent = useRoot ? root : this
        for (name in qualifiedName.split("\\.")) {
            // TODO: Review, I'm not sure
            //namespace = currentParent.root.findNamespace(name)
            def namespace = currentParent.findNamespace name
            if (namespace == null) {
                namespace = new NamespaceModel()
                namespace.name = name
                namespace.parent = currentParent
            }
            currentParent = namespace
        }
        return currentParent
    }

    ImportModel createImport(String qualifiedName, boolean useRoot = false) {
        def values = _createName(qualifiedName, useRoot)
        def name = new ImportModel()
        name.name = values[0] as String
        name.namespace = values[1] as NamespaceModel
        return name
    }

    NameModel createName(String qualifiedName, boolean useRoot = false) {
        def values = _createName(qualifiedName, useRoot)
        def name = new NameModel()
        name.name = values[0] as String
        name.namespace = values[1] as NamespaceModel
        return name
    }

    private List<Object> _createName(String qualifiedName, boolean useRoot = false) {
        def parent = useRoot ? root : this
        if (qualifiedName.contains(".")) {
            def parts = qualifiedName.split("\\.")
            qualifiedName = parts[parts.size() - 1]
            parent = create parts[0..parts.size() - 2].join("."), useRoot
        }
        return [qualifiedName, parent]
    }

    NamespaceModel copyInto(NamespaceModel other) {
        this.types.addAll(other.types)
        def names = new HashMap<String, NamespaceModel>()
        namespaces.each { names.put(it.name, it) }
        for (subother in other.namespaces) {
            if (names.containsKey(subother.name)) {
                names.get(subother.name).copyInto(subother)
            } else {
                this.namespaces.add(subother)
                subother.parent = this
            }
        }
    }
}

//! @CompileStatic 
enum ModelFlag {
    Private,
    Protected,
    Internal,
    Public,
    Abstract,
    Final,
    Static,
    ThreadSafe,
    Native
}

