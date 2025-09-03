package co.eci.blacklist.application;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.MatchResult;
import org.springframework.stereotype.Service;

@Service
public class BlacklistService {
    private final BlacklistChecker checker;

    public BlacklistService(BlacklistChecker checker) {
        this.checker = checker;
    }

    public MatchResult check(String ip, int threads) {
        return checker.checkHost(ip, threads);
    }
}
