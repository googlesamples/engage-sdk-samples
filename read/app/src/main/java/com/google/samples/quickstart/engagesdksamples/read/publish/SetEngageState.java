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
package com.google.samples.quickstart.engagesdksamples.read.publish;

import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_CONTINUATION;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_CONTINUATION_ONE_TIME_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_CONTINUATION_PERIODICALLY_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_FEATURED;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_FEATURED_ONE_TIME_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_FEATURED_PERIODICALLY_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_RECOMMENDATIONS;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_RECOMMENDATIONS_ONE_TIME_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_RECOMMENDATIONS_PERIODICALLY_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_STATE_KEY;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_USER_MANAGEMENT;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_USER_MANAGEMENT_PERIODICALLY_WORK_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.android.engage.service.AppEngageErrorCode;
import com.google.android.engage.service.AppEngageException;
import java.util.concurrent.TimeUnit;

/**
 * Non-instantiable class containing methods to triggering publishing of different clusters through
 * WorkManager. These methods are the main entry points to publish in the app.
 */
public final class SetEngageState {

  private static final String TAG = SetEngageState.class.getSimpleName();

  /**
   * Sets recommendation clusters, featured cluster, continuation cluster, and user management
   * cluster to the appropriate state by publishing or deleting the clusters based on whether a
   * saved account is present. While doing so, also sets publish status to the appropriate state.
   * This occurs immediately then once every 24 hours.
   * <p>
   * The appropriate state is that if a saved account is present, we publish the recommendation,
   * featured, and continuation clusters, delete the sign-in card if present, and set the publish
   * status to PUBLISHED. If an account is not present, we delete the recommendation, featured, and
   * continuation clusters, publish a sign-in card, and set the publish status to
   * NOT_PUBLISHED_REQUIRES_SIGN_IN.
   * <p>
   * Note that this approach of deleting the recommendation and featured clusters when no account is
   * present is NOT advised. The best and recommended practice is to publish non-personalized
   * recommendation and featured clusters in the absence of account info. We are not publishing any
   * non-personalized clusters to demonstrate the use of the updatePublishStatus API, setting the
   * status to NOT_PUBLISHED_REQUIRES_SIGN_IN when no account is present. In a real app, we
   * recommend publishing both recommendation and featured clusters in accordance with the
   * guidelines found
   * <a href="https://developer.android.com/guide/playcore/engage/publish#rec-signed-out">here</a>.
   *
   * @param appContext Application's context.
   */
  public static void setAllEngageStatePeriodically(Context appContext) {
    queuePeriodicSetEngageStateWorker(
        appContext, SET_RECOMMENDATIONS_PERIODICALLY_WORK_NAME, SET_RECOMMENDATIONS);
    queuePeriodicSetEngageStateWorker(
        appContext, SET_FEATURED_PERIODICALLY_WORK_NAME, SET_FEATURED);
    queuePeriodicSetEngageStateWorker(
        appContext, SET_CONTINUATION_PERIODICALLY_WORK_NAME, SET_CONTINUATION);
    queuePeriodicSetEngageStateWorker(
        appContext, SET_USER_MANAGEMENT_PERIODICALLY_WORK_NAME, SET_USER_MANAGEMENT);
  }

  /**
   * Sets recommendation clusters and publish status to the appropriate state using WorkManager.
   * More detail on what the appropriate state is described in
   * {@link
   * com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState#setAllEngageStatePeriodically(Context)}.
   *
   * @param appContext Application's context
   */
  public static void setRecommendationClusters(Context appContext) {
    queueOneTimeSetEngageStateWorker(
        appContext, SET_RECOMMENDATIONS_ONE_TIME_WORK_NAME, SET_RECOMMENDATIONS);
  }

  /**
   * Sets featured cluster and publish status to the appropriate state using WorkManager. More
   * detail on what the appropriate state is described in
   * {@link
   * com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState#setAllEngageStatePeriodically(Context)}
   *
   * @param appContext Application's context
   */
  public static void setFeaturedCluster(Context appContext) {
    queueOneTimeSetEngageStateWorker(appContext, SET_FEATURED_ONE_TIME_WORK_NAME, SET_FEATURED);
  }

  /**
   * Sets continuation cluster and publish status to the appropriate state using WorkManager. More
   * detail on what the appropriate state is described in
   * {@link
   * com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState#setAllEngageStatePeriodically(Context)}
   *
   * @param appContext Application's context
   */
  public static void setContinuationCluster(Context appContext) {
    queueOneTimeSetEngageStateWorker(
        appContext, SET_CONTINUATION_ONE_TIME_WORK_NAME, SET_CONTINUATION);
  }

  @SuppressLint("RestrictedApi")
  private static void queueOneTimeSetEngageStateWorker(Context appContext,
      String publishWorkName, String clusterToPublish) {
    Data clusterToPublishData =
        new Data.Builder().put(SET_STATE_KEY, clusterToPublish).build();
    WorkManager workManager = WorkManager.getInstance(appContext);
    OneTimeWorkRequest publishRequest =
        new OneTimeWorkRequest.Builder(EngageServiceWorker.class)
            .setInputData(clusterToPublishData)
            .build();
    workManager.enqueueUniqueWork(publishWorkName, ExistingWorkPolicy.REPLACE, publishRequest);
  }

  @SuppressLint("RestrictedApi")
  private static void queuePeriodicSetEngageStateWorker(Context appContext, String publishWorkName,
      String clusterToPublish) {
    Data clusterToPublishData =
        new Data.Builder().put(SET_STATE_KEY, clusterToPublish).build();
    WorkManager workManager = WorkManager.getInstance(appContext);
    PeriodicWorkRequest publishRequest =
        new PeriodicWorkRequest.Builder(
                EngageServiceWorker.class, /* repeatInterval= */ 24, TimeUnit.HOURS)
            .setInputData(clusterToPublishData)
            .build();
    workManager.enqueueUniquePeriodicWork(
        publishWorkName, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, publishRequest);
  }

  /**
   * Logs an exception publishingException that occurred during publishing
   */
  static void logPublishingError(AppEngageException publishingException) {
    @AppEngageErrorCode int errorCode = publishingException.getErrorCode();
    String logErrorMsg;
    switch (errorCode) {
      case AppEngageErrorCode.SERVICE_NOT_FOUND:
        logErrorMsg = "Service not found";
        break;
      case AppEngageErrorCode.SERVICE_NOT_AVAILABLE:
        logErrorMsg = "Service not available";
        break;
      case AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE:
        logErrorMsg = "Execution failure";
        break;
      case AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED:
        logErrorMsg = "Service call permission denied";
        break;
      case AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT:
        logErrorMsg = "Service call invalid argument";
        break;
      case AppEngageErrorCode.SERVICE_CALL_INTERNAL:
        logErrorMsg = "Service call internal";
        break;
      case AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED:
        logErrorMsg = "Service call resource exhausted";
        break;
      default:
        logErrorMsg = "AppEngageException type unknown";
    }
    Log.e(TAG, logErrorMsg, publishingException);
  }

  /**
   * While publishing, errors are expected. Some are recoverable and indicate that we should
   * republish. Some errors are unrecoverable so we should not republish. This method returns
   * whether one should republish or not as a result of the error.
   *
   * @param publishingException Exception received as a result of failed publishing.
   * @return Whether one should republish or not as a result of the error.
   */
  static boolean isErrorRecoverable(AppEngageException publishingException) {
    @AppEngageErrorCode int errorCode = publishingException.getErrorCode();
    boolean recoverable;
    switch (errorCode) {
      case AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE:
      case AppEngageErrorCode.SERVICE_CALL_INTERNAL:
      case AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED:
        recoverable = true;
        break;
      case AppEngageErrorCode.SERVICE_NOT_FOUND:
      case AppEngageErrorCode.SERVICE_NOT_AVAILABLE:
      case AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED:
      case AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT:
      default:
        recoverable = false;
    }
    return recoverable;
  }

  private SetEngageState() {}
}
