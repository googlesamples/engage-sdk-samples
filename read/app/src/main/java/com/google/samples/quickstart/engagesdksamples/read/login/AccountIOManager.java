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

import android.content.Context;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import com.google.common.base.Optional;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** Handles account saving, loading, and deleting from app-specific internal storage. */
public final class AccountIOManager {

  private static final String TAG = AccountIOManager.class.getSimpleName();
  private static final String ACCOUNT_FILE_NAME = "account";

  /**
   * Saves an account to app-specific internal storage.
   *
   * @param appContext Application's context
   * @param account Account to save
   */
  public static void saveAccount(Context appContext, Account account) {
    saveAccount(appContext, account, ACCOUNT_FILE_NAME);
  }

  @VisibleForTesting
  static void saveAccount(Context appContext, Account account, String fileName) {
    try {
      FileOutputStream fileOutputStream = appContext.openFileOutput(fileName, Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

      objectOutputStream.writeObject(account);

      objectOutputStream.close();
      fileOutputStream.close();
    } catch (IOException exception) {
      Log.e(TAG, "save: ", exception);
    }
  }

  /**
   * Loads and returns the saved account from app-specific internal storage if present.
   *
   * @param appContext Application's context.
   * @return Saved account. Returns Optional.absent() if no account is saved.
   */
  public static Optional<Account> loadAccount(Context appContext) {
    return loadAccount(appContext, ACCOUNT_FILE_NAME);
  }

  @VisibleForTesting
  static Optional<Account> loadAccount(Context appContext, String fileName) {
    Optional<Account> account = Optional.absent();
    try {
      FileInputStream fileInputStream = appContext.openFileInput(fileName);
      ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
      account = Optional.of((Account) objectInputStream.readObject());

      objectInputStream.close();
      fileInputStream.close();
    } catch (IOException | ClassNotFoundException exception) {
      Log.e(TAG, "load: ", exception);
    }
    return account;
  }

  /**
   * Deletes the saved account if present.
   *
   * @param appContext Application's context
   */
  public static void deleteAccount(Context appContext) {
    deleteAccount(appContext, ACCOUNT_FILE_NAME);
  }

  @VisibleForTesting
  static void deleteAccount(Context appContext, String fileName) {
    appContext.deleteFile(fileName);
  }

  private AccountIOManager() {}
}
