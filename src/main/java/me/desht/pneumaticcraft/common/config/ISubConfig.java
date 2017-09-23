package me.desht.pneumaticcraft.common.config;

import java.io.File;
import java.io.IOException;

public interface ISubConfig {
    String getFolderName();

    void init(File file) throws IOException;

    void postInit() throws IOException;
}
