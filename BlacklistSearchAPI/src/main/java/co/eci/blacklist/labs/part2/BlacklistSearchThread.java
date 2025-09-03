package co.eci.blacklist.labs.part2;

import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;
import java.util.ArrayList;
import java.util.List;

public class BlacklistSearchThread extends Thread {
    private final HostBlackListsDataSourceFacade facade;
    private final String ip;
    private final int startIdx;
    private final int endIdx;
    private final List<Integer> matches = new ArrayList<>();
    private int checkedCount = 0;

    public BlacklistSearchThread(HostBlackListsDataSourceFacade facade, String ip, int startIdx, int endIdx) {
        this.facade = facade;
        this.ip = ip;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }
    @Override
    public void run() {
        for (int i = startIdx; i < endIdx; i++) {
            checkedCount++;
            if (facade.isInBlackListServer(i, ip)) {
                matches.add(i);
            }
        }
    }
    public List<Integer> getMatches() {
        return matches;
    }
    public int getCheckedCount() {
        return checkedCount;
    }
}
