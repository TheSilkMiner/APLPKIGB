package net.thesilkminer.mc.austin.boot;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public final class AustinModLocator extends AbstractJarFileLocator {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NAME = "aplp:mod_locator";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Stream<Path> scanCandidates() {
        try {
            LOGGER.info("Attempting to scan candidates in {} (fs: {})", BootDataManager.get().own(), BootDataManager.get().own().getFileSystem());
            final List<Path> candidates = new ArrayList<>();
            final Predicate<SecureJar> predicate = JarInJarLocatorVisitor.manifest(this::isProvider).or(this::isMod);
            final FileVisitor<Path> visitor = JarInJarLocatorVisitor.ofPath(predicate, candidates::add);
            Files.walkFileTree(BootDataManager.get().own(), visitor);
            LOGGER.info(() -> "Identified candidates " + candidates.stream().map(Path::toAbsolutePath).toList());
            return candidates.stream();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
    }

    private boolean isProvider(final Manifest manifest) {
        final Object type = manifest.getMainAttributes().getValue("FMLModType");
        return type instanceof String s && "LANGPROVIDER".equals(s);
    }

    private boolean isMod(final SecureJar jar) {
        final Path metaFile = jar.getPath("META-INF", "mods.toml");
        return Files.exists(metaFile);
    }
}
