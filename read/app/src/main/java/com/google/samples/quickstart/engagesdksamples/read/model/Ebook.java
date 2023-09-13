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
package com.google.samples.quickstart.engagesdksamples.read.model;

import com.google.common.collect.ImmutableList;
import com.google.samples.quickstart.engagesdksamples.read.R;

/** Represents an Ebook. */
public final class Ebook {

  private static final int MINIMUM_NUM_PAGES = 10;

  private final int id;

  public Ebook(int id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Ebook id must be positive.");
    }
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return "Ebook_Name_" + id;
  }

  public int getSquareImageResourceId() {
    return R.drawable.blue_square;
  }

  public ImmutableList<String> getAuthors() {
    // Subject to change: May return multiple authors
    return new ImmutableList.Builder<String>().add("Author_1").build();
  }

  /**
   * In epoch milliseconds
   */
  public long getPublishDate() {
    return id;
  }

  public String getDescription() {
    return "Description_" + id;
  }

  public String getPrice() {
    return "$" + id + ".00";
  }

  public int getNumPages() {
    // Want a minimum number of pages so all books may be in progress.
    return Math.max(MINIMUM_NUM_PAGES, id);
  }

  public ImmutableList<String> getGenres() {
    return new ImmutableList.Builder<String>().add("Genre_1").build();
  }

  public String getSeriesName() {
    return "Series_" + id;
  }

  public int getSeriesUnitIndex() {
    return id;
  }

}
