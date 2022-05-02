package net.thesilkminer.mc.austin.boot

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.IModFile

@CompileStatic
@PackageScope
class LoaderMojoFileInfo implements IModFileInfo {
    final IModFile file

    @PackageScope
    LoaderMojoFileInfo(final IModFile file) {
        this.file = file
    }

    @Override
    List<IModInfo> getMods() {
        []
    }

    @Override
    List<LanguageSpec> requiredLanguageLoaders() {
        []
    }

    @Override
    boolean showAsResourcePack() {
        false
    }

    @Override
    Map<String, Object> getFileProperties() {
        [:]
    }

    @Override
    String getLicense() {
        BootDataManager.INSTANCE.license
    }

    @Override
    String moduleName() {
        this.file.secureJar.name()
    }

    @Override
    String versionString() {
        BootDataManager.INSTANCE.version
    }

    @Override
    List<String> usesServices() {
        []
    }

    @Override
    IModFile getFile() {
        this.file
    }

    @Override
    IConfigurable getConfig() {
        new IConfigurable() {
            @Override
            <T> Optional<T> getConfigElement(final String... key) {
                Optional.empty()
            }

            @Override
            List<? extends IConfigurable> getConfigList(final String... key) {
                []
            }
        }
    }
}
