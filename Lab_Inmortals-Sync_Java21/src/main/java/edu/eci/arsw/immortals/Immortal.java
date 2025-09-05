package edu.eci.arsw.immortals;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import edu.eci.arsw.concurrency.PauseController;

public final class Immortal implements Runnable {
    private final String name;
    private final AtomicInteger health;
    private final int damage;
    private final ConcurrentLinkedQueue<Immortal> population;
    private final ScoreBoard scoreBoard;
    private final PauseController controller;
    private volatile boolean running = true;

    public Immortal(String name, int health, int damage, ConcurrentLinkedQueue<Immortal> population,
                    ScoreBoard scoreBoard, PauseController controller) {
        this.name = Objects.requireNonNull(name);
        this.health = new AtomicInteger(health);
        this.damage = damage;
        this.population = Objects.requireNonNull(population);
        this.scoreBoard = Objects.requireNonNull(scoreBoard);
        this.controller = Objects.requireNonNull(controller);
    }

    public String name() { return name; }

    public int getHealth() { return health.get(); }

    public boolean isAlive() { return health.get() > 0 && running; }

    public void stop() { running = false; }

    @Override
    public void run() {
        try {
            while (running) {
                controller.awaitIfPaused();
                if (!running) break;

                if (health.get() <= 0) {
                    population.remove(this);
                    running = false;
                    break;
                }

                Immortal opponent = pickOpponent();
                if (opponent == null) continue;

                fight(opponent);

                Thread.sleep(2);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Immortal pickOpponent() {
        Immortal[] arr = population.toArray(new Immortal[0]);
        if (arr.length <= 1) return null;
        Immortal other;
        do {
            other = arr[ThreadLocalRandom.current().nextInt(arr.length)];
        } while (other == this || !other.isAlive());
        return other.isAlive() ? other : null;
    }

    private void fight(Immortal other) {
        int h;
        do {
            h = other.health.get();
            if (h <= 0) return;
        } while (!other.health.compareAndSet(h, Math.max(0, h - damage)));

        health.addAndGet(damage / 2);
        scoreBoard.recordFight();
    }

}
