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

import com.google.android.engage.books.datamodel.EbookEntity;
import com.google.android.engage.common.datamodel.RecommendationCluster;
import com.google.common.collect.ImmutableList;
import com.google.samples.quickstart.engagesdksamples.read.converters.EbookToEntityConverter;

final class GetRecommendationClusters {

  /**
   * Constructs and returns a list of recommendation clusters for publishing. It is possible that
   * some clusters may be empty, or no clusters may be present at all. These cases must be handled
   * elsewhere, as empty clusters cannot be published and a publish request must have at least one
   * cluster.
   */
  static ImmutableList<RecommendationCluster> getRecommendationClusters() {
    return ImmutableList.of(getForYouRecommendationCluster());
  }

  private static RecommendationCluster getForYouRecommendationCluster() {
    ImmutableList<Integer> ebookIds = getForYouRecommendationEbookIds();
    RecommendationCluster.Builder clusterBuilder = new RecommendationCluster.Builder();
    clusterBuilder.setTitle("For You");
    for (int id : ebookIds) {
      EbookEntity entity = EbookToEntityConverter.convert(id);
      clusterBuilder.addEntity(entity);
    }
    return clusterBuilder.build();
  }

  private static ImmutableList<Integer> getForYouRecommendationEbookIds() {
    return ImmutableList.of(1, 2, 3);
  }

  private GetRecommendationClusters() {}
}
