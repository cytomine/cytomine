package be.cytomine.service.report;

public class ReportColumn {

    public String property;
    public String title;
    public float columnWidth;

    public ReportColumn(String property, String title, float columnWidth) {
        this.property = property;
        this.title = title;
        this.columnWidth = columnWidth;
    }
}
