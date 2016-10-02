package com.example.FundigoApp.Customer.CustomerMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.FundigoApp.Customer.CustomerDetails;
import com.example.FundigoApp.GlobalVariables;
import com.example.FundigoApp.R;
import com.example.FundigoApp.StaticMethod.FileAndImageMethods;
import com.example.FundigoApp.StaticMethod.UserDetailsMethod;
import com.example.FundigoApp.Verifications.LoginActivity;
import com.example.FundigoApp.Verifications.SmsSignUpActivity;
import com.example.events.ActivityFacebook;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {
    LoginButton facebook_login_button;
    CallbackManager callbackManager;
    Button sms_login_button;
    Button user_profile_button;
    LoginButton facebook_logout_button;
    String currentUserName;
    String phoneNum;
    String userImage;
    TableLayout tableLayout; //table to prsent profile
    ImageView drawView; // profile picture
    TextView facebookUserNameView;
    ImageView profileFacebookPictureView;
    Context context;
    Button user_profile_update_button;
    Button user_evnets_tickets_button;
    Button save_credit_card_button;
    Button delete_credit_card_button;
    /**
     * button for producer login
     */
    Button producerPage_button;
    ImageLoader loader;

    CustomerDetails customerDetails;
    ArrayList<ImageAdapter.MenuItem> items;
    TextView textViewHader;
    TextView userNameTv;
    ImageView user_imageView;
    GridView gridview;
    ArrayList<Integer> image = new ArrayList<>();
    ArrayList<Integer> titel = new ArrayList<>();
    ImageAdapter gridAdapter = null;
    public static Integer[] mThumbIds = {
            R.drawable.square_facebook,
            R.drawable.user_signup,
            R.drawable.user_profile_edit_button,
            R.drawable.ticket_icon,
            R.drawable.credit_card_icon,
            R.drawable.producer_icon
    };
    public static Integer[] mTitels = {
            R.string.title_activity_facebook_login,
            R.string.sms_verification,
            R.string.updateProfile,
            R.string.tickets,
            R.string.save_credit_card,
            R.string.producer_login
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        context = this.getApplicationContext();
        gridview = (GridView) findViewById(R.id.grid_view_menu);
        textViewHader = (TextView) findViewById(R.id.textViewHader);
        userNameTv = (TextView) findViewById(R.id.user_name_tv);
        user_imageView = (ImageView) findViewById(R.id.user_imageView);
        loader = FileAndImageMethods.getImageLoader(this);
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) // change default button text to Logout also in case of Guest
        {
            mTitels[0] = R.string.log_out_from_facebook;
        }

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent;
                switch ((int) id) {
                    case R.string.title_activity_facebook_login:
                        intent = new Intent(MenuActivity.this, ActivityFacebook.class);
                        startActivityForResult(intent, 1);
                        break;
                    case R.string.log_out_from_facebook:
                        // intent = new Intent(MenuActivity.this, ActivityFacebook.class);
                        // startActivity(intent);
                        logOutFacebook();
                        break;
                    case R.string.sms_verification:
                        intent = new Intent(MenuActivity.this, SmsSignUpActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.producer_login:
                        intent = new Intent(MenuActivity.this, LoginActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.save_credit_card:
                        intent = new Intent(MenuActivity.this, SaveCreditCard.class);
                        startActivity(intent);
                        break;
                    case R.string.delete_credit_card:
                        deleteCreditCard();
                        break;
                    case R.string.updateProfile:
                        try {
                            Intent I = new Intent(MenuActivity.this, CustomerProfileUpdate.class);
                            startActivity(I);
                        } catch (Exception e) {
                            Log.e(e.toString(), "error in update flow");
                        }
                        break;
                    case R.string.tickets:
                        intent = new Intent(MenuActivity.this, MyEventsTicketsActivity.class);
                        startActivity(intent);
                        break;


                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        image.clear();
        titel.clear();

        // in case that return back from Prodcuer area then need to check if customer registered or not
        GlobalVariables.CUSTOMER_PHONE_NUM = FileAndImageMethods.getCustomerPhoneNumFromFile(this);

        // in case that return back from Prodcuer area then need to check if customer registered or not
        if (GlobalVariables.CUSTOMER_PHONE_NUM == null || GlobalVariables.CUSTOMER_PHONE_NUM.equals("")) {
            GlobalVariables.IS_CUSTOMER_REGISTERED_USER = false;
            GlobalVariables.IS_CUSTOMER_GUEST = true;
            GlobalVariables.CUSTOMER_PHONE_NUM = "";
            GlobalVariables.IS_PRODUCER = false;//26.09 assaf fixed
        } else {
            GlobalVariables.IS_CUSTOMER_GUEST = false;
            GlobalVariables.IS_PRODUCER = false;
            GlobalVariables.IS_CUSTOMER_REGISTERED_USER = true;
        }

        try
        {
        if (GlobalVariables.IS_CUSTOMER_REGISTERED_USER) {
            phoneNum = GlobalVariables.CUSTOMER_PHONE_NUM;
            customerDetails = UserDetailsMethod.getUserDetailsFromParseInMainThread(phoneNum);
            textViewHader.setText(this.getString(R.string.you_logged_in_as) + " " + GlobalVariables.CUSTOMER_PHONE_NUM);
            userNameTv.setText(customerDetails.getCustomerName());
            currentUserName = customerDetails.getCustomerName();
            userImage = customerDetails.getCustomerImage();


            if (userImage != null) {
                loader.displayImage(userImage, user_imageView);
            } else {
                user_imageView.setImageResource(R.drawable.avatar);
            }

            final AccessToken accessToken = AccessToken.getCurrentAccessToken();
            for (int i = 0; i < mThumbIds.length; i++) {
                if (!mTitels[i].equals(R.string.sms_verification)) {
                    if (accessToken == null) {
                        image.add(mThumbIds[i]);
                        titel.add(mTitels[i]);
                    } else {
                        image.add(mThumbIds[i]);
                        titel.add(mTitels[i]);
                        if (i == 0)
                            mTitels[0] = R.string.log_out_from_facebook;
                    }
                }
            }

            ParseQuery<CreditCard> query = new ParseQuery("creditCards");
            query.whereEqualTo("IdCostumer", GlobalVariables.CUSTOMER_PHONE_NUM);
            query.getFirstInBackground(new GetCallback<CreditCard>() {
                public void done(CreditCard creditCard, ParseException e) {
                    if (e == null) {
                        String creditCardNumber = creditCard.getCreditCardNumber();
                        String last4Digits = creditCardNumber.substring(creditCardNumber.length() - 4, creditCardNumber.length());
                        for (int i = 0; i < titel.size(); i++) {
                            if (titel.get(i).equals(R.string.save_credit_card)) {
//                                Integer str=Integer.valueOf("/n" +
//                                        "Card XXXX-" + last4Digits);
                                titel.set(i, R.string.delete_credit_card);
                                gridAdapter = new ImageAdapter(MenuActivity.this, image, titel);
                                gridview.setAdapter(gridAdapter);
                            }
                        }
                       /* save_credit_card_button.setVisibility (View.GONE);
                        delete_credit_card_button.setVisibility (View.VISIBLE);

                        delete_credit_card_button.setText ("Delete Credit Card XXXX-" + last4Digits);
                        */
                    } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        /*save_credit_card_button.setVisibility (View.VISIBLE);
                        delete_credit_card_button.setVisibility (View.GONE);
                        */
                    } else {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            textViewHader.setText("Your Social Life App");
            userNameTv.setText(R.string.guest);
            user_imageView.setImageResource(R.drawable.avatar);
            for (int i = 0; i < mThumbIds.length; i++) {
                if (!mTitels[i].equals(R.string.updateProfile) && !mTitels[i].equals(R.string.save_credit_card) && !mTitels[i].equals(R.string.tickets)) {
                    image.add(mThumbIds[i]);
                    titel.add(mTitels[i]);
                }
            }
        }
        gridAdapter = new ImageAdapter(this, image, titel);
        gridview.setAdapter(gridAdapter);
      }
     catch(Exception ex)
     {
       ex.printStackTrace();
     }
   }



    public void deleteCreditCard() {
        ParseQuery<CreditCard> query = new ParseQuery ("creditCards");
        query.whereEqualTo ("IdCostumer", GlobalVariables.CUSTOMER_PHONE_NUM);
        CreditCard creditCard;
        try {
            creditCard = query.getFirst ();
            creditCard.delete ();
            titel.set(titel.indexOf(R.string.delete_credit_card),R.string.save_credit_card);
            gridAdapter =new ImageAdapter(MenuActivity.this,image,titel);
            gridview.setAdapter(gridAdapter);
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }




    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<Integer> image;
        ArrayList<Integer> title;
        public ImageAdapter(Context c,ArrayList<Integer> image,ArrayList<Integer> ti) {
            this.title=ti;
            this.image=image;
            mContext = c;
        }

        public int getCount() {
            return image.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return title.get(position);
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            try {
                if (convertView == null) {

                    gridView = new View(mContext);
                    // get layout from mobile.xml
                    gridView = inflater.inflate(R.layout.menu_item_layout, null);
                    // set image based on selected text
                    ImageView imageView = (ImageView) gridView
                            .findViewById(R.id.menu_item_image);
                    imageView.setImageResource(image.get(position));

                    TextView textView = (TextView) gridView.findViewById(R.id.menu_item_title);
                    textView.setText(mContext.getResources().getString(title.get(position)));
                } else {
                    gridView = (View) convertView;
                }

                return gridView;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return null;
            }
            catch (OutOfMemoryError ex)
            {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

        }


        public class MenuItem {
            String itemTitle;
            Integer itemImage;


            MenuItem(String title, Integer image) {
                this.itemImage = image;
                this.itemTitle = title;
            }

            public String getTitle(){
                return itemTitle;
            }
            public Integer getImage(){
                return itemImage;
            }
        }
    }

    public void logOutFacebook() {
        new GraphRequest(AccessToken.getCurrentAccessToken (), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback () {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                mTitels[0] = R.string.title_activity_facebook_login;  // change butto text to login after Logout was completed
                LoginManager.getInstance().logOut();
                Toast.makeText(context, R.string.loged_out_of_facebook, Toast.LENGTH_SHORT).show();
            }
        }).executeAsync();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult (requestCode, resultCode, data);
         if(requestCode==1) {
             if (resultCode == RESULT_OK && data != null) {
                 mTitels[0] = R.string.log_out_from_facebook; //chnage button to logout after Login done
                 gridAdapter.notifyDataSetChanged();
             }
         }
    }
}
