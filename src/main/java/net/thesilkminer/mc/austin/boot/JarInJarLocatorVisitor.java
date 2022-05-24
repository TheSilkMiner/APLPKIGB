package net.thesilkminer.mc.austin.boot;

import cpw.mods.jarhandling.SecureJar;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.Manifest;

final class JarInJarLocatorVisitor extends SimpleFileVisitor<Path> {
    private final Predicate<SecureJar> jarVerifier;
    private final Consumer<SecureJar> jarConsumer;
    private final Consumer<Path> pathConsumer;

    private JarInJarLocatorVisitor(final Predicate<SecureJar> jarVerifier, final Consumer<SecureJar> jarConsumer, final Consumer<Path> pathConsumer) {
        this.jarVerifier = jarVerifier;
        this.jarConsumer = jarConsumer;
        this.pathConsumer = pathConsumer;
    }

    static JarInJarLocatorVisitor ofSecure(final Predicate<SecureJar> jarVerifier, final Consumer<SecureJar> jarConsumer) {
        return new JarInJarLocatorVisitor(jarVerifier, jarConsumer, null);
    }

    static JarInJarLocatorVisitor ofPath(final Predicate<SecureJar> jarVerifier, final Consumer<Path> pathConsumer) {
        return new JarInJarLocatorVisitor(jarVerifier, null, pathConsumer);
    }

    static Predicate<SecureJar> manifest(final Predicate<Manifest> manifestVerifier) {
        return it -> manifestVerifier.test(it.getManifest());
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        final FileVisitResult result = super.visitFile(file, attrs);
        if (result != FileVisitResult.CONTINUE) {
            return result;
        }
        final String fileName = file.getFileName().toString();
        if (fileName.endsWith(".jar")) {
            final SecureJar jar = SecureJar.from(file);
            if (this.jarVerifier.test(jar)) {
                if (this.jarConsumer != null) {
                    this.jarConsumer.accept(jar);
                    return FileVisitResult.TERMINATE;
                }
                this.pathConsumer.accept(file);
                return FileVisitResult.CONTINUE;
            }
        }
        return FileVisitResult.CONTINUE;
    }
}
