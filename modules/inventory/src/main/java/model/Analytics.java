package model;

public class Analytics {

	private String product;
	private String locationColumn;
	private int currentQuantity;
	private int minThreshold;
	private String status;
	private String lastRestock;
	private int daysUntilReorder;
	
	
	public Analytics(String product,String locationColumn,
			int currentQuantity,int minThreshold,String status,String lastRestock,int daysUntilReorder) {
		this.product = product;
		this.locationColumn = locationColumn;
		this.currentQuantity = currentQuantity;
		this.minThreshold = minThreshold;
		this.status = status;
		this.lastRestock = lastRestock;
		this.daysUntilReorder = daysUntilReorder;

	}
	
	public String getProduct() {
		return product;
	}
	
	public String getLocation() {
		return locationColumn;
	}
	
	public int getCurrentQuantity() {
		return currentQuantity;
	}
	
	public int getMinThreshold() {
		return minThreshold;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getLastRestock() {
		return lastRestock;
	}
	
	public int getDaysUntilReorder() {
		return daysUntilReorder;
	}
	
}
