package net.thesilkminer.mc.austin.ast

import groovy.transform.CompileStatic
import net.minecraftforge.eventbus.api.EventPriority
import net.thesilkminer.mc.austin.rt.EventMetaFactory
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.LoopingStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.NullObject
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.ErrorCollecting
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
final class AddListenerAstTransform implements ASTTransformation, ErrorCollecting {

    private static final Object REQUIRE_REFLECTIVE_LOOKUP_EXTRACTION = new Object()

    private static final ClassNode EVENT_META_FACTORY = ClassHelper.make(EventMetaFactory)
    private static final ClassNode EVENT_PRIORITY = ClassHelper.make(EventPriority)
    private static final ClassNode NULL_OBJECT = ClassHelper.make(NullObject)

    private static final String CANCELED = 'canceled'
    private static final String CONSUMER = 'consumer'
    private static final String FILTER = 'filter'
    private static final String PRIORITY = 'priority'
    private static final String TYPE = 'type'

    // the index into the list is made up like ((isGeneric? 8 : 0) | count)
    private static final List<Map<String, Integer>> ELEMENTS_TO_EXTRACT = [
            null,
            [ consumer : 0 ],
            [ priority: 0, consumer: 1 ],
            [ priority: 0, canceled: 1, consumer: 2 ],
            [ priority: 0, canceled: 1, type: 2, consumer: 3 ],
            null,
            null,
            null,
            null,
            null,
            [ filter: 0, consumer: 1 ],
            [ filter: 0, priority: 1, consumer: 2 ],
            [ filter: 0, priority: 1, canceled: 2, consumer: 3 ],
            [ filter: 0, priority: 1, canceled: 2, type: 3, consumer: 4 ]
    ] as List<Map<String, Integer>>

    private SourceUnit unit
    private MethodNode currentMethod

    @Override
    void addError(final String msg, final ASTNode expr) {
        this.unit.errorCollector.addErrorAndContinue("$msg\n", expr, this.unit)
    }

    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        this.unit = source
        this.doVisit(source.AST)
    }

    private void doVisit(final ModuleNode node) {
        if (node == null) return

        node.classes.each(this.&doVisit)
    }

    private void doVisit(final ClassNode node) {
        if (node == null) return

        node.declaredConstructors.each(this.&doVisit)
        node.methods.each(this.&doVisit)
        node.fields.each(this.&doVisit)
        node.innerClasses.each(this.&doVisit)
    }

    private void doVisit(final MethodNode node) {
        if (node == null) return

        this.currentMethod = node
        this.doVisit(node.code)
        this.currentMethod = null
    }

    private void doVisit(final FieldNode node) {
        if (node == null) return

        this.doVisit(node.initialExpression)
    }

    private void doVisit(final Statement statement) {
        if (statement == null) return

        switch (statement) {
            case BlockStatement -> (statement as BlockStatement).statements.each(this.&doVisit)
            case CatchStatement -> this.doVisit((statement as CatchStatement).code)
            case ExpressionStatement -> this.doVisit((statement as ExpressionStatement).expression)
            case LoopingStatement -> this.doVisit((statement as LoopingStatement).loopBlock)
            case ReturnStatement -> this.doVisit((statement as ReturnStatement).expression)
            case SynchronizedStatement -> this.doVisit((statement as SynchronizedStatement).code)
            case ThrowStatement -> this.doVisit((statement as ThrowStatement).expression)
            case AssertStatement -> {
                final AssertStatement assertStatement = statement as AssertStatement
                this.doVisit(assertStatement.booleanExpression)
                this.doVisit(assertStatement.messageExpression)
            }
            case IfStatement -> {
                final IfStatement ifStatement = statement as IfStatement
                this.doVisit(ifStatement.booleanExpression)
                this.doVisit(ifStatement.ifBlock)
                this.doVisit(ifStatement.elseBlock)
            }
            case SwitchStatement -> {
                final SwitchStatement switchStatement = statement as SwitchStatement
                this.doVisit(switchStatement.expression)
                switchStatement.caseStatements.each(this.&doVisit)
                this.doVisit(switchStatement.defaultStatement)
            }
            case TryCatchStatement -> {
                final TryCatchStatement tryCatchStatement = statement as TryCatchStatement
                tryCatchStatement.resourceStatements.each(this.&doVisit)
                this.doVisit(tryCatchStatement.tryStatement)
                tryCatchStatement.catchStatements.each(this.&doVisit)
                this.doVisit(tryCatchStatement.finallyStatement)
            }
        }
    }

    private void doVisit(final Expression expression) {
        if (expression == null) return

        switch (expression) {
            case BitwiseNegationExpression -> this.doVisit((expression as BitwiseNegationExpression).expression)
            case BooleanExpression -> this.doVisit((expression as BooleanExpression).expression)
            case CastExpression -> this.doVisit((expression as CastExpression).expression)
            case ClosureExpression -> this.doVisit((expression as ClosureExpression).code)
            case ConstructorCallExpression -> this.doVisit((expression as ConstructorCallExpression).arguments)
            case ListExpression -> (expression as ListExpression).expressions.each(this.&doVisit)
            case MapExpression -> (expression as MapExpression).mapEntryExpressions.each(this.&doVisit)
            case MethodCallExpression -> this.doVisit(expression as MethodCallExpression)
            case PostfixExpression -> this.doVisit((expression as PostfixExpression).expression)
            case PrefixExpression -> this.doVisit((expression as PrefixExpression).expression)
            case SpreadExpression -> this.doVisit((expression as SpreadExpression).expression)
            case SpreadMapExpression -> this.doVisit((expression as SpreadMapExpression).expression)
            case TupleExpression -> (expression as TupleExpression).expressions.each(this.&doVisit)
            case UnaryMinusExpression -> this.doVisit((expression as UnaryMinusExpression).expression)
            case UnaryPlusExpression -> this.doVisit((expression as UnaryPlusExpression).expression)
            case ArrayExpression -> {
                final ArrayExpression arrayExpression = expression as ArrayExpression
                arrayExpression.expressions.each(this.&doVisit)
                arrayExpression.sizeExpression.each(this.&doVisit)
            }
            case BinaryExpression -> {
                final BinaryExpression binaryExpression = expression as BinaryExpression
                this.doVisit(binaryExpression.leftExpression)
                this.doVisit(binaryExpression.rightExpression)
            }
            case GStringExpression -> {
                final GStringExpression gStringExpression = expression as GStringExpression
                gStringExpression.strings.each(this.&doVisit)
                gStringExpression.values.each(this.&doVisit)
            }
            case MapEntryExpression -> {
                final MapEntryExpression mapEntryExpression = expression as MapEntryExpression
                this.doVisit(mapEntryExpression.keyExpression)
                this.doVisit(mapEntryExpression.valueExpression)
            }
            case MethodPointerExpression -> {
                final MethodPointerExpression methodPointerExpression = expression as MethodPointerExpression
                this.doVisit(methodPointerExpression.expression)
                this.doVisit(methodPointerExpression.methodName)
            }
            case PropertyExpression -> {
                final PropertyExpression propertyExpression = expression as PropertyExpression
                this.doVisit(propertyExpression.objectExpression)
                this.doVisit(propertyExpression.property)
            }
            case RangeExpression -> {
                final RangeExpression rangeExpression = expression as RangeExpression
                this.doVisit(rangeExpression.from)
                this.doVisit(rangeExpression.to)
            }
            case TernaryExpression -> {
                final TernaryExpression ternaryExpression = expression as TernaryExpression
                this.doVisit(ternaryExpression.booleanExpression)
                this.doVisit(ternaryExpression.trueExpression)
                this.doVisit(ternaryExpression.falseExpression)
            }
        }
    }

    private void doVisit(final MethodCallExpression methodCall) {
        final Expression objectExpression = methodCall.objectExpression
        final Expression method = methodCall.method
        final Expression arguments = methodCall.arguments

        this.doVisit(objectExpression)
        this.doVisit(method)
        this.doVisit(arguments)

        if (method instanceof ConstantExpression) {
            final String name = (method as ConstantExpression).text
            final TupleExpression methodArguments = arguments as TupleExpression
            if (shouldTargetMethodTransformation(name, methodArguments)) {
                this.transform(methodCall, objectExpression, name, methodArguments)
            }
        }
    }

    private static boolean shouldTargetMethodTransformation(final String name, final TupleExpression arguments) {
        if (name != 'addListener' && name != 'addGenericListener') return false
        final isGeneric = name == 'addGenericListener'

        final int argumentSize = arguments.size()
        final int minSize = isGeneric? 2 : 1
        final int maxSize = isGeneric? 5 : 4
        if (argumentSize < minSize || argumentSize > maxSize) return false

        final Expression last = arguments.last()
        if (!(last instanceof ClosureExpression) && !(last instanceof MethodPointerExpression)) return false

        return true
    }

    private void transform(final MethodCallExpression methodCall, final Expression receiver, final String name, final TupleExpression arguments) {
        final int count = arguments.size()
        final boolean isGeneric = name == 'addGenericListener'

        final originalCall = GeneralUtils.closureX(GeneralUtils.block(GeneralUtils.stmt(clone(methodCall))))
        final priority = getPriorityExpression(arguments, count, isGeneric)
        final cancelled = getCanceledExpression(arguments, count, isGeneric)
        final genericType = getGenericType(arguments, count, isGeneric)
        final eventTypeReference = this.tryExtractEventType(arguments, count, isGeneric)
        final methodPointerExpression = getReflectiveTypeInformation(arguments, count, isGeneric, eventTypeReference)
        final subscriber = getClosure(arguments, count, isGeneric)

        if (!eventTypeReference || (eventTypeReference == REQUIRE_REFLECTIVE_LOOKUP_EXTRACTION && !methodPointerExpression)) {
            this.addError('Unable to identify the type from the Closure or method pointer in an untyped Closure-based subscriber', methodCall)
            return
        }

        originalCall.variableScope = new VariableScope(this.currentMethod.variableScope)

        methodCall.objectExpression = GeneralUtils.classX(EVENT_META_FACTORY)
        methodCall.method = GeneralUtils.constX('subscribeToBus')
        methodCall.arguments = GeneralUtils.args(
                originalCall,
                receiver,
                priority,
                cancelled,
                genericType,
                asClass(eventTypeReference == REQUIRE_REFLECTIVE_LOOKUP_EXTRACTION? makeNullObjectExpression() : (eventTypeReference as Expression)),
                methodPointerExpression? methodPointerExpression.expression : makeNullObjectExpression(),
                methodPointerExpression? methodPointerExpression.methodName : makeNullObjectExpression(),
                subscriber
        )
    }

    private static MethodCallExpression clone(final MethodCallExpression methodCallExpression) {
        return new MethodCallExpression(methodCallExpression.objectExpression, methodCallExpression.method, methodCallExpression.arguments)
    }

    private static Expression getPriorityExpression(final TupleExpression arguments, final int count, final boolean isGeneric) {
        GeneralUtils.asX(EVENT_PRIORITY, extractMethodParameter(arguments, count, PRIORITY, isGeneric) ?: makeNullObjectExpression())
    }

    private static Expression getCanceledExpression(final TupleExpression arguments, final int count, final boolean isGeneric) {
        GeneralUtils.asX(ClassHelper.Boolean_TYPE, extractMethodParameter(arguments, count, CANCELED, isGeneric) ?: makeNullObjectExpression())
    }

    private static Expression getGenericType(final TupleExpression arguments, final int count, final boolean isGeneric) {
        asClass(extractMethodParameter(arguments, count, FILTER, isGeneric) ?: makeNullObjectExpression())
    }

    private static Expression getClosure(final TupleExpression arguments, final int count, final boolean isGeneric) {
        GeneralUtils.asX(ClassHelper.CLOSURE_TYPE, extractMethodParameter(arguments, count, CONSUMER, isGeneric))
    }

    private tryExtractEventType(final TupleExpression arguments, final int count, final boolean isGeneric) {
        final explicitType = extractMethodParameter(arguments, count, TYPE, isGeneric)
        if (explicitType) {
            return asClass(explicitType)
        }

        final closure = extractMethodParameter(arguments, count, CONSUMER, isGeneric)
        final extractedData = this.tryExtractEventType(closure)

        return switch (extractedData) {
            case Expression -> asClass(extractedData as Expression)
            case REQUIRE_REFLECTIVE_LOOKUP_EXTRACTION -> REQUIRE_REFLECTIVE_LOOKUP_EXTRACTION
            default -> {
                this.addError('Unable to identify event type for untyped Closure-based event subscriber', closure)
                null
            }
        }
    }

    private tryExtractEventType(final Expression closure) {
        return switch (closure) {
            case ClosureExpression -> this.tryExtractEventType(closure as ClosureExpression)
            case MethodPointerExpression -> REQUIRE_REFLECTIVE_LOOKUP_EXTRACTION
            default -> {
                this.addError('Not a Closure in a Closure-based event subscriber', closure)
                null
            }
        }
    }

    private Expression tryExtractEventType(final ClosureExpression closure) {
        final target = this.extractTypeFrom(closure)
        target == null? null : GeneralUtils.classX(this.extractTypeFrom(closure))
    }

    private Class<?> extractTypeFrom(final ClosureExpression closure) {
        final parameters = closure.parameters
        if (parameters.size() != 1) {
            this.addError('Implicit "it" is not supported in closure-based untyped listener addition', closure)
            return null
        }
        final type = parameters[0].type
        if (!type || ClassHelper.isDynamicTyped(type)) {
            this.addError('Dynamic parameters are not supported in closure-based untyped listener addition', closure)
            return null
        }
        return type.typeClass
    }

    private static MethodPointerExpression getReflectiveTypeInformation(final TupleExpression arguments, final int count, final boolean isGeneric, final eventTypeReference) {
        eventTypeReference == REQUIRE_REFLECTIVE_LOOKUP_EXTRACTION? extractMethodParameter(arguments, count, CONSUMER, isGeneric) as MethodPointerExpression : null
    }

    private static Expression extractMethodParameter(final TupleExpression arguments, final int count, final String name, final boolean isGeneric) {
        final Integer targetExpression = ELEMENTS_TO_EXTRACT[(isGeneric? 8 : 0) | count][name]
        targetExpression == null? null : arguments.getExpression(targetExpression)
    }

    private static Expression asClass(final Expression expression) {
        GeneralUtils.asX(ClassHelper.CLASS_Type, expression)
    }

    private static Expression makeNullObjectExpression() {
        GeneralUtils.callX(NULL_OBJECT, 'getNullObject')
    }
}
