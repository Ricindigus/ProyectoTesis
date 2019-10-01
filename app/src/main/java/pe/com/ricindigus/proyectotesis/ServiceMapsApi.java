package pe.com.ricindigus.proyectotesis;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceMapsApi {
    @GET("place/nearbysearch/json")
    Call<Places> getPlaces(@Query("location") String location,
                                         @Query("radius") String radius,
                                         @Query("type") String type,
                                         @Query("keyword") String keyword,
                                         @Query("key") String key);

    @GET("directions/json")
    Call<Route> getRoute(@Query("origin") String origin,
                         @Query("destination") String destination,
                         @Query("key") String key);

    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?
    // location=-12.178163%2C-77.0239138&
    // radius=1500&
    // type=bank&
    // keyword=BCP&
    // key=AIzaSyB8H60Ay0aohbSWMURj5LgwNkHAJ6zppXk&fbclid=IwAR3ougOj8t9aOgQARW9hCN0vilcxQfoQ0L_y5hBQ_kHePfIKKRcnTsqMv9c


    //https://maps.googleapis.com/maps/api/directions/json?
    // origin=4.53%2C-75.67&
    // destination=4.54%2C-75.66&
    // key=AIzaSyACyqEFTQcbBMagXte67g8i72BoIAXkq3c&fbclid=IwAR0ioVdmdDbbrUpP6es1fp76RS1bUxqTlimtbquKKUkQdar4m6ZL9XRAA2g



}
