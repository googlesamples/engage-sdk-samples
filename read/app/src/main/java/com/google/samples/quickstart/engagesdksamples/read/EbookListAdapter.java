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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.samples.quickstart.engagesdksamples.read.model.Ebook;

public class EbookListAdapter extends RecyclerView.Adapter<EbookListAdapter.ViewHolder> {

  private static int MAX_EBOOKS_TO_DISPLAY = 999;

  private final HomeFragment homeFragment;

  EbookListAdapter(HomeFragment homeFragment) {
    this.homeFragment = homeFragment;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.ebook_item_card, parent, /* attachToRoot= */ false);
    return new ViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ConstraintLayout itemCard = holder.getEbookItemCard();
    Ebook ebook = new Ebook(position + 1); // Cannot have an id of 0.

    itemCard.setOnClickListener(
        view -> {
          homeFragment.loadInfoFragment(ebook.getId());
        });

    ImageView thumbnail = itemCard.findViewById(R.id.imageThumbnail);
    thumbnail.setImageResource(ebook.getSquareImageResourceId());

    TextView title = itemCard.findViewById(R.id.textTitle);
    title.setText(ebook.getName());

    TextView author = itemCard.findViewById(R.id.textAuthor);
    author.setText(ebook.getAuthors().get(0));

    TextView price = itemCard.findViewById(R.id.textPrice);
    price.setText(ebook.getPrice());
  }

  @Override
  public int getItemCount() {
    return MAX_EBOOKS_TO_DISPLAY;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    private final ConstraintLayout ebookItemCard;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);

      ebookItemCard = itemView.findViewById(R.id.constraintEbookItemCard);
    }

    public ConstraintLayout getEbookItemCard() {
      return ebookItemCard;
    }
  }
}
