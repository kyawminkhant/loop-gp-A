package services;

import model.Ingredient;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryReportServiceTest {

    @Test
    void generatesReadablePdfReport() throws Exception {
        Path directory = Files.createTempDirectory("loop-inventory-report-");
        Path requested = directory.resolve("inventory-report");
        try {
            Path report = InventoryReportService.generate(
                    requested,
                    "2026",
                    List.of(new Ingredient("TEST-1", "Test Ingredient", 12, "WH-01", 50)));

            byte[] bytes = Files.readAllBytes(report);
            assertTrue(report.getFileName().toString().endsWith(".pdf"));
            assertTrue(bytes.length > 100);
            assertTrue(new String(bytes, 0, 4).equals("%PDF"));
        } finally {
            if (Files.exists(directory)) {
                try (var files = Files.list(directory)) {
                    files.forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                            // Best-effort test cleanup.
                        }
                    });
                }
                Files.deleteIfExists(directory);
            }
        }
    }
}
