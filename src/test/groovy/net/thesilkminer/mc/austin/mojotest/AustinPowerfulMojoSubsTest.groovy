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
