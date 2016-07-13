package com.example.FundigoApp.Customer.CustomerMenu;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class CustomerProfileUpdate extends AppCompatActivity {
    String customer;
    EditText customerName;
    ImageView customerImg;
    boolean IMAGE_SELECTED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_customer_profile_update);
        customerName = (EditText) findViewById (R.id.userEdit);
        customerImg = (ImageView) findViewById (R.id.customerImage);
        getCurrentUserProfile ();
    }

    public void updateProfile(View view) {
        customer = customerName.getText ().toString ();
        byte[] imageToUpdate;
        List<ParseObject> list;
        if (!customer.isEmpty () || IMAGE_SELECTED) {
            String _userPhoneNumber = GlobalVariables.CUSTOMER_PHONE_NUM;
            if (!_userPhoneNumber.isEmpty ()) {
                try {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery ("Profile");
                    query.whereEqualTo ("number", _userPhoneNumber);
                    list = query.find ();
                    for (ParseObject obj : list) {
                        obj.put ("name", customer);
                        if (IMAGE_SELECTED) {
                            imageToUpdate = imageUpdate ();
                            ParseFile picFile = new ParseFile (imageToUpdate);
                            obj.put ("pic", picFile);
                        }
                        obj.save ();
                        finish ();
                    }
                } catch (Exception e) {
                    Log.e ("Exception catch", e.toString ());
                }
            } else {
                Toast.makeText (getApplicationContext (),
                                       R.string.user_may_not_registered_or_not_exist,
                                       Toast.LENGTH_SHORT).show ();
            }
            if (!customer.isEmpty ())
                Toast.makeText (getApplicationContext (),
                                       R.string.user_updated_and_now_it_is,
                                       Toast.LENGTH_SHORT).show ();
            else
                Toast.makeText (getApplicationContext (),
                                       R.string.picture_updated,
                                       Toast.LENGTH_SHORT).show ();
        } else
            Toast.makeText (getApplicationContext (),
                                   R.string.nothing_selected_to_update,
                                   Toast.LENGTH_SHORT).show ();
    }

    public void imageUpload(View view) {
         String str[] = new String[] { "Camera", "Gallery","Facebook" };
         new AlertDialog.Builder(CustomerProfileUpdate.this).setTitle("Choose Your Photo").setItems(str,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i;
                        Log.e("int click" ,""+which);
                        switch (which)
                        {
                            case 0:
                                i = new Intent ("android.media.action.IMAGE_CAPTURE");
                                startActivityForResult(i,GlobalVariables.SELECT_PICTURE);
                                break;
                            case 1:
                                 i = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                if (i.resolveActivity (getPackageManager ()) != null) {
                                    startActivityForResult (i, GlobalVariables.SELECT_PICTURE);
                                }
                                break;
                            case 2:
                                if(Profile.getCurrentProfile()!=null)
                                {
                                    //String id = GlobalVariables.FB_ID;
                                    Bundle parameters = new Bundle ();
                                    parameters.putString ("fields", "email,name,picture,link");
                                    new GraphRequest(
                                            AccessToken.getCurrentAccessToken(),
                                            /*"/"+id+"/picture"*/"/me",
                                            parameters,
                                            HttpMethod.GET,
                                            new GraphRequest.Callback() {
                                                public void onCompleted(GraphResponse response) {
                                            /* handle the result */
                                                    try {
                                                        JSONObject picture = response.getJSONObject ().getJSONObject ("picture");
                                                        JSONObject data = picture.getJSONObject ("data");
                                                        Picasso.with (CustomerProfileUpdate.this).load (data.getString ("url")).into (customerImg);
                                                        IMAGE_SELECTED =true;
                                                        Toast.makeText(CustomerProfileUpdate.this,"finish",Toast.LENGTH_SHORT).show();
                                                    } catch (JSONException e) {
                                                        //e.printStackTrace();
                                                        Toast.makeText(CustomerProfileUpdate.this,"Not Success try agian",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                    ).executeAsync();
                                }
                                else
                                {
                                    Toast.makeText(CustomerProfileUpdate.this,"You need to login facebook via fundigo",Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                }).show();

    }

    public byte[] imageUpdate() {
        byte[] image;
        try {
            customerImg.buildDrawingCache ();
            Bitmap bitmap = customerImg.getDrawingCache ();
            ByteArrayOutputStream stream = new ByteArrayOutputStream ();
            bitmap.compress (Bitmap.CompressFormat.JPEG, 100, stream);
            image = stream.toByteArray ();
            return image;
        } catch (Exception e) {
            Log.e ("Exceptpion in In Image", e.toString ());
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == GlobalVariables.SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
                Bitmap image = FileAndImageMethods.getImageFromDevice (data, this);
                customerImg.setImageBitmap (image);
                customerImg.setVisibility (View.VISIBLE);
                IMAGE_SELECTED = true;
            }
        } catch (Exception e) {
            Log.e ("On ActivityResult Error", e.toString ());
        }
    }

    public void getCurrentUserProfile() {
        String phoneNum = GlobalVariables.CUSTOMER_PHONE_NUM;
        CustomerDetails customerDetails = UserDetailsMethod.getUserDetailsFromParseInMainThreadWithBitmap (phoneNum);
        String currentUserName = customerDetails.getCustomerName ();
        if(customerName.getText ().toString ().isEmpty ()){
            customerName.setText (currentUserName);
            customerName.setSelection (customerName.getText ().length ());
        }
        Bitmap userImage = customerDetails.getBitmap ();
        if(userImage != null && !IMAGE_SELECTED){
            customerImg.setImageBitmap (userImage);
            customerImg.setVisibility (View.VISIBLE);
        }
    }
}
