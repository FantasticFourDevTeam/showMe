package com.example.events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ActivityFacebook extends AppCompatActivity {

    LoginButton facebook_login_button;
    CallbackManager callbackManager;
    LoginButton facebook_logout_button;
    TextView facebookUserNameView;
    ImageView profileFacebookPictureView;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);
        context = this;
        facebook_login_button = (LoginButton) findViewById (R.id.login_button11);
        facebook_logout_button = (LoginButton) findViewById (R.id.logout_button11);
        facebookUserNameView = (TextView) findViewById (R.id.profileUserName);
        profileFacebookPictureView = (ImageView) findViewById (R.id.faebook_profile);

        final AccessToken accessToken = AccessToken.getCurrentAccessToken ();
        if (accessToken != null) {
            facebook_login_button.setVisibility (View.GONE);
            profileFacebookPictureView.setVisibility (View.VISIBLE);
            facebookUserNameView.setVisibility (View.VISIBLE);
            facebook_logout_button.setVisibility (View.VISIBLE);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences (ActivityFacebook.this);
            String name = sp.getString (GlobalVariables.FB_NAME, null);
            String pic_url = sp.getString (GlobalVariables.FB_PIC_URL, null);
            Picasso.with (context).load (pic_url).into (profileFacebookPictureView);
            facebookUserNameView.setText (name);
        } else {
            facebook_login_button.setVisibility (View.VISIBLE);
            facebook_logout_button.setVisibility (View.GONE);
            profileFacebookPictureView.setVisibility (View.GONE);
            facebookUserNameView.setVisibility (View.GONE);
        }

        callbackManager = CallbackManager.Factory.create ();
        facebook_login_button.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance ().
                        logInWithReadPermissions
                                (ActivityFacebook.this,
                                        Arrays.asList
                                                ("public_profile",
                                                        "user_friends",
                                                        "email"));
            }
        });
        // Callback registration
        facebook_login_button.registerCallback (callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                accessToken.setCurrentAccessToken (loginResult.getAccessToken ());
                getUserDetailsFromFB ();
                facebook_login_button.setVisibility (View.GONE);
                facebook_logout_button.setVisibility (View.VISIBLE);
                profileFacebookPictureView.setVisibility (View.VISIBLE);
                facebookUserNameView.setVisibility (View.VISIBLE);
            }

            @Override
            public void onCancel() {
                Toast.makeText (context, R.string.canceled_logging_facebook, Toast.LENGTH_SHORT).show ();

            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText (context, R.string.error_logging_facebook, Toast.LENGTH_SHORT).show ();
                Log.e("error_logging_facebook", exception.getMessage());
                exception.printStackTrace ();
            }
        });

    }

    private void getUserDetailsFromFB() {
        Bundle parameters = new Bundle ();
        parameters.putString ("fields", "email,name,picture,link");
        new GraphRequest(
                AccessToken.getCurrentAccessToken (),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback () {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject picture = response.getJSONObject ().getJSONObject ("picture");
                            JSONObject data = picture.getJSONObject ("data");
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences (ActivityFacebook.this);
                            SharedPreferences.Editor editor = sp.edit ();
                            editor.putString (GlobalVariables.FB_NAME, response.getJSONObject ().getString ("name"));
                            editor.putString (GlobalVariables.FB_PIC_URL, data.getString ("url"));
                            editor.putString (GlobalVariables.FB_ID, response.getJSONObject ().getString ("id"));
                            editor.apply ();
                            Picasso.with (context).load (data.getString ("url")).into (profileFacebookPictureView);
                            facebookUserNameView.setText (response.getJSONObject ().getString ("name"));
                        } catch (JSONException e) {
                            e.printStackTrace ();
                        }
                    }
                }
        ).executeAsync ();
    }

    public void logOutFacebook(View view) {
        new GraphRequest (AccessToken.getCurrentAccessToken (), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback () {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance ().logOut ();
                Toast.makeText (context, R.string.loged_out_of_facebook, Toast.LENGTH_SHORT).show ();
                facebook_login_button.setVisibility (View.VISIBLE);
                facebook_logout_button.setVisibility (View.GONE);
                profileFacebookPictureView.setVisibility (View.GONE);
                facebookUserNameView.setVisibility (View.GONE);
            }
        }).executeAsync ();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
        callbackManager.onActivityResult (requestCode, resultCode, data);
    }
}
