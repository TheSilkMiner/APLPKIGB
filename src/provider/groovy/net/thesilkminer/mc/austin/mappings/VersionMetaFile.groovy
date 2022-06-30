package net.thesilkminer.mc.austin.mappings

import com.google.gson.annotations.Expose
import groovy.transform.CompileStatic

@CompileStatic
class VersionMetaFile {
    @Expose
    DownloadsMeta downloads

    @CompileStatic
    class DownloadsMeta {
        @Expose
        MappingsMeta client_mappings
        @Expose
        MappingsMeta server_mappings
    }

    @CompileStatic
    class MappingsMeta {
        @Expose
        String sha1
        @Expose
        String url
    }
}
