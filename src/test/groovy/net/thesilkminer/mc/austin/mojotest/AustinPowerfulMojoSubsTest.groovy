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

package net.thesilkminer.mc.austin.mojotest

import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.thesilkminer.mc.austin.api.EventBus
import net.thesilkminer.mc.austin.api.EventBusSubscriber
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@EventBusSubscriber(mojoId = 'mojotest', bus = EventBus.MOJO)
class AustinPowerfulMojoSubsTest {
    private static final Logger LOGGER = LogManager.getLogger(AustinPowerfulMojoSubsTest)

    @SubscribeEvent
    void onCommon(final FMLCommonSetupEvent event) {
        LOGGER.info('Successfully received event {} on mojoBus', event)
        LOGGER.info('Our meta-class is {} and we are {}', this.metaClass, this.toString())
    }

    @SubscribeEvent
    static void onStaticCommon(final FMLCommonSetupEvent event) {
        LOGGER.info('Successfully received static event {} on mojoBus', event)
    }
}
