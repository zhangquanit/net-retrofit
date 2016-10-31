package com.retrofit.demo.util;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 张全
 */

public class HttpClientProvider {


    /**
     * 日志拦截器
     */
    static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            System.out.println("----------LoggingInterceptor---start");
            Request request = chain.request();
            long t1 = System.nanoTime();
            System.out.println(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            System.out.println(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            System.out.println("----------LoggingInterceptor---end");
            return response;
        }
    }

    /**
     * 消息头拦截器
     */
    static class HeadInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            System.out.println("----------HeadInterceptor---start");
            Request request = chain.request();
            request = request.newBuilder().addHeader("User-Agent", "com.cheyu.taoban/2.1.0(Android OS 6.0.1,MI 3W").build();
            Response response = chain.proceed(request);
            System.out.println("----------HeadInterceptor---end");
            return response;
        }
    }

    static class MyNetWorkInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            System.out.println("----------MyNetWorkInterceptor---start");
            Request request = chain.request();
            Response response = chain.proceed(request);
            System.out.println("----------MyNetWorkInterceptor---end");
            return response;
        }
    }

    public static OkHttpClient build() {
        /*
         关于Intercepter，普通Interceptor先于NetworkInterceptor执行,同类Interceptor按照添加顺序执行。
         */
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new HeadInterceptor())
                .addInterceptor(new LoggingInterceptor())
                .addNetworkInterceptor(new MyNetWorkInterceptor())
                .build();
        return httpClient;
        /*
10-20 17:29:59.085 23838-23889/com.retrofit.demo I/System.out: ----------HeadInterceptor---start
10-20 17:29:59.087 23838-23889/com.retrofit.demo I/System.out: ----------LoggingInterceptor---start
10-20 17:29:59.135 23838-23889/com.retrofit.demo I/System.out: ----------MyNetWorkInterceptor---start
10-20 17:29:59.193 23838-23889/com.retrofit.demo I/System.out: ----------MyNetWorkInterceptor---end
10-20 17:29:59.195 23838-23889/com.retrofit.demo I/System.out: ----------LoggingInterceptor---end
10-20 17:29:59.195 23838-23889/com.retrofit.demo I/System.out: ----------HeadInterceptor---end
         */
    }
}
