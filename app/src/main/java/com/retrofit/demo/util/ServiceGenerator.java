package com.retrofit.demo.util;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 张全
 */

public class ServiceGenerator {
    public static final String API_BASE_URL = "http://api-beauty.51yishijia.com/";

   private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(HttpClientProvider.build());

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.baseUrl(API_BASE_URL).build();
        return retrofit.create(serviceClass);
    }
    public static <S> S createService(String baseUrl,Class<S> serviceClass) {
        Retrofit retrofit =  builder.baseUrl(baseUrl).build();
        return retrofit.create(serviceClass);
    }
    public static <S> S createServiceWithNoConverter(Class<S> serviceClass) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(HttpClientProvider.build())
                .build();
        return retrofit.create(serviceClass);
    }


}
