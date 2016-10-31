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

import java.io.IOException;
import okhttp3.Request;

/**
 * 网络请求
 * @param <T> Successful response body type.
 */
public interface Call<T> extends Cloneable {
  /**
   * 发送同步请求
   *
   * @throws IOException if a problem occurred talking to the server.
   * @throws RuntimeException (and subclasses) if an unexpected error occurs creating the request
   * or decoding the response.
   */
  Response<T> execute() throws IOException;

  /**
   * 发送异步请求
   */
  void enqueue(Callback<T> callback);

  /**
   * 该请求是否已执行，一个请求只能执行一次，多次执行可通过clone()一个新请求
   */
  boolean isExecuted();

  /**
   * 取消请求
   */
  void cancel();

  boolean isCanceled();

  Call<T> clone();

  /** The original HTTP request. */
  Request request();
}
