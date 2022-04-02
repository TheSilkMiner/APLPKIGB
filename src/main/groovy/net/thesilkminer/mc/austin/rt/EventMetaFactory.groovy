package net.thesilkminer.mc.austin.rt

import groovy.transform.CompileStatic
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus

import java.lang.reflect.Method
import java.util.function.Consumer

@CompileStatic
final class EventMetaFactory {

    static void subscribeToBus(
            final Closure<?> originalCall,
            final maybeBus,
            final EventPriority maybePriority,
            final Boolean maybeReceiveCancelled,
            final Class<?> maybeGenericType,
            final Class<?> eventTypeReference,
            final methodPointerOwner,
            final methodPointerName,
            final Closure<?> subscriber
    ) {

        if (!(maybeBus instanceof IEventBus)) {
            originalCall()
            return
        }

        final IEventBus bus = maybeBus as IEventBus
        final boolean isGeneric = maybeGenericType != null
        final EventPriority priority = maybePriority ?: EventPriority.NORMAL
        final boolean receiveCanceled = maybeReceiveCancelled ?: false
        final Class<?> eventType = eventTypeReference? eventTypeReference : {
            final Class owner = methodPointerOwner instanceof Class<?>? methodPointerOwner as Class<?> : methodPointerOwner.class
            final String methodName = methodPointerName as String
            final Method target = owner.getDeclaredMethods().find { it.name == methodName }

            if (target.parameterCount != 1) {
                throw new IllegalStateException("Unable to subscribe to event: invalid parameter count ${target.parameterCount}")
            }

            target.parameters[0].type
        }()
        final Consumer consumer = { event -> subscriber(eventType.cast(event)) }

        if (isGeneric) {
            bus.addGenericListener(maybeGenericType as Class, priority, receiveCanceled, eventType as Class, consumer)
        } else {
            bus.addListener(priority, receiveCanceled, eventType as Class, consumer)
        }
    }
}
