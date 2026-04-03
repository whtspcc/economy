package app;

import loader.DataLoader;
import loader.RosstatHtmlDataLoader;
import loader.TextDataLoader;
import loader.XlsxDataLoader;
import service.DataLoadService;
import service.LoadResult;
import ui.EconomyMainFrame;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class EconomyRosstatApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fallback to default look and feel.
            }

            List<DataLoader> loaders = Arrays.asList(
                    new RosstatHtmlDataLoader("https://rosstat.gov.ru/free_doc/new_site/population/urov/murov15.htm"),
                    new XlsxDataLoader("prices.xlsx"),
                    new TextDataLoader("prices.txt")
            );

            DataLoadService loadService = new DataLoadService(loaders);
            LoadResult result = loadService.load();

            EconomyMainFrame frame = new EconomyMainFrame(result);
            frame.setVisible(true);
        });
    }
}
