package com.retrofit.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.google.gson.Gson;
import com.retrofit.demo.api.VersionUpdateService;
import com.retrofit.demo.entity.VersionEntity;
import com.retrofit.demo.entity.VersionPostData;
import com.retrofit.demo.util.HttpClientProvider;
import com.retrofit.demo.util.ServiceGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * http://blog.csdn.net/ljd2038/article/details/51046512
 * <p>
 * https://api.github.com/repos/square/retrofit/contributors
 * <p>
 * https://futurestud.io/tutorials/retrofit-2-how-to-upload-files-to-server
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private Call<?> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.twoAct).setVisibility(View.VISIBLE);
        findViewById(R.id.twoAct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,TwoActivity.class));
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                        getTest();
//        postFormTest();
//        postFormDataTest();
//        postBodyTest();
//        postBodyTest2();
//                urlTest();
//                downloadFileTest();
//                postStringTest();
                break;
            case R.id.cancel:
                if (null != this.call) call.cancel();
                break;
        }
    }

    private void urlTest() {
        //http://blog.csdn.net/zhangquanit/article/details/52851432
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://blog.csdn.net/zhangquanit/")
//                .addConverterFactory(GsonConverterFactory.create())
                .client(HttpClientProvider.build())
                .build();

        VersionUpdateService versionUpdateService = retrofit.create(VersionUpdateService.class);
//       Call<ResponseBody> call = versionUpdateService.doUrl(); //http://blog.csdn.net/article/details/52851432
//       Call<ResponseBody> call = versionUpdateService.doUrl2("article/details/52851432");// http://blog.csdn.net/zhangquanit/article/details/52851432
//       Call<ResponseBody> call = versionUpdateService.doUrl2("/article/details/52851432"); // http://blog.csdn.net/article/details/52851432
        Call<ResponseBody> call = versionUpdateService.doUrl2("http://blog.csdn.net/zhangquanit/article/details/52851432");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(response.body());

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });
    }

    private void getTest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api-beauty.51yishijia.com/")
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RXJAVA
                .client(HttpClientProvider.build())
                .build();
        VersionUpdateService versionUpdateService = retrofit.create(VersionUpdateService.class);

        //path
        String path_v1 = "v1";

        //query
        String query = "中文";
        //queryMap
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("from", "Android_uc");
        queryMap.put("platform", "android");
        queryMap.put("token", "");
        queryMap.put("appVersion", "20100");

        //header
        String head3 = "value3";
        //header map
        Map<String, String> headMap = new HashMap<>();
        headMap.put("header4", "value4");

        Call<VersionEntity> call = versionUpdateService.doGet(path_v1, query, queryMap, head3, headMap);
        call.enqueue(new Callback<VersionEntity>() {
            @Override
            public void onResponse(Call<VersionEntity> call, retrofit2.Response<VersionEntity> response) {
                String name = Thread.currentThread().getName();//main
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<VersionEntity> call, Throwable t) {
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });
/*
    GET http://api-beauty.51yishijia.com/api/v1/updateVersion?queryKey=%E4%B8%AD%E6%96%87&platform=android&from=Android_uc&token=&appVersion=20100 HTTP/1.1
    header1: value1
    header2: value2
    header3: value3
    header4: value4
    User-Agent: com.cheyu.taoban/2.1.0(Android OS 6.0.1,MI 3W
    Host: api-beauty.51yishijia.com
    Connection: Keep-Alive
    Accept-Encoding: gzip

*/

    }


    /**
     * POST Body
     * 能够实现任何POST请求
     */
    private void postBodyTest() {

        VersionUpdateService versionUpdateService = ServiceGenerator.createService("http://blog.csdn.net/", VersionUpdateService.class);
        Call<okhttp3.ResponseBody> call = null;


        //提交表单数据
        FormBody formBody = new FormBody.Builder()
                .addEncoded("key1", "value1")
                .addEncoded("key2", "value2")
                .build();
//        call = versionUpdateService.doPostBody(formBody);

        //上传文件
        File file = null;
        String fileName = "zhangquan.jpg";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = new File(Environment.getExternalStorageDirectory(), fileName);
        }
        RequestBody uploadImg = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key1", "value1")
                .addFormDataPart("key2", "value2")
                .addFormDataPart("img", fileName, uploadImg)
                .build();
//        call = versionUpdateService.doPostBody(multipartBody);

        //提交 Json数据
        VersionEntity versionEntity = new VersionEntity();
        versionEntity.setErrorCode(1);
        VersionEntity.DataBean dataBean = new VersionEntity.DataBean();
        dataBean.setFrom("Android_UC");
        versionEntity.setData(dataBean);
        RequestBody jsonBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(versionEntity));
//        call = versionUpdateService.doPostBody(jsonBody);

        //普通的Post请求
        RequestBody postBody = RequestBody.create(null, "content");
//        call = versionUpdateService.doPostBody(postBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(Thread.currentThread().getName());
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });
    }

    /**
     * @Body 使用GsonConverter
     */
    private void postBodyTest2() {

        VersionUpdateService versionUpdateService = ServiceGenerator.createService(VersionUpdateService.class);

        VersionPostData versionPostData = new VersionPostData();
        versionPostData.setFrom("Android_uc");
        versionPostData.setPlatform("Android");
        versionPostData.setAppVersion(20100);
        Call<VersionEntity> call = versionUpdateService.doPostBody2(versionPostData);
        call.enqueue(new Callback<VersionEntity>() {
            @Override
            public void onResponse(Call<VersionEntity> call, Response<VersionEntity> response) {
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<VersionEntity> call, Throwable t) {
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });
    }

    /**
     * Form表单请求
     */
    private void postFormTest() {
        VersionUpdateService versionUpdateService = ServiceGenerator.createService(VersionUpdateService.class);

        //path
        String path_v1 = "v1";

        //field
        String field = "Android_uc";
        //fieldMap
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("platform", "android");
        fieldMap.put("token", "");
        fieldMap.put("appVersion", "20100");

        Call<VersionEntity> call = versionUpdateService.doPostForm(path_v1, field, fieldMap);
        call.enqueue(new Callback<VersionEntity>() {
            @Override
            public void onResponse(Call<VersionEntity> call, Response<VersionEntity> response) {
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<VersionEntity> call, Throwable t) {
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });

 /*
        POST http://api-beauty.51yishijia.com/api/v1/updateVersion HTTP/1.1
        header1: value1
        header2: value2
        User-Agent: com.cheyu.taoban/2.1.0(Android OS 6.0.1,MI 3W
        Content-Type: application/x-www-form-urlencoded
        Content-Length: 56
        Host: api-beauty.51yishijia.com
        Connection: Keep-Alive
        Accept-Encoding: gzip

        from=Android_uc&platform=android&token=&appVersion=20100

   */
    }

    /**
     * Form表单上传文件
     */
    private void postFormDataTest() {
        VersionUpdateService versionUpdateService = ServiceGenerator.createService(VersionUpdateService.class);

        //path
        String path_v1 = "v1";

        //上传的文件
        File file = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = new File(Environment.getExternalStorageDirectory(), "zhangquan.jpg");
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part uploadImg =
                MultipartBody.Part.createFormData("img", file.getName(), requestFile);

        final MediaType MULTIPART = MediaType.parse("multipart/form-data");
        //单个part
        RequestBody part =
                RequestBody.create(MULTIPART, "Android_uc");

        //part map
        Map<String, RequestBody> partMap = new HashMap<>();
        partMap.put("platform", RequestBody.create(MULTIPART, "android"));
        partMap.put("token", RequestBody.create(MULTIPART, ""));
        partMap.put("appVersion", RequestBody.create(MULTIPART, "20100"));


        Call<VersionEntity> call = versionUpdateService.doPostFormData(path_v1, uploadImg, part, partMap);
        call.enqueue(new Callback<VersionEntity>() {
            @Override
            public void onResponse(Call<VersionEntity> call, Response<VersionEntity> response) {
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<VersionEntity> call, Throwable t) {
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });

    }

    /**
     * 文件下载
     */
    private void downloadFileTest() {
        String url = "http://www.taobanapp.com/app/Android_uc.apk";
        OkHttpClient httpClient = HttpClientProvider.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.baidu.com")
//                .client(httpClient)
                .build();
        VersionUpdateService versionUpdateService = retrofit.create(VersionUpdateService.class);
        Call<ResponseBody> call = versionUpdateService.downloadFile(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                checkRequestContent(call.request());
                if (response.isSuccessful()) {
                    System.out.println("server contacted and has file");
                    //不能在UI线程中下载
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                            System.out.println("file download was a success? " + writtenToDisk);
                        }
                    }).start();

                } else {
                    System.out.println("server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                checkRequestContent(call.request());
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });
        this.call = call;
    }


    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            File file = new File(Environment.getExternalStorageDirectory(), "beauty.apk");
            if (file.exists()) {
                file.delete();
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    System.out.println("file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void checkRequestContent(Request request) {
        Headers requestHeaders = request.headers();
        RequestBody requestBody = request.body();
        HttpUrl requestUrl = request.url();

        System.out.println("requestUrl: " + requestUrl);
        System.out.println("requestHeaders: " + requestHeaders);
        System.out.println("requestBody: " + requestBody);

    }

    private void postStringTest() {
        //http://blog.csdn.net/zhangquanit/article/details/52851432
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://blog.csdn.net/zhangquanit/")
                .addConverterFactory(ScalarsConverterFactory.create()) //增加基本数据类型和String类型的转换器
                .addConverterFactory(GsonConverterFactory.create()) //GSON转换器放在最后一个
                .client(HttpClientProvider.build())
                .build();

        VersionUpdateService versionUpdateService = retrofit.create(VersionUpdateService.class);
        Call<String> call = versionUpdateService.postString("post content");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                System.out.println(response.body());

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (call.isCanceled()) {
                    System.out.println("onFailure...........request cancelled");
                } else {
                    System.out.println("onFailure...........");
                    t.printStackTrace();
                }
            }
        });
    }
}