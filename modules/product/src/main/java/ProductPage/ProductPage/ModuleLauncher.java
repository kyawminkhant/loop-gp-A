package ProductPage.ProductPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Launches the independently built JavaFX modules from the shared Team Hub. */
public final class ModuleLauncher {

    private ModuleLauncher() { }

    public static void launch(String module, String startView) throws IOException {
        Path root = findRepositoryRoot();
        Path wrapper = root.resolve("modules/finance/mvnw.cmd");
        Path pom = root.resolve("modules").resolve(module).resolve("pom.xml");
        if (!Files.isRegularFile(wrapper)) {
            throw new IOException("Maven wrapper not found: " + wrapper);
        }
        if (!Files.isRegularFile(pom)) {
            throw new IOException("Module pom.xml not found: " + pom);
        }

        Path logs = root.resolve("logs");
        Files.createDirectories(logs);
        ProcessBuilder process = new ProcessBuilder(
                "cmd.exe", "/c", wrapper.toString(),
                "-f", pom.toString(),
                "-Dloop.start=" + startView,
                "-Dloop.db.path=" + root.resolve("database/loop.db").toAbsolutePath(),
                "javafx:run");
        process.directory(root.toFile());
        process.redirectOutput(ProcessBuilder.Redirect.appendTo(logs.resolve(module + ".log").toFile()));
        process.redirectError(ProcessBuilder.Redirect.appendTo(logs.resolve(module + ".log").toFile()));
        process.start();
    }

    private static Path findRepositoryRoot() throws IOException {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve("modules"))
                    && Files.isRegularFile(current.resolve("database/loop.db"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IOException("Run the Product hub from the LOOP-Group-Project repository root.");
    }
}
