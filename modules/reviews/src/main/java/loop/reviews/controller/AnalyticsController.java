package loop.reviews.controller;

import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.ProductDao;
import loop.reviews.db.ReviewDao;
import loop.reviews.model.Product;
import loop.reviews.model.Review;
import loop.reviews.util.SentimentAnalyzer;

import java.util.ArrayList;
import java.util.List;

/** FR9 - average rating, rating distribution donut and sentiment summary. */
public class AnalyticsController {

    @FXML private Label productNameLabel;
    @FXML private PieChart distributionChart;
    @FXML private Label avgStarsLabel;
    @FXML private Label avgValueLabel;
    @FXML private Label totalLabel;
    @FXML private VBox sentimentBox;

    private final ProductDao productDao = new ProductDao();
    private final ReviewDao reviewDao = new ReviewDao();

    @FXML
    private void initialize() {
        Product product = productDao.findById(Session.getSelectedProductId());
        if (product == null) { SceneManager.switchTo("home"); return; }
        productNameLabel.setText(product.getName());

        int[] dist = reviewDao.ratingDistribution(product.getId());
        int total = 0;
        for (int s = 1; s <= 5; s++) total += dist[s];

        List<PieChart.Data> data = new ArrayList<>();
        for (int s = 5; s >= 1; s--) {
            if (dist[s] > 0) data.add(new PieChart.Data(s + " star (" + dist[s] + ")", dist[s]));
        }
        if (data.isEmpty()) data.add(new PieChart.Data("No reviews", 1));
        distributionChart.setData(FXCollections.observableArrayList(data));

        double avg = product.getAverageRating();
        avgStarsLabel.setText(HomeController.starString(avg));
        avgValueLabel.setText(String.format("%.1f / 5", avg));
        totalLabel.setText("Based on " + total + (total == 1 ? " review" : " reviews"));

        // Sentiment over active comments.
        List<Review> reviews = reviewDao.findByProduct(product.getId(), "date", 0, null);
        List<String> comments = new ArrayList<>();
        for (Review r : reviews) comments.add(r.getCommentText());
        int[] sent = SentimentAnalyzer.summarise(comments);
        sentimentBox.getChildren().setAll(
            sentimentRow("Positive", sent[0], "sent-pos"),
            sentimentRow("Neutral",  sent[1], "sent-neu"),
            sentimentRow("Negative", sent[2], "sent-neg"));
    }

    private HBox sentimentRow(String label, int pct, String styleClass) {
        Label name = new Label(label);
        name.getStyleClass().add("field-label");
        name.setMinWidth(70);

        Region fill = new Region();
        fill.getStyleClass().addAll("sent-fill", styleClass);
        fill.setMinWidth(Math.max(2, pct * 2.2));
        fill.setPrefWidth(Math.max(2, pct * 2.2));

        Region track = new Region();
        HBox.setHgrow(track, Priority.ALWAYS);

        Label val = new Label(pct + "%");
        val.getStyleClass().add("dist-count");

        HBox row = new HBox(10, name, fill, track, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    @FXML private void goBack() { SceneManager.switchTo("product_reviews"); }
}
