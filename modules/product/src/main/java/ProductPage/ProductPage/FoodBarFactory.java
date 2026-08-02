package ProductPage.ProductPage;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.util.function.Consumer;

public final class FoodBarFactory {

    private FoodBarFactory() {
    }

    public static Parent create(FoodBarData data) throws IOException {
        return create(data, null, false);
    }

    public static Parent create(
            FoodBarData data,
            boolean showCalories) throws IOException {
        return create(data, null, showCalories);
    }

    public static Parent create(
            FoodBarData data,
            Consumer<FoodBarData> selectionHandler) throws IOException {
        return create(data, selectionHandler, false);
    }

    public static Parent create(
            FoodBarData data,
            Consumer<FoodBarData> selectionHandler,
            boolean showCalories) throws IOException {

        FXMLLoader loader = new FXMLLoader(
            FoodBarFactory.class.getResource("FoodBarTemplate.fxml")
        );

        Parent card = loader.load();
        FoodBarController controller = loader.getController();
        controller.setData(data);
        controller.setCaloriesVisible(showCalories);
        controller.setSelectionHandler(selectionHandler);
        return card;
    }
}
