package loader;

import model.DataSet;
import parser.TableParser;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TextDataLoader implements DataLoader {
    private final Path path;
    private static final List<Charset> CHARSETS = Arrays.asList(
            StandardCharsets.UTF_8,
            Charset.forName("windows-1251"),
            Charset.forName("CP866"),
            Charset.forName("KOI8-R")
    );

    public TextDataLoader(String filePath) {
        this.path = Path.of(filePath);
    }

    @Override
    public String sourceName() {
        return "prices.txt";
    }

    @Override
    public DataSet load() throws Exception {
        if (!Files.exists(path)) {
            throw new IllegalStateException("файл не найден: " + path.toAbsolutePath());
        }

        List<String> lines = null;
        for (Charset charset : CHARSETS) {
            try {
                lines = Files.readAllLines(path, charset);
                if (lines != null && !lines.isEmpty()) {
                    break;
                }
            } catch (Exception ignored) {
                // Try next encoding.
            }
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalStateException("не удалось прочитать файл");
        }
        return TableParser.parseTabbedLines(lines);
    }
}
