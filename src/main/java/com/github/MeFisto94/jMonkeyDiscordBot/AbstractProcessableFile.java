package com.github.MeFisto94.jMonkeyDiscordBot;

import java.io.File;

public abstract class AbstractProcessableFile implements ProcessableFile {

    boolean processed;

    @Override
    public void processFile() {
        process();
        processed = true;
    }

    @Override
    public boolean isProcessed() {
        return processed;
    }

    public abstract void process();
}
