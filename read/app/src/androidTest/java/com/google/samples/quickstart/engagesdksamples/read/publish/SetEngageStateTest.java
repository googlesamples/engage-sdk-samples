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

import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_CONTINUATION_ONE_TIME_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_CONTINUATION_PERIODICALLY_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_FEATURED_ONE_TIME_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_FEATURED_PERIODICALLY_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_RECOMMENDATIONS_ONE_TIME_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_RECOMMENDATIONS_PERIODICALLY_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.Constants.SET_USER_MANAGEMENT_PERIODICALLY_WORK_NAME;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setAllEngageStatePeriodically;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setContinuationCluster;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setFeaturedCluster;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setRecommendationClusters;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.Configuration;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.WorkManagerTestInitHelper;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SetEngageStateTest {

  private Context appContext;
  private WorkManager workManager;

  @Before
  public void setUp() {
    appContext = ApplicationProvider.getApplicationContext();
    SynchronousExecutor synchronousExecutor = new SynchronousExecutor();
    Configuration config =
        new Configuration.Builder()
            .setExecutor(synchronousExecutor)
            .setTaskExecutor(synchronousExecutor)
            .build();
    WorkManagerTestInitHelper.initializeTestWorkManager(appContext, config);
    workManager = WorkManager.getInstance(appContext);
  }

  @Test
  public void setRecommendationsStartsWorkTest() throws Exception {
    setRecommendationClusters(appContext);
    assertSetStateWorkIsQueuedHelper(SET_RECOMMENDATIONS_ONE_TIME_WORK_NAME);
  }

  @Test
  public void setFeaturedStartsWorkTest() throws Exception {
    setFeaturedCluster(appContext);
    assertSetStateWorkIsQueuedHelper(SET_FEATURED_ONE_TIME_WORK_NAME);
  }

  @Test
  public void setContinuationStartsWorkTest() throws Exception {
    setContinuationCluster(appContext);
    assertSetStateWorkIsQueuedHelper(SET_CONTINUATION_ONE_TIME_WORK_NAME);
  }

  @Test
  public void setAllEngageStatePeriodicallyQueuesAllWorkTest() throws Exception {
    setAllEngageStatePeriodically(appContext);
    assertSetStateWorkIsQueuedHelper(SET_RECOMMENDATIONS_PERIODICALLY_WORK_NAME);
    assertSetStateWorkIsQueuedHelper(SET_FEATURED_PERIODICALLY_WORK_NAME);
    assertSetStateWorkIsQueuedHelper(SET_CONTINUATION_PERIODICALLY_WORK_NAME);
    assertSetStateWorkIsQueuedHelper(SET_USER_MANAGEMENT_PERIODICALLY_WORK_NAME);
  }

  private void assertSetStateWorkIsQueuedHelper(String workName) throws Exception {
    List<WorkInfo> workInfos = workManager.getWorkInfosForUniqueWork(workName).get();
    // This should always be true since publishing work is unique and non-chainable
    assertTrue(workInfos.size() == 0 || workInfos.size() == 1);

    // Work info will only be present if the work was triggered.
    boolean hasStarted = (workInfos.size() == 1);
    assertTrue(hasStarted);
  }
}
