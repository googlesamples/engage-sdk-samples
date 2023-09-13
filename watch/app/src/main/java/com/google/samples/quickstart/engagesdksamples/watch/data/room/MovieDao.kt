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
import androidx.room.Update
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem.Companion.CURRENTLY_WATCHING
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem.Companion.ID
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

/** Data access object for entities table. */
@Dao
abstract class MovieDao {
  /** Inserts a list of [MovieItem] instances */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertMovieItems(movies: List<MovieItem>)

  /** Deletes a [MovieItem] instance by id */
  @Query("DELETE FROM $TABLE_NAME WHERE $ID IS :id")
  abstract suspend fun deleteMovieItem(id: String)

  /** Loads all [MovieItem] instances by whether they are being currently watched */
  @Query("SELECT * FROM $TABLE_NAME WHERE $CURRENTLY_WATCHING IS :currentlyWatching")
  abstract suspend fun loadMovieIsCurrentlyWatching(currentlyWatching: Boolean): List<MovieItem>

  /** Loads all [MovieItem] instances */
  @Query("SELECT * FROM $TABLE_NAME")
  abstract fun loadAllMovieItems(): Flow<List<MovieItem>>

  /** Loads all currently watching [MovieItem] instances, to be used for the UI */
  @Query("SELECT * FROM $TABLE_NAME WHERE $CURRENTLY_WATCHING = 1")
  abstract fun loadAllCurrentlyWatchingMovies(): Flow<List<MovieItem>>

  /** Updates the [MovieItem] instance */
  @Update abstract suspend fun updateMovieItem(movieItem: MovieItem)
}
