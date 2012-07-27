/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.java.android.apps.androidito.util;

import com.google.analytics.tracking.EasyTracker;
import no.java.android.apps.androidito.R;
import no.java.android.apps.androidito.appwidget.MyScheduleWidgetProvider;
import no.java.android.apps.androidito.provider.ScheduleContract;
import no.java.android.apps.androidito.sync.ScheduleUpdaterService;
import no.java.android.apps.androidito.ui.MapFragment;
import no.java.android.apps.androidito.ui.SocialStreamActivity;
import no.java.android.apps.androidito.ui.SocialStreamFragment;

import com.actionbarsherlock.view.MenuItem;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ShareCompat;

import static no.java.android.apps.androidito.util.LogUtils.LOGD;
import static no.java.android.apps.androidito.util.LogUtils.makeLogTag;

/**
 * Helper class for dealing with common actions to take on a session.
 */
public final class SessionsHelper {

    private static final String TAG = makeLogTag(SessionsHelper.class);

    private final Activity mActivity;

    public SessionsHelper(Activity activity) {
        mActivity = activity;
    }

    public void startMapActivity(String roomId) {
        Intent intent = new Intent(mActivity.getApplicationContext(),
                UIUtils.getMapActivityClass(mActivity));
        intent.putExtra(MapFragment.EXTRA_ROOM, roomId);
        mActivity.startActivity(intent);
    }

    public Intent createShareIntent(int messageTemplateResId, String title, String hashtags,
            String url) {
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(mActivity)
                .setType("text/plain")
                .setText(mActivity.getString(messageTemplateResId,
                        title, UIUtils.getSessionHashtagsString(hashtags), url));
        return builder.getIntent();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void tryConfigureShareMenuItem(MenuItem menuItem, int messageTemplateResId,
            final String title, String hashtags, String url) {
        // TODO: uncomment pending ShareActionProvider fixes for split AB
//        if (UIUtils.hasICS()) {
//            ActionProvider itemProvider = menuItem.getActionProvider();
//            ShareActionProvider provider;
//            if (!(itemProvider instanceof ShareActionProvider)) {
//                provider = new ShareActionProvider(mActivity);
//            } else {
//                provider = (ShareActionProvider) itemProvider;
//            }
//            provider.setShareIntent(createShareIntent(messageTemplateResId, title, hashtags, url));
//            provider.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {
//                @Override
//                public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
//                    EasyTracker.getTracker().trackEvent("Session", "Shared", title, 0L);
//                    LOGD("Tracker", "Shared: " + title);
//                    return false;
//                }
//            });
//
//            menuItem.setActionProvider(provider);
//        }
    }

    public void shareSession(Context context, int messageTemplateResId, String title,
            String hashtags, String url) {
        EasyTracker.getTracker().trackEvent("Session", "Shared", title, 0L);
        LOGD("Tracker", "Shared: " + title);
        context.startActivity(Intent.createChooser(
                createShareIntent(messageTemplateResId, title, hashtags, url),
                context.getString(R.string.title_share)));
    }

    public void setSessionStarred(Uri sessionUri, boolean starred, String title) {
        LOGD(TAG, "setSessionStarred uri=" + sessionUri + " starred=" +
                starred + " title=" + title);
        sessionUri = ScheduleContract.addCallerIsSyncAdapterParameter(sessionUri);
        final ContentValues values = new ContentValues();
        values.put(ScheduleContract.Sessions.SESSION_STARRED, starred);
        AsyncQueryHandler handler =
                new AsyncQueryHandler(mActivity.getContentResolver()) {
                };
        handler.startUpdate(-1, null, sessionUri, values, null, null);

        EasyTracker.getTracker().trackEvent(
                "Session", starred ? "Starred" : "Unstarred", title, 0L);

        // Because change listener is set to null during initialization, these
        // won't fire on pageview.
        final Intent refreshIntent = new Intent(mActivity, MyScheduleWidgetProvider.class);
        refreshIntent.setAction(MyScheduleWidgetProvider.REFRESH_ACTION);
        mActivity.sendBroadcast(refreshIntent);

        // Sync to the cloud.
        final Intent updateServerIntent = new Intent(mActivity, ScheduleUpdaterService.class);
        updateServerIntent.putExtra(ScheduleUpdaterService.EXTRA_SESSION_ID,
                ScheduleContract.Sessions.getSessionId(sessionUri));
        updateServerIntent.putExtra(ScheduleUpdaterService.EXTRA_IN_SCHEDULE, starred);
        mActivity.startService(updateServerIntent);
    }

    public void startSocialStream(String hashtags) {
        Intent intent = new Intent(mActivity, SocialStreamActivity.class);
        intent.putExtra(SocialStreamFragment.EXTRA_QUERY, hashtags);
        mActivity.startActivity(intent);
    }
}
