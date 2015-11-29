package io.github.froger.instamaterial.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.github.froger.instamaterial.R;

/**
 * Created by froger_mcs on 11.11.14.
 */
public class PhotoFiltersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int itemsCount = 12;

    public PhotoFiltersAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_photo_filter, parent, false);
        return new PhotoFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    }

    @Override
    public int getItemCount() {
        return itemsCount;
    }

    public static class PhotoFilterViewHolder extends RecyclerView.ViewHolder {

        public PhotoFilterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
