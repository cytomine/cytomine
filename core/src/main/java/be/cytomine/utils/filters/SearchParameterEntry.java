package be.cytomine.utils.filters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class SearchParameterEntry {

    public SearchParameterEntry() {

    }

    public SearchParameterEntry(String property, SearchOperation operation, Object value) {
        this.value = value;
        this.operation = operation;
        this.property = property;
    }

    Object value;

    SearchOperation operation;

    String property;

    String sql;

    Map<String, Object> sqlParameter;

}
