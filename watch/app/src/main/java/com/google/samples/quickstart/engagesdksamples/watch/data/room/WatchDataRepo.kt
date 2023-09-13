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

import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem
import kotlinx.coroutines.flow.Flow

/** Repository class to access to database data.*/
class WatchDataRepo(private val db: WatchDatabase) {
  val allMovies: Flow<List<MovieItem>> = db.movieDao().loadAllMovieItems()

  val allCurrentlyWatchingMovies: Flow<List<MovieItem>> =
    db.movieDao().loadAllCurrentlyWatchingMovies()

  suspend fun updateMovieItem(movieItem: MovieItem) {
    db.movieDao().updateMovieItem(movieItem)
  }

  suspend fun signIntoAccount() {
    db.accountDao().setAccountSignedIn()
  }

  suspend fun signOutOfAccount() {
    db.accountDao().setAccountSignedOut()
  }
}
