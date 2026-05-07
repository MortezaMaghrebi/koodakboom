package com.codestoon.koodakboom;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class AssetLoader {

    private Context context;

    public AssetLoader(Context context) {
        this.context = context;
    }

    // تابع بارگذاری تصویر از assets
    public void loadImageFromAssets(ImageView imageView, String imageName) {
        try {
            // باز کردن فایل تصویر از assets
            InputStream inputStream = context.getAssets().open(imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // در صورت خطا، تصویر پیش‌فرض را نمایش بده
            imageView.setImageResource(R.drawable.sample_thumbnail);
        }
    }

    // تابع خواندن متن از فایل txt در assets
    public String[] readTextFromAssets(String fileName) {
        String[] result = new String[2]; // index 0: title, index 1: description
        StringBuilder title = new StringBuilder();
        StringBuilder description = new StringBuilder();

        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 2) {
                if (lineCount == 0) {
                    title.append(line);
                } else if (lineCount == 1) {
                    description.append(line);
                }
                lineCount++;
            }

            result[0] = title.toString();
            result[1] = description.toString();

            reader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            result[0] = "عنوان پیش‌فرض";
            result[1] = "توضیحات پیش‌فرض";
        }

        return result;
    }

    // تابع یکپارچه برای بارگذاری تمام اطلاعات
    public void loadAllContent(ImageView imageView, TextView titleView, TextView descriptionView) {
        // بارگذاری تصویر (نام فایل تصویر را مشخص کنید)
        loadImageFromAssets(imageView, "titleimage.jpg"); // یا "titleimage.png"

        // بارگذاری متن‌ها
        String[] texts = readTextFromAssets("title.txt");
        titleView.setText(texts[0]);
        descriptionView.setText(texts[1]);
    }

    public String[][] loadVideosFromAssets() {
        String[][] aparatVideosData;
        try {
            InputStream inputStream = context.getAssets().open("videos.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String text = new String(buffer, "UTF-8");

            // حذف کرل بریس‌های ابتدا و انتها
            text = text.trim();
            if (text.startsWith("{") && text.endsWith("}")) {
                text = text.substring(1, text.length() - 1).trim();
            }

            // جدا کردن هر object (هر رکورد که داخل {...} است)
            String[] records = text.split("\\},\\s*\\{");

            aparatVideosData = new String[records.length][8];

            for (int i = 0; i < records.length; i++) {
                String record = records[i];
                // حذف { و } اضافی در ابتدا و انتهای هر رکورد
                record = record.replace("{", "").replace("}", "").trim();

                // جدا کردن فیلدها بر اساس کاما
                // توجه: مقادیر ممکن است داخل دابل کوتیشن باشند یا نباشند
                String[] fields = record.split(",\\s*");

                for (int j = 0; j < 8 && j < fields.length; j++) {
                    String field = fields[j].trim();
                    // حذف دابل کوتیشن از ابتدا و انتها
                    if (field.startsWith("\"") && field.endsWith("\"")) {
                        field = field.substring(1, field.length() - 1);
                    }
                    // حذف &amp; و جایگزینی با &
                    field = field.replace("&amp;", "&");
                    aparatVideosData[i][j] = field;
                }
            }

            // بررسی نتیجه
            for (int i = 0; i < aparatVideosData.length; i++) {
                Log.d("VideoData", "Record " + i + ": " + Arrays.toString(aparatVideosData[i]));
            }

        } catch (IOException e) {
            e.printStackTrace();
            // در صورت خطا، آرایه خالی یا پیش‌فرض قرار بدهید
            aparatVideosData = new String[0][8];
        }
        return aparatVideosData;
    }
}