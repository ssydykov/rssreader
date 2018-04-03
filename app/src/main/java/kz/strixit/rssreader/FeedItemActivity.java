package kz.strixit.rssreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FeedItemActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProgressDialog pDialog;
    private ArrayList<HashMap<String, String>> rssItemList = new ArrayList<HashMap<String,String>>();
    private List<RSSItem> rssItems = new ArrayList<RSSItem>();
    private ListView lv;
    private LinearLayout noConnectionLayout;

    RSSParser rssParser = new RSSParser();
    RSSFeed rssFeed;

    private static String TAG_TITLE = "title";
    private static String TAG_LINK = "link";
    private static String TAG_DESRIPTION = "description";
    private static String TAG_PUB_DATE = "pubDate";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_item);

        Intent i = getIntent();
        Integer site_id = Integer.parseInt(i.getStringExtra("id"));
        String title = i.getStringExtra(TAG_TITLE);

        toolbar = (Toolbar) findViewById(R.id.layoutHeader);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RSSDatabaseHandler rssDB = new RSSDatabaseHandler(getApplicationContext());

        Website site = rssDB.getSite(site_id);
        String rss_link = site.getRSSLink();

        noConnectionLayout = (LinearLayout) findViewById(R.id.noConnectionLayout);
        lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent in = new Intent(getApplicationContext(), SingleFeedActivity.class);

                // getting page url
                String page_url = ((TextView) view.findViewById(R.id.page_url)).getText().toString();
                Toast.makeText(getApplicationContext(), page_url, Toast.LENGTH_SHORT).show();
                in.putExtra("page_url", page_url);
                startActivity(in);
            }
        });

        new loadRSSFeedItems().execute(rss_link);
        if (isOnline()) {

            lv.setVisibility(View.VISIBLE);
            noConnectionLayout.setVisibility(View.GONE);

        } else {

            lv.setVisibility(View.GONE);
            noConnectionLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    class loadRSSFeedItems extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FeedItemActivity.this);
            pDialog.setMessage("Loading recent articles...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting all recent articles and showing them in listview
         * */
        @Override
        protected String doInBackground(String... args) {
            // rss link url
            String rss_url = args[0];

            // list of rss items
            rssItems = rssParser.getRSSFeedItems(rss_url);

            // looping through each item
            for(RSSItem item : rssItems){
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put(TAG_TITLE, item.getTitle());
                map.put(TAG_LINK, item.getLink());
                map.put(TAG_PUB_DATE, item.getPubdate());
                String description = item.getDescription();
                // taking only 200 chars from description
                if(description.length() > 100){
                    description = description.substring(0, 97) + "..";
                }
                map.put(TAG_DESRIPTION, description);

                // adding HashList to ArrayList
                rssItemList.add(map);
            }

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed items into listview
                     * */
                    ListAdapter adapter = new SimpleAdapter(FeedItemActivity.this,
                            rssItemList, R.layout.item_list,
                            new String[] { TAG_LINK, TAG_TITLE, TAG_PUB_DATE, TAG_DESRIPTION },
                            new int[] { R.id.page_url, R.id.title, R.id.pub_date, R.id.link });

                    // updating listview
                    lv.setAdapter(adapter);
                }
            });
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String args) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
        }
    }
}
