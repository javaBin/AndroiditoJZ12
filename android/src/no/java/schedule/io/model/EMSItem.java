/*
 * Copyright 2013 Google Inc.
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

public class EMSItem {


  public String getValue(String key){

    // Todo add lookup map
    for (EMSData emsData : data) {
      if (key.equalsIgnoreCase(emsData.name)){
        return emsData.value;
      }
    }

    return null;
  }

  public String[] getArray(String key){

     // Todo add lookup map
     for (EMSData emsData : data) {
       if (key.equalsIgnoreCase(emsData.name)){
         return emsData.array;
       }
     }

     return null;
   }

  public URI href;

  public EMSData[] data;
  public EMSLinks[] links;

  public EMSLinks getLink(final String rel) {
    for (EMSLinks link : links) {
      if(rel.equalsIgnoreCase(link.rel)){
        return link;
      }
    }

    return null;
  }

  public String getLinkHref(final String rel) {
    EMSLinks link = getLink(rel);
    if (link!=null){
      return link.href;
    } else {
      return null;
    }

  }
}
