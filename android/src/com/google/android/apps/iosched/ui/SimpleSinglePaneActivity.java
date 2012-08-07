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

package com.google.android.apps.iosched.ui;

import com.lokling.androidito.iosched.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A {@link BaseActivity} that simply contains a single fragment. The intent used to invoke this
 * activity is forwarded to the fragment as arguments during fragment instantiation. Derived
 * activities should only need to implement {@link SimpleSinglePaneActivity#onCreatePane()}.
 */
public abstract class SimpleSinglePaneActivity extends BaseActivity {
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepane_empty);

        if (getIntent().hasExtra(Intent.EXTRA_TITLE)) {
            setTitle(getIntent().getStringExtra(Intent.EXTRA_TITLE));
        }

        final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        setTitle(customTitle != null ? customTitle : getTitle());

        if (savedInstanceState == null) {
            mFragment = onCreatePane();
            mFragment.setArguments(intentToFragmentArguments(getIntent()));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root_container, mFragment, "single_pane")
                    .commit();
        } else {
            mFragment = getSupportFragmentManager().findFragmentByTag("single_pane");
        }
    }

    /**
     * Called in <code>onCreate</code> when the fragment constituting this activity is needed.
     * The returned fragment's arguments will be set to the intent used to invoke this activity.
     */
    protected abstract Fragment onCreatePane();

    public Fragment getFragment() {
        return mFragment;
    }
}
