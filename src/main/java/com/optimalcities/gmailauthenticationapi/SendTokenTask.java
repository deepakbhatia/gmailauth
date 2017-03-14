package com.optimalcities.gmailauthenticationapi;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by obelix on 10/03/2017.
 */

public class SendTokenTask extends AsyncTask<String, Boolean, Boolean> {
    private Map<String,String> params = new HashMap<>();
    @Override
    protected Boolean doInBackground(String... strings) {
        Long result = 0L;
        params.put("tokenResponse",strings[0]);
        params.put("tokenActive",strings[1]);
        try{
            URL url = new URL("https://friendlyscoreapi.com");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            String input = new Gson().toJson(params);
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            os.close();
             if(conn.getResponseCode()!=200)
                return false;
        } catch (MalformedURLException x) {
            Log.e("Error in Url", "Using Sample FriendlyScore URL");
            return false;
        }
        catch (IOException ex){
            return false;
        }
        return true;
    }
}
