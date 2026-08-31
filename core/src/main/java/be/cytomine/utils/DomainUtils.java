package be.cytomine.utils;

import java.util.List;
import java.util.stream.Collectors;

import be.cytomine.domain.CytomineDomain;

public class DomainUtils {
    public static List<Long> extractIds(List<? extends CytomineDomain> domains) {
        return domains.stream().map(CytomineDomain::getId).collect(Collectors.toList());
    }
}
