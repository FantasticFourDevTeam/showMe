package com.example.FundigoApp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

public class DeepLinkActivity extends Activity {
    Intent intent;

    //Assaf:option currently was marked and the option to share open directly a deep link dialog
    //Once we will add more options to share it will be back
    //Also in manifest file the style Holo.Dialog - was changed to no Display
    //android:theme="@android:style/Theme.NoDisplay"

    @Override
         protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //requestWindowFeature(Window.FEATURE_NO_TITLE);
         //setContentView(R.layout.activity_deeplink);
         this.AppPage();
    }

  //  public void AppPage(View v) {
    public void AppPage()
    {
        final BranchUniversalObject branchUniversalObject = new BranchUniversalObject ()
                                                              .setCanonicalIdentifier ("item/1234")
                                                              .setTitle(getIntent().getStringExtra("name") + " is amazing event, one of your friends shared it through WhoGO App")
                                                              .setContentDescription("" + R.string.my_content_description)
                                                              .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                                                              .setContentImageUrl(getIntent().getStringExtra("eventPic"))
                                                              .addContentMetadata("objectId", getIntent().getStringExtra("objectId"));

        final LinkProperties linkProperties = new LinkProperties ()
                                                .setChannel ("My Application")
                                                .setFeature ("sharing");

        final ShareSheetStyle shareSheetStyle = new ShareSheetStyle (this, "", getIntent().getStringExtra("name") + " is amazing event join it by using WhoGO App")
                                                  .setCopyUrlStyle (getResources ().getDrawable (android.R.drawable.ic_menu_send), "Copy Link", "Copied to clipboard")
                                                  .setMoreOptionStyle(getResources().getDrawable(android.R.drawable.ic_menu_search), "Show more")
                                                  .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                                                  .addPreferredSharingOption (SharingHelper.SHARE_WITH.EMAIL)
                                                  .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP);
        branchUniversalObject.showShareSheet (this,
                                                     linkProperties,
                                                     shareSheetStyle,
                                                     new Branch.BranchLinkShareListener () {
                                                         @Override
                                                         public void onShareLinkDialogLaunched() {
                                                         }

                                                         @Override
                                                         public void onShareLinkDialogDismissed() {
                                                             finish();
                                                         }

                                                         @Override
                                                         public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
                                                         }

                                                         @Override
                                                         public void onChannelSelected(String channelName) {//Open the Applications Picker
//                                                             if(channelName.equals("WhatsApp")) {
//                                                                 Intent sendIntent = new Intent();
//                                                                 sendIntent.setAction(Intent.ACTION_SEND);
//                                                                 sendIntent.putExtra(Intent.EXTRA_TEXT,branchUniversalObject.getCanonicalUrl().toString());
//                                                                 sendIntent.setType("text/plain");
//                                                                 startActivity(sendIntent);
//                                                                 finish();
                                                                    finish(); // Activity finish();
//                                                             }
                                                         }
                                                     });
        branchUniversalObject.generateShortUrl (this, linkProperties, new Branch.BranchLinkCreateListener () {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                   // Toast.makeText (getApplicationContext (), url, Toast.LENGTH_LONG).show ();
                } else
                    Toast.makeText (getApplication (), error.getMessage () + "", Toast.LENGTH_SHORT).show ();

            }
        });

    }

    public void WebPage(View v) {
        String faceBookUrl = intent.getStringExtra("fbUrl");
        Intent webIntent;

        if (faceBookUrl != "" && faceBookUrl != null) {
            try {
                getPackageManager().getPackageInfo("com.facebook.katana", 0);
                webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" + faceBookUrl));
                startActivity(webIntent);
            } catch (Exception e) {
                Log.e(e.toString(), "Open link to FaceBook App is fail, sending to Browser");
                try {
                    webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(faceBookUrl));
                    startActivity(webIntent);
                }
                catch (Exception e1)
                {
                    Log.e(e1.toString(), "Open link to FaceBook Browser is fail");
                }            }
        } else
            Toast.makeText(v.getContext(), "No FaceBook Page to Present", Toast.LENGTH_SHORT).show();
    }
}
