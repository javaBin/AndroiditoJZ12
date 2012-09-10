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

package no.java.schedule.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import com.actionbarsherlock.ActionBarSherlock;
import no.java.schedule.R;


public class JZPreferenceActivity extends PreferenceActivity {
    private ActionBarSherlock sherlock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sherlock = ActionBarSherlock.wrap(this);//,ActionBarSherlock.FLAG_DELEGATE);
        sherlock.getActionBar().setHomeButtonEnabled(true);
    }

    @Override
       public boolean onOptionsItemSelected(MenuItem item) {
           switch (item.getItemId()) {
               case android.R.id.home:

                   NavUtils.navigateUpFromSameTask(this);
                   return true;
           }
           return super.onOptionsItemSelected(item);
       }

}