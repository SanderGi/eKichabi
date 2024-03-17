package com.example.ekichabi.ui.main;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.temp.R;

import java.util.HashMap;
import java.util.List;

/**
 * Manages the underlying logic for our main RecyclerView display
 */
public class BrowseFilterDisplayAdapter extends RecyclerView.Adapter<BrowseFilterDisplayAdapter.ViewHolder> {
    private List<FilterResult> mData;
    private MainViewModel.FILTER_TYPES mType;
    private LayoutInflater mInflater;
    private RecyclerView mRecyclerView;
    private BrowseFilterFragment parentFragment;
    private Context mContext;

    // Icons for known sector ids
    public static HashMap<Long, Integer> ICON_ID_TO_RES_ID = new HashMap<Long, Integer>() {{
        put(null, R.drawable.all_icon);
        put(731492820L, R.drawable.agri_icon);
        put(-1737081259L, R.drawable.financial_icon);
        put(-1042553564L, R.drawable.labor_icon);
        put(-1505054966L, R.drawable.merchant_icon);
        put(948432703L, R.drawable.non_agri_icon);
        put(1443853438L, R.drawable.repair_icon);
        put(-1848367466L, R.drawable.trading_icon);
        put(-1238034679L, R.drawable.transportation_icon);
    }};

    public interface BrowseFragmentInterface {
        void sendBackResult(Object result);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    public BrowseFilterDisplayAdapter(Context context, List<FilterResult> data, MainViewModel.FILTER_TYPES type, BrowseFilterFragment parentFragment) {
        this.mContext = context;
        this.mData = data;
        this.mType = type;
        this.mInflater = LayoutInflater.from(context);
        this.parentFragment = parentFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.browse_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = mRecyclerView.getChildAdapterPosition(view);
                FilterResult item = mData.get(itemPosition);
//                // If it's a special case, set it to null
//                if (itemPosition == 0) {
//                    item = null;
//                }
                parentFragment.sendBackResult(item);
                parentFragment.dismiss();
            }
        });
        FilterResult d = mData.get(position);
        // Based on the type, grab the correct sort of data
        switch (mType) {
            case VILLAGE:
                holder.mTextView.setText(d.getValue());
                holder.mImageView.setVisibility(View.GONE);
                break;
            case DISTRICT:
                holder.mTextView.setText(d.getValue());
                holder.mImageView.setVisibility(View.GONE);
                break;
            case SECTOR:
                holder.mTextView.setText(d.getValue());
                Integer imageId = ICON_ID_TO_RES_ID.get(d.getId());
                Log.i("imageid", "id" + d.getId() + d.getValue());
                if (imageId == null) {
                    imageId = R.drawable.baseline_close_black_36;
                }
                holder.mImageView.setImageResource(imageId);
                break;
            case SUBSECTOR:
                holder.mTextView.setText(d.getValue());
                holder.mImageView.setVisibility(View.GONE);
            default:
                break;
        }
    }

    public void update(List<FilterResult> newData) {
        this.mData = newData;
        // Manually introduce an empty value to clear the filter
        switch (mType) {
            case DISTRICT:
                this.mData.add(0, new FilterResult(MainViewModel.FILTER_TYPES.DISTRICT, mContext.getResources().getString(R.string.clear_district_filter_entry), null));
                break;
            case VILLAGE:
                this.mData.add(0, new FilterResult(MainViewModel.FILTER_TYPES.VILLAGE, mContext.getResources().getString(R.string.clear_village_filter_entry), null));
                break;
            case SECTOR:
                this.mData.add(0, new FilterResult(MainViewModel.FILTER_TYPES.SECTOR, mContext.getResources().getString(R.string.clear_sector_filter_entry), null));
                break;
            case SUBSECTOR:
                this.mData.add(0, new FilterResult(MainViewModel.FILTER_TYPES.SUBSECTOR, mContext.getResources().getString(R.string.all_subsector), null));
                break;
            default:
                this.mData.add(0, new FilterResult(MainViewModel.FILTER_TYPES.DISTRICT, mContext.getResources().getString(R.string.clear_filter_entry), null));
                break;
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public AppCompatImageView mImageView;
        ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.browse_entry_name);
            mImageView = itemView.findViewById(R.id.browse_entry_icon);
        }
    }
}

