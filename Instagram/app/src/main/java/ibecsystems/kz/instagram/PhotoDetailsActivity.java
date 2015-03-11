package ibecsystems.kz.instagram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class PhotoDetailsActivity extends ActionBarActivity {

    public static final String APIURL = "https://api.instagram.com/v1";
    private String id;
    private String accessToken;
    private final String PREFNAME = "INSTAPREFERENCES";
    private SharedPreferences sharedPreferences;
    private String photoURL, commentsNumber, likesNumber;
    ArrayList<Comment> comments;
    RelativeLayout progressBar, progressBar1;
    ListView commentList;
    ImageView image;
    TextView numberOfComments, numberOfLikes;
    CommentListAdapter adapter;
    Bitmap mainImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        sharedPreferences = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", "");

        init();


        if(savedInstanceState!=null){
            Log.e("savedInstance", "ok");
            mainImage = savedInstanceState.getParcelable("mainImage");
            comments = savedInstanceState.getParcelableArrayList("comments");
            commentsNumber = savedInstanceState.getString("commentsNumber");
            likesNumber = savedInstanceState.getString("likesNumber");

            if(mainImage!=null) {
                progressBar.setVisibility(View.INVISIBLE);
                image.setImageBitmap(mainImage);
                fillContent();
            }else
                new getInfo().execute();


        }else
            new getInfo().execute();
    }


    public void init(){
        progressBar = (RelativeLayout) findViewById(R.id.progressBar);
        progressBar1 = (RelativeLayout) findViewById(R.id.progressBar1);
        commentList = (ListView) findViewById(R.id.comments_list);
        image = (ImageView) findViewById(R.id.main_image);
        numberOfComments = (TextView) findViewById(R.id.number_of_comments);
        numberOfLikes = (TextView) findViewById(R.id.number_of_likes);
        comments = new ArrayList<Comment>();

    }

    public void fillContent(){
        numberOfComments.setText(commentsNumber);
        numberOfLikes.setText(likesNumber);

        progressBar1.setVisibility(View.INVISIBLE);
        adapter = new CommentListAdapter(PhotoDetailsActivity.this);
        commentList.setAdapter(adapter);



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

                String urlString = APIURL + "/media/" +id+"?access_token="+accessToken;

                //Log.e("response", urlString);

                URL url = new URL(urlString);
                InputStream inputStream = url.openConnection().getInputStream();
                String response = streamToString(inputStream);
                //Log.e("response", response);

                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                JSONObject dataJsonObject = jsonObject.getJSONObject("data");

                jsonObject = dataJsonObject.getJSONObject("images").getJSONObject("standard_resolution");
                photoURL = jsonObject.getString("url");


                jsonObject = dataJsonObject.getJSONObject("comments");
                commentsNumber = jsonObject.getString("count");

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for(int i=0; i<jsonArray.length(); i++) {
                    Comment comment = new Comment();

                    jsonObject = jsonArray.getJSONObject(i);

                    String str = jsonObject.getString("text");

                    comment.setText(str);
                    JSONObject fromJsonObject = jsonObject.getJSONObject("from");
                    str = fromJsonObject.getString("username");
                    comment.setAuthor(str);

                    Log.e("comment", str);
                    comments.add(comment);

                }

                jsonObject = dataJsonObject.getJSONObject("likes");
                likesNumber = jsonObject.getString("count");



            }catch (Exception e)
            {
                e.printStackTrace();
            }






            return null;
        }
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

           fillContent();
            new ImageDownloader().execute();
        };
    }


    class CommentListAdapter extends BaseAdapter {

        Context context;
        public CommentListAdapter(Context context){
            this.context = context;
        }
        @Override
        public int getCount() {

            return comments.size();
        }

        @Override
        public Object getItem(int position) {
            return comments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            CommentViewHolder holder;
            //View root = convertView;

            if(convertView==null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate( R.layout.list_item, null);
                holder = new CommentViewHolder();
                holder.userName = (TextView) convertView.findViewById(R.id.username);
                holder.text = (TextView)convertView.findViewById(R.id.text);
                convertView.setTag(holder);


            }else {
                holder = (CommentViewHolder) convertView.getTag();

            }



          holder.text.setText(comments.get(position).getText());
          holder.userName.setText(comments.get(position).getAuthor());

            return convertView;
        }



    }


    class ImageDownloader extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected void onPreExecute() {


        }

        protected Bitmap doInBackground(String... params) {


            Log.e("url", photoURL);
            URL url = null;
            try {
                url = new URL(photoURL);
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

            progressBar.setVisibility(View.INVISIBLE);
            mainImage = result;
            image.setImageBitmap(result);
        }

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("mainImage", mainImage);
        outState.putParcelableArrayList("comments", comments);
        outState.putString("commentsNumber", commentsNumber);
        outState.putString("likesNumber", likesNumber);

        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_details, menu);
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
