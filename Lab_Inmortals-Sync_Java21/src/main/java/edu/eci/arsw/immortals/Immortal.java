package edu.eci.arsw.immortals;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import edu.eci.arsw.concurrency.PauseController;

public final class Immortal implements Runnable {
  private final ReentrantLock lock = new ReentrantLock();
  private final String name;
  private int health;
  private final int damage;
  private final List<Immortal> population;
  private final ScoreBoard scoreBoard;
  private final PauseController controller;
  private volatile boolean running = true;

  public Immortal(String name, int health, int damage, List<Immortal> population, ScoreBoard scoreBoard, PauseController controller) {
    this.name = Objects.requireNonNull(name);
    this.health = health;
    this.damage = damage;
    this.population = Objects.requireNonNull(population);
    this.scoreBoard = Objects.requireNonNull(scoreBoard);
    this.controller = Objects.requireNonNull(controller);
  }

  public String name() { return name; }
  public int getHealth() { 
    lock.lock();
    try {
      return health;
    } finally {
      lock.unlock();
    }
  }
  public boolean isAlive() { return getHealth() > 0 && running; }
  public void stop() { running = false; }

  @Override
  public void run() {
    try {
      while (running) {
        controller.awaitIfPaused();
        if (!running) break;

        if (getHealth() <= 0) {
          synchronized (population) {
            population.remove(this);
          }
          break;
        }

        var opponent = pickOpponent();
        if (opponent == null) continue;

        String mode = System.getProperty("fight", "ordered");
        if ("naive".equalsIgnoreCase(mode)) fightNaive(opponent);
        else fightOrdered(opponent);

        Thread.sleep(2);
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }


  private Immortal pickOpponent() {
    if (population.size() <= 1) return null;
    Immortal other;
    do {
        other = population.get(ThreadLocalRandom.current().nextInt(population.size()));
    } while (other == this || !other.isAlive());
    return other.isAlive() ? other : null;
  }

  private void fightNaive(Immortal other) {
    Immortal first = this.name.compareTo(other.name) < 0 ? this : other;
    Immortal second = this.name.compareTo(other.name) < 0 ? other : this;
    synchronized (first) {
      synchronized (second) {
        if (this.health <= 0 || other.health <= 0) return;
        other.health = Math.max(0, other.health - this.damage);
        this.health += this.damage / 2;
        scoreBoard.recordFight();
      }
    }
  }

  
  private void fightOrdered(Immortal other) {
    Immortal first = this.name.compareTo(other.name) < 0 ? this : other;
    Immortal second = this.name.compareTo(other.name) < 0 ? other : this;
    boolean done = false;
    int retries = 5;
    while (!done && retries-- > 0) {
      try {
        if (first.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
          try {
            if (second.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
              try {
                if (this.health <= 0 || other.health <= 0) return;
                other.health = Math.max(0, other.health - this.damage);
                this.health += this.damage / 2;
                scoreBoard.recordFight();
                done = true;
              } finally {
                second.lock.unlock();
              }
            }
          } finally {
            first.lock.unlock();
          }
        }
        if (!done) {
          Thread.sleep(2 + ThreadLocalRandom.current().nextInt(5)); // backoff
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }
}
