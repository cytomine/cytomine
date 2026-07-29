package be.cytomine.utils;

import java.util.ArrayList;
import java.util.List;

public class MinMax<T> {
    T min;
    T max;
    List<T> list = new ArrayList<>();

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
