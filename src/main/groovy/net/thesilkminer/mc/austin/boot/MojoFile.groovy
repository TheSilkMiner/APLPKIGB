package net.thesilkminer.mc.austin.boot


import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.fml.loading.moddiscovery.ModFile
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.locating.IModLocator

import java.util.jar.Manifest

@CompileStatic
@PackageScope
final class MojoFile {
    private static final List<String> BOOT_LAYER_PACKAGES = [
            'net.thesilkminer.mc.austin.boot'
    ]

    private static final List<String> GAME_LAYER_PACKAGES = [
            'net.thesilkminer.mc.austin.rt'
    ]

    private static final List<String> PAINT_PACKAGES = []

    @PackageScope
    static ModFile makeLoaderModFile(final IModLocator locator) {
        fuckCpwHardcodedFile('LANGPROVIDER', '.lang', filterLoader(), locator, { file -> new LoaderMojoFileInfo(file as ModFile) })
    }

    @PackageScope
    static ModFile makeGameModFile(final IModLocator locator) {
        fuckCpwHardcodedFile('MOD', '.rt', MojoFile.&filterGame, locator, { file -> RedirectingMojoFileInfo.of(file as ModFile, 'mods_aplp.toml') })
    }

    @PackageScope
    static ModFile makePaintModFile(final IModLocator locator) {
        fuckCpwHardcodedFile('MOD', '.paint', MojoFile.&filterPaint, locator, { file -> RedirectingMojoFileInfo.of(file as ModFile, 'mods_paint.toml') })
    }

    private static ModFile fuckCpwHardcodedFile(
            final String mojoType,
            final String discriminator,
            final Closure<Boolean> filter,
            final IModLocator locator,
            final Closure<IModFileInfo> parser
    ) {
        final jar = new FilteredJar(manifest(mojoType, BootDataManager.INSTANCE.version), [BootDataManager.INSTANCE.own], discriminator, filter)
        new ModFile(jar, locator, parser)
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
