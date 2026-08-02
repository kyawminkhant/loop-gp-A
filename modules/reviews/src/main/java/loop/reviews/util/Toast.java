package loop.reviews.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Minimal toast notification. Shows a temporary message overlaid at the bottom
 * of the given StackPane (each screen's root is a StackPane), then fades out.
 * Used for confirmations and inline error feedback (FR3/FR7/FR10).
 */
public final class Toast {

    private Toast() { }

    public static void show(StackPane host, String message, boolean error) {
        if (host == null) return;
        Label toast = new Label(message);
        toast.getStyleClass().add(error ? "toast-error" : "toast-success");
        toast.setMaxWidth(420);
        toast.setWrapText(true);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new javafx.geometry.Insets(0, 0, 28, 0));
        host.getChildren().add(toast);

        FadeTransition in = new FadeTransition(Duration.millis(200), toast);
        in.setFromValue(0); in.setToValue(1);
        PauseTransition stay = new PauseTransition(Duration.millis(2600));
        FadeTransition out = new FadeTransition(Duration.millis(400), toast);
        out.setFromValue(1); out.setToValue(0);
        SequentialTransition seq = new SequentialTransition(in, stay, out);
        seq.setOnFinished(e -> host.getChildren().remove(toast));
        seq.play();
    }
}
