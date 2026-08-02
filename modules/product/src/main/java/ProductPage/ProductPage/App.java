package ProductPage.ProductPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;
    private static final String DATABASE_URL = "jdbc:sqlite:" +
            System.getProperty("loop.db.path", "database/loop.db");

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Team Hub"), 1280, 800);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("team-hub.css").toExternalForm());

        Image icon = new Image(getClass().getResourceAsStream("images/appicon.png"));
        stage.getIcons().add(icon);
        stage.setTitle("Loop");

        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        cleanUnreferencedProductUploads();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    static void setRoot(Parent root) {
        scene.setRoot(root);
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        launch();
    }

    private static void cleanUnreferencedProductUploads() {
        Path uploadsDirectory = productUploadsDirectory();
        if (!Files.isDirectory(uploadsDirectory)) {
            return;
        }

        Set<Path> referencedImages = loadReferencedUploadImages();
        if (referencedImages == null) {
            return;
        }
        try (Stream<Path> uploads = Files.walk(uploadsDirectory)) {
            uploads
                .sorted(Comparator.reverseOrder())
                .forEach(path -> deleteUploadPath(path, uploadsDirectory, referencedImages));
        } catch (IOException exception) {
            System.err.println("Could not clean product uploads: " + exception.getMessage());
        }
    }

    private static Path productUploadsDirectory() {
        Path projectRoot = projectRootDirectory();
        Path projectUploads = projectRoot.resolve("product-uploads").normalize();
        if (Files.exists(projectUploads)) {
            return projectUploads;
        }
        return Paths.get("product-uploads").toAbsolutePath().normalize();
    }

    private static Path projectRootDirectory() {
        try {
            Path codeLocation = Paths.get(
                App.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            ).toAbsolutePath().normalize();

            if (codeLocation.endsWith(Paths.get("target", "classes"))) {
                return codeLocation.getParent().getParent();
            }
            if (codeLocation.endsWith("bin")) {
                return codeLocation.getParent();
            }
            return codeLocation;
        } catch (Exception exception) {
            return Paths.get("").toAbsolutePath().normalize();
        }
    }

    private static Set<Path> loadReferencedUploadImages() {
        Set<Path> referencedImages = new HashSet<>();
        try (
            Connection connection = DriverManager.getConnection(DATABASE_URL);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT imageURL FROM product_ProductImage")
        ) {
            while (result.next()) {
                Path uploadPath = referencedUploadPath(result.getString("imageURL"));
                if (uploadPath != null) {
                    referencedImages.add(uploadPath);
                }
            }
        } catch (SQLException exception) {
            System.err.println("Could not read product image references: " + exception.getMessage());
            return null;
        }
        return referencedImages;
    }

    private static Path referencedUploadPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        String cleanUrl = imageUrl.replace('\\', '/');
        if (!cleanUrl.startsWith("product-uploads/")) {
            return null;
        }

        return Path.of(cleanUrl).toAbsolutePath().normalize();
    }

    private static void deleteUploadPath(
            Path path,
            Path uploadsDirectory,
            Set<Path> referencedImages) {
        try {
            if (Files.isRegularFile(path)) {
                Path absolutePath = path.toAbsolutePath().normalize();
                if (!referencedImages.contains(absolutePath)) {
                    Files.deleteIfExists(path);
                }
                return;
            }

            if (!path.equals(uploadsDirectory)) {
                Files.deleteIfExists(path);
            } else if (isDirectoryEmpty(path)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException exception) {
            System.err.println("Could not delete upload path " + path + ": " + exception.getMessage());
        }
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (Stream<Path> children = Files.list(directory)) {
            return children.findAny().isEmpty();
        }
    }
}
