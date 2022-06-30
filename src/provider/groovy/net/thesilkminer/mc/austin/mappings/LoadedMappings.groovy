package net.thesilkminer.mc.austin.mappings

import groovy.transform.CompileStatic

@CompileStatic
class LoadedMappings {
    // moj class name (with dots) to map of moj -> srg names
    final Map<String, Map<String, String>> methods
    final Map<String, Map<String, String>> fields
    final Set<String> mappable

    LoadedMappings(Map<String, Map<String, String>> methods, Map<String, Map<String, String>> fields) {
        this.methods = methods
        this.fields = fields
        this.mappable = new HashSet<>(methods.keySet())
        this.mappable.addAll(fields.keySet())
    }
}
