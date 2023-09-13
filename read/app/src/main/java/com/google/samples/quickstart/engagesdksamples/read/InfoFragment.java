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

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;

public class InfoFragment extends Fragment {

  private Ebook clickedEbook;
  private MainActivityViewModel viewModel;

  public InfoFragment() {
    super(R.layout.fragment_info);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    clickedEbook = new Ebook(getArguments().getInt(EBOOK_ID_INFO_KEY));
  }

  /**
   * Initializes the view model, sets the in view ebook in the view model, populates all fields
   * given current account info and the clicked ebook.
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Resources res = getResources();

    viewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

    viewModel.setInViewEbook(clickedEbook);

    ImageView image = view.findViewById(R.id.imageInfo);
    image.setImageResource(clickedEbook.getSquareImageResourceId());

    TextView title = view.findViewById(R.id.textInfoTitle);
    title.setText(clickedEbook.getName());

    TextView price = view.findViewById(R.id.textInfoPrice);
    price.setText(clickedEbook.getPrice());

    TextView author = view.findViewById(R.id.textInfoAuthor);
    author.setText(
        res.getString(R.string.infoAuthors, clickedEbook.getAuthors().get(0))); // Assumes 1 author

    TextView published = view.findViewById(R.id.textInfoPublished);
    published.setText(res.getString(R.string.infoPublished, clickedEbook.getPublishDate()));

    TextView pageCount = view.findViewById(R.id.textInfoPageCount);
    pageCount.setText(res.getString(R.string.infoPageCount, clickedEbook.getNumPages()));

    TextView genre = view.findViewById(R.id.textInfoGenres);
    genre.setText(
        res.getString(R.string.infoGenres, clickedEbook.getGenres().get(0))); // Assume 1 genre

    TextView series = view.findViewById(R.id.textInfoSeries);
    series.setText(res.getString(R.string.infoSeries, clickedEbook.getSeriesName()));

    TextView description = view.findViewById(R.id.textInfoDescription);
    description.setText(clickedEbook.getDescription());

    initializeSetPageButton(view);
    initializeProgressText(view, res);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    viewModel.resetInViewEbook();
  }

  /**
   * If an account is present, the select page button will open a dialog to mark a page. Otherwise,
   * the select account button should be invisible.
   *
   * @param view View from argument of onViewCreated;
   */
  private void initializeSetPageButton(View view) {
    Button selectPage = view.findViewById(R.id.buttonSelectPage);
    if (viewModel.isLoggedIn()) {
      selectPage.setOnClickListener(unused -> onSelectPage(clickedEbook));
    } else {
      selectPage.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * Initialized the progress text view. If no account is present, we indicate that the user must
   * sign in to see progress. Otherwise, we show current progress through the ebook by showing the
   * current page if the book is in progress, and a notice that the book is not in progress
   * otherwise.
   *
   * @param view View from argument of onViewCreated;
   * @param res Resources for this fragment.
   */
  private void initializeProgressText(View view, Resources res) {
    TextView progressTextView = view.findViewById(R.id.textPageProgress);
    if (!viewModel.isLoggedIn()) {
      progressTextView.setText(res.getString(R.string.infoProgressNotSignedIn));
      return;
    }
    viewModel
        .getInViewEbookPage()
        .observe(
            getViewLifecycleOwner(),
            newOptionalPage -> {
              String text;
              if (!newOptionalPage.isPresent()) {
                text = res.getString(R.string.infoProgressNotInProgress);
              } else {
                text =
                    res.getString(
                        R.string.infoProgressInProgress,
                        newOptionalPage.get(),
                        clickedEbook.getNumPages());
              }
              progressTextView.setText(text);
            });
  }

  /**
   * Opens a NumberPicker dialog allowing the user to select some page to mark from the first page
   * to the last page of the ebook. After clicking "OK", the page and engagement time is saved in
   * the view model.
   *
   * @param ebook Ebook currently in view.
   */
  private void onSelectPage(Ebook ebook) {
    NumberPicker numPicker = new NumberPicker(requireContext());

    numPicker.setMinValue(1);
    numPicker.setMaxValue(ebook.getNumPages());

    AlertDialog.Builder builder =
        new AlertDialog.Builder(requireContext(), R.style.selectPageDialog);

    Resources res = getResources();
    builder.setTitle(res.getString(R.string.infoSelectPageDialogTitle));
    builder.setView(numPicker);
    builder.setPositiveButton(
        res.getString(R.string.infoSelectPageDialogPositiveButton),
        (unused1, unused2) -> {
          int page = numPicker.getValue();
          long engagementTime = System.currentTimeMillis();
          viewModel.markPageOfInViewEbook(page, engagementTime);
        });

    AlertDialog dialog = builder.create();
    dialog.show();
  }
}
