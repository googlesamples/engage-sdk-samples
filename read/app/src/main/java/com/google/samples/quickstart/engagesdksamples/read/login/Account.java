/* Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.samples.quickstart.engagesdksamples.read.login;

import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Contains data of a logged in user including place in ebooks and completed ebooks. */
public class Account implements Serializable {

  private final Map<Integer, Integer> inProgressEbookIdToPage;
  private final Map<Integer, Long> inProgressEbookIdToLastEngagementTime;

  public Account() {
    inProgressEbookIdToPage = new HashMap<>();
    inProgressEbookIdToLastEngagementTime = new HashMap<>();
  }

  /**
   * Marks a users page in an ebook.
   */
  public void markInProgressPageInEbook(Ebook ebook, int page, long engagementTime) {
    int numPages = ebook.getNumPages();
    if (page <= 0) {
      throw new IllegalArgumentException("Page to be marked is non-positive. Page is " + page);
    } else if (page > numPages) {
      throw new IllegalArgumentException(
          "Page to be marked is greater than number of pages in "
              + "ebook. Page is "
              + page
              + " and the Ebook "
              + ebook
              + " has "
              + numPages
              + " pages.");
    } else if (engagementTime <= 0) {
      throw new IllegalArgumentException(
          "Engagement time must be positive. " + "Engagement time is " + engagementTime);
    }
    inProgressEbookIdToPage.put(ebook.getId(), page);
    inProgressEbookIdToLastEngagementTime.put(ebook.getId(), engagementTime);
  }

  /**
   * Returns whether an ebook with ID ebookId is in progress for the account.
   */
  public boolean isEbookInProgress(int ebookId) {
    return inProgressEbookIdToPage.containsKey(ebookId);
  }

  /**
   * Returns the account's current page in an ebook. Throws an IllegalArgumentException if the ebook
   * is not in progress.
   */
  public int getCurrentPageOfInProgressEbook(int ebookId) {
    Integer currPage = inProgressEbookIdToPage.get(ebookId);
    if (currPage == null) {
      throw new IllegalArgumentException("Ebook with id " + ebookId + " is not in progress.");
    }
    return currPage;
  }

  /**
   * Returns the account's last engagement time in an ebook. Throws an IllegalArgumentException if
   * the ebook is not in progress.
   */
  public long getLastEngagementTimeOfInProgressEbook(int ebookId) {
    Long engagementTime = inProgressEbookIdToLastEngagementTime.get(ebookId);
    if (engagementTime == null) {
      throw new IllegalArgumentException("Ebook with id " + ebookId + " is not in progress.");
    }
    return engagementTime;
  }


  /**
   * Returns a set of the account's currently in progress ebooks.
   */
  public Set<Integer> getInProgressEbookIds() {
    return inProgressEbookIdToPage.keySet();
  }

}
