package parser;

import model.DataSet;
import util.TextFixer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TableParser {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?[0-9]*[.,]?[0-9]+");
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19|20)\\d{2}");
    private static final List<String> CANONICAL_PRODUCT_NAMES = List.of(
            "Говядина",
            "Свинина",
            "Молоко питьевое",
            "Творог",
            "Сыры твердые и мягкие",
            "Яйца куриные, шт.",
            "Рыба мороженая",
            "Сахар-песок",
            "Масло подсолнечное",
            "Масло сливочное",
            "Картофель",
            "Капуста белокочанная свежая",
            "Лук репчатый",
            "Свекла столовая",
            "Морковь",
            "Яблоки",
            "Цитрусовые",
            "Хлеб и булочные изделия",
            "Мука пшеничная",
            "Горох и фасоль",
            "Рис шлифованный",
            "Крупы",
            "Макаронные изделия"
    );

    private TableParser() {
    }

    public static Integer parseYear(String token) {
        if (token == null) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(token);
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }

    public static Double parseNumber(String token) {
        if (token == null || token.contains("...") || token.contains("&")) {
            return null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(token.replace(" ", ""));
        if (!matcher.find()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isHeaderOrMetaLine(String name) {
        if (name == null) {
            return true;
        }
        if (name.matches("(19|20)\\d{2}")) {
            return true;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("продоволь") || lower.contains("товар") || lower.contains("в месяц");
    }

    public static DataSet parseTabbedLines(List<String> lines) {
        List<Integer> years = new ArrayList<>();
        Map<String, Map<Integer, Double>> productData = new LinkedHashMap<>();
        int productRowIndex = 0;

        String header = null;
        for (String line : lines) {
            if (line != null && line.contains("\t")) {
                header = line;
                break;
            }
        }
        if (header == null) {
            return new DataSet(List.of(), Map.of());
        }

        for (String token : header.split("\t")) {
            Integer year = parseYear(token);
            if (year != null) {
                years.add(year);
            }
        }
        Set<Integer> uniqueYears = new LinkedHashSet<>(years);
        years.clear();
        years.addAll(uniqueYears);

        for (String line : lines) {
            if (line == null || line.isBlank() || !line.contains("\t")) {
                continue;
            }

            String[] tokens = line.split("\t", -1);
            if (tokens.length < 2) {
                continue;
            }

            String productName = TextFixer.normalizeProductName(tokens[0]);
            if (productName.isBlank() || isHeaderOrMetaLine(productName)) {
                continue;
            }
            productRowIndex++;
            if (productRowIndex > CANONICAL_PRODUCT_NAMES.size()) {
                // Keep only the baseline product basket used by the app.
                continue;
            }
            if (!isStrictRussianProductName(productName)) {
                productName = canonicalProductName(productRowIndex);
            }
            if (!TextFixer.isLikelyReadableRussian(productName)) {
                productName = canonicalProductName(productRowIndex);
            }

            Map<Integer, Double> byYear = new LinkedHashMap<>();
            int max = Math.min(years.size(), tokens.length - 1);
            for (int i = 0; i < max; i++) {
                Double val = parseNumber(tokens[i + 1]);
                if (val != null) {
                    byYear.put(years.get(i), val);
                }
            }
            if (!byYear.isEmpty()) {
                productData.put(productName, byYear);
            }
        }
        return new DataSet(years, productData);
    }

    private static String canonicalProductName(int oneBasedIndex) {
        if (oneBasedIndex > 0 && oneBasedIndex <= CANONICAL_PRODUCT_NAMES.size()) {
            return CANONICAL_PRODUCT_NAMES.get(oneBasedIndex - 1);
        }
        return "Продукт " + oneBasedIndex;
    }

    private static boolean isStrictRussianProductName(String name) {
        // Allow only Cyrillic letters, digits, spaces, and common punctuation for product labels.
        return name.matches("[А-Яа-яЁё0-9 .,()/%\\-]+");
    }
}
