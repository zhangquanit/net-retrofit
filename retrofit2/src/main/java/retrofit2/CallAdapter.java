/*
 * Copyright (C) 2015 Square, Inc.
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Retrofit.Builder.addCallAdapterFactory(Factory)
 */
public interface CallAdapter<T> {
  /**
   * 返回值类型，就是Call<?> 中的泛型? ，比如Call<Repo>的Repo
   */
  Type responseType();

  <R> T adapt(Call<R> call);

  /**
   * CallAdapter对象工厂
   * Creates {@link CallAdapter} instances based on the return type of {@linkplain
   * Retrofit#create(Class) the service interface} methods.
   */
  abstract class Factory {
    /**
     * @param returnType  :返回值类型 比如Call<?>
     * @param annotations :方法上的注解，比如@Post
     */
    public abstract CallAdapter<?> get(Type returnType, Annotation[] annotations,
        Retrofit retrofit);

    /**
     * Extract the upper bound of the generic parameter at {@code index} from {@code type}. For
     * example, index 1 of {@code Map<String, ? extends Runnable>} returns {@code Runnable}.
     */
    protected static Type getParameterUpperBound(int index, ParameterizedType type) {
      return Utils.getParameterUpperBound(index, type);
    }

    /**
     * Extract the raw class type from {@code type}. For example, the type representing
     * {@code List<? extends Runnable>} returns {@code List.class}.
     */
    protected static Class<?> getRawType(Type type) {
      return Utils.getRawType(type);
    }
  }
}
