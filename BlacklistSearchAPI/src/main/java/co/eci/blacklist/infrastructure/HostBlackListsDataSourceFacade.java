package co.eci.blacklist.infrastructure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Simplified, thread-safe facade inspired by the ARSW lab.
 * In the original lab this class is provided and should not be modified.
 * Here we provide a minimal in-memory implementation suitable for the REST service and tests.
 */
public class HostBlackListsDataSourceFacade {

    private static final Logger logger = Logger.getLogger(HostBlackListsDataSourceFacade.class.getName());
    private static final HostBlackListsDataSourceFacade INSTANCE = new HostBlackListsDataSourceFacade();

    private final int registeredServersCount;
    private final ConcurrentMap<String, Set<Integer>> blacklistedByIp = new ConcurrentHashMap<>();

    private HostBlackListsDataSourceFacade() {
        this.registeredServersCount = 10_000;
        // Seed some deterministic data for demo purposes
        seed("200.24.34.55", List.of(0,1,2,3,4,5,6,7,8,9)); // concentrated early
        seed("202.24.34.55", List.of(5,111,999,2048,4096,8191)); // dispersed
    }

    public static HostBlackListsDataSourceFacade getInstance() {
        return INSTANCE;
    }

    public int getRegisteredServersCount() {
        return registeredServersCount;
    }

    public boolean isInBlackListServer(int serverIndex, String ip) {
        Set<Integer> set = blacklistedByIp.get(ip);
        return set != null && set.contains(serverIndex);
    }

    public void reportAsTrustworthy(String ip) {
        logger.info("HOST " + ip + " Reported as trustworthy");
    }

    public void reportAsNotTrustworthy(String ip) {
        logger.info("HOST " + ip + " Reported as NOT trustworthy");
    }

    // Utilities
    public void seed(String ip, List<Integer> indices) {
        blacklistedByIp.computeIfAbsent(ip, k -> ConcurrentHashMap.newKeySet()).addAll(indices);
    }

    public void clear(String ip) {
        blacklistedByIp.remove(ip);
    }
}
