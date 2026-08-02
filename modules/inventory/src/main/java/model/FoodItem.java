package model;

public class FoodItem {
	private int id;
    private String name;
    private String URLPath;

    public FoodItem(int id, String name, String URLPath) {
    		this.id = id;
        this.name = name;
        this.URLPath = URLPath;
    }
    
    public FoodItem(String name, String URLPath) {
    		this.name = name;
    		this.URLPath = URLPath;
}

    public String getName() {
        return name;
    }
    
    public int getID() {
        return id;
    }
    
    public String getURLPath() {
        return URLPath;
    }
    
	
}