package com.sannsyn.dca.vaadin.servlet;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by mashiur on 3/25/16.
 */
public class DCASchedulerUtil {
    private static ScheduledExecutorService chartUpdateExecutorService = Executors.newScheduledThreadPool(5);
    public static ScheduledExecutorService getChartUpdateExecutorService() {
        return chartUpdateExecutorService;
    }
}
