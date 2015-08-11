package no.java.schedule.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.apps.iosched.util.ImageCache;
import com.google.android.apps.iosched.util.ImageFetcher;
import no.java.schedule.R;
import no.java.schedule.io.model.Tweet;

import java.util.Date;
import java.util.List;

public class TwitterStreamAdapter extends ArrayAdapter<Tweet> {

    private static final int VIEW_TYPE_ACTIVITY = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private Context context;
    private int layoutResId;
    private List<Tweet> tweetList;
    private ImageFetcher imageFetcher;

    public TwitterStreamAdapter(Context context, int layoutResId, List<Tweet> tweetList, ImageFetcher imageFetcher) {
        super(context, layoutResId, tweetList);
        this.context = context;
        this.layoutResId = layoutResId;
        this.tweetList = tweetList;
        this.imageFetcher = imageFetcher;
    }

    @Override
    public Tweet getItem(int position){
        return tweetList.get(position);
    }
    @Override
    public int getCount(){
        return tweetList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TweetHolder holder = null;

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResId, parent, false);

            holder = new TweetHolder();
            holder.userAvatar = (ImageView)row.findViewById(R.id.stream_user_avatar);
            holder.user = (TextView)row.findViewById(R.id.stream_user_name);
            holder.time = (TextView)row.findViewById(R.id.stream_time);
            holder.text = (TextView)row.findViewById(R.id.stream_text);
            row.setTag(holder);
        } else {
            holder = (TweetHolder)row.getTag();
        }

        Tweet tweet = tweetList.get(position);

        imageFetcher.loadImage(tweet.getProfileImageUri().toString(), holder.userAvatar);
        holder.user.setText(tweet.getUserName() + " (" + tweet.getUser() + ")");
        holder.time.setText(DateUtils.getRelativeTimeSpanString(tweet.getCreatedAt().getTime(), new Date().getTime(), 0l, DateUtils.FORMAT_ABBREV_TIME));
        holder.text.setText(tweet.getText());

        this.notifyDataSetChanged();
        return row;
    }

    static class TweetHolder {
        ImageView userAvatar;
        TextView user;
        TextView time;
        TextView text;
    }
}
