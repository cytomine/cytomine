package be.cytomine.utils.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SearchParameterProcessed {

    List<SearchParameterEntry> data;

    Map<String, Object> sqlParameters;
}
