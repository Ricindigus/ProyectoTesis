package pe.com.ricindigus.proyectotesis;

public class RouteElement {
    private String copyrights;
    private Polyline overview_polyline;
    private String summary;

    public Polyline getOverviewPolyline() {
        return overview_polyline;
    }

    public void setOverviewPolyline(Polyline overviewPolyline) {
        this.overview_polyline = overviewPolyline;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
