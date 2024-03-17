package com.example.ekichabi.ui.main;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekichabi.MainActivity;
import com.example.ekichabi.QueryHandler;
import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.temp.R;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Manages the underlying logic for our RecyclerView display in the Favorites tab
 */
public class BusinessListDisplayAdapter extends RecyclerView.Adapter<BusinessListDisplayAdapter.ViewHolder> implements Filterable {
    private final static String DEBUG_TAG = "BusinessListDisplayAdap";
    private List<BusinessResult> mData; // this list should only change in constructor and update methods
    private List<BusinessResult> mFilteredData; // stores filtered businesses
    private final LayoutInflater mInflater;
    private RecyclerView mRecyclerView;
    private final RecyclerViewTabFragment mParentFragment;
    private List<Map.Entry<String,String>> strActionSCombs;
    private final boolean favoritePage;


    public interface BusinessListContainerFragmentInterface {
        /**
         * Callback triggered to notify parent Fragment that filtering has been done
         * @param result The result object to send back, or null if there is no data to be sent
         */
        void sendBackResult(@Nullable Object result);
        void markAsFavorite(int businessId, boolean favorite);
        void call(String number);
        void saveToContact(BusinessResult business);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    public BusinessListDisplayAdapter(Context context, List<BusinessResult> data, RecyclerViewTabFragment parentFragment, List<Map.Entry<String,String>> strActionSCombs, boolean favoritePage) {
        this.mData = new ArrayList<>(data);
        this.mFilteredData = new ArrayList<>(data);
        this.mInflater = LayoutInflater.from(context);
        this.mParentFragment = parentFragment;
        this.strActionSCombs = strActionSCombs;
        this.favoritePage = favoritePage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.business_list_entry, parent, false);
        return new ViewHolder(view);
    }

    public Map.Entry<String,String> createEntry(String a, String b) {
        return new AbstractMap.SimpleEntry<>(a, b);
    }

    public void updateFavorite(ViewHolder holder, BusinessResult d) {
        holder.mImageView.setImageDrawable(d.isFavorite ?
                ResourcesCompat.getDrawable(this.mInflater.getContext().getResources(), android.R.drawable.btn_star_big_on, null) :
                ResourcesCompat.getDrawable(this.mInflater.getContext().getResources(), android.R.drawable.btn_star_big_off, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BusinessResult d = mFilteredData.get(position);
        holder.mBusinessNameTextView.setText(d.name);
        if (d.phone != null) {
            holder.mBusinessPhoneNumberTextView.setText(d.phone);
        } else {
            holder.mBusinessPhoneNumberTextView.setVisibility(View.GONE);
        }
        holder.mBusinessSubsectorTextView.setText(d.subcategory == null || d.subcategory.equals("") ? d.category : d.subcategory);
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean currFavoriteStatus = d.isFavorite;
                d.isFavorite = !currFavoriteStatus; // optimistic update
                mParentFragment.markAsFavorite((int) d.id, !currFavoriteStatus);

                if (d.isFavorite) {
                    AndroidLogger logger = new AndroidLogger();
                    strActionSCombs.add(createEntry(d.name, "favorite"));
                    try {
                        logger.writeBytesToFile(logger.getFavoriteAction(d.id), mParentFragment.requireContext());
                        Log.i("isFavorite()", "filter action:" + logger.toJSON(mParentFragment.requireContext(), logger.getFavoriteAction(d.id)));
//                        Log.i("isFavorite()", "content:" + logger.readFileContent(mParentFragment.requireContext()));
//                        Log.i("isFavorite()", "content:" + Arrays.toString(logger.readBytes(mParentFragment.requireContext())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    holder.popup_info.setText(d.name + " " + mParentFragment.getString(R.string.is_added_to_Favorite));
                } else {
                    AndroidLogger logger = new AndroidLogger();
                    strActionSCombs.add(createEntry(d.name, "unfavorite"));
                    try {
                        logger.writeBytesToFile(logger.getUnFavoriteAction(d.id), mParentFragment.requireContext());
                        Log.i("isFavorite()", "filter action:" + logger.toJSON(mParentFragment.requireContext(), logger.getUnFavoriteAction(d.id)));
//                        Log.i("isFavorite()", "content:" + logger.readFileContent(mParentFragment.requireContext()));
//                        Log.i("isFavorite()", "content:" + Arrays.toString(logger.readBytes(mParentFragment.requireContext())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    holder.popup_info.setText(d.name + " " + mParentFragment.getString(R.string.is_removed_from_Favorite));
                }
                if (favoritePage) {
                    updateFavorite(holder, d);
                }
//                if (favoritePage) {
//                    if (d.isFavorite) {
//                        holder.itemView.setVisibility(View.VISIBLE);
//                    } else {
//                        holder.itemView.setVisibility(View.GONE);
//                    }
//                }
                holder.popup.show();
            }
        });

        View.OnClickListener openSingleFragmentView = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SingleBusinessFragment singleBusinessFragment = SingleBusinessFragment.newInstance(d, favoritePage);
                if (view.getContext() instanceof MainActivity) {
                    AndroidLogger logger = new AndroidLogger();
                    strActionSCombs.add(createEntry(d.name, "clickFinalBussinessPage"));
                    try {
                        logger.writeBytesToFile(logger.getOpenBusinessScreenAction(d.id), mParentFragment.requireContext());
                        Log.i("SingleBusinessFragment()", "content:" + logger.toJSON(mParentFragment.requireContext(), logger.readBytes(mParentFragment.requireContext())) + ", " + Arrays.toString(logger.readBytes(mParentFragment.requireContext())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    singleBusinessFragment.show(((MainActivity) view.getContext()).getSupportFragmentManager(), "single_business_fragment");
                }
            }
        };

        updateFavorite(holder, d);

        holder.itemView.setOnClickListener(openSingleFragmentView);

        if (d.phone != null) {
            holder.mCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AndroidLogger logger = new AndroidLogger();
                    strActionSCombs.add(createEntry(d.name, "call"));
                    try {
                        logger.writeBytesToFile(logger.getCallAction(d.id), mParentFragment.requireContext());
                        Log.i("mCallButton()", "content:" + logger.toJSON(mParentFragment.requireContext(), logger.readBytes(mParentFragment.requireContext())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mParentFragment.call(d.phone);
                }
            });
        } else {
            holder.mCallButton.setVisibility(View.GONE);
        }
        holder.mSaveContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidLogger logger = new AndroidLogger();
                strActionSCombs.add(createEntry(d.name, "contact"));
                try {
                    logger.writeBytesToFile(logger.getContactAction(d.id), mParentFragment.requireContext());
                    Log.i("mSaveContactButton()", "content:" + logger.toJSON(mParentFragment.requireContext(), logger.readBytes(mParentFragment.requireContext())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mParentFragment.saveToContact(d);
            }
        });
    }

    public void updateFullData(List<BusinessResult> newData) {
        this.mData = new ArrayList<>(newData);
        this.notifyDataSetChanged();
    }

    public void updateFilteredData(List<BusinessResult> newData) {
        this.mData = new ArrayList<>(newData);
        this.mFilteredData = new ArrayList<>(newData);
        this.notifyDataSetChanged();
    }

    public void updateActionCombs() {
        this.strActionSCombs.clear();
    }

    @Override
    public int getItemCount() {
        return mFilteredData.size();
    }

    public List<Map.Entry<String,String>> getActionCombs() {
        return strActionSCombs;
    }

    // stores and recycles views as they are scrolled off screen
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mBusinessNameTextView;
        public TextView mBusinessPhoneNumberTextView;
        public TextView mBusinessSubsectorTextView;
        public ImageView mImageView;
        public ImageButton mCallButton;
        public ImageButton mSaveContactButton;
        public Dialog popup;
        public TextView popup_info;

        public ViewHolder(View itemView) {
            super(itemView);
            mBusinessNameTextView = itemView.findViewById(R.id.list_entry_business_name);
            mBusinessPhoneNumberTextView = itemView.findViewById(R.id.list_entry_business_phone_number);
            mBusinessSubsectorTextView = itemView.findViewById(R.id.list_entry_business_subsector);
            mCallButton = itemView.findViewById(R.id.list_entry_call_button);
            mSaveContactButton = itemView.findViewById(R.id.list_entry_save_contact_button);
            mImageView = itemView.findViewById(R.id.favorite_star);
            popup = new Dialog(itemView.getContext());
            popup.setContentView(R.layout.popup);
            popup_info = (TextView)popup.findViewById(R.id.popup_info);

        }
    }

    @Override
    public Filter getFilter() {
        if (this.mParentFragment instanceof BrowseTabFragment) {
            return browseFilter;
        }
        return prefixMatchFilter;
    }

    // Filter object for custom prefix search
    private Filter prefixMatchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<BusinessResult> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mData);
            } else {
                String filterPattern = constraint.toString().toLowerCase();
                for (BusinessResult item : mData) {
                    // defines substring match behavior
                    if (item.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    } else if (item.category != null && item.category.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    } else if (item.subcategory != null && item.subcategory.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredData = ((List) results.values);
            mParentFragment.sendBackResult(null); // trigger callback without sending data
            notifyDataSetChanged();
        }
    };

    // Filter object for using filter selection from the browse tab. Using a Filter object prevents
    // the screen from flickering when a user marks a business as favorite.
    private final Filter browseFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // Based on the existing filters from the parent, create a list of business data
            BrowseTabFragment browseParent = (BrowseTabFragment) mParentFragment;
            Map<MainViewModel.FILTER_TYPES, String> filterValues = browseParent.getFilterValues();
            List<BusinessResult> filteredList = new ArrayList<>(mData);
            if (filterValues.containsKey(MainViewModel.FILTER_TYPES.DISTRICT)) {
                filteredList = QueryHandler.filterByDistrict(filteredList, filterValues.get(MainViewModel.FILTER_TYPES.DISTRICT));
            }
            if (filterValues.containsKey(MainViewModel.FILTER_TYPES.VILLAGE)) {
                filteredList = QueryHandler.filterByVillage(filteredList, filterValues.get(MainViewModel.FILTER_TYPES.VILLAGE));
            }
            if (filterValues.containsKey(MainViewModel.FILTER_TYPES.SECTOR)) {
                filteredList = QueryHandler.filterBySector(filteredList, filterValues.get(MainViewModel.FILTER_TYPES.SECTOR));
            }
            if (filterValues.containsKey(MainViewModel.FILTER_TYPES.SUBSECTOR)) {
                filteredList = QueryHandler.filterBySubsector(filteredList, filterValues.get(MainViewModel.FILTER_TYPES.SUBSECTOR));
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredData = ((List) results.values);
            mParentFragment.sendBackResult(null); // trigger callback without sending data
            notifyDataSetChanged();
        }
    };
}

