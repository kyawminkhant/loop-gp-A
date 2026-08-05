package services;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import javafx.scene.image.Image;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/** Stores user-selected ingredient images beside the shared database. */
public final class InventoryImageService {

    private static final String IMAGE_DIRECTORY = "inventory-images";

    private InventoryImageService() { }

    public static String storeUploadedImage(File source) throws Exception {
        if (source == null) {
            return null;
        }

        Path directory = DBConnection.getDatabasePath().getParent().resolve(IMAGE_DIRECTORY);
        Files.createDirectories(directory);

        String originalName = source.getName().replaceAll("[^A-Za-z0-9._-]", "_");
        String safeName = System.currentTimeMillis() + "-" + originalName;
        Path destination = directory.resolve(safeName).normalize();
        if (!destination.startsWith(directory)) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return IMAGE_DIRECTORY + "/" + safeName;
    }

    public static Optional<Image> loadImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return Optional.empty();
        }

        try {
            if (imagePath.startsWith("/")) {
                try (InputStream stream = InventoryImageService.class.getResourceAsStream(imagePath)) {
                    return stream == null ? Optional.empty() : Optional.of(new Image(stream));
                }
            }

            if (imagePath.startsWith("file:")) {
                return Optional.of(new Image(URI.create(imagePath).toString()));
            }

            Path file = DBConnection.getDatabasePath().getParent().resolve(imagePath).normalize();
            if (Files.isRegularFile(file)) {
                return Optional.of(new Image(file.toUri().toString()));
            }
        } catch (Exception exception) {
            System.err.println("Could not load inventory image " + imagePath + ": "
                    + exception.getMessage());
        }
        return Optional.empty();
    }
}
