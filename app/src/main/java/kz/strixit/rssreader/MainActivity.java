package kz.strixit.rssreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    private Toolbar toolbar;
    ArrayList<HashMap<String, String>> rssFeedList;
    String[] sqliteIds;

    public static String TAG_ID = "id";
    public static String TAG_TITLE = "title";
    public static String TAG_LINK = "link";

    // List view
    ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.layoutHeader);
        setSupportActionBar(toolbar);

        rssFeedList = new ArrayList<HashMap<String, String>>();

        new loadStoreSites().execute();
        lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String sqlite_id = ((TextView) view.findViewById(R.id.sqlite_id)).getText().toString();
                String title = ((TextView) view.findViewById(R.id.title)).getText().toString();
                Intent in = new Intent(getApplicationContext(), FeedItemActivity.class);
                in.putExtra(TAG_ID, sqlite_id);
                in.putExtra(TAG_TITLE, title);
                startActivity(in);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 100) {

            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        if (v.getId() == R.id.list) {

            menu.setHeaderTitle("Delete this feed portal?");
            menu.add(Menu.NONE, 0, 0, "Delete Feed");
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_add) {

            Intent i = new Intent(getApplicationContext(), AddActivity.class);
            startActivityForResult(i, 100);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        if(menuItemIndex == 0) {

            RSSDatabaseHandler rssDb = new RSSDatabaseHandler(getApplicationContext());
            Website site = new Website();
            site.setId(Integer.parseInt(sqliteIds[info.position]));
            rssDb.deleteSite(site);

            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        return true;
    }

    class loadStoreSites extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading websites ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            runOnUiThread(new Runnable() {
                public void run() {
                    RSSDatabaseHandler rssDb = new RSSDatabaseHandler(
                            getApplicationContext());

                    List<Website> siteList = rssDb.getAllSites();
                    sqliteIds = new String[siteList.size()];

                    for (int i = 0; i < siteList.size(); i++) {

                        Website s = siteList.get(i);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TAG_ID, s.getId().toString());
                        map.put(TAG_TITLE, s.getTitle());
                        map.put(TAG_LINK, s.getLink());

                        rssFeedList.add(map);
                        sqliteIds[i] = s.getId().toString();
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            MainActivity.this,
                            rssFeedList, R.layout.item_main,
                            new String[] { TAG_ID, TAG_TITLE, TAG_LINK },
                            new int[] { R.id.sqlite_id, R.id.title, R.id.link });

                    lv.setAdapter(adapter);
                    registerForContextMenu(lv);
                }
            });
            return null;
        }

        protected void onPostExecute(String args) {

            pDialog.dismiss();
        }
    }
}
