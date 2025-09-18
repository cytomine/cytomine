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

import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.Relation;
import be.cytomine.domain.ontology.Term;

public interface TermRepository extends JpaRepository<Term, Long>, JpaSpecificationExecutor<Term> {

    List<Term> findAllByOntology(Ontology ontology);

    @Query("SELECT term FROM Term as term WHERE term.ontology = :ontology AND term.id NOT IN " +
        "(SELECT DISTINCT rel.term1.id FROM RelationTerm as rel, Term as t WHERE rel.relation = " +
        ":relation AND t.ontology = :ontology AND t.id=rel.term1.id)")
    List<Term> findAllLeafTerms(Ontology ontology, Relation relation);

    @Query("SELECT term.id FROM Term as term WHERE term.ontology = :ontology")
    List<Long> listAllIds(Ontology ontology);

    Optional<Term> findByNameAndOntology(String name, Ontology ontology);
}
