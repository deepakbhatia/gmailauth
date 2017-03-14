package com.optimalcities.gmailauthenticationapi;

import android.os.AsyncTask;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by obelix on 13/03/2017.
 */

public class GmailApiAuthTask extends AsyncTask<String, Void, JSONObject> {

    private GmailAuthActivity activity;
    public GmailApiAuthTask(){

    }

    public GmailApiAuthTask(GmailAuthActivity activity){
        this.activity = activity;
    }

    @Override
    protected JSONObject doInBackground(String... tokens) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v3/userinfo")
                .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String jsonBody = response.body().string();
            //Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
            return new JSONObject(jsonBody);
        } catch (Exception exception) {
            //Log.w(LOG_TAG, exception);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject userInfo) {
        if (userInfo != null && !userInfo.has("error")) {

            String fullName = userInfo.optString("name", null);
            String givenName = userInfo.optString("given_name", null);
            String familyName = userInfo.optString("family_name", null);
            String imageUrl = userInfo.optString("picture", null);

            GmailAuthActivity.gmailUserProfileModel = new GmailUserProfileModel(
                    userInfo.optString("email", null),
                    true,
                    imageUrl,
                    familyName,
                    givenName,
                    fullName);

            GmailAuthActivity.binding.setGmailuser(GmailAuthActivity.gmailUserProfileModel);


            EventBus.getDefault().post(new PersistUserMessage(userInfo));


            String message;
            /*if (userInfo.has("error")) {
                message = String.format("%s [%s]", GmailAuthActivity.getString(R.string.request_failed), userInfo.optString("error_description", "No description"));
            } else {
                message = getString(R.string.request_complete);
            }
            Snackbar.make(mProfileView, message, Snackbar.LENGTH_SHORT)
                    .show();*/
        }
        else{
            EventBus.getDefault().post(new UserSignInError().errorMessage = activity.getString(R.string.request_failed));
        }
    }
}
