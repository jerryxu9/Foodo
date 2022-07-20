package com.example.foodo.service;

import android.util.Log;

import org.json.JSONException;
import com.example.foodo.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OKHttpService {

    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final String TAG = "OKHttpService";
    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

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
     * @throws MalformedURLException if unable to parse server URL
     */
    private static HttpUrl parseURL(String endpoint) throws MalformedURLException {
        String url = buildURL(endpoint);
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            String errorMessage = String.format("unable to parse server URL: %s", url);
            Log.d(TAG, errorMessage);
            throw new MalformedURLException(errorMessage);
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
     */
    public static void getRequest(String endpoint, Callback callbackMethod, Map<String, String> queryParameters) {
        try {
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
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
        }
    }

    /***
     * A function to modularize the setup needed for a POST Request made using the OKHttp library
     *
     *
     * @param endpoint A String containing the endpoint to make an HTTP Request to.
     *                 Format is "endpoint" rather than "/endpoint"
     * @param callbackMethod A Callback that runs once the server responds to the HTTP POST Request
     * @param bodyParams A HashMap with Strings as both keys and values.
     *                   This hashmap will get parsed into a JSON object to use as the body of the request
     */
    public static void postRequest(String endpoint, Callback callbackMethod, Map<String, String> bodyParams) {
        try {
            HttpUrl httpUrl = parseURL(endpoint);

            JSONObject paramsJSON = new JSONObject(bodyParams);
            RequestBody body = RequestBody.create(paramsJSON.toString(), JSON);
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(callbackMethod);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
        }
    }

    /***
     * A function to modularize the setup needed for a PATCH Request made using the OKHttp library
     *
     *
     * @param endpoint A String containing the endpoint to make an HTTP Request to.
     *                 Format is "endpoint" rather than "/endpoint"
     * @param callbackMethod A Callback that runs once the server responds to the HTTP PATCH Request
     * @param bodyParams A HashMap with Strings as both keys and values.
     *                   This hashmap will get parsed into a JSON object to use as the body of the request
     */
    public static void patchRequest(String endpoint, Callback callbackMethod, Map<String, String> bodyParams) {
        try {
            HttpUrl httpUrl = parseURL(endpoint);
            JSONObject paramsJSON = new JSONObject(bodyParams);
            RequestBody body = RequestBody.create(paramsJSON.toString(), JSON);
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(callbackMethod);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     *  A function to modularize the setup needed for a DELETE Request made using the OKHttp library
     *
     * @param endpoint A String containing the endpoint to make an HTTP Request to.
     *                 Format is "endpoint" rather than "/endpoint"
     * @param callbackMethod A Callback that runs once the server responds to the HTTP DELETE Request
     * @param bodyParams A HashMap with Strings as both keys and values.
     *                   This hashmap will get parsed into a JSON object to use as the body of the request
     * @param queryParameters A HashMap with Strings as both keys and values.
     *                        This hashmap maps DELETE Request query parameter names to their values
     */
    public static void deleteRequest(String endpoint, Callback callbackMethod, Map<String, String> bodyParams, Map<String, String> queryParameters) {
        try {
            HttpUrl httpUrl = parseURL(endpoint);

            HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
            for (Map.Entry<String, String> set :
                    queryParameters.entrySet()) {
                httpBuilder.addQueryParameter(set.getKey(), set.getValue());
            }

            JSONObject paramsJSON = new JSONObject(bodyParams);
            RequestBody body = RequestBody.create(paramsJSON.toString(), JSON);

            Request request = new Request.Builder()
                    .url(httpBuilder.build())
                    .delete(body)
                    .build();

            client.newCall(request).enqueue(callbackMethod);
        }
        catch (MalformedURLException exception) {
            exception.printStackTrace();
        }
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
