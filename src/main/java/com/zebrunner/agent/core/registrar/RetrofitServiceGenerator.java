package com.zebrunner.agent.core.registrar;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zebrunner.agent.core.config.ConfigurationHolder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public final class RetrofitServiceGenerator {
    private static final String API_HOST = ConfigurationHolder.getHost();
    private static Gson gson = Converters.registerOffsetDateTime(new GsonBuilder()).create();
    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_HOST)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson));
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private static Retrofit retrofit = builder.build();
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static <T> T createService(Class<T> serviceClass) {
        httpClient.interceptors()
                  .clear();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                                      .addHeader("Connection", "close")
                                      .addHeader("Content-Type", "application/json")
                                      .addHeader("Accept", "application/json")
                                      .build();
            return chain.proceed(request);
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        builder.client(httpClient.build());
        retrofit = builder.build();
        return retrofit.create(serviceClass);
    }
}
