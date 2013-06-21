package com.example.restuarant;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends Activity {

    private TextView restaurantView;
    private static final String DEBUG_TAG = "restaurant";
    private static final long RESULT_RESTUARANT = 1000 ;
    private String testString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restaurantView = (TextView) findViewById(R.id.restuarantname);

        checkNetworkConnection(restaurantView);

//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                checkNetworkConnection(restaurantView);
//            }
//        },RESULT_RESTUARANT);
    }


    public void checkNetworkConnection(View view) {
        // Gets the URL from the UI's text field.
        String stringUrl = "http://10.0.2.2:8080/panda";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);

        } else {
            restaurantView.setText("No network connection available.");
        }
    }

    private String instantiateParse(InputStream inputStream) {
        XmlPullParserFactory pullParserFactory;
        try {

            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            return parseXML(parser);

        } catch (XmlPullParserException e) {
            return "There was an XmlPullParseException in instantiateParse() Method";
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return "There was an IOException in instantiateParse() Method";
        }
    }

    private String parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
    {
        ArrayList<Panda> pandas = null;
        int eventType = parser.getEventType();
        Panda currentProduct = null;
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                //testString = "{" + eventType + "}";
                case XmlPullParser.START_DOCUMENT:
                    pandas = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("post")){
                        currentProduct = new Panda();
                    } else if (currentProduct != null){
                        if (name.equals("content")){
                            currentProduct.name = parser.nextText();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("content") && currentProduct != null){
                        pandas.add(currentProduct);
                    }
            }
            eventType = parser.next();
        }
        return getPandaName(pandas);

    }
    private String getPandaName(ArrayList<Panda> pandas)
    {
        String content = "";
        Iterator<Panda> it = pandas.iterator();
        while(it.hasNext())
        {
            Panda currProduct  = it.next();
            content = currProduct.name;
        }

        if (content.equals("")) {
            content = "Panda Bite.";
        }
        return content;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            } catch (XmlPullParserException e) {
                return "Xml parser failed";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            restaurantView = (TextView) findViewById(R.id.restuarantname);
            restaurantView.setText(result);
        }

        private String downloadUrl(String myurl) throws IOException, XmlPullParserException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();


                return instantiateParse(is);

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }

        }

    }
}
