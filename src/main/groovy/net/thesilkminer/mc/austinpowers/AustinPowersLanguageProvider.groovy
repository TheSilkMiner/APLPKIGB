package net.thesilkminer.mc.austinpowers

import net.thesilkminer.mc.austinpowers.api.Mole
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class AustinPowersLanguageProvider {
    private static final Logger LOGGER = LogManager.getLogger(AustinPowersLanguageProvider)

    AustinPowersLanguageProvider(final Mole mole) {
        LOGGER.info("Successfully loaded Groovy mod with mole '${mole}'")
    }
}
