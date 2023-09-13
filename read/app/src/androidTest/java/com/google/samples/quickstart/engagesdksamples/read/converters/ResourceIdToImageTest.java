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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.TextUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.engage.common.datamodel.Image;
import com.google.samples.quickstart.engagesdksamples.read.R;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ResourceIdToImageTest {

  @Test
  public void checkImageExistsTest() {
    Image image = ResourceIdToImage.convert(R.drawable.blue_square);
    assertTrue(image.getImageHeightInPixel() > 0);
    assertTrue(image.getImageWidthInPixel() > 0);
    assertFalse(TextUtils.isEmpty(image.getImageUri().getPath()));
  }
}
