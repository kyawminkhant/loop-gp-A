package loop.reviews.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewImageServiceTest {

    @TempDir
    Path temporaryDirectory;

    private String previousDatabasePath;

    private void useTemporaryDatabasePath() {
        previousDatabasePath = System.getProperty("loop.db.path");
        System.setProperty(
                "loop.db.path",
                temporaryDirectory.resolve("database").resolve("loop.db").toString());
    }

    @AfterEach
    void restoreDatabasePath() {
        if (previousDatabasePath == null) {
            System.clearProperty("loop.db.path");
        } else {
            System.setProperty("loop.db.path", previousDatabasePath);
        }
    }

    @Test
    void importsAndResizesPhotoBesideSharedDatabase() throws Exception {
        useTemporaryDatabasePath();
        Path source = createPng("large-source.png", 1800, 900);

        String storedPath = ReviewImageService.importImage(source.toFile(), 12, 34);
        Path imported = ReviewImageService.resolveStoredPath(storedPath);

        assertTrue(storedPath.startsWith("review-images/review-34-12-"));
        assertTrue(storedPath.endsWith(".png"));
        assertTrue(Files.isRegularFile(imported));
        assertTrue(imported.startsWith(
                temporaryDirectory.resolve("database").resolve("review-images")));

        BufferedImage image = ImageIO.read(imported.toFile());
        assertEquals(1200, image.getWidth());
        assertEquals(600, image.getHeight());
    }

    @Test
    void rejectsFileThatIsNotAnImage() throws Exception {
        useTemporaryDatabasePath();
        Path fakeImage = temporaryDirectory.resolve("not-an-image.png");
        Files.writeString(fakeImage, "This is not PNG data.");

        IOException exception = assertThrows(
                IOException.class,
                () -> ReviewImageService.importImage(fakeImage.toFile(), 1, 2));

        assertTrue(exception.getMessage().contains("Unsupported image"));
    }

    @Test
    void deletesOnlyManagedReviewImage() throws Exception {
        useTemporaryDatabasePath();
        Path source = createPng("source.png", 160, 120);
        String storedPath = ReviewImageService.importImage(source.toFile(), 3, 4);
        Path imported = ReviewImageService.resolveStoredPath(storedPath);

        ReviewImageService.deleteManagedImage(storedPath);

        assertFalse(Files.exists(imported));
        assertTrue(Files.exists(source));
    }

    private Path createPng(String filename, int width, int height) throws IOException {
        Path source = temporaryDirectory.resolve(filename);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(190, 72, 49));
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(new Color(244, 182, 58));
        graphics.fillOval(width / 4, height / 4, width / 2, height / 2);
        graphics.dispose();
        ImageIO.write(image, "png", source.toFile());
        return source;
    }
}
