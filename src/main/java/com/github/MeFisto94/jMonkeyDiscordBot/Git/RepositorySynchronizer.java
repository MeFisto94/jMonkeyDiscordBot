package com.github.MeFisto94.jMonkeyDiscordBot.Git;

import com.github.MeFisto94.jMonkeyDiscordBot.BranchOrCommit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RepositorySynchronizer {
    File folder;
    Git git;
    Repository repository;
    GitLock lock;
    boolean needsCloning = false;
    boolean needsUpdate = false;
    String name;
    String URI;

    public RepositorySynchronizer(String name, String URI) throws IllegalStateException {
        this.name = name;
        this.URI = URI;
        folder = new File("repositories/" + name);
        lock = new GitLock();

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IllegalStateException("Unable to create the required directory repositories/" + name + ". Check your permissions");
            }

            needsCloning = true;
        } else {
            try {
                git = Git.open(folder);
                git.checkout();
                repository = git.getRepository();
            } catch (IOException io) {
                throw new IllegalStateException("IO Exception when accessing repository", io);
            }
        }
    }

    public boolean needsCloning() {
        return needsCloning;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public CompletableFuture<Void> cloneRepository() throws IllegalArgumentException {
        return CompletableFuture.runAsync(() -> {
            try {
                git = Git.cloneRepository()
                        .setURI(URI)
                        .setDirectory(folder)
                        /*.setBranchesToClone(Arrays.asList("refs/heads/" + branchOrCommit.getContent()))
                        .setBranch("refs/heads/" + branchOrCommit.getContent())*/
                        .call();
                repository = git.getRepository();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> pullRepository() throws IllegalArgumentException {
        return CompletableFuture.runAsync(() -> {
            try {
                git.pull().call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void checkNeedsUpdate() {
        try {
            repository.getRefDatabase().getRefs().forEach(ref -> {
                try {
                    repository.updateRef(ref.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            needsUpdate = repository.getRefDatabase().getRefs().stream().anyMatch(ref -> {
                try {

                    if (ref.getName().startsWith("refs/tags/")) {
                        return false; // Ignore tags as they need to be on a branch anyway.
                    }
                    // @TODO FIXME: BranchTrackingStatus.of() fails for most stuff, because it cannot find the trackingBranch...
                    // but it works for master and only there...
                    String name = ref.getName();//.substring("refs/remotes/origin".length() + 1);
                    BranchTrackingStatus tracking = BranchTrackingStatus.of(repository, name);

                    if (tracking == null) {
                        System.err.println("Error in checkNeedsUpdate()");
                        return false;
                    } else {
                        /*System.out.println(tracking.getAheadCount());
                        System.out.println(tracking.getBehindCount());*/
                        return tracking.getAheadCount() > 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<GitLock> checkoutAndLock(BranchOrCommit boc) {
        if (lock.acquireLock()) {
            if (boc.isBranch()) {
                try {
                    // "refs/head/origin/" +
                    //git.checkout().setStartPoint(boc.getContent()).call(); // @TODO: FIXME
                } catch (Exception e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            }

            return Optional.of(lock);
        } else {
            return Optional.empty();
        }
    }

    public File getFolder() {
        return folder;
    }
}
