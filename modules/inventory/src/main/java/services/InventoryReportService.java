package services;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.Ingredient;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Creates an Inventory PDF independently of the JavaFX window. */
public final class InventoryReportService {

    private InventoryReportService() { }

    public static Path generate(Path requestedFile, String year, List<Ingredient> ingredients)
            throws Exception {
        if (requestedFile == null) {
            throw new IllegalArgumentException("Choose where to save the report.");
        }
        if (year == null || year.isBlank()) {
            throw new IllegalArgumentException("Choose a report year.");
        }

        Path reportFile = ensurePdfExtension(requestedFile.toAbsolutePath().normalize());
        Path parent = reportFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Document document = new Document(PageSize.A4);
        try (OutputStream output = Files.newOutputStream(reportFile)) {
            try {
                PdfWriter.getInstance(document, output);
                document.open();

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                document.add(new Paragraph("LOOP Inventory Report - " + year, titleFont));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addHeader(table, "Stock code");
                addHeader(table, "Ingredient");
                addHeader(table, "Quantity");
                addHeader(table, "Warehouse");
                addHeader(table, "Capacity");

                for (Ingredient ingredient : ingredients) {
                    table.addCell(ingredient.getIngredientID());
                    table.addCell(ingredient.getIngredientName());
                    table.addCell(String.valueOf(ingredient.getStockQuantity()));
                    table.addCell(ingredient.getWarehouseID());
                    table.addCell(String.valueOf(ingredient.getCapacity()));
                }
                document.add(table);
            } finally {
                if (document.isOpen()) {
                    document.close();
                }
            }
        }
        return reportFile;
    }

    private static void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static Path ensurePdfExtension(Path path) {
        String filename = path.getFileName().toString();
        return filename.toLowerCase().endsWith(".pdf")
                ? path
                : path.resolveSibling(filename + ".pdf");
    }
}
