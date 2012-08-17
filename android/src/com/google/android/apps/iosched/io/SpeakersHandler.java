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

package com.google.android.apps.iosched.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import com.google.android.apps.iosched.provider.ScheduleContract;
import com.google.android.apps.iosched.provider.ScheduleContract.SyncColumns;
import com.google.android.apps.iosched.util.Lists;
import com.google.gson.Gson;
import com.lokling.androidito.iosched.io.model.JZSessionsResponse;
import com.lokling.androidito.iosched.io.model.JZSessionsResult;
import com.lokling.androidito.iosched.io.model.JZSpeaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.android.apps.iosched.provider.ScheduleContract.Speakers;
import static com.google.android.apps.iosched.util.LogUtils.LOGI;
import static com.google.android.apps.iosched.util.LogUtils.makeLogTag;

/**
 * Handler that parses speaker JSON data into a list of content provider operations.
 */
public class SpeakersHandler extends JSONHandler {

    private static final String TAG = makeLogTag(SpeakersHandler.class);

    public SpeakersHandler(Context context, boolean local) {
        super(context);
    }

    public ArrayList<ContentProviderOperation> parse(String json)
            throws IOException {
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

        JZSessionsResponse response = new Gson().fromJson(json, JZSessionsResponse.class);

        List<JZSpeaker> speakers = new ArrayList<JZSpeaker>();
        JZSessionsResult[] sessions = response.sessions;

        for (JZSessionsResult session : sessions) {
          Collections.addAll(speakers, session.speakers);
        }

        int numEvents = 0;
        if (speakers != null) {
            numEvents = speakers.size();
        }

        if (numEvents > 0) {
            LOGI(TAG, "Updating speakers data");

            // Clear out existing speakers
            batch.add(ContentProviderOperation
                    .newDelete(ScheduleContract.addCallerIsSyncAdapterParameter(
                            Speakers.CONTENT_URI))
                    .build());


          for (JZSpeaker speaker : speakers) {
                String speakerId = speaker.name;

                // Insert speaker info
                batch.add(ContentProviderOperation
                        .newInsert(ScheduleContract
                                .addCallerIsSyncAdapterParameter(Speakers.CONTENT_URI))
                        .withValue(SyncColumns.UPDATED, System.currentTimeMillis())
                        .withValue(Speakers.SPEAKER_ID, speakerId)
                        .withValue(Speakers.SPEAKER_NAME, speaker.name)
                        .withValue(Speakers.SPEAKER_ABSTRACT, speaker.bioHtml)
                        .withValue(Speakers.SPEAKER_IMAGE_URL, speaker.photoUrl.toString())
                        .withValue(Speakers.SPEAKER_URL, "")//TODO
                        .build());
            }
        }

        return batch;
    }
}
