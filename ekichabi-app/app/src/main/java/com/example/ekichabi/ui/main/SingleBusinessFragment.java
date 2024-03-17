package com.example.ekichabi.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.temp.R;

import java.io.IOException;
import java.util.Objects;

public class SingleBusinessFragment extends DialogFragment {
    private final String DEBUG_TAG = "SingleBusinessFragment";
    private MainViewModel mViewModel;
    private final BusinessResult businessData;
    private Button mFavoriteButton;
    private ImageView mCloseButton;
    private LayoutInflater mInflater;
    private final boolean favoritePage;

    private final ActivityResultLauncher<String> requestContactPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        // Permission is granted.
                        saveToContact();
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(getResources().getString(R.string.save_contact_permission_denied))
                                .setCancelable(true)
                                .setPositiveButton("Close", null);
                        builder.create().show();
                    }
                }
            });


    public static SingleBusinessFragment newInstance(BusinessResult businessData, Boolean favoritePage) {
        return new SingleBusinessFragment(businessData, favoritePage);
    }

    public SingleBusinessFragment(BusinessResult businessData, boolean favoritePage) {
        super();
        this.favoritePage = favoritePage;
        this.businessData = businessData;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater;
        return mInflater.inflate(R.layout.single_business_fragment, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender == mViewModel && propertyId == (int) businessData.id) {
                    mFavoriteButton.setText(businessData.isFavorite ?
                            mInflater.getContext().getResources().getString(R.string.remove_favorite_button) :
                            mInflater.getContext().getResources().getString(R.string.favorite_button));
                    mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(null,
                            businessData.isFavorite ?
                                    ResourcesCompat.getDrawable(mInflater.getContext().getResources(), android.R.drawable.btn_star_big_on, null) :
                                    ResourcesCompat.getDrawable(mInflater.getContext().getResources(), android.R.drawable.btn_star_big_off, null),
                            null, null);
                }
            }
        };
        this.mViewModel.addOnPropertyChangedCallback(callback);

        ((TextView) view.findViewById(R.id.single_business_name)).setText(this.businessData.name);
        // Get the ID for the resource via database
        this.mViewModel.getIdFromName(businessData.category).observe(this.getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                Integer iconInteger = BrowseFilterDisplayAdapter.ICON_ID_TO_RES_ID.get(aLong);
                if (iconInteger != null) {
                    ((ImageView) view.findViewById(R.id.single_business_sector_icon)).setImageDrawable(
                            ResourcesCompat.getDrawable(SingleBusinessFragment.this.getResources(), iconInteger, null));
                }
            }
        });
        ((TextView) view.findViewById(R.id.single_business_ower)).setText(this.businessData.owner);
        ((TextView) view.findViewById(R.id.single_business_location)).setText(getBusinessLocation());
        ((TextView) view.findViewById(R.id.single_business_sector)).setText(this.businessData.category);
        ((TextView) view.findViewById(R.id.single_business_subsector)).setText(this.businessData.subcategory == null
                || this.businessData.subcategory.equals("") ? "—" : this.businessData.subcategory);
        TextView phoneNumberText = view.findViewById(R.id.single_business_phone_number);
        Button callButton = view.findViewById(R.id.single_business_call_button);
        if (this.businessData.phone != null) {
            phoneNumberText.setText(this.businessData.phone);
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ACTION_DIAL is used instead of ACTION_CALL because it requires no permissions
                    // and lets the user choose whether or not to make the call.
                    AndroidLogger logger = new AndroidLogger();
                    try {

                        logger.writeBytesToFile(logger.getCallAction(businessData.id), requireContext());
                        Log.i("call()", "filter action:" + logger.toJSON(requireContext(), logger.getCallAction(businessData.id)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    callIntent.setData(Uri.parse("tel:" + businessData.phone));
                    requireActivity().startActivity(callIntent);
                }
            });
        } else {
            phoneNumberText.setText(getResources().getText(R.string.no_phone_number));
            callButton.setVisibility(View.GONE);
        }

        this.mFavoriteButton = view.findViewById(R.id.single_business_favorite_button);
        this.mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean currFavoriteStatus = businessData.isFavorite;
                Log.i(DEBUG_TAG, "Current fav status: " + currFavoriteStatus);
                businessData.isFavorite = !currFavoriteStatus; // optimistic update
                AndroidLogger logger = new AndroidLogger();
                try {
                    if (businessData.isFavorite) {
                        logger.writeBytesToFile(logger.getFavoriteAction(businessData.id), requireContext());
                    } else {
                        logger.writeBytesToFile(logger.getUnFavoriteAction(businessData.id), requireContext());
                    }
                    Log.i("favorite/unfavorite()", "filter action:" + logger.toJSON(requireContext(), logger.getFavoriteAction(businessData.id)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mFavoriteButton.setText(businessData.isFavorite ?
                        mInflater.getContext().getResources().getString(R.string.remove_favorite_button) :
                        mInflater.getContext().getResources().getString(R.string.favorite_button));
                mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(null,
                        businessData.isFavorite ?
                                ResourcesCompat.getDrawable(mInflater.getContext().getResources(), android.R.drawable.btn_star_big_on, null) :
                                ResourcesCompat.getDrawable(mInflater.getContext().getResources(), android.R.drawable.btn_star_big_off, null),
                        null, null);
                mViewModel.markFavorite((int) businessData.id, !currFavoriteStatus);
            }
        });
        this.mFavoriteButton.setText(this.businessData.isFavorite ?
                getContext().getResources().getString(R.string.remove_favorite_button) :
                getContext().getResources().getString(R.string.favorite_button));
        this.mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(null, this.businessData.isFavorite ?
                        ResourcesCompat.getDrawable(getContext().getResources(), android.R.drawable.btn_star_big_on, null) :
                        ResourcesCompat.getDrawable(getContext().getResources(), android.R.drawable.btn_star_big_off, null),
                null, null);

        ((Button) view.findViewById(R.id.single_business_save_contact_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroidLogger logger = new AndroidLogger();
                try {
                    logger.writeBytesToFile(logger.getContactAction(businessData.id), requireContext());
                    Log.i("addContact()", "filter action:" + logger.toJSON(requireContext(), logger.getFavoriteAction(businessData.id)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // Permission already granted
                    saveToContact();
                } else {
                    // Ask for the permission
                    requestContactPermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS);
                }
            }
        });
        this.mViewModel.removeOnPropertyChangedCallback(callback);
        this.mCloseButton = view.findViewById(R.id.business_close);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void saveToContact() {
        // Creates a new Intent to insert a contact
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        // Sets the MIME type to match the Contacts Provider
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, this.businessData.name);
        if (this.businessData.phone != null) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, this.businessData.phone);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        }
        startActivity(intent);
    }

    private String getBusinessLocation() {
        String village = this.businessData.village;
        if (this.businessData.subvillage != null) {
            return village + " - " + this.businessData.subvillage;
        }
        return village;
    }
}
