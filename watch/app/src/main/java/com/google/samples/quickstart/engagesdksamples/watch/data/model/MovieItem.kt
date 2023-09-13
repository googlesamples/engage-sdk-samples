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
import com.google.android.engage.video.datamodel.WatchNextType

@Entity(tableName = MovieItem.TABLE_NAME)
data class MovieItem(
  @PrimaryKey @ColumnInfo(name = ID) val id: String,
  @ColumnInfo(name = MOVIE_NAME) val movieName: String,
  @ColumnInfo(name = LANDSCAPE_POSTER) var landscapePoster: Int, // Resource ID
  @ColumnInfo(name = PLAYBACK_URI) val playbackUri: String,
  @ColumnInfo(name = RELEASE_DATE) val releaseDate: Long,
  @ColumnInfo(name = AVAILABILITY) val availability: Int, // ContentAvailability.AVAILABILITY_TYPE
  @ColumnInfo(name = OFFER_PRICE) var offerPrice: String,
  @ColumnInfo(name = DURATION_MILLIS) val durationMillis: Long, // Epoch ms
  @ColumnInfo(name = GENRE) val genre: String,
  @ColumnInfo(name = CONTENT_RATINGS) val contentRatings: String
) {
  @ColumnInfo(name = CURRENTLY_WATCHING) var currentlyWatching: Boolean = false
  @ColumnInfo(name = WATCH_NEXT_TYPE) var watchNextType: Int = WatchNextType.TYPE_UNKNOWN
  @ColumnInfo(name = LAST_ENGAGEMENT_TIME_MILLIS)
  var lastEngagementTimeMillis: Long = 0L // Epoch ms
  @ColumnInfo(name = LAST_PLAYBACK_TIME_MILLIS) var lastPlaybackTimeMillis: Long = 0L // Epoch ms

  companion object {
    const val TABLE_NAME = "movie_table"
    const val ID = "id"
    const val MOVIE_NAME = "movie_name"
    const val LANDSCAPE_POSTER = "landscape_poster"
    const val PLAYBACK_URI = "playback_uri"
    const val RELEASE_DATE = "release_date"
    const val AVAILABILITY = "availability"
    const val OFFER_PRICE = "offer_price"
    const val DURATION_MILLIS = "duration_millis"
    const val GENRE = "genre"
    const val CONTENT_RATINGS = "content_ratings"
    const val CURRENTLY_WATCHING = "currently_watching"
    const val WATCH_NEXT_TYPE = "watch_next_type"
    const val LAST_ENGAGEMENT_TIME_MILLIS = "last_engagement_time_millis"
    const val LAST_PLAYBACK_TIME_MILLIS = "last_playback_time_millis"
  }
}
