package net.thesilkminer.mc.austinpowers.mojotest

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class AustinPowersMojoTest {
    private static final Logger LOGGER = LogManager.getLogger(AustinPowersMojoTest)

    AustinPowersMojoTest() {
        LOGGER.info('Successfully loaded Groovy mod "{}"', this.toString())
        LOGGER.info('Say hello to my meta-class {}', this.metaClass)
        LOGGER.info('Buses are mojo "{}" and Forge "{}"', mojoBus, forgeBus)
    }
}
