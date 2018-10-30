package com.github.MeFisto94.jMonkeyDiscordBot.Git;

import com.github.MeFisto94.jMonkeyDiscordBot.BranchOrCommit;
import com.github.MeFisto94.jMonkeyDiscordBot.JavaClass;
import com.github.MeFisto94.jMonkeyDiscordBot.Main;
import com.github.MeFisto94.jMonkeyDiscordBot.Module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GitBranch {
    GitRepo repo;
    BranchOrCommit boc;
    String branchName;
    List<Module> moduleList;

    public GitBranch(GitRepo repo, BranchOrCommit boc) {
        this.repo = repo;
        this.boc = boc;
        this.branchName = boc.getContent();
        moduleList = Arrays.stream(repo.getFolder().listFiles())
                        .filter(f -> f.isDirectory())
                        // when checking out the engine, only treat jme3-xyz modules.
                        .filter(f -> (!repo.getName().equals("engine") && !repo.getName().equals("sdk")) || f.getName().startsWith("jme3-"))
                        .map(f -> new Module(repo.getFolder(), f, boc))
                        // used to save power during dev.
                        .filter(f -> !Main.Dev_Powersave || f.getName().equals("jme3-core"))
                        .collect(Collectors.toList());

        moduleList.parallelStream().forEach(Module::parseModule);
    }

    public @Nullable
    JavaClass findClassByName(@Nonnull String className) {
        return moduleList.stream()
                .map(m -> m.findClassByName(className))
                .filter(m -> m != null)
                .findFirst()
                .orElse(null);
    }

    public @Nullable JavaClass findClassByNameIgnoreCase(@Nonnull String className) {
        return moduleList.stream()
                .map(m -> m.findClassByNameIgnoreCase(className))
                .filter(m -> m != null)
                .findFirst()
                .orElse(null);
    }

    public @Nullable JavaClass findClassBothCases(@Nonnull String className) {
        JavaClass jc = findClassByName(className);

        if (jc != null) {
            return jc;
        } else {
            return findClassByNameIgnoreCase(className);
        }
    }
}
