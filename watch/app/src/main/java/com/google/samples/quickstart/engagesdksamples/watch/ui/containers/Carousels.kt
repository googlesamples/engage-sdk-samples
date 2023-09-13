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
package com.google.samples.quickstart.engagesdksamples.watch.ui.containers

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.samples.quickstart.engagesdksamples.watch.MainActivityViewModel
import com.google.samples.quickstart.engagesdksamples.watch.R
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem

class Carousels(context: Context) {
  private val cards = MovieCards(context)

  @Composable
  fun MovieCarousel(
    clusterTitle: String,
    movieList: List<MovieItem>,
    viewModel: MainActivityViewModel
  ) {
    Column(modifier = Modifier.padding(15.dp)) {
      Text(
        text = clusterTitle,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(10.dp)
      )
      LazyRow(
        modifier =
          Modifier.clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
      ) {
        items(movieList) { movie -> cards.MovieCard(movieItem = movie, viewModel) }
      }
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun FeaturedCarousel(
    clusterTitle: String,
    movieList: List<MovieItem>,
    viewModel: MainActivityViewModel
  ) {
    val state = rememberLazyListState()
    Column(modifier = Modifier.padding(20.dp)) {
      Text(
        text = clusterTitle,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 5.dp, start = 10.dp, end = 10.dp)
      )
      LazyRow(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier =
          Modifier.height(175.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        state = state,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
      ) {
        items(movieList) { movie -> cards.FeaturedMovieCard(movieItem = movie, viewModel) }
      }
    }
  }

  @Composable
  fun ContinuationCarousel(movieList: List<MovieItem>, viewModel: MainActivityViewModel) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(15.dp).fillMaxWidth()
    ) {
      Text(
        text = stringResource(R.string.continue_watching_carousel),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(10.dp).fillMaxWidth()
      )
      LazyRow(
        modifier =
          Modifier.clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
      ) {
        items(movieList) { movie -> cards.ContinueCard(movieItem = movie, viewModel) }
      }
    }
  }
}
