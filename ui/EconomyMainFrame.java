package ui;

import model.DataSet;
import service.LoadResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EconomyMainFrame extends JFrame {
    private final JComboBox<Integer> yearBox = new JComboBox<>();
    private final JList<String> productList = new JList<>();
    private final JTextArea resultArea = new JTextArea();
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

        JLabel leftTitle = new JLabel("Продукты");
        leftTitle.setFont(UiTheme.baseFont().deriveFont(Font.BOLD));
        leftTitle.setForeground(UiTheme.TEXT);
        leftTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel leftCard = new JPanel(new BorderLayout(0, 0));
        leftCard.setOpaque(false);
        leftCard.add(leftTitle, BorderLayout.NORTH);
        leftCard.add(leftScroll, BorderLayout.CENTER);

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
        toolbar.add(all);
        toolbar.add(none);

        wrap.add(leftHead, BorderLayout.CENTER);
        wrap.add(toolbar, BorderLayout.EAST);
        return wrap;
    }

    private void fillUiFromData() {
        DefaultComboBoxModel<Integer> yearModel = new DefaultComboBoxModel<>();
        for (Integer year : dataSet.getYears()) {
            yearModel.addElement(year);
        }
        yearBox.setModel(yearModel);
        if (yearModel.getSize() > 0) {
            yearBox.setSelectedIndex(yearModel.getSize() - 1);
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

    private String trimProduct(String product) {
        return product.length() <= 40 ? product : product.substring(0, 37) + "...";
    }
}
