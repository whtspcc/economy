package ui;

import model.DataSet;
import service.LoadResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class EconomyMainFrame extends JFrame {
    private final JComboBox<Integer> yearBox = new JComboBox<>();
    private final JList<Integer> compareYearsList = new JList<>();
    private final JList<String> productList = new JList<>();
    private final JTextArea resultArea = new JTextArea();
    private final TrendChartPanel trendChartPanel = new TrendChartPanel();
    private final JLabel sourceLabel = new JLabel();

    private final DataSet dataSet;

    public EconomyMainFrame(LoadResult loadResult) {
        this.dataSet = loadResult.getDataSet();

        setTitle("Покупательная способность");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 560));
        setSize(1000, 640);
        setLocationRelativeTo(null);

        initUi();
        fillUiFromData();

        String sourceText = "<html><span style='color:#6B6964;'>Источник: <b style='color:#1A1816;'>"
                + escapeHtml(loadResult.getSourceName())
                + "</b> · кг/мес · яйца — шт./мес</span></html>";
        if (loadResult.getWarning() != null && !loadResult.getWarning().isBlank()) {
            sourceText = "<html><span style='color:#6B6964;'>Источник: <b style='color:#1A1816;'>"
                    + escapeHtml(loadResult.getSourceName())
                    + "</b> · есть предупреждения</span></html>";
            resultArea.append("\n\nПредупреждения при загрузке:\n" + loadResult.getWarning());
        }
        sourceLabel.setText(sourceText);
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void initUi() {
        Font base = UiTheme.baseFont();
        UIManager.put("Label.font", base);
        UIManager.put("ComboBox.font", base);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UiTheme.BG);
        root.setBorder(new EmptyBorder(20, 24, 16, 24));
        setContentPane(root);

        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);

        productList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        productList.setFont(UiTheme.baseFont());
        productList.setBackground(UiTheme.SURFACE);
        productList.setSelectionBackground(UiTheme.ACCENT_SOFT);
        productList.setSelectionForeground(UiTheme.TEXT);
        productList.setFixedCellHeight(32);
        productList.setBorder(new EmptyBorder(4, 0, 4, 0));
        productList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(4, 12, 4, 12));
                l.setFont(UiTheme.baseFont());
                return l;
            }
        });

        JScrollPane leftScroll = new JScrollPane(productList);
        leftScroll.setBorder(UiTheme.cardBorder());
        leftScroll.getViewport().setBackground(UiTheme.SURFACE);
        leftScroll.setBackground(UiTheme.SURFACE);

        compareYearsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        compareYearsList.setFont(UiTheme.baseFont());
        compareYearsList.setBackground(UiTheme.SURFACE);
        compareYearsList.setSelectionBackground(UiTheme.ACCENT_SOFT);
        compareYearsList.setSelectionForeground(UiTheme.TEXT);
        compareYearsList.setFixedCellHeight(28);
        compareYearsList.setVisibleRowCount(6);
        compareYearsList.setBorder(new EmptyBorder(4, 0, 4, 0));
        compareYearsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(3, 12, 3, 12));
                l.setFont(UiTheme.baseFont());
                return l;
            }
        });

        JScrollPane yearsScroll = new JScrollPane(compareYearsList);
        yearsScroll.setBorder(UiTheme.cardBorder());
        yearsScroll.getViewport().setBackground(UiTheme.SURFACE);
        yearsScroll.setBackground(UiTheme.SURFACE);
        yearsScroll.setPreferredSize(new Dimension(180, 170));

        JLabel yearsTitle = new JLabel("Годы для сравнения");
        yearsTitle.setFont(UiTheme.baseFont().deriveFont(Font.BOLD));
        yearsTitle.setForeground(UiTheme.TEXT);
        yearsTitle.setBorder(new EmptyBorder(12, 0, 8, 0));

        JPanel yearsCard = new JPanel(new BorderLayout(0, 0));
        yearsCard.setOpaque(false);
        yearsCard.add(yearsTitle, BorderLayout.NORTH);
        yearsCard.add(yearsScroll, BorderLayout.CENTER);

        JLabel leftTitle = new JLabel("Продукты");
        leftTitle.setFont(UiTheme.baseFont().deriveFont(Font.BOLD));
        leftTitle.setForeground(UiTheme.TEXT);
        leftTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel leftCard = new JPanel(new BorderLayout(0, 0));
        leftCard.setOpaque(false);
        leftCard.add(leftTitle, BorderLayout.NORTH);
        leftCard.add(leftScroll, BorderLayout.CENTER);
        leftCard.add(yearsCard, BorderLayout.SOUTH);

        resultArea.setEditable(false);
        resultArea.setFont(UiTheme.monoFont());
        resultArea.setBackground(new Color(0xFA, 0xFA, 0xF8));
        resultArea.setForeground(UiTheme.TEXT);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBorder(new EmptyBorder(8, 12, 8, 12));

        JScrollPane rightScroll = new JScrollPane(resultArea);
        rightScroll.setBorder(UiTheme.cardBorder());
        rightScroll.getViewport().setBackground(new Color(0xFA, 0xFA, 0xF8));

        JLabel rightTitle = new JLabel("Результат");
        rightTitle.setFont(UiTheme.baseFont().deriveFont(Font.BOLD));
        rightTitle.setForeground(UiTheme.TEXT);
        rightTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel rightCard = new JPanel(new BorderLayout(0, 0));
        rightCard.setOpaque(false);
        rightCard.add(rightTitle, BorderLayout.NORTH);
        rightCard.add(rightScroll, BorderLayout.CENTER);
        trendChartPanel.setBorder(UiTheme.cardBorder());
        trendChartPanel.setBackground(UiTheme.SURFACE);
        trendChartPanel.setPreferredSize(new Dimension(420, 260));
        rightCard.add(trendChartPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, rightCard);
        split.setResizeWeight(0.38);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(20);
        split.setContinuousLayout(true);
        split.setDividerLocation(0.38);

        center.add(split, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER),
                new EmptyBorder(12, 0, 0, 0)));
        sourceLabel.setFont(UiTheme.captionFont());
        footer.add(sourceLabel, BorderLayout.WEST);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout(16, 0));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(4, 1));
        accentBar.setBackground(UiTheme.ACCENT_LINE);
        accentBar.setOpaque(true);

        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Покупательная способность");
        title.setFont(UiTheme.titleFont());
        title.setForeground(UiTheme.TEXT);
        JLabel sub = new JLabel("Среднедушевой доход · сколько кг (или шт. яиц) можно купить в месяц");
        sub.setFont(UiTheme.captionFont());
        sub.setForeground(UiTheme.MUTED);
        titles.add(title, BorderLayout.NORTH);
        titles.add(sub, BorderLayout.CENTER);

        JPanel leftHead = new JPanel(new BorderLayout(12, 0));
        leftHead.setOpaque(false);
        leftHead.add(accentBar, BorderLayout.WEST);
        leftHead.add(titles, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        JLabel yearLbl = new JLabel("Год");
        yearLbl.setForeground(UiTheme.MUTED);
        yearLbl.setFont(UiTheme.baseFont());
        yearBox.setPrototypeDisplayValue(9999);
        UiTheme.styleCombo(yearBox);

        JButton calc = new JButton("Рассчитать");
        UiTheme.stylePrimaryButton(calc);
        calc.addActionListener(e -> calculatePurchasingPower());

        JButton compare = new JButton("Сравнить");
        UiTheme.styleGhostButton(compare);
        compare.addActionListener(e -> compareByYears());

        JButton all = new JButton("Все");
        UiTheme.styleGhostButton(all);
        all.addActionListener(e -> {
            if (productList.getModel().getSize() > 0) {
                productList.setSelectionInterval(0, productList.getModel().getSize() - 1);
            }
        });

        JButton none = new JButton("Снять");
        UiTheme.styleGhostButton(none);
        none.addActionListener(e -> productList.clearSelection());

        toolbar.add(yearLbl);
        toolbar.add(yearBox);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(calc);
        toolbar.add(compare);
        toolbar.add(all);
        toolbar.add(none);

        wrap.add(leftHead, BorderLayout.CENTER);
        wrap.add(toolbar, BorderLayout.EAST);
        return wrap;
    }

    private void fillUiFromData() {
        DefaultComboBoxModel<Integer> yearModel = new DefaultComboBoxModel<>();
        DefaultListModel<Integer> compareYearModel = new DefaultListModel<>();
        for (Integer year : dataSet.getYears()) {
            yearModel.addElement(year);
            compareYearModel.addElement(year);
        }
        yearBox.setModel(yearModel);
        if (yearModel.getSize() > 0) {
            yearBox.setSelectedIndex(yearModel.getSize() - 1);
        }
        compareYearsList.setModel(compareYearModel);
        if (compareYearModel.getSize() > 0) {
            compareYearsList.setSelectionInterval(Math.max(0, compareYearModel.getSize() - 5), compareYearModel.getSize() - 1);
        }

        DefaultListModel<String> productsModel = new DefaultListModel<>();
        for (String name : dataSet.getProductData().keySet()) {
            productsModel.addElement(name);
        }
        productList.setModel(productsModel);

        if (productsModel.getSize() > 0) {
            productList.setSelectionInterval(0, Math.min(4, productsModel.getSize() - 1));
            resultArea.setText(
                    "Выберите год и товары слева, затем нажмите «Рассчитать».\n\n"
                            + "Интерпретация\n"
                            + "— значение: кг продукта на среднедушевой доход за месяц.\n"
                            + "— «Яйца куриные»: штук в месяц.\n"
                            + "— для графика выберите годы и нажмите «Сравнить».\n"
            );
        } else {
            resultArea.setText("Данные не загружены. Проверьте источник.");
        }
    }

    private void calculatePurchasingPower() {
        Integer year = (Integer) yearBox.getSelectedItem();
        List<String> selectedProducts = productList.getSelectedValuesList();

        if (year == null) {
            resultArea.setText("Сначала загрузите данные и выберите год.");
            return;
        }
        if (selectedProducts.isEmpty()) {
            resultArea.setText("Выберите хотя бы один продукт.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Год ").append(year).append('\n');
        sb.append("Выбрано позиций: ").append(selectedProducts.size()).append("\n\n");
        sb.append(String.format("%-42s %12s%n", "Товар", "кг/мес"));
        sb.append("────────────────────────────────────────────────────────────\n");

        double sum = 0.0;
        int count = 0;
        String maxProduct = "";
        String minProduct = "";
        double maxVal = Double.NEGATIVE_INFINITY;
        double minVal = Double.POSITIVE_INFINITY;

        for (String product : selectedProducts) {
            Double value = dataSet.getProductData().getOrDefault(product, Map.of()).get(year);
            if (value == null) {
                sb.append(String.format("%-42s %12s%n", trimProduct(product), "—"));
                continue;
            }
            sb.append(String.format(Locale.US, "%-42s %12.2f%n", trimProduct(product), value));
            sum += value;
            count++;
            if (value > maxVal) {
                maxVal = value;
                maxProduct = product;
            }
            if (value < minVal) {
                minVal = value;
                minProduct = product;
            }
        }

        sb.append('\n');
        if (count == 0) {
            sb.append("Нет данных за ").append(year).append('.');
        } else {
            double avg = sum / count;
            sb.append(String.format(Locale.US, "Среднее по выбору: %.2f%n", avg));
            sb.append(String.format(Locale.US, "Сумма (индекс корзины): %.2f%n", sum));
            sb.append(String.format(Locale.US, "Максимум: %.2f — %s%n", maxVal, maxProduct));
            sb.append(String.format(Locale.US, "Минимум: %.2f — %s%n", minVal, minProduct));
        }

        resultArea.setText(sb.toString());
    }

    private void compareByYears() {
        List<String> selectedProducts = productList.getSelectedValuesList();
        List<Integer> selectedYears = compareYearsList.getSelectedValuesList();

        if (selectedProducts.isEmpty()) {
            resultArea.setText("Для сравнения выберите хотя бы один продукт.");
            trendChartPanel.clearData("Нет данных для графика");
            return;
        }
        if (selectedYears.isEmpty()) {
            resultArea.setText("Для сравнения выберите хотя бы один год.");
            trendChartPanel.clearData("Нет данных для графика");
            return;
        }

        List<Integer> years = new ArrayList<>(selectedYears);
        years.sort(Integer::compareTo);

        Map<String, Map<Integer, Double>> series = new LinkedHashMap<>();
        int points = 0;
        for (String product : selectedProducts) {
            Map<Integer, Double> valuesByYear = dataSet.getProductData().getOrDefault(product, Map.of());
            Map<Integer, Double> productSeries = new LinkedHashMap<>();
            for (Integer year : years) {
                Double value = valuesByYear.get(year);
                if (value != null) {
                    productSeries.put(year, value);
                    points++;
                }
            }
            if (!productSeries.isEmpty()) {
                series.put(product, productSeries);
            }
        }

        if (series.isEmpty()) {
            resultArea.setText("По выбранным продуктам и годам данных не найдено.");
            trendChartPanel.clearData("Нет данных для графика");
            return;
        }

        trendChartPanel.setData(years, series);
        resultArea.setText("Сравнение по годам построено.\n"
                + "Выбрано продуктов: " + selectedProducts.size() + "\n"
                + "Выбрано лет: " + years.size() + "\n"
                + "Построено точек: " + points + "\n\n"
                + "Ломаная на графике показывает, как менялось значение по годам.");
    }

    private String trimProduct(String product) {
        return product.length() <= 40 ? product : product.substring(0, 37) + "...";
    }

    private static class TrendChartPanel extends JPanel {
        private static final Color[] SERIES_COLORS = new Color[] {
                new Color(0x2F, 0x6F, 0xD0),
                new Color(0xD1, 0x75, 0x1B),
                new Color(0x2E, 0x8B, 0x57),
                new Color(0xB0, 0x31, 0x60),
                new Color(0x6C, 0x4C, 0x9C),
                new Color(0x3C, 0x7D, 0x80)
        };

        private List<Integer> years = List.of();
        private Map<String, Map<Integer, Double>> series = Map.of();
        private String infoText = "Нажмите «Сравнить», чтобы построить график";

        private void setData(List<Integer> years, Map<String, Map<Integer, Double>> series) {
            this.years = new ArrayList<>(years);
            this.series = new LinkedHashMap<>(series);
            this.infoText = "";
            repaint();
        }

        private void clearData(String text) {
            this.years = List.of();
            this.series = Map.of();
            this.infoText = text;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 56;
            int right = 18;
            int top = 18;
            int bottom = 40;
            int plotW = Math.max(1, w - left - right);
            int plotH = Math.max(1, h - top - bottom);

            g2.setColor(new Color(0xF8, 0xF8, 0xF6));
            g2.fillRect(left, top, plotW, plotH);
            g2.setColor(UiTheme.BORDER);
            g2.drawRect(left, top, plotW, plotH);

            if (series.isEmpty() || years.isEmpty()) {
                g2.setColor(UiTheme.MUTED);
                g2.setFont(UiTheme.baseFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = Math.max(10, (w - fm.stringWidth(infoText)) / 2);
                int y = h / 2;
                g2.drawString(infoText, x, y);
                g2.dispose();
                return;
            }

            double minVal = Double.POSITIVE_INFINITY;
            double maxVal = Double.NEGATIVE_INFINITY;
            for (Map<Integer, Double> productSeries : series.values()) {
                for (Double value : productSeries.values()) {
                    minVal = Math.min(minVal, value);
                    maxVal = Math.max(maxVal, value);
                }
            }
            if (Double.compare(minVal, maxVal) == 0) {
                minVal -= 1.0;
                maxVal += 1.0;
            }

            g2.setFont(UiTheme.captionFont());
            g2.setColor(UiTheme.MUTED);
            for (int i = 0; i <= 4; i++) {
                int y = top + (int) Math.round((plotH * i) / 4.0);
                g2.setColor(new Color(0xE9, 0xE8, 0xE4));
                g2.drawLine(left, y, left + plotW, y);
                double v = maxVal - (maxVal - minVal) * i / 4.0;
                g2.setColor(UiTheme.MUTED);
                g2.drawString(String.format(Locale.US, "%.1f", v), 8, y + 4);
            }

            int yearCount = years.size();
            for (int i = 0; i < yearCount; i++) {
                int x = xAt(left, plotW, i, yearCount);
                g2.setColor(new Color(0xE9, 0xE8, 0xE4));
                g2.drawLine(x, top, x, top + plotH);
                g2.setColor(UiTheme.MUTED);
                String yearLabel = String.valueOf(years.get(i));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(yearLabel, x - fm.stringWidth(yearLabel) / 2, h - 14);
            }

            int colorIndex = 0;
            int legendX = left + 8;
            int legendY = top + 14;
            for (Map.Entry<String, Map<Integer, Double>> entry : series.entrySet()) {
                Color color = SERIES_COLORS[colorIndex % SERIES_COLORS.length];
                Map<Integer, Double> productSeries = entry.getValue();
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f));

                Integer prevYear = null;
                Double prevVal = null;
                for (Integer year : years) {
                    Double currentVal = productSeries.get(year);
                    if (currentVal == null) {
                        prevYear = null;
                        prevVal = null;
                        continue;
                    }
                    int x = xAt(left, plotW, years.indexOf(year), yearCount);
                    int y = yAt(top, plotH, currentVal, minVal, maxVal);
                    if (prevYear != null && prevVal != null) {
                        int px = xAt(left, plotW, years.indexOf(prevYear), yearCount);
                        int py = yAt(top, plotH, prevVal, minVal, maxVal);
                        g2.drawLine(px, py, x, y);
                    }
                    Shape marker = new Ellipse2D.Double(x - 3, y - 3, 6, 6);
                    g2.fill(marker);
                    prevYear = year;
                    prevVal = currentVal;
                }

                g2.fillRect(legendX, legendY - 8, 10, 10);
                g2.setColor(UiTheme.TEXT);
                g2.setFont(UiTheme.captionFont());
                g2.drawString(trimLegend(entry.getKey()), legendX + 14, legendY);
                legendY += 16;
                colorIndex++;
            }

            g2.dispose();
        }

        private int xAt(int left, int width, int index, int count) {
            if (count <= 1) {
                return left + width / 2;
            }
            return left + (int) Math.round(index * (width / (double) (count - 1)));
        }

        private int yAt(int top, int height, double value, double minVal, double maxVal) {
            double ratio = (value - minVal) / (maxVal - minVal);
            return top + height - (int) Math.round(ratio * height);
        }

        private String trimLegend(String product) {
            return product.length() <= 28 ? product : product.substring(0, 25) + "...";
        }
    }
}
