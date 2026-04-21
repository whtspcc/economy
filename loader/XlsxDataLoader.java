package loader;

import model.DataSet;
import parser.TableParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class XlsxDataLoader implements DataLoader {
    private final Path path;

    public XlsxDataLoader(String filePath) {
        this.path = Path.of(filePath);
    }

    @Override
    public String sourceName() {
        return "prices.xlsx";
    }

    @Override
    public DataSet load() throws Exception {
        if (!Files.exists(path)) {
            throw new IllegalStateException("файл не найден: " + path.toAbsolutePath());
        }

        try (ZipFile zip = new ZipFile(path.toFile())) {
            String sharedStringsXml = readZipEntry(zip, "xl/sharedStrings.xml");
            List<String> sharedStrings = parseSharedStrings(sharedStringsXml);
            DataSet ds = parseBestSheet(zip, sharedStrings);
            if (ds.isEmpty()) {
                throw new IllegalStateException("в xlsx не найдено таблицы с годами/товарами");
            }
            return ds;
        }
    }

    private DataSet parseBestSheet(ZipFile zip, List<String> sharedStrings) throws Exception {
        List<? extends ZipEntry> entries = zip.stream()
                .filter(e -> !e.isDirectory())
                .filter(e -> e.getName().startsWith("xl/worksheets/sheet") && e.getName().endsWith(".xml"))
                .sorted(Comparator.comparing(ZipEntry::getName))
                .toList();

        DataSet best = new DataSet(List.of(), Map.of());
        int bestScore = -1;
        for (ZipEntry entry : entries) {
            String sheetXml = readZipEntry(zip, entry.getName());
            if (sheetXml == null || sheetXml.isBlank()) {
                continue;
            }
            List<List<String>> rows = parseSheetRows(sheetXml, sharedStrings);
            DataSet candidate = parseRowsAsTable(rows);
            int score = candidate.getYears().size() * Math.max(1, candidate.getProductData().size());
            if (!candidate.isEmpty() && score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private String readZipEntry(ZipFile zip, String entryName) throws Exception {
        ZipEntry entry = zip.getEntry(entryName);
        if (entry == null) {
            return null;
        }
        try (InputStream in = zip.getInputStream(entry);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private List<String> parseSharedStrings(String xml) {
        List<String> list = new ArrayList<>();
        if (xml == null || xml.isBlank()) {
            return list;
        }
        Matcher matcher = Pattern.compile("(?is)<si[^>]*>(.*?)</si>").matcher(xml);
        while (matcher.find()) {
            String si = matcher.group(1);
            Matcher tMatcher = Pattern.compile("(?is)<t[^>]*>(.*?)</t>").matcher(si);
            StringBuilder sb = new StringBuilder();
            while (tMatcher.find()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(tMatcher.group(1));
            }
            list.add(sb.toString().trim());
        }
        return list;
    }

    private List<List<String>> parseSheetRows(String sheetXml, List<String> sharedStrings) {
        List<List<String>> table = new ArrayList<>();
        Matcher rowMatcher = Pattern.compile("(?is)<row[^>]*>(.*?)</row>").matcher(sheetXml);
        while (rowMatcher.find()) {
            String rowBody = rowMatcher.group(1);
            Map<Integer, String> rowCells = new TreeMap<>();

            Matcher cellMatcher = Pattern.compile("(?is)<c([^>]*)>(.*?)</c>").matcher(rowBody);
            while (cellMatcher.find()) {
                String attrs = cellMatcher.group(1);
                String body = cellMatcher.group(2);
                Integer col = parseColIndex(attrs);
                if (col == null) {
                    continue;
                }

                String type = parseAttr(attrs, "t");
                String value = parseCellValue(body, type, sharedStrings);
                rowCells.put(col, value == null ? "" : value);
            }

            if (!rowCells.isEmpty()) {
                int maxCol = rowCells.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= maxCol; i++) {
                    row.add(rowCells.getOrDefault(i, ""));
                }
                table.add(row);
            }
        }
        return table;
    }

    private Integer parseColIndex(String attrs) {
        String ref = parseAttr(attrs, "r");
        if (ref == null || ref.isBlank()) {
            return null;
        }
        StringBuilder letters = new StringBuilder();
        for (char c : ref.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            } else {
                break;
            }
        }
        if (letters.isEmpty()) {
            return null;
        }
        int col = 0;
        for (char c : letters.toString().toCharArray()) {
            col = col * 26 + (c - 'A' + 1);
        }
        return col;
    }

    private String parseAttr(String attrs, String name) {
        Matcher matcher = Pattern.compile(name + "=\"([^\"]*)\"").matcher(attrs);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String parseCellValue(String body, String type, List<String> sharedStrings) {
        if ("s".equals(type)) {
            Matcher m = Pattern.compile("(?is)<v>(.*?)</v>").matcher(body);
            if (m.find()) {
                int idx = Integer.parseInt(m.group(1).trim());
                if (idx >= 0 && idx < sharedStrings.size()) {
                    return sharedStrings.get(idx);
                }
            }
            return "";
        }

        if ("inlineStr".equals(type)) {
            Matcher m = Pattern.compile("(?is)<t[^>]*>(.*?)</t>").matcher(body);
            return m.find() ? m.group(1) : "";
        }

        Matcher m = Pattern.compile("(?is)<v>(.*?)</v>").matcher(body);
        return m.find() ? m.group(1).trim() : "";
    }

    private DataSet parseRowsAsTable(List<List<String>> rows) {
        if (rows == null || rows.isEmpty()) {
            return new DataSet(List.of(), Map.of());
        }

        int headerIdx = -1;
        int bestYearCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            int count = 0;
            for (String cell : rows.get(i)) {
                if (TableParser.parseYear(cell) != null) {
                    count++;
                }
            }
            if (count > bestYearCount) {
                bestYearCount = count;
                headerIdx = i;
            }
        }

        if (headerIdx < 0 || bestYearCount < 3) {
            return new DataSet(List.of(), Map.of());
        }

        List<String> tabbedLines = new ArrayList<>();
        for (List<String> row : rows.subList(headerIdx, rows.size())) {
            tabbedLines.add(String.join("\t", row));
        }
        return TableParser.parseTabbedLines(tabbedLines);
    }
}
