package me.desht.pneumaticcraft.common.config.aux;

import java.io.File;
import java.io.IOException;

public interface IAuxConfig {
    /**
     * Get the filename (within to the "pneumaticcraft/" folder in the top-level config directory) where this
     * config should be stored.
     *
     * @return the config file name
     */
    String getConfigFilename();

    /**
     * Called during the pre-init phase, with the top-level mod config file name (pneumaticcraft.cfg)
     *
     * @param file the mod config file name
     * @throws IOException if there is a problem reading/writing any files
     */
    void preInit(File file) throws IOException;

    /**
     * Called during the post-init phase.  No file parameter is provide here; if you need one, cache it in the
     * {@link #preInit(File)} method.
     *
     * @throws IOException if there is a problem reading/writing any files
     */
    void postInit() throws IOException;
}
