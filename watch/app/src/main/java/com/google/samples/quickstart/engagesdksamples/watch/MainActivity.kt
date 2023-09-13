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
package com.google.samples.quickstart.engagesdksamples.watch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.samples.quickstart.engagesdksamples.watch.publish.Publisher
import com.google.samples.quickstart.engagesdksamples.watch.ui.home.HomeScreen
import com.google.samples.quickstart.engagesdksamples.watch.ui.theme.EngageWatchSampleApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel: MainActivityViewModel by viewModels()
    setContent {
      EngageWatchSampleApplicationTheme {
        Surface(modifier = Modifier) {
          HomeScreen(context = applicationContext, showOssLicense = { showOssLicenses() })
            .Home(viewModel)
        }
      }
    }
  }

  private fun showOssLicenses() {
    startActivity(Intent(this, OssLicensesMenuActivity::class.java))
  }

  override fun onStop() {
    super.onStop()
    Publisher.publishPeriodically(applicationContext)
  }
}
