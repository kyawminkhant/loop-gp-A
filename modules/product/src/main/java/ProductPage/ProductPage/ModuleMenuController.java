package ProductPage.ProductPage;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ModuleMenuController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label firstTitle;
    @FXML private Label firstCaption;
    @FXML private Label secondTitle;
    @FXML private Label secondCaption;
    @FXML private Label statusLabel;
    @FXML private Button secondButton;

    private String module;
    private String firstView;
    private String secondView;

    public void configure(String title, String subtitle, String module,
            String firstTitleText, String firstCaptionText, String firstView,
            String secondTitleText, String secondCaptionText, String secondView) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        this.module = module;
        this.firstView = firstView;
        this.secondView = secondView;
        firstTitle.setText(firstTitleText);
        firstCaption.setText(firstCaptionText);
        secondTitle.setText(secondTitleText);
        secondCaption.setText(secondCaptionText);
        secondButton.setVisible(secondView != null);
        secondButton.setManaged(secondView != null);
    }

    @FXML
    private void openFirst() {
        launch(firstView);
    }

    @FXML
    private void openSecond() {
        launch(secondView);
    }

    private void launch(String view) {
        try {
            statusLabel.setText("Opening " + module + "...");
            ModuleLauncher.showInCurrentWindow(statusLabel.getScene(), module, view);
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not open " + module + ": " + ex.getMessage());
        }
    }
}
