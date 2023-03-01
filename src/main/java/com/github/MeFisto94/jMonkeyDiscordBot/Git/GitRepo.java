package com.github.MeFisto94.jMonkeyDiscordBot.Git;

import com.github.MeFisto94.jMonkeyDiscordBot.BranchOrCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitRepo {
    private final String name;
    private final List<String> branches;
    private final RepositorySynchronizer sync;
    private final File folder;
    private final ConcurrentHashMap<String, GitBranch> branchList;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepo.class);

    public GitRepo(String repoName, String URI, Stream<String> branches) {
        this.name = repoName;
        this.branches = branches.collect(Collectors.toList());
        sync = new RepositorySynchronizer(repoName, URI);
        folder = sync.getFolder();
        branchList = new ConcurrentHashMap<>();
    }

    public boolean hasBranch(String branchName) {
        return branches.contains(branchName);
    }

    public boolean needsToParseBranch(String branchName) {
        return branchList.get(branchName) == null;
    }

    public void parseBranch(String branchName) {
        BranchOrCommit boc = new BranchOrCommit(branchName, false);
        if (hasBranch(branchName) && needsToParseBranch(branchName)) {
            Optional<GitLock> lock;
            do {
                lock = sync.checkoutAndLock(boc);
                if (lock.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException intex) {
                        Thread.currentThread().interrupt();
                    }
                }
            } while (lock.isEmpty());

            GitBranch br = new GitBranch(this, boc);
            lock.get().releaseLock();
            branchList.put(branchName, br);
        }
    }

    public GitBranch getBranch(String branchName) {
        return branchList.get(branchName);
    }

    protected File getFolder() {
        return folder;
    }

    public void tickUpdate() {
        try {
            if (sync.needsCloning()) {
                LOGGER.debug("Cloning {}", name);
                sync.cloneRepository().get();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        }

        sync.checkNeedsUpdate(branches);

        if (sync.needsUpdate) {
            try {
                sync.pullRepository().get();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }
}
