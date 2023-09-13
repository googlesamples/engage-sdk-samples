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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AccountTest {

  private Account account;
  private Ebook ebook;

  @Before
  public void setUp() {
    account = new Account();
    ebook = new Ebook(/* id= */ 1);
  }

  @Test
  public void markInProgressTest() {
    assertFalse(account.getInProgressEbookIds().contains(ebook.getId()));
    assertFalse(account.isEbookInProgress(ebook.getId()));

    long engagementTime = 100L;
    account.markInProgressPageInEbook(ebook, ebook.getNumPages(), engagementTime);

    assertEquals(ebook.getNumPages(), account.getCurrentPageOfInProgressEbook(ebook.getId()));
    assertEquals(engagementTime, account.getLastEngagementTimeOfInProgressEbook(ebook.getId()));
    assertTrue(account.isEbookInProgress(ebook.getId()));
    assertTrue(account.getInProgressEbookIds().contains(ebook.getId()));
  }

  @Test
  public void inProgressExceptionsTest() {
    assertThrows("No exception thrown getting in progress page of not in progress ebook.",
        IllegalArgumentException.class,
        () -> account.getCurrentPageOfInProgressEbook(ebook.getId()));
    assertThrows("No exception thrown with non-positive engagement time",
        IllegalArgumentException.class,
        () -> account.markInProgressPageInEbook(ebook, ebook.getNumPages(),
            /* engagementTime= */ -1L));
    assertThrows("No exception thrown when marking non-positive page number",
        IllegalArgumentException.class,
        () -> account.markInProgressPageInEbook(ebook, /* page= */ 0, /* engagementTime= */ 100L));
    assertThrows("No exception thrown when marked page number exceeds pages in ebook",
        IllegalArgumentException.class,
        () -> account.markInProgressPageInEbook(ebook, ebook.getNumPages() + 1,
            /*engagementTime= */ 100L));
  }
}
