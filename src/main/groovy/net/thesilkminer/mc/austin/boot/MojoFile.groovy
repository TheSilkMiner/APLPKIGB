package net.thesilkminer.mc.austin.boot


import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.fml.loading.moddiscovery.ModFile
import net.minecraftforge.forgespi.locating.IModLocator

import java.util.jar.Manifest

@CompileStatic
@PackageScope
final class MojoFile {
    private static final class FuckYouCpwInterfacesExistForAReasonYouKnow {
        // Just so this class appears in stack traces just in case
        static <T> T exec(final Closure<T> closure) {
            closure()
        }
    }

    private static final List<String> BOOT_LAYER_PACKAGES = [
            'net.thesilkminer.mc.austin.boot'
    ]

    private static final List<String> GAME_LAYER_PACKAGES = [
            'net.thesilkminer.mc.austin.rt'
    ]

    private static final List<String> PAINT_PACKAGES = []

    @PackageScope
    static ModFile makeLoaderModFile(final IModLocator locator) {
        FuckYouCpwInterfacesExistForAReasonYouKnow.exec {
            final jar = new FilteredJar(manifest('LANGPROVIDER', BootDataManager.INSTANCE.version), [BootDataManager.INSTANCE.own], '.lang', filterLoader())
            new ModFile(jar, locator, LoaderMojoFileInfo.&new)
        }
    }

    @PackageScope
    static ModFile makeGameModFile(final IModLocator locator) {
        FuckYouCpwInterfacesExistForAReasonYouKnow.exec {
            final jar = new FilteredJar(manifest('MOD', BootDataManager.INSTANCE.version), [BootDataManager.INSTANCE.own], '.rt', MojoFile.&filterGame)
            new ModFile(jar, locator, { file -> RedirectingMojoFileInfo.of(file as ModFile, 'mods_aplp.toml') })
        }
    }

    @PackageScope
    static ModFile makePaintModFile(final IModLocator locator) {
        FuckYouCpwInterfacesExistForAReasonYouKnow.exec {
            final jar = new FilteredJar(manifest('MOD', BootDataManager.INSTANCE.version), [BootDataManager.INSTANCE.own], '.paint', MojoFile.&filterPaint)
            new ModFile(jar, locator, { file -> RedirectingMojoFileInfo.of(file as ModFile, 'mods_paint.toml') })
        }
    }

    private static Manifest manifest(final String type, final String version) {
        final manifest = new Manifest()
        manifest.mainAttributes.putValue('FMLModType', type)
        manifest.mainAttributes.putValue('Implementation-Version', version)
        manifest
    }

    private static Closure<Boolean> filterLoader() {
        final List<String> disallowedPackages = BOOT_LAYER_PACKAGES + GAME_LAYER_PACKAGES + PAINT_PACKAGES
        return { String name -> name.startsWith('net.thesilkminer.mc') && !disallowedPackages.any { name.startsWith(it) } }
    }

    private static boolean filterGame(final String name) {
        GAME_LAYER_PACKAGES.any { name.startsWith(it) }
    }

    private static boolean filterPaint(final String name) {
        PAINT_PACKAGES.any { name.startsWith(it) }
    }
}
