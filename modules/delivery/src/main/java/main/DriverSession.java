package main;

public final class DriverSession {

    private static String driverName;
    private static Runnable loginNavigator;

    private DriverSession() { }

    public static void start(String name, Runnable returnToLogin) {
        driverName = name;
        loginNavigator = returnToLogin;
    }

    public static String getDriverName() {
        return driverName == null || driverName.isBlank() ? "Current Driver" : driverName;
    }

    public static boolean returnToLogin() {
        Runnable navigator = loginNavigator;
        clear();
        if (navigator == null) {
            return false;
        }
        navigator.run();
        return true;
    }

    public static void clear() {
        driverName = null;
        loginNavigator = null;
    }
}
