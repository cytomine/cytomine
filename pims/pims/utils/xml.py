import logging
from xml import etree

logger = logging.getLogger("pims.app")


class XMLValidator:
    def __init__(self, xsd_path) -> None:
        xsd_doc = etree.parse(xsd_path)
        self.schema = etree.XMLSchema(xsd_doc)

    def validate(self, xml_path) -> bool:
        try:
            xml_doc = etree.parse(xml_path)
            return self.schema.validate(xml_doc)
        except Exception as e:
            logger.info(f"Error: {e}")
            return False
