/*
 * This file is part of APLP: KIGB, licensed under the MIT License
 *
 * Copyright (c) 2021 TheSilkMiner
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

import net.minecraftforge.fml.Logging
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Collectors

class MojoLanguageProvider implements IModLanguageProvider {
    @SuppressWarnings('SpellCheckingInspection') private static final String NAME = 'austinpowers'

    private static final Logger LOGGER = LogManager.getLogger(MojoLanguageProvider)

    @Override
    String name() { NAME }

    @Override
    Consumer<ModFileScanData> getFileVisitor() {
        return { scanData ->
            def mojos = scanData.IModInfoData
                    .stream()
                    .flatMap { it.mods.stream() }
                    .filter { check(it, it instanceof IConfigurable, 'Ignoring Mojo data "{}" since it is not of a compatible type') }
                    .map { new Tuple2<>(it, (it as IConfigurable).getConfigElement('mojo') as Optional<String>) }
                    .filter { check(it, it.v2.isPresent(), 'Ignoring Mojo data "{}" since it lacks "mojo" entry') }
                    .map { new MojoLanguageLoader(it.v2.get(), it.v1.modId) }
                    .peek { LOGGER.debug(Logging.SCAN, 'Found entry-point Mojo class "{}" for ID "{}"', it.className, it.mojoId) }
                    .collect(
                            Collectors.toMap(
                                    { MojoLanguageLoader it -> it.mojoId },
                                    { MojoLanguageLoader it -> it },
                                    { MojoLanguageLoader a, MojoLanguageLoader b ->
                                        LOGGER.warn('Found duplicated Mojo entries for ID "{}": {} and {}, only the first will be kept', a.mojoId, a, b)
                                        a
                                    },
                                    { new LinkedHashMap<String, MojoLanguageLoader>() }
                            )
                    )
            scanData.addLanguageLoader(mojos)
        }
    }

    @Override
    <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(final Supplier<R> consumeEvent) {}

    private static boolean check(final def it, final boolean expr, final String log) {
        if (expr) return true
        String modId = ((it instanceof Tuple2? it.v1 : it) as IModInfo).modId
        LOGGER.warn(Logging.SCAN, log, modId)
        return false
    }
}
