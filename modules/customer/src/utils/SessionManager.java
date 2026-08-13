package utils;

import java.util.function.Consumer;

import models.Customer;

public class SessionManager {

    private static Customer currentCustomer;
    private static Runnable personalizedProductNavigator;
    private static Runnable reviewAdminNavigator;
    private static Runnable productManagementNavigator;
    private static String currentDriverName;
    private static Consumer<String> driverDeliveryNavigator;

    public static void setCurrentCustomer(Customer customer) {
        currentCustomer = customer;
    }

    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }

    /**
     * Registered by the integrated Team Hub. The standalone Customer module
     * simply continues to its dashboard when no navigator is available.
     */
    public static void setPersonalizedProductNavigator(Runnable navigator) {
        personalizedProductNavigator = navigator;
    }

    public static boolean openPersonalizedProducts() {
        if (personalizedProductNavigator == null) {
            return false;
        }
        personalizedProductNavigator.run();
        return true;
    }

    public static void setReviewAdminNavigator(Runnable navigator) {
        reviewAdminNavigator = navigator;
    }

    public static boolean openReviewAdmin() {
        if (reviewAdminNavigator == null) {
            return false;
        }
        reviewAdminNavigator.run();
        return true;
    }

    public static void setProductManagementNavigator(Runnable navigator) {
        productManagementNavigator = navigator;
    }

    public static boolean openProductManagement() {
        if (productManagementNavigator == null) {
            return false;
        }
        productManagementNavigator.run();
        return true;
    }

    public static void setCurrentDriverName(String driverName) {
        currentDriverName = driverName;
    }

    public static String getCurrentDriverName() {
        return currentDriverName;
    }

    public static void setDriverDeliveryNavigator(Consumer<String> navigator) {
        driverDeliveryNavigator = navigator;
    }

    public static boolean openDriverDelivery() {
        if (driverDeliveryNavigator == null || currentDriverName == null) {
            return false;
        }
        driverDeliveryNavigator.accept(currentDriverName);
        return true;
    }

    public static void clearDriver() {
        currentDriverName = null;
    }

    public static void clear() {
        currentCustomer = null;
    }
}
