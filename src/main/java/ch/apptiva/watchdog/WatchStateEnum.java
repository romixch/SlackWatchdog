package ch.apptiva.watchdog;

public enum WatchStateEnum {
    UNKNOWN(1000), HEALTHY(1000 * 60 * 15), UNWELL(1000 * 60 * 1), SICK(1000 * 60 * 5);


    private int pollingIntervalInMillis;

    WatchStateEnum(int pollingIntervalInMillis) {

        this.pollingIntervalInMillis = pollingIntervalInMillis;
    }

    public int getPollingIntervalInMillis() {
        return pollingIntervalInMillis;
    }
}
