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

package net.thesilkminer.mc.austin.rt

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus

import java.lang.reflect.Method
import java.util.function.Consumer

@CompileStatic
final class EventMetaFactory {

    private static final class DispatchException extends RuntimeException {
        @PackageScope
        DispatchException(final String message, final Throwable cause) {
            super(message, cause)
        }
    }

    static <T extends Event> void subscribeToBus(
            final Closure<?> originalCall,
            final maybeBus,
            final EventPriority maybePriority,
            final Boolean maybeReceiveCancelled,
            final Class<?> maybeGenericType,
            final Class<T> eventTypeReference,
            final methodPointerOwner,
            final methodPointerName,
            @ClosureParams(value = FromString, options = "T") final Closure<?> subscriber
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
        final Consumer consumer = { event -> dispatch(subscriber, eventType, event) }

        if (isGeneric) {
            bus.addGenericListener(maybeGenericType as Class, priority, receiveCanceled, eventType as Class, consumer)
        } else {
            bus.addListener(priority, receiveCanceled, eventType as Class, consumer)
        }
    }

    static void dispatch(final Closure<?> subscriber, final Class<?> type, final event) {
        try {
            subscriber(type.cast(event))
        } catch (final Throwable e) {
            throw new DispatchException('An error occurred while performing Closure-based event dispatching', e)
        }
    }
}
