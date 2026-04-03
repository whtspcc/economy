package loader;

import model.DataSet;
import parser.TableParser;
import util.TextFixer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RosstatHtmlDataLoader implements DataLoader {
    private final String url;

    public RosstatHtmlDataLoader(String url) {
        this.url = url;
    }

    @Override
    public String sourceName() {
        return "Росстат (сайт)";
    }

    @Override
    public DataSet load() throws Exception {
        try {
            byte[] bytes = download(url);
            String html = decodeHtml(bytes);
            DataSet dataSet = parseHtmlTable(html);
            if (dataSet.isEmpty()) {
                throw new IllegalStateException("на странице не найдена подходящая таблица");
            }
            return dataSet;
        } catch (javax.net.ssl.SSLHandshakeException e) {
            throw new IllegalStateException("TLS/сертификат на сайте недоступен для Java в этой системе");
        }
    }

    private byte[] download(String urlValue) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlValue).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8");

        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP статус " + code);
        }

        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private String decodeHtml(byte[] bytes) {
        String utf = new String(bytes, StandardCharsets.UTF_8);
        String win = new String(bytes, Charset.forName("windows-1251"));
        if (utf.toLowerCase(Locale.ROOT).contains("windows-1251")) {
            return win;
        }
        return TextFixer.scoreRussianReadability(win) > TextFixer.scoreRussianReadability(utf) ? win : utf;
    }

    private DataSet parseHtmlTable(String html) {
        Matcher tableMatcher = Pattern.compile("(?is)<table[^>]*>(.*?)</table>").matcher(html);
        while (tableMatcher.find()) {
            DataSet ds = parseSingleTable(tableMatcher.group(1));
            if (!ds.isEmpty()) {
                return ds;
            }
        }
        return new DataSet(List.of(), Map.of());
    }

    private DataSet parseSingleTable(String tableHtml) {
        List<Integer> years = new ArrayList<>();
        Map<String, Map<Integer, Double>> productData = new LinkedHashMap<>();
        boolean yearsDetected = false;

        Matcher rowMatcher = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>").matcher(tableHtml);
        while (rowMatcher.find()) {
            List<String> cells = extractCells(rowMatcher.group(1));
            if (cells.size() < 2) {
                continue;
            }

            if (!yearsDetected) {
                for (String cell : cells) {
                    Integer year = TableParser.parseYear(cell);
                    if (year != null) {
                        years.add(year);
                    }
                }
                if (!years.isEmpty()) {
                    Set<Integer> unique = new LinkedHashSet<>(years);
                    years.clear();
                    years.addAll(unique);
                    yearsDetected = true;
                }
                continue;
            }

            String productName = TextFixer.normalizeProductName(cells.get(0));
            if (productName.isBlank() || TableParser.isHeaderOrMetaLine(productName)) {
                continue;
            }

            Map<Integer, Double> byYear = new LinkedHashMap<>();
            int max = Math.min(years.size(), cells.size() - 1);
            for (int i = 0; i < max; i++) {
                Double value = TableParser.parseNumber(cells.get(i + 1));
                if (value != null) {
                    byYear.put(years.get(i), value);
                }
            }
            if (!byYear.isEmpty()) {
                productData.put(productName, byYear);
            }
        }
        return new DataSet(years, productData);
    }

    private List<String> extractCells(String rowHtml) {
        List<String> cells = new ArrayList<>();
        Matcher cellMatcher = Pattern.compile("(?is)<t[dh][^>]*>(.*?)</t[dh]>").matcher(rowHtml);
        while (cellMatcher.find()) {
            String cell = cellMatcher.group(1)
                    .replaceAll("(?is)<br\\s*/?>", " ")
                    .replaceAll("(?is)<[^>]+>", " ");
            cells.add(decodeHtmlEntities(cell).trim().replaceAll("\\s+", " "));
        }
        return cells;
    }

    private String decodeHtmlEntities(String text) {
        return text.replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }
}
