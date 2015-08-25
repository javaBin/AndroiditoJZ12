package com.google.android.apps.iosched.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.iosched.io.model.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import no.java.schedule.io.model.JZFeedback;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RestServiceDevNull {
    private static RestServiceDevNull instance = null;
    private RestDevApi restDevApi = null;
    private Activity activity = null;

    private RestServiceDevNull(String mode) {
        //TODO
        String endPoint = null;
        if(mode.equals("TEST")) {
         endPoint = Constants.SESSION_FEEDBACK_WEB_URI_TEST;
        }

        RestAdapter movieAPIRest = new RestAdapter.Builder()
                .setEndpoint(endPoint)
                .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
                .build();
        restDevApi = movieAPIRest.create(RestDevApi.class);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public static RestServiceDevNull getInstance(String mode, Activity activity) {
        if (instance == null) {
            instance = new RestServiceDevNull(mode);
        }
        instance.setActivity(activity);
        return instance;
    }

    public void submitFeedbackToDevNull(String eventId, String sessionId, String voterId, JZFeedback feedbackBody) {
        restDevApi.postSessionFeedback(eventId, sessionId, voterId, feedbackBody, retrofitCallBack);
    }

    public void submitFeedbackTestToDevNull(String voterId, JZFeedback feedbackBody) {
        restDevApi.postSessionFeedbackTest(voterId, feedbackBody, retrofitCallBack);
    }

    public Callback retrofitCallBack = new Callback() {

        @Override
        public void success(Object o, Response response) {
            if (o instanceof JsonElement) {
                JsonObject jsonObj = ((JsonElement)o).getAsJsonObject();
                String strObj = jsonObj.toString();
                Toast.makeText(activity,
                        "Thank you for the feedback!",
                        Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(activity,
                    "Something went wrong with the connection, please try again!",
                    Toast.LENGTH_SHORT).show();
        }
    };
}