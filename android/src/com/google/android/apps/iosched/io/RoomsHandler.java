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
import com.google.android.apps.iosched.util.Lists;
import com.google.android.apps.iosched.util.ParserUtils;
import com.google.gson.Gson;
import no.java.schedule.io.model.JZSessionsResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.google.android.apps.iosched.util.LogUtils.makeLogTag;

/**
 * Handler that parses room JSON data into a list of content provider operations.
 */
public class RoomsHandler extends JSONHandler {

    private static final String TAG = makeLogTag(RoomsHandler.class);

    public RoomsHandler(Context context) {
        super(context);
    }

    public ArrayList<ContentProviderOperation> parse(String json) throws IOException {

      JZSessionsResponse response = new Gson().fromJson(json, JZSessionsResponse.class);

      Set<String> rooms = new HashSet<String>();

      //TODO
      //for (JZSessionsResult session : response.collection) {
      //  rooms.add(session.room);
      //}


      final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
      Object[] roomArray = rooms.toArray();
        int noOfRooms = rooms.size();
        for (int i = 0; i < noOfRooms; i++) {
            parseRoom(String.valueOf(roomArray[i]), batch);
        }
        return batch;
    }

    private static void parseRoom(String room, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(ScheduleContract.addCallerIsSyncAdapterParameter(
                        ScheduleContract.Rooms.CONTENT_URI));
        builder.withValue(ScheduleContract.Rooms.ROOM_ID, ParserUtils.sanitizeId(room));
        builder.withValue(ScheduleContract.Rooms.ROOM_NAME, room);
        builder.withValue(ScheduleContract.Rooms.ROOM_FLOOR, "");
        batch.add(builder.build());
    }
}
