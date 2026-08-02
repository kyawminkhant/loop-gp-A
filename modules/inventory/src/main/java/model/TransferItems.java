package model;

public class TransferItems {

    private String fromAddress;
    private String toAddress;
    private String product;
    private int quantity;
    private String reason;
    private String manager;

    public TransferItems() {
    }

    public TransferItems(String fromAddress,
                         String toAddress,
                         String product,
                         int quantity,
                         String reason, String manager) {

        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.product = product;
        this.quantity = quantity;
        this.reason = reason;
        this.manager = manager;
    }

    public String getFromAddress() { 
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }
    
    public String getManager() {
		return manager;
	}

    public void setReason(String reason) {
        this.reason = reason;
    }

	
}