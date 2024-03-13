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

import androidx.annotation.NonNull;
import com.google.android.engage.books.datamodel.EbookEntity;
import com.google.android.engage.common.datamodel.AccountProfile;
import com.google.android.engage.common.datamodel.ContinuationCluster;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.samples.quickstart.engagesdksamples.read.converters.EbookToEntityConverter;
import com.google.samples.quickstart.engagesdksamples.read.login.Account;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;

final class GetContinuationCluster {

  /** Maximum amount of entities allowed in a continuation cluster */
  private static final int MAX_ENTITIES = 10;

  /**
   * Constructs and returns the continuation cluster with the given account information. Returns a
   * cluster with at most MAX_ENTITIES of the account's in progress Ebooks.
   */
  static ContinuationCluster getContinuationCluster(@NonNull Account account) {
    ImmutableList<Integer> ebookIds = getContinuationEbookIds(account);
    ContinuationCluster.Builder clusterBuilder = new ContinuationCluster.Builder();
    for (int id : ebookIds) {
      // A continuation cluster expects the following values to show progress through an ebook to
      // the user.
      // Only previously read books will be in the continuation cluster, so these are safe calls.
      Integer progressPercentComplete =
          account.getCurrentPageOfInProgressEbook(id) * 100 / (new Ebook(id).getNumPages());
      // For Engage, only entities with less than 100% completion may be in the continuation cluster
      if (progressPercentComplete < 100) {
        Long lastEngagementTimeMillis = account.getLastEngagementTimeOfInProgressEbook(id);
        EbookEntity entity =
            EbookToEntityConverter.convert(
                id, Optional.of(lastEngagementTimeMillis), Optional.of(progressPercentComplete));
        clusterBuilder.addEntity(entity);
      }
    }
    clusterBuilder.setUserConsentToSyncAcrossDevices(true);
    clusterBuilder.setAccountProfile(
            new AccountProfile(Constants.ACCOUNT_ID, Constants.PROFILE_ID));
    return clusterBuilder.build();
  }

  private static ImmutableList<Integer> getContinuationEbookIds(@NonNull Account account) {
    ImmutableList.Builder<Integer> ebookIdsBuilder = new ImmutableList.Builder<>();

    int numAdded = 0;
    for (int ebookId : account.getInProgressEbookIds()) {
      if (numAdded >= MAX_ENTITIES) {
        break;
      }
      ebookIdsBuilder.add(ebookId);
      numAdded++;
    }

    return ebookIdsBuilder.build();
  }

  private GetContinuationCluster() {}
}
