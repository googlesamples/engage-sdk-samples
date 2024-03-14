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
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.android.engage.common.datamodel.PlatformType
import com.google.android.engage.service.AppEngageErrorCode
import com.google.android.engage.service.AppEngageException
import com.google.android.engage.service.AppEngagePublishClient
import com.google.android.engage.service.AppEngagePublishStatusCode
import com.google.android.engage.video.datamodel.WatchNextType
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.samples.quickstart.engagesdksamples.watch.data.model.MovieItem
import com.google.samples.quickstart.engagesdksamples.watch.data.room.AccountDao
import com.google.samples.quickstart.engagesdksamples.watch.data.room.MovieDao
import com.google.samples.quickstart.engagesdksamples.watch.data.room.WatchDatabase
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.MAX_PUBLISHING_ATTEMPTS
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_CONTINUATION
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_FEATURED
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_RECOMMENDATIONS
import com.google.samples.quickstart.engagesdksamples.watch.publish.Constants.PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EngageServiceWorkerTest {

  @Before
  fun setUp() {
    mockedContext = ApplicationProvider.getApplicationContext()
    every { mockedDb.accountDao() } returns mockedAccountDao
    every { mockedDb.movieDao() } returns mockedDao
    inProgressMovieItem =
      MovieItem(
          id = "1",
          movieName = "Test",
          landscapePoster = 1,
          playbackUri = "Test",
          platformSpecificPlaybackUri = "Test",
          platformType =  PlatformType.TYPE_ANDROID_TV,
          releaseDate = 1L,
          availability = 1,
          durationMillis = 1L,
          genre = "Test",
          contentRatingAgency = "ContentRatingAgency",
          contentRating = "Test"
        )
        .apply {
          currentlyWatching = true
          watchNextType = WatchNextType.TYPE_CONTINUE
          lastEngagementTimeMillis = 9078563412L
          lastPlaybackTimeMillis = 123456789L
        }
  }

  @Test
  fun publishRecommendationsFailsWhenServiceUnavailableTest() {
    val mockedAvailability = Tasks.forResult(false)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val mockedWorker = createEngageServiceWorker(mockedContext, PUBLISH_TYPE_RECOMMENDATIONS, 0)
    runBlocking {
      val resultFail = mockedWorker.doWork()
      assertEquals(Result.failure(), resultFail)
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.updatePublishStatus(any()) wasNot Called
      }
    }
  }

  @Test
  fun publishFeaturedFailsWhenServiceUnavailableTest() {
    val mockedAvailability = Tasks.forResult(false)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val mockedWorker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_FEATURED, runAttempts = 0)
    runBlocking {
      val resultFail = mockedWorker.doWork()
      assertEquals(Result.failure(), resultFail)
      verify {
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.updatePublishStatus(any()) wasNot Called
      }
    }
  }

  @Test
  fun publishContinuationFailsWhenServiceUnavailableTest() {
    val mockedAvailability = Tasks.forResult(false)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val mockedWorker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_CONTINUATION, runAttempts = 0)
    runBlocking {
      val resultFail = mockedWorker.doWork()
      assertEquals(Result.failure(), resultFail)
      verify {
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.updatePublishStatus(any()) wasNot Called
      }
    }
  }

  @Test
  fun publishUserManagementFailsWhenServiceUnavailableTest() {
    val mockedAvailability = Tasks.forResult(false)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val mockedWorker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT,
        runAttempts = 0
      )
    runBlocking {
      val resultFail = mockedWorker.doWork()
      assertEquals(Result.failure(), resultFail)
      verify {
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called
        mockedClient.updatePublishStatus(any()) wasNot Called
      }
    }
  }

  @Test
  fun publishRecommendationsWithAccountSucceedsAndPublishesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishRecommendationClusters(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_RECOMMENDATIONS, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED
      verify {
        mockedClient.publishRecommendationClusters(any())
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishRecommendationsWithoutAccountSucceedsAndDeletesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns false
    every { mockedClient.deleteRecommendationsClusters() } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_RECOMMENDATIONS, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters()
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishFeaturedWithAccountSucceedsAndPublishesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishFeaturedCluster(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker = createEngageServiceWorker(mockedContext, PUBLISH_TYPE_FEATURED, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any())
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishFeaturedWithoutAccountSucceedsAndDeletesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns false
    every { mockedClient.deleteFeaturedCluster() } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker = createEngageServiceWorker(mockedContext, PUBLISH_TYPE_FEATURED, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster()
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishContinuationWithAccountAndInProgressMoviesSucceedsAndPublishesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishContinuationCluster(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    // At least one movie is in progress
    coEvery { mockedDao.loadMovieIsCurrentlyWatching(currentlyWatching = true) } returns
      listOf(inProgressMovieItem)

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_CONTINUATION, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any())
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishContinuationWithAccountAndNoInProgressMoviesSucceedsAndDeletesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.deleteContinuationCluster() } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    // No movie is in progress
    coEvery { mockedDao.loadMovieIsCurrentlyWatching(currentlyWatching = true) } returns listOf()

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_CONTINUATION, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster()
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishContinuationWithoutAccountSucceedsAndDeletesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns false
    every { mockedClient.deleteContinuationCluster() } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_CONTINUATION, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster()
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishUserManagementWithNoAccountSucceedsAndPublishesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns false
    every { mockedClient.publishUserAccountManagementRequest(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT,
        runAttempts = 0
      )

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.NOT_PUBLISHED_REQUIRES_SIGN_IN
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.publishUserAccountManagementRequest(any())
        mockedClient.deleteUserManagementCluster() wasNot Called

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishUserManagementWithAccountSucceedsAndDeletesAndSetsStatusTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.deleteUserManagementCluster() } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT,
        runAttempts = 0
      )

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(Result.success(), actualResult)
      val expectedStatusCode = AppEngagePublishStatusCode.PUBLISHED
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster()

        mockedClient.updatePublishStatus(
          withArg { assertTrue(it.statusCode == expectedStatusCode) }
        )
        mockedClient.updatePublishStatus(
          withArg { assertFalse(it.statusCode == expectedStatusCode) }
        ) wasNot Called
      }
    }
  }

  @Test
  fun publishRecommendationsFailsOnUnrecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_NOT_FOUND,
        AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
        AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT,
        AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishRecommendationsWithErrorReturnsResultHelper(errorCode, Result.failure())
    }
  }

  @Test
  fun publishRecommendationsRetryOnRecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED,
        AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
        AppEngageErrorCode.SERVICE_CALL_INTERNAL
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishRecommendationsWithErrorReturnsResultHelper(errorCode, Result.retry())
    }
  }

  @Test
  fun publishFeaturedFailsOnUnrecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_NOT_FOUND,
        AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
        AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT,
        AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishFeaturedWithErrorReturnsResultHelper(errorCode, Result.failure())
    }
  }

  @Test
  fun publishFeaturedRetryOnRecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED,
        AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
        AppEngageErrorCode.SERVICE_CALL_INTERNAL
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishFeaturedWithErrorReturnsResultHelper(errorCode, Result.retry())
    }
  }

  @Test
  fun publishContinuationFailsOnUnrecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_NOT_FOUND,
        AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
        AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT,
        AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishContinuationWithErrorReturnsResultHelper(errorCode, Result.failure())
    }
  }

  @Test
  fun publishContinuationRetryOnRecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED,
        AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
        AppEngageErrorCode.SERVICE_CALL_INTERNAL
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishContinuationWithErrorReturnsResultHelper(errorCode, Result.retry())
    }
  }

  @Test
  fun publishUserManagementFailsOnUnrecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_NOT_FOUND,
        AppEngageErrorCode.SERVICE_NOT_AVAILABLE,
        AppEngageErrorCode.SERVICE_CALL_INVALID_ARGUMENT,
        AppEngageErrorCode.SERVICE_CALL_PERMISSION_DENIED,
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishUserManagementWithErrorReturnsResultHelper(errorCode, Result.failure())
    }
  }

  @Test
  fun publishUserManagementRetryOnRecoverableExceptions() {
    val unrecoverableErrorCodes =
      listOf(
        AppEngageErrorCode.SERVICE_CALL_RESOURCE_EXHAUSTED,
        AppEngageErrorCode.SERVICE_CALL_EXECUTION_FAILURE,
        AppEngageErrorCode.SERVICE_CALL_INTERNAL
      )
    for (errorCode in unrecoverableErrorCodes) {
      verifyPublishUserManagementWithErrorReturnsResultHelper(errorCode, Result.retry())
    }
  }

  @Test
  fun attemptToPublishRecommendationsAtMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishRecommendationClusters(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_RECOMMENDATIONS,
        MAX_PUBLISHING_ATTEMPTS
      )

    runBlocking {
      worker.doWork()
      verify { mockedClient.publishRecommendationClusters(any()) }
    }
  }

  @Test
  fun attemptToPublishFeaturedAtMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishFeaturedCluster(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_FEATURED, MAX_PUBLISHING_ATTEMPTS)

    runBlocking {
      worker.doWork()
      verify { mockedClient.publishFeaturedCluster(any()) }
    }
  }

  @Test
  fun attemptToPublishContinuationAtMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishContinuationCluster(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    // At least one movie is in progress
    coEvery { mockedDao.loadMovieIsCurrentlyWatching(currentlyWatching = true) } returns
      listOf(inProgressMovieItem)

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_CONTINUATION, MAX_PUBLISHING_ATTEMPTS)

    runBlocking {
      worker.doWork()
      verify { mockedClient.publishContinuationCluster(any()) }
    }
  }

  @Test
  fun attemptToPublishUserManagementAtMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultingTask: Task<Void> = Tasks.forResult(null)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns false
    every { mockedClient.publishUserAccountManagementRequest(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns resultingTask

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT,
        MAX_PUBLISHING_ATTEMPTS
      )

    runBlocking {
      worker.doWork()
      verify { mockedClient.publishUserAccountManagementRequest(any()) }
    }
  }

  @Test
  fun doNotAttemptToPublishOrDeleteRecommendationsPastMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_RECOMMENDATIONS,
        MAX_PUBLISHING_ATTEMPTS + 1
      )

    runBlocking {
      worker.doWork()
      verify {
        mockedClient.publishRecommendationClusters(any()) wasNot Called
        mockedClient.deleteRecommendationsClusters() wasNot Called
      }
    }
  }

  @Test
  fun doNotAttemptToPublishOrDeleteFeaturedPastMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_FEATURED, MAX_PUBLISHING_ATTEMPTS + 1)

    runBlocking {
      worker.doWork()
      verify {
        mockedClient.publishFeaturedCluster(any()) wasNot Called
        mockedClient.deleteFeaturedCluster() wasNot Called
      }
    }
  }

  @Test
  fun doNotAttemptToPublishOrDeleteContinuationPastMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_CONTINUATION,
        MAX_PUBLISHING_ATTEMPTS + 1
      )

    runBlocking {
      worker.doWork()
      verify {
        mockedClient.publishContinuationCluster(any()) wasNot Called
        mockedClient.deleteContinuationCluster() wasNot Called
      }
    }
  }

  @Test
  fun doNotAttemptToPublishOrDeleteUserManagementPastMaxAttemptsTest() {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT,
        MAX_PUBLISHING_ATTEMPTS + 1
      )

    runBlocking {
      worker.doWork()
      verify {
        mockedClient.publishUserAccountManagementRequest(any()) wasNot Called
        mockedClient.deleteUserManagementCluster() wasNot Called
      }
    }
  }

  private fun verifyPublishRecommendationsWithErrorReturnsResultHelper(
    errorCode: Int,
    expectedResult: Result
  ) {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultException = AppEngageException(errorCode)
    val resultingTask: Task<Void> = Tasks.forException(resultException)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishRecommendationClusters(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns Tasks.forResult(null)

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_RECOMMENDATIONS, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(expectedResult, actualResult)
    }
  }

  private fun verifyPublishFeaturedWithErrorReturnsResultHelper(
    errorCode: Int,
    expectedResult: Result
  ) {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultException = AppEngageException(errorCode)
    val resultingTask: Task<Void> = Tasks.forException(resultException)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishFeaturedCluster(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns Tasks.forResult(null)

    val worker = createEngageServiceWorker(mockedContext, PUBLISH_TYPE_FEATURED, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(expectedResult, actualResult)
    }
  }

  private fun verifyPublishContinuationWithErrorReturnsResultHelper(
    errorCode: Int,
    expectedResult: Result
  ) {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultException = AppEngageException(errorCode)
    val resultingTask: Task<Void> = Tasks.forException(resultException)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns true
    every { mockedClient.publishContinuationCluster(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns Tasks.forResult(null)

    // At least one movie is in progress
    coEvery { mockedDao.loadMovieIsCurrentlyWatching(currentlyWatching = true) } returns
      listOf(inProgressMovieItem)

    val worker =
      createEngageServiceWorker(mockedContext, PUBLISH_TYPE_CONTINUATION, runAttempts = 0)

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(expectedResult, actualResult)
    }
  }

  private fun verifyPublishUserManagementWithErrorReturnsResultHelper(
    errorCode: Int,
    expectedResult: Result
  ) {
    val mockedAvailability = Tasks.forResult(true)
    every { mockedClient.isServiceAvailable } returns mockedAvailability

    val resultException = AppEngageException(errorCode)
    val resultingTask: Task<Void> = Tasks.forException(resultException)

    coEvery { mockedAccountDao.isAccountSignedIn() } returns false
    every { mockedClient.publishUserAccountManagementRequest(any()) } returns resultingTask
    every { mockedClient.updatePublishStatus(any()) } returns Tasks.forResult(null)

    val worker =
      createEngageServiceWorker(
        mockedContext,
        PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT,
        runAttempts = 0
      )

    runBlocking {
      val actualResult = worker.doWork()
      assertEquals(expectedResult, actualResult)
    }
  }

  private fun createEngageServiceWorker(
    context: Context,
    publishClusterType: String,
    runAttempts: Int
  ): EngageServiceWorker {
    val workerData = workDataOf(PUBLISH_TYPE to publishClusterType)
    return TestListenableWorkerBuilder<EngageServiceWorker>(
        context = context,
        inputData = workerData,
        runAttemptCount = runAttempts
      )
      .setWorkerFactory(EngageServiceWorkerFactory())
      .build()
  }

  private class EngageServiceWorkerFactory() : WorkerFactory() {
    override fun createWorker(
      appContext: Context,
      workerClassName: String,
      workerParameters: WorkerParameters
    ): ListenableWorker {
      return EngageServiceWorker(appContext, workerParameters, mockedClient, mockedDb)
    }
  }

  companion object {
    private lateinit var inProgressMovieItem: MovieItem
    private lateinit var mockedContext: Context
    private val mockedClient = mockk<AppEngagePublishClient>()
    private val mockedDb = mockk<WatchDatabase>()
    private val mockedDao = mockk<MovieDao>()
    private val mockedAccountDao = mockk<AccountDao>()
  }
}
