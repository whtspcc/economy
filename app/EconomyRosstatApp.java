package app;

import loader.DataLoader;
import loader.RosstatHtmlDataLoader;
import loader.TextDataLoader;
import loader.XlsxDataLoader;
import service.DataLoadService;
import service.LoadResult;
import ui.EconomyMainFrame;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class EconomyRosstatApp {
    private static final String DOWNLOADS_XLSX_PRIMARY = Path.of(
            System.getProperty("user.home"),
            "Downloads",
            "sred_potreb_cen_1991-2025_1.xlsx").toString();
    private static final String DOWNLOADS_XLSX_FALLBACK = Path.of(
            System.getProperty("user.home"),
            "Downloads",
            "sred_potreb_cen_1991-2025.xlsx").toString();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fallback to default look and feel.
            }

            List<DataLoader> loaders = Arrays.asList(
                    // Prefer local files first so we can load 2025 values from user's spreadsheet.
                    new XlsxDataLoader(DOWNLOADS_XLSX_PRIMARY),
                    new XlsxDataLoader(DOWNLOADS_XLSX_FALLBACK),
                    new XlsxDataLoader("prices.xlsx"),
                    new TextDataLoader("prices.txt"),
                    new RosstatHtmlDataLoader("https://rosstat.gov.ru/free_doc/new_site/population/urov/murov15.htm"),
                    new XlsxDataLoader("C:\\Users\\ADMIN\\Downloads\\sred_potreb_cen_1991-2025.xlsx")
            );

            DataLoadService loadService = new DataLoadService(loaders);
            LoadResult result = loadService.load();

            EconomyMainFrame frame = new EconomyMainFrame(result);
            frame.setVisible(true);
        });
    }
}
