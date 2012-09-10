package com.google.android.apps.iosched.ui;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.iosched.util.ImageCache;
import com.google.android.apps.iosched.util.ImageFetcher;
import no.java.schedule.R;
import no.java.schedule.adapter.TwitterStreamAdapter;
import no.java.schedule.io.model.Tweet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.android.apps.iosched.Config.*;

public class SocialStreamFragment extends SherlockListFragment implements AbsListView.OnScrollListener {
    private ImageFetcher imageFetcher;
    private TwitterStreamAdapter adapter;
    private ArrayList<Tweet> tweetArrayList;
    private String nextResult;
    private String refreshUrl;

    private int visibleThreshold = 5;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        if (this.imageFetcher == null){
            this.imageFetcher = new ImageFetcher(getActivity());
            this.imageFetcher.setImageCache(new ImageCache(getActivity(), "twitterCache"));
        }

        if (this.tweetArrayList == null){
            this.tweetArrayList = new ArrayList<Tweet>();
        }

        if (adapter == null){
            adapter = new TwitterStreamAdapter(getActivity(), R.layout.list_item_stream_activity, tweetArrayList, imageFetcher);
        }

        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        new TwitterSearchAsyncTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            refresh();
            return true;
        }

        return false;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        // Do nothing
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            new TwitterSearchAsyncTask().execute(nextResult);
            loading = true;
        }
    }

    public void refresh(){
        tweetArrayList.clear();
        adapter.notifyDataSetInvalidated();
        new TwitterSearchAsyncTask().execute();
    }

    public class TwitterSearchAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = null;
            if (strings.length == 0){
                httpGet = new HttpGet(TWITTER_SEARCH_URL + HASHTAG);
            } else {
                httpGet = new HttpGet(TWITTER_SEARCH_URL + strings[0]);
            }

            try {
                HttpResponse response = client.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                }
            } catch (ClientProtocolException e) {
                throw new IllegalArgumentException(e);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }

            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                nextResult = jsonObject.optString("next_page");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    Tweet tweet = new Tweet();
                    tweet.setId(object.getLong("id"));
                    tweet.setCreatedAt(new Date(object.getString("created_at")));
                    tweet.setUser(object.getString("from_user"));
                    tweet.setUserName(object.getString("from_user_name"));
                    tweet.setProfileImageUri(Uri.parse(object.getString("profile_image_url")));
                    tweet.setText(object.getString("text"));

                    tweetArrayList.add(tweet);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            adapter.notifyDataSetChanged();
            getSherlockActivity().setProgressBarIndeterminateVisibility(false);

        }
    }

    private static boolean containsId(List<Tweet> tweets, long id) {
        for (Tweet tweet : tweets) {
            if (tweet.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
