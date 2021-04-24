package ru.sutulaantoh86.imagePickerExample;

import androidx.appcompat.app.AppCompatActivity;
import ru.sutulaantoh86.imagePickerExample.databinding.ActivityMainBinding;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ImagePicker.PalettesChangerListener {

    static final String TAG="MainActivity";

    public ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        //устанавливаем лисенер чтобы при воборе пользователем изображение вызывалась функция onSelectedIndex
        activityMainBinding.imagePicker.setPalettesChangerListener(this);

        activityMainBinding.imagePicker.setMarginPix(30);
        activityMainBinding.imagePicker.setTextSizeSp(20);

        // { заполняем массив с изображениями
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        Bitmap bm0 = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_delete);
        bitmaps.add(bm0);
        Bitmap bm1 = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_lock_silent_mode);
        bitmaps.add(bm1);
        Bitmap bm2 = BitmapFactory.decodeResource(getResources(), android.R.drawable.btn_star_big_on);
        bitmaps.add(bm2);
        Bitmap bm3 = BitmapFactory.decodeResource(getResources(), android.R.drawable.arrow_down_float);
        bitmaps.add(bm3);
        Bitmap bm4 = BitmapFactory.decodeResource(getResources(), android.R.drawable.arrow_up_float);
        bitmaps.add(bm4);
        Bitmap bm5 = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_dialog_map);
        bitmaps.add(bm5);
        Bitmap bm6 = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_input_add);
        bitmaps.add(bm6);
        // } заполняем массив с изображениями

        // { заполняем массив с надписями
        ArrayList<String> labels = new ArrayList<>();
        for(int i=0;i<bitmaps.size();i++) labels.add(String.valueOf(i));
        // } заполняем массив с надписями


        boolean ret = activityMainBinding.imagePicker.addImages(bitmaps, labels);
        if(!ret) Log.e(TAG, "onCreate: ERROR imagePicker.addImages()" );

    }

    public void add(View view) {
        activityMainBinding.imagePicker.setCurIndex(5);

    }

    @Override
    public void onSelectedIndex(int index) {
        activityMainBinding.textView.setText(String.valueOf(index));
    }



}

