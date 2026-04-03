package service;

import model.DataSet;

public class LoadResult {
    private final DataSet dataSet;
    private final String sourceName;
    private final String warning;

    public LoadResult(DataSet dataSet, String sourceName, String warning) {
        this.dataSet = dataSet;
        this.sourceName = sourceName;
        this.warning = warning;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getWarning() {
        return warning;
    }
}
