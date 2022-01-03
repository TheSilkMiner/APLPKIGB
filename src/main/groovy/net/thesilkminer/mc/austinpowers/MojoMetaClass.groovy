package net.thesilkminer.mc.austinpowers

import groovy.transform.PackageScope

@PackageScope
class MojoMetaClass extends DelegatingMetaClass {

    private static final OVERRIDDEN_PROPERTIES = [
            'mojoBus' : MojoMetaClass.&obtainMojoBus,
            'forgeBus' : MojoMetaClass.&obtainForgeBus
    ]

    private static final OVERRIDDEN_METHODS = [
            'toString' : MojoMetaClass.&mojoToString
    ]

    private static final FORGE_BUS = {
        Class.forName('net.minecraftforge.common.MinecraftForge').getDeclaredField('EVENT_BUS').get(null)
    }.memoize()

    private final MojoContainer metaContainer

    MojoMetaClass(final MetaClass delegate, final MojoContainer metaContainer) {
        super(delegate)
        this.metaContainer = metaContainer
    }

    @Override
    Object getProperty(final Object object, final String property) {
        final propertyProvider = OVERRIDDEN_PROPERTIES[property]
        propertyProvider != null? propertyProvider(this) : super.getProperty(object, property)
    }

    @Override
    Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        final method = OVERRIDDEN_METHODS[methodName]
        method != null? method(this, arguments) : super.invokeMethod(object, methodName, arguments)
    }

    private static obtainMojoBus(final MojoMetaClass self) {
        self.metaContainer.mojoBus
    }

    private static obtainForgeBus(final MojoMetaClass self) {
        FORGE_BUS()
    }

    private static mojoToString(final MojoMetaClass self, final Object[] args) {
        "Mojo[${self.metaContainer.modId} -> ${self.metaContainer.mod.getClass().name}]"
    }

    @Override
    String toString() {
        "MojoMetaClass[${this.metaContainer.modId} -> ${super.toString()}]"
    }
}
