# -*- coding: utf-8 -*-

from typing import Any

from cytomine.cytomine import Cytomine
from cytomine.models import (
    Ontology,
    OntologyCollection,
    RelationTerm,
    Term,
    TermCollection,
)
from tests.conftest import random_string

class TestOntology:
    def test_ontology(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        name = random_string()
        ontology = Ontology(name).save()
        assert isinstance(ontology, Ontology)
        assert ontology.name == name

        ontology = Ontology().fetch(ontology.id)
        assert isinstance(ontology, Ontology)
        assert ontology.name == name

        name = random_string()
        ontology.name = name
        ontology.update()
        assert isinstance(ontology, Ontology)
        assert ontology.name == name

        ontology.delete()
        assert not Ontology().fetch(ontology.id)

    def test_ontologies(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        ontologies = OntologyCollection().fetch()
        assert isinstance(ontologies, OntologyCollection)

        ontologies = OntologyCollection()
        ontologies.append(Ontology(random_string()))
        assert ontologies.save()

class TestTerm:
    def test_term(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        name = random_string()
        term = Term(name, dataset["ontology"].id, "#AAAAAA").save()
        assert isinstance(term, Term)
        assert term.name == name

        term = Term().fetch(term.id)
        assert isinstance(term, Term)
        assert term.name == name

        name = random_string()
        term.name = name
        term.update()
        assert isinstance(term, Term)
        assert term.name == name

        term.delete()
        assert not Term().fetch(term.id)

    def test_terms(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch()
        assert isinstance(terms, TermCollection)

        terms = TermCollection()
        terms.append(Term(random_string(), dataset["ontology"].id, "#AAAAAA"))
        assert terms.save()

    def test_terms_by_project(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch_with_filter("project", dataset["project"].id)
        assert isinstance(terms, TermCollection)

    def test_terms_by_ontology(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch_with_filter("ontology", dataset["ontology"].id)
        assert isinstance(terms, TermCollection)

    def test_terms_by_annotation(
        self,
        connect: Cytomine,
        dataset: dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch_with_filter(
            "annotation",
            dataset["annotation"].id,
        )
        assert isinstance(terms, TermCollection)

class TestRelationTerm:
    def test_relation_term(self, connect: Cytomine, dataset: dict[str, Any]) -> None:
        rt = RelationTerm(dataset["term1"].id, dataset["term2"].id).save()
        assert isinstance(rt, RelationTerm)

        rt = RelationTerm().fetch(dataset["term1"].id, dataset["term2"].id)
        assert rt.term1 == dataset["term1"].id  # type: ignore

        rt.delete()  # type: ignore
        assert not RelationTerm().fetch(dataset["term1"].id, dataset["term2"].id)
