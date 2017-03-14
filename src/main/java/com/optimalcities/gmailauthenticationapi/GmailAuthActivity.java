package com.optimalcities.gmailauthenticationapi;

/**
 * Created by obelix on 12/03/2017.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.optimalcities.gmailauthenticationapi.databinding.ActivityGmailauthBinding;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class GmailAuthActivity extends AppCompatActivity {

    private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
    private static final String AUTH_STATE = "AUTH_STATE";
    private static final String USED_INTENT = "USED_INTENT";
    private static final String LOG_TAG = "GmailAuthActivity";
    private static final String USERINFO = "USER_INFO";

    // state
    AuthState mAuthState;

    public static ActivityGmailauthBinding binding;

    public static GmailUserProfileModel gmailUserProfileModel = new GmailUserProfileModel();

    static AuthorizationService authorizationService = null;

    public  GmailConnectHandlers gmailConnectHandlers = new GmailConnectHandlers();

    private GmailAuthActivity gmailAuthActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gmailAuthActivity =this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_gmailauth);

        binding.setHandlers(gmailConnectHandlers);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        if(authorizationService == null)
           authorizationService = new AuthorizationService(getApplicationContext());

        mAuthState = restoreAuthState();
        if (mAuthState != null && mAuthState.isAuthorized()) {
            String authUserJson = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .getString(USERINFO, null);
            if(authUserJson!=null)
                Log.d(LOG_TAG,authUserJson);

        }

        enablePostAuthorizationFlows();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.optimalcities.gmailauthenticationapi.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIntent(getIntent());

        EventBus.getDefault().register(this);//Register for Events
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void signOutFlow(){
        clearUserInfo();

    }
    private void enablePostAuthorizationFlows() {
        mAuthState = restoreAuthState();
        if (mAuthState != null && mAuthState.isAuthorized()) {


            requestApi(this,mAuthState,authorizationService);

        } else {

        }
    }

    /**
     * Exchanges the code, for the {@link TokenResponse}.
     *
     * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
     */
    private void handleAuthorizationResponse(@NonNull Intent intent) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);

        if (response != null) {
            Log.i(LOG_TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
            authorizationService.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                    if (exception != null) {
                        Log.w(LOG_TAG, "Token Exchange failed", exception);
                    } else {
                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
                            persistAuthState(authState);

                            SendTokenTask sendTokenTask = new SendTokenTask();

                            boolean tokenActive = true;

                            sendTokenTask.execute(tokenResponse.toString(), String.valueOf(tokenActive));
                            //tokenResponse
                            Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s, Referesh Token %s ]", tokenResponse.accessToken, tokenResponse.idToken, tokenResponse.refreshToken));
                        }
                    }
                }
            });
        }
    }

    //Store Token Data
    private void persistAuthState(@NonNull AuthState authState) {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(AUTH_STATE, authState.toJsonString())
                .commit();
        enablePostAuthorizationFlows();
    }

    //Clear Token Data
    private void clearAuthState() {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(AUTH_STATE)
                .apply();
    }

    //Restore Authentication State Tokens
    @Nullable
    private AuthState restoreAuthState() {
        String jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(AUTH_STATE, null);
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return AuthState.fromJson(jsonString);
            } catch (JSONException jsonException) {
                // should never happen
            }
        }
        return null;
    }

    private  void requestApi(    GmailAuthActivity mMainActivity,
             AuthState mAuthState,
             AuthorizationService mAuthorizationService){
        mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {


                GmailApiAuthTask authTask = new GmailApiAuthTask(gmailAuthActivity);

                authTask.execute(accessToken);
            }
        });
    }

    //Remove Signed in User from Device when User Signs Out
    private void clearUserInfo() {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(USERINFO)
                .apply();
    }

    //Save Signed in User to Device

    public  void setUserInfo(JSONObject userInfo){

        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(USERINFO, userInfo.toString())
                .commit();

    }

    //Receives Event When a New User Signs in
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewUserEvent(PersistUserMessage event) {

        setUserInfo( event.userInfo);
    };

    public class GmailConnectHandlers {

        public void onAuthorizeClick(View view) {
            AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                    Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
                    Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
            );

            String clientId = "110547839860-vn9monhug2q52kc9b71f6cror79visu5.apps.googleusercontent.com";
            Uri redirectUri = Uri.parse("com.optimalcities.gmailauthenticationapi:/oauth2callback");
            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                    serviceConfiguration,
                    clientId,
                    AuthorizationRequest.RESPONSE_TYPE_CODE,
                    redirectUri
            );
            HashMap addParam = new HashMap();

            addParam.put("access_type","offline");
            builder.setAdditionalParameters(addParam);
            builder.setScopes(new String[]{"EMAIL","https://www.googleapis.com/auth/gmail.readonly"});
            AuthorizationRequest request = builder.build();

            AuthorizationService authorizationService = new AuthorizationService(view.getContext());

            String action = "com.optimalcities.gmailauthenticationapi.HANDLE_AUTHORIZATION_RESPONSE";
            Intent postAuthorizationIntent = new Intent(action);
            PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), request.hashCode(), postAuthorizationIntent, 0);
            authorizationService.performAuthorizationRequest(request, pendingIntent);
        }

        public void onSignOutClick(View view) {

            mAuthState = null;
            clearAuthState();
            //enablePostAuthorizationFlows();

            Log.d(LOG_TAG,"Sign Out Click");

            signOutFlow();
            gmailUserProfileModel.changeStatus(false);
        }
    }

}