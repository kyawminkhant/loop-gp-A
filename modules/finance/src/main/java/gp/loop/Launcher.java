package gp.loop;

import javafx.application.Application;

/**
 * Entry point for non-modular / classpath launches (Maven {@code javafx:run}, many IDE setups).
 * <p><b>Eclipse:</b> run {@code mvn compile} (or Maven → Update Project) first so
 * {@code target/javafx-modules} exists. The shared launch config passes
 * {@code --module-path .../target/javafx-modules --add-modules javafx.controls,javafx.fxml}.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
