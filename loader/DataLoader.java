package loader;

import model.DataSet;

public interface DataLoader {
    String sourceName();

    DataSet load() throws Exception;
}
