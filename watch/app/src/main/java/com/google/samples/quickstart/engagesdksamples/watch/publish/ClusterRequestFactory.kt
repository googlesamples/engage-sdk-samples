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
package com.google.samples.quickstart.engagesdksamples.watch.publish

import android.content.Context
import android.net.Uri
import com.google.android.engage.common.datamodel.ContinuationCluster
import com.google.android.engage.common.datamodel.FeaturedCluster
import com.google.android.engage.common.datamodel.Image
import com.google.android.engage.common.datamodel.RecommendationCluster
import com.google.android.engage.common.datamodel.SignInCardEntity
import com.google.android.engage.service.PublishContinuationClusterRequest
import com.google.android.engage.service.PublishFeaturedClusterRequest
import com.google.android.engage.service.PublishRecommendationClustersRequest
import com.google.android.engage.service.PublishUserAccountManagementRequest
import com.google.samples.quickstart.engagesdksamples.watch.R
import com.google.samples.quickstart.engagesdksamples.watch.data.converters.ItemToEntityConverter
import com.google.samples.quickstart.engagesdksamples.watch.data.converters.PACKAGE_NAME
import com.google.samples.quickstart.engagesdksamples.watch.data.room.WatchDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Class in charge of constructing the publishing requests and sending them to their respective
 * publishers
 */
class ClusterRequestFactory(context: Context) {

  private val db = WatchDatabase.getDatabase(context, CoroutineScope(SupervisorJob()))
  private val movieDao = db.movieDao()
  private val recommendationClusterTitle =
    context.resources.getString(R.string.recommendation_cluster_title)
  private val signInCardAction = context.resources.getString(R.string.sign_in_card_action_text)
  private val signInCard =
    SignInCardEntity.Builder()
      .addPosterImage(
        Image.Builder()
          .setImageUri(Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.yellow))
          .setImageHeightInPixel(500)
          .setImageWidthInPixel(500)
          .build()
      )
      .setActionText(signInCardAction)
      .setActionUri(Uri.parse("https://xyz.com/signin"))
      .build()

  /**
   * [constructFeaturedClusterRequest] returns a [PublishFeaturedClusterRequest] to be used by the
   * [EngageServiceWorker] to publish Featured clusters
   *
   * @return PublishFeaturedClusterRequest.Builder
   */
  suspend fun constructFeaturedClusterRequest(): PublishFeaturedClusterRequest {
    val featuredList = movieDao.loadMovieIsCurrentlyWatching(false)
    val featuredCluster = FeaturedCluster.Builder()
    for (item in featuredList) {
      featuredCluster.addEntity(ItemToEntityConverter.convertMovie(item))
    }
    return PublishFeaturedClusterRequest.Builder()
      .setFeaturedCluster(featuredCluster.build())
      .build()
  }

  /**
   * [constructRecommendationClustersRequest] returns a [PublishRecommendationClustersRequest] to be
   * used by the [EngageServiceWorker] to publish Recommendations clusters
   *
   * @return PublishRecommendationClustersRequest
   */
  suspend fun constructRecommendationClustersRequest(): PublishRecommendationClustersRequest {
    val recommendationsList = movieDao.loadMovieIsCurrentlyWatching(false)
    val recommendationCluster = RecommendationCluster.Builder()
    for (item in recommendationsList) {
      recommendationCluster.addEntity(ItemToEntityConverter.convertMovie(item))
    }
    return PublishRecommendationClustersRequest.Builder()
      .addRecommendationCluster(recommendationCluster.setTitle(recommendationClusterTitle).build())
      .build()
  }

  /**
   * [constructContinuationClusterRequest] returns a [PublishContinuationClusterRequest] to be used
   * by the [EngageServiceWorker] to publish Continuations clusters
   *
   * @return PublishContinuationClusterRequest
   */
  suspend fun constructContinuationClusterRequest(): PublishContinuationClusterRequest {
    val continuationList = movieDao.loadMovieIsCurrentlyWatching(true)
    val continuationCluster = ContinuationCluster.Builder()
    for (item in continuationList) {
      continuationCluster.addEntity(ItemToEntityConverter.convertMovie(item))
    }
    return PublishContinuationClusterRequest.Builder()
      .setContinuationCluster(continuationCluster.build())
      .build()
  }

  /**
   * [constructUserAccountManagementClusterRequest] returns a [PublishUserAccountManagementRequest]
   * to be used by the [EngageServiceWorker] to publish User Account Management clusters
   *
   * @return PublishUserAccountManagementRequest
   */
  fun constructUserAccountManagementClusterRequest(): PublishUserAccountManagementRequest =
    PublishUserAccountManagementRequest.Builder().setSignInCardEntity(signInCard).build()
}
