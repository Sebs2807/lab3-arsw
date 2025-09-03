package co.eci.blacklist.infrastructure;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.Policies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean
    public HostBlackListsDataSourceFacade hostBlackListsDataSourceFacade() {
        return HostBlackListsDataSourceFacade.getInstance();
    }

    @Bean
    public BlacklistChecker blacklistChecker(HostBlackListsDataSourceFacade facade, Policies policies) {
        return new BlacklistChecker(facade, policies);
    }
}
