package pe.com.ricindigus.proyectotesis;

import java.util.List;

public class Places {
    private List<Result> results;
    private String status;

    public List<Result> getResults() { return results; }
    public void setResults(List<Result> value) { this.results = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
}
