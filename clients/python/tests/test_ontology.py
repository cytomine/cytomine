from typing import Any, Dict

from cytomine.cytomine import Cytomine
from cytomine.models import (
    Ontology,
    OntologyCollection,
    RelationTerm,
    Term,
    TermCollection,
)
from tests.conftest import random_string


def test_fetch_ontology(ontology_factory) -> None:
    expected_name = random_string()
    created = ontology_factory(name=expected_name)
    print(created)

    fetched = Ontology().fetch(created.id)

    assert isinstance(fetched, Ontology)
    assert fetched.name == expected_name


def test_create_ontology(ontology_factory) -> None:
    expected_name = random_string()
    ontology = ontology_factory(name=expected_name)

    assert ontology.name == expected_name


def test_update_ontology(ontology_factory) -> None:
    previous_name = random_string()
    expected_name = random_string()
    ontology = ontology_factory(name=previous_name)

    ontology.name = expected_name
    ontology.update()

    assert ontology.name != previous_name
    assert ontology.name == expected_name


def test_delete_ontology(ontology_factory) -> None:
    ontology = ontology_factory(name=random_string())

    ontology.delete()

    assert not Ontology().fetch(ontology.id)


def test_fetch_ontologies(ontology_factory) -> None:
    created = [ontology_factory() for _ in range(5)]

    fetched = OntologyCollection().fetch()
    assert isinstance(fetched, OntologyCollection)

    fetched_names = {o.name for o in fetched}
    for ontology in created:
        assert ontology.name in fetched_names


class TestTerm:
    def test_term(self, connect: Cytomine, dataset: Dict[str, Any]) -> None:
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
        dataset: Dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch()
        assert isinstance(terms, TermCollection)

        terms = TermCollection()
        terms.append(Term(random_string(), dataset["ontology"].id, "#AAAAAA"))
        assert terms.save()

    def test_terms_by_project(
        self,
        connect: Cytomine,
        dataset: Dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch_with_filter("project", dataset["project"].id)
        assert isinstance(terms, TermCollection)

    def test_terms_by_ontology(
        self,
        connect: Cytomine,
        dataset: Dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch_with_filter("ontology", dataset["ontology"].id)
        assert isinstance(terms, TermCollection)

    def test_terms_by_annotation(
        self,
        connect: Cytomine,
        dataset: Dict[str, Any],
    ) -> None:
        terms = TermCollection().fetch_with_filter(
            "annotation",
            dataset["annotation"].id,
        )
        assert isinstance(terms, TermCollection)


class TestRelationTerm:
    def test_relation_term(self, connect: Cytomine, dataset: Dict[str, Any]) -> None:
        rt = RelationTerm(dataset["term1"].id, dataset["term2"].id).save()
        assert isinstance(rt, RelationTerm)

        rt = RelationTerm().fetch(dataset["term1"].id, dataset["term2"].id)
        assert rt.term1 == dataset["term1"].id  # type: ignore

        rt.delete()  # type: ignore
        assert not RelationTerm().fetch(dataset["term1"].id, dataset["term2"].id)
