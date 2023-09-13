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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.engage.books.datamodel.EbookEntity;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EbookToEntityConverterTest {

  /**
   * Tests that EbookToEntityConverter.convert preserves data from the ebook it is converting.
   */
  @Test
  public void convertTest() {
    int id = 1;
    EbookEntity entity = EbookToEntityConverter.convert(id);
    Ebook ebook = new Ebook(id);
    assertEquals(ebook.getName(), entity.getName());

    assertEquals(ebook.getAuthors().size(), entity.getAuthors().size());
    for (int i = 0; i < ebook.getAuthors().size(); i++) {
      assertEquals(ebook.getAuthors().get(i), entity.getAuthors().get(i));
    }

    assertTrue(entity.getPublishDateEpochMillis().isPresent());
    assertEquals((Long) ebook.getPublishDate(), entity.getPublishDateEpochMillis().get());

    assertTrue(entity.getDescription().isPresent());
    assertEquals(ebook.getDescription(), entity.getDescription().get());

    assertTrue(entity.getPrice().isPresent());
    assertEquals(ebook.getPrice(), entity.getPrice().get().getCurrentPrice());

    assertTrue(entity.getPageCount().isPresent());
    assertEquals((Integer) ebook.getNumPages(), entity.getPageCount().get());

    assertTrue(entity.getSeriesName().isPresent());
    assertEquals(ebook.getSeriesName(), entity.getSeriesName().get());

    assertTrue(entity.getSeriesUnitIndex().isPresent());
    assertEquals((Integer) ebook.getSeriesUnitIndex(), entity.getSeriesUnitIndex().get());

    assertEquals(1, entity.getPosterImages().size());
  }
}
