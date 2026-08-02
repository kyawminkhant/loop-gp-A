package models;

public class CustomerPreference {

    private String preferenceID;
    private String customerID;
    private String favoriteCategories;
    private String notificationSettings;
    private String deliveryInstructions;

    public CustomerPreference() {}

    public String getPreferenceID() { return preferenceID; }
    public void setPreferenceID(String preferenceID) { this.preferenceID = preferenceID; }

    public String getCustomerID() { return customerID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }

    public String getFavoriteCategories() { return favoriteCategories; }
    public void setFavoriteCategories(String favoriteCategories) { this.favoriteCategories = favoriteCategories; }

    public String getNotificationSettings() { return notificationSettings; }
    public void setNotificationSettings(String notificationSettings) { this.notificationSettings = notificationSettings; }

    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }
}