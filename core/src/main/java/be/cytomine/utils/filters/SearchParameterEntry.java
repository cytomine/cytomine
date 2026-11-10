package be.cytomine.utils.filters;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SearchParameterEntry {

    Object value;
    SearchOperation operation;
    String property;
    String sql;
    Map<String, Object> sqlParameter;

    public SearchParameterEntry() {

    }

    public SearchParameterEntry(String property, SearchOperation operation, Object value) {
        this.value = value;
        this.operation = operation;
        this.property = property;
    }

}
