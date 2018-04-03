package kz.strixit.rssreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

public class AddActivity extends AppCompatActivity {

    private Button btnSubmit;
    private Button btnCancel;
    private Toolbar toolbar;
    private MaterialEditText txtUrl;
    private ProgressDialog pDialog;

    RSSParser rssParser = new RSSParser();
    RSSFeed rssFeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        toolbar = (Toolbar) findViewById(R.id.layoutHeader);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // buttons
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        txtUrl = (MaterialEditText) findViewById(R.id.txtUrl);
//        lblMessage = (TextView) findViewById(R.id.lblMessage);

        // Submit button click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String url = txtUrl.getText().toString();

                // Validation url
                Log.d("URL Length", "" + url.length());
                // check if user entered any data in EditText
                if (url.length() > 0) {
//                    lblMessage.setText("");
                    String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
                    if (url.matches(urlPattern)) {
                        // valid url
                        new loadRSSFeed().execute(url);
                    } else {
                        // URL not valid
                        txtUrl.setHelperText("Please enter a valid url");
                        txtUrl.setUnderlineColor(getResources().getColor(R.color.red));
                    }
                } else {
                    // Please enter url
                    txtUrl.setHelperText("Please enter website url");
                    txtUrl.setUnderlineColor(getResources().getColor(R.color.red));
                }

            }
        });

        // Cancel button click event
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class loadRSSFeed extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddActivity.this);
            pDialog.setMessage("Fetching RSS Information ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Inbox JSON
         * */
        @Override
        protected String doInBackground(String... args) {
            String url = args[0];
            rssFeed = rssParser.getRSSFeed(url);
            Log.d("rssFeed", " "+ rssFeed);
            if (rssFeed != null) {
                Log.e("RSS URL",
                        rssFeed.getTitle() + "" + rssFeed.getLink() + ""
                                + rssFeed.getDescription() + ""
                                + rssFeed.getLanguage());
                RSSDatabaseHandler rssDb = new RSSDatabaseHandler(
                        getApplicationContext());
                Website site = new Website(rssFeed.getTitle(), rssFeed.getLink(), rssFeed.getRSSLink(),
                        rssFeed.getDescription());
                rssDb.addSite(site);
                Intent i = getIntent();
                // send result code 100 to notify about product update
                setResult(100, i);
                finish();
            } else {
                // updating UI from Background Thread
                runOnUiThread(new Runnable() {
                    public void run() {
                        txtUrl.setHelperText("Rss url not found. Please check the url or try again");
                        txtUrl.setUnderlineColor(getResources().getColor(R.color.red));
//                        lblMessage.setText("Rss url not found. Please check the url or try again");
                    }
                });
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String args) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    if (rssFeed != null) {

                    }

                }
            });

        }

    }
}
