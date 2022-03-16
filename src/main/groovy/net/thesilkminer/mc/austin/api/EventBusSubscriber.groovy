package net.thesilkminer.mc.austin.api

import net.minecraftforge.api.distmarker.Dist
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a class as a subscriber to a specific event bus.
 *
 * <p>Classes annotated with this annotation are automatically identified during Mojo construction and registered to the
 * targeted event bus. The mojo that owns this class must be specified in {@link EventBusSubscriber#mojoId()} to avoid
 * any potential class-loading errors.</p>
 *
 * <p>Every method that wants to listen to a posted event on the bus targeted through {@link EventBusSubscriber#bus()}
 * must be annotated with the {@link net.minecraftforge.eventbus.api.SubscribeEvent} annotation. Such methods must have
 * no return type (i.e. be {@code void}) and have a single parameter, representing the event that they want to listen
 * to. Additionally, if {@link EventBus#MOJO} is specified, the parameter must implement (directly or indirectly) the
 * {@link net.minecraftforge.fml.event.IModBusEvent} interface. It is a compile-time error if any of these constraints
 * are not followed. There are no constraints on the method type (i.e. static or virtual).</p>
 *
 * <p>It is also possible to specify a {@linkplain Dist distribution} on which the event bus subscriber will be
 * subscribed on. If the game is currently loading on a different distribution, the event subscriber will be simply
 * ignored.</p>
 *
 * <p>A simple example of usage of this annotation is in the text that follows:</p>
 *
 * <pre>
 * {@literal @}EventBusSubscriber(modId = 'mymojo', bus = EventBus.MOJO)
 * class MySubscriber {
 *     {@literal @}SubscribeEvent
 *     void onCommonSetup(final FMLCommonSetupEvent e) {}
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@GroovyASTTransformationClass('net.thesilkminer.mc.austin.ast.EventBusSubscriberAstTransform')
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings('unused')
@Target(ElementType.TYPE)
@interface EventBusSubscriber {

    /**
     * The ID of the mojo that owns this event subscriber.
     *
     * @return The ID of the mojo that owns this event subscriber.
     *
     * @since 1.0.0
     */
    String mojoId()

    /**
     * The event bus to which the subscriber should be subscribed to.
     *
     * @return The event bus to which the subscriber should be subscribed to.
     *
     * @since 1.0.0
     */
    EventBus bus()

    /**
     * The distributions on which this event subscriber should be loaded.
     *
     * <p>If the distribution does not match any of the specified ones, the event subscriber will not be loaded. This
     * allows the annotation to act as a class-loading barrier.</p>
     *
     * <p>By default, the event subscriber loads on all known distributions. Empty values are allowed, but are
     * effectively useless.</p>
     *
     * @return The distributions on which this event subscriber should be loaded.
     *
     * @since 1.0.0
     */
    Dist[] dist() default [Dist.CLIENT, Dist.DEDICATED_SERVER]
}
