package be.cytomine.repository.ontology;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import be.cytomine.domain.ontology.Relation;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.domain.ontology.Term;

public interface RelationTermRepository extends JpaRepository<RelationTerm, Long>,
    JpaSpecificationExecutor<Relation> {

    Optional<RelationTerm> findByRelationAndTerm1AndTerm2(Relation relation, Term term1,
                                                          Term term2);

    @Query("SELECT rt FROM RelationTerm rt WHERE rt.relation.id = :relation AND rt.term1.id = " +
        ":term1 AND rt.term2.id = :term2")
    Optional<RelationTerm> findByRelationAndTerm1AndTerm2(Long relation, Long term1, Long term2);

    List<RelationTerm> findAllByTerm1(Term term);

    List<RelationTerm> findAllByTerm2(Term term);

    @Query("SELECT rt FROM RelationTerm rt WHERE rt.term1 = :term OR rt.term2 = :term")
    List<RelationTerm> findAllByTerm(Term term);


}
