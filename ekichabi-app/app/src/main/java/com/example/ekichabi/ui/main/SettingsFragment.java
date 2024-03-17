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

public class SettingsFragment extends DialogFragment {
    private static final String DEBUG_TAG = "SettingsFragment";
    private Button mSwahiliButton;
    private Button mEnglishButton;
    private RadioGroup mRadioGroup;
    private Button mApplyChangesButton;
    private ImageView mCloseButton;
    private LayoutInflater mInflater;
    private MainActivity mMainActivity;

    public SettingsFragment(MainActivity mainActivity) {
        super();
        this.mMainActivity = mainActivity;
    }

    private void setLanguagePreference(String langCode) {
        SharedPreferences ensharedPreferences = getContext().getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        SharedPreferences.Editor eneditor = ensharedPreferences.edit();
        eneditor.putString("language", langCode);
        eneditor.apply();
        Locale myLocale = new Locale(langCode);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public void setTextSizePreference(int resId) {
        Log.i("settings", "set text size " + resId);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("selectedTextSize", Context.MODE_PRIVATE);
        SharedPreferences.Editor eneditor = sharedPreferences.edit();
        eneditor.putInt("size", resId);
        eneditor.apply();
    }


    public static SettingsFragment newInstance(MainActivity mainActivity) {
        return new SettingsFragment(mainActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater;
        return mInflater.inflate(R.layout.settings_fragment, container);
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

        this.mRadioGroup = (RadioGroup) view.findViewById(R.id.switch_font_size_buttons);
        this.mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was clicked
                switch(checkedId) {
                    case R.id.small:
                        Log.i("settings", "small text button pressed");
                        setTextSizePreference(TextSize.Small.resId);
                        break;
                    case R.id.medium:
                        Log.i("settings", "medium text button pressed");
                        setTextSizePreference(TextSize.Medium.resId);
                        break;
                    case R.id.large:
                        Log.i("settings", "large text button pressed");
                        setTextSizePreference(TextSize.Large.resId);
                        break;
                    case R.id.extra_large:
                        Log.i("settings", "extra large text button pressed");
                        setTextSizePreference(TextSize.XLarge.resId);
                        break;
                }
            }
        });

        this.mSwahiliButton = view.findViewById(R.id.language_swahili);
        this.mSwahiliButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("settings", "swahili button pressed");
                setLanguagePreference("sw");
                //mSwahiliButton.setBackgroundColor(Color.parseColor("#6200EE"));
                mSwahiliButton.setBackgroundColor(Color.TRANSPARENT);
                mEnglishButton.setBackgroundColor(Color.parseColor("#757575"));
            }
        });

        this.mEnglishButton = view.findViewById(R.id.language_english);
        this.mEnglishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("settings", "english button pressed");
                setLanguagePreference("en");
                mEnglishButton.setBackgroundColor(Color.TRANSPARENT);
                mSwahiliButton.setBackgroundColor(Color.parseColor("#757575"));
            }
        });

        this.mApplyChangesButton = view.findViewById(R.id.apply_settings_button);
        this.mApplyChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("settings", "apply changes pressed");
                Log.i("settings", "apply text size " + getContext().getSharedPreferences("selectedTextSize", Context.MODE_PRIVATE).getInt("size", TextSize.Large.resId));
                mMainActivity.refreshApp();
            }
        });

        // Indicate current text size in the UI
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("selectedTextSize", Context.MODE_PRIVATE);
        int textSize = sharedPreferences.getInt("size", TextSize.Large.resId);
        Log.i("settings", "text size: " + textSize);
        RadioButton b = null;
        switch(textSize) {
            // TODO: Find a better alternative to hardcoding the text sizes here
            case 2131820820: // small
                b = (RadioButton) this.mRadioGroup.findViewById(R.id.small);
                b.setChecked(true);
                break;
            case 2131820819: // medium
                b = (RadioButton) this.mRadioGroup.findViewById(R.id.medium);
                b.setChecked(true);
                break;
            case 2131820821: // x-large
                b = (RadioButton) this.mRadioGroup.findViewById(R.id.extra_large);
                b.setChecked(true);
                break;
            case 2131820818: // large
                b = (RadioButton) this.mRadioGroup.findViewById(R.id.large);
                b.setChecked(true);
                break;
        }

        // Indicate current language in the UI
        sharedPreferences = getContext().getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "sw");
        Log.i("settings", "language: " + language);
        switch(language) {
            case "en":
                mEnglishButton.setBackgroundColor(Color.TRANSPARENT);
                mSwahiliButton.setBackgroundColor(Color.parseColor("#757575"));
                break;
            default:
                mSwahiliButton.setBackgroundColor(Color.TRANSPARENT);
                mEnglishButton.setBackgroundColor(Color.parseColor("#757575"));
        }

        this.mCloseButton = view.findViewById(R.id.settings_close);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
