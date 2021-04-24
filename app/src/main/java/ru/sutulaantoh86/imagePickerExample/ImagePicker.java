package ru.sutulaantoh86.imagePickerExample;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

// Горизонтальное колесо. Наподобие Numberpicker только для выбора изображений, над каждым изображении так же пишется индекс
public class ImagePicker extends RecyclerView {

    // { интерфейс для того чтобы сообщать о том что был выбран новый индекс изображения
    public interface PalettesChangerListener {
        void onSelectedIndex(int index);
    }
    PalettesChangerListener palettesChangerListener=null;
    public void setPalettesChangerListener(PalettesChangerListener palettesChangerListener) {
        this.palettesChangerListener = palettesChangerListener;
    }
    // } интерфейс для того чтобы сообщать о том что был выбран новый индекс изображения


    private final List<Item> items= new ArrayList<>();
    private final ItemAdapter adapter = new ItemAdapter(this.items);

    LinearLayoutManager layoutManager;

    RecyclerView.SmoothScroller smoothScroller;// чтобы после скролинга центральный элемент плавно смещался к центру, а не резко
    // а также чтобы выбранный индекс был именно по центру, а не слева

    int marginPix=20;
    int textSizeSp=15;
    int curIndex=0;

    public ImagePicker(@NonNull Context context) {
        this(context,null);
    }
    public ImagePicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }
    public ImagePicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);

        setLayoutManager(layoutManager);
        setAdapter(adapter);

        smoothScroller = new CenterSmoothScroller(getContext());// чтобы после скролинга центральный элемент плавно смещался к центру, а не резко
        // а также чтобы выбранный индекс был именно по центру, а не слева

        // { после того как пользователь проскролил изображения, скрол будет продолжатся еще некоторое время по инерции
        // и после того как скролинг остановится нужно нужно найти тот элемент который ближе к центру нашего RecyclerView
        // и поставить его точно по центру, а также сообщить пользователю класса, что выбран новый индекс
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState==RecyclerView.SCROLL_STATE_IDLE)
                {
                    int firstPos = layoutManager.findFirstVisibleItemPosition();
                    if(firstPos==0)firstPos=1;
                    int lastPos  = layoutManager.findLastVisibleItemPosition();
                    if(lastPos>=layoutManager.getItemCount()-1)lastPos=layoutManager.getItemCount()-2;


                    int centerRecyclerView = getWidth() / 2;
                    int minDelta=Integer.MAX_VALUE;
                    int middle = 0;


                    for (int i = firstPos; i <= lastPos; i++) {
                        View view = layoutManager.findViewByPosition(i);
                        if (view != null) {
                            int positionCenterView = view.getLeft() + (view.getRight() - view.getLeft()) / 2;
                            int delta = Math.abs(centerRecyclerView - positionCenterView);
                            if (delta < minDelta) {
                                minDelta = delta;
                                middle = i;
                            }
                        }
                    }

                    setCurIndex(middle-1);
                }
            }

        });
        // } после того как пользователь проскролил изображения, скрол будет продолжатся еще некоторое время по инерции
        // и после того как скролинг остановится нужно нужно найти тот элемент который ближе к центру нашего RecyclerView
        // и поставить его точно по центру, а также сообщить пользователю класса, что выбран новый индекс


        // { это нужно чтобы получить размеры вьюхи когда вьюха отрисуется и можно будет получить ее размер
        ViewTreeObserver vto = this.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                ImagePicker.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                adapter.setPalettesChangerWidth(ImagePicker.this.getWidth());
                adapter.setPalettesChangerHeight(ImagePicker.this.getHeight());

                adapter.notifyDataSetChanged();

                setCurIndex(curIndex); // это чтобы при первом появлении на экран было точное положение вьюх, иначе пока пользователь не дотронется
                // до нашего элемента, он находится между 0 и первым элементом
            }
        });
        // } это нужно чтобы получить размеры вьюхи когда вьюха отрисуется и можно будет получить ее размер

    }

    // { самая главная функция. Принимает изображения которые будут показаны пользователю и подписи к ним
    boolean addImages(ArrayList<Bitmap> images, ArrayList<String> labels)
    {
        if(images.size()!=labels.size()) {
            Log.e("ImagePicker", "ERROR addImages: images.size()!=labels.size()");
            return false;
        }

        items.clear();
        items.add(new Item(null,null));
        for(int i=0;i<images.size();i++)
        {
            items.add(new Item(images.get(i),labels.get(i)));
        }
        items.add(new Item(null,null));

        if(adapter!=null) adapter.notifyDataSetChanged();

        return true;
    }
    // } самая главная функция. Принимает изображения которые будут показаны пользователю и подписи к ним


    public int getCurIndex() {
        return curIndex;
    }

    public void setCurIndex(int curIndex) {
        this.curIndex = curIndex;

        smoothScroller.setTargetPosition(curIndex+1);
        layoutManager.startSmoothScroll(smoothScroller);

        palettesChangerListener.onSelectedIndex(curIndex);
    }
    public int getMarginPix() {
        return marginPix;
    }

    // { устанавливает расстояние между изображениями. Задается в пикселях. Фактически расстояния будет в 2 раза больше.
    public void setMarginPix(int marginPix) {
        this.marginPix = marginPix;
        adapter.notifyDataSetChanged();
    }
    // } устанавливает расстояние между изображениями. Задается в пикселях. Фактически расстояния будет в 2 раза больше.

    public int getTextSizeSp() {
        return textSizeSp;
    }

    // { устанавливает размер текста для подписей. Здается в sp
    public void setTextSizeSp(int textSizeSp) {
        this.textSizeSp = textSizeSp;
        adapter.notifyDataSetChanged();
    }
    // } устанавливает размер текста для подписей. Здается в sp


    public final class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private final List<Item> items;

        public void setPalettesChangerWidth(int palettesChangerWidth) {
            this.palettesChangerWidth = palettesChangerWidth;
        }
        public void setPalettesChangerHeight(int palettesChangerHeight) {      this.palettesChangerHeight = palettesChangerHeight;    }

        int palettesChangerWidth=0;
        int palettesChangerHeight=0;

        public ItemAdapter(List<Item> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            // { создаем слой с с меткой и изображением
            LinearLayout itemRootLayout = new LinearLayout(parent.getContext());
            itemRootLayout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(parent.getContext());
            LinearLayout.LayoutParams lp_textView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); // Verbose!

            ImageView imageView = new ImageView(parent.getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); // Verbose!
            lp.weight = 1.0f;

            itemRootLayout.addView(textView,lp_textView);
            itemRootLayout.addView(imageView, lp);
            // } создаем слой с с меткой и изображением

            return new RecyclerView.ViewHolder(itemRootLayout){};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            LinearLayout itemRootLayout =(LinearLayout) holder.itemView;// получаем корневой Layout
            itemRootLayout.setGravity(Gravity.CENTER);

            Bitmap bm = this.items.get(position).getImg();// получаем Bitmap который будет отрисован

            TextView textView = (TextView) itemRootLayout.getChildAt(0); //в Layout сначала идет TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, ImagePicker.this.getTextSizeSp()  );
            if(bm!=null) textView.setText(this.items.get(position).getStr());
            else textView.setText("");//если нет изображения то и номер не надо показывать

            ImageView imageView = (ImageView) itemRootLayout.getChildAt(1);//в Layout изображение добавлено после TextView
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            lp.setMargins(ImagePicker.this.getMarginPix(), 0, ImagePicker.this.getMarginPix(), 0);// это чтобы между изображениями было небольшое расстояние. Задается пользователем в setMarginPix

            imageView.setImageBitmap(bm);
            imageView.setMinimumWidth((int) (palettesChangerWidth * 0.5f));// устанавливаем ширину нашего изображения (и всего item) равной половине от всего RecyclerView
            imageView.setMinimumHeight(palettesChangerHeight);

        }

        @Override
        public int getItemCount() {
            return this.items.size();
        }
    }

    // { чтобы после скролинга центральный элемент плавно смещался к центру, а не резко
    // а также чтобы выбранный индекс был именно по центру, а не слева
    static class CenterSmoothScroller extends LinearSmoothScroller {
        private static final float MILLISECONDS_PER_INCH = 170f;//скорость с которой будет подползать к нужному айтему

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        protected void onStop() {
            super.onStop();
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
        }
    }
    // } чтобы после скролинга центральный элемент плавно смещался к центру, а не резко
    // а также чтобы выбранный индекс был именно по центру, а не слева


    static class Item {
        public Item(Bitmap img , String str){
            this.img =img;
            this.str =str;
        }

        public Bitmap getImg() {
            return img;
        }

        public void setImg(Bitmap img) {
            this.img = img;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        private Bitmap img;
        private String str;
    }
}
