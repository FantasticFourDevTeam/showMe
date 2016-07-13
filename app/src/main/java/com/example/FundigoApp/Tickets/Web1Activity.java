package com.example.FundigoApp.Tickets;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.example.FundigoApp.R;

public class Web1Activity extends AppCompatActivity {
    private WebView myWebView;
    private int amount;
    private int idorder;
    private String myPayHtml = "<html><head><title>My ParseApp site</title><style>body { font-family: Helvetica, Arial, sans-serif; } div { width: 800px; height: 400px; margin: 40px auto; padding: 20px; border: 2px solid #5298fc; }h1 { font-size: 30px; margin: 0; }p { margin: 40px 0; em { font-family: monospace; }a { color: #5298fc; text-decoration: none; } </style><meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" /></head><body><form name=\"pelepayform\" action=\"https://www.pelepay.co.il/pay/paypage.aspx\" method=\"post\"><input type=\"hidden\" value=\"nesizagury@gmail.com\" name=\"business\"><INPUT TYPE=\"hidden\" value=\"\" NAME=\"amount\"><INPUT TYPE=\"hidden\" value=\"\" NAME=\"orderid\"><INPUT TYPE=\"hidden\" value=\"_product\" NAME=\"description\"><input type=\"image\" src=\"http://www.pelepay.co.il/btn_images/pay_button_3.gif\" name=\"submit\" alt=\"Make payments with pelepay\"></form><script language=\"javascript\">document.pelepayform.amount.value= \"" + amount + "\";document.pelepayform.orderid.value= \"" + idorder + "\";</script></body></html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_web1);
        myWebView = (WebView) findViewById (R.id.webView2);
        WebSettings webSettings = myWebView.getSettings ();
        webSettings.setJavaScriptEnabled (true);
        amount = 122;
        idorder = 111;

        Intent i = new Intent ();

        // MUST instantiate android browser, otherwise it won't work
        i.setComponent (new ComponentName ("com.android.browser", "com.android.browser.BrowserActivity"));
        i.setAction (Intent.ACTION_VIEW);
        // String dataUri = "data:text/html," + URLEncoder.encode(myPayHtml).replaceAll("\\+","%20");
        i.setData (Uri.parse (myPayHtml));

        startActivity (i);
    }
}