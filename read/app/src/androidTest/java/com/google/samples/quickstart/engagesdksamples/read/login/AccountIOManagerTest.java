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
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.Optional;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AccountIOManagerTest {

  private static final String TEST_ACCOUNT_FILE_NAME = "test-account";

  private Context appContext;
  private Account freshAccount;
  private Ebook ebook;

  @Before
  public void setUp() {
    appContext = ApplicationProvider.getApplicationContext();
    freshAccount = new Account();
    ebook = new Ebook(/* id= */ 1);
    AccountIOManager.deleteAccount(appContext, TEST_ACCOUNT_FILE_NAME);
  }

  @After
  public void cleanUp() {
    AccountIOManager.deleteAccount(appContext, TEST_ACCOUNT_FILE_NAME);
  }

  @Test
  public void saveThenLoadPreservesDataTest() {
    int numPages = ebook.getNumPages();
    long engagementTime = 100L;
    freshAccount.markInProgressPageInEbook(ebook, numPages, engagementTime);
    AccountIOManager.saveAccount(appContext, freshAccount, TEST_ACCOUNT_FILE_NAME);
    Optional<Account> loadedAccount =
        AccountIOManager.loadAccount(appContext, TEST_ACCOUNT_FILE_NAME);
    assertTrue(loadedAccount.isPresent());
    assertEquals(numPages, loadedAccount.get().getCurrentPageOfInProgressEbook(ebook.getId()));
    assertEquals(
        engagementTime, loadedAccount.get().getLastEngagementTimeOfInProgressEbook(ebook.getId()));
  }

  @Test
  public void deleteTest() {
    AccountIOManager.saveAccount(appContext, freshAccount, TEST_ACCOUNT_FILE_NAME);
    assertTrue(AccountIOManager.loadAccount(appContext, TEST_ACCOUNT_FILE_NAME).isPresent());
    AccountIOManager.deleteAccount(appContext, TEST_ACCOUNT_FILE_NAME);
    assertFalse(AccountIOManager.loadAccount(appContext, TEST_ACCOUNT_FILE_NAME).isPresent());
  }
}
