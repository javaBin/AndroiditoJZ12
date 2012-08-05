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

package no.java.android.apps.androidito.ui;

import com.google.analytics.tracking.EasyTracker;
import no.java.android.apps.androidito.R;
import no.java.android.apps.androidito.util.AccountUtils;
import no.java.android.apps.androidito.util.BeamUtils;
import no.java.android.apps.androidito.util.UIUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

/**
 * A base activity that handles common functionality in the app.
 */
public abstract class BaseActivity extends SherlockFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasyTracker.getTracker().setContext(this);

        /*
        TODO: Comment back in if G2CM shall be used
        // Show authentication screen if not authenticated
        if (!AccountUtils.isAuthenticated(this)) {
            AccountUtils.startAuthenticationFlow(this, getIntent());
            finish();
        }*/

        // If Android Beam APIs are available, set up the Beam easter egg as the default Beam
        // content. This can be overridden by subclasses.
        if (UIUtils.hasICS()) {
            BeamUtils.setDefaultBeamUri(this);
            if (!BeamUtils.isBeamUnlocked(this)) {
                BeamUtils.setBeamCompleteCallback(this,
                        new NfcAdapter.OnNdefPushCompleteCallback() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                            public void onNdefPushComplete(NfcEvent event) {
                                onBeamSent();
                            }
                        });
            }
        }

        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void onBeamSent() {
        if (!BeamUtils.isBeamUnlocked(this)) {
            BeamUtils.setBeamUnlocked(this);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(BaseActivity.this)
                            .setTitle(R.string.just_beamed)
                            .setMessage(R.string.beam_unlocked_default)
                            .setNegativeButton(R.string.close, null)
                            .setPositiveButton(R.string.view_beam_session,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface di, int i) {
                                            BeamUtils.launchBeamSession(BaseActivity.this);
                                            di.dismiss();
                                        }
                                    })
                            .create()
                            .show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (this instanceof HomeActivity) {
                    return false;
                }

                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Converts an intent into a {@link Bundle} suitable for use as fragment arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable("_uri");
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getTracker().trackActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getTracker().trackActivityStop(this);
    }
}
