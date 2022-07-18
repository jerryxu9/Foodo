package com.example.foodo.service;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OKHttpService {

    private static final String BASE_URL = "http://20.51.215.223:3000";
    private static final String TAG = "OKHttpService";

    private static final OkHttpClient client = new OkHttpClient();

    public OKHttpService() {
    }

    private static String buildURL(String endpoint) {
        return BASE_URL + endpoint;
    }

    private static HttpUrl parseURL(String endpoint) throws Exception {
        String url = buildURL(endpoint);
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            String errorMessage = String.format("unable to parse server URL: %s", url);
            Log.d(TAG, errorMessage);
            throw new Exception(errorMessage);
        }
        return httpUrl;
    }


    public static void getRequest(String endpoint, Callback callbackMethod, Map<String, String> queryParameters) throws Exception {
        HttpUrl httpUrl = parseURL(endpoint);
        HttpUrl.Builder httpBuilder = httpUrl.newBuilder();

        for (Map.Entry<String, String> set :
                queryParameters.entrySet()) {
            httpBuilder.addQueryParameter(set.getKey(), set.getValue());
        }

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();
        client.newCall(request).enqueue(callbackMethod);
    }


    public static String getResponseBody(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (!response.isSuccessful() || responseBody == null) {
            throw new IOException(String.format("Unexpected code %s", response));
        }
        return responseBody.string();
    }
}
