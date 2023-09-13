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
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_FEATURED;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_RECOMMENDATIONS;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_STATE_KEY;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_USER_MANAGEMENT;
import static com.google.samples.quickstart.engagesdksamples.read.publish.GetContinuationCluster.getContinuationCluster;
import static com.google.samples.quickstart.engagesdksamples.read.publish.GetFeaturedCluster.getFeaturedCluster;
import static com.google.samples.quickstart.engagesdksamples.read.publish.GetRecommendationClusters.getRecommendationClusters;
import static com.google.samples.quickstart.engagesdksamples.read.publish.GetSignInCard.getSignInCard;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.isErrorRecoverable;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.logPublishingError;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.android.engage.common.datamodel.ContinuationCluster;
import com.google.android.engage.common.datamodel.RecommendationCluster;
import com.google.android.engage.service.AppEngageException;
import com.google.android.engage.service.AppEngagePublishClient;
import com.google.android.engage.service.AppEngagePublishStatusCode;
import com.google.android.engage.service.PublishContinuationClusterRequest;
import com.google.android.engage.service.PublishFeaturedClusterRequest;
import com.google.android.engage.service.PublishRecommendationClustersRequest;
import com.google.android.engage.service.PublishStatusRequest;
import com.google.android.engage.service.PublishUserAccountManagementRequest;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.samples.quickstart.engagesdksamples.read.login.Account;
import com.google.samples.quickstart.engagesdksamples.read.login.AccountIOManager;

/**
 * Worker that sets one of the recommendation clusters, featured cluster, continuation cluster, user
 * management cluster, if the Engage service is available. If publishing is successful, then sets
 * publish status. More information about what state is set to what is described in
 * {@link
 * com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState#setAllEngageStatePeriodically(Context)}.
 * In the event of a recoverable error, startWork() will resolve to a retry result and WorkManager
 * will attempt to republish by restarting this worker.
 */
public class EngageServiceWorker extends ListenableWorker {

  private static final String TAG = EngageServiceWorker.class.getSimpleName();

  private final AppEngagePublishClient client;
  private final Optional<Account> loggedInAccount;

  EngageServiceWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    this(
        context,
        workerParams,
        new AppEngagePublishClient(context),
        AccountIOManager.loadAccount(context));
  }

  @VisibleForTesting
  EngageServiceWorker(
      @NonNull Context context,
      @NonNull WorkerParameters workerParams,
      @NonNull AppEngagePublishClient client,
      @NonNull Optional<Account> loggedInAccount) {
    super(context, workerParams);
    this.client = client;
    this.loggedInAccount = loggedInAccount;
  }

  /**
   * Checks if the engage service is available, if so, sets one of the recommendation clusters,
   * featured cluster, continuation cluster, or user engagement cluster, depending on the state to
   * set sent through the input data. If the state to set string is invalid, no cluster is
   * published.
   * <p>
   * If we set the clusters successfully, we then set the publish status according to whether an
   * account is logged in or not. If an account is logged in, we set the publish status to
   * PUBLISHED. Otherwise, we set the publish status to NOT_PUBLISHED_REQUIRES_SIGN_IN.
   * <p>
   * Note that this approach of deleting the clusters when no account is present is NOT advised. The
   * best and recommended practice is to publish non-personalized recommendation and featured
   * clusters in the absence of account info. We are not publishing any non-personalized clusters to
   * demonstrate the use of the updatePublishStatus API, setting the status to
   * NOT_PUBLISHED_REQUIRES_SIGN_IN when no account is present. In a real app, we recommend
   * publishing both recommendation and featured clusters in accordance with the guidelines found
   * <a href="https://developer.android.com/guide/playcore/engage/publish#rec-signed-out">here</a>.
   *
   * @return A result indicating whether publishing was successful or not or should be retried
   */
  @NonNull
  @Override
  public ListenableFuture<Result> startWork() {
    // If too many publishing attempts have failed, do not attempt to publish again.
    if (getRunAttemptCount() > Constants.MAX_SET_STATE_RETRIES) {
      Log.e(TAG, "Maximum number of publishing retries exceeded");
      return Futures.immediateFuture(Result.failure());
    }

    // The cluster to set must be passed into the worker through the input data. Instead of using
    // one worker with flags to determine what cluster to publish, you may also choose to your
    // separate workers to publish different clusters; use whichever approach better fits your
    // app architecture.
    String stateToSet = getInputData().getString(SET_STATE_KEY);

    if (!isValidSetStateValue(stateToSet)) {
      Log.e(TAG, "startWork: Invalid state to set value. Value is " + stateToSet);
      return Futures.immediateFuture(Result.failure());
    }

    // Check if engage service is available before publishing.
    Task<Boolean> isAvailable = client.isServiceAvailable();
    ListenableFuture<Result> resultFuture =
        CallbackToFutureAdapter.getFuture(
            completer -> {
              isAvailable.addOnSuccessListener(
                  result -> {
                    // If the service is available, publish
                    if (result) {
                      publishAndSetResult(completer, stateToSet);
                      // Otherwise log failure and return failure
                    } else {
                      Log.d(TAG, "Service unavailable");
                      completer.set(Result.failure());
                    }
                  });
              // This value is used only for debug purposes: it will be used in toString()
              // of returned future or error cases.
              return "Publishing result";
            });
    return resultFuture;
  }

  private void publishAndSetResult(
      CallbackToFutureAdapter.Completer<Result> resultFutureCompleter, String stateToSet) {
    Task<Void> setStateTask;
    // stateToSet defines which cluster to set and must be one of these predefined values.
    switch (stateToSet) {
      case SET_RECOMMENDATIONS:
        setStateTask = setRecommendations(client, loggedInAccount);
        break;
      case SET_FEATURED:
        setStateTask = setFeatured(client, loggedInAccount);
        break;
      case SET_CONTINUATION:
        setStateTask = setContinuation(client, loggedInAccount);
        break;
      case SET_USER_MANAGEMENT:
        setStateTask = setUserManagementCluster(client, loggedInAccount);
        break;
      default:
        throw new IllegalStateException(
            "Cluster to Publish string invalid. String is: " + stateToSet);
    }
    // setStateTask is now a publish or delete task for one of the cluster
    setStateTask
        .addOnSuccessListener(
            // If publishing was successful, set publish status according to whether we published
            // or deleted the cluster. See the comment below for what status to set when and why.
            unused -> {
              resultFutureCompleter.set(Result.success());
              int publishStatusCode;
              if (loggedInAccount.isPresent()) {
                // If an account is logged in, we are definitely publishing content.
                publishStatusCode = AppEngagePublishStatusCode.PUBLISHED;
              } else {
                // Choosing to not publish any content in the absence of account info is not
                // recommended. We do so here purely to demonstrate the updatePublishStatus API.
                // Best practice is to publish non-personalized featured and recommendation clusters
                // (and continuation clusters personalized to guest sessions if applicable).
                // Guidelines for publishing non-personalized featured and recommendation clusters
                // can be found here:
                // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
                publishStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN;
              }
              setPublishStatus(client, publishStatusCode);
            })
        .addOnFailureListener(
            // We received some error while publishing
            publishException -> {
              logPublishingError((AppEngageException) publishException);
              // Some errors are recoverable, such as a threading issue, some are unrecoverable
              // such as a cluster not containing all necessary fields.
              boolean recoverable = isErrorRecoverable((AppEngageException) publishException);
              if (recoverable) {
                // If an error is recoverable, we should attempt to publish again. Setting the
                // result to retry means WorkManager will attempt to run the worker again, thus
                // attempting to publish again.
                resultFutureCompleter.set(Result.retry());
              } else {
                resultFutureCompleter.set(Result.failure());
              }
            });
  }

  static boolean isValidSetStateValue(String setStateValue) {
    if (setStateValue == null) {
      return false;
    }
    switch (setStateValue) {
      case (SET_RECOMMENDATIONS):
      case (SET_FEATURED):
      case (SET_CONTINUATION):
      case (SET_USER_MANAGEMENT):
        return true;
      default:
        return false;
    }
  }

  /**
   * If a logged in account is present, publishes the clusters constructed from
   * getRecommendationClusters(). If no logged in account is present, delete the cluster to emulate
   * not publishing anything.
   * <p>
   * Note this approach of deleting the recommendation cluster when no account is present is NOT
   * advised for a non-sample app. The rationale for doing so in this sample can be found in the
   * {@link
   * com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState#setAllEngageStatePeriodically(Context)}
   *
   * @return Publish task or delete clusters task depending on account status.
   */
  private static Task<Void> setRecommendations(AppEngagePublishClient client,
      Optional<Account> loggedInAccount) {
    if (!loggedInAccount.isPresent()) {
      // Deleting the clusters in the absence of account info is not recommended. We do so here
      // purely to demonstrate the updatePublishStatus API. Best practice is to publish
      // non-personalized clusters. Guidelines for publishing non-personalized clusters can be
      // found here:
      // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
      return client.deleteRecommendationsClusters();
    }
    // Assumes all recommendation clusters are non-empty and there is at least one cluster
    ImmutableList<RecommendationCluster> clusters = getRecommendationClusters();
    PublishRecommendationClustersRequest.Builder publishRequestBuilder =
        new PublishRecommendationClustersRequest.Builder();
    for (RecommendationCluster cluster : clusters) {
      publishRequestBuilder.addRecommendationCluster(cluster);
    }
    return client.publishRecommendationClusters(publishRequestBuilder.build());
  }

  /**
   * If a logged in account is present, publishes the cluster constructed from getFeaturedCluster().
   * If no logged in account is present, then delete the cluster to emulate not publishing
   * anything.
   * <p>
   * Note this approach of deleting the featured cluster when no account is present is NOT advised
   * for a non-sample app. The rationale for doing so in this sample can be found in
   * {@link
   * com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState#setAllEngageStatePeriodically(Context)}
   *
   * @return Publish task or delete clusters task depending on account status.
   */
  private static Task<Void> setFeatured(AppEngagePublishClient client,
      Optional<Account> loggedInAccount) {
    if (!loggedInAccount.isPresent()) {
      // Deleting the clusters in the absence of account info is not recommended. We do so here
      // purely to demonstrate the updatePublishStatus API. Best practice is to publish
      // non-personalized clusters. Guidelines for publishing non-personalized clusters can be
      // found here:
      // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
      return client.deleteFeaturedCluster();
    }
    // Assumes a non-empty featured cluster from getFeaturedCluster()
    PublishFeaturedClusterRequest publishRequest =
        new PublishFeaturedClusterRequest.Builder()
            .setFeaturedCluster(getFeaturedCluster())
            .build();
    return client.publishFeaturedCluster(publishRequest);
  }

  /**
   * Publishes the cluster given current account info. If no account is present, delete the cluster.
   * Otherwise publish the built cluster from getContinuationCluster(), as long as it is non-empty.
   * If that built cluster is empty, instead delete the cluster. Deleting the cluster emulates
   * publishing nothing.
   *
   * @return Publish task or delete clusters task depending on account status.
   */
  private static Task<Void> setContinuation(AppEngagePublishClient client,
      Optional<Account> loggedInAccount) {
    if (!loggedInAccount.isPresent()) {
      // Choosing to not publish any content in the absence of account info is not recommended. We
      // do so here purely to demonstrate the updatePublishStatus API. Best practice is to publish
      // non-personalized featured and recommendation clusters (and continuation clusters
      // personalized to guest sessions if applicable). Guidelines for publishing non-personalized
      // featured and recommendation clusters can be found here:
      // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
      return client.deleteContinuationCluster();
    }
    ContinuationCluster cluster = getContinuationCluster(loggedInAccount.get());
    if (cluster.getEntities().isEmpty()) {
      return client.deleteContinuationCluster();
    }
    PublishContinuationClusterRequest publishRequest =
        new PublishContinuationClusterRequest.Builder().setContinuationCluster(cluster).build();
    return client.publishContinuationCluster(publishRequest);
  }

  /**
   * Publishes a sign in card to the user management cluster and sets publish status to
   * NOT_PUBLISHED_REQUIRES_SIGN_IN if no account is logged in. Otherwise deletes the user
   * management cluster to emulate publishing nothing and sets publish status to PUBLISHED.
   *
   * @return Publish task of delete clusters task depending on account status.
   */
  private static Task<Void> setUserManagementCluster(AppEngagePublishClient client,
      Optional<Account> loggedInAccount) {
    if (loggedInAccount.isPresent()) {
      return client.deleteUserManagementCluster();
    }
    // Choosing to not publish any content in the absence of account info is not recommended. We
    // do so here purely to demonstrate the updatePublishStatus API. Best practice is to publish
    // non-personalized featured and recommendation clusters (and continuation clusters
    // personalized to guest sessions if applicable). Guidelines for publishing non-personalized
    // featured and recommendation clusters can be found here:
    // https://developer.android.com/guide/playcore/engage/publish#rec-signed-out
    PublishUserAccountManagementRequest publishRequest =
        new PublishUserAccountManagementRequest.Builder()
            .setSignInCardEntity(getSignInCard())
            .build();
    return client.publishUserAccountManagementRequest(publishRequest);
  }

  private static void setPublishStatus(AppEngagePublishClient client, int publishStatusCode) {
    client.updatePublishStatus(
        new PublishStatusRequest.Builder().setStatusCode(publishStatusCode).build());
  }
}
