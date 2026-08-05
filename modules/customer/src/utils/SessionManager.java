package utils;

import models.Customer;

public class SessionManager {

    private static Customer currentCustomer;
    private static Runnable personalizedProductNavigator;
    private static Runnable reviewAdminNavigator;
    private static Runnable productManagementNavigator;

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

    public static void clear() {
        currentCustomer = null;
    }
}
