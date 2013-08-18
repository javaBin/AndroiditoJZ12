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
import com.google.android.apps.iosched.provider.ScheduleContract.Blocks;
import com.google.android.apps.iosched.util.Lists;
import com.google.android.apps.iosched.util.ParserUtils;
import com.google.gson.Gson;
import no.java.schedule.io.model.EMSCollection;
import no.java.schedule.io.model.EMSItems;
import no.java.schedule.io.model.JZSlotsResponse;

import java.io.IOException;
import java.util.ArrayList;

import static com.google.android.apps.iosched.util.LogUtils.LOGE;
import static com.google.android.apps.iosched.util.LogUtils.LOGV;
import static com.google.android.apps.iosched.util.LogUtils.makeLogTag;


public class BlocksHandler extends JSONHandler {

    private static final String TAG = makeLogTag(BlocksHandler.class);

    public BlocksHandler(Context context) {
        super(context);
    }

    public ArrayList<ContentProviderOperation> parse(String json) throws IOException {
      final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
      try {
        Gson gson = new Gson();
        JZSlotsResponse response = gson.fromJson(json, JZSlotsResponse.class);
        EMSCollection eventSlots = response.collection;

        for (EMSItems slot : eventSlots.items) {
          parseSlot(slot, batch);
        }
      } catch (Throwable e) {
        LOGE(TAG, e.toString());
      }

        return batch;
    }

    private static void parseSlot(EMSItems slot, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(ScheduleContract.addCallerIsSyncAdapterParameter(Blocks.CONTENT_URI));
        //LOGD(TAG, "Inside parseSlot:" + date + ",  " + slot);
        String startTime = slot.getValue("start");
        String endTime = slot.getValue("end");

        String type = "N_D";
        if (slot.getValue("type") != null) {
            type = slot.getValue("type");
        }
        String title = "N_D";
        if (slot.getValue("title") != null) {
            title = slot.getValue("title");
        }

        String meta = "N_D";
        if (slot.getValue("meta") != null) {
                title = slot.getValue("meta");
        }

        LOGV(TAG, "startTime:" + startTime);
        long startTimeL = ParserUtils.parseTime(startTime);
        long endTimeL = ParserUtils.parseTime(endTime);
        final String blockId = slot.href.toString();//Blocks.generateBlockId(startTimeL, endTimeL);

        LOGV(TAG, "blockId:" + blockId);
        LOGV(TAG, "title:" + title);
        LOGV(TAG, "start:" + startTimeL);
        builder.withValue(Blocks.BLOCK_ID, blockId);
        builder.withValue(Blocks.BLOCK_TITLE, title);
        builder.withValue(Blocks.BLOCK_START, startTimeL);
        builder.withValue(Blocks.BLOCK_END, endTimeL);
        builder.withValue(Blocks.BLOCK_TYPE, type);
        builder.withValue(Blocks.BLOCK_META, meta);
        batch.add(builder.build());
    }
}
