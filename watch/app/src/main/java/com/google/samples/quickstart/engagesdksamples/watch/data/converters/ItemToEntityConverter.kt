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
import com.google.android.engage.common.datamodel.DisplayTimeWindow
import com.google.android.engage.common.datamodel.Image
import com.google.android.engage.common.datamodel.ImageTheme
import com.google.android.engage.common.datamodel.PlatformSpecificUri
import com.google.android.engage.video.datamodel.MovieEntity
import com.google.android.engage.video.datamodel.RatingSystem
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
        .setEntityId(movie.id)
        .addPosterImage(
          Image.Builder()
            .setImageUri(
              Uri.parse("android.resource://" + PACKAGE_NAME + "/" + movie.landscapePoster)
            )
            .setImageWidthInPixel(408)
            .setImageHeightInPixel(960)
            .setImageTheme(ImageTheme.IMAGE_THEME_LIGHT)
            .build()
        )
        .setPlayBackUri(Uri.parse(movie.playbackUri))
        .addPlatformSpecificPlaybackUri(
          PlatformSpecificUri.Builder()
            .setActionUri(Uri.parse(movie.platformSpecificPlaybackUri))
            .setPlatformType(movie.platformType)
            .build()
        )
        .setReleaseDateEpochMillis(movie.releaseDate)
        .setAvailability(movie.availability)
        .setDurationMillis(movie.durationMillis)
        .addGenre(movie.genre)
        .addAvailabilityTimeWindow(
          DisplayTimeWindow.Builder()
            .setStartTimestampMillis(movie.availabilityStartTimeMillis)
            .setEndTimestampMillis(movie.availabilityEndTimeMillis)
            .build()
        )
        .addContentRating(
          RatingSystem.Builder()
            .setAgencyName(movie.contentRatingAgency)
            .setRating(movie.contentRating)
            .build()
        )
    if (movie.currentlyWatching) {
      movieBuilder
        .setWatchNextType(movie.watchNextType)
        .setLastEngagementTimeMillis(movie.lastEngagementTimeMillis)
        .setLastPlayBackPositionTimeMillis(movie.lastPlaybackTimeMillis)
    }
    return movieBuilder.build()
  }
}
