package net.thesilkminer.mc.austin.ast

import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModList
import net.thesilkminer.mc.austin.api.EventBus
import net.thesilkminer.mc.austin.api.GrabEventBus
import net.thesilkminer.mc.austin.api.Mod
import net.thesilkminer.mc.austin.api.Mojo
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
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
class GrabEventBusAstTransform extends AbstractASTTransformation implements CompilationUnitAware {

    private static final ClassNode TARGET_ANNOTATION = ClassHelper.make(GrabEventBus)

    private static final ClassNode BUS_ENUM = ClassHelper.make(EventBus)
    private static final ClassNode EVENT_BUS_INTERFACE = ClassHelper.make(IEventBus)
    private static final ClassNode MOD = ClassHelper.make(Mod)
    private static final ClassNode MOD_LIST = ClassHelper.make(ModList)
    private static final ClassNode MOJO = ClassHelper.make(Mojo)

    private CompilationUnit unit

    @Override
    void setCompilationUnit(final CompilationUnit unit) {
        this.unit = unit
    }

    @Override
    void visit(final ASTNode[] nodes, final SourceUnit source) {
        this.init(nodes, source)
        final AnnotationNode annotation = nodes[0] as AnnotationNode
        final AnnotatedNode field = nodes[1] as AnnotatedNode

        if (annotation.classNode != TARGET_ANNOTATION) return
        if (!(field instanceof FieldNode)) return

        this.doVisit(annotation, field as FieldNode, source)
    }

    private void doVisit(final AnnotationNode annotation, final FieldNode field, final SourceUnit source) {
        if (!this.verifyFieldValidity(field)) {
            return
        }

        final EventBus bus = findBusFromAnnotation(annotation)
        addInitialization(field, bus)
    }

    private boolean verifyFieldValidity(final FieldNode field) {
        boolean valid = true

        if (field.static) {
            this.addError('Field annotated with GrabEventBus cannot be static', field)
            valid = false
        }
        if (!field.final) {
            this.addError('Field annotated with GrabEventBus must be final', field)
            valid = false
        }
        if (field.type != EVENT_BUS_INTERFACE) {
            this.addError('Field annotated with GrabEventBus must have type "IEventBus"', field)
            valid = false
        }
        if (field.hasInitialExpression()) {
            this.addError('Field annotated with GrabEventBus must not be initialized', field)
            valid = false
        }
        if (!isInsideMojoClass(field)) {
            this.addError('Field annotated with GrabEventBus can only be within class annotated with @Mojo or @Mod', field)
            valid = false
        }

        return valid
    }

    private static boolean isInsideMojoClass(final FieldNode field) {
        final List<AnnotationNode> ownerAnnotations = field.owner.annotations
        return ownerAnnotations.find { it.classNode == MOJO || it.classNode == MOD }
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

    private static void addInitialization(final FieldNode node, final EventBus bus) {
        node.owner.declaredConstructors.each { addInitialization(node, it.code as BlockStatement, bus) }
    }

    private static void addInitialization(final FieldNode node, final BlockStatement code, final EventBus bus) {
        code.statements.add(0, generateInitStatement(node, bus))
    }

    private static Statement generateInitStatement(final FieldNode node, final EventBus bus) {
        GeneralUtils.assignS(GeneralUtils.thisPropX(false, node.name), GeneralUtils.thisPropX(false, idFromBus(bus)))
    }

    private static String idFromBus(final EventBus bus) {
        return switch (bus) {
            case EventBus.FORGE -> 'forgeBus'
            case EventBus.MOJO, EventBus.MOD -> 'mojoBus'
        }
    }
}
