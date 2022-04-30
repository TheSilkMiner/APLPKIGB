/*
 * This file is part of APLP: KIGB, licensed under the MIT License
 *
 * Copyright (c) 2022 TheSilkMiner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.thesilkminer.mc.austin.ast

import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import groovy.transform.Generated
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.IModBusEvent
import net.thesilkminer.mc.austin.MojoContainer
import net.thesilkminer.mc.austin.api.EventBus
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
final class EventBusSubscriberAstTransform extends AbstractASTTransformation implements CompilationUnitAware {

    private static final ClassNode TARGET_ANNOTATION = ClassHelper.make(EventBusSubscriber)

    private static final ClassNode BUS_ENUM = ClassHelper.make(EventBus)
    private static final ClassNode EVENT = ClassHelper.make(Event)
    private static final ClassNode GENERATED = ClassHelper.make(Generated)
    private static final ClassNode MINECRAFT_FORGE = ClassHelper.make(MinecraftForge)
    private static final ClassNode MOD_BUS_EVENT = ClassHelper.make(IModBusEvent)
    private static final ClassNode MOJO_CONTAINER = ClassHelper.make(MojoContainer)
    private static final ClassNode SUBSCRIBE_EVENT = ClassHelper.make(SubscribeEvent)
    private static final ClassNode VOID = ClassHelper.make(void)

    private static final String EVENT_BUS = 'EVENT_BUS'
    @SuppressWarnings('SpellCheckingInspection')
    private static final String GENERATED_METHOD_NAME_BEGINNING = '$$aplp$synthetic$registerSubscribers'
    private static final String MOJO_CONTAINER_PARAMETER_NAME = '$$mojoContainer$$'
    private static final String MOJO_BUS = 'mojoBus'

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
        final EventBus bus = findBusFromAnnotation(annotation)

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

    private static EventBus findBusFromAnnotation(final AnnotationNode node) {
        final Expression busExpression = node.getMember('bus')
        if (!(busExpression instanceof PropertyExpression)) return null

        final PropertyExpression expression = busExpression as PropertyExpression
        final Expression target = expression.objectExpression
        final Expression property = expression.property

        return findBusFromExpressions(target, property)
    }

    private static EventBus findBusFromExpressions(final Expression target, final Expression property) {
        if (!(target instanceof ClassExpression)) return null
        if (!(property instanceof ConstantExpression)) return null

        final ClassExpression probablyBusEnum = target as ClassExpression
        if (probablyBusEnum.type != BUS_ENUM) return null

        final String name = (property as ConstantExpression).text
        EventBus.valueOf(name)
    }

    private boolean verifySubscriberValidity(final MethodNode subscriber, final EventBus bus, final SourceUnit unit) {
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

    private boolean verifyEventParameterValidity(final Parameter event, final EventBus bus) {
        final ClassNode eventType = event.type
        final List<ClassNode> targetSuperClasses = getSuperTypesForBus(bus)
        final boolean validity = targetSuperClasses.every { it.interface? eventType.implementsInterface(it) : eventType.isDerivedFrom(it) }

        if (!validity) {
            final List<String> classes = targetSuperClasses.collect { it.name }
            this.addError("Event listener parameter should be a subclass of all the given classes $classes", event)
        }

        validity
    }

    private static List<ClassNode> getSuperTypesForBus(final EventBus bus) {
        ClassNode other = switch (bus) {
            case EventBus.FORGE -> null
            case EventBus.MOJO, EventBus.MOD -> MOD_BUS_EVENT
        }

        [EVENT, other].findAll(Objects.&nonNull)
    }

    private static void injectMethod(final boolean needsStatic, final boolean needsVirtual, final ClassNode node, final EventBus bus) {
        final Statement syntheticCode = generateMethodCode(needsStatic, needsVirtual, bus, node)
        final Parameter[] parameters = GeneralUtils.params(GeneralUtils.param(MOJO_CONTAINER, MOJO_CONTAINER_PARAMETER_NAME))
        final String name = "${GENERATED_METHOD_NAME_BEGINNING}__${bus.toString()}\$\$"
        final MethodNode syntheticMethod = node.addSyntheticMethod(name, 25, VOID, parameters, new ClassNode[0], syntheticCode)
        syntheticMethod.addAnnotation(new AnnotationNode(GENERATED))
    }

    private static BlockStatement generateMethodCode(final boolean needsStatic, final boolean needsVirtual, final EventBus bus, final ClassNode clazz) {
        final BlockStatement body = new BlockStatement()
        if (needsStatic) {
            body.addStatement(generateStatement(bus, GeneralUtils.classX(clazz)))
        }
        if (needsVirtual) {
            body.addStatement(generateStatement(bus, GeneralUtils.ctorX(clazz)))
        }
        body
    }

    private static Statement generateStatement(final EventBus bus, final Expression argument) {
        GeneralUtils.stmt(GeneralUtils.callX(expressionFromBus(bus), 'register', GeneralUtils.args(argument)))
    }

    private static Expression expressionFromBus(final EventBus bus) {
        return switch (bus) {
            case EventBus.FORGE -> GeneralUtils.propX(GeneralUtils.classX(MINECRAFT_FORGE), EVENT_BUS)
            case EventBus.MOJO, EventBus.MOD -> GeneralUtils.propX(GeneralUtils.varX(MOJO_CONTAINER_PARAMETER_NAME, MOJO_CONTAINER), MOJO_BUS)
        }
    }
}
