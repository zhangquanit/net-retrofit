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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import okhttp3.Request;

final class ExecutorCallAdapterFactory extends CallAdapter.Factory {
    final Executor callbackExecutor; //默认为Platform.MainThreadExecutor

    ExecutorCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * @param returnType  :返回值类型 比如Call<?>
     * @param annotations :方法上的注解，比如@Post
     */
    @Override
    public CallAdapter<Call<?>> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) { //返回值类型必须是Call
            return null;
        }
        final Type responseType = Utils.getCallResponseType(returnType); //具体的返回值,Call<?>中的泛型参数？
        return new CallAdapter<Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            // 适配Call
            @Override
            public <R> Call<R> adapt(Call<R> call) {
                return new ExecutorCallbackCall<>(callbackExecutor, call); //call =OkHttpCall
            }
        };
    }

    static final class ExecutorCallbackCall<T> implements Call<T> {
        final Executor callbackExecutor; // Callback执行器
        final Call<T> delegate; //委托的网络请求，比如OkHttpCall

        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void enqueue(final Callback<T> callback) {
            if (callback == null) throw new NullPointerException("callback == null");

            delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, final Response<T> response) {
                    //Callback执行器
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (delegate.isCanceled()) {
                                  // 被取消的请求通过onFailure 回调
                                callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                            } else {
                                callback.onResponse(ExecutorCallbackCall.this, response);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, final Throwable t) {
                    //Callback执行器
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(ExecutorCallbackCall.this, t);
                        }
                    });
                }
            });
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public Response<T> execute() throws IOException {
            return delegate.execute();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }
    }
}
