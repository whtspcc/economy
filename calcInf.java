import loader.XlsxDataLoader;
import model.DataSet;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class calcInf extends JFrame {
    
    private static final Map<String, Map<Integer, Double>> productData = new TreeMap<>(); // TreeMap для сортировки названий
    private static final List<Integer> availableYears = new ArrayList<>();
    
    private JComboBox<Integer> yearFromBox, yearToBox;
    private JPanel productPanel;
    private Map<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
    private JLabel resultLabel;

    public calcInf() {
        // 1. Сначала ГРУЗИМ данные
        loadData();

        // 2. Только ПОТОМ настраиваем интерфейс
        setTitle("Анализ цен по продуктовой корзине");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        setLocationRelativeTo(null);

        // Панель выбора лет
        JPanel topPanel = new JPanel();
        if (availableYears.isEmpty()) {
            topPanel.add(new JLabel("ОШИБКА: Данные не загружены. Проверьте xlsx/csv файл"));
        } else {
            yearFromBox = new JComboBox<>(availableYears.toArray(new Integer[0]));
            yearToBox = new JComboBox<>(availableYears.toArray(new Integer[0]));
            yearToBox.setSelectedIndex(availableYears.size() - 1);

            topPanel.add(new JLabel("С года:"));
            topPanel.add(yearFromBox);
            topPanel.add(new JLabel("По год:"));
            topPanel.add(yearToBox);
        }

        // Панель продуктов
        productPanel = new JPanel();
        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
        
        // Создаем чекбоксы только если данные есть
        for (String prodName : productData.keySet()) {
            JCheckBox cb = new JCheckBox(prodName);
            checkBoxes.put(prodName, cb);
            productPanel.add(cb);
        }

        JScrollPane scrollPane = new JScrollPane(productPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Выберите товары из таблицы (загружено: " + productData.size() + ")"));

        // Нижняя панель
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton calcBtn = new JButton("Рассчитать покупательную способность");
        resultLabel = new JLabel("<html><center>Выберите товары и нажмите расчет</center></html>", SwingConstants.CENTER);

        calcBtn.addActionListener(e -> calculate());
        bottomPanel.add(calcBtn);
        bottomPanel.add(resultLabel);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        productData.clear();
        availableYears.clear();

        List<String> xlsxCandidates = List.of(
                Path.of(System.getProperty("user.home"), "Downloads", "sred_potreb_cen_1991-2025_1.xlsx").toString(),
                Path.of(System.getProperty("user.home"), "Downloads", "sred_potreb_cen_1991-2025.xlsx").toString(),
                "prices.xlsx"
        );

        for (String xlsxPath : xlsxCandidates) {
            if (loadFromXlsx(xlsxPath)) {
                return;
            }
        }

        loadFromHorizontalCSV("prices.csv");
    }

    private boolean loadFromXlsx(String filePath) {
        try {
            DataSet dataSet = new XlsxDataLoader(filePath).load();
            if (dataSet == null || dataSet.isEmpty()) {
                return false;
            }
            availableYears.addAll(dataSet.getYears());
            for (Map.Entry<String, Map<Integer, Double>> entry : dataSet.getProductData().entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    productData.put(entry.getKey(), new HashMap<>(entry.getValue()));
                }
            }
            return !availableYears.isEmpty() && !productData.isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    private BufferedReader createReader(String fileName) throws IOException {
        try {
            return new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName), "UTF-8"));
        } catch (Exception e) {
            return new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName), "windows-1251"));
        }
    }
    
    private void loadFromHorizontalCSV(String fileName) {
    try (BufferedReader br = createReader(fileName)) {

        String headerLine = br.readLine();
        if (headerLine == null) return;

        // Убираем BOM
        if (headerLine.startsWith("\uFEFF")) {
            headerLine = headerLine.substring(1);
        }

        String delimiter = headerLine.contains(";") ? ";" : ",";
        String[] headers = headerLine.split(delimiter);

        availableYears.clear();
        for (int i = 1; i < headers.length; i++) {
            String yearStr = headers[i].trim().replaceAll("[^0-9]", "");
            if (!yearStr.isEmpty()) {
                availableYears.add(Integer.parseInt(yearStr));
            }
        }

        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(delimiter);
            if (values.length < 2) continue;

            String productName = values[0].trim().replace("\"", "");
            if (productName.isEmpty()) continue;

            Map<Integer, Double> yearsMap = new HashMap<>();
            for (int i = 1; i < values.length; i++) {
                if (i - 1 < availableYears.size()) {
                    try {
                        String val = values[i].trim().replace(",", ".");
                        if (!val.isEmpty()) {
                            yearsMap.put(
                                availableYears.get(i - 1),
                                Double.parseDouble(val)
                            );
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (!yearsMap.isEmpty()) {
                productData.put(productName, yearsMap);
            }
        }

        System.out.println("Успешно загружено! Продуктов: " + productData.size());

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Ошибка чтения: " + e.getMessage());
    }
}

    private void calculate() {
        if (yearFromBox == null || yearFromBox.getSelectedItem() == null) return;

        int yearFrom = (int) yearFromBox.getSelectedItem();
        int yearTo = (int) yearToBox.getSelectedItem();
        double totalFrom = 0, totalTo = 0;
        boolean selected = false;

        for (Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                Double p1 = productData.get(entry.getKey()).get(yearFrom);
                Double p2 = productData.get(entry.getKey()).get(yearTo);
                if (p1 != null && p2 != null) {
                    totalFrom += p1;
                    totalTo += p2;
                    selected = true;
                }
            }
        }

        if (!selected) {
            resultLabel.setText("Выберите продукты, доступные в оба года!");
            return;
        }

        double ratio = totalTo / totalFrom;
        resultLabel.setText(String.format(
            "<html><center>Корзина в %d г.: %.2f р.<br>Корзина в %d г.: %.2f р.<br>" +
            "<b>Цены изменились в %.2f раз</b></center></html>", 
            yearFrom, totalFrom, yearTo, totalTo, ratio
        ));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Устанавливаем стиль оформления как у системы
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            calcInf frame = new calcInf();
            frame.setVisible(true);
        });
    }
}