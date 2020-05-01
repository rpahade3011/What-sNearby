package com.nearby.whatsnearby.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nearby.whatsnearby.R;
import com.nearby.whatsnearby.beans.SearchItemBean;

import java.util.List;

/**
 * Created by rudhraksh.pahade on 8/2/2016.
 */

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    private Context mContext;
    private List<SearchItemBean> items;
    private LayoutInflater inflater;

    public SearchResultAdapter(Context context, List<SearchItemBean> items) {
        this.mContext = context;
        this.items = items;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.auto_complete_list_item,
                parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        holder.searchResult.setText(items.get(position).getName());
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        private TextView searchResult;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            searchResult = itemView.findViewById(R.id.place_auto_complete_desc);
        }
    }
}