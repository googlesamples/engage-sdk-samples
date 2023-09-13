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
package com.google.samples.quickstart.engagesdksamples.watch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem
import com.google.samples.quickstart.engagesdksamples.watch.data.room.WatchDataRepo
import com.google.samples.quickstart.engagesdksamples.watch.data.room.WatchDatabase
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
  private val movieRepo = WatchDataRepo(WatchDatabase.getDatabase(application, viewModelScope))
  val movies = movieRepo.allMovies
  val currentlyWatchingMovies = movieRepo.allCurrentlyWatchingMovies

  fun updateMovieItem(movieItem: MovieItem) {
    viewModelScope.launch { movieRepo.updateMovieItem(movieItem) }
  }

  fun signIn() {
    viewModelScope.launch { movieRepo.signIntoAccount() }
  }

  fun signOut() {
    viewModelScope.launch { movieRepo.signOutOfAccount() }
  }
}
