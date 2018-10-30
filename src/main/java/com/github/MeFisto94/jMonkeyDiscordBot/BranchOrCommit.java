package com.github.MeFisto94.jMonkeyDiscordBot;

public class BranchOrCommit {
    boolean commit;
    String content;

    public BranchOrCommit(String content, boolean commit) {
        this.content = content;
        this.commit = commit;
    }

    public boolean isCommit() {
        return commit;
    }

    public boolean isBranch() {
        return !commit;
    }

    public String getContent() {
        return content;
    }

    public String toPath() {
        String path = "repositories/";
        if (!commit) {
            path += "branches/";
            path += content;
        } else {
            path += "commits/";
            path += content;
        }

        return path;
    }
}
