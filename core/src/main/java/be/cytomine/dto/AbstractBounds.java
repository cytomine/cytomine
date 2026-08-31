package be.cytomine.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import be.cytomine.utils.MinMax;

@Getter
public class AbstractBounds {

    protected <T extends Comparable> void updateMinMax(MinMax<T> currentValue, T newValue) {
        if (newValue == null) {
            return;
        }
        if (currentValue.getMin() == null) {
            currentValue.setMin(newValue);
        } else if (newValue.compareTo(currentValue.getMin()) < 0) {
            currentValue.setMin(newValue);
        }
        if (currentValue.getMax() == null) {
            currentValue.setMax(newValue);
        } else if (newValue.compareTo(currentValue.getMax()) > 0) {
            currentValue.setMax(newValue);
        }
    }

    protected <T extends Comparable> void updateChoices(MinMax<T> currentValue, T newValue) {
        if (newValue == null) {
            return;
        }
        if (currentValue.getList() == null) {
            currentValue.setList(new ArrayList<>());
        }
        if (!currentValue.getList().contains(newValue)) {
            currentValue.getList().add(newValue);
        }
    }

    protected <T extends Comparable> void updateChoices(List<T> currentValue, T newValue) {
        if (newValue == null) {
            return;
        }
        if (currentValue == null) {
            currentValue = new ArrayList<>();
        }
        if (!currentValue.contains(newValue)) {
            currentValue.add(newValue);
        }
    }
}

