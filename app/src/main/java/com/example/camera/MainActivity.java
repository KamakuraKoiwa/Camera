package com.example.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;  //  TAKE_PHOTO来作为case处理图片的标识
    private ImageView imgIcon;  //  显示拍照后的图片
    private Button btnPhoto;    //  拍照
    private Uri imageUri;   //  通用资源标志符

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(); //初始化控件
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 *      问：   首先为什么这里要创建时间对象？
                 *      答：   因为，如果我们不用当前本地的时间作为图片的名字，当然这里图片的名字可以设为一个死值
                 *      例如：Monday.jpg    —  那么对于这个死值作为图片的名字，当我们需要拍多张照片时，就会一直覆盖
                 *      这张照片，也就是说，我们就无法实现多张照片的保存，之前的照片就会被新的照片覆盖掉。
                 *      所以说，这里用本地的当前时间作为照片的名字，就解决了这个问题
                 *      简单的来说就是，利用本地时间 — 解决图片名冲突问题
                 */


                Locale aLocale = Locale.getDefault();
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss", aLocale);
                Date curDate = new Date(System.currentTimeMillis());
                String str = format.format(curDate);

                /**
                 *          创建File对象，用于存储拍照后的照片
                 *          第一个参数：  是这张照片存放在手机SD卡的对应关联缓存应用
                 *          第二个参数：  这张图片的命名
                 */
                File outputImage = new File(getExternalCacheDir(),str+".jpg");
                try {
                    if (outputImage.exists()){          //  检查与File对象相连接的文件和目录是否存在于磁盘中
                        outputImage.delete();           //  删除与File对象相连接的文件和目录
                    }
                    outputImage.createNewFile();        //  如果与File对象相连接的文件不存在，则创建一个空文件
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= 24){       //  如果运行设备的系统版本高于 Android7.0
                    /**
                     *          将File对象转换成一个封装过的Uri对象
                     *          第一个参数：  要求传入Context参数
                     *          第二个参数：  可以是任意唯一的字符串
                     *          第三个参数：  我们刚刚创建的File对象
                     */
                    imageUri = FileProvider.getUriForFile(MainActivity.this,"com.example.camera.fileprovider", outputImage);
                }
                else{
                    //  如果运行设备的系统版本低于 Android7.0
                    //  将File对象转换成Uri对象，这个Uri对象表示着 str + ".jpg" 这张图片的本地真实路径
                    imageUri = Uri.fromFile(outputImage);
                }
                /**
                 *      启动相机程序
                 */
                //  将Intent的action指定为 拍照到指定目录 —— android.media.action.IMAGE_CAPTURE
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                //  指定图片的输出地址
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                /**
                 *      在通过startActivityForResult()，来启动活动，因此拍完照后会有结果返回到 onActivityResult()方法中
                 */
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });

    }

    /**
     *
     * @param requestCode       第一个是请求码，可以进行传递数据前的一些操作，比如根据不同的请求码，设置不同的传递内容。
     * @param resultCode        第二个是返回码，也就是在B中设置的int的数值，这个是得到返回的内容的标识。
     * @param data              第三个是Intent的数据，比如在B中的setResult方法中传递了一些数据，在A中就可以通过解析Intent的内容来获得传递过来的数据。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK){       //  当拍照成功后，会返回一个返回码，这个值为 -1 — RESULT_OK
                    try{
                        //  根据Uri找到这张照片的资源位置，将它解析成Bitmap对象，然后将把它设置到imageView中显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imgIcon.setImageBitmap(bitmap);
                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", "description"); //插入系统相册
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void initView() {
        btnPhoto = findViewById(R.id.btn_photo);
        imgIcon = findViewById(R.id.img_icon);
    }
}


