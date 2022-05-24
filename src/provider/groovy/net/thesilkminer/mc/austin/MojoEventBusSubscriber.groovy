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

package net.thesilkminer.mc.austin

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.PackageScope
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData
import net.thesilkminer.mc.austin.api.EventBus
import net.thesilkminer.mc.austin.api.EventBusSubscriber
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.objectweb.asm.Type

import java.lang.reflect.Method

@CompileStatic
@MapConstructor(includeFields = true)
@PackageScope
@VisibilityOptions(constructor = Visibility.PACKAGE_PRIVATE)
final class MojoEventBusSubscriber {

    private static final Logger LOGGER = LogManager.getLogger(MojoEventBusSubscriber)
    private static final Type EVENT_BUS_SUBSCRIBER = Type.getType(EventBusSubscriber)

    @SuppressWarnings('SpellCheckingInspection')
    private static final String SUBSCRIBER_METHOD_NAME_BEGINNING = '$$aplp$synthetic$registerSubscribers'

    private MojoContainer mojoContainer
    private ModFileScanData scanData
    private ClassLoader loader

    @PackageScope
    void doSubscribing() {
        this.scanData.annotations
                .findAll { it.annotationType() == EVENT_BUS_SUBSCRIBER }
                .each(this.&subscribe)
    }

    private void subscribe(final AnnotationData data) {
        final String mojoId = data.annotationData().modId as String
        if (mojoId != this.mojoContainer.modId) return

        final EventBus bus = bus(data)
        final Set<Dist> distributions = distributions(data)

        if (FMLEnvironment.dist in distributions) {
            this.doSubscribe(bus, distributions, data.clazz())
        }
    }

    private void doSubscribe(final EventBus bus, final Set<Dist> distributions, final Type clazz) {
        try {
            LOGGER.debug(Logging.LOADING, 'Performing subscription of {} for {} onto bus {} with dist {}', clazz.className, this.mojoContainer.modId, bus, distributions)
            final Class<?> initializedClass = Class.forName(clazz.className, true, this.loader)
            final String name = "${SUBSCRIBER_METHOD_NAME_BEGINNING}__${bus.toString()}\$\$"
            final Method initMethod = initializedClass.getDeclaredMethod(name, MojoContainer)
            initMethod.invoke(null, this.mojoContainer)
        } catch (final Throwable t) {
            LOGGER.fatal(Logging.LOADING, "Unable to load subscriber $clazz for ${this.mojoContainer.modId}", t)
            throw new RuntimeException(t)
        }
    }

    private static EventBus bus(final AnnotationData data) {
        final ModAnnotation.EnumHolder holder = data.annotationData().bus as ModAnnotation.EnumHolder
        EventBus.valueOf(holder.value)
    }

    @CompileDynamic
    private static Set<Dist> distributions(final AnnotationData data) {
        final List<ModAnnotation.EnumHolder> declaredHolders = data.annotationData().dist as List<ModAnnotation.EnumHolder>
        final List<ModAnnotation.EnumHolder> holders = declaredHolders ?: (this.&makeDefaultDistributionHolders)()
        holders.collect { Dist.valueOf(it.value) }.toSet()
    }

    private static List<ModAnnotation.EnumHolder> makeDefaultDistributionHolders() {
        Dist.values().collect { new ModAnnotation.EnumHolder(null, it.name()) }
    }
}
