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
package com.google.samples.quickstart.engagesdksamples.read;

import static com.google.samples.quickstart.engagesdksamples.read.Constants.EBOOK_ID_INFO_KEY;
import static com.google.samples.quickstart.engagesdksamples.read.publish.SetEngageState.setAllEngageStatePeriodically;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.engage.service.Intents;
import com.google.samples.quickstart.engagesdksamples.read.databinding.ActivityMainBinding;
import com.google.samples.quickstart.engagesdksamples.read.publish.EngageServiceBroadcastReceiver;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {

  private MainActivityViewModel viewModel;
  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    registerReceiver();

    viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

    viewModel.loadAccount();

    // Load home fragment
    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .setReorderingAllowed(true)
          .add(R.id.fragmentContainerView, HomeFragment.class, /* args= */ null)
          .commit();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    viewModel.saveAccount();
    setAllEngageStatePeriodically(getApplicationContext());
  }

  void loadInfoFragment(int clickedEbookId) {
    Bundle bundle = new Bundle();
    bundle.putInt(EBOOK_ID_INFO_KEY, clickedEbookId);
    InfoFragment infoFragment = new InfoFragment();
    infoFragment.setArguments(bundle);
    getSupportFragmentManager()
        .beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fragmentContainerView, infoFragment)
        .addToBackStack(/* name= */ null)
        .commit();
  }

  private void registerReceiver() {
    BroadcastReceiver publishReceiver = new EngageServiceBroadcastReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intents.ACTION_PUBLISH_RECOMMENDATION);
    filter.addAction(Intents.ACTION_PUBLISH_FEATURED);
    filter.addAction(Intents.ACTION_PUBLISH_CONTINUATION);
    int flags = ContextCompat.RECEIVER_EXPORTED;
    ContextCompat.registerReceiver(getApplicationContext(), publishReceiver, filter, flags);
  }
}
