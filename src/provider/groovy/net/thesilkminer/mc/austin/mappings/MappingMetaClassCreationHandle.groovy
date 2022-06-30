package net.thesilkminer.mc.austin.mappings

import groovy.transform.CompileStatic

@CompileStatic
class MappingMetaClassCreationHandle extends MetaClassRegistry.MetaClassCreationHandle {

    final MetaClassRegistry.MetaClassCreationHandle delegated
    final LoadedMappings mappings

    private static boolean hasWrapped = false

    MappingMetaClassCreationHandle(MetaClassRegistry.MetaClassCreationHandle delegated, LoadedMappings mappings) {
        this.delegated = delegated
        this.mappings = mappings
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        return wrapMetaClass(delegated.createNormalMetaClass(theClass, registry))
    }

    MetaClass wrapMetaClass(MetaClass delegated) {
        if (mappings.mappable.contains(delegated.theClass.name)) {
            // Check if the class is in the remapping key set
            return new MappingMetaClass(delegated, mappings)
        }
        return delegated
    }

    static applyCreationHandle() {
        if (!hasWrapped) {
            hasWrapped = true
            var registry = GroovySystem.metaClassRegistry
            var instance = new MappingMetaClassCreationHandle(registry.getMetaClassCreationHandler(), MappingsProvider.INSTANCE.mappingsProvider.get())
            registry.metaClassCreationHandle = instance
            synchronized (MetaClassRegistry) {
                for (def it : registry.iterator()) {
                    if (it instanceof MetaClass) registry.setMetaClass(it.theClass, instance.wrapMetaClass(it))
                }
            }
        }
    }
}
