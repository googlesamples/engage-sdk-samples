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

import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.IMAGE_HEIGHT;
import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.IMAGE_WIDTH;
import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.PACKAGE_NAME;

import android.net.Uri;
import com.google.android.engage.common.datamodel.Image;
import com.google.android.engage.common.datamodel.ImageTheme;

/** Converts a ResourceId to an Engage Image. */
public class ResourceIdToImage {

  public static Image convert(int imageResourceId) {
    return new Image.Builder()
        .setImageUri(getImageUriFromResourceId(imageResourceId))
        .setImageHeightInPixel(IMAGE_HEIGHT)
        .setImageWidthInPixel(IMAGE_WIDTH)
        .setImageTheme(ImageTheme.IMAGE_THEME_UNSPECIFIED)
        .build();
  }

  private static Uri getImageUriFromResourceId(int resourceId) {
    return Uri.parse("android.resource://" + PACKAGE_NAME + "/" + resourceId);
  }
}
