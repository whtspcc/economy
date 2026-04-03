package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataSet {
    private final List<Integer> years;
    private final Map<String, Map<Integer, Double>> productData;

    public DataSet(List<Integer> years, Map<String, Map<Integer, Double>> productData) {
        this.years = new ArrayList<>(years);
        this.productData = new LinkedHashMap<>(productData);
    }

    public List<Integer> getYears() {
        return years;
    }

    public Map<String, Map<Integer, Double>> getProductData() {
        return productData;
    }

    public boolean isEmpty() {
        return years.isEmpty() || productData.isEmpty();
    }
}
