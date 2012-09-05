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

package no.java.schedule;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;


public class JZScheduleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final String ACCOUNT_NAME = "JavaZone Schedule";
        final String ACCOUNT_TYPE = "no.java.schedule";
        final String PROVIDER = "no.java.schedule";

        Account appAccount = new Account(ACCOUNT_NAME,ACCOUNT_TYPE);
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        if (accountManager.addAccountExplicitly(appAccount, null, null)) {
           ContentResolver.setIsSyncable(appAccount, PROVIDER, 1);
           ContentResolver.setSyncAutomatically(appAccount, PROVIDER, true);
        }
    }
}
