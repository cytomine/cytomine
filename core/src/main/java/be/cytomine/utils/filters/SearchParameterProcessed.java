package be.cytomine.utils.filters;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchParameterProcessed {

    List<SearchParameterEntry> data;

    Map<String, Object> sqlParameters;
}
