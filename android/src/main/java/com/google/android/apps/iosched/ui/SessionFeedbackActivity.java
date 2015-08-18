package com.google.android.apps.iosched.ui;

import android.support.v4.app.Fragment;

public class SessionFeedbackActivity extends SimpleSinglePaneActivity {
    @Override
    protected Fragment onCreatePane() {
        return new SessionFeedbackFragment();
    }
}
