package co.eci.blacklist.labs.part1;

public class CountThread extends Thread {
    private final int from;
    private final int to;

    public CountThread(int from, int to) {
        this.from = from;
        this.to = to;
        setName("CountThread-" + from + "-" + to);
    }

    @Override
    public void run() {
        for (int i = from; i <= to; i++) {
            System.out.println("[" + getName() + "] " + i);
        }
    }
}