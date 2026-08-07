package loop.reviews.util;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Optional;

/** Creates one consistently styled photo block for all review screens. */
public final class ReviewPhotoView {

    private ReviewPhotoView() { }

    public static StackPane create(String storedPath) {
        Optional<Image> image = ReviewImageService.loadImage(storedPath);
        if (image.isEmpty()) {
            return null;
        }
        ImageView imageView = new ImageView(image.get());
        imageView.setFitWidth(560);
        imageView.setFitHeight(320);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("review-photo");

        StackPane frame = new StackPane(imageView);
        frame.setAlignment(Pos.CENTER_LEFT);
        frame.setMaxWidth(580);
        frame.getStyleClass().add("review-photo-frame");
        return frame;
    }
}
