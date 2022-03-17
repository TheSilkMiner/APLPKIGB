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