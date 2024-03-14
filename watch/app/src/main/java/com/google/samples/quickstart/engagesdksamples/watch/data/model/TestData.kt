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

import com.google.android.engage.common.datamodel.PlatformType
import com.google.samples.quickstart.engagesdksamples.watch.R

class TestData() {

  fun getTestData(): List<MovieItem> {
    val movieList = mutableListOf<MovieItem>()
    for (i in 0..9) {
      movieList.add(
        MovieItem(
          id = "$i",
          movieName = "Title $i",
          landscapePoster = R.drawable.red,
          platformType = PlatformType.TYPE_UNSPECIFIED,
          platformSpecificPlaybackUri = "https://tv.com/playback/${i}",
          playbackUri = "https://tv.com/playback/${i}",
          releaseDate = 1633032875L,
          availability = 1,
          durationMillis = 123456789L,
          genre = "mystery",
          contentRatingAgency = "ContentRatingAgency",
          contentRating = "PG-13"
        )
      )
    }
    return movieList
  }
}
