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
 * Denotes a class as a provider of a loadable mod content (known as Mojo) in APLP.
 *
 * <p>For a class annotated with this annotation to be properly loaded, it must have a parameter-less constructor and
 * the value of {@link Mojo#value()} must also exist in the {@code mods.toml} file. The constructor of the class will
 * automatically be called when appropriate by APLP.</p>
 *
 * <p>Annotating a class with this annotation also implicitly adds two properties (namely {@code forgeBus} and
 * {@code mojoBus}) to the class, allowing users to reference the Forge and Mojo bus for event subscribing.</p>
 *
 * <p>An example of a simple class using this annotation is:</p>
 *
 * <pre>
 * {@literal @}Mojo("mymojo")
 * class MyMojo {
 *     MyMojo() {
 *         forgeBus.addEventListener(ForgeBusEventHandler)
 *     }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@GroovyASTTransformationClass('net.thesilkminer.mc.austin.ast.MojoAstTransform')
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Mojo {
    /**
     * Gets the name that uniquely identifies this mojo.
     *
     * <p>This name must match one of the values present into the {@code mods.toml} file.</p>
     *
     * @return The name that uniquely identifies the mojo.
     *
     * @since 1.0.0
     */
    String value()
}
