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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.engage.service.AppEngageErrorCode
import com.google.android.engage.service.AppEngageException
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PERIODIC_WORKER_NAME_CONTINUATION
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PERIODIC_WORKER_NAME_FEATURED
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PERIODIC_WORKER_NAME_RECOMMENDATIONS
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PERIODIC_WORKER_NAME_USER_ACCOUNT_MANAGEMENT
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_CONTINUATION
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_FEATURED
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_RECOMMENDATIONS
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.WORKER_NAME_CONTINUATION
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.WORKER_NAME_FEATURED
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.WORKER_NAME_RECOMMENDATIONS
import java.util.concurrent.TimeUnit

object Publisher {
  private const val TAG = "PUBLISHER:"

  /**
   * Sets recommendation clusters, featured cluster, continuation cluster, and user management
   * cluster to the appropriate state by publishing or deleting the clusters based on whether a
   * saved account is present. While doing so, also sets publish status to the appropriate state.
   * This occurs immediately then once every 24 hours.
   *
   * <p>
   * The appropriate state is that if a saved account is present, we publish the recommendation,
   * featured, and continuation clusters, delete the sign-in card if present, and set the publish
   * status to PUBLISHED. If an account is not present, we delete the recommendation, featured, and
   * continuation clusters, publish a sign-in card, and set the publish status to
   * NOT_PUBLISHED_REQUIRES_SIGN_IN.
   *
   * <p>
   * Note that this approach of deleting the recommendation and featured clusters when no account is
   * present is NOT advised. The best and recommended practice is to publish non-personalized
   * recommendation and featured clusters in the absence of account info. We are not publishing any
   * non-personalized clusters to demonstrate the use of the updatePublishStatus API, setting the
   * status to NOT_PUBLISHED_REQUIRES_SIGN_IN when no account is present. In a real app, we
   * recommend publishing both recommendation and featured clusters in accordance with the
   * guidelines found <a
   * href="https://developer.android.com/guide/playcore/engage/publish#rec-signed-out">here</a>.
   *
   * @param context Application's context.
   */
  fun publishPeriodically(context: Context) {
    periodicallyCallEngageServiceWorker(
      PERIODIC_WORKER_NAME_RECOMMENDATIONS,
      PUBLISH_TYPE_RECOMMENDATIONS,
      context
    )
    periodicallyCallEngageServiceWorker(
      PERIODIC_WORKER_NAME_CONTINUATION,
      PUBLISH_TYPE_CONTINUATION,
      context
    )
    periodicallyCallEngageServiceWorker(
      PERIODIC_WORKER_NAME_FEATURED,
      PUBLISH_TYPE_FEATURED,
      context
    )
    periodicallyCallEngageServiceWorker(
      PERIODIC_WORKER_NAME_USER_ACCOUNT_MANAGEMENT,
      PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT,
      context
    )
  }

  /**
   * Sets recommendation clusters and publish status to the appropriate state using WorkManager.
   * More detail on what the appropriate state is described in {@link
   * com.google.samples.quickstart.engagesdksamples.watch.publish.Publisher#publishPeriodically(Context)}.
   *
   * @param context Application's context
   */
  fun publishRecommendationClusters(context: Context) {
    queueOneTimeEngageServiceWorker(
      WORKER_NAME_RECOMMENDATIONS,
      PUBLISH_TYPE_RECOMMENDATIONS,
      context
    )
  }

  /**
   * Sets featured cluster and publish status to the appropriate state using WorkManager. More
   * detail on what the appropriate state is described in {@link
   * com.google.samples.quickstart.engagesdksamples.watch.publish.Publisher#publishPeriodically(Context)}.
   *
   * @param context Application's context
   */
  fun publishFeaturedClusters(context: Context) {
    queueOneTimeEngageServiceWorker(WORKER_NAME_FEATURED, PUBLISH_TYPE_FEATURED, context)
  }

  /**
   * Sets continuation cluster and publish status to the appropriate state using WorkManager. More
   * detail on what the appropriate state is described in {@link
   * com.google.samples.quickstart.engagesdksamples.watch.publish.Publisher#publishPeriodically(Context)}.
   *
   * @param context Application's context
   */
  fun publishContinuationClusters(context: Context) {
    queueOneTimeEngageServiceWorker(WORKER_NAME_CONTINUATION, PUBLISH_TYPE_CONTINUATION, context)
  }

  private fun periodicallyCallEngageServiceWorker(
    workerName: String,
    publishType: String,
    context: Context
  ) {
    val workRequest =
      PeriodicWorkRequestBuilder<EngageServiceWorker>(
          repeatInterval = 24,
          repeatIntervalTimeUnit = TimeUnit.HOURS
        )
        .setInputData(workDataOf(PUBLISH_TYPE to publishType))
        .build()
    WorkManager.getInstance(context)
      .enqueueUniquePeriodicWork(
        workerName,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        workRequest
      )
  }

  private fun queueOneTimeEngageServiceWorker(
    workerName: String,
    publishType: String,
    context: Context
  ) {
    val workRequest =
      OneTimeWorkRequestBuilder<EngageServiceWorker>()
        .setInputData(workDataOf(PUBLISH_TYPE to publishType))
        .build()
    WorkManager.getInstance(context)
      .enqueueUniqueWork(workerName, ExistingWorkPolicy.REPLACE, workRequest)
  }

  fun logPublishing(publishingException: AppEngageException) {
    val logMessage =
      when (publishingException.errorCode) {
        AppEngageErrorCode.SERVICE_NOT_FOUND ->
          "SERVICE_NOT_FOUND - The service is not available on the given device"
        AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE ->
          "SERVICE_CALL_EXECUTION_FAILURE - The task execution failed due to threading issues, can be retired"
        AppEngageErrorCode.SERVICE_NOT_AVAILABLE ->
          "SERVICE_NOT_AVAILABLE - The service is available on the given device, but not available at the time of the call"
        AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED ->
          "SERVICE_CALL_PERMISSION_DENIED - The The caller is not allowed to make the service call"
        AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT ->
          "SERVICE_CALL_INVALID_ARGUMENT - The request contains invalid data (e.g. more than allowed number of clusters"
        AppEngageErrorCode.SERVICE_CALL_INTERNAL ->
          "SERVICE_CALL_INTERNAL - There is an error on the service side"
        AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED ->
          "SERVICE_CALL_RESOURCE_EXHAUSTED - The service call is made too frequently"
        else -> "An unknown error has occurred"
      }
    Log.d(TAG, logMessage)
  }

  fun isErrorRecoverable(publishingException: AppEngageException): Boolean {
    return when (publishingException.errorCode) {
      // Recoverable Error codes
      AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
      AppEngageErrorCode.SERVICE_CALL_INTERNAL,
      AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED -> true
      // Non recoverable error codes
      AppEngageErrorCode.SERVICE_NOT_FOUND,
      AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT,
      AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      AppEngageErrorCode.SERVICE_NOT_AVAILABLE -> false
      else -> throw IllegalArgumentException(publishingException.localizedMessage)
    }
  }
}
