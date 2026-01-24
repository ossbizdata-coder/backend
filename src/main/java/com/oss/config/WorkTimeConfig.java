package com.oss.config;
import java.time.LocalTime;
import java.time.ZoneId;
public final class WorkTimeConfig {
    public static final ZoneId SRI_LANKA = ZoneId.of("Asia/Colombo");
    public static final LocalTime CHECKIN_START = LocalTime.of(6, 30); // 6:30 AM
    public static final LocalTime CHECKOUT_END = LocalTime.of(20, 30); // 8:30 PM
    private WorkTimeConfig() {}
}