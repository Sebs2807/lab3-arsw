package co.eci.blacklist.api.dto;

import co.eci.blacklist.domain.MatchResult;
import java.util.List;

public record CheckResponseDTO(
        String ip,
        boolean trustworthy,
        List<Integer> matches,
        int checkedServers,
        int totalServers,
        long elapsedMs,
        int threads
) {
    public static CheckResponseDTO from(MatchResult r) {
        return new CheckResponseDTO(r.ip(), r.trustworthy(), r.matches(), r.checkedServers(), r.totalServers(), r.elapsedMs(), r.threads());
    }
}
