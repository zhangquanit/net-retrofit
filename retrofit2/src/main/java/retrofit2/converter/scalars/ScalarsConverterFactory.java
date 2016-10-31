//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package retrofit2.converter.scalars;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.BooleanResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.ByteResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.CharacterResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.DoubleResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.FloatResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.IntegerResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.LongResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.ShortResponseBodyConverter;
import retrofit2.converter.scalars.ScalarResponseBodyConverters.StringResponseBodyConverter;

public final class ScalarsConverterFactory extends Factory {
    public static ScalarsConverterFactory create() {
        return new ScalarsConverterFactory();
    }

    private ScalarsConverterFactory() {
    }

    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return type != String.class && type != Boolean.TYPE && type != Boolean.class && type != Byte.TYPE && type != Byte.class && type != Character.TYPE && type != Character.class && type != Double.TYPE && type != Double.class && type != Float.TYPE && type != Float.class && type != Integer.TYPE && type != Integer.class && type != Long.TYPE && type != Long.class && type != Short.TYPE && type != Short.class?null:ScalarRequestBodyConverter.INSTANCE;
    }

    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return (Converter)(type == String.class?StringResponseBodyConverter.INSTANCE:(type != Boolean.class && type != Boolean.TYPE?(type != Byte.class && type != Byte.TYPE?(type != Character.class && type != Character.TYPE?(type != Double.class && type != Double.TYPE?(type != Float.class && type != Float.TYPE?(type != Integer.class && type != Integer.TYPE?(type != Long.class && type != Long.TYPE?(type != Short.class && type != Short.TYPE?null:ShortResponseBodyConverter.INSTANCE):LongResponseBodyConverter.INSTANCE):IntegerResponseBodyConverter.INSTANCE):FloatResponseBodyConverter.INSTANCE):DoubleResponseBodyConverter.INSTANCE):CharacterResponseBodyConverter.INSTANCE):ByteResponseBodyConverter.INSTANCE):BooleanResponseBodyConverter.INSTANCE));
    }
}
