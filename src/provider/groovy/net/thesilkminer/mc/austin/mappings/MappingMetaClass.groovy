package net.thesilkminer.mc.austin.mappings

import groovy.transform.CompileStatic

@CompileStatic
class MappingMetaClass extends DelegatingMetaClass {

    final LoadedMappings mappings

    MappingMetaClass(MetaClass delegate, LoadedMappings mappings) {
        super(delegate)
        this.mappings = mappings
    }

    @Override
    Object invokeMissingMethod(Object instance, String methodName, Object[] arguments) {
        var map = mappings.methods.get(theClass.name)
        if (map!=null) {
            // Check whether the method is in the mappables
            // If it is, map it and invoke that method
            String mapped = map.get(methodName)
            if (mapped!=null) return invokeMethod(instance, mapped, arguments)
        }
        return delegate.invokeMissingMethod(instance, methodName, arguments)
    }

    @Override
    Object invokeMissingProperty(Object instance, String propertyName, Object optionalValue, boolean isGetter) {
        var map = mappings.fields.get(theClass.name)
        if (map!=null) {
            // Check whether the field is in the mappables
            // If it is, map it and invoke that method
            String mapped = map.get(propertyName)
            if (mapped!=null) return invokeMethod(instance, mapped, optionalValue, isGetter)
        }
        return delegate.invokeMissingMethod(instance, propertyName, optionalValue, isGetter)
    }
}
