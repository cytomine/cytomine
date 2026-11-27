from types import SimpleNamespace

from pims_plugin_format_openslide.ndpi import NDPIParser

# Valeur réelle relevée sur un fichier fautif, pendant votre enquête
COMMENTS = "<valeur du tag Comments qui provoquait l'erreur 500>"


def test_comments_with_nested_block(monkeypatch):
    monkeypatch.setattr(
        NDPIParser, "baseline",
        property(lambda self: SimpleNamespace(ndpi_tags={"Comments": COMMENTS})),
    )
    parser = NDPIParser.__new__(NDPIParser)   # sans fichier
    tags = parser._parsed_ndpi_tags           # levait une exception avant la correction
    assert "Comments" not in tags
