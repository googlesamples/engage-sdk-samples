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
package com.google.samples.quickstart.engagesdksamples.watch.data.converters

import android.net.Uri
import com.google.android.engage.common.datamodel.Image
import com.google.android.engage.video.datamodel.MovieEntity
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem

const val PACKAGE_NAME: String = "com.google.samples.quickstart.engagesdksamples.watch"

/** Converts Room Entities to Engage Entitis */
object ItemToEntityConverter {
  /**
   * Converts a [MovieItem] instance into a [MovieEntity] instance that can be used to construct
   * clusters for publishing
   *
   * @param movie - [MovieItem] instance that will be converted to a [MovieEntity]
   * @return Returns a [MovieEntity] instance from a given [MovieItem]
   */
  fun convertMovie(movie: MovieItem): MovieEntity {
    val movieBuilder: MovieEntity.Builder =
      MovieEntity.Builder()
        .setName(movie.movieName)
        .addPosterImage(
          Image.Builder()
            .setImageUri(
              Uri.parse("android.resource://" + PACKAGE_NAME + "/" + movie.landscapePoster)
            )
            .setImageWidthInPixel(408)
            .setImageHeightInPixel(960)
            .build()
        )
        .setPlayBackUri(Uri.parse(movie.playbackUri))
        .setReleaseDateEpochMillis(movie.releaseDate)
        .setAvailability(movie.availability)
        .setOfferPrice(movie.offerPrice)
        .setDurationMillis(movie.durationMillis)
        .addGenre(movie.genre)
        .addContentRating(movie.contentRatings)
    if (movie.currentlyWatching) {
      movieBuilder
        .setWatchNextType(movie.watchNextType)
        .setLastEngagementTimeMillis(movie.lastEngagementTimeMillis)
        .setLastPlayBackPositionTimeMillis(movie.lastPlaybackTimeMillis)
    }
    return movieBuilder.build()
  }
}
