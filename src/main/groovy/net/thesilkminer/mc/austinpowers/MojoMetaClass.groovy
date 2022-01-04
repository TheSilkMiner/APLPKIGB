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

package net.thesilkminer.mc.austinpowers

import groovy.transform.PackageScope

@PackageScope
class MojoMetaClass extends DelegatingMetaClass {

    private static final OVERRIDDEN_PROPERTIES = [
            'mojoBus' : MojoMetaClass::obtainMojoBus,
            'forgeBus' : MojoMetaClass::obtainForgeBus
    ]

    private static final OVERRIDDEN_METHODS = [
            'toString' : MojoMetaClass::mojoToString
    ]

    private static final FORGE_BUS = {
        Class.forName('net.minecraftforge.common.MinecraftForge').getDeclaredField('EVENT_BUS').get(null)
    }.memoize()

    private final MojoContainer metaContainer

    MojoMetaClass(final MetaClass delegate, final MojoContainer metaContainer) {
        super(delegate)
        this.metaContainer = metaContainer
    }

    @Override
    Object getProperty(final Object object, final String property) {
        final propertyProvider = OVERRIDDEN_PROPERTIES[property]
        propertyProvider != null? propertyProvider(this) : super.getProperty(object, property)
    }

    @Override
    Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        final method = OVERRIDDEN_METHODS[methodName]
        method != null? method(this, arguments) : super.invokeMethod(object, methodName, arguments)
    }

    private static obtainMojoBus(final MojoMetaClass self) {
        self.metaContainer.mojoBus
    }

    private static obtainForgeBus(final MojoMetaClass self) {
        FORGE_BUS()
    }

    private static mojoToString(final MojoMetaClass self, final Object[] args) {
        "Mojo[${self.metaContainer.modId} -> ${self.metaContainer.mod.getClass().name}]"
    }

    @Override
    String toString() {
        "MojoMetaClass[${this.metaContainer.modId} -> ${super.toString()}]"
    }
}
