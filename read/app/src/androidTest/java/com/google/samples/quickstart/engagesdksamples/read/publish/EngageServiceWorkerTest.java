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

import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.MAX_SET_STATE_RETRIES;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_CONTINUATION;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_FEATURED;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_RECOMMENDATIONS;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_STATE_KEY;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_USER_MANAGEMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.ListenableWorker.Result;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;
import androidx.work.testing.TestListenableWorkerBuilder;
import com.google.android.engage.service.AppEngageErrorCode;
import com.google.android.engage.service.AppEngageException;
import com.google.android.engage.service.AppEngagePublishClient;
import com.google.android.engage.service.AppEngagePublishStatusCode;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Optional;
import com.google.samples.quickstart.engagesdksamples.read.login.Account;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(AndroidJUnit4.class)
public class EngageServiceWorkerTest {

  @Mock private AppEngagePublishClient mockPublishClient;
  private Context appContext;
  private WorkerFactory workerFactoryWithoutAccount;
  private WorkerFactory workerFactoryWithFreshAccount;
  private WorkerFactory workerFactoryWithAccountWithOneReadEbook;

  @Before
  public void setUp() {
    appContext = ApplicationProvider.getApplicationContext();
    mockPublishClient = mock();
    // Factories used to inject the mock publish client into test workers with different accounts.
    Ebook ebook = new Ebook(/* id= */ 1);
    Account accountWithReadEbooks = new Account();
    accountWithReadEbooks.markInProgressPageInEbook(
        ebook, /* page= */ 1, /* engagementTime= */ 100L);
    workerFactoryWithAccountWithOneReadEbook =
        new EngageServiceWorkerFactory(mockPublishClient, Optional.of(accountWithReadEbooks));

    workerFactoryWithFreshAccount =
        new EngageServiceWorkerFactory(mockPublishClient, Optional.of(new Account()));

    workerFactoryWithoutAccount =
        new EngageServiceWorkerFactory(mockPublishClient, /* loggedInAccount= */ Optional.absent());
  }

  @Test
  public void whenAvailableWithAccountPublishOnlyRecommendationsAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.publishRecommendationClusters(any())).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_RECOMMENDATIONS, /* runAttemptCount= */ 0, workerFactoryWithFreshAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).publishRecommendationClusters(any());
    verify(mockPublishClient, never()).deleteRecommendationsClusters();
    verify(mockPublishClient, never()).publishFeaturedCluster(any());
    verify(mockPublishClient, never()).publishContinuationCluster(any());
    verify(mockPublishClient, never()).publishUserAccountManagementRequest(any());

    int expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithoutAccountDeleteRecommendationsAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.deleteRecommendationsClusters()).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_RECOMMENDATIONS, /* runAttemptCount= */ 0, workerFactoryWithoutAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).deleteRecommendationsClusters();
    verify(mockPublishClient, never()).publishRecommendationClusters(any());
    verify(mockPublishClient, never()).publishFeaturedCluster(any());
    verify(mockPublishClient, never()).publishContinuationCluster(any());
    verify(mockPublishClient, never()).publishUserAccountManagementRequest(any());

    int expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithAccountPublishOnlyFeaturedAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.publishFeaturedCluster(any())).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(SET_FEATURED, /* runAttemptCount= */ 0, workerFactoryWithFreshAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).publishFeaturedCluster(any());
    verify(mockPublishClient, never()).deleteFeaturedCluster();
    verify(mockPublishClient, never()).publishRecommendationClusters(any());
    verify(mockPublishClient, never()).publishContinuationCluster(any());
    verify(mockPublishClient, never()).publishUserAccountManagementRequest(any());

    int expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithoutAccountDeleteFeaturedAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.deleteFeaturedCluster()).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(SET_FEATURED, /* runAttemptCount= */ 0, workerFactoryWithoutAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).deleteFeaturedCluster();
    verify(mockPublishClient, never()).publishFeaturedCluster(any());
    verify(mockPublishClient, never()).publishRecommendationClusters(any());
    verify(mockPublishClient, never()).publishContinuationCluster(any());
    verify(mockPublishClient, never()).publishUserAccountManagementRequest(any());

    int expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithReadBooksPublishOnlyContinuationAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.publishContinuationCluster(any())).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_CONTINUATION, /* runAttemptCount= */ 0, workerFactoryWithAccountWithOneReadEbook);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).publishContinuationCluster(any());
    verify(mockPublishClient, never()).deleteContinuationCluster();
    verify(mockPublishClient, never()).publishRecommendationClusters(any());
    verify(mockPublishClient, never()).publishFeaturedCluster(any());
    verify(mockPublishClient, never()).publishUserAccountManagementRequest(any());

    int expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithFreshAccountDeleteContinuationAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.deleteContinuationCluster()).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_CONTINUATION, /* runAttemptCount= */ 0, workerFactoryWithFreshAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).deleteContinuationCluster();
    verify(mockPublishClient, never()).publishContinuationCluster(any());

    int expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithoutAccountDeleteContinuationAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.deleteContinuationCluster()).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(SET_CONTINUATION, /* runAttemptCount= */ 0, workerFactoryWithoutAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).deleteContinuationCluster();
    verify(mockPublishClient, never()).publishContinuationCluster(any());

    int expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithAccountDeleteUserManagementAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.deleteUserManagementCluster()).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_USER_MANAGEMENT, /* runAttemptCount= */ 0, workerFactoryWithFreshAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).deleteUserManagementCluster();
    verify(mockPublishClient, never()).publishUserAccountManagementRequest(any());

    int expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void whenAvailableWithoutAccountPublishUserManagementAndSetStatusAndReturnSuccessTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishSuccess = Tasks.forResult(null);
    when(mockPublishClient.publishUserAccountManagementRequest(any())).thenReturn(publishSuccess);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_USER_MANAGEMENT, /* runAttemptCount= */ 0, workerFactoryWithoutAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.success(), result);

    verify(mockPublishClient, times(1)).publishUserAccountManagementRequest(any());
    verify(mockPublishClient, never()).deleteUserManagementCluster();

    int expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN;
    verify(mockPublishClient, times(1))
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() == expectedStatusCode));
    verify(mockPublishClient, never())
        .updatePublishStatus(
            argThat(
                publishStatusRequest ->
                    publishStatusRequest.getStatusCode() != expectedStatusCode));
  }

  @Test
  public void publishRecommendationsExceptionsIndicatingRetryReturnRetryTest() throws Exception {
    int[] retryEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
      AppEngageErrorCode.SERVICE_CALL_INTERNAL,
      AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED
    };
    for (int retryEngageErrorCode : retryEngageErrorCodes) {
      verifyErrorCodeForRecommendationsWithAccountGivesResultHelper(
          retryEngageErrorCode, Result.retry());
    }
  }

  @Test
  public void publishFeaturedExceptionsIndicatingRetryReturnRetryTest() throws Exception {
    int[] retryEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
      AppEngageErrorCode.SERVICE_CALL_INTERNAL,
      AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED
    };
    for (int retryEngageErrorCode : retryEngageErrorCodes) {
      verifyErrorCodeForFeaturedWithAccountGivesResultHelper(retryEngageErrorCode, Result.retry());
    }
  }

  @Test
  public void publishContinuationExceptionsIndicatingRetryReturnRetryTest() throws Exception {
    int[] retryEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
      AppEngageErrorCode.SERVICE_CALL_INTERNAL,
      AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED
    };
    for (int retryEngageErrorCode : retryEngageErrorCodes) {
      verifyErrorCodeForContinuationWithReadEbooksGivesResultHelper(
          retryEngageErrorCode, Result.retry());
    }
  }

  @Test
  public void publishUserManagementExceptionsIndicatingRetryReturnRetryTest() throws Exception {
    int[] retryEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
      AppEngageErrorCode.SERVICE_CALL_INTERNAL,
      AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED
    };
    for (int retryEngageErrorCode : retryEngageErrorCodes) {
      verifyErrorCodeForUserManagementWithoutAccountGivesResultHelper(
          retryEngageErrorCode, Result.retry());
    }
  }

  @Test
  public void publishRecommendationsExceptionsIndicatingFailureReturnFailureTest()
      throws Exception {
    int[] failureEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_NOT_FOUND,
      AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
      AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT
    };
    for (int failureEngageErrorCode : failureEngageErrorCodes) {
      verifyErrorCodeForRecommendationsWithAccountGivesResultHelper(
          failureEngageErrorCode, Result.failure());
    }
  }

  @Test
  public void publishFeaturedExceptionsIndicatingFailureReturnFailureTest()
      throws Exception {
    int[] failureEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_NOT_FOUND,
      AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
      AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT
    };
    for (int failureEngageErrorCode : failureEngageErrorCodes) {
      verifyErrorCodeForFeaturedWithAccountGivesResultHelper(
          failureEngageErrorCode, Result.failure());
    }
  }

  @Test
  public void publishContinuationExceptionsIndicatingFailureReturnFailureTest()
      throws Exception {
    int[] failureEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_NOT_FOUND,
      AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
      AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT
    };
    for (int failureEngageErrorCode : failureEngageErrorCodes) {
      verifyErrorCodeForContinuationWithReadEbooksGivesResultHelper(
          failureEngageErrorCode, Result.failure());
    }
  }

  @Test
  public void publishUserManagementExceptionsIndicatingFailureReturnFailureTest() throws Exception {
    int[] failureEngageErrorCodes = {
      AppEngageErrorCode.SERVICE_NOT_FOUND,
      AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
      AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT
    };
    for (int failureEngageErrorCode : failureEngageErrorCodes) {
      verifyErrorCodeForUserManagementWithoutAccountGivesResultHelper(
          failureEngageErrorCode, Result.failure());
    }
  }

  @Test
  public void attemptToPublishRecommendationsWithAccountAtMaxAttemptsTest() throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishOutput = Tasks.forResult(null);
    when(mockPublishClient.publishRecommendationClusters(any())).thenReturn(publishOutput);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_RECOMMENDATIONS, MAX_SET_STATE_RETRIES, workerFactoryWithFreshAccount);

    worker.startWork().get();
    verify(mockPublishClient, times(1)).publishRecommendationClusters(any());
  }

  @Test
  public void attemptToPublishFeaturedWithAccountAtMaxAttemptsTest() throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishOutput = Tasks.forResult(null);
    when(mockPublishClient.publishFeaturedCluster(any())).thenReturn(publishOutput);

    EngageServiceWorker worker =
        getSetStateWorker(SET_FEATURED, MAX_SET_STATE_RETRIES, workerFactoryWithFreshAccount);

    worker.startWork().get();
    verify(mockPublishClient, times(1)).publishFeaturedCluster(any());
  }

  @Test
  public void attemptToPublishContinuationWithReadBooksAtMaxAttemptsTest() throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishOutput = Tasks.forResult(null);
    when(mockPublishClient.publishContinuationCluster(any())).thenReturn(publishOutput);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_CONTINUATION, MAX_SET_STATE_RETRIES, workerFactoryWithAccountWithOneReadEbook);

    worker.startWork().get();
    verify(mockPublishClient, times(1)).publishContinuationCluster(any());
  }

  @Test
  public void attemptToPublishUserManagementWithoutAccountAtMaxAttemptsTest() throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> publishOutput = Tasks.forResult(null);
    when(mockPublishClient.publishUserAccountManagementRequest(any())).thenReturn(publishOutput);

    EngageServiceWorker worker =
        getSetStateWorker(SET_USER_MANAGEMENT, MAX_SET_STATE_RETRIES, workerFactoryWithoutAccount);

    worker.startWork().get();
    verify(mockPublishClient, times(1)).publishUserAccountManagementRequest(any());
  }

  @Test
  public void doNotPublishRecommendationsAndReturnFailurePastMaxAttemptsTest() throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_RECOMMENDATIONS, MAX_SET_STATE_RETRIES + 1, workerFactoryWithoutAccount);

    Result result = worker.startWork().get();
    assertEquals(Result.failure(), result);
    verify(mockPublishClient, never()).publishRecommendationClusters(any());
  }

  @Test
  public void doNotPublishOrDeleteFeaturedAndReturnFailurePastMaxAttemptsTest() throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    EngageServiceWorker worker =
        getSetStateWorker(SET_FEATURED, MAX_SET_STATE_RETRIES + 1, workerFactoryWithoutAccount);
    Result result = worker.startWork().get();
    assertEquals(Result.failure(), result);
    verify(mockPublishClient, never()).publishFeaturedCluster(any());
    verify(mockPublishClient, never()).deleteFeaturedCluster();
  }

  @Test
  public void doNotPublishOrDeleteContinuationAndReturnFailurePastMaxAttemptsTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    EngageServiceWorker worker =
        getSetStateWorker(SET_CONTINUATION, MAX_SET_STATE_RETRIES + 1, workerFactoryWithoutAccount);
    Result result = worker.startWork().get();
    assertEquals(Result.failure(), result);
    verify(mockPublishClient, never()).publishContinuationCluster(any());
    verify(mockPublishClient, never()).deleteContinuationCluster();
  }

  @Test
  public void doNotPublishOrDeleteUserManagementAndReturnFailurePastMaxAttemptsTest()
      throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_USER_MANAGEMENT, MAX_SET_STATE_RETRIES + 1, workerFactoryWithoutAccount);
    Result result = worker.startWork().get();
    assertEquals(Result.failure(), result);
    verify(mockPublishClient, never()).publishUserAccountManagementRequest(any());
    verify(mockPublishClient, never()).deleteUserManagementCluster();
  }

  @Test
  public void returnFailureAndDoNotPublishWithInvalidClusterToPublish() throws Exception {
    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    String invalidCluster = "Invalid Cluster";
    assertFalse(EngageServiceWorker.isValidSetStateValue(invalidCluster));

    EngageServiceWorker worker =
        getSetStateWorker(invalidCluster, /* runAttemptCount= */ 0, workerFactoryWithoutAccount);
    Result result = worker.startWork().get();
    assertEquals(Result.failure(), result);

    verify(mockPublishClient, never()).publishRecommendationClusters(any());
    verify(mockPublishClient, never()).publishFeaturedCluster(any());
    verify(mockPublishClient, never()).publishContinuationCluster(any());
  }

  private EngageServiceWorker getSetStateWorker(String clusterToPublish, int runAttemptCount,
      WorkerFactory workerFactory) {
    Data clusterToPublishData =
        new Data.Builder().put(SET_STATE_KEY, clusterToPublish).build();
    EngageServiceWorker worker =
        TestListenableWorkerBuilder.from(appContext, EngageServiceWorker.class)
            .setWorkerFactory(workerFactory)
            .setInputData(clusterToPublishData)
            .setRunAttemptCount(runAttemptCount)
            .build();
    return worker;
  }

  private void verifyErrorCodeForRecommendationsWithAccountGivesResultHelper(
      int engageErrorCode,
      Result expected)
      throws Exception {
    AppEngageException engageException = new AppEngageException(engageErrorCode);

    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> exceptionOutput = Tasks.forException(engageException);
    when(mockPublishClient.publishRecommendationClusters(any())).thenReturn(exceptionOutput);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_RECOMMENDATIONS, /* runAttemptCount= */ 0, workerFactoryWithFreshAccount);

    Result result = worker.startWork().get();
    assertEquals(expected, result);
  }

  private void verifyErrorCodeForFeaturedWithAccountGivesResultHelper(int engageErrorCode,
      Result expected)
      throws Exception {
    AppEngageException engageException = new AppEngageException(engageErrorCode);

    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> exceptionOutput = Tasks.forException(engageException);
    when(mockPublishClient.publishFeaturedCluster(any())).thenReturn(exceptionOutput);

    EngageServiceWorker worker =
        getSetStateWorker(SET_FEATURED, /* runAttemptCount= */ 0, workerFactoryWithFreshAccount);

    Result result = worker.startWork().get();
    assertEquals(expected, result);
  }

  private void verifyErrorCodeForContinuationWithReadEbooksGivesResultHelper(int engageErrorCode,
      Result expected)
      throws Exception {
    AppEngageException engageException = new AppEngageException(engageErrorCode);

    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> exceptionOutput = Tasks.forException(engageException);
    when(mockPublishClient.publishContinuationCluster(any())).thenReturn(exceptionOutput);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_CONTINUATION, /* runAttemptCount= */ 0, workerFactoryWithAccountWithOneReadEbook);

    Result result = worker.startWork().get();
    assertEquals(expected, result);
  }

  private void verifyErrorCodeForUserManagementWithoutAccountGivesResultHelper(int engageErrorCode,
      Result expected)
      throws Exception {
    AppEngageException engageException = new AppEngageException(engageErrorCode);

    Task<Boolean> serviceAvailable = Tasks.forResult(Boolean.TRUE);
    when(mockPublishClient.isServiceAvailable()).thenReturn(serviceAvailable);

    Task<Void> exceptionOutput = Tasks.forException(engageException);
    when(mockPublishClient.publishUserAccountManagementRequest(any())).thenReturn(exceptionOutput);

    EngageServiceWorker worker =
        getSetStateWorker(
            SET_USER_MANAGEMENT, /* runAttemptCount= */ 0, workerFactoryWithoutAccount);

    Result result = worker.startWork().get();
    assertEquals(expected, result);
  }

  /**
   * Allows creation of an EngageServiceWorker with a custom publishing client, used to inject a
   * mock client into the worker
   */
  private static final class EngageServiceWorkerFactory extends WorkerFactory {

    private final AppEngagePublishClient client;
    private final Optional<Account> loggedInAccount;

    private EngageServiceWorkerFactory(
        AppEngagePublishClient client, Optional<Account> loggedInAccount) {
      this.client = client;
      this.loggedInAccount = loggedInAccount;
    }

    @Override
    @Nullable
    public ListenableWorker createWorker(
        @NonNull Context context,
        @NonNull String wrapperClassName,
        @NonNull WorkerParameters workerParameters) {
      if (wrapperClassName.equals(EngageServiceWorker.class.getName())) {
        return new EngageServiceWorker(context, workerParameters, client, loggedInAccount);
      } else {
        return null;
      }
    }
  }
}
