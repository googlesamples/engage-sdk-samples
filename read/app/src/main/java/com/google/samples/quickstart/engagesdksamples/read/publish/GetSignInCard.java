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

import static com.google.samples.quickstart.engagesdksamples.read.converters.Constants.ENGAGE_SDK_DOCS_URL;

import android.net.Uri;
import com.google.android.engage.common.datamodel.SignInCardEntity;
import com.google.samples.quickstart.engagesdksamples.read.R;
import com.google.samples.quickstart.engagesdksamples.read.converters.ResourceIdToImage;

public class GetSignInCard {

  private static final SignInCardEntity SIGN_IN_CARD_ENTITY =
      new SignInCardEntity.Builder()
          .addPosterImage(ResourceIdToImage.convert(R.drawable.blue_square))
          .setActionText("Sign In")
          .setActionUri(Uri.parse(ENGAGE_SDK_DOCS_URL))
          .build();

  static SignInCardEntity getSignInCard() {
    return SIGN_IN_CARD_ENTITY;
  }
}
