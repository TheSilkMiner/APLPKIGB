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

import groovy.transform.CompileStatic
import groovy.transform.Generated
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import net.thesilkminer.mc.austin.MojoContainer
import net.thesilkminer.mc.austin.api.EventBus
import net.thesilkminer.mc.austin.api.GrabEventBus
import net.thesilkminer.mc.austin.api.Mod
import net.thesilkminer.mc.austin.api.Mojo
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
@SuppressWarnings('unused')
final class MojoAstTransform extends AbstractASTTransformation {

    private static final ClassNode TARGET_ANNOTATION = ClassHelper.make(Mojo)
    private static final ClassNode ALTERNATIVE_TARGET_ANNOTATION = ClassHelper.make(Mod)

    private static final ClassNode BUS_ENUM = ClassHelper.make(EventBus)
    private static final ClassNode EVENT_BUS_INTERFACE = ClassHelper.make(IEventBus)
    private static final ClassNode GENERATED = ClassHelper.make(Generated)
    private static final ClassNode GRAB_EVENT_BUS = ClassHelper.make(GrabEventBus)
    private static final ClassNode MINECRAFT_FORGE = ClassHelper.make(MinecraftForge)
    private static final ClassNode MOJO_CONTAINER = ClassHelper.make(MojoContainer)
    private static final ClassNode STRING = ClassHelper.make(String)

    private static final String CLASS_PROPERTY = 'class'
    private static final String EVENT_BUS = 'EVENT_BUS'
    private static final String FORGE_BUS = 'forgeBus'
    private static final String MOD_BUS = 'modBus'
    private static final String MOD_ID = 'modId'
    @SuppressWarnings('SpellCheckingInspection')
    private static final String MOJO_CONTAINER_NAME = '$$aplp$synthetic$mojoContainer$$'
    private static final String MOJO_BUS = 'mojoBus'
    private static final String NAME = 'name'
    private static final String TO_STRING = 'toString'

    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        this.init(nodes, source)
        final AnnotationNode annotation = nodes[0] as AnnotationNode
        final AnnotatedNode node = nodes[1] as AnnotatedNode

        if (annotation.classNode != TARGET_ANNOTATION && annotation.classNode != ALTERNATIVE_TARGET_ANNOTATION) return
        if (!(node instanceof ClassNode)) return

        this.doVisit(node as ClassNode, source)
    }

    private void doVisit(final ClassNode node, final SourceUnit source) {
        final List<FieldNode> busGrabbers = gatherBusGrabbers(node, source)
        if (!this.verifyGrabbers(busGrabbers)) return
        generateMethods(node, busGrabbers)
    }

    private static List<FieldNode> gatherBusGrabbers(final ClassNode node, final SourceUnit source) {
        node.fields.findAll { !it.getAnnotations(GRAB_EVENT_BUS).isEmpty() }
    }

    private boolean verifyGrabbers(final List<FieldNode> grabbers) {
        grabbers.every { this.verifyGrabber(it) }
    }

    private boolean verifyGrabber(final FieldNode grabber) {
        boolean valid = true

        if (grabber.static) {
            this.addError('Field annotated with GrabEventBus cannot be static', grabber)
            valid = false
        }
        if (!grabber.final) {
            this.addError('Field annotated with GrabEventBus must be final', grabber)
            valid = false
        }
        if (grabber.type != EVENT_BUS_INTERFACE) {
            this.addError('Field annotated with GrabEventBus must have type "IEventBus"', grabber)
            valid = false
        }
        if (grabber.hasInitialExpression()) {
            this.addError('Field annotated with GrabEventBus must not be initialized', grabber)
            valid = false
        }

        return valid
    }

    private static void generateMethods(final ClassNode node, final List<FieldNode> busGrabbers) {
        fixConstructor(node, busGrabbers)
        generateContainerField(node)
        generateMojoBusGetter(node)
        generateForgeBusGetter(node)
        generateModBusGetter(node)
        generateToString(node)
    }

    private static void fixConstructor(final ClassNode node, final List<FieldNode> busGrabbers) {
        final ConstructorNode noArgConstructor = node.declaredConstructors.find {it.parameters.size() == 0 }
        node.declaredConstructors.remove(noArgConstructor)
        node.addConstructor(generateNewConstructor(node, noArgConstructor, busGrabbers))
    }

    private static ConstructorNode generateNewConstructor(final ClassNode owner, final ConstructorNode previous, final List<FieldNode> busGrabbers) {
        final int modifiers = previous.modifiers | 0x1000
        final Parameter[] parameters = [new Parameter(MOJO_CONTAINER, MOJO_CONTAINER_NAME)]
        final Statement code = GeneralUtils.block(
                GeneralUtils.assignS(GeneralUtils.thisPropX(false, MOJO_CONTAINER_NAME), GeneralUtils.varX(MOJO_CONTAINER_NAME)),
                generateBusGrabbersCode(owner, busGrabbers),
                previous.code
        )
        final ConstructorNode node = new ConstructorNode(modifiers, parameters, new ClassNode[0], code)
        node.addAnnotation(new AnnotationNode(GENERATED))
        node
    }

    private static Statement generateBusGrabbersCode(final ClassNode owner, final List<FieldNode> busGrabbers) {
        GeneralUtils.block(busGrabbers.collect {generateBusGrabberCode(owner, it) }.toArray(new Statement[0]) as Statement[])
    }

    private static Statement generateBusGrabberCode(final ClassNode owner, final FieldNode field) {
        final EventBus bus = findBusToGrab(field)
        if (bus == null) throw new IllegalStateException()
        GeneralUtils.assignS(GeneralUtils.thisPropX(false, field.name), expressionFromBus(bus))
    }

    private static EventBus findBusToGrab(final FieldNode node) {
        findBusFromAnnotation(node.getAnnotations(GRAB_EVENT_BUS)[0])
    }

    private static EventBus findBusFromAnnotation(final AnnotationNode node) {
        final Expression busExpression = node.getMember('value')
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

    private static Expression expressionFromBus(final EventBus bus) {
        return switch (bus) {
            case EventBus.FORGE -> GeneralUtils.propX(GeneralUtils.classX(MINECRAFT_FORGE), EVENT_BUS)
            case EventBus.MOJO, EventBus.MOD -> GeneralUtils.propX(GeneralUtils.thisPropX(false, MOJO_CONTAINER_NAME), MOJO_BUS)
        }
    }

    private static void generateContainerField(final ClassNode owner) {
        if (owner.getDeclaredField(MOJO_CONTAINER_NAME)) return

        final FieldNode node = owner.addField(MOJO_CONTAINER_NAME, 0x112, MOJO_CONTAINER, null)
        node.addAnnotation(new AnnotationNode(GENERATED))
    }

    private static void generateMojoBusGetter(final ClassNode owner) {
        generateProperty(owner, MOJO_BUS, expressionFromBus(EventBus.MOJO))
    }

    private static void generateForgeBusGetter(final ClassNode owner) {
        generateProperty(owner, FORGE_BUS, expressionFromBus(EventBus.FORGE))
    }

    private static void generateModBusGetter(final ClassNode owner) {
        generateProperty(owner, MOD_BUS, GeneralUtils.thisPropX(false, MOJO_BUS))
    }

    private static void generateProperty(final ClassNode owner, final String name, final Expression getter) {
        if (owner.getProperty(name)) return
        if (owner.getDeclaredField(name)) return

        final String methodName = "get${name.capitalize()}"

        if (owner.getDeclaredMethod(methodName, new Parameter[0])) return

        final Statement getterCode = GeneralUtils.block(GeneralUtils.returnS(getter))
        final MethodNode node = owner.addSyntheticMethod(methodName, 0x11, EVENT_BUS_INTERFACE, new Parameter[0], new ClassNode[0], getterCode)
        node.addAnnotation(new AnnotationNode(GENERATED))
    }

    private static void generateToString(final ClassNode owner) {
        if (owner.getDeclaredMethod(TO_STRING, new Parameter[0])) return

        final Statement code = GeneralUtils.block(GeneralUtils.returnS(generateGStringExpression(owner)))
        final MethodNode node = owner.addMethod(TO_STRING, 0x11, STRING, new Parameter[0], new ClassNode[0], code)
        node.addAnnotation(new AnnotationNode(GENERATED))
    }

    private static Expression generateGStringExpression(final ClassNode owner) {
        return new GStringExpression(
                'Mojo[${id} -> ${clazz}]',
                ['Mojo[', ' -> ', ']'].collect(GeneralUtils.&constX) as List<ConstantExpression>,
                [
                        GeneralUtils.propX(GeneralUtils.thisPropX(false, MOJO_CONTAINER_NAME), MOD_ID),
                        GeneralUtils.propX(GeneralUtils.thisPropX(false, CLASS_PROPERTY), NAME)
                ] as List<Expression>
        )
    }
}
