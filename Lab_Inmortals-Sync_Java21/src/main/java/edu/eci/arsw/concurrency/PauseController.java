package edu.eci.arsw.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class PauseController {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();
    private final Condition allPaused = lock.newCondition();
    private volatile boolean paused = false;

    private int threadsPaused = 0;
    private final int totalHilos;

    public PauseController(int totalHilos) {
        this.totalHilos = totalHilos;
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

    public boolean paused() {
        return paused;
    }

	public void awaitIfPaused() throws InterruptedException {
		lock.lockInterruptibly();
		try {
			while (paused) {
				threadsPaused++;
				if (threadsPaused == totalHilos) {
					allPaused.signalAll();
				}
				try {
					unpaused.await();
				} finally {
					threadsPaused--;
				}
			}
		} finally {
			lock.unlock();
		}
	}

    public void waitUntilAllPaused() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (threadsPaused < totalHilos) {
                allPaused.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
