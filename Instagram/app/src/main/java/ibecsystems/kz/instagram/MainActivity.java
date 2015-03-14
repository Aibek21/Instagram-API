package ibecsystems.kz.instagram;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


public class MainActivity extends ActionBarActivity {

    private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
    public static final String APIURL = "https://api.instagram.com/v1";
    public static String CALLBACKURL = "http://favella.kz";
    String client_id = "9df68c71fa2845f7a838b306b7cad846";
    String authURLString =  AUTHURL + "?client_id=" + client_id +
                           "&redirect_uri=" + CALLBACKURL +
                            "&response_type=code&scope=basic+likes";
    Dialog webDialog;
    public final String PREFNAME = "INSTAPREFERENCES";
    private SharedPreferences sharedPreferences;
    private Button login;
    private RelativeLayout progressBar;
    private WebView webView;
    private boolean isOpened= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        if(!token.equals("")) showPhotosList();
        setContentView(R.layout.activity_main);




        login = (Button) findViewById(R.id.login);
        progressBar = (RelativeLayout) findViewById(R.id.progressBar);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authorize();
            }
        });

        init();
    }


    public void init(){

        webDialog = new Dialog(this);
        webDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        webDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(webDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        webDialog.getWindow().setAttributes(lp);

        webDialog.setCancelable(true);


        webDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(progressBar.getVisibility()==View.INVISIBLE) progressBar.setVisibility(View.VISIBLE);
                else progressBar.setVisibility(View.INVISIBLE);
            }
        });
        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);

        webView.setWebViewClient(new AuthWebViewClient());

        webDialog.setContentView(webView);
    }

    public void authorize(){



        progressBar.setVisibility(View.VISIBLE);


        isOpened = true;
            webView.loadUrl(authURLString);

        webDialog.setContentView(webView);
        webDialog.show();


    }


    public void showPhotosList(){
        Intent intent = new Intent(this, PhotosListActivity.class);
        startActivity(intent);
        finish();
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

                showPhotosList();

                return true;
            }
            return false;
        }
    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);

        if(webDialog.isShowing()) isOpened=true;
        else isOpened=false;

        outState.putBoolean("isOpened", isOpened);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        isOpened = savedInstanceState.getBoolean("isOpened");
        if(isOpened) {
            webView.restoreState(savedInstanceState);

            webDialog.show();
        }
        super.onRestoreInstanceState(savedInstanceState);
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


        return super.onOptionsItemSelected(item);
    }

}
