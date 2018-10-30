package com.github.MeFisto94.jMonkeyDiscordBot;

import java.io.File;

public interface ProcessableFile {
    void processFile();
    boolean isProcessed();
    File getFile();
}
