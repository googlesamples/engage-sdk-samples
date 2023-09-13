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
package com.google.samples.quickstart.engagesdksamples.watch.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.samples.quickstart.engagesdksamples.watch.data.model.Account
import com.google.samples.quickstart.engagesdksamples.watch.data.model.Account.Companion.ID
import com.google.samples.quickstart.engagesdksamples.watch.data.model.Account.Companion.SIGNED_IN
import com.google.samples.quickstart.engagesdksamples.watch.data.model.Account.Companion.TABLE_NAME

@Dao
abstract class AccountDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertAccount(accountToInsert: Account)

  @Query("UPDATE $TABLE_NAME SET $SIGNED_IN = 1 WHERE $ID IS '1'")
  abstract suspend fun setAccountSignedIn()

  @Query("UPDATE $TABLE_NAME SET $SIGNED_IN = 0 WHERE $ID IS '1'")
  abstract suspend fun setAccountSignedOut()

  @Query("SELECT $SIGNED_IN FROM $TABLE_NAME WHERE $ID IS '1'")
  abstract suspend fun isAccountSignedIn(): Boolean
}
