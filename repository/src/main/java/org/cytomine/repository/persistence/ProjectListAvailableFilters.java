package org.cytomine.repository.persistence;

import java.util.Optional;
import java.util.Set;

import be.cytomine.common.repository.model.list.filtered.BoundedFilter;

public record ProjectListAvailableFilters(Set<String> ontologyNames,
                                          Set<String> roleNames,
                                          Set<String> tagLabels,
                                          Optional<String> name,
                                          BoundedFilter memberCount,
                                          BoundedFilter imageCount,
                                          BoundedFilter manualAnnotationCount,
                                          BoundedFilter reviewedAnnotationCount) {}
