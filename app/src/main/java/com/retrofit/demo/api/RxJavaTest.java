package com.retrofit.demo.api;

import com.retrofit.demo.entity.VersionEntity;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;
import rx.Observable;

/**
 * @author 张全
 */

public interface RxJavaTest {
    @POST("api/{version}/updateVersion")
    @FormUrlEncoded
    Observable<VersionEntity> doPostForm(@Path("version") String version, @Field("from") String field, @FieldMap Map<String, String> fieldMap);

    @GET
    Observable<ResponseBody> doGet(@Url String url);

}
