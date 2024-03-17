package com.example.ekichabi.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ekichabi.QueryHandler;
import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.ekichabi.MainActivity;

import com.example.temp.R;

import java.util.Locale;

public class HelpFragment extends DialogFragment {
    private static final String DEBUG_TAG = "SettingsFragment";
    private Button mSwahiliButton;
    private Button mEnglishButton;
    private RadioGroup mRadioGroup;
    private Button mApplyChangesButton;
    private ImageView mCloseButton;
    private LayoutInflater mInflater;
    private MainActivity mMainActivity;

    public HelpFragment(MainActivity mainActivity) {
        super();
        this.mMainActivity = mainActivity;
    }

    public static HelpFragment newInstance(MainActivity mainActivity) {
        return new HelpFragment(mainActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater;
        return mInflater.inflate(R.layout.help_fragment, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (size.x * 0.8);
        params.height = (int) (size.y * 0.8);
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


        this.mCloseButton = view.findViewById(R.id.help_close);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
