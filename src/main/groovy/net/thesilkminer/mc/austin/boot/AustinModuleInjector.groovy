package net.thesilkminer.mc.austin.boot

import cpw.mods.modlauncher.api.IEnvironment
import cpw.mods.modlauncher.api.IModuleLayerManager
import cpw.mods.modlauncher.api.ITransformationService
import cpw.mods.modlauncher.api.ITransformer
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException
import groovy.transform.CompileStatic
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.nio.file.Files
import java.util.function.Function
import java.util.function.Supplier

@CompileStatic
final class AustinModuleInjector implements ITransformationService {
    private static final Logger LOGGER = LogManager.getLogger(AustinModuleInjector)
    private static final String NAME = 'aplp:module_injector'

    AustinModuleInjector() {}

    @Override
    String name() {
        NAME
    }

    @Override
    void initialize(final IEnvironment environment) {
        LOGGER.info('Module injector initialized in environment {}: time to hack around FML', environment)
        LOGGER.info('Module injector is located at {}', BootDataManager.INSTANCE.own)
    }

    @Override
    List<Resource> beginScanning(final IEnvironment environment) {
        // TODO("Use this to find our own locator if it is not found")
        []
    }

    @Override
    List<Resource> completeScan(final IModuleLayerManager layerManager) {
        // TODO("Use this to find mods if they are not found")
        []
    }

    @Override
    Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalClassesLocator() {
        final key = BootDataManager.INSTANCE.packages
                .findAll { it.startsWith('aplp_module_hider.') }
                .collect { "${it.substring('aplp_module_hider.'.length())}.".toString() }
                .toSet()
        final value = { -> AustinModuleInjector.&findClass as Function<String, Optional<URL>> }
        new AbstractMap.SimpleEntry<>(key, value as Supplier<Function<String, Optional<URL>>>)
    }

    @Override
    Map.Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalResourcesLocator() {
        null
    }

    @Override
    void onLoad(final IEnvironment env, final Set<String> otherServices) throws IncompatibleEnvironmentException {

    }

    @Override
    List<ITransformer> transformers() {
        []
    }

    private static Optional<URL> findClass(final String className) {
        final String resourceName = className.endsWith('.class') && className.contains('austin') && !className.contains('boot')? "aplp_module_hider/$className" : className
        final path = BootDataManager.INSTANCE.own.resolve(resourceName)
        Files.exists(path)? Optional.of(path.toUri().toURL()) : Optional.empty() as Optional<URL>
    }
}
