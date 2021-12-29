package net.thesilkminer.mc.austinpowers.mojotest

import net.thesilkminer.mc.austinpowers.api.Mole
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class AustinPowersMojoTest {
    private static final Logger LOGGER = LogManager.getLogger(AustinPowersMojoTest)

    AustinPowersMojoTest(final Mole mole) {
        LOGGER.info("Successfully loaded Groovy mod with mole '${mole}'")
    }
}
