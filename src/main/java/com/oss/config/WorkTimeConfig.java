package com.oss.config;
import java.time.LocalTime;
import java.time.ZoneId;
public final class WorkTimeConfig {
    // ✅ Server stores everything in UTC
    // Timezone is ONLY used to determine "what is today" for the user
    // Changed to Asia/Kuala_Lumpur to fix midnight date issue
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Kuala_Lumpur");

    // Legacy constant name kept for compatibility (Sri Lanka = UTC+5:30)
    public static final ZoneId SRI_LANKA = ZoneId.of("Asia/Colombo");

    public static final LocalTime CHECKIN_START = LocalTime.of(6, 30); // 6:30 AM
    public static final LocalTime CHECKOUT_END = LocalTime.of(20, 30); // 8:30 PM
    private WorkTimeConfig() {}
}