/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

import static java.util.Collections.unmodifiableList;
import static retrofit2.Utils.checkNotNull;

/**
 * Retrofit adapts a Java interface to HTTP calls by using annotations on the declared methods to
 * define how requests are made. Create instances using {@linkplain Builder
 * the builder} and pass your interface to {@link #create} to generate an implementation.
 * <p>
 * For example,
 * <pre><code>
 * Retrofit retrofit = new Retrofit.Builder()
 *     .baseUrl("https://api.example.com/")
 *     .addConverterFactory(GsonConverterFactory.create())
 *     .build();
 *
 * MyApi api = retrofit.create(MyApi.class);
 * Response&lt;User&gt; user = api.getUser().execute();
 * </code></pre>
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 */
public final class Retrofit {
    private final Map<Method, ServiceMethod> serviceMethodCache = new LinkedHashMap<>();

    private final okhttp3.Call.Factory callFactory;
    private final HttpUrl baseUrl;
    private final List<Converter.Factory> converterFactories;
    private final List<CallAdapter.Factory> adapterFactories;
    private final Executor callbackExecutor;
    private final boolean validateEagerly;

    Retrofit(okhttp3.Call.Factory callFactory, HttpUrl baseUrl,
             List<Converter.Factory> converterFactories, List<CallAdapter.Factory> adapterFactories,
             Executor callbackExecutor, boolean validateEagerly) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
        this.converterFactories = unmodifiableList(converterFactories); // Defensive copy at call site.
        this.adapterFactories = unmodifiableList(adapterFactories); // Defensive copy at call site.
        this.callbackExecutor = callbackExecutor;
        this.validateEagerly = validateEagerly;
    }

    @SuppressWarnings("unckecked")
    public <T> T create(final Class<T> service) {

        //验证service，service必须为接口，且不能继承父接口
        Utils.validateServiceInterface(service);

        //如果需要预先验证加载接口 ServiceMethod
        if (validateEagerly) {
            eagerlyValidateMethods(service);
        }

        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    private final Platform platform = Platform.get();

                    @Override
                    public Object invoke(Object proxy, Method method, Object... args)
                            throws Throwable {
                        // Object中定义的方法
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        //针对Java8
                        if (platform.isDefaultMethod(method)) {
                            return platform.invokeDefaultMethod(method, service, proxy, args);
                        }

                        //解析method，并包装成ServiceMethod
                        ServiceMethod serviceMethod = loadServiceMethod(method);

                        //使用Okhttp完成网络请求
                        OkHttpCall okHttpCall = new OkHttpCall<>(serviceMethod, args);
                        return serviceMethod.callAdapter.adapt(okHttpCall); //返回的是 ExecutorCallbackCall
                    }
                });
    }

    private void eagerlyValidateMethods(Class<?> service) {
        Platform platform = Platform.get(); //Android上为Android平台
        for (Method method : service.getDeclaredMethods()) {
            if (!platform.isDefaultMethod(method)) {
                loadServiceMethod(method);
            }
        }
    }

    ServiceMethod loadServiceMethod(Method method) {
        ServiceMethod result;
        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    /**
     * The factory used to create {@linkplain okhttp3.Call OkHttp calls} for sending a HTTP requests.
     * Typically an instance of {@link OkHttpClient}.
     */
    public okhttp3.Call.Factory callFactory() {
        return callFactory;
    }

    /**
     * The API base URL.
     */
    public HttpUrl baseUrl() {
        return baseUrl;
    }

    /**
     * Returns a list of the factories tried when creating a
     * {@linkplain #callAdapter(Type, Annotation[])} call adapter}.
     */
    public List<CallAdapter.Factory> callAdapterFactories() {
        return adapterFactories;
    }

    /**
     * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
     * #callAdapterFactories() factories}.
     *
     * @throws IllegalArgumentException if no call adapter available for {@code type}.
     */
    public CallAdapter<?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null, returnType, annotations);
    }

    /**
     * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
     * #callAdapterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no call adapter available for {@code type}.
     */
    /**
     *
     * @param skipPast
     * @param returnType  方法返回值类型，比如 retrofit2.Call<VersionEntity>
     * @param annotations
     * @return
     */
    public CallAdapter<?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType,
                                          Annotation[] annotations) {
        checkNotNull(returnType, "returnType == null");
        checkNotNull(annotations, "annotations == null");

        int start = adapterFactories.indexOf(skipPast) + 1; //默认是ExecutorCallAdapterFactory
        for (int i = start, count = adapterFactories.size(); i < count; i++) {
            CallAdapter<?> adapter = adapterFactories.get(i).get(returnType, annotations, this);
            if (adapter != null) {
                return adapter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
                .append(returnType)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(adapterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = adapterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(adapterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a list of the factories tried when creating a
     * {@linkplain #requestBodyConverter(Type, Annotation[], Annotation[]) request body converter}, a
     * {@linkplain #responseBodyConverter(Type, Annotation[]) response body converter}, or a
     * {@linkplain #stringConverter(Type, Annotation[]) string converter}.
     */
    public List<Converter.Factory> converterFactories() {
        return converterFactories;
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<T, RequestBody> requestBodyConverter(Type type,
                                                              Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        return nextRequestBodyConverter(null, type, parameterAnnotations, methodAnnotations);
    }

    /**
     *  获取支持当前RequestBody类型的转换器
     * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<T, RequestBody> nextRequestBodyConverter(Converter.Factory skipPast,
                                                                  Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
        checkNotNull(type, "type == null");
        checkNotNull(parameterAnnotations, "parameterAnnotations == null");
        checkNotNull(methodAnnotations, "methodAnnotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter.Factory factory = converterFactories.get(i);
             //RequestBody转换器
            Converter<?, RequestBody> converter =
                    factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<T, RequestBody>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
     * {@linkplain #converterFactories() factories}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
        return nextResponseBodyConverter(null, type, annotations);
    }

    /**
     * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
     * {@linkplain #converterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory skipPast,
                                                                    Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter<ResponseBody, ?> converter =
                    converterFactories.get(i).responseBodyConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<ResponseBody, T>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link String} from the available
     * {@linkplain #converterFactories() factories}.
     */
    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        for (int i = 0, count = converterFactories.size(); i < count; i++) {
            Converter<?, String> converter =
                    converterFactories.get(i).stringConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<T, String>) converter;
            }
        }

        // Nothing matched. Resort to default converter which just calls toString().
        //noinspection unchecked
        return (Converter<T, String>) BuiltInConverters.ToStringConverter.INSTANCE;
    }

    /**
     * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    public Executor callbackExecutor() {
        return callbackExecutor;
    }



    /**
     * 用来构建Retrofit实例
     * 其中baseUrl()是必须调用的，其他方法可选。
     */
    public static final class Builder {
        private Platform platform; //当前环境平台
        private okhttp3.Call.Factory callFactory; // OkHttpClient
        private HttpUrl baseUrl; // 注意@Get
        private List<Converter.Factory> converterFactories = new ArrayList<>(); //转换器，用来序列化Java对象为RequestBody，以及反序列化ResponseBody为Java对象
        private List<CallAdapter.Factory> adapterFactories = new ArrayList<>();//http请求适配器，目前都是OkHttp请求
        private Executor callbackExecutor;
        private boolean validateEagerly;

        Builder(Platform platform) {
            this.platform = platform;
            // Add the built-in converter factory first. This prevents overriding its behavior but also
            // ensures correct behavior when using converters that consume all types.
            converterFactories.add(new BuiltInConverters());
        }

        public Builder() {
            this(Platform.get());
        }

        /**
         * 设置OkHttpClient
         * 注意：这里是引用OkhttpClient,如果修改OkHttpClient配置，则会影响之后的请求。
         *
         * Note: This method <b>does not</b> make a defensive copy of {@code client}. Changes to its
         * settings will affect subsequent requests. Pass in a {@linkplain OkHttpClient#clone() cloned}
         * instance to prevent this if desired.
         */
        public Builder client(OkHttpClient client) {
            return callFactory(checkNotNull(client, "client == null"));
        }

        /**
         * 设置创建Call实例的工厂，现在都是OkHttpClient
         * Note: Calling {@link #client} automatically sets this value.
         */
        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.callFactory = checkNotNull(factory, "factory == null");
            return this;
        }

        /**
         * 设置请求的baseUrl，必须以 / 结尾
         */
        public Builder baseUrl(String baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            return baseUrl(httpUrl);
        }

        /**
         * Set the API base URL.
         * <p>
         * The specified endpoint values (such as with {@link GET @GET}) are resolved against this
         * value using {@link HttpUrl#resolve(String)}. The behavior of this matches that of an
         * {@code <a href="">} link on a website resolving on the current URL.
         * <p>
         * <b>Base URLs should always end in {@code /}.</b>
         * <p>
         * A trailing {@code /} ensures that endpoints values which are relative paths will correctly
         * append themselves to a base which has path components.
         * <p>
         * <b>Correct:</b><br>
         * Base URL: http://example.com/api/<br>
         * Endpoint: foo/bar/<br>
         * Result: http://example.com/api/foo/bar/
         * <p>
         * <b>Incorrect:</b><br>
         * Base URL: http://example.com/api<br>
         * Endpoint: foo/bar/<br>
         * Result: http://example.com/foo/bar/
         * <p>
         * This method enforces that {@code baseUrl} has a trailing {@code /}.
         * <p>
         * <b>Endpoint values which contain a leading {@code /} are absolute.</b>
         * <p>
         * Absolute values retain only the host from {@code baseUrl} and ignore any specified path
         * components.
         * <p>
         * Base URL: http://example.com/api/<br>
         * Endpoint: /foo/bar/<br>
         * Result: http://example.com/foo/bar/
         * <p>
         * Base URL: http://example.com/<br>
         * Endpoint: /foo/bar/<br>
         * Result: http://example.com/foo/bar/
         * <p>
         * <b>Endpoint values may be a full URL.</b>
         * <p>
         * Values which have a host replace the host of {@code baseUrl} and values also with a scheme
         * replace the scheme of {@code baseUrl}.
         * <p>
         * Base URL: http://example.com/<br>
         * Endpoint: https://github.com/square/retrofit/<br>
         * Result: https://github.com/square/retrofit/
         * <p>
         * Base URL: http://example.com<br>
         * Endpoint: //github.com/square/retrofit/<br>
         * Result: http://github.com/square/retrofit/ (note the scheme stays 'http')
         */
        public Builder baseUrl(HttpUrl baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) { //检测是否以 / 结尾
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Add converter factory for serialization and deserialization of objects.
         */
        public Builder addConverterFactory(Converter.Factory factory) {
            converterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /** 接口方法除了返回Call<?>,还支持其他类型，比如RxJava，CallAdapter.Factory就是用来适配Call对象的
         * Add a call adapter factory for supporting service method return types other than {@link
         * Call}.
         */
        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            adapterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /**
         * Callback回调方法所在的Executor，默认回调到主线程中
         * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
         * your service method.
         * <p>
         * Note: {@code executor} is not used for {@linkplain #addCallAdapterFactory custom method
         * return types}.
         */
        public Builder callbackExecutor(Executor executor) {
            this.callbackExecutor = checkNotNull(executor, "executor == null");
            return this;
        }

        /**
         * 调用Retrofit.create()时就完成接口方法注解信息的解析生成ServiceMethod
         * 默认调用接口方法时才完成注解信息的解析生成ServiceMethod
         */
        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }

        /**
         * 根据配置信息创建Retrofit实例
         */
        public Retrofit build() {
            //必须设置base url
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            //OkHttpCllient
            okhttp3.Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            //Callback回调方法执行器
            Executor callbackExecutor = this.callbackExecutor;
            if (callbackExecutor == null) {
                callbackExecutor = platform.defaultCallbackExecutor();
            }

            // 添加默认的ExecutorCallAdapterFactory
            List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories);
            adapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));

            // 转换器
            List<Converter.Factory> converterFactories = new ArrayList<>(this.converterFactories);

            return new Retrofit(callFactory, baseUrl, converterFactories, adapterFactories,
                    callbackExecutor, validateEagerly);
        }
    }
}
