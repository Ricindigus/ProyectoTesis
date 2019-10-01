package pe.com.ricindigus.proyectotesis;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebServiceMaps {

    private final String BASE_URL_API_MAPS = "https://maps.googleapis.com/maps/api/";


    private static WebServiceMaps instance;
    private Retrofit retrofit;
    private HttpLoggingInterceptor httpLoggingInterceptor;
    private OkHttpClient.Builder okHttpClientBuilder;


    public static void setInstance(WebServiceMaps instance) {
        WebServiceMaps.instance = instance;
    }

    public WebServiceMaps(){
        httpLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
        okHttpClientBuilder = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder requestBuild = original.newBuilder()
                                .method(original.method(),original.body());
                        Request request = requestBuild.build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(httpLoggingInterceptor);


        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_API_MAPS)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized WebServiceMaps getInstance(){
        if (instance == null)
            instance = new WebServiceMaps();
        return instance;
    }

    public <S> S createService(Class<S> serviceClass){
        return retrofit.create(serviceClass);
    }
}
