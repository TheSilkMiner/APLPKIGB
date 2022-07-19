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
import net.minecraftforge.fml.Logging
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import net.thesilkminer.mc.austin.mappings.MappingsProvider
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.objectweb.asm.Type

import java.util.function.Consumer
import java.util.function.Supplier

@CompileStatic
final class MojoLanguageProvider implements IModLanguageProvider {
    @SuppressWarnings('SpellCheckingInspection') private static final String NAME = 'aplp'
    @SuppressWarnings('SpellCheckingInspection') private static final String MOD_DESC = 'Lnet/thesilkminer/mc/austin/api/Mod;'
    @SuppressWarnings('SpellCheckingInspection') private static final String MOJO_DESC = 'Lnet/thesilkminer/mc/austin/api/Mojo;'

    private static final Logger LOGGER = LogManager.getLogger(MojoLanguageProvider)
    private static final Type MOD_ANNOTATION = Type.getType(MOD_DESC)
    private static final Type MOJO_ANNOTATION = Type.getType(MOJO_DESC)

    MojoLanguageProvider() {
        MappingsProvider.INSTANCE.startMappingsSetup()
        LOGGER.info('Successfully initialized Mojo Language Provider on name {}', this.name())
    }

    @Override
    String name() {
        NAME
    }

    @CompileDynamic
    @Override
    Consumer<ModFileScanData> getFileVisitor() {
        return { scanData ->
            final Map<String, MojoLanguageLoader> mojos = scanData.annotations
                    .findAll { it.annotationType() == MOD_ANNOTATION || it.annotationType() == MOJO_ANNOTATION }
                    .collect { new MojoLanguageLoader(className: it.clazz().className, mojoId: it.annotationData()['value']) }
                    .each { LOGGER.debug(Logging.SCAN, 'Found entry-point Mojo class "{}" for ID "{}"', it.className, it.mojoId) }
                    .collectEntries { [it.mojoId, it] }
            scanData.addLanguageLoader(mojos)
        }
    }

    @Override
    <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(final Supplier<R> consumeEvent) {}
}
