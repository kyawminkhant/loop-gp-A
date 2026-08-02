package models;

public class Chef {
    private String chefID;
    private String chefName;
    private String speciality;
    private double averageRating;

    public Chef() {}

    public String getChefID() { return chefID; }
    public void setChefID(String chefID) { this.chefID = chefID; }
    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }
    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    @Override
    public String toString() {
        return chefName + " — " + speciality;
    }
}