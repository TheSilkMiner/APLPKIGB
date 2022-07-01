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

package net.thesilkminer.mc.austin.mappings

import groovy.transform.CompileStatic
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap

@CompileStatic
class MappingMetaClassCreationHandle extends MetaClassRegistry.MetaClassCreationHandle {

    private static final String GROOVY_SYSTEM = "groovy.lang.GroovySystem"

    final LoadedMappings mappings

    private static boolean hasWrapped = false

    MappingMetaClassCreationHandle(LoadedMappings mappings) {
        this.mappings = mappings
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        return wrapMetaClass(super.createNormalMetaClass(theClass, registry))
    }

    MetaClass wrapMetaClass(MetaClass delegated) {
        if (mappings.mappable.contains(delegated.theClass.name)) {
            // Check if the class is in the remapping key set
            return new MappingMetaClass(delegated, mappings)
        }
        return delegated
    }

    static synchronized applyCreationHandle(LoadedMappings mappings, ClassLoader loader) {

        if (!hasWrapped) {
            Class groovySystem = Class.forName(GROOVY_SYSTEM, true, loader)
            MetaClassRegistry registry = groovySystem.getMethod("getMetaClassRegistry").invoke(null) as MetaClassRegistry

            if (mappings === null) throw new IllegalArgumentException("Found uninitialized runtime mappings!")
            hasWrapped = true
            var instance = new MappingMetaClassCreationHandle(mappings)
            registry.metaClassCreationHandle = instance
            synchronized (MetaClassRegistry) {
                Map<Class, MetaClass> queue = new Object2ObjectArrayMap<>()
                for (def it : registry.iterator()) {
                    if (it instanceof MetaClass) queue[it.theClass] = instance.wrapMetaClass(it)
                }
                queue.forEach {clazz, metaClazz ->
                    registry.setMetaClass(clazz, metaClazz)
                }
            }
        }
    }
}
