package model;

public class StorageLocations {

	private String name;
	private String locations;
	private int locationID;
	private int capacity;
	private int currentStock;
	private String manager;	
	
	public StorageLocations(String name,String locations,
			int locationID,int capacity, int currentStock, String manager) {
		this.name = name;
		this.locations = locations;
		this.locationID = locationID;
		this.capacity = capacity;
		this.currentStock = currentStock;
		this.manager = manager;
	}
	
	
	public StorageLocations(String locations) {
		this.locations = locations;
	}

	public double getUsage() {
	    if (capacity == 0) {
	        return 0;
	    }
	    return Math.round(((currentStock * 100.0) / capacity));
	}
	
	public String getName() {
		return name;
	}
	
	public String getLocations() {
		return locations;
	}
	
	public int getLocationID() {
		return locationID;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public int getCurrentStock() {
		return currentStock;
	}
	
	public String getManager() {
		return manager;
	}
	
	
}
