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
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.engage.video.datamodel.WatchNextType
import com.google.samples.quickstart.engagesdksamples.watch.MainActivityViewModel
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem

class MovieCards(val context: Context) {
  @Composable
  fun MovieCard(movieItem: MovieItem, viewModel: MainActivityViewModel) {
    ElevatedCard(
      modifier = Modifier.width(140.dp).height(180.dp).padding(10.dp),
      shape = CardDefaults.outlinedShape,
      colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary),
    ) {
      Image(
        painter = painterResource(movieItem.landscapePoster),
        contentDescription = null,
        modifier = Modifier.width(130.dp).blur(130.dp).height(100.dp)
      )
      Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
      ) {
        Text(movieItem.movieName, fontWeight = FontWeight.Bold)
        IconButton(
          onClick = {
            viewModel.updateMovieItem(
              movieItem.apply {
                currentlyWatching = true
                lastPlaybackTimeMillis += 60000000L
                lastEngagementTimeMillis = 60000000L
                watchNextType = WatchNextType.TYPE_CONTINUE
              }
            )
            Toast.makeText(context, toastWatchMovieText, Toast.LENGTH_SHORT).show()
          }
        ) {
          Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
        }
      }
    }
  }

  @Composable
  fun ContinueCard(movieItem: MovieItem, viewModel: MainActivityViewModel) {
    ElevatedCard(
      modifier = Modifier.width(140.dp).height(180.dp).padding(10.dp),
      shape = CardDefaults.outlinedShape,
      colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary),
    ) {
      Box(modifier = Modifier.width(140.dp)) {
        Image(
          painter = painterResource(movieItem.landscapePoster),
          contentDescription = null,
          modifier = Modifier.fillMaxWidth().height(100.dp).blur(130.dp)
        )
        IconButton(
          onClick = {
            viewModel.updateMovieItem(
              movieItem.apply {
                currentlyWatching = false
                lastPlaybackTimeMillis += 60000000L
                lastEngagementTimeMillis = 60000000L
                watchNextType = WatchNextType.TYPE_UNKNOWN
              }
            )
            Toast.makeText(context, toastRemoveFromContinueText, Toast.LENGTH_SHORT).show()
          }
        ) {
          Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
        }
      }
      Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
      ) {
        Text(movieItem.movieName, fontWeight = FontWeight.Bold)
        IconButton(
          onClick = {
            viewModel.updateMovieItem(
              movieItem.apply {
                currentlyWatching = true
                lastPlaybackTimeMillis += 60000000L
                lastEngagementTimeMillis = 60000000L
                watchNextType = WatchNextType.TYPE_CONTINUE
              }
            )
            Toast.makeText(context, toastInMovieText, Toast.LENGTH_SHORT).show()
          }
        ) {
          Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
        }
      }
    }
  }

  @Composable
  fun FeaturedMovieCard(movieItem: MovieItem, viewModel: MainActivityViewModel) {
    Card(
      modifier = Modifier.width(350.dp).padding(10.dp).fillMaxHeight(),
      shape = RoundedCornerShape(15.dp),
      colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
    ) {
      Image(
        painter = painterResource(movieItem.landscapePoster),
        contentDescription = null,
        modifier = Modifier.width(350.dp).height(100.dp).blur(130.dp)
      )
      Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
      ) {
        Text(movieItem.movieName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        IconButton(
          onClick = {
            viewModel.updateMovieItem(
              movieItem.apply {
                currentlyWatching = true
                lastPlaybackTimeMillis += 60000000L
                lastEngagementTimeMillis = 60000000L
                watchNextType = WatchNextType.TYPE_CONTINUE
              }
            )
            Toast.makeText(context, toastWatchMovieText, Toast.LENGTH_SHORT).show()
          }
        ) {
          Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
        }
      }
    }
  }

  // Toast notification texts
  companion object {
    private const val toastWatchMovieText = "Added to Continue"
    private const val toastInMovieText = "Watching"
    private const val toastRemoveFromContinueText = "Removed from Continue"
  }
}
