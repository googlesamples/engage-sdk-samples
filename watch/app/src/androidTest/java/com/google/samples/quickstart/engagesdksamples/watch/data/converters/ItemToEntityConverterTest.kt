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

import android.net.Uri
import com.google.samples.quickstart.engagesdksamples.watch.data.converters.ItemToEntityConverter
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem
import org.junit.Test

class ItemToEntityConverterTest {

  @Test
  fun convertsMovieItemTest() {
    val movieEntity = ItemToEntityConverter.convertMovie(movieItem)

    assert(movieEntity.availability == movieItem.availability)
    assert(movieEntity.name == movieItem.movieName)
    assert(movieEntity.playBackUri == Uri.parse(movieItem.playbackUri))
    assert(movieEntity.releaseDateEpochMillis.get() == movieItem.releaseDate)
    assert(movieEntity.offerPrice.get() == movieItem.offerPrice)
    assert(movieEntity.durationMillis == movieItem.durationMillis)
    assert(movieEntity.genres[0] == movieItem.genre)
    assert(movieEntity.contentRatings[0] == movieItem.contentRatings)
  }

  @Test
  fun convertsInProgressMovieItemTest() {
    inProgressMovieItem.currentlyWatching = true
    inProgressMovieItem.lastEngagementTimeMillis = 234567891L
    inProgressMovieItem.lastPlaybackTimeMillis = 123456789L
    inProgressMovieItem.watchNextType = 1
    val inProgressMovieEntity = ItemToEntityConverter.convertMovie(inProgressMovieItem)

    assert(inProgressMovieEntity.availability == inProgressMovieItem.availability)
    assert(inProgressMovieEntity.name == inProgressMovieItem.movieName)
    assert(inProgressMovieEntity.playBackUri == Uri.parse(inProgressMovieItem.playbackUri))
    assert(inProgressMovieEntity.releaseDateEpochMillis.get() == inProgressMovieItem.releaseDate)
    assert(inProgressMovieEntity.offerPrice.get() == inProgressMovieItem.offerPrice)
    assert(inProgressMovieEntity.durationMillis == inProgressMovieItem.durationMillis)
    assert(inProgressMovieEntity.genres[0] == inProgressMovieItem.genre)
    assert(inProgressMovieEntity.contentRatings[0] == inProgressMovieItem.contentRatings)
    assert(
      inProgressMovieEntity.lastEngagementTimeMillis.get() ==
        inProgressMovieItem.lastEngagementTimeMillis
    )
    assert(
      inProgressMovieEntity.lastPlayBackPositionTimeMillis.get() ==
        inProgressMovieItem.lastPlaybackTimeMillis
    )
    assert(inProgressMovieEntity.watchNextType.get() == inProgressMovieItem.watchNextType)
  }

  companion object {
    private var movieItem =
      MovieItem(
        id = "1",
        movieName = "Title1",
        landscapePoster = R.drawable.blue,
        playbackUri = "https://tv.com/playback/1",
        releaseDate = 1633032875L,
        availability = 1,
        offerPrice = "0.99",
        durationMillis = 123456789L,
        genre = "mystery",
        contentRatings = "PG-13"
      )

    private var inProgressMovieItem =
      MovieItem(
        id = "2",
        movieName = "Title2",
        landscapePoster = R.drawable.blue,
        playbackUri = "https://tv.com/playback/1",
        releaseDate = 2744143986L,
        availability = 2,
        offerPrice = "1.99",
        durationMillis = 234567891L,
        genre = "comedy",
        contentRatings = "R"
      )
  }
}
