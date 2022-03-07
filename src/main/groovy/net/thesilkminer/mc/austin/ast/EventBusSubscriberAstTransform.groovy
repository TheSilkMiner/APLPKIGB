package net.thesilkminer.mc.austin.ast

import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import groovy.transform.Generated
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.IModBusEvent
import net.thesilkminer.mc.austin.api.EventBusSubscriber
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@SuppressWarnings('unused')
class EventBusSubscriberAstTransform extends AbstractASTTransformation implements CompilationUnitAware {

    private static final ClassNode TARGET_ANNOTATION = ClassHelper.make(EventBusSubscriber)

    private static final ClassNode BUS_ENUM = ClassHelper.make(EventBusSubscriber.Bus)
    private static final ClassNode EVENT = ClassHelper.make(Event)
    private static final ClassNode GENERATED = ClassHelper.make(Generated)
    private static final ClassNode MOD_BUS_EVENT = ClassHelper.make(IModBusEvent)
    private static final ClassNode OBJECT = ClassHelper.make(Object)
    private static final ClassNode SUBSCRIBE_EVENT = ClassHelper.make(SubscribeEvent)
    private static final ClassNode VOID = ClassHelper.make(void)

    @SuppressWarnings('SpellCheckingInspection')
    private static final String GENERATED_METHOD_NAME_BEGINNING = '$$aplp$synthetic$registerSubscribers'
    private static final String MOD_OBJECT_PARAMETER_NAME = '$$mojo$$'

    private CompilationUnit unit

    @Override
    void setCompilationUnit(final CompilationUnit unit) {
        this.unit = unit
    }

    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        this.init(nodes, source)
        final AnnotationNode annotation = nodes[0] as AnnotationNode
        final AnnotatedNode target = nodes[1] as AnnotatedNode

        if (annotation.classNode != TARGET_ANNOTATION) return
        if (!(target instanceof ClassNode)) return

        this.doVisit(annotation, target as ClassNode, source)
    }

    private void doVisit(final AnnotationNode annotation, final ClassNode clazz, final SourceUnit unit) {
        final EventBusSubscriber.Bus bus = findBusFromAnnotation(annotation)

        if (bus == null) {
            this.addError('Unable to identify bus from EventBusSubscriber annotation', annotation)
            return
        }

        final List<MethodNode> subscribers = clazz.methods.findAll { !it.getAnnotations(SUBSCRIBE_EVENT).isEmpty() }
        final boolean subscribersValid = subscribers.every { this.verifySubscriberValidity(it, bus, unit) }
        if (!subscribersValid) {
            return
        }

        final boolean needsStatic = subscribers.any { it.static }
        final boolean needsVirtual = subscribers.any { !it.static }

        injectMethod(needsStatic, needsVirtual, clazz, bus)
    }

    private static EventBusSubscriber.Bus findBusFromAnnotation(final AnnotationNode node) {
        final Expression busExpression = node.getMember('bus')
        if (!(busExpression instanceof PropertyExpression)) return null

        final PropertyExpression expression = busExpression as PropertyExpression
        final Expression target = expression.objectExpression
        final Expression property = expression.property

        return findBusFromExpressions(target, property)
    }

    private static EventBusSubscriber.Bus findBusFromExpressions(final Expression target, final Expression property) {

        if (!(target instanceof ClassExpression)) return null
        if (!(property instanceof ConstantExpression)) return null

        final ClassExpression probablyBusEnum = target as ClassExpression
        if (probablyBusEnum.type != BUS_ENUM) return null

        final String name = (property as ConstantExpression).text
        EventBusSubscriber.Bus.valueOf(name)
    }

    private boolean verifySubscriberValidity(final MethodNode subscriber, final EventBusSubscriber.Bus bus, final SourceUnit unit) {

        boolean valid = true

        if (!subscriber.public) {
            this.addError('Event listener must be public', subscriber)
            valid = false
        }
        if (subscriber.returnType != VOID) {
            this.addError('Event listener must return void', subscriber)
            valid = false
        }

        final Parameter[] parameters = subscriber.parameters
        if (parameters.size() != 1) {
            this.addError('Event listener must have a single argument', subscriber)
            valid = false
        }

        final Parameter event = parameters.size() < 1? null : parameters[0]
        //noinspection GroovyUnusedAssignment
        final GroovyClassLoader loader = this.unit != null? this.unit.classLoader : unit.classLoader
        if (event != null && !this.verifyEventParameterValidity(event, bus)) {
            valid = false
        }

        valid
    }

    private boolean verifyEventParameterValidity(final Parameter event, final EventBusSubscriber.Bus bus) {

        final ClassNode eventType = event.type
        final List<ClassNode> targetSuperClasses = getSuperTypesForBus(bus)
        final boolean validity = targetSuperClasses.every { it.interface? eventType.implementsInterface(it) : eventType.isDerivedFrom(it) }

        if (!validity) {
            final List<String> classes = targetSuperClasses.collect { it.name }
            this.addError("Event listener parameter should be a subclass of all the given classes $classes", event)
        }

        validity
    }

    private static List<ClassNode> getSuperTypesForBus(final EventBusSubscriber.Bus bus) {

        ClassNode other = switch (bus) {
            case EventBusSubscriber.Bus.FORGE -> null
            case EventBusSubscriber.Bus.MOJO, EventBusSubscriber.Bus.MOD -> MOD_BUS_EVENT
        }

        [EVENT, other].findAll(Objects.&nonNull)
    }

    private static void injectMethod(final boolean needsStatic, final boolean needsVirtual, final ClassNode node, final EventBusSubscriber.Bus bus) {

        final Statement syntheticCode = generateMethodCode(needsStatic, needsVirtual, bus, node)
        final Parameter[] parameters = GeneralUtils.params(GeneralUtils.param(OBJECT, MOD_OBJECT_PARAMETER_NAME))
        final String name = "${GENERATED_METHOD_NAME_BEGINNING}__${bus.toString()}\$\$"
        final MethodNode syntheticMethod = node.addSyntheticMethod(name, 25, VOID, parameters, new ClassNode[0], syntheticCode)
        syntheticMethod.addAnnotation(new AnnotationNode(GENERATED))
    }

    private static BlockStatement generateMethodCode(final boolean needsStatic, final boolean needsVirtual, final EventBusSubscriber.Bus bus, final ClassNode clazz) {

        final BlockStatement body = new BlockStatement()
        if (needsStatic) {
            body.addStatement(generateStatement(bus, GeneralUtils.classX(clazz)))
        }
        if (needsVirtual) {
            body.addStatement(generateStatement(bus, GeneralUtils.ctorX(clazz)))
        }
        body
    }

    private static Statement generateStatement(final EventBusSubscriber.Bus bus, final Expression argument) {

        final Expression modObject = GeneralUtils.varX(MOD_OBJECT_PARAMETER_NAME)
        final Expression busObject = GeneralUtils.propX(modObject, idFromBus(bus))
        GeneralUtils.stmt(GeneralUtils.callX(busObject, 'register', GeneralUtils.args(argument)))
    }

    private static String idFromBus(final EventBusSubscriber.Bus bus) {

        return switch (bus) {
            case EventBusSubscriber.Bus.FORGE -> 'forgeBus'
            case EventBusSubscriber.Bus.MOJO, EventBusSubscriber.Bus.MOD -> 'mojoBus'
        }
    }
}
