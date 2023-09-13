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
package com.google.samples.quickstart.engagesdksamples.read;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.common.base.Optional;
import com.google.samples.quickstart.engagesdksamples.read.login.Account;
import com.google.samples.quickstart.engagesdksamples.read.login.AccountIOManager;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;

/** View model handling account management for the main activity. */
public class MainActivityViewModel extends AndroidViewModel {

  private static final String TAG = MainActivityViewModel.class.getSimpleName();

  @NonNull private Optional<Account> loggedInAccount = Optional.absent();

  @NonNull private Optional<Ebook> inViewEbook = Optional.absent();

  @NonNull
  private final MutableLiveData<Optional<Integer>> inViewEbookCurrPage = new MutableLiveData<>();

  public MainActivityViewModel(@NonNull Application application) {
    super(application);
  }

  /**
   * Saves a logged in account if present.
   */
  public void saveAccount() {
    if (loggedInAccount.isPresent()) {
      AccountIOManager.saveAccount(getApplication(), loggedInAccount.get());
      Log.i(TAG, "saveAccount: Successfully saved");
    }
  }

  /**
   * Loads the saved account if present.
   */
  public void loadAccount() {
    Optional<Account> optionalLoadedAccount = AccountIOManager.loadAccount(getApplication());
    if (optionalLoadedAccount.isPresent()) {
      Log.i(TAG, "loadAccount: Account successfully loaded");
    }
    loggedInAccount = optionalLoadedAccount;
  }

  /**
   * Deletes any saved account and logs the user out.
   */
  public void deleteAccount() {
    loggedInAccount = Optional.absent();
    AccountIOManager.deleteAccount(getApplication());
  }

  /**
   * Sets the user's logged in account. Does nothing if the user is already logged in, otherwise
   * creates a new account.
   */
  public void logIn() {
    if (loggedInAccount.isPresent()) {
      Log.i(TAG, "logIn: Already logged in");
      return;
    }
    Log.i(TAG, "logIn: New account created");
    loggedInAccount = Optional.of(new Account());
  }

  /**
   * Returns whether an account is logged in.
   */
  public boolean isLoggedIn() {
    return loggedInAccount.isPresent();
  }

  /**
   * Sets ebook as in view and retrieves and sets the progress through the ebook if an account is
   * signed in and the ebook is in progress for the account.
   */
  public void setInViewEbook(Ebook ebook) {
    inViewEbook = Optional.of(ebook);
    int ebookId = ebook.getId();
    if (loggedInAccount.isPresent() && loggedInAccount.get().isEbookInProgress(ebookId)) {
      int currPage = loggedInAccount.get().getCurrentPageOfInProgressEbook(ebookId);
      inViewEbookCurrPage.setValue(Optional.of(currPage));
    } else {
      inViewEbookCurrPage.setValue(Optional.absent());
    }
  }

  /**
   * Marks the given page and engagement time in the logged in account for the in view ebook. If no
   * ebook is in view, or no account is present, throws an IllegalStateException, although this
   * should not occur.
   */
  public void markPageOfInViewEbook(int page, long engagementTime) {
    if (!inViewEbook.isPresent()) {
      throw new IllegalStateException("Tried to mark a page when no ebook is in view");
    } else if (!loggedInAccount.isPresent()) {
      throw new IllegalStateException("Tried to mark a page when no account is logged in");
    }
    inViewEbookCurrPage.setValue(Optional.of(page));
    loggedInAccount.get().markInProgressPageInEbook(inViewEbook.get(), page, engagementTime);
  }

  /**
   * Removes the in view ebook and associated progress through it if present.
   */
  public void resetInViewEbook() {
    inViewEbook = Optional.absent();
    inViewEbookCurrPage.setValue(Optional.absent());
  }

  /**
   * @return A livedata of the current page of the in progress Ebook. This page may not be present
   *     if no account is logged in or the ebook has not been started on the logged in account.
   */
  public LiveData<Optional<Integer>> getInViewEbookPage() {
    return inViewEbookCurrPage;
  }
}
