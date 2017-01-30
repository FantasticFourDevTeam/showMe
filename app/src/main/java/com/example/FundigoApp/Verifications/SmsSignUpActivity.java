package com.example.FundigoApp.Verifications;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.Customer.Social.Profile;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.verification.CodeInterceptionException;
import com.sinch.verification.Config;
import com.sinch.verification.IncorrectCodeException;
import com.sinch.verification.InvalidInputException;
import com.sinch.verification.PhoneNumberUtils;
import com.sinch.verification.ServiceErrorException;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SmsSignUpActivity extends AppCompatActivity {
    Spinner s;
    private String array_spinner[];
    String username;
    EditText phoneET;
    String phone_number_to_verify;
    String area;
    TextView phoneTV;
    TextView usernameTV;
    EditText usernameTE;
    Button upload_button;
    Button signup;
    ImageView customerImageView;
    TextView optionalTV;
    TextView expTV;
    static private TextView emailAddressTitle;
    static private EditText emailAddress;
    private String emailAddressValue="";
    boolean image_selected = false;
    Profile previousDataFound = null;
    private Locale locale = null;
    boolean imageSelected = false;
    boolean image_was_before = false;
    Bitmap image;
    private Profile profileParseObject;
    ProgressDialog smsDialog;
    String currentEmailAddress = "";
    private String facebookEmail="";
    private String facebookName="";
    private String facebookPicUrl="";
    private SharedPreferences sp;
    private String facebookID;
    private static Button signUpButton;
    final AccessToken accessToken = AccessToken.getCurrentAccessToken();// cancel Login to Facebook if the same email address already used


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow ().getDecorView ().setLayoutDirection (View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //forceRTLIfSupported ();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_login);
        Locale.getDefault ().getDisplayLanguage();
        array_spinner = new String[8];
        array_spinner[0] = "050";
        array_spinner[1] = "052";
        array_spinner[2] = "053";
        array_spinner[3] = "054";
        array_spinner[4] = "055";
        array_spinner[5] = "056";
        array_spinner[6] = "058";
        array_spinner[7] = "059";
        s = (Spinner) findViewById (R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
                                                                        android.R.layout.simple_spinner_item,
                                                                        array_spinner);
        s.setAdapter(adapter);

        usernameTV = (TextView) findViewById (R.id.usernameTV);
        usernameTE = (EditText) findViewById (R.id.usernameTE);
        emailAddressTitle = (TextView) findViewById (R.id.emailAddressTitle);
        emailAddress = (EditText) findViewById (R.id.emailAddress);
        phoneET = (EditText) findViewById (R.id.phoneET);
        phoneTV = (TextView) findViewById (R.id.phoneTV);
        customerImageView = (ImageView) findViewById (R.id.imageV);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        sp = PreferenceManager.getDefaultSharedPreferences(SmsSignUpActivity.this);
        facebookEmail = sp.getString(GlobalVariables.FB_EMAIL, "");
        facebookName = sp.getString(GlobalVariables.FB_NAME,"");
        facebookPicUrl = sp.getString(GlobalVariables.FB_PIC_URL,"");
        facebookID = sp.getString(GlobalVariables.FB_ID, "");

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Signup();
            }
        });

        phoneET.setOnEditorActionListener (new TextView.OnEditorActionListener () {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             if ((event != null &&
                (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                (actionId == EditorInfo.IME_ACTION_DONE)) {
                 area = s.getSelectedItem().toString();
                 phone_number_to_verify = getNumber(phoneET.getText().toString(), area);
                 if (!phoneET.getText().toString().equals("") && phoneET.getText() != null) {
                   smsDialog = new ProgressDialog(SmsSignUpActivity.this);
                   smsDialog.setTitle(getString(R.string.verification));
                   smsDialog.show();
                   smsVerify(area + phoneET.getText().toString());
               } else {
                   Toast.makeText(SmsSignUpActivity.this, getString(R.string.enter_phone_number), Toast.LENGTH_SHORT).show();
               }

              }
                return false;
            }
        });

        usernameTE.setOnEditorActionListener (new TextView.OnEditorActionListener () {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode () == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    username = usernameTE.getText().toString();
                    currentEmailAddress = emailAddress.getText().toString();
                    if (!username.equals("") && username!=null) {
                        usernameTE.setVisibility(View.INVISIBLE);
                        usernameTV.setVisibility(View.INVISIBLE);

                        if (facebookEmail==null || facebookEmail.isEmpty()) { // Show Email only if User name is Ok and if Email not set as partt of regitrtaion by Facenbook
                            emailAddressTitle.setVisibility(View.VISIBLE);
                            emailAddress.setVisibility(View.VISIBLE);
                        }
                        else{
                            emailAddressValue = facebookEmail;
                            emailAddress.setText(emailAddressValue);
                            ToSign("userName");
                        }
                    }
                    else
                    {
                        Toast.makeText(SmsSignUpActivity.this,getString(R.string.user_name_missing),Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }
        });

        emailAddress.setOnEditorActionListener (new TextView.OnEditorActionListener () {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode () == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    emailAddressValue = emailAddress.getText().toString();
                    if (!emailAddressValue.equals("") && emailAddress!=null) {
                        emailAddressTitle.setVisibility(View.INVISIBLE);
                        emailAddress.setVisibility(View.INVISIBLE);

                        if (facebookPicUrl == null || facebookPicUrl.isEmpty()) { // Only in case that no Picture from Facebook
                            customerImageView = (ImageView) findViewById(R.id.imageV);
                            customerImageView.setVisibility(View.VISIBLE);
                            upload_button = (Button) findViewById(R.id.upload_button);
                            upload_button.setVisibility(View.VISIBLE);
                            signup = (Button) findViewById(R.id.signUpButton);
                            signup.setVisibility(View.VISIBLE);
                        }
                        else {
                            ToSign("emailAddress");
                        }

                        //optionalTV = (TextView) findViewById(R.id.optionalTV);
                        //optionalTV.setVisibility(View.VISIBLE);
                    }
                    else
                        Toast.makeText(SmsSignUpActivity.this,getString(R.string.email_address_missing),Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    public void Signup() {

        Boolean setUserDone = false;
        if (previousDataFound != null) { // exist User detailes
            profileParseObject = previousDataFound;
            GlobalVariables.CUSTOMER_PHONE_NUM = previousDataFound.getNumber ();
            ParseUser.logOut ();
            try { // 29.11 - assaf updated to support both new and existing users
                //save the user in User Table as well
                ParseUser _user = ParseUser.logIn(GlobalVariables.CUSTOMER_PHONE_NUM, GlobalVariables.CUSTOMER_PHONE_NUM);
                currentEmailAddress = _user.getEmail();
                ParseACL parseUserAcl = new ParseACL();
                parseUserAcl.setPublicReadAccess(true);
                parseUserAcl.setWriteAccess(_user, true);
                _user.setACL(parseUserAcl);
                if (!currentEmailAddress.equals(emailAddress.getText().toString())) {// assaf-  update mail in User table only if email address changed
                    _user.put("email", emailAddress.getText().toString());
                    _user.save();
                }
                //save the user in Profile Table as well
                profileParseObject.setName (username);
                profileParseObject.setEmail(emailAddressValue);

                setUserDone = true;
            } catch (ParseException e) {
                e.printStackTrace ();
            }
            if (profileParseObject.getChanels () != null) {
                GlobalVariables.userChanels.addAll (profileParseObject.getChanels ());
            }
            if (!GlobalVariables.userChanels.isEmpty ()) {
                ParseInstallation installation = ParseInstallation.getCurrentInstallation ();
                installation.addAll ("Channels", (Collection<?>) GlobalVariables.userChanels);
                installation.saveInBackground ();
                for (int i = 0; i < GlobalVariables.userChanels.size (); i++) {
                    ParsePush.subscribeInBackground ("a" + GlobalVariables.userChanels.get (i));
                }
            }
        } else { // new user
            profileParseObject = new Profile ();
            ParseUser user = new ParseUser ();
            user.setUsername (area + phoneET.getText ().toString ());
            user.setPassword(area + phoneET.getText().toString());
            user.setEmail(emailAddressValue);//19.11 - assaf added

            try {
                user.signUp ();  // save the user in "User table and make email verification"
                 profileParseObject.setUser (user); //save the user in Profile Table as well
                 profileParseObject.setName (username);
                 profileParseObject.setEmail(emailAddressValue);
                 setUserDone = true; // if userSign up is successfull - assaf 19.11
           } catch (ParseException e) {
                e.printStackTrace ();
                Toast.makeText(getApplicationContext(),getString(R.string.user_exist),Toast.LENGTH_LONG).show();
                   if (accessToken!=null) {
                       cancelFacebookLogin();// In case that Login with facebook has a conflict, then closing the connection
                   }
            }
        }

        if (setUserDone) // if user signUp is ok - 19.11 - assaf
        {
           if (imageSelected) {
                customerImageView.buildDrawingCache();
                Bitmap bitmap = image;
                byte[] image;
                if (bitmap.getByteCount() > 500000) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(CompressFormat.JPEG, 100, stream);
                    image = stream.toByteArray();
                } else {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    image = stream.toByteArray();
                }
                ParseFile file = new ParseFile("picturePath", image);
                try {
                    file.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ParseACL parseAcl = new ParseACL();
                parseAcl.setPublicReadAccess(true);
                parseAcl.setPublicWriteAccess(true);
                profileParseObject.setACL(parseAcl);
                profileParseObject.put("pic", file);
            } else if (!image_was_before) {
                Bitmap bmp = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.no_image_icon_md);
                customerImageView.setImageBitmap(bmp);
                customerImageView.buildDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();
                ParseFile file = new ParseFile("picturePath", image);
                try {
                    file.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ParseACL parseAcl = new ParseACL();
                parseAcl.setPublicReadAccess(true);
                parseAcl.setPublicWriteAccess(true);
                profileParseObject.setACL(parseAcl);
                profileParseObject.put("pic", file);
            }
            profileParseObject.setNumber(area + phoneET.getText().toString());

            if (!facebookID.isEmpty()) {
                profileParseObject.setFbId(facebookID);
            }
            if (!facebookPicUrl.isEmpty()) {
                profileParseObject.setFbUrl(facebookPicUrl);
            }
            if (GlobalVariables.MY_LOCATION != null) {
                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(GlobalVariables.MY_LOCATION.getLatitude(),
                        GlobalVariables.MY_LOCATION.getLongitude());
                profileParseObject.setLocation(parseGeoPoint);
            } else {
                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(31.8971205,
                        34.8136008);
                profileParseObject.setLocation(parseGeoPoint);
            }
            try {
                profileParseObject.saveInBackground();

             if (!emailAddress.getText().toString().equals(currentEmailAddress)) {
                 Toast.makeText(getApplicationContext(), R.string.account_created_successfully_verification_mail, Toast.LENGTH_LONG).show();
             }
              else {
                 Toast.makeText(getApplicationContext(), R.string.successfully_signed_up, Toast.LENGTH_SHORT).show();
             }

                saveToFile(area + phoneET.getText().toString());
                GlobalVariables.CUSTOMER_PHONE_NUM = area + phoneET.getText().toString();
                GlobalVariables.IS_CUSTOMER_REGISTERED_USER = true;
                GlobalVariables.IS_CUSTOMER_GUEST = false;
                finish();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
        }
    }

    public String getNumber(String number, String area) {
        switch (area) {
            case "050":
                number = "97250" + number;
                break;
            case "052":
                number = "97252" + number;
                break;
            case "053":
                number = "97253" + number;
                break;
            case "054":
                number = "97254" + number;
                break;
            case "055":
                number = "97255" + number;
                break;
            case "058":
                number = "97258" + number;
                break;
            default:
                number="";
                break;
        }
        return number;
    }

    public void imageUpload(View view) {
        Intent i = new Intent (
                                      Intent.ACTION_PICK,
                                      MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult (i, GlobalVariables.SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GlobalVariables.SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
            image = FileAndImageMethods.getImageFromDevice (data, this);
            customerImageView.setImageBitmap (image);
            image_selected = true;
            imageSelected = true;
        }
    }

    public void smsVerify(String phone_number) {
        Config config = SinchVerification.config ().applicationKey("a80ac872-351d-4f45-bc84-deb3c0eb2ccc").context(getApplicationContext()).build();
        VerificationListener listener = new MyVerificationListener();
        String defaultRegion = PhoneNumberUtils.getDefaultCountryIso (this);
        String phoneNumberInE164 = PhoneNumberUtils.formatNumberToE164(phone_number, defaultRegion);
        Verification verification = SinchVerification.createSmsVerification(config, phoneNumberInE164, listener);
        verification.initiate();
        onVer(); //  //just for test, DON"T Expose THIS METHOD
    }

    class MyVerificationListener implements VerificationListener {
        @Override
        public void onInitiated() {
            // Verification initiated
        }
        @Override
        public void onInitiationFailed(Exception e) {
            if (e instanceof InvalidInputException) {
                // Incorrect number provided
                e.printStackTrace ();
            } else if (e instanceof ServiceErrorException) {
                // Sinch service error
                e.printStackTrace ();
            } else {
                // Other system error, such as UnknownHostException in case of network error
                e.printStackTrace ();
            }
        }
         @Override
        public void onVerified() {
          // Verification successful
            smsDialog.dismiss();
            usernameTV.setVisibility (View.VISIBLE);
            usernameTE.setVisibility (View.VISIBLE);
            phoneET.setVisibility (View.INVISIBLE);
            phoneTV.setVisibility (View.INVISIBLE);
            expTV = (TextView) findViewById (R.id.explanationTV);
            expTV.setVisibility (View.INVISIBLE);
            s.setVisibility (View.INVISIBLE);
       }

        @Override
        public void onVerificationFailed(Exception e) {
            if (e instanceof InvalidInputException) {
                // Incorrect number or code provided
                e.printStackTrace ();
            } else if (e instanceof CodeInterceptionException) {
                // Intercepting the verification code automatically failed, input the code manually with verify()
                e.printStackTrace ();
            } else if (e instanceof IncorrectCodeException) {
                // The verification code provided was incorrect
                e.printStackTrace ();
            } else if (e instanceof ServiceErrorException) {
                // Sinch service error
                e.printStackTrace ();
            } else {
                // Other system error, such as UnknownHostException in case of network error
                e.printStackTrace ();
            }
            smsDialog.dismiss();
            Toast.makeText(SmsSignUpActivity.this,getString(R.string.verification_failed),Toast.LENGTH_LONG).show();
        }
    }
    public void onVer () //just for test, DON"T Expose THIS METHOD - > OnVerify method need td be fix once the SMS will start back to work (24.01)
    {
        // Verification successful - assaf 23.01
        smsDialog.dismiss();
        if (facebookName == null || facebookName.isEmpty()) {// if registartrion was done not through Facebook Regitsration flow
            usernameTV.setVisibility(View.VISIBLE);
            usernameTE.setVisibility(View.VISIBLE);
            phoneET.setVisibility (View.INVISIBLE);
            phoneTV.setVisibility (View.INVISIBLE);
            expTV = (TextView) findViewById (R.id.explanationTV);
            expTV.setVisibility (View.INVISIBLE);
            s.setVisibility (View.INVISIBLE);
            getUserPreviousDetails(area + phoneET.getText().toString()); // verify if User detailes already exist
        }
        else
        {
            getUserPreviousDetails(area + phoneET.getText().toString()); // verify if User detailes already exist
        }
    }

    void saveToFile(String phone_number) {
        phone_number = phone_number + " isFundigo";
        File myExternalFile = new File (Environment.getExternalStoragePublicDirectory (Environment.DIRECTORY_DOWNLOADS), "verify.txt");
        try {
            FileOutputStream fos = new FileOutputStream (myExternalFile);
            fos.write (phone_number.getBytes ());
            fos.close ();
            Log.e ("number", phone_number);
        } catch (IOException e) {
            e.printStackTrace ();
        }

    }

    private void getUserPreviousDetails(String user_number) {
        ParseQuery<Profile> query = ParseQuery.getQuery ("Profile");
        query.whereEqualTo ("number", user_number);
        query.findInBackground(new FindCallback<Profile>() {
            public void done(List<Profile> numbers, ParseException e) {
                if (e == null) {
                    if (numbers.size() > 0) {
                        previousDataFound = numbers.get(0);
                        CustomerDetails customerDetails = UserDetailsMethod.getUserDetailsWithBitmap(numbers);
                        if (usernameTE.getText().toString().isEmpty()) {
                            usernameTE.setText(customerDetails.getCustomerName() + "");
                            usernameTE.setSelection(usernameTE.getText().length());
                        }
                        if (!image_selected) {
                            Bitmap customerImage = customerDetails.getBitmap();
                            if (customerImage != null) {
                                customerImageView.setImageBitmap(customerImage);
                                image_was_before = true;
                            }
                        }
                        if (customerDetails.getEmail() != null) {
                            emailAddress.setText(customerDetails.getEmail().toString());
                        }
                    }
                    ToSign("phoneNumber"); // In case of Facebook login Sign is immediatly
                } else {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (locale != null) {
            newConfig.locale = locale;
            Locale.setDefault (locale);
            getBaseContext ().getResources ().updateConfiguration (newConfig, getBaseContext ().getResources ().getDisplayMetrics ());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (accessToken!=null) {
            cancelFacebookLogin();
        }
           // Get the Facebook Object and disconnect from Facebook in case the Access token was created.
    }

    private void ToSign(String parameter)
    {
        switch (parameter) {
            case "phoneNumber":
                if (!facebookName.isEmpty() && !facebookEmail.isEmpty()) {

                    username = facebookName;
                    emailAddressValue = facebookEmail;
                    Signup();
                }
                break;

            case "userName":
                  if (!facebookEmail.isEmpty()){
                      emailAddressValue = facebookEmail;
                      Signup();
                  }

                break;

            case "emailAddrees":
                if (!facebookPicUrl.isEmpty())
                    Signup();
                break;
         }
    }


      private void cancelFacebookLogin()
      {
          new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback () {
              @Override
              public void onCompleted(GraphResponse graphResponse) {
                  LoginManager.getInstance().logOut();
                  Toast.makeText(getApplicationContext(), R.string.loged_out_of_facebook, Toast.LENGTH_SHORT).show();
                 // 24.01- assaf: remove Facebook items from Shard prefrencess
                  sp.edit().putString(GlobalVariables.FB_PIC_URL, "").commit();
                  sp.edit().putString(GlobalVariables.FB_NAME, "").commit();
                  sp.edit().putString(GlobalVariables.FB_EMAIL, "").commit();
                  sp.edit().putString(GlobalVariables.FB_ID, "").commit();
                  finish();
              }


          }).executeAsync();

      }

         ///CLEAN SP
    }
