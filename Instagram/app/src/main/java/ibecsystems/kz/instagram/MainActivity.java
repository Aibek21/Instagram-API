package ibecsystems.kz.instagram;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;


public class MainActivity extends ActionBarActivity {

    private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
    public static final String APIURL = "https://api.instagram.com/v1";
    public static String CALLBACKURL = "http://favella.kz";
    String client_id = "9df68c71fa2845f7a838b306b7cad846";
    String authURLString =  AUTHURL + "?client_id=" + client_id +
                           "&redirect_uri=" + CALLBACKURL +
                            "&response_type=code&scope=basic";
    WebView webView;
    Dialog webDialog;
    public final String PREFNAME = "INSTAPREFERENCES";
    SharedPreferences sharedPreferences;
    Button login;
    RelativeLayout progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webDialog = new Dialog(this);
        webDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        webDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(webDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        webDialog.getWindow().setAttributes(lp);


        webDialog.setCancelable(true);

        login = (Button) findViewById(R.id.login);
        progressBar = (RelativeLayout) findViewById(R.id.progressBar);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authorize();
            }
        });
    }


    public void authorize(){


        progressBar.setVisibility(View.VISIBLE);
        webView = new WebView(this);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new AuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(authURLString);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        webView.setLayoutParams(lp);
        webDialog.setContentView(webView);
        webDialog.show();

    }


    public void showPhotosList(){
        Intent intent = new Intent(this, PhotosListActivity.class);
        startActivity(intent);
    }

    public class AuthWebViewClient extends WebViewClient
    {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if (url.startsWith(CALLBACKURL))
            {
                String parts[] = url.split("=");
                String request_token = parts[1];
                //InstagramLoginDialog.this.dismiss();

                sharedPreferences = getSharedPreferences(PREFNAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("token", request_token);
                editor.commit();

                webDialog.dismiss();
                showPhotosList();

                return true;
            }
            return false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
