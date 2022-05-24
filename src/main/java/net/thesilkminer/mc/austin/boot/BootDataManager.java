package net.thesilkminer.mc.austin.boot;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

final class BootDataManager {
    private static final BootDataManager INSTANCE = new BootDataManager();

    private volatile Path own;

    private BootDataManager() {
        this.own = null;
    }

    static BootDataManager get() {
        return INSTANCE;
    }

    Path own() {
        if (this.own == null) {
            synchronized (this) {
                if (this.own == null) {
                    try {
                        this.own = Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                    } catch (final URISyntaxException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return this.own;
    }
}
