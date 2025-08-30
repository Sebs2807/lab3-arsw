package edu.eci.arsw.pc;

import java.util.ArrayDeque;
import java.util.Deque;

/** Correcta implementación con espera eficiente (no busy-wait). */
public final class BusySpinQueue<T> {
    private final Deque<T> q = new ArrayDeque<>();
    private final int capacity;

    public BusySpinQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void put(T item) throws InterruptedException {
        while (q.size() == capacity) {
            wait(); // espera hasta que haya espacio
        }
        q.addLast(item);
        notifyAll(); // despierta  hilos
    }

    public synchronized T take() throws InterruptedException {
        while (q.isEmpty()) {
            wait(); // espera hasta que haya elementos
        }
        T v = q.pollFirst();
        notifyAll(); // despierta hilos
        return v;
    }

    public synchronized int size() {
        return q.size();
    }
}
