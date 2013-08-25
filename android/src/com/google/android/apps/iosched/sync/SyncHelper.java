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

package com.google.android.apps.iosched.sync;

import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.apps.iosched.Config;
import com.google.android.apps.iosched.io.HandlerException;
import com.google.android.apps.iosched.io.JSONHandler;
import com.google.android.apps.iosched.io.SessionsHandler;
import com.google.android.apps.iosched.io.TracksHandler;
import com.google.android.apps.iosched.io.model.ErrorResponse;
import com.google.android.apps.iosched.provider.ScheduleContract;
import com.google.android.apps.iosched.util.UIUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import static com.google.android.apps.iosched.util.LogUtils.*;

/**
 * A helper class for dealing with sync and other remote persistence operations.
 * All operations occur on the thread they're called from, so it's best to wrap
 * calls in an {@link android.os.AsyncTask}, or better yet, a
 * {@link android.app.Service}.
 */
public class SyncHelper {

    private static final String TAG = makeLogTag(SyncHelper.class);

    static {
        // Per http://android-developers.blogspot.com/2011/09/androids-http-clients.html
        if (!UIUtils.hasFroyo()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    public static final int FLAG_SYNC_LOCAL = 0x1;
    public static final int FLAG_SYNC_REMOTE = 0x2;

    private static final int LOCAL_VERSION_CURRENT = 19;

    private Context mContext;
    private String mUserAgent;

    public SyncHelper(Context context) {
        mContext = context;
        mUserAgent = buildUserAgent(context);
    }

    /**
     * Loads conference information (sessions, rooms, tracks, speakers, etc.)
     * from a local static cache data and then syncs down data from the
     * Conference API.
     * 
     * @param syncResult Optional {@link SyncResult} object to populate.
     * @throws IOException
     */
    public void performSync(SyncResult syncResult) throws IOException {


        // Bulk of sync work, performed by executing several fetches from
        // local and online sources.
        final ContentResolver resolver = mContext.getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        LOGI(TAG, "Performing sync");


      try {
        final long startRemote = System.currentTimeMillis();
        LOGI(TAG, "Remote syncing sessions");
        batch.addAll(fetchResource( Config.GET_ALL_SESSIONS_URL, new SessionsHandler(mContext, false, false)));
        LOGI(TAG, "Remote syncing tracks");
        batch.addAll(fetchResource( Config.GET_ALL_SESSIONS_URL, new TracksHandler(mContext)));

        //TODO Enable announcements for JavaZone
        //LOGI(TAG, "Remote syncing announcements");
        //batch.addAll(executeGet(Config.GET_ALL_ANNOUNCEMENTS_URL,
        //        new AnnouncementsHandler(mContext, false), auth));

        LOGD(TAG, "Remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");
        if (syncResult != null) {
          ++syncResult.stats.numUpdates;
          ++syncResult.stats.numEntries;
        }

        EasyTracker.getTracker().dispatch();

      } catch (HandlerException.UnauthorizedException e) {
        LOGI(TAG, "Unauthorized; getting a new auth token.", e);
        if (syncResult != null) {
          ++syncResult.stats.numAuthExceptions;
        }

      }
      // all other IOExceptions are thrown


      try {
            // Apply all queued up remaining batch operations (only remote content at this point).
            resolver.applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);

            // Delete empty blocks
            Cursor emptyBlocksCursor = resolver.query(ScheduleContract.Blocks.CONTENT_URI,
                    new String[]{ScheduleContract.Blocks.BLOCK_ID,ScheduleContract.Blocks.SESSIONS_COUNT},
                    ScheduleContract.Blocks.EMPTY_SESSIONS_SELECTION, null, null);
            batch = new ArrayList<ContentProviderOperation>();
            int numDeletedEmptyBlocks = 0;
            while (emptyBlocksCursor.moveToNext()) {
                batch.add(ContentProviderOperation
                        .newDelete(ScheduleContract.Blocks.buildBlockUri(
                                emptyBlocksCursor.getString(0)))
                        .build());
                ++numDeletedEmptyBlocks;
            }
            emptyBlocksCursor.close();
            resolver.applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);
            LOGD(TAG, "Deleted " + numDeletedEmptyBlocks + " empty session blocks.");
        } catch (RemoteException e) {
            throw new RuntimeException("Problem applying batch operation", e);
        } catch (OperationApplicationException e) {
            throw new RuntimeException("Problem applying batch operation", e);
        }

        if (UIUtils.hasICS()) {
            LOGD(TAG, "Done with sync'ing conference data. Starting to sync "
                    + "session with Calendar.");
            syncCalendar();
        }
    }

    private void syncCalendar() {
        //Intent intent = new Intent(SessionCalendarService.ACTION_UPDATE_ALL_SESSIONS_CALENDAR);
        //intent.setClass(mContext, SessionCalendarService.class);
        //mContext.startService(intent);
    }

    /**
     * Build and return a user-agent string that can identify this application
     * to remote servers. Contains the package name and version code.
     */
    private static String buildUserAgent(Context context) {
        String versionName = "unknown";
        int versionCode = 0;

        try {
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return context.getPackageName() + "/" + versionName + " (" + versionCode + ") (gzip)";
    }

    public void addOrRemoveSessionFromSchedule(Context context, String sessionId,
            boolean inSchedule) throws IOException {
      //TODO sync at EMS server
      /**
        mAuthToken = AccountUtils.getAuthToken(mContext);
        JsonObject starredSession = new JsonObject();
        starredSession.addProperty("sessionid", sessionId);

        byte[] postJsonBytes = new Gson().toJson(starredSession).getBytes();

        URL url = new URL(Config.EDIT_MY_SCHEDULE_URL + (inSchedule ? "add" : "remove"));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("User-Agent", mUserAgent);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer " + mAuthToken);
        urlConnection.setDoOutput(true);
        urlConnection.setFixedLengthStreamingMode(postJsonBytes.length);

        LOGD(TAG, "Posting to URL: " + url);
        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
        out.write(postJsonBytes);
        out.flush();

        urlConnection.connect();
        throwErrors(urlConnection);
        String json = readInputStream(urlConnection.getInputStream());
        EditMyScheduleResponse response = new Gson().fromJson(json,
                EditMyScheduleResponse.class);
        if (!response.success) {
            String responseMessageLower = (response.message != null)
                    ? response.message.toLowerCase()
                    : "";

            if (responseMessageLower.contains("no profile")) {
                throw new HandlerException.NoDevsiteProfileException();
            }
        }
       **/
    }

    public ArrayList<ContentProviderOperation> fetchResource( String urlString, JSONHandler handler) throws IOException {

      String response = null;

      if (isFirstRun()) {
         response = getLocalResource(mContext, urlString);
      } else if (isOnline(mContext)) {
          response = getHttpResource(urlString);
      }

      if (response!=null && !response.trim().equals("")){
        return handler.parse(response);
      } else {
        return new ArrayList<ContentProviderOperation>();
      }
    }


  private boolean isFirstRun(){
    return isFirstRun(mContext);
  }

  public static boolean isFirstRun(Context context) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getBoolean("first_run", true);
  }

  public static String getLocalResource(Context pContext,String pUrlString) {

    pUrlString = pUrlString.replaceFirst("http://", "");

    //fix file/directory clashes....
    if (pUrlString.endsWith("/sessions")) {
      pUrlString=pUrlString+".json";
    }

    LOGD("LocalResourceUrls", pUrlString);

    try {
      InputStream asset = pContext.getAssets().open(pUrlString);


      LOGD("LocalResourceUrls Found",pUrlString);
      return convertStreamToString(asset);

    }
    catch (IOException e) {
      LOGE("LocalResourceUrls NotFound",pUrlString);
      LOGE(makeLogTag(SyncHelper.class),"Exception reading asset",e);
      return null;
    }

  }

  static String convertStreamToString(java.io.InputStream is) {
      Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
  }

  public static String getHttpResource(final String urlString) throws IOException {
    LOGD(TAG, "Requesting URL: " + urlString);
    URL url = new URL(urlString);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestProperty("User-Agent", "Androidito JZ13");

    urlConnection.setRequestProperty("Accept","application/json");

    urlConnection.connect();
    throwErrors(urlConnection);

    String response = readInputStream(urlConnection.getInputStream());
    LOGV(TAG, "HTTP response: " + response);
    return response;
  }

  private static void throwErrors(HttpURLConnection urlConnection) throws IOException {
        final int status = urlConnection.getResponseCode();
        if (status < 200 || status >= 300) {
            String errorMessage = null;
            try {
                String errorContent = readInputStream(urlConnection.getErrorStream());
                LOGV(TAG, "Error content: " + errorContent);
                ErrorResponse errorResponse = new Gson().fromJson(
                        errorContent, ErrorResponse.class);
                errorMessage = errorResponse.error.message;
            } catch (IOException ignored) {
            } catch (JsonSyntaxException ignored) {
            }

            String exceptionMessage = "Error response "
                    + status + " "
                    + urlConnection.getResponseMessage()
                    + (errorMessage == null ? "" : (": " + errorMessage))
                    + " for " + urlConnection.getURL();

            // TODO: the API should return 401, and we shouldn't have to parse the message
            throw (errorMessage != null && errorMessage.toLowerCase().contains("auth"))
                    ? new HandlerException.UnauthorizedException(exceptionMessage)
                    : new HandlerException(exceptionMessage);
        }
    }

    public static String readInputStream(InputStream inputStream)
            throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseLine;
        StringBuilder responseBuilder = new StringBuilder();
        while ((responseLine = bufferedReader.readLine()) != null) {
            responseBuilder.append(responseLine);
        }
        return responseBuilder.toString();
    }

    public static boolean isOnline(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
