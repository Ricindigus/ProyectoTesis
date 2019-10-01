package pe.com.ricindigus.proyectotesis;

import java.util.List;

public class Route {
    private List<RouteElement> routes;
    private String status;


    public List<RouteElement> getRoutes() { return routes; }
    public void setRoutes(List<RouteElement> value) { this.routes = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
}
