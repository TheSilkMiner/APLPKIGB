package net.thesilkminer.mc.austin.boot

import cpw.mods.jarhandling.JarMetadata
import cpw.mods.jarhandling.SecureJar
import cpw.mods.jarhandling.impl.Jar
import cpw.mods.jarhandling.impl.SimpleJarMetadata
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.Manifest

@CompileStatic
@PackageScope
final class FilteredJar extends Jar {
    private static final Logger LOGGER = LogManager.getLogger(FilteredJar)

    private final Manifest manifest
    private final Closure<Boolean> filter

    private Set<String> cachedPackages
    private List<Provider> cachedProviders

    FilteredJar(final Manifest manifest, final List<Path> paths, final String discriminator, final Closure<Boolean> filter) {
        super(Manifest.&new, metadataFilter(discriminator, filter), FilteredJar.&pathFilter, paths.toArray(Path[].&new))
        this.manifest = manifest
        this.filter = filter
    }

    private static Closure<JarMetadata> metadataFilter(final String discriminator, final Closure<Boolean> filter) {
        return { SecureJar jar -> new SimpleJarMetadata(
                "net.thesilkminer.mc.aplpkigb${discriminator}",
                '1.0.0+i.hate.modules',
                filterPackages(jar.packages, filter),
                filterProviders(jar.providers, filter)
        ) } as Closure<JarMetadata>
    }

    private static String removeHide(final String hide) {
        hide.startsWith('aplp_module_hider.')? hide.substring('aplp_module_hider.'.length()) : hide
    }

    private static Provider removeHide(final Provider provider) {
        new Provider(provider.serviceName(), provider.providers().collect(FilteredJar.&removeHide) as List<String>)
    }

    private static Set<String> filterPackages(final Set<String> packages, final Closure<Boolean> filter) {
        packages.findAll { isPackageFiltered(it, filter) }
    }

    private static List<Provider> filterProviders(final List<Provider> providers, final Closure<Boolean> filter) {
        providers.collect { filterProvider(it, filter) }
    }

    private static boolean isPackageFiltered(final String name, final Closure<Boolean> filter) {
        filter(name)
    }

    private static Provider filterProvider(final Provider provider, final Closure<Boolean> filter) {
        return new Provider(provider.serviceName(), provider.providers().findAll { isEntryFiltered(it, filter) })
    }

    private static boolean isEntryFiltered(final String name, final Closure<Boolean> filter) {
        isPackageFiltered(name, filter)
    }

    @SuppressWarnings('unused')
    private static boolean pathFilter(final String a, final String b) {
        true
    }

    @Override
    Optional<URI> findFile(final String name) {
        final superFinder = super.findFile(name)
        final serviceFinder = superFinder.isEmpty()? super.findFile(name.replace('/services/', '/aplp_services/')) : superFinder
        serviceFinder.isEmpty()? super.findFile("aplp_module_hider/$name") : serviceFinder
    }

    @Override
    Manifest getManifest() {
        this.manifest ?: new Manifest() // Because ModLauncher is just shit
    }

    @CompileDynamic
    @Override
    Set<String> getPackages() {
        final packages = this.computePackages()
        if (this.cachedPackages == null) {
            if (this.filter == null) {
                LOGGER.error('Invalid request for packages prior to full class initialization')
            } else {
                this.cachedPackages = packages
            }
        }
        packages
    }

    @CompileDynamic
    @Override
    List<Provider> getProviders() {
        final providers = this.computeProviders()
        if (this.cachedProviders == null) {
            if (this.filter == null) {
                LOGGER.error('Invalid request for providers prior to full class initialization')
            } else {
                this.cachedProviders = providers
            }
        }
        providers
    }

    @CompileDynamic
    private Set<String> computePackages() {
        if (this.cachedPackages == null) {
            return filterPackages(super.packages.collect(FilteredJar.&removeHide) as Set<String>, this.filter ?: { true })
        }
        this.cachedPackages
    }

    @CompileDynamic
    private List<Provider> computeProviders() {
        if (this.cachedProviders == null) {
            final providers = super.providers.collect(FilteredJar.&removeHide) + this.findHiddenProviders()
            final filtered = filterProviders(providers, this.filter ?: { true })
            return filtered.findAll { !it.providers().isEmpty() }
        }
        this.cachedProviders
    }

    private List<Provider> findHiddenProviders() {
        final services = Paths.get this.findFile('META-INF/aplp_services').get()
        try (final walker = Files.walk(services)) {
            walker.toList()
                    .findAll { !Files.isDirectory(it) }
                    .collect { Provider.fromPath(it, FilteredJar.&pathFilter) }
                    .collect(FilteredJar.&removeHide) as List<Provider>
        }
    }
}
