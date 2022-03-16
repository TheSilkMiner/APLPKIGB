package net.thesilkminer.mc.austin.api

/**
 * Identifies the event bus to which an annotation refers to.
 *
 * @since 1.0.0
 */
enum EventBus {
    /**
     * The Mojo bus, where main mojo lifecycle events are posted.
     *
     * @since 1.0.0
     */
    MOJO,
    /**
     * The Mojo bus, where main mojo lifecycle events are posted.
     *
     * <p>It is <strong>highly suggested</strong> to use {@link #MOJO} instead. This field is provided only to ease
     * adoption.</p>
     *
     * @since 1.0.0
     */
    MOD,
    /**
     * The Forge bus, where game events are posted.
     *
     * @since 1.0.0
     */
    FORGE
}
