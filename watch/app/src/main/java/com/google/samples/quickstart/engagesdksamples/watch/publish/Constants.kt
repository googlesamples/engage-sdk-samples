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

/** Constants that are to be used as reference for publishing guidelines */
object Constants {
  const val MAX_PUBLISHING_ATTEMPTS: Int = 5

  const val WORKER_NAME_RECOMMENDATIONS: String = "Upload Recommendations"
  const val WORKER_NAME_CONTINUATION: String = "Upload Continuation"
  const val WORKER_NAME_FEATURED: String = "Upload Featured"

  const val PERIODIC_WORKER_NAME_RECOMMENDATIONS: String = "Periodically Upload Recommendations"
  const val PERIODIC_WORKER_NAME_CONTINUATION: String = "Periodically Upload Continuation"
  const val PERIODIC_WORKER_NAME_FEATURED: String = "Periodically Upload Featured"
  const val PERIODIC_WORKER_NAME_USER_ACCOUNT_MANAGEMENT: String =
    "Periodically Upload User Account Management"

  const val PUBLISH_TYPE: String = "PUBLISH_TYPE"
  const val PUBLISH_TYPE_RECOMMENDATIONS = "PUBLISH_RECOMMENDATIONS"
  const val PUBLISH_TYPE_CONTINUATION = "PUBLISH_CONTINUATION"
  const val PUBLISH_TYPE_FEATURED = "PUBLISH_FEATURED"
  const val PUBLISH_TYPE_USER_ACCOUNT_MANAGEMENT = "PUBLISH_USER_ACCOUNT_MANAGEMENT"
}
