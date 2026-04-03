package service;

import loader.DataLoader;
import model.DataSet;

import java.util.List;
import java.util.Map;

public class DataLoadService {
    private final List<DataLoader> loaders;

    public DataLoadService(List<DataLoader> loaders) {
        this.loaders = loaders;
    }

    public LoadResult load() {
        StringBuilder warnings = new StringBuilder();
        for (DataLoader loader : loaders) {
            try {
                DataSet dataSet = loader.load();
                if (dataSet != null && !dataSet.isEmpty()) {
                    // If a fallback source loaded successfully, do not distract user with previous warnings.
                    return new LoadResult(dataSet, loader.sourceName(), "");
                }
                warnings.append(loader.sourceName()).append(": пустой набор данных.\n");
            } catch (Exception e) {
                warnings.append(loader.sourceName()).append(": ").append(e.getMessage()).append('\n');
            }
        }
        return new LoadResult(new DataSet(List.of(), Map.of()), "нет данных", warnings.toString().trim());
    }
}
