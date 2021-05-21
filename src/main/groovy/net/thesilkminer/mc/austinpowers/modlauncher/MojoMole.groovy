package net.thesilkminer.mc.austinpowers.modlauncher

import groovy.transform.PackageScope
import net.minecraftforge.eventbus.api.IEventBus
import net.thesilkminer.mc.austinpowers.api.Mole

@PackageScope
class MojoMole implements Mole {
    private final MojoContainer mojoContainer

    @PackageScope
    MojoMole(final MojoContainer container) {
        this.mojoContainer = container
    }

    @Override
    IEventBus getMojoBus() {
        return this.mojoContainer.mojoBus
    }

    @Override
    String toString() {
        return "Mole{${this.mojoContainer.modId}}"
    }
}
