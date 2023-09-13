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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.engage.service.Intents.ACTION_PUBLISH_CONTINUATION
import com.google.android.engage.service.Intents.ACTION_PUBLISH_FEATURED
import com.google.android.engage.service.Intents.ACTION_PUBLISH_RECOMMENDATION
import com.google.samples.quickstart.engagesdksamples.watch.publish.Publisher.publishContinuationClusters
import com.google.samples.quickstart.engagesdksamples.watch.publish.Publisher.publishFeaturedClusters
import com.google.samples.quickstart.engagesdksamples.watch.publish.Publisher.publishRecommendationClusters

/** Broadcast Receiver to trigger a one time publish of clusters. */
class EngageServiceBroadcastReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent == null || context == null) {
      return
    }
    when (intent.action) {
      ACTION_PUBLISH_RECOMMENDATION -> publishRecommendationClusters(context)
      ACTION_PUBLISH_FEATURED -> publishFeaturedClusters(context)
      ACTION_PUBLISH_CONTINUATION -> publishContinuationClusters(context)
      else -> Log.e(TAG, "onReceive: Received unrecognized intent: $intent")
    }
  }

  private companion object {
    const val TAG = "EngageBroadcastReceiver"
  }
}
