package be.cytomine.service.annotation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BigPictureXmlWriterTest {

    private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    @Test
    public void emptyWriterOnlyContainsDeclaration() {
        assertThat(new BigPictureXmlWriter().build()).isEqualTo(DECLARATION);
    }

    @Test
    public void openAndCloseNestElementsWithTabIndentation() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("A");
        xml.open("B");
        xml.leaf("C", "text");
        xml.close("B");
        xml.close("A");

        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<A>",
            "\t<B>",
            "\t\t<C>text</C>",
            "\t</B>",
            "</A>"
        ));
    }

    @Test
    public void leafWritesTextAndAttributesOnASingleLine() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.leaf("FILE", "content", "filename", "a.geojson", "filetype", "geojson");

        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<FILE filename=\"a.geojson\" filetype=\"geojson\">content</FILE>"
        ));
    }

    @Test
    public void emptyElementIsNotSelfClosed() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.empty("IMAGE_REF", "alias", "IMAGE_abc");

        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<IMAGE_REF alias=\"IMAGE_abc\"></IMAGE_REF>"
        ));
    }

    @Test
    public void danglingAttributeValueIsIgnored() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.empty("REF", "alias", "value", "orphan");

        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<REF alias=\"value\"></REF>"
        ));
    }

    @Test
    public void textContentIsXmlEscaped() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.leaf("VALUE", "a & b < c > d \" e ' f");

        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<VALUE>a &amp; b &lt; c &gt; d &quot; e &apos; f</VALUE>"
        ));
    }

    @Test
    public void attributeValuesAreXmlEscaped() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.empty("REF", "alias", "a<b>&\"'");

        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<REF alias=\"a&lt;b&gt;&amp;&quot;&apos;\"></REF>"
        ));
    }

    @Test
    public void stringAttributeWritesTagValueBlock() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.stringAttribute("date_of_creation", "2025-01-01T00:00:00");

        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<STRING_ATTRIBUTE>",
            "\t<TAG>date_of_creation</TAG>",
            "\t<VALUE>2025-01-01T00:00:00</VALUE>",
            "</STRING_ATTRIBUTE>"
        ));
    }

    @Test
    public void nilStringAttributeWritesNilValue() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.nilStringAttribute("tox_study_duration");

        String nilValue = "\t<VALUE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></VALUE>";
        assertThat(xml.build()).isEqualTo(String.join("\n",
            DECLARATION,
            "<STRING_ATTRIBUTE>",
            "\t<TAG>tox_study_duration</TAG>",
            nilValue,
            "</STRING_ATTRIBUTE>"
        ));
    }

    @Test
    public void buildStripsOnlyTrailingNewlines() {
        BigPictureXmlWriter xml = new BigPictureXmlWriter();
        xml.open("SET");
        xml.empty("REF", "alias", "x");
        xml.close("SET");

        String result = xml.build();
        assertThat(result).doesNotEndWith("\n");
        assertThat(result).endsWith("</SET>");
        assertThat(result).contains("\n\t<REF alias=\"x\"></REF>\n");
    }
}
