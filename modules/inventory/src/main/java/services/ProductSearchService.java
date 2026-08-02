package services;

import dao.FoodItemDAO;
import model.FoodItem;


public class ProductSearchService {


    public FoodItem search(String code){

        return FoodItemDAO
                .getProductByIdOrName(code);

    }
}