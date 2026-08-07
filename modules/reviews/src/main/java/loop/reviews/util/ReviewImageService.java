package loop.reviews.util;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/** Stores review photos beside the shared database and resolves them portably. */
public final class ReviewImageService {

    private static final String UPLOAD_FOLDER = "review-images";
    private static final int MAX_EDGE = 1200;
    private static final long MAX_SOURCE_BYTES = 12L * 1024L * 1024L;

    private ReviewImageService() { }

    /** Verifies that a selected file is a supported, readable image. */
    public static void validate(File source) throws IOException {
        readAndValidate(source);
    }

    /**
     * Imports a review photo as an optimized PNG and returns a database-relative path.
     * The path is relative to the directory containing loop.db, not the current process.
     */
    public static String importImage(File source, int customerId, int productId)
            throws IOException {
        BufferedImage original = readAndValidate(source);
        BufferedImage optimized = resizeToFit(original, MAX_EDGE);

        Path directory = uploadDirectory();
        Files.createDirectories(directory);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String filename = "review-" + productId + "-" + customerId + "-" + suffix + ".png";
        Path target = directory.resolve(filename);
        Path temporary = directory.resolve(filename + ".tmp");

        try {
            if (!ImageIO.write(optimized, "png", temporary.toFile())) {
                throw new IOException("PNG writer is unavailable.");
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return UPLOAD_FOLDER + "/" + filename;
    }

    /** Loads a stored review image, including legacy absolute file paths. */
    public static Optional<Image> loadImage(String storedPath) {
        Path resolved = resolveStoredPath(storedPath);
        if (resolved == null || !Files.isRegularFile(resolved)) {
            return Optional.empty();
        }
        try {
            Image image = new Image(
                    resolved.toUri().toString(), 560, 320, true, true, false);
            return image.isError() ? Optional.empty() : Optional.of(image);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    /** Removes only managed review images; legacy paths outside the upload folder are untouched. */
    public static void deleteManagedImage(String storedPath) {
        Path resolved = resolveStoredPath(storedPath);
        Path directory = uploadDirectory();
        if (resolved == null || !resolved.startsWith(directory)) {
            return;
        }
        try {
            Files.deleteIfExists(resolved);
        } catch (IOException ignored) {
            // A missing image should not prevent a review record from being deleted.
        }
    }

    static Path resolveStoredPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        try {
            if (storedPath.startsWith("file:")) {
                return Path.of(URI.create(storedPath)).toAbsolutePath().normalize();
            }
            Path raw = Path.of(storedPath);
            return raw.isAbsolute()
                    ? raw.normalize()
                    : databaseDirectory().resolve(raw).normalize();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static BufferedImage readAndValidate(File source) throws IOException {
        if (source == null || !source.isFile()) {
            throw new IOException("Choose an image file that still exists.");
        }
        if (source.length() > MAX_SOURCE_BYTES) {
            throw new IOException("Review photos must be 12 MB or smaller.");
        }
        BufferedImage image = ImageIO.read(source);
        if (image == null) {
            throw new IOException(
                    "Unsupported image. Choose a genuine PNG, JPG, JPEG, or GIF file.");
        }
        return image;
    }

    private static BufferedImage resizeToFit(BufferedImage source, int maximumEdge) {
        int longest = Math.max(source.getWidth(), source.getHeight());
        double scale = longest <= maximumEdge ? 1.0 : (double) maximumEdge / longest;
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int type = source.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        BufferedImage result = new BufferedImage(width, height, type);
        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return result;
    }

    private static Path uploadDirectory() {
        return databaseDirectory().resolve(UPLOAD_FOLDER).toAbsolutePath().normalize();
    }

    private static Path databaseDirectory() {
        Path database = Path.of(System.getProperty("loop.db.path", "database/loop.db"))
                .toAbsolutePath().normalize();
        Path parent = database.getParent();
        return parent == null ? Path.of(".").toAbsolutePath().normalize() : parent;
    }
}
