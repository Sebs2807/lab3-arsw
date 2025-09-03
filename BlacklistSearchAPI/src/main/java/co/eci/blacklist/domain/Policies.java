package co.eci.blacklist.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blacklist")
public class Policies {
    /**
     * Minimum occurrences to flag an IP as NOT trustworthy.
     */
    private int alarmCount = 5;

    public int getAlarmCount() {
        return alarmCount;
    }

    public void setAlarmCount(int alarmCount) {
        this.alarmCount = alarmCount;
    }
}
