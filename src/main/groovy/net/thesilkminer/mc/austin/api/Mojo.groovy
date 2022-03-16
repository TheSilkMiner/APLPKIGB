package net.thesilkminer.mc.austin.api

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Denotes a class as a provider of a loadable mod content (known as Mojo) in APLP.
 *
 * <p>For a class annotated with this annotation to be properly loaded, it must have a parameter-less constructor and
 * the value of {@link Mojo#value()} must also exist in the {@code mods.toml} file. The constructor of the class will
 * automatically be called when appropriate by APLP.</p>
 *
 * <p>Annotating a class with this annotation also implicitly adds two properties (namely {@code forgeBus} and
 * {@code mojoBus}) to the class, allowing users to reference the Forge and Mojo bus for event subscribing.</p>
 *
 * <p>An example of a simple class using this annotation is:</p>
 *
 * <pre>
 * {@literal @}Mojo("mymojo")
 * class MyMojo {
 *     MyMojo() {
 *         forgeBus.addEventListener(ForgeBusEventHandler)
 *     }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Mojo {
    /**
     * Gets the name that uniquely identifies this mojo.
     *
     * <p>This name must match one of the values present into the {@code mods.toml} file.</p>
     *
     * @return The name that uniquely identifies the mojo.
     *
     * @since 1.0.0
     */
    String value()
}
