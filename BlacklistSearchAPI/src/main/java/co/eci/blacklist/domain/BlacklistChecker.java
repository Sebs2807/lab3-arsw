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

    public MatchResult checkHost(String ip, int nThreads) {
        int threshold = policies.getAlarmCount();
        int total = facade.getRegisteredServersCount();
        long start = System.currentTimeMillis();

        // Refactorización para parar en 5 encuentros de las listas negras
        AtomicInteger coincidencias = new AtomicInteger(0);
        AtomicInteger revisados = new AtomicInteger(0);
        AtomicBoolean parar = new AtomicBoolean(false);

        List<Integer> matches = Collections.synchronizedList(new ArrayList<>());

        int chunk = total / nThreads;
        int remainder = total % nThreads;
        int begin = 0;

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            int startIdx = begin;   // final (efectivamente)
            int endIdx = begin + chunk + (i < remainder ? 1 : 0);
            begin = endIdx; // reasignamos begin para la siguiente iteración

            Thread t = new Thread(() -> {
                for (int s = startIdx; s < endIdx && !parar.get(); s++) {
                    revisados.incrementAndGet();

                    if (facade.isInBlackListServer(s, ip)) {
                        matches.add(s);
                        int current = coincidencias.incrementAndGet();
                        // Revisión para parar los hilos si se llegó al alarm count de policies
                        if (current >= threshold) {
                            parar.set(true);
                            break;
                        }
                    }
                }
            });
            threads.add(t);
        }

        for (Thread t : threads) {
            t.start();  // arranca cada hilo
        }
        
        // Esperar que terminen
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        boolean trustworthy = coincidencias.get() < threshold;
        logger.info("revisados blacklists: " + revisados.get() + " of " + total);
        if (trustworthy) {
            facade.reportAsTrustworthy(ip);
        } else {
            facade.reportAsNotTrustworthy(ip);
        }

        long elapsed = System.currentTimeMillis() - start;
        return new MatchResult(ip, trustworthy, List.copyOf(matches),
                revisados.get(), total, elapsed, nThreads);
    }
}
