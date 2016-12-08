package com.retrofit.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.retrofit.demo.api.RxJavaTest;
import com.retrofit.demo.entity.VersionEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Retrofit-RxJava
 *
 * @author 张全
 */

public class TwoActivity extends Activity implements View.OnClickListener {
    private Call<?> call;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://api-beauty.51yishijia.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create()) //Gson
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//支持RXJAVA
                .build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
//                test();
//                test2();
                test3();
                break;
            case R.id.cancel:
                if (null != this.call) call.cancel();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != this.call) call.cancel();
    }

    /**
     * Retrofit2使用RxJava基本测试
     */
    private void test() {
        //path
        String path_v1 = "v1";

        //field
        String field = "Android_uc";
        //fieldMap
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("platform", "android");
        fieldMap.put("token", "");
        fieldMap.put("appVersion", "20100");

        RxJavaTest rxJavaTest = retrofit.create(RxJavaTest.class);
        rxJavaTest.doPostForm(path_v1, field, fieldMap)
                .subscribeOn(Schedulers.io()) //在io线程中请求网络
                .observeOn(AndroidSchedulers.mainThread()) //回调到主线程中
                .subscribe(new Subscriber<VersionEntity>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        System.out.println("onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(VersionEntity versionEntity) {
                        System.out.println("onNext=" + versionEntity);
                    }
                });
    }


    /**
     * 加载失败时  重试3次 每次延迟5秒执行
     *
     * @note: 可关闭网络后测试
     */
    private void test2() {
        //path
        String path_v1 = "v1";

        //field
        String field = "Android_uc";
        //fieldMap
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("platform", "android");
        fieldMap.put("token", "");
        fieldMap.put("appVersion", "20100");

        RxJavaTest rxJavaTest = retrofit.create(RxJavaTest.class);
        rxJavaTest.doPostForm(path_v1, field, fieldMap)
                .subscribeOn(Schedulers.io()) //在io线程中请求网络
                .observeOn(AndroidSchedulers.mainThread()) //回调到主线程中
                //加载失败时  重试3次 每次延迟5秒执行
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> error) {
                        return error.zipWith(Observable.range(1, 3), new Func2<Throwable, Integer, Integer>() {
                            //重复3次
                            @Override
                            public Integer call(Throwable throwable, Integer integer) {
                                System.out.println("zipWith----" + integer + ",e:" + throwable.getMessage());
                                return integer;
                            }
                        }).flatMap(new Func1<Integer, Observable<?>>() {
                            @Override
                            public Observable<?> call(Integer integer) {
                                System.out.println("flatMap----" + integer);
                                return Observable.timer(3, TimeUnit.SECONDS); //延迟5秒
                            }
                        });
                    }
                })
                .subscribe(new Subscriber<VersionEntity>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        System.out.println("onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(VersionEntity versionEntity) {
                        System.out.println("onNext=" + versionEntity);
                    }
                });
    }

    /**
     * 测试接口依赖的情况：先请求A接口，然后请求B接口
     */
    private void test3() {
        final RxJavaTest rxJavaTest = retrofit.create(RxJavaTest.class);
        rxJavaTest.doGet("http://www.baidu.com") //先请求A接口
                .flatMap(new Func1<ResponseBody, Observable<VersionEntity>>() {
                    @Override
                    public Observable<VersionEntity> call(ResponseBody responseBody) { //继续请求B接口
                        //path
                        String path_v1 = "v1";

                        //field
                        String field = "Android_uc";
                        //fieldMap
                        Map<String, String> fieldMap = new HashMap<>();
                        fieldMap.put("platform", "android");
                        fieldMap.put("token", "");
                        fieldMap.put("appVersion", "20100");
                        return rxJavaTest.doPostForm(path_v1, field, fieldMap);
                    }
                })
                .subscribeOn(Schedulers.io()) //在io线程中请求网络
                .observeOn(AndroidSchedulers.mainThread()) //回调到主线程中
                .subscribe(new Subscriber<VersionEntity>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        System.out.println("onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(VersionEntity versionEntity) {
                        System.out.println("onNext=" + versionEntity);
                    }
                });
    }
}
