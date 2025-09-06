package edu.eci.arsw.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntSupplier;

public final class PauseController {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();
    private final Condition allPaused = lock.newCondition();

    private boolean paused = false;
    private int threadsPaused = 0;
    private final IntSupplier liveThreadsSupplier;

    public PauseController(IntSupplier liveThreadsSupplier) {
        this.liveThreadsSupplier = liveThreadsSupplier;
    }

    public void pause() {
        lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
    }

    public void resume() {
        lock.lock();
        try {
            paused = false;
            unpaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void awaitIfPaused() throws InterruptedException {
        lock.lock();
        boolean counted = false;
        try {
            while (paused) {
                if (!counted) {
                    threadsPaused++;
                    counted = true;
                    if (threadsPaused == liveThreadsSupplier.getAsInt()) {
                        allPaused.signalAll();
                    }
                }
                unpaused.await();
            }
        } finally {
            if (counted) {
                threadsPaused--;
            }
            lock.unlock();
        }
    }

    public void waitUntilAllPaused() throws InterruptedException {
        lock.lock();
        try {
            while (paused && threadsPaused < liveThreadsSupplier.getAsInt()) {
                allPaused.await();
            }
        } finally {
            lock.unlock();
        }
    }
}