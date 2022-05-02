package net.thesilkminer.mc.austin.boot

import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.core.file.FileConfig
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.fml.loading.moddiscovery.InvalidModFileException
import net.minecraftforge.fml.loading.moddiscovery.ModFile
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo

import java.nio.file.Files
import java.nio.file.Path

@CompileStatic
@PackageScope
final class RedirectingMojoFileInfo {
    private static final class RedirectingWrapper implements IConfigurable {
        private final UnmodifiableConfig config
        private IModFileInfo file

        @PackageScope
        RedirectingWrapper(final UnmodifiableConfig config, final IModFileInfo info = null) {
            this.config = config
            this.file = info
        }

        void setFile(final IModFileInfo info) {
            this.file = info
        }

        @Override
        <T> Optional<T> getConfigElement(final String... key) {
            this.config.getOptional(key as List<String>).map {
                it instanceof UnmodifiableConfig? ((it as UnmodifiableConfig).valueMap()) as T : it as T
            }
        }

        @Override
        List<? extends IConfigurable> getConfigList(final String... key) {
            final List<String> path = key as List<String>
            if (this.config.contains(path) && !(this.config.get(path) instanceof Collection<?>)) {
                throw new InvalidModFileException('Invalid data', this.file)
            }
            this.config.getOrElse(path, []).collect { new RedirectingWrapper(it as UnmodifiableConfig, this.file) }
        }
    }

    @PackageScope
    static IModFileInfo of(final ModFile file, final String fileName) {
        final Path data = file.findResource('META-INF', fileName)

        if (!Files.exists(data)) {
            throw new IllegalStateException()
        }

        final FileConfig config = FileConfig.builder(data).build()
        config.load()
        config.close()
        final RedirectingWrapper wrapper = new RedirectingWrapper(config)
        final IModFileInfo info = new ModFileInfo(file, wrapper, [])
        wrapper.file = info
        info
    }
}
