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
package com.google.samples.quickstart.engagesdksamples.read.converters;

import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.ENGAGE_SDK_DOCS_URL;

import android.net.Uri;
import com.google.android.engage.books.datamodel.EbookEntity;
import com.google.android.engage.common.datamodel.Price;
import com.google.common.base.Optional;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;

/** Converts an Ebook with a given ID to an EbookEntity. */
public final class EbookToEntityConverter {

  /**
   * Converts data from an Ebook with id ebookId into an EbookEntity with the same data and returns
   * it. Does not set last engagement time or progress percentage complete.
   */
  public static EbookEntity convert(int ebookId) {
    return convert(ebookId, Optional.absent(), Optional.absent());
  }

  /**
   * Converts data from an Ebook with id ebookId into an EbookEntity with the same data and returns
   * it. Sets the Ebook Entity's last engagement time to lastEngagementTime if present and progress
   * percentage complete to progressPercentageComplete if present.
   */
  public static EbookEntity convert(int ebookId, Optional<Long> lastEngagementTime,
      Optional<Integer> progressPercentageComplete) {

    Ebook ebook = new Ebook(ebookId);
    EbookEntity.Builder entityBuilder = new EbookEntity.Builder();
    entityBuilder
        .setName(ebook.getName())
        .addAuthors(ebook.getAuthors())
        .setActionLinkUri(Uri.parse(ENGAGE_SDK_DOCS_URL))
        .addPosterImage(ResourceIdToImage.convert(ebook.getSquareImageResourceId()))
        .setPublishDateEpochMillis(ebook.getPublishDate())
        .setDescription(ebook.getDescription())
        .setPrice(getPrice(ebook.getPrice()))
        .setPageCount(ebook.getNumPages())
        .addGenres(ebook.getGenres())
        .setSeriesName(ebook.getSeriesName())
        .setSeriesUnitIndex(ebook.getSeriesUnitIndex());
    if (lastEngagementTime.isPresent()) {
      entityBuilder.setLastEngagementTimeMillis(lastEngagementTime.get());
    }
    if (progressPercentageComplete.isPresent()) {
      entityBuilder.setProgressPercentComplete(progressPercentageComplete.get());
    }
    return entityBuilder.build();
  }

  private static Price getPrice(String priceString) {
    return new Price.Builder().setCurrentPrice(priceString).build();
  }

  private EbookToEntityConverter() {}
}