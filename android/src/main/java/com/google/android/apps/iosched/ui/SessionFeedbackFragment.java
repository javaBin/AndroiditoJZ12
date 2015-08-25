package com.google.android.apps.iosched.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.iosched.io.model.Constants;
import com.google.android.apps.iosched.ui.widget.NumberRatingBar;
import com.google.android.apps.iosched.util.RestServiceDevNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.java.schedule.R;
import no.java.schedule.io.model.JZFeedback;

public class SessionFeedbackFragment extends Fragment {
    private static final String TAG = "SessionFeedbackFragment";
    private TextView mTitleText;
    private TextView mSubTitleText;
    private TextView mFeedbackCommentText;
    private FrameLayout mSubmitFeedback;

    // set ratings, and content ratings
    private RatingBar mRatingBarMandatory;
    private NumberRatingBar mRelevantRatingBar;
    private NumberRatingBar mContentRatingBar;
    private NumberRatingBar mSpeakerRatingBar;
    private static final long INTERVAL_TO_REDRAW_UI = 1000L;

    private String mSessionId;
    private RestServiceDevNull mDevNullService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_session_feedback, container, false);
        mTitleText = (TextView) rootView.findViewById(R.id.session_feedback_title);
        mSubTitleText = (TextView) rootView.findViewById(R.id.session_feedback_subtitle);
        mFeedbackCommentText = (TextView) rootView.findViewById(R.id.session_feedback_comments);
        Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());

        mSessionId = intent.getStringExtra(Constants.SESSION_ID);
        mTitleText.setText(intent.getStringExtra(Constants.SESSION_FEEDBACK_TITLE));
        mSubTitleText.setText(intent.getStringExtra(Constants.SESSION_FEEDBACK_SUBTITLE));
        mSubmitFeedback = (FrameLayout) rootView.findViewById(R.id.submit_feedback_button);

        initRatings(rootView);
        initSubmitFeedbackListener();
        //TODO for now it is testmode
        mDevNullService = RestServiceDevNull.getInstance("TEST", getActivity());
        return rootView;
    }

    private void initRatings(View rootView) {
        mRatingBarMandatory = (RatingBar) rootView.findViewById(R.id.rating_bar_0);
        mRelevantRatingBar = (NumberRatingBar) rootView.findViewById(R.id.rating_bar_1);
        mContentRatingBar = (NumberRatingBar) rootView.findViewById(R.id.rating_bar_2);
        mSpeakerRatingBar = (NumberRatingBar) rootView.findViewById(R.id.rating_bar_3);
    }

    private void initSubmitFeedbackListener() {
        mSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasFilledMandatoryRating()) {
                    Toast.makeText(getActivity(),
                            "You have to provide rating for the session before submitting feedback!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    submitAllFeedback();
                }
            }
        });
    }

    private boolean hasFilledMandatoryRating() {
        return mRatingBarMandatory.getRating() > 0;
    }

    private void submitAllFeedback() {
        int overallMandatoryRating = (int) mRatingBarMandatory.getRating();
        int contentRating = mContentRatingBar.getProgress();
        int relevantRating = mRelevantRatingBar.getProgress();
        int qualitySpeakerRating = mSpeakerRatingBar.getProgress();
        String sessionId = null;
        String eventId = null;

        Pattern pattern = Pattern.compile(".*\\/events\\/(.*)\\/sessions\\/(.*)");
        Matcher matcher = pattern.matcher(mSessionId);

        if (matcher.matches()) {
            sessionId = matcher.group(1);
            eventId = matcher.group(2);
        }

        String feedbackComment = mFeedbackCommentText.getText().toString();
        JZFeedback jzFeedback = new JZFeedback(overallMandatoryRating, relevantRating,
                contentRating, qualitySpeakerRating, feedbackComment);

        // mDevNullService.submitFeedbackToDevNull(eventId, sessionId, generateUniqueVoterId(),jzFeedback);
        mDevNullService.submitFeedbackTestToDevNull(generateUniqueVoterId(), jzFeedback);
    }

    public String generateUniqueVoterId() {
        return Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
