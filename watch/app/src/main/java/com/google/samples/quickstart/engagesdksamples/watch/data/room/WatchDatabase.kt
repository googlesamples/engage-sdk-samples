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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.samples.quickstart.engagesdksamples.watch.data.model.Account
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem
import com.google.samples.quickstart.engagesdksamples.watch.data.model.TestData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Room database for Engage SDK Watch Sample App */
@Database(
  entities = [MovieItem::class, Account::class],
  version = 1,
  exportSchema = true,
)
abstract class WatchDatabase : RoomDatabase() {
  /** Provides the data access object for movie table. */
  abstract fun movieDao(): MovieDao

  abstract fun accountDao(): AccountDao

  private class WatchDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      databaseInstance?.let { database ->
        scope.launch { populateInitialData(database.movieDao(), database.accountDao()) }
      }
    }

    suspend fun populateInitialData(movieDao: MovieDao, accountDao: AccountDao) {
      movieDao.insertMovieItems(TestData().getTestData())
      accountDao.insertAccount(Account(id = "1"))
    }
  }

  companion object {
    private const val DATABASE_NAME = "watch_database.db"
    /** Singleton instance of [WatchDatabase] */
    @Volatile private var databaseInstance: WatchDatabase? = null

    @JvmStatic
    /** Retrieves the single [WatchDatabase] instance */
    fun getDatabase(context: Context, scope: CoroutineScope): WatchDatabase {
      return databaseInstance
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                WatchDatabase::class.java,
                DATABASE_NAME
              )
              .addCallback(WatchDatabaseCallback(scope))
              .build()
          databaseInstance = instance
          instance
        }
    }
  }
}
