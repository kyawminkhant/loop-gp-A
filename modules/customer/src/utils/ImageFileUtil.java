package utils;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ImageFileUtil {

    private static final Path ID_CARD_DIR = Path.of("id_cards");

    private ImageFileUtil() {}

    public static String saveIdCardImage(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.isFile()) {
            throw new IOException("ID card image file not found.");
        }

        Files.createDirectories(ID_CARD_DIR);

        String extension = getExtension(sourceFile.getName());
        String storedName = UUID.randomUUID() + extension;
        Path target = ID_CARD_DIR.resolve(storedName);

        Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public static Image loadImage(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }

        File file = new File(storedPath);
        if (!file.isFile()) {
            return null;
        }

        return new Image(file.toURI().toString(), true);
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return ".png";
        }
        return filename.substring(dot).toLowerCase();
    }
}
