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

package no.java.schedule.io.model;

import java.net.URI;

public class JZSessionsResult {

  public String bodyHtml;
  public JZDate start;
  public JZDate end;

  public String format;
  public String id;
  public JZLabel [] labels;

  public JZLevel level;
  public String room;

  public URI selfUri;
  public URI sessionHtmlUrl;

  public JZSpeaker speakers[];

  public String title;

  public String attending;

  public String labelstrings() {

    String result="";

    if (labels==null){
      return "";
    }

    for (int i = 0; i < labels.length; i++) {
      result+=labels[i].displayName+",";

    }

    return result;
  }

  public static JZSessionsResult from(final EMSItems pItem) {

    JZSessionsResult session = new JZSessionsResult();

    session.bodyHtml = pItem.getValue("body");
    //session.start =
    //session.end =
    session.format = pItem.getValue("format");
    session.id = pItem.href.toString();
    //session.labels = pItem.getValue("keywords"); // TODO
    session.level = new JZLevel(pItem.getValue("level"));
    session.room = pItem.getLinkHref("session room");
    session.selfUri = pItem.href;
    //session.sessionHtmlUrl
    //session.speakers
    session.title = pItem.getValue("title");
    //session.attending

    return session;

  }
}
