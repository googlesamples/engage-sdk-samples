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

import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setContinuationCluster;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setFeaturedCluster;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setRecommendationClusters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.engage.service.Intents;

public class EngageServiceBroadcastReceiver extends BroadcastReceiver {

  private static final String TAG = EngageServiceBroadcastReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "onReceive: Broadcast received. Intent is " + intent);
    switch (intent.getAction()) {
      case (Intents.ACTION_PUBLISH_RECOMMENDATION):
        setRecommendationClusters(context);
        break;
      case (Intents.ACTION_PUBLISH_FEATURED):
        setFeaturedCluster(context);
        break;
      case (Intents.ACTION_PUBLISH_CONTINUATION):
        setContinuationCluster(context);
        break;
      default:
        Log.e(TAG, "onReceive: Received unrecognized intent: " + intent);
    }
  }
}
