package net.thesilkminer.mc.austin.api

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a field as holding an event bus.
 *
 * <p>Fields annotated with this annotation must be non-{@code static} and reside in a class annotated with either the
 * {@link Mojo} (preferred) or {@link Mod} annotation. The field must be of type
 * {@link net.minecraftforge.eventbus.api.IEventBus}, marked as {@code final} and left uninitialized. APLP will handle
 * its initialization automatically at compile-time.</p>
 *
 * <p>It is not an error attempting to reinitialize the fields in the constructor, although this leads to the injection
 * being overridden.</p>
 *
 * <p>An example of usage is:</p>
 *
 * <pre>
 * {@literal @}Mojo('mymojo')
 * class MyMojo {
 *     {@literal @}GrabEventBus(EventBus.FORGE)
 *     private final IEventBus grabbedForgeBus
 *
 *     MyMojo() {
 *         this.grabbedForgeBus.register(this)
 *     }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@GroovyASTTransformationClass('net.thesilkminer.mc.austin.ast.GrabEventBusAstTransform')
@Retention(RetentionPolicy.SOURCE)
@SuppressWarnings('unused')
@Target(ElementType.FIELD)
@interface GrabEventBus {
    /**
     * The event bus that should be grabbed and injected into the field.
     *
     * @return The event bus that should be grabbed and injected into the field.
     */
    EventBus value()
}
