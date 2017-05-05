package com.github.rerorero.oleoleflake.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executor {
    public static ExecutorService gen() {
        int procs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        if (procs < 3) {
            procs = 3;
        }
        return Executors.newFixedThreadPool(procs);
    }
}
