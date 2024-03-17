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

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.ekichabi.QueryHandler;
import com.example.ekichabi.localdatabase.BusinessResult;
import com.example.ekichabi.MainActivity;

import com.example.temp.R;

import java.util.ArrayList;
import java.util.Locale;

public class Demo extends DialogFragment {
    private static final String DEBUG_TAG = "Demo";
    private Button mCloseButton;
    private LayoutInflater mInflater;
    private MainActivity mMainActivity;
    private ImageSlider imageSlider;
    private Display display;

    public Demo(MainActivity mainActivity) {
        super();
        this.mMainActivity = mainActivity;
    }

    public static Demo newInstance(MainActivity mainActivity) {
        return new Demo(mainActivity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater;
        return mInflater.inflate(R.layout.demo, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (size.x);
        params.height = (int) (size.y);
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


        this.imageSlider = view.findViewById(R.id.demoimage);
        ArrayList<SlideModel> imageList = new ArrayList<SlideModel>();
        imageList.add(new SlideModel(R.drawable.demo_detailed_info, ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.demo_click_fa, ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.demo_call,ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.demo_save,ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.claim,ScaleTypes.CENTER_CROP));
        imageSlider.setImageList(imageList);

        this.mCloseButton = view.findViewById(R.id.start_button);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
}
