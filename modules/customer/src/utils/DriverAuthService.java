package utils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class DriverAuthService {

    private static final String DEMO_PASSWORD = "driver123";
    private static final Map<String, String> DRIVERS = createDrivers();

    private DriverAuthService() { }

    public static String authenticate(String driverId, String password) {
        if (driverId == null || password == null || !DEMO_PASSWORD.equals(password)) {
            return null;
        }
        return DRIVERS.get(driverId.trim().toLowerCase(Locale.ROOT));
    }

    private static Map<String, String> createDrivers() {
        Map<String, String> drivers = new LinkedHashMap<>();
        drivers.put("iman", "Iman");
        drivers.put("efrin", "Efrin");
        drivers.put("prakash", "Prakash");
        drivers.put("jonny", "Jonny");
        drivers.put("samira", "Samira");
        return Map.copyOf(drivers);
    }
}
