package co.eci.blacklist.domain;

import java.util.List;

public record MatchResult(
        String ip,
        boolean trustworthy,
        List<Integer> matches,
        int checkedServers,
        int totalServers,
        long elapsedMs,
        int threads
) {}
