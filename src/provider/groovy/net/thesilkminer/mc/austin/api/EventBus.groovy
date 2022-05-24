/*
 * This file is part of APLP: KIGB, licensed under the MIT License
 *
 * Copyright (c) 2022 TheSilkMiner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
