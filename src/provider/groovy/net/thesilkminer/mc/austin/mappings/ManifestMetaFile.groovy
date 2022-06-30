package net.thesilkminer.mc.austin.mappings

import com.google.gson.annotations.Expose
import groovy.transform.CompileStatic

@CompileStatic
class ManifestMetaFile {
    @Expose
    List<VersionMeta> versions

    @CompileStatic
    class VersionMeta {
        @Expose
        String id
        @Expose
        String url
        @Expose
        String sha1
    }
}
