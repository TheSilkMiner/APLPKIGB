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

package net.thesilkminer.mc.austin.boot;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
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

public final class AustinModLocator extends AbstractJarFileModLocator {
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
    public void initArguments(final Map<String, ?> arguments) {}

    private boolean isProvider(final Manifest manifest) {
        final Object type = manifest.getMainAttributes().getValue("FMLModType");
        return type instanceof String s && "LANGPROVIDER".equals(s);
    }

    private boolean isMod(final SecureJar jar) {
        final Path metaFile = jar.getPath("META-INF", "mods.toml");
        return Files.exists(metaFile);
    }
}
