package co.eci.blacklist.labs.part2;
import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;

public class MainThreads {
    private static final HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();

    public static void main(String[] args) throws InterruptedException {
        String ip = "200.24.34.55";
        int totalServers = facade.getRegisteredServersCount();

        int chunk = totalServers / 3;
        BlacklistSearchThread t1 = new BlacklistSearchThread(facade, ip, 0, chunk);
        BlacklistSearchThread t2 = new BlacklistSearchThread(facade, ip, chunk, 2 * chunk);
        BlacklistSearchThread t3 = new BlacklistSearchThread(facade, ip, 2 * chunk, totalServers);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        int totalMatches = t1.getMatches().size() + t2.getMatches().size() + t3.getMatches().size();
        int totalChecked = t1.getCheckedCount() + t2.getCheckedCount() + t3.getCheckedCount();

        System.out.println("Total matches: " + totalMatches);
        System.out.println("Checked servers: " + totalChecked + " of " + totalServers);
        System.out.println("Matches indices: ");
        System.out.println(t1.getMatches());
        System.out.println(t2.getMatches());
        System.out.println(t3.getMatches());
	}
}