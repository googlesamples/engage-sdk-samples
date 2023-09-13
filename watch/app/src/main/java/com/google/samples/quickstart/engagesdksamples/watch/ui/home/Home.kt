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
package com.google.samples.quickstart.engagesdksamples.watch.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.samples.quickstart.engagesdksamples.watch.MainActivityViewModel
import com.google.samples.quickstart.engagesdksamples.watch.R
import com.google.samples.quickstart.engagesdksamples.watch.ui.containers.Carousels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeScreen(val context: Context, val showOssLicense: () -> Unit) {

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun Home(viewModel: MainActivityViewModel) {
    val movies by viewModel.movies.collectAsState(initial = emptyList())
    val currentlyWatchingMovies by
      viewModel.currentlyWatchingMovies.collectAsState(initial = emptyList())

    val drawerState = remember { mutableStateOf(DrawerState.CLOSED) }
    val signedInState = remember { mutableStateOf(SignInState.SIGNED_OFF) }
    val carousels = Carousels(context)

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              stringResource(R.string.top_bar_title),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          },
          navigationIcon = {
            IconButton(
              onClick = {
                drawerState.value =
                  if (drawerState.value == DrawerState.OPEN) {
                    DrawerState.CLOSED
                  } else {
                    DrawerState.OPEN
                  }
              }
            ) {
              Icon(imageVector = Icons.Outlined.Menu, contentDescription = null)
            }
          },
          actions = {
            IconButton(onClick = showOssLicense) {
              Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.oss_licenses)
              )
            }
          },
          colors =
            TopAppBarDefaults.mediumTopAppBarColors(MaterialTheme.colorScheme.primaryContainer)
        )
      },
      content = { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxHeight()) {
          when (signedInState.value) {
            SignInState.SIGNED_IN -> {
              LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                if (currentlyWatchingMovies.isNotEmpty()) {
                  item() {
                    carousels.ContinuationCarousel(
                      movieList = currentlyWatchingMovies,
                      viewModel = viewModel
                    )
                  }
                }
                item() {
                  carousels.FeaturedCarousel(
                    clusterTitle = stringResource(R.string.featured_carousel),
                    movieList = movies,
                    viewModel
                  )
                }
                item() {
                  carousels.MovieCarousel(
                    clusterTitle = stringResource(R.string.recommended_carousel),
                    movieList = movies,
                    viewModel
                  )
                }
                item() {
                  carousels.MovieCarousel(
                    clusterTitle = stringResource(R.string.recommendation_cluster_title),
                    movieList = movies,
                    viewModel
                  )
                }
              }
            }
            else -> {
              Text(
                text = stringResource(R.string.sign_in_to_view_content),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().fillMaxWidth().padding(18.dp)
              )
            }
          }
          if (drawerState.value == DrawerState.OPEN) {
            UserAccountActions(context, signedInState, viewModel)
          }
        }
      }
    )
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun UserAccountActions(
    context: Context,
    signedInState: MutableState<SignInState>,
    viewModel: MainActivityViewModel
  ) {
    val signedInToast = stringResource(R.string.signed_in_toast)
    val signedOutToast = stringResource(R.string.signed_out_toast)
    ModalDrawerSheet(
      modifier = Modifier.width(300.dp),
      drawerContainerColor = MaterialTheme.colorScheme.primaryContainer,
      drawerTonalElevation = 150.dp
    ) {
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
      ) {
        Image(
          painterResource(R.drawable.blue),
          contentDescription = null,
          modifier = Modifier.height(150.dp).fillMaxWidth().blur(400.dp)
        )
        Button(
          onClick = {
            Toast.makeText(context, signedInToast, Toast.LENGTH_SHORT).show()
            signIn(viewModel, signedInState)
          },
          modifier = Modifier.padding(10.dp)
        ) {
          Text(stringResource(R.string.sign_in_button))
        }
        Button(
          onClick = {
            Toast.makeText(context, signedOutToast, Toast.LENGTH_SHORT).show()
            signOut(viewModel, signedInState)
          },
          modifier = Modifier.padding(10.dp)
        ) {
          Text(stringResource(R.string.sign_out_button))
        }
      }
    }
  }

  private fun signIn(viewModel: MainActivityViewModel, signedInState: MutableState<SignInState>) =
    scope.launch {
      viewModel.signIn()
      signedInState.value = SignInState.SIGNED_IN
    }

  private fun signOut(viewModel: MainActivityViewModel, signedInState: MutableState<SignInState>) =
    scope.launch {
      viewModel.signOut()
      signedInState.value = SignInState.SIGNED_OFF
    }

  companion object {
    val scope = CoroutineScope(Dispatchers.Default)

    enum class DrawerState {
      OPEN,
      CLOSED
    }

    enum class SignInState {
      SIGNED_IN,
      SIGNED_OFF
    }
  }
}
