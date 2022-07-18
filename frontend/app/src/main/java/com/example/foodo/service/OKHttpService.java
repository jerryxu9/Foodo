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
        // Empty constructor for now... Feel free to add on more.
    }

    /***
     *
     * @param endpoint: A String containing the endpoint to make an HTTP Request to.
     *                  Format is "endpoint" rather than "/endpoint".
     * @return A String containing the full URL
     */
    private static String buildURL(String endpoint) {
        return BASE_URL + "/" + endpoint;
    }

    /***
     *
     * @param endpoint A String containing the endpoint to make an HTTP Request to.
     *                 Format is "endpoint" rather than "/endpoint".
     * @return An HttpUrl object that represents the full URL
     * @throws Exception if unable to parse server URL
     */
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

    /***
     * A function to modularize the setup needed for a GET Request made using the OKHttp library
     *
     *
     * @param endpoint A String containing the endpoint to make an HTTP Request to.
     *                 Format is "endpoint" rather than "/endpoint"
     * @param callbackMethod A Callback that runs once the server responds to the HTTP GET Request
     * @param queryParameters A HashMap with Strings as both keys and values.
     *                        This hashmap maps GET Request query parameter names to their values
     * @throws Exception if unable to parse server URL
     */
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

    /***
     * A method to return the response body as a String
     *
     * @param response An OKHttp Requests' response
     * @return The response body as a String
     * @throws IOException if response is unsuccessful or response body is null
     */
    public static String getResponseBody(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        checkResponseIsSuccessful(response, responseBody);
        return responseBody.string();
    }

    /**
     * @param response     An OKHttp Requests' response
     * @param responseBody An OKHttp Requests' response body
     * @throws IOException if response is unsuccessful or response body is null
     */
    private static void checkResponseIsSuccessful(Response response, ResponseBody responseBody) throws IOException {
        if (!response.isSuccessful() || responseBody == null) {
            throw new IOException(String.format("Unexpected code %s", response));
        }
    }
}
