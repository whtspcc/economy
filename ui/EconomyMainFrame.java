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

        setTitle("Экономика: Покупательная способность (Росстат)");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        initUi();
        fillUiFromData();

        String sourceText = "Источник данных: " + loadResult.getSourceName()
                + " | единицы: кг/месяц (яйца - шт./месяц)";
        if (loadResult.getWarning() != null && !loadResult.getWarning().isBlank()) {
            sourceText += " | предупреждения есть";
            resultArea.append("\n\nПредупреждения при загрузке:\n" + loadResult.getWarning());
        }
        sourceLabel.setText(sourceText);
    }

    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("Год:"));
        yearBox.setPrototypeDisplayValue(9999);
        topPanel.add(yearBox);

        JButton calculateButton = new JButton("Рассчитать");
        calculateButton.addActionListener(e -> calculatePurchasingPower());
        topPanel.add(calculateButton);

        JButton selectAllButton = new JButton("Выбрать все");
        selectAllButton.addActionListener(e -> {
            if (productList.getModel().getSize() > 0) {
                productList.setSelectionInterval(0, productList.getModel().getSize() - 1);
            }
        });
        topPanel.add(selectAllButton);

        JButton clearSelectionButton = new JButton("Снять выбор");
        clearSelectionButton.addActionListener(e -> productList.clearSelection());
        topPanel.add(clearSelectionButton);

        root.add(topPanel, BorderLayout.NORTH);

        productList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane leftScroll = new JScrollPane(productList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Продукты"));

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane rightScroll = new JScrollPane(resultArea);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Результат"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        splitPane.setResizeWeight(0.4);
        root.add(splitPane, BorderLayout.CENTER);

        root.add(sourceLabel, BorderLayout.SOUTH);
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
                    "Выберите год и товары слева, затем нажмите \"Рассчитать\".\n\n"
                            + "Интерпретация:\n"
                            + "- значение = сколько кг продукта можно купить на среднедушевой доход за месяц.\n"
                            + "- для позиции \"Яйца куриные\" единица измерения: штук в месяц.\n"
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
        sb.append("Год: ").append(year).append('\n');
        sb.append("Выбрано товаров: ").append(selectedProducts.size()).append("\n\n");
        sb.append(String.format("%-45s %12s%n", "Товар", "Значение"));
        sb.append("--------------------------------------------------------------\n");

        double sum = 0.0;
        int count = 0;
        String maxProduct = "";
        String minProduct = "";
        double maxVal = Double.NEGATIVE_INFINITY;
        double minVal = Double.POSITIVE_INFINITY;

        for (String product : selectedProducts) {
            Double value = dataSet.getProductData().getOrDefault(product, Map.of()).get(year);
            if (value == null) {
                sb.append(String.format("%-45s %12s%n", trimProduct(product), "нет данных"));
                continue;
            }
            sb.append(String.format(Locale.US, "%-45s %12.2f%n", trimProduct(product), value));
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
            sb.append("Для выбранных товаров нет данных за ").append(year).append('.');
        } else {
            double avg = sum / count;
            sb.append(String.format(Locale.US, "Среднее значение по выбранным товарам: %.2f%n", avg));
            sb.append(String.format(Locale.US, "Суммарный индекс корзины: %.2f%n", sum));
            sb.append(String.format(Locale.US, "Максимум: %.2f (%s)%n", maxVal, maxProduct));
            sb.append(String.format(Locale.US, "Минимум: %.2f (%s)%n", minVal, minProduct));
        }

        resultArea.setText(sb.toString());
    }

    private String trimProduct(String product) {
        return product.length() <= 43 ? product : product.substring(0, 40) + "...";
    }
}
