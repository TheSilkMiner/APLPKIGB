package net.thesilkminer.mc.austin.boot;

import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import joptsimple.OptionSpecBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.Manifest;

public final class AustinTransformationService implements ITransformationService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String NAME = "aplp:transformation";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(final IEnvironment environment) {
        LOGGER.info("Successfully initialized service {} in environment {}", this.name(), environment);
        LOGGER.info("Service is located in {} (fs: {})", BootDataManager.get().own(), BootDataManager.get().own().getFileSystem());
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {

    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }

    @Override
    public void arguments(final BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
    }

    @Override
    public void argumentValues(final OptionResult option) {
    }

    @Override
    public List<Resource> beginScanning(final IEnvironment environment) {
        try {
            final List<SecureJar> resources = new ArrayList<>();
            final FileVisitor<Path> visitor = JarInJarLocatorVisitor.ofSecure(JarInJarLocatorVisitor.manifest(this::isGroovy), resources::add);
            Files.walkFileTree(BootDataManager.get().own(), visitor);
            return Collections.singletonList(new Resource(IModuleLayerManager.Layer.PLUGIN, resources));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Resource> completeScan(final IModuleLayerManager layerManager) {
        return Collections.emptyList();
    }

    @Override
    public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalClassesLocator() {
        return null;
    }

    @Override
    public Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalResourcesLocator() {
        return null;
    }

    private boolean isGroovy(final Manifest manifest) {
        return manifest.getMainAttributes().getValue("GroovyMarker") != null;
    }
}
