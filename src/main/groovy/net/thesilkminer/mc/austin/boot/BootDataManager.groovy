package net.thesilkminer.mc.austin.boot

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
@PackageScope
final class BootDataManager {
    private static final String IMPL_VERSION = 'Implementation-Version: '

    @PackageScope
    static final BootDataManager INSTANCE = new BootDataManager()

    @Lazy
    Path own = { Paths.get this.class.protectionDomain.codeSource.location.toURI() }()

    @Lazy
    String version = {
        final manifestPath = this.own.resolve('META-INF/MANIFEST.MF')
        final lines = Files.readAllLines(manifestPath)
        lines.find { it.startsWith(IMPL_VERSION) }
                ?.substring(IMPL_VERSION.length())
                ?.trim()
                ?: '1.0.0'
    }()

    final String license = 'MIT'

    @Lazy
    Set<String> packages = {
        try (final walker = Files.walk(this.own)) {
            walker.toList()
                    .findAll { it.nameCount > 0 }
                    .findAll { it.getName(0).toString() != 'META-INF' }
                    .findAll { it.fileName.toString().endsWith('.class') }
                    .findAll(Files.&isRegularFile)
                    .collect { it.parent.toString().replace([ '/' : '.' ] as Map<CharSequence, CharSequence>) }
                    .findAll { !it.isEmpty() }
                    .toSet()
        }

    }()

    private BootDataManager() {}
}
