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
import com.google.android.engage.common.datamodel.FeaturedCluster;
import com.google.common.collect.ImmutableList;
import com.google.samples.quickstart.engagesdksamples.read.converters.EbookToEntityConverter;

final class GetFeaturedCluster {

  /**
   * Constructs and returns the possibly empty featured cluster for publishing. Handling an empty
   * returned cluster must be handled elsewhere as empty clusters cannot be published.
   */
  static FeaturedCluster getFeaturedCluster() {
    ImmutableList<Integer> ebookIds = getFeaturedEbookIds();
    FeaturedCluster.Builder clusterBuilder = new FeaturedCluster.Builder();
    for (int id : ebookIds) {
      EbookEntity entity = EbookToEntityConverter.convert(id);
      clusterBuilder.addEntity(entity);
    }
    return clusterBuilder.build();
  }

  private static ImmutableList<Integer> getFeaturedEbookIds() {
    return ImmutableList.of(1, 2, 3);
  }

  private GetFeaturedCluster() {}
}
