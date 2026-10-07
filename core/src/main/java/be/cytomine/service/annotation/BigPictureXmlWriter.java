package be.cytomine.service.annotation;

class BigPictureXmlWriter {

    private final StringBuilder sb = new StringBuilder();

    private int depth = 0;

    BigPictureXmlWriter() {
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    }

    private void indent() {
        sb.append("\t".repeat(depth));
    }

    private void appendAttributes(String... attributes) {
        for (int i = 0; i + 1 < attributes.length; i += 2) {
            sb.append(' ').append(attributes[i]).append("=\"").append(escape(attributes[i + 1])).append('"');
        }
    }

    void open(String tag, String... attributes) {
        indent();
        sb.append('<').append(tag);
        appendAttributes(attributes);
        sb.append(">\n");
        depth++;
    }

    void close(String tag) {
        depth--;
        indent();
        sb.append("</").append(tag).append(">\n");
    }

    void leaf(String tag, String text, String... attributes) {
        indent();
        sb.append('<').append(tag);
        appendAttributes(attributes);
        sb.append('>').append(escape(text)).append("</").append(tag).append(">\n");
    }

    void empty(String tag, String... attributes) {
        leaf(tag, "", attributes);
    }

    void stringAttribute(String tag, String value) {
        open("STRING_ATTRIBUTE");
        leaf("TAG", tag);
        leaf("VALUE", value);
        close("STRING_ATTRIBUTE");
    }

    void nilStringAttribute(String tag) {
        open("STRING_ATTRIBUTE");
        leaf("TAG", tag);
        empty("VALUE", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance", "xsi:nil", "true");
        close("STRING_ATTRIBUTE");
    }

    private static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    String build() {
        int end = sb.length();
        while (end > 0 && sb.charAt(end - 1) == '\n') {
            end--;
        }
        return sb.substring(0, end);
    }
}
