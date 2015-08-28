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
import android.graphics.Color;
import com.google.android.apps.iosched.provider.ScheduleContract;
import com.google.android.apps.iosched.util.Lists;
import com.google.gson.Gson;
import no.java.schedule.io.model.JZLabel;
import no.java.schedule.io.model.JZSessionsResponse;
import no.java.schedule.io.model.JZSessionsResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.google.android.apps.iosched.util.LogUtils.makeLogTag;

/**
 * Handler that parses track JSON data into a list of content provider operations.
 */
public class TracksHandler extends JSONHandler {

    private static final String TAG = makeLogTag(TracksHandler.class);

    public TracksHandler(Context context) {
        super(context);
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(String json)
            throws IOException {

      JZSessionsResponse response = new Gson().fromJson(json, JZSessionsResponse.class);


        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        batch.add(ContentProviderOperation.newDelete(
                ScheduleContract.addCallerIsSyncAdapterParameter(
                        ScheduleContract.Tracks.CONTENT_URI)).build());

      List<JZSessionsResult> sessions = SessionsHandler.toJZSessionResultList(response.collection.items);

      HashSet<JZLabel> labels = new HashSet<JZLabel>();

      for (JZSessionsResult session : sessions) {
        Collections.addAll(labels, session.labels);
      }

      for (JZLabel label : labels) {
        parseTrack(label, batch);
      }

        return batch;
    }

    private static void parseTrack(JZLabel track, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                ScheduleContract.addCallerIsSyncAdapterParameter(
                        ScheduleContract.Tracks.CONTENT_URI));
        builder.withValue(ScheduleContract.Tracks.TRACK_ID,
                ScheduleContract.Tracks.generateTrackId(track.id));
        builder.withValue(ScheduleContract.Tracks.TRACK_NAME, track.displayName);
        builder.withValue(ScheduleContract.Tracks.TRACK_COLOR, Color.TRANSPARENT);//TODO - fetch icon and derive color...?
        builder.withValue(ScheduleContract.Tracks.TRACK_ABSTRACT, "");//TODO
        batch.add(builder.build());
    }
}