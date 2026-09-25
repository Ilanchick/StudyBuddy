package com.example.studybuddy.repository;

import android.content.Context;
import com.example.studybuddy.data.NetworkService;
import com.example.studybuddy.model.GeminiRequest;

import com.example.studybuddy.model.GeminiResponse;
import com.google.firebase.appcheck.interop.BuildConfig;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.Arrays;

public class GeminiRepository{

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/";
    private final String apiKey;
    private final NetworkService networkService;
    private final Gson gson;

    public GeminiRepository(Context context) {


        apiKey = context.getString(
                com.example.studybuddy.R.string.gemini_api_key
        );

        HttpLoggingInterceptor loggingInterceptor =
                new HttpLoggingInterceptor();

        loggingInterceptor.setLevel(
                HttpLoggingInterceptor.Level.BODY
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build();

        networkService = retrofit.create(NetworkService.class);
        gson = new Gson();
    }

    public void generateFlashcards(
            String topic,
            GeminiCallback callback) {

        String prompt =
                "Create 5 simple flashcards about: " + topic;

        GeminiRequest.Part part =
                new GeminiRequest.Part(prompt);

        GeminiRequest.Content content =
                new GeminiRequest.Content(
                        Arrays.asList(part)
                );

        GeminiRequest request =
                new GeminiRequest(
                        Arrays.asList(content)
                );

        String jsonRequest = gson.toJson(request);

        String url =
                "v1beta/models/gemini-2.5-flash:generateContent";

        networkService.generateContent(
                apiKey,
                request
        ).enqueue(new Callback<GeminiResponse>() {

            @Override
            public void onResponse(
                    Call<String> call,
                    Response<String> response) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    callback.onSuccess(response.body());

                } else {

                    callback.onError(
                            "API error: HTTP "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<String> call,
                    Throwable t) {

                callback.onError(
                        "Network error: "
                                + t.getMessage()
                );
            }
        });
    }

    public interface GeminiCallback {

        void onSuccess(String response);

        void onError(String error);
    }
}