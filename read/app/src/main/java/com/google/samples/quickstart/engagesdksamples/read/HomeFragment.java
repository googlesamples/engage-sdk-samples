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

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

/**
 * Represents the home screen containing a sign-in button, a delete account button, and a grid of
 * ebooks, which when tapped, open up an info page containing info about the ebook.
 */
public class HomeFragment extends Fragment {

  private RecyclerView recyclerView;
  private MainActivityViewModel viewModel;

  public HomeFragment() {
    super(R.layout.fragment_home);
  }

  @Override
  @MainThread
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    viewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

    recyclerView = view.findViewById(R.id.recyclerViewEbooks);
    recyclerView.setAdapter(new EbookListAdapter(this));
    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), /* spanCount= */ 3));

    Button signInButton = view.findViewById(R.id.buttonSignIn);
    signInButton.setOnClickListener(unused -> signIn());

    Button deleteAccountButton = view.findViewById(R.id.buttonDeleteAccount);
    deleteAccountButton.setOnClickListener(unused -> deleteAccount());

    Button showLicenseButton = view.findViewById(R.id.buttonShowLicense);
    showLicenseButton.setOnClickListener(
        unused -> startActivity(new Intent(requireActivity(), OssLicensesMenuActivity.class)));
  }

  public void loadInfoFragment(int clickedEbookId) {
    if (requireActivity() instanceof MainActivity) {
      MainActivity mainActivity = (MainActivity) requireActivity();
      mainActivity.loadInfoFragment(clickedEbookId);
    }
  }

  private void signIn() {
    Resources res = getResources();
    String toastText;
    if (viewModel.isLoggedIn()) {
      toastText = res.getString(R.string.signInAlreadySignedIn);
    } else {
      toastText = res.getString(R.string.signInNotSignedIn);
    }
    Toast.makeText(requireActivity(), toastText, Toast.LENGTH_SHORT).show();
    viewModel.logIn();
  }

  private void deleteAccount() {
    Resources res = getResources();
    String toastText;
    if (viewModel.isLoggedIn()) {
      toastText = res.getString(R.string.deleteAccountSignedIn);
    } else {
      toastText = res.getString(R.string.deleteAccountNotSignedIn);
    }
    Toast.makeText(requireActivity(), toastText, Toast.LENGTH_SHORT).show();
    viewModel.deleteAccount();
  }
}
