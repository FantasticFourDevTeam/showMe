package com.example.FundigoApp.Customer.CustomerMenu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class CustomerProfileUpdate extends AppCompatActivity {
    String customer;
    EditText customerName;
    ImageView customerImg;
    EditText emailAddressText;
    String emailValue;
    boolean IMAGE_SELECTED = false;
    ProgressDialog dialog;
    private static Bitmap imageToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile_update);
        customerName = (EditText) findViewById (R.id.userEdit);
        customerImg = (ImageView) findViewById (R.id.customerImage);
        emailAddressText =  (EditText)findViewById(R.id.emailValue);
        getCurrentUserProfile();
    }

    public void updateProfile(View view) {
        customer = customerName.getText ().toString ();
        emailValue = emailAddressText.getText().toString();
        boolean emailChanged= false;
        byte[] imageToUpdate;
        List<ParseObject> listProfile;
        List<ParseUser> listUser;
        ParseACL parseAcl = new ParseACL();
        parseAcl.setPublicReadAccess(true);
        parseAcl.setPublicWriteAccess(true);

        if ((!customer.equals("")&& !emailValue.equals("")) || IMAGE_SELECTED ) {
            String _userPhoneNumber = GlobalVariables.CUSTOMER_PHONE_NUM;
            try {
                if (!_userPhoneNumber.isEmpty()) {

                    ParseQuery<ParseObject> queryProfile = ParseQuery.getQuery("Profile");
                    queryProfile.whereEqualTo("number", _userPhoneNumber); // assaf 20.11 - update profile Table
                    listProfile = queryProfile.find();
                    for (ParseObject obj : listProfile) {
                        obj.setACL(parseAcl);
                        obj.put("name", customer);
                        obj.put("email", emailValue);
                        if (IMAGE_SELECTED) {
                            imageToUpdate = imageUpdate();
                            ParseFile picFile = new ParseFile(imageToUpdate);
                            obj.put("pic", picFile);
                        }
                        obj.saveInBackground();
                    }

                    ParseQuery<ParseUser> queryUser = ParseUser.getQuery(); // 20.1 - assaf update User table with email only
                    queryUser.whereEqualTo("username", _userPhoneNumber);
                    listUser = queryUser.find();

                    for (ParseUser obj1 : listUser) {
                        ParseUser _user = ParseUser.logIn(_userPhoneNumber, _userPhoneNumber);
                        String currentEmail = _user.getEmail();
                        ParseACL parseUserAcl = new ParseACL();
                        parseUserAcl.setReadAccess(_user, true);
                        parseUserAcl.setWriteAccess(_user, true);
                        _user.setACL(parseUserAcl);
                        if (!currentEmail.equals(emailValue)) {// assaf-  update mail in User table only if email address changed
                            _user.put("email", emailValue);
                            _user.saveInBackground();
                            emailChanged = true;
                        }
                        finish();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            R.string.user_may_not_registered_or_not_exist,
                            Toast.LENGTH_SHORT).show();
                }
                if (!customer.isEmpty())
                    Toast.makeText(getApplicationContext(),
                            R.string.user_updated_and_now_it_is,
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),
                            R.string.picture_updated,
                            Toast.LENGTH_SHORT).show();
                if (emailChanged) {
                    Toast.makeText(getApplicationContext(),
                            R.string.email_updated, Toast.LENGTH_LONG).show();
                }
            }

            catch(ParseException ex1)
            {
                ex1.printStackTrace();
            }

            catch(Exception e){
                e.printStackTrace();
            }

            catch(OutOfMemoryError ex)
            {
                ex.printStackTrace();
            }

        }
        else
            Toast.makeText (getApplicationContext (),
                                   R.string.nothing_selected_to_update,
                                   Toast.LENGTH_SHORT).show ();
    }

    public void imageUpload(View view) {

        try {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(i, GlobalVariables.SELECT_PICTURE);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        catch (OutOfMemoryError ex1)
        {
            ex1.printStackTrace();
        }
    }

    public byte[] imageUpdate() {
        byte[] image;
        try {
            //customerImg.buildDrawingCache ();
            //Bitmap bitmap = customerImg.getDrawingCache ();
            Bitmap bitmap = imageToUpdate;//24.10 assaf updated tro get better picture view
            ByteArrayOutputStream stream = new ByteArrayOutputStream ();
            bitmap.compress (Bitmap.CompressFormat.JPEG, 100, stream);
            image = stream.toByteArray ();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        catch (OutOfMemoryError ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == GlobalVariables.SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
                imageToUpdate = FileAndImageMethods.getImageFromDevice (data, this);
                customerImg.setImageBitmap (imageToUpdate);
                customerImg.setVisibility (View.VISIBLE);
                IMAGE_SELECTED = true;
            }
        } catch (Exception e) {
            Log.e ("On ActivityResult Error", e.toString ());
        }
        catch (OutOfMemoryError ex)
        {
            ex.printStackTrace();
        }
    }

    public void getCurrentUserProfile() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading..");
        try {
            String phoneNum = GlobalVariables.CUSTOMER_PHONE_NUM;
            CustomerDetails customerDetails = UserDetailsMethod.getUserDetailsFromParseInMainThreadWithBitmap(phoneNum);
            String currentUserName = customerDetails.getCustomerName();
            String emailAddress = customerDetails.getEmail();
            if (customerName.getText().toString().isEmpty()) {
                customerName.setText(currentUserName);
                customerName.setSelection(customerName.getText().length());
            }
            Bitmap userImage = customerDetails.getBitmap();
            if (userImage != null && !IMAGE_SELECTED) {
                customerImg.setImageBitmap(userImage);
                customerImg.setVisibility(View.VISIBLE);
            }

            if (customerDetails.getEmail() != null) {
                emailAddressText.setText(emailAddress.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError ex)
        {
            ex.printStackTrace();
        }

        dialog.dismiss();
    }
}
