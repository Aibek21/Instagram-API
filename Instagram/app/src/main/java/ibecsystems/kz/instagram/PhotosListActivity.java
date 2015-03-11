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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    GridView gridView;
    RelativeLayout progressBar;
    GridViewImageAdapter adapter;
    boolean isLoading=false;

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

            if(max_id!=null)
                fillContent();
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
        gridView = (GridView) findViewById(R.id.grid_view);


        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            columnWidth = ((utils.getScreenWidth() - 30) / 3);
            gridView.setNumColumns(3);
        }else{
            columnWidth = ((utils.getScreenWidth() - 40) / 5);
            gridView.setNumColumns(5);
        }


        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding(5, 5, 5, 5);
        gridView.setHorizontalSpacing(5);
        gridView.setVerticalSpacing(5);

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem+visibleItemCount>=totalItemCount && !isLoading){

                    new getPhotos().execute();

                }


            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            gridView.setAdapter(adapter);
        }else
            adapter.notifyDataSetChanged();
    }
    public void showDetails(int position){

        sharedPreferences = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.commit();
        Intent intent = new Intent(this, PhotoDetailsActivity.class);
        intent.putExtra("id", photos.get(position).getId());
        startActivity(intent);
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
                URL url = new URL(tokenURLString);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                outputStreamWriter.write("client_id="+client_id+
                        "&client_secret="+ client_secret +
                        "&grant_type=authorization_code" +
                        "&redirect_uri="+CALLBACKURL+
                        "&code=" + token);
                outputStreamWriter.flush();
                InputStream inputStream = httpsURLConnection.getInputStream();
                String response = streamToString(inputStream);
                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                 accessToken = jsonObject.getString("access_token");
                 user_id = jsonObject.getJSONObject("user").getString("id");
                Log.e("id", user_id);
                 username = jsonObject.getJSONObject("user").getString("username");
                Log.e("username", username);
            }catch (Exception e)
            {
                e.printStackTrace();
            }


            return null;
        }
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
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
                    urlString = APIURL + "/users/" + user_id + "/media/recent/?max_id="+max_id+"&access_token=" + accessToken ;
                }else{
                    urlString = APIURL + "/users/" + user_id + "/media/recent/?access_token=" + accessToken ;
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
                    JSONObject mainImageJsonObject = jsonObject.getJSONObject("images").getJSONObject("thumbnail");
                    String str = mainImageJsonObject.getString("url");
                    photo.setThumbnail(str);



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
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            //View root = convertView;

            if(convertView==null) {
               LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate( R.layout.grid_item, null);
                holder = new ViewHolder();
                holder.imageView = (ImageView)convertView.findViewById(R.id.image);
                holder.comments = (TextView) convertView.findViewById(R.id.number_of_comments);
                holder.likes = (TextView)convertView.findViewById(R.id.number_of_likes);
                convertView.setTag(holder);


            }else {
                holder = (ViewHolder) convertView.getTag();
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.white_bg);
                holder.imageView.setImageBitmap(bitmap);
            }



            holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imageWidth,
                    imageWidth));

            holder.comments.setText(photos.get(position).getComments());

            holder.likes.setText(photos.get(position).getLikes());

            String url =photos.get(position).getThumbnail();
            if(images.get(url) != null)
              holder.imageView.setImageBitmap(images.get(url));
            else new ImageDownloader(holder.imageView, position).execute();

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

                 url1 = photos.get(pos).getThumbnail();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
