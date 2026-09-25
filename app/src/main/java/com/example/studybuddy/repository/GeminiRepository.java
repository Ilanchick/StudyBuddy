package com.example.studybuddy.repository;

import android.content.Context;
import com.example.studybuddy.data.NetworkService;
import com.example.studybuddy.model.GeminiRequest;
import com.example.studybuddy.model.GeminiResponse;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Arrays;

public class GeminiRepository {

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

        // Only log full request/response bodies in debug builds —
        // your API key travels in these requests, so BODY logging
        // in a release build would leak it to logcat.
        loggingInterceptor.setLevel(
                com.example.studybuddy.BuildConfig.DEBUG
                        ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.NONE
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        gson = new Gson();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        networkService = retrofit.create(NetworkService.class);
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

        networkService.generateContent(
                apiKey,
                request
        ).enqueue(new Callback<GeminiResponse>() {

            @Override
            public void onResponse(
                    Call<GeminiResponse> call,
                    Response<GeminiResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    String text = extractText(response.body());

                    if (text != null) {
                        callback.onSuccess(text);
                    } else {
                        callback.onError(
                                "Empty or unexpected response format"
                        );
                    }

                } else {

                    callback.onError(
                            "API error: HTTP "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<GeminiResponse> call,
                    Throwable t) {

                callback.onError(
                        "Network error: "
                                + t.getMessage()
                );
            }
        });
    }

    /**
     * Pulls the generated text out of a GeminiResponse.
     * Adjust the getter names below to match your actual
     * GeminiResponse model class.
     */
    private String extractText(GeminiResponse response) {
        try {
            return response
                    .getCandidates().get(0)
                    .getContent()
                    .getParts().get(0)
                    .getText();
        } catch (Exception e) {
            return null;
        }
    }

    public interface GeminiCallback {

        void onSuccess(String response);

        void onError(String error);
    }
}