package com.github.MeFisto94.jMonkeyDiscordBot.Git;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Regular Locks are designed to provide exactly one Thread access to a given ressource.
 * In the case of a git repository we don't even want the same thread to access the ressource twice.
 * On the other hand the same same ressource can even be accessed by multiple threads simultaneously.
 * We only need to make sure that only one "thing" at the same time has access to the lock to checkout another branch
 * for this repository. That's why there is a checkoutAndLock method.
 */
public class GitLock {
    AtomicBoolean isLocked;

    public GitLock() {
        this.isLocked = new AtomicBoolean(false);
    }

    public boolean acquireLock() {
        return isLocked.compareAndSet(false, true);
    }

    public boolean releaseLock() {
        isLocked.set(false);
        return true;
    }
}
