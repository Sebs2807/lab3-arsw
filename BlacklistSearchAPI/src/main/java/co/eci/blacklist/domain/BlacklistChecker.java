package co.eci.blacklist.domain;

import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;
import co.eci.blacklist.labs.part2.BlacklistSearchThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class BlacklistChecker {

    private static final Logger logger = Logger.getLogger(BlacklistChecker.class.getName());

    private final HostBlackListsDataSourceFacade facade;
    private final Policies policies;

    public BlacklistChecker(HostBlackListsDataSourceFacade facade, Policies policies) {
        this.facade = Objects.requireNonNull(facade);
        this.policies = Objects.requireNonNull(policies);
    }

    /** 
    public MatchResult checkHost(String ip, int nThreads) {
        int threshold = policies.getAlarmCount();
        int total = facade.getRegisteredServersCount();

        long start = System.currentTimeMillis();
        AtomicInteger found = new AtomicInteger(0);
        AtomicInteger checked = new AtomicInteger(0);
        AtomicBoolean stop = new AtomicBoolean(false);
        List<Integer> matches = Collections.synchronizedList(new ArrayList<>());

        int threads = Math.max(1, nThreads);
        int chunk = Math.max(1, total / threads);

        List<Future<?>> futures = new ArrayList<>(threads);
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                final int startIdx = i * chunk;
                final int endIdx = (i == threads - 1) ? total : Math.min(total, (i + 1) * chunk);
                futures.add(exec.submit(() -> {
                    for (int s = startIdx; s < endIdx && !stop.get(); s++) {
                        // short-circuit if another thread already reached threshold
                        if (stop.get()) break;
                        if (facade.isInBlackListServer(s, ip)) {
                            matches.add(s);
                            if (found.incrementAndGet() >= threshold) {
                                stop.set(true);
                                // do not break immediately: allow checked counter to reflect this check
                            }
                        }
                        checked.incrementAndGet();
                    }
                }));
            }
            // wait
            for (Future<?> f : futures) {
                f.get();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during blacklist checking", e);
        }

        boolean trustworthy = found.get() < threshold;
        // Keep original lab-style log line about coverage
        logger.info("Checked blacklists: " + checked.get() + " of " + total);
        if (trustworthy) {
            facade.reportAsTrustworthy(ip);
        } else {
            facade.reportAsNotTrustworthy(ip);
        }
        long elapsed = System.currentTimeMillis() - start;
        return new MatchResult(ip, trustworthy, List.copyOf(matches), checked.get(), total, elapsed, threads);
    }

    */

    public MatchResult checkHost(String ip, int nThreads) {
        int threshold = policies.getAlarmCount();
        int total = facade.getRegisteredServersCount();
        long start = System.currentTimeMillis();
        List<BlacklistSearchThread> threads = new ArrayList<>();
        List<Integer> matches = Collections.synchronizedList(new ArrayList<>());
        int chunk = total / nThreads;
        int remainder = total % nThreads;
        int begin = 0;
        // Crear los hilos y asignar rangos
        for (int i = 0; i < nThreads; i++) {
            int end = begin + chunk + (i < remainder ? 1 : 0);
            BlacklistSearchThread thread = new BlacklistSearchThread(facade, ip, begin, end);
            threads.add(thread);
            begin = end;
        }
        // Ejecutar los hilos
        for (BlacklistSearchThread t : threads) {
            t.start();
        }
        for (BlacklistSearchThread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        }

        // Sumar resultados
        int found = 0;
        int checked = 0;
        for (BlacklistSearchThread t : threads) {
            matches.addAll(t.getMatches());
            found += t.getMatches().size();
            checked += t.getCheckedCount();
        }

        boolean trustworthy = found < threshold;
        logger.info("Checked blacklists: " + checked + " of " + total);
        if (trustworthy) {
            logger.info("HOST " + ip + " Reported as trustworthy");
            facade.reportAsTrustworthy(ip);
        } else {
            logger.info("HOST " + ip + " Reported as NOT trustworthy");
            facade.reportAsNotTrustworthy(ip);
        }
        long elapsed = System.currentTimeMillis() - start;
        return new MatchResult(ip, trustworthy, List.copyOf(matches), checked, total, elapsed, nThreads);
    }
}
