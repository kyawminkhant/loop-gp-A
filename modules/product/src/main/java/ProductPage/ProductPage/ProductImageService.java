package ProductPage.ProductPage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

public final class ProductImageService {

    private static final int OUTPUT_SIZE = 700;
    private static final String RESOURCE_PREFIX = "exampleFoods/";
    private static final Path IMAGE_DIRECTORY = Path.of(
        "src",
        "main",
        "resources",
        "ProductPage",
        "ProductPage",
        "exampleFoods"
    );
    private static final DateTimeFormatter DATABASE_DATE =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ProductImageService() {
    }

    public static String addProductImage(
            File source,
            String productName,
            int displayOrder,
            int productId)
            throws IOException, ClassNotFoundException, SQLException {

        String resourcePath = optimizeProductImage(
            source,
            productName,
            productId
        );

        Class.forName("org.sqlite.JDBC");
        String sql =
            "INSERT INTO product_ProductImage "
                + "(imageURL, altText, displayOrder, uploadDate, productID) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
            Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:database/loop.db"
            );
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, resourcePath);
            statement.setString(2, productName);
            statement.setInt(3, displayOrder);
            statement.setString(
                4,
                LocalDate.now().format(DATABASE_DATE)
            );
            statement.setInt(5, productId);
            statement.executeUpdate();
        }

        return resourcePath;
    }

    public static String optimizeProductImage(
            File source,
            String productName,
            int productId) throws IOException {

        if (source == null || !source.isFile()) {
            throw new IOException("Product image does not exist.");
        }

        BufferedImage original = ImageIO.read(source);
        if (original == null) {
            throw new IOException(
                "Unsupported image format. Import PNG, JPG, or JPEG. "
                    + "Do not rename AVIF or WebP files to .png."
            );
        }

        Files.createDirectories(IMAGE_DIRECTORY);

        String fileName =
            productId + "-" + slugify(productName) + ".png";
        Path target = IMAGE_DIRECTORY.resolve(fileName);
        Path temporary = IMAGE_DIRECTORY.resolve(fileName + ".tmp");

        BufferedImage square = centerCrop(original);
        BufferedImage optimized = sharpen(
            resize(square, OUTPUT_SIZE, OUTPUT_SIZE)
        );

        if (!ImageIO.write(optimized, "png", temporary.toFile())) {
            throw new IOException("PNG writer is unavailable.");
        }

        try {
            Files.move(
                temporary,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                temporary,
                target,
                StandardCopyOption.REPLACE_EXISTING
            );
        }

        return RESOURCE_PREFIX + fileName;
    }

    private static BufferedImage centerCrop(BufferedImage image) {
        int size = Math.min(image.getWidth(), image.getHeight());
        int x = (image.getWidth() - size) / 2;
        int y = (image.getHeight() - size) / 2;
        return image.getSubimage(x, y, size, size);
    }

    private static BufferedImage resize(
            BufferedImage source,
            int width,
            int height) {

        BufferedImage result = new BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        graphics.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        );
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return result;
    }

    private static BufferedImage sharpen(BufferedImage source) {
        float[] matrix = {
             0.0f, -0.08f,  0.0f,
            -0.08f,  1.32f, -0.08f,
             0.0f, -0.08f,  0.0f
        };
        ConvolveOp operation = new ConvolveOp(
            new Kernel(3, 3, matrix),
            ConvolveOp.EDGE_NO_OP,
            null
        );
        return operation.filter(source, null);
    }

    private static String slugify(String value) {
        String slug = value == null
            ? "product"
            : value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "product" : slug;
    }
}
