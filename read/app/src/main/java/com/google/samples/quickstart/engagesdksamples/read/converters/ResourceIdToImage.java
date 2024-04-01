package com.google.samples.quickstart.engagesdksamples.read.converters;

import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.IMAGE_HEIGHT;
import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.IMAGE_WIDTH;
import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.PACKAGE_NAME;

import android.net.Uri;
import com.google.android.engage.common.datamodel.Image;

/** Converts a ResourceId to an Engage Image. */
public class ResourceIdToImage {

  public static Image convert(int imageResourceId) {
    return new Image.Builder()
        .setImageUri(getImageUriFromResourceId(imageResourceId))
        .setImageHeightInPixel(IMAGE_HEIGHT)
        .setImageWidthInPixel(IMAGE_WIDTH)
        .build();
  }

  private static Uri getImageUriFromResourceId(int resourceId) {
    return Uri.parse("android.resource://" + PACKAGE_NAME + "/" + resourceId);
  }
}