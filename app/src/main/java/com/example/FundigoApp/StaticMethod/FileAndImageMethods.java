package com.example.FundigoApp.StaticMethod;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileAndImageMethods {

    public static String getCustomerPhoneNumFromFile(Context context) {
        String number = "";
        String myData = "";
        try {
            File myExternalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "verify.txt");
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (myData != null) {
            if (myData.contains("isFundigo")) {
                String[] parts = myData.split(" ");
                number = parts[0];
            } else
                number = myData;
        }
        return number;
        //return "0545262140";
    }

    public static Bitmap getImageFromDevice(Intent data, Context context) { //14.10 assaf updated for prevent outpfmemory exceptions

        Uri selectedImage = data.getData();
        Matrix matrix = new Matrix();
        int angleToRotate = getOrientation(selectedImage, context);
        matrix.postRotate(angleToRotate);
        Bitmap rotatedBitmap;
       // ParcelFileDescriptor parcelFileDescriptor =
         //       null;
      //  try {
          //  parcelFileDescriptor = context.getContentResolver().openFileDescriptor(selectedImage, "r");
       // } catch (FileNotFoundException e) {
         //   e.printStackTrace();
       // }
        try {
            Bitmap image =  decodeFile(new File (getPath(selectedImage,context)));

          //   FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

           // Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

           // parcelFileDescriptor.close();


              rotatedBitmap = Bitmap.createBitmap(image,
                    0,
                    0,
                    image.getWidth(),
                    image.getHeight(),
                    matrix,
                    true);

           // return image;
            return rotatedBitmap;

        } catch (Exception e) { //14.10 - assaf added exceptiosn handling
            e.printStackTrace();
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return null;
    }



    private static int getOrientation(Uri selectedImage, Context context) {
        int orientation = 0;
        final String[] projection = new String[]{MediaStore.Images.Media.ORIENTATION};
        final Cursor cursor = context.getContentResolver ().query (selectedImage, projection, null, null, null);
        if (cursor != null) {
            final int orientationColumnIndex = cursor.getColumnIndex (MediaStore.Images.Media.ORIENTATION);
            if (cursor.moveToFirst()) {
                orientation = cursor.isNull (orientationColumnIndex) ? 0 : cursor.getInt (orientationColumnIndex);
            }
            cursor.close ();
        }
        return orientation;
    }

    public static ImageLoader getImageLoader(Context context) {

        ImageLoader imageLoader = null;
        DisplayImageOptions options = null;

        options = new DisplayImageOptions.Builder ()
                          .cacheOnDisk (true)
                          .cacheInMemory (true)
                          .bitmapConfig (Bitmap.Config.RGB_565)
                          .imageScaleType (ImageScaleType.EXACTLY)
                          .resetViewBeforeLoading (true)
                          .build ();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder (context)
                                                  .defaultDisplayImageOptions (options)
                                                  .threadPriority (Thread.MAX_PRIORITY)
                                                  .threadPoolSize (4)
                                                  .memoryCache (new WeakMemoryCache ())
                                                  .denyCacheImageMultipleSizesInMemory()
                                                  .build();
        imageLoader = ImageLoader.getInstance ();
        imageLoader.init (config);

        return imageLoader;
    }

    private static Bitmap decodeFile(File f){ //assaf new method : 14.10 Shrink the File to minimal to prevent out of Memort exceptions
        Bitmap b = null;
        int IMAGE_MAX_SIZE = 650;
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        try
        {
            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return b;
    }

    private static String getPath(Uri uri,Context context) //14.10 assaf: new method convert from Uri to File
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }
}
