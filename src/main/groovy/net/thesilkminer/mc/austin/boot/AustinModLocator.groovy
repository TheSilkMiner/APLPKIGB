package net.thesilkminer.mc.austin.boot

import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModLocator
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.nio.file.Path
import java.util.function.Consumer

final class AustinModLocator implements IModLocator {
    private static final Logger LOGGER = LogManager.getLogger(AustinModLocator)
    private static final String NAME = 'aplp:mod_locator'

    AustinModLocator() {
        LOGGER.info('Successfully initialized mod locator {}, ready to inject fake mods', NAME)
    }

    @Override
    List<IModFile> scanMods() {
        final modFiles = [
                MojoFile.makeLoaderModFile(this),
                MojoFile.makeGameModFile(this),
                MojoFile.makePaintModFile(this)
        ]
        LOGGER.info('Mod scanning has "identified" {} additional JARs to load: {}', modFiles.size(), modFiles)
        modFiles
    }

    @Override
    String name() {
        NAME
    }

    @Override
    void scanFile(final IModFile modFile, final Consumer<Path> pathConsumer) {
        modFile.setSecurityStatus(SecureJar.Status.VERIFIED)
    }

    @Override
    void initArguments(final Map<String, ?> arguments) {

    }

    @Override
    boolean isValid(final IModFile modFile) {
        true
    }
}
