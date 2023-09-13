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
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.engage.service.AppEngageException
import com.google.android.engage.service.AppEngagePublishClient
import com.google.android.engage.service.AppEngagePublishStatusCode
import com.google.android.engage.service.PublishStatusRequest
import com.google.android.gms.tasks.Task
import com.google.common.annotations.VisibleForTesting
import com.google.samples.quickstart.engagesdksamples.watch.data.room.WatchDatabase
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_CONTINUATION
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_FEATURED
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_RECOMMENDATIONS
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.tasks.await

/**
 * [EngageServiceWorker] is a [CoroutineWorker] class that is tasked with publishing cluster
 * requests to Engage Service
 */
class EngageServiceWorker(
  context: Context,
  workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

  @VisibleForTesting
  constructor(
    context: Context,
    workerParams: WorkerParameters,
    client: AppEngagePublishClient,
    db: WatchDatabase
  ) : this(context, workerParams) {
    this.client = client
    this.db = db
  }

  val TAG = "ENGAGE_SERVICE_WORKER"
  private var client = AppEngagePublishClient(context)
  private val clusterRequestFactory = ClusterRequestFactory(context)
  private var db = WatchDatabase.getDatabase(context, CoroutineScope(SupervisorJob()))

  /**
   * [doWork] is the entry point for the [EngageServiceWorker], and differentiates between
   * publishing tasks of each cluster
   */
  override suspend fun doWork(): Result {
    // If too many publishing attempts have failed, do not attempt to publish again.
    if (runAttemptCount > Constants.MAX_PUBLISHING_ATTEMPTS) {
      return Result.failure()
    }

    // Check if engage service is available before publishing.
    val isAvailable = client.isServiceAvailable.await()
    // If the service is not available, do not attempt to publish and indicate failure.
    if (!isAvailable) {
      return Result.failure()
    }
    // The cluster to publish must be passed into the worker through the input data.
    // This value must be one of the predefined values indicated a valid cluster to publish. Instead
    // of using one worker with flags to determine what cluster to publish, you may also choose to
    // your separate workers to publish different clusters; use whichever approach better fits your
    // app architecture.
    return when (inputData.getString(PUBLISH_TYPE)) {
      PUBLISH_TYPE_RECOMMENDATIONS -> publishRecommendations()
      PUBLISH_TYPE_CONTINUATION -> publishContinuation()
      PUBLISH_TYPE_FEATURED -> publishFeatured()
      PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT -> publishUserAccountManagement()
      else -> throw IllegalArgumentException("Bad publish type")
    }
  }

  /**
   * [publishRecommendations] publishes recommendations clusters and returns the result of the
   * attempt to publish the recommendation clusters if the user is signed in. If the user is signed
   * out it instead publishes a request to delete recommendation clusters that had been previously
   * published.
   *
   * @return result Result of publishing recommendation clusters, or recommendation cluster deletion
   */
  private suspend fun publishRecommendations(): Result {
    val publishTask: Task<Void>
    val statusCode: Int
    if (db.accountDao().isAccountSignedIn()) {
      publishTask =
        client.publishRecommendationClusters(
          clusterRequestFactory.constructRecommendationClustersRequest()
        )
      statusCode = AppEngagePublishStatusCode.PUBLISHED
    } else {
      // Choosing to not publish any content in the absence of account info is not recommended. We
      // do so here purely to demonstrate the updatePublishStatus API. Best practice is to publish
      // non-personalized featured and recommendation clusters (and continuation clusters
      // personalized to guest sessions if applicable). Guidelines for publishing non-personalized
      // featured and recommendation clusters can be found here:
      // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
      publishTask = client.deleteRecommendationsClusters()
      statusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
    }
    return publishAndProvideResult(publishTask, statusCode)
  }

  /**
   * [publishContinuation] publishes continuation clusters and returns the result of the attempt to
   * publish the continuation clusters if the user is signed in. If the user is signed out it
   * instead publishes a request to delete the continuation cluster that had previously been
   * published.
   *
   * @return result Result of publishing a continuation cluster, or continuation cluster deletion
   */
  private suspend fun publishContinuation(): Result {
    val movieDao = db.movieDao()
    val publishTask: Task<Void>
    val statusCode: Int
    if (!db.accountDao().isAccountSignedIn()) {
      // Choosing to not publish any content in the absence of account info is not recommended. We
      // do so here purely to demonstrate the updatePublishStatus API. Best practice is to publish
      // non-personalized featured and recommendation clusters (and continuation clusters
      // personalized to guest sessions if applicable). Guidelines for publishing non-personalized
      // featured and recommendation clusters can be found here:
      // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
      publishTask = client.deleteContinuationCluster()
      statusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
      // If no movies are in progress
    } else if (movieDao.loadMovieIsCurrentlyWatching(currentlyWatching = true).isEmpty()) {
      publishTask = client.deleteContinuationCluster()
      statusCode = AppEngagePublishStatusCode.PUBLISHED
    } else { // Signed in with in-progress movies
      publishTask =
        client.publishContinuationCluster(
          clusterRequestFactory.constructContinuationClusterRequest()
        )
      statusCode = AppEngagePublishStatusCode.PUBLISHED
    }
    return publishAndProvideResult(publishTask, statusCode)
  }

  /**
   * [publishFeatured] publishes featured clusters and returns the result of the attempt to publish
   * the featured clusters if the user is signed in. If the user is signed out, it instead publishes
   * a request to delete the featured cluster that had previously been published.
   *
   * @return result Result of publishing a featured cluster, or continuation featured cluster
   *   deletion
   */
  private suspend fun publishFeatured(): Result {
    val publishTask: Task<Void>
    val statusCode: Int
    if (db.accountDao().isAccountSignedIn()) {
      publishTask =
        client.publishFeaturedCluster(clusterRequestFactory.constructFeaturedClusterRequest())
      statusCode = AppEngagePublishStatusCode.PUBLISHED
    } else {
      // Choosing to not publish any content in the absence of account info is not recommended. We
      // do so here purely to demonstrate the updatePublishStatus API. Best practice is to publish
      // non-personalized featured and recommendation clusters (and continuation clusters
      // personalized to guest sessions if applicable). Guidelines for publishing non-personalized
      // featured and recommendation clusters can be found here:
      // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
      publishTask = client.deleteFeaturedCluster()
      statusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
    }
    return publishAndProvideResult(publishTask, statusCode)
  }

  /**
   * [publishUserAccountManagement] publishes user account management clusters and returns the
   * result of the attempt to publish the suer account management clusters if the user is signed
   * out. If the user is signed in, it instead publishes a request to delete a user account
   * management cluster that had previously been published
   *
   * @return result Result of publishing a user account management clusters, or user account
   *   management cluster deletion
   */
  private suspend fun publishUserAccountManagement(): Result {
    val publishTask: Task<Void>
    val statusCode: Int
    if (db.accountDao().isAccountSignedIn()) {
      publishTask = client.deleteUserManagementCluster()
      statusCode = AppEngagePublishStatusCode.PUBLISHED
    } else {
      // Choosing to not publish any content in the absence of account info is not recommended. We
      // do so here purely to demonstrate the updatePublishStatus API. Best practice is to publish
      // non-personalized featured and recommendation clusters (and continuation clusters
      // personalized to guest sessions if applicable). Guidelines for publishing non-personalized
      // featured and recommendation clusters can be found here:
      // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
      publishTask =
        client.publishUserAccountManagementRequest(
          clusterRequestFactory.constructUserAccountManagementClusterRequest()
        )
      statusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
    }
    return publishAndProvideResult(publishTask, statusCode)
  }

  /**
   * [publishAndProvideResult] is a method that is in charge of publishing a given task
   *
   * @param publishTask A task to publish some cluster or delete some cluster
   * @param publishStatusCode Publish status code to set through Engage.
   * @return publishResult Result of [publishTask]
   */
  private suspend fun publishAndProvideResult(
    publishTask: Task<Void>,
    publishStatusCode: Int
  ): Result {
    setPublishStatusCode(publishStatusCode)

    // Result initialized to success, it is changed to retry or failure if an exception occurs.
    var result: Result = Result.success()
    try {
      // An AppEngageException may occur while publishing, so we may not be able to await the
      // result.
      publishTask.await()
    } catch (publishException: Exception) {
      Publisher.logPublishing(publishException as AppEngageException)
      // Some errors are recoverable, such as a threading issue, some are unrecoverable
      // such as a cluster not containing all necessary fields. If an error is recoverable, we
      // should attempt to publish again. Setting the  result to retry means WorkManager will
      // attempt to run the worker again, thus attempting to publish again.
      result =
        if (Publisher.isErrorRecoverable(publishException)) Result.retry() else Result.failure()
    }
    // This result is returned back to doWork.
    return result
  }

  /**
   * [setPublishStatusCode] method is in charge of updating the publish status code, which monitors
   * the health of the integration with EngageSDK
   *
   * @param statusCode PublishStatus code to be set through Engage.
   */
  private fun setPublishStatusCode(statusCode: Int) {
    client
      .updatePublishStatus(PublishStatusRequest.Builder().setStatusCode(statusCode).build())
      .addOnSuccessListener {
        Log.i(TAG, "Successfully updated publish status code to $statusCode")
      }
      .addOnFailureListener { exception ->
        Log.e(TAG, "Failed to update publish status code to $statusCode\n${exception.stackTrace}")
      }
  }
}
