package ProductPage.ProductPage;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/** Makes each module's LOOP logo return to the single, connected Team Hub. */
final class HubNavigation {

    private static final String INJECTED_LOGO_ID = "loop-hub-logo";
    private static final String NAVIGATION_MARKER = "loopHubNavigation";
    private static final Map<Stage, HubNavigation> ACTIVE = new WeakHashMap<>();

    private final Stage stage;
    private final ChangeListener<Scene> sceneListener;
    private ChangeListener<Parent> rootListener;
    private Scene observedScene;

    private HubNavigation(Stage stage) {
        this.stage = stage;
        this.sceneListener = (observable, oldScene, newScene) -> observe(newScene);
    }

    static void install(Stage stage) {
        uninstall(stage);
        HubNavigation navigation = new HubNavigation(stage);
        ACTIVE.put(stage, navigation);
        stage.sceneProperty().addListener(navigation.sceneListener);
        navigation.observe(stage.getScene());
    }

    static void uninstall(Stage stage) {
        HubNavigation existing = ACTIVE.remove(stage);
        if (existing != null) {
            existing.detach();
        }
    }

    private void observe(Scene scene) {
        detachRootListener();
        observedScene = scene;
        if (scene == null) {
            return;
        }

        rootListener = (observable, oldRoot, newRoot) -> addHubLogo(newRoot);
        scene.rootProperty().addListener(rootListener);
        addHubLogo(scene.getRoot());
    }

    private void addHubLogo(Parent root) {
        if (root == null) {
            return;
        }
        if (root.getStyleClass().contains("no-hub-logo")) {
            return;
        }

        /*
         * Orders and Delivery draw their logo as a text Label rather than an ImageView, so the
         * search below now matches those too. Previously they were missed and an image logo was
         * injected on top of the existing text, which looked broken.
         */
        Node logo = findLoopLogo(root);
        if (logo == null && root instanceof Pane) {
            ImageView injected = createHubLogo();
            ((Pane) root).getChildren().add(injected);
            injected.toFront();
            logo = injected;
        }

        if (logo != null) {
            wireLogo(logo);
        }
    }

    private Node findLoopLogo(Node node) {
        if (node instanceof ImageView && isLoopLogo((ImageView) node)
                && hasUsableImage((ImageView) node)) {
            return node;
        }
        if (node instanceof Label && isLoopTextLogo((Label) node)) {
            return node;
        }

        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                Node match = findLoopLogo(child);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    /**
     * Four Delivery screens declare an ImageView for images/logo.png, but that file is not in the
     * module, so the logo was invisible and unclickable while still blocking the fallback logo.
     * Treat a logo whose image did not load as absent so a working one is added instead.
     */
    private boolean hasUsableImage(ImageView imageView) {
        Image image = imageView.getImage();
        return image != null && !image.isError() && image.getWidth() > 0;
    }

    /** Orders and Delivery use a styled "Loop" text label as their logo. */
    private boolean isLoopTextLogo(Label label) {
        boolean logoStyle = label.getStyleClass().stream()
                .map(this::lower)
                .anyMatch(style -> style.contains("logo"));
        return logoStyle || "loop".equals(lower(label.getText()).trim());
    }

    private boolean isLoopLogo(ImageView imageView) {
        String id = lower(imageView.getId());
        if (id.equals("logo") || id.contains("looplogo") || id.contains("loop-logo")
                || id.contains("dashboardlogo") || id.equals(INJECTED_LOGO_ID)) {
            return true;
        }

        boolean logoStyle = imageView.getStyleClass().stream()
                .map(this::lower)
                .anyMatch(style -> style.contains("loop-logo") || style.contains("looplogo"));
        if (logoStyle) {
            return true;
        }

        Image image = imageView.getImage();
        String url = image == null ? "" : lower(image.getUrl());
        return url.contains("nobglooplogo") || url.contains("loop-logo");
    }

    private ImageView createHubLogo() {
        ImageView logo = new ImageView(new Image(
                HubNavigation.class.getResource("images/nobglooplogo.png").toExternalForm()));
        logo.setId(INJECTED_LOGO_ID);
        logo.setManaged(false);
        logo.setFitWidth(88);
        logo.setFitHeight(64);
        logo.setPreserveRatio(true);
        logo.setPickOnBounds(true);
        logo.setLayoutX(18);
        logo.setLayoutY(14);
        return logo;
    }

    private void wireLogo(Node logo) {
        if (Boolean.TRUE.equals(logo.getProperties().get(NAVIGATION_MARKER))) {
            return;
        }

        logo.getProperties().put(NAVIGATION_MARKER, true);
        logo.setPickOnBounds(true);
        logo.setCursor(Cursor.HAND);
        logo.setFocusTraversable(true);
        logo.setAccessibleText("Back to Loop Team Hub");
        Tooltip.install(logo, new Tooltip("Back to Loop Team Hub"));
        logo.setOnMouseClicked(event -> returnToHub());
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void returnToHub() {
        uninstall(stage);
        try {
            App.showHub(stage);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void detach() {
        stage.sceneProperty().removeListener(sceneListener);
        detachRootListener();
    }

    private void detachRootListener() {
        if (observedScene != null && rootListener != null) {
            observedScene.rootProperty().removeListener(rootListener);
        }
        rootListener = null;
        observedScene = null;
    }
}
