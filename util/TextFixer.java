package util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TextFixer {
    private static final List<Charset> CHARSETS = Arrays.asList(
            StandardCharsets.UTF_8,
            Charset.forName("windows-1251"),
            Charset.forName("CP866"),
            Charset.forName("KOI8-R"),
            Charset.forName("ISO-8859-5")
    );

    private TextFixer() {
    }

    public static String normalizeProductName(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.replace("\"", "")
                .replace("\u00A0", " ")
                .trim();
        return recoverMojibake(cleaned);
    }

    public static String recoverMojibake(String source) {
        if (source == null || source.isBlank()) {
            return source == null ? "" : source;
        }

        List<String> candidates = new ArrayList<>();
        candidates.add(source);
        for (Charset from : CHARSETS) {
            for (Charset to : CHARSETS) {
                if (!from.equals(to)) {
                    String c1 = reencode(source, from, to);
                    candidates.add(c1);
                    for (Charset to2 : CHARSETS) {
                        if (!to.equals(to2)) {
                            candidates.add(reencode(c1, to, to2));
                        }
                    }
                }
            }
        }

        String best = source;
        int bestScore = scoreRussianReadability(source);
        for (String candidate : candidates) {
            int score = scoreRussianReadability(candidate);
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best.trim();
    }

    public static boolean isLikelyReadableRussian(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (containsBadGlyphs(text)) {
            return false;
        }
        int cyr = 0;
        int letters = 0;
        int unique = 0;
        boolean[] seen = new boolean[Character.MAX_VALUE + 1];
        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                letters++;
            }
            if ((ch >= 'а' && ch <= 'я') || (ch >= 'А' && ch <= 'Я') || ch == 'ё' || ch == 'Ё') {
                cyr++;
            }
            if (!seen[ch]) {
                seen[ch] = true;
                unique++;
            }
        }
        if (letters == 0) {
            return false;
        }
        if (containsMojibakeMarkers(text)) {
            return false;
        }
        if (hasTooManyNonRussianSymbols(text)) {
            return false;
        }
        if (looksLikeAaaMojibake(text, letters, unique)) {
            return false;
        }
        return cyr >= 2 && ((double) cyr / (double) letters) >= 0.35;
    }

    public static int scoreRussianReadability(String text) {
        if (text == null || text.isBlank()) {
            return Integer.MIN_VALUE;
        }
        int score = 0;
        for (char ch : text.toCharArray()) {
            if ((ch >= 'а' && ch <= 'я') || (ch >= 'А' && ch <= 'Я') || ch == 'ё' || ch == 'Ё') {
                score += 4;
            } else if (Character.isLetterOrDigit(ch)) {
                score += 1;
            } else if (Character.isWhitespace(ch) || ",.-()%/".indexOf(ch) >= 0) {
                score += 0;
            } else if (ch < 32 || ch == 65533) {
                score -= 6;
            } else if ("<>;:=`~^|".indexOf(ch) >= 0) {
                score -= 3;
            } else {
                score -= 1;
            }
        }
        if (containsMojibakeMarkers(text)) {
            score -= 120;
        }
        score += scoreRussianBigrams(text);
        return score;
    }

    private static boolean containsMojibakeMarkers(String text) {
        String lower = text.toLowerCase();
        return lower.contains("рџ") || lower.contains("рў") || lower.contains("рс")
                || lower.contains("яп") || lower.contains("пн") || lower.contains("нп")
                || lower.contains("ѓ") || lower.contains("ќ");
    }

    private static boolean containsBadGlyphs(String text) {
        return text.indexOf('\uFFFD') >= 0 || text.indexOf('□') >= 0 || text.indexOf('\u0000') >= 0;
    }

    private static boolean looksLikeAaaMojibake(String text, int letters, int uniqueChars) {
        int aCount = 0;
        for (char ch : text.toCharArray()) {
            if (ch == 'а' || ch == 'А') {
                aCount++;
            }
        }
        double aRatio = letters == 0 ? 0 : (double) aCount / (double) letters;
        return aRatio > 0.28 && uniqueChars < 18;
    }

    private static boolean hasTooManyNonRussianSymbols(String text) {
        int letters = 0;
        int nonExpected = 0;
        for (char ch : text.toCharArray()) {
            if (Character.isWhitespace(ch) || Character.isDigit(ch)) {
                continue;
            }
            if (",.-()%/\"'".indexOf(ch) >= 0) {
                continue;
            }
            if ((ch >= 'а' && ch <= 'я') || (ch >= 'А' && ch <= 'Я') || ch == 'ё' || ch == 'Ё') {
                letters++;
                continue;
            }
            // Latin symbols inside Russian product names are usually a mojibake indicator.
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                nonExpected += 2;
            } else {
                nonExpected += 3;
            }
        }
        // For short names keep a strict threshold.
        int threshold = Math.max(3, letters / 3);
        return nonExpected > threshold;
    }

    private static int scoreRussianBigrams(String text) {
        String lower = text.toLowerCase();
        int score = 0;
        String[] good = {"ст", "но", "ов", "ен", "ко", "пр", "мо", "го", "на", "то", "ни", "ия"};
        for (String g : good) {
            if (lower.contains(g)) {
                score += 3;
            }
        }
        String[] bad = {"яп", "пн", "нп", "япн", "пня", "њ", "ѓ"};
        for (String b : bad) {
            if (lower.contains(b)) {
                score -= 15;
            }
        }
        return score;
    }

    private static String reencode(String text, Charset from, Charset to) {
        try {
            return new String(text.getBytes(from), to);
        } catch (Exception e) {
            return text;
        }
    }
}
