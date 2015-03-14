package ibecsystems.kz.instagram;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;


public class PhotosListActivity extends ActionBarActivity {


    private static final String TOKENURL ="https://api.instagram.com/oauth/access_token";
    public static final String APIURL = "https://api.instagram.com/v1";
    public static String CALLBACKURL = "http://favella.kz";
    String client_id = "9df68c71fa2845f7a838b306b7cad846";
    String client_secret = "683b3a79025b4d3b92b09f23c83dd60d";

    String tokenURLString = TOKENURL + "?client_id=" + client_id + "&client_secret=" +
            client_secret + "&redirect_uri=" + CALLBACKURL + "&grant_type=authorization_code";
    private String token;
    private final String PREFNAME = "INSTAPREFERENCES";
    private SharedPreferences sharedPreferences;
    private String user_id, accessToken, username, max_id;
    private ArrayList<Photo> photos;
    private HashMap<String ,Bitmap> images;

    Utils utils;
    int columnWidth;
    ListView listView;
    RelativeLayout progressBar;
    GridViewImageAdapter adapter;
    boolean isLoading=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_list);
        sharedPreferences = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");
        utils = new Utils(this);


        init();
        initilizeGridLayout();

        if(savedInstanceState!=null){
            photos = savedInstanceState.getParcelableArrayList("photos");
            images = (HashMap<String,Bitmap>)savedInstanceState.getSerializable("images");
            user_id = savedInstanceState.getString("user_id");
            username = savedInstanceState.getString("username");
            accessToken = savedInstanceState.getString("accessToken");
            max_id = savedInstanceState.getString("max_id");

            if(max_id!=null) {
                setTitle();
                fillContent();
                isLoading=false;
            }
            else
                new getInfo().execute();
        }else
            new getInfo().execute();

    }


    public void init(){

        progressBar = (RelativeLayout) findViewById(R.id.progressBar);
        photos = new ArrayList<Photo>();
        images = new HashMap<String, Bitmap>();

    }

    private void initilizeGridLayout() {
        listView = (ListView) findViewById(R.id.grid_view);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem+visibleItemCount>=totalItemCount && !isLoading){

                    isLoading=true;
                    new getPhotos().execute();

                }


            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDetails(position);
            }
        });
    }


    public void fillContent(){
        if (adapter == null) {
            progressBar.setVisibility(View.INVISIBLE);
            adapter = new GridViewImageAdapter(PhotosListActivity.this, columnWidth);
            listView.setAdapter(adapter);
        }else
            adapter.notifyDataSetChanged();
    }
    public void showDetails(int position){


        Intent intent = new Intent(this, PhotoDetailsActivity.class);
        intent.putExtra("id", photos.get(position).getId());
        intent.putExtra("username", photos.get(position).getUserName());

        startActivity(intent);
    }

    public void setTitle(){
        Log.e("username",username);
        getSupportActionBar().setTitle(username);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList("photos", photos);
        outState.putSerializable("images", images);
        outState.putString("username", username);
        outState.putString("user_id", user_id);
        outState.putString("accessToken", accessToken);
        outState.putString("max_id", max_id);
        super.onSaveInstanceState(outState);
    }

    private String streamToString(InputStream is) {
        ByteArrayOutputStream oas = new ByteArrayOutputStream();
        copyStream(is, oas);
        String t = oas.toString();
        try {
            oas.close();
            oas = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    private void copyStream(InputStream is, OutputStream os)
    {
        final int buffer_size = 1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    private class getInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute()
        {



        };
        @Override
        protected Void doInBackground(Void... voids) {


            try
            {
                accessToken = sharedPreferences.getString("access_token", "");
                username = sharedPreferences.getString("username", "");
                user_id = sharedPreferences.getString("user_id", "");


                if(accessToken.equals("")) {
                    URL url = new URL(tokenURLString);
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    httpsURLConnection.setRequestMethod("POST");
                    httpsURLConnection.setDoInput(true);
                    httpsURLConnection.setDoOutput(true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                    outputStreamWriter.write("client_id=" + client_id +
                            "&client_secret=" + client_secret +
                            "&grant_type=authorization_code" +
                            "&redirect_uri=" + CALLBACKURL +
                            "&code=" + token);
                    outputStreamWriter.flush();
                    InputStream inputStream = httpsURLConnection.getInputStream();
                    String response = streamToString(inputStream);
                    JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                    accessToken = jsonObject.getString("access_token");
                    user_id = jsonObject.getJSONObject("user").getString("id");
                    Log.e("id", user_id);
                    username = jsonObject.getJSONObject("user").getString("username");
                    Log.e("username1", username);

                    sharedPreferences = getSharedPreferences(PREFNAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_token", accessToken);
                    editor.putString("username", username);
                    editor.putString("user_id", user_id);
                    editor.commit();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }


            return null;
        }
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            setTitle();
            new getPhotos().execute();
        };
    }

    private class getPhotos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute()
        {


        };
        @Override
        protected Void doInBackground(Void... voids) {


            try
            {

                String urlString ;

                isLoading=true;
                if(max_id!=null) {
                    urlString = APIURL + "/users/self/feed/?max_id="+max_id+"&count=10"+"&access_token=" + accessToken ;
                }else{
                    urlString = APIURL + "/users/self/feed/?access_token=" + accessToken +"&count=10";
                }

                //Log.e("response", urlString);

                URL url = new URL(urlString);
                InputStream inputStream = url.openConnection().getInputStream();
                String response = streamToString(inputStream);
                //Log.e("response", response);

                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for(int i=0; i<jsonArray.length(); i++) {
                    Photo photo = new Photo();

                    jsonObject = jsonArray.getJSONObject(i);
                    photo.setUserHasLiked(jsonObject.getBoolean("user_has_liked"));
                    JSONObject mainImageJsonObject = jsonObject.getJSONObject("images").getJSONObject("low_resolution");
                    String str = mainImageJsonObject.getString("url");
                    photo.setLowResolution(str);


                    str = jsonObject.getJSONObject("user").getString("username");
                    photo.setUserName(str);

                    mainImageJsonObject = jsonObject.getJSONObject("comments");
                    str = mainImageJsonObject.getString("count");
                    photo.setComments(str);

                    mainImageJsonObject = jsonObject.getJSONObject("likes");
                    str = mainImageJsonObject.getString("count");
                    photo.setLikes(str);

                    str = jsonObject.getString("id");
                    photo.setId(str);


                    photos.add(photo);

                }

                max_id = photos.get(photos.size()-1).getId();

                Integer integer = photos.size();

                Log.e("size", integer.toString());

            }catch (Exception e)
            {
                e.printStackTrace();
            }






            return null;
        }
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            isLoading=false;
            fillContent();

        };
    }




    class GridViewImageAdapter extends BaseAdapter {

        Context context;
        int imageWidth;
        public GridViewImageAdapter(Context context,  int imageWidth){
            this.context = context;
            this.imageWidth = imageWidth;
        }
        @Override
        public int getCount() {

            return photos.size();
        }

        @Override
        public Object getItem(int position) {
            return photos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            //View root = convertView;

            if(convertView==null) {
               LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate( R.layout.grid_item, null);
                holder = new ViewHolder();
                holder.imageView = (ImageView)convertView.findViewById(R.id.image);
                holder.comments = (TextView) convertView.findViewById(R.id.number_of_comments);
                holder.likes = (TextView)convertView.findViewById(R.id.number_of_likes);
                holder.like = (CheckBox) convertView.findViewById(R.id.like);
                holder.userName = (TextView) convertView.findViewById(R.id.username);
                convertView.setTag(holder);

            }else {
                holder = (ViewHolder) convertView.getTag();
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.white_bg);
                holder.imageView.setImageBitmap(bitmap);
            }



            //holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            //holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imageWidth,
              //      imageWidth));

            Photo photo = photos.get(position);
            holder.comments.setText(photo.getComments());

            holder.likes.setText(photo.getLikes());

            holder.like.setChecked(photo.isUserHasLiked());

            holder.userName.setText(photo.getUserName());
            String url =photos.get(position).getLowResolution();

            if(images.get(url) != null)
              holder.imageView.setImageBitmap(images.get(url));
            else new ImageDownloader(holder.imageView, position).execute();

            holder.like.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Photo photo = photos.get(position);
                    Integer tmp = Integer.parseInt(photo.getLikes());
                    if(photo.isUserHasLiked())
                        tmp--;
                    else  tmp++;
                    photos.get(position).setLikes(tmp.toString());
                    holder.likes.setText( photos.get(position).getLikes());
                    new setLike(position).execute();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetails(position);
                }
            });

            return convertView;
        }

        class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
            int pos;
            ImageView imageView;
            String url1;
            public ImageDownloader( ImageView imageView, int pos) {

                this.pos = pos;
                this.imageView = imageView;
            }

            @Override
            protected void onPreExecute() {


            }

            protected Bitmap doInBackground(String... params) {

                 url1 = photos.get(pos).getLowResolution();
                Log.e("url", url1);
                URL url = null;
                try {
                    url = new URL(url1);
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                }
                Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return bmp;


            }

            protected void onPostExecute(Bitmap result) {

                images.put(url1, result);
                imageView.setImageBitmap(result);
            }

        }


        private class setLike extends AsyncTask<Void, Void, Void> {


            int pos;

            public setLike(int pos){
                this.pos = pos;
            }
            @Override
            protected void onPreExecute()
            {


                //adapter.notifyDataSetChanged();


            };
            @Override
            protected Void doInBackground(Void... voids) {


                try
                {

                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    String urlString = APIURL + "/media/"+photos.get(pos).getId()+"/likes/"+"?access_token=" + accessToken ;;

                    if(photos.get(pos).isUserHasLiked()) {
                        HttpDelete delete = new HttpDelete(urlString);
                        HttpResponse httpResponse = httpClient.execute(delete);
                        String response = EntityUtils.toString(httpResponse.getEntity());
                        photos.get(pos).setUserHasLiked(false);
                    }else{
                        HttpPost post = new HttpPost(urlString);
                        HttpResponse httpResponse = httpClient.execute(post);
                        String response = EntityUtils.toString(httpResponse.getEntity());
                        photos.get(pos).setUserHasLiked(true);
                        Log.e("response", response);
                    }






                }catch (Exception e)
                {
                    e.printStackTrace();
                }






                return null;
            }
            protected void onPostExecute(Void result)
            {
                super.onPostExecute(result);


            };
        }



    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photos_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {


            exit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void exit(){
        WebView webView = new WebView(this);
        webView.loadUrl("https://instagram.com/accounts/logout/");
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new AuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("access_token");
        editor.remove("username");
        editor.remove("user_id");
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public class AuthWebViewClient extends WebViewClient
    {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {

            return false;
        }
    }

}
