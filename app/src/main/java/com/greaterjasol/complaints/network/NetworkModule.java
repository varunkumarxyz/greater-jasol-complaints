package com.greaterjasol.complaints.network;

import android.util.Log;

import com.greaterjasol.complaints.data.TokenStore;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkModule {
    private static TokenProvider tokenProvider;

    public interface TokenProvider { String getToken(); }

    public static void setTokenProvider(TokenProvider p) { tokenProvider = p; }

    private static OkHttpClient buildClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) { Log.d("OkHttp", message); }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(logging);
        builder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws java.io.IOException {
                Request original = chain.request();
                Request.Builder rb = original.newBuilder();
                if (tokenProvider != null) {
                    String t = tokenProvider.getToken();
                    if (t != null && !t.isEmpty()) rb.header("Authorization", "Bearer " + t);
                }
                Request req = rb.build();
                return chain.proceed(req);
            }
        });
        return builder.build();
    }

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://example-backend.local/")
            .client(buildClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService api = retrofit.create(ApiService.class);
}
