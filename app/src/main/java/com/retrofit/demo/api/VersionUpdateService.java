package com.retrofit.demo.api;


import com.retrofit.demo.entity.VersionEntity;
import com.retrofit.demo.entity.VersionPostData;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 张全
 */
public interface VersionUpdateService {

    //----------------GET请求
    @Headers(
            {
                    "header1: value1",
                    "header2: value2"
            }
    )
    @GET("api/{version}/updateVersion")
    Call<VersionEntity> doGet(@Path("version") String version,
                              @Query(value = "queryKey",encoded =true) String query,
                              @QueryMap Map<String, String> paramMap,
                              @Header("header3") String header3,
                              @HeaderMap Map<String, String> headMap);

    //----------------表单请求
    @Headers(
            {
                    "header1: value1",
                    "header2: value2"
            }
    )
    @POST("api/{version}/updateVersion")
    @FormUrlEncoded //代表表单请求
    Call<VersionEntity> doPostForm(@Path("version") String version, @Field("from") String field, @FieldMap Map<String, String> fieldMap);


    @POST("api/{version}/updateVersion")
    @Multipart //代表表单上传文件
    Call<VersionEntity> doPostFormData(
            @Path("version") String version,
            @Part MultipartBody.Part file, //对于MultipartBody.Part  不能指定名称，创建Part时指定  MultipartBody.Part.createFormData("img", file.getName(), requestFile);
            @Part("from") RequestBody part,
            @PartMap Map<String,
                    RequestBody> partMap);


    /**
     * @param body
     * @return
     * @Body 如果不指定Converter，@Body的参数类型只能RequestBody，返回ResponseBody
     */
    @POST("zhangquanit/article/details/52851432")
    Call<ResponseBody> doPostBody(@Body RequestBody body);


    /**
     * @param body
     * @return
     * @Body 如果使用Converter，@Body的参数类型可以为Converter支持反序列化的普通JavaBean(实际上也是通过Converter转换为RequestBody)，返回普通JavaBean
     */
    @POST("api/v1/updateVersion")
    Call<VersionEntity> doPostBody2(@Body VersionPostData body);


    @GET("/article/details/52851432")
        //  以/开头，会直接拼接到baseurl的host后面, baseurl中host后面的部分会被去掉
    Call<ResponseBody> doUrl();

    @GET()
    Call<ResponseBody> doUrl2(@Url String url);

    @Streaming
    @GET()
    Call<ResponseBody> downloadFile(@Url String url);


    @POST("article/details/52851432")
    Call<String> postString(@Body String content);

    @POST("article/details/52851432")
    Call<ResponseBody> postString2(@Body String content);
}
