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
package com.google.samples.quickstart.engagesdksamples.watch.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Account.TABLE_NAME)
data class Account(
  @PrimaryKey @ColumnInfo(name = ID) val id: String,
  @ColumnInfo(name = SIGNED_IN) var signedIn: Boolean = false
) {
  companion object {
    const val TABLE_NAME = "account_table"
    const val ID = "id"
    const val SIGNED_IN = "signed_in"
  }
}
