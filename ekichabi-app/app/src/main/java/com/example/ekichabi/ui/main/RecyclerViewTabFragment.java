package com.example.ekichabi.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.temp.R;

import java.util.List;

abstract class RecyclerViewTabFragment extends Fragment implements BusinessListDisplayAdapter.BusinessListContainerFragmentInterface {
    protected MainViewModel mViewModel;

    // Data currently being displayed
    protected RecyclerView mRecyclerView;
    protected BusinessListDisplayAdapter mBusinessListDisplayAdapter;
    protected List<BusinessResult> businessData;
    protected BusinessResult mLastSelectedBusiness;

    protected ProgressBar mLoadingInterstitial;
    protected ConstraintLayout mNoBusinessesMessage;

    protected final ActivityResultLauncher<String> requestContactPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        // Permission is granted.
                        saveBusinessAsContact(mLastSelectedBusiness);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.mRecyclerView = view.findViewById(R.id.business_list);
        this.mLoadingInterstitial = view.findViewById(R.id.loading_interstitial);
        this.mNoBusinessesMessage = view.findViewById(R.id.no_businesses_found);
    }

    protected abstract void toggleNoBusinessMessage();

    protected void toggleLoadingInterstitial() {
        if (this.mViewModel.isDataLoaded.getValue()) {
            this.mLoadingInterstitial.setVisibility(View.GONE);
            this.mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            this.mLoadingInterstitial.setVisibility(View.VISIBLE);
            this.mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void sendBackResult(Object result) {

    }

    @Override
    public void markAsFavorite(int businessId, boolean favorite) {
        this.mViewModel.markFavorite(businessId, favorite);
    }

    @Override
    public void call(String number) {
        // ACTION_DIAL is used instead of ACTION_CALL because it requires no permissions
        // and lets the user choose whether or not to make the call.
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    @Override
    public void saveToContact(BusinessResult business) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            this.saveBusinessAsContact(business);
        } else {
            // Ask for the permission
            this.mLastSelectedBusiness = business;
            this.requestContactPermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS);
        }
    }

    private void saveBusinessAsContact(BusinessResult business) {
        if (business == null) {
            return;
        }
        // Creates a new Intent to insert a contact
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        // Sets the MIME type to match the Contacts Provider
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, business.name);
        if (business.phone != null) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, business.phone);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        }
        startActivity(intent);
    }
}
