package utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Shared UI animation helpers for polished screen / control transitions.
 */
public final class AnimationUtil {

    private AnimationUtil() {}

    /** Fade + slight upward slide when a screen appears. */
    public static void fadeSlideIn(Node node) {
        if (node == null) return;
        node.setOpacity(0);
        node.setTranslateY(18);

        FadeTransition fade = new FadeTransition(Duration.millis(380), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(380), node);
        slide.setFromY(18);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();
    }

    /** Soft pop-in for cards / panels. */
    public static void popIn(Node node) {
        if (node == null) return;
        node.setOpacity(0);
        node.setScaleX(0.92);
        node.setScaleY(0.92);

        FadeTransition fade = new FadeTransition(Duration.millis(340), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(340), node);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale).play();
    }

    /** Fade content when switching tabs. */
    public static void fadeInContent(Node node) {
        if (node == null) return;
        node.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(260), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /** Gentle shake for validation / error feedback. */
    public static void shake(Node node) {
        if (node == null) return;
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), node);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }

    /** Quick highlight when a success message appears. */
    public static void pulse(Node node) {
        if (node == null) return;
        node.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), node);
        scale.setFromX(0.94);
        scale.setFromY(0.94);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale).play();
    }

    /** Staggered fade-in for a list of cards / sections. */
    public static void staggerIn(Node... nodes) {
        if (nodes == null) return;
        int delay = 0;
        for (Node node : nodes) {
            if (node == null) continue;
            node.setOpacity(0);
            node.setTranslateY(12);

            FadeTransition fade = new FadeTransition(Duration.millis(300), node);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(delay));

            TranslateTransition slide = new TranslateTransition(Duration.millis(300), node);
            slide.setFromY(12);
            slide.setToY(0);
            slide.setDelay(Duration.millis(delay));
            slide.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(fade, slide).play();
            delay += 70;
        }
    }

    /** Hover / press scale feedback on common action buttons. */
    public static void enableButtonEffects(Parent root) {
        if (root == null) return;

        for (Node node : root.lookupAll(".button")) {
            if (!(node instanceof Button button)) continue;

            button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e ->
                    scaleTo(button, 1.045));
            button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e ->
                    scaleTo(button, 1.0));
            button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e ->
                    scaleTo(button, 0.96));
            button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
                if (button.isHover()) {
                    scaleTo(button, 1.045);
                } else {
                    scaleTo(button, 1.0);
                }
            });
        }
    }

    /** Count-up style animation for numeric labels (e.g. order stats). */
    public static void countUp(Labeled label, int target) {
        if (label == null) return;
        final int frames = 18;
        final int[] frame = {0};

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(28), e -> {
            frame[0]++;
            double progress = (double) frame[0] / frames;
            int value = (int) Math.round(target * progress);
            label.setText(String.valueOf(value));
        }));
        timeline.setCycleCount(frames);
        timeline.setOnFinished(e -> label.setText(String.valueOf(target)));
        timeline.play();
    }

    private static void scaleTo(Node node, double value) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(120), node);
        scale.setToX(value);
        scale.setToY(value);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        scale.play();
    }

    /** Soft opacity pulse for live indicators (text badges). */
    public static Animation startGlowPulse(Node node) {
        if (node == null) return null;
        FadeTransition fade = new FadeTransition(Duration.seconds(1.4), node);
        fade.setFromValue(0.45);
        fade.setToValue(1.0);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setInterpolator(Interpolator.EASE_BOTH);
        fade.play();
        return fade;
    }

    /**
     * Flow-field particle ambient (CodePen-style), Loop colour palette.
     */
    public static void attachBackgroundAmbient(Parent contentRoot) {
        if (contentRoot == null || contentRoot.getScene() == null) return;

        Scene scene = contentRoot.getScene();

        if (scene.getRoot() instanceof StackPane existing
                && existing.getStyleClass().contains("ambient-root")) {
            return;
        }

        StackPane wrap = new StackPane();
        wrap.getStyleClass().add("ambient-root");

        FlowFieldAmbient.Handle ambient = FlowFieldAmbient.create(wrap);

        wrap.getChildren().addAll(ambient.getLayer(), contentRoot);
        scene.setRoot(wrap);

        fadeSlideIn(wrap);
    }

    /** Kept for call-site compatibility — logo no longer animates. */
    public static void startAmbientLoop(Node node) {
        // intentionally empty
    }
}
