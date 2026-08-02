package models;

public class Customer {

    private String customerID;
    private String personID;
    private String deliveryAddress;
    private String idCardNo;
    private String idCardImagePath;
    private String status;

    private String name;
    private String email;
    private String mobile;

    public Customer() {}

    public String getCustomerID() { return customerID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }

    public String getPersonID() { return personID; }
    public void setPersonID(String personID) { this.personID = personID; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getIdCardNo() { return idCardNo; }
    public void setIdCardNo(String idCardNo) { this.idCardNo = idCardNo; }

    public String getIdCardImagePath() { return idCardImagePath; }
    public void setIdCardImagePath(String idCardImagePath) { this.idCardImagePath = idCardImagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    // Preference snapshot for Super Admin live view
    private String favoriteCategories;
    private String notificationSettings;
    private String deliveryInstructions;

    public String getFavoriteCategories() { return favoriteCategories; }
    public void setFavoriteCategories(String favoriteCategories) { this.favoriteCategories = favoriteCategories; }

    public String getNotificationSettings() { return notificationSettings; }
    public void setNotificationSettings(String notificationSettings) { this.notificationSettings = notificationSettings; }

    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }
}