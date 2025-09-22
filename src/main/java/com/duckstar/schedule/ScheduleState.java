package com.duckstar.schedule;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ScheduleState {

    private final AtomicBoolean adminMode = new AtomicBoolean(false);

    public boolean isAdminMode() {
        return adminMode.get();
    }

    public void startAdminMode() {
        adminMode.set(true);
    }

    public void stopAdminMode() {
        adminMode.set(false);
    }

    private final AtomicBoolean weeklyScheduleRunning = new AtomicBoolean(false);

    public boolean isWeeklyScheduleRunning() {
        return weeklyScheduleRunning.get();
    }

    public void startRunning() {
        weeklyScheduleRunning.set(true);
    }

    public void stopRunning() {
        weeklyScheduleRunning.set(false);
    }
}
