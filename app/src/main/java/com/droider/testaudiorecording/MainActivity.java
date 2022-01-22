package com.droider.testaudiorecording;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_start, btn_stop;
    private ListView lv_content;
    private File sdcardfile = null;
    private String[] files;
    private MediaRecorder recorder = null;

    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyPermissions(this);
        initView();
        getSDCardFile();
        getFileList();

    }

    /**
     * ① 实例化控件
     */
    private void initView() {
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        lv_content = (ListView) findViewById(R.id.lv_content);
        //⑤给按钮添加监听事件
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        //设置起始状态开始按钮可用，停止按钮不可用
        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);

    }

    /**
     * ②获取内存卡中文件的方法
     */
//    @RequiresApi(api = Build.VERSION_CODES.R)
    private void getSDCardFile() {
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//内存卡存在
//             sdcardfile = Environment.getExternalStorageDirectory();//获取目录文件
//            sdcardfile = Environment.getStorageDirectory();
//        } else {
//            Toast.makeText(this, "未找到内存卡", Toast.LENGTH_SHORT).show();
//        }
        sdcardfile = new File("/data/data/com.droider.testaudiorecording");
    }

    /**
     * ③获取文件列表（listView中的数据源）
     * 返回指定文件类型的文件名的集合作为数据源
     */
    private void getFileList() {
        if (sdcardfile != null) {
            files = sdcardfile.list(new MyFilter());
            lv_content.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files));
            //⑥给ListView中的元素添加点击播放事件
            lv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //⑩定义播放音频的方法
                    play(files[position]);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                    startRecord();
                break;
            case R.id.btn_stop:
                stopRcecord();
                break;
        }

    }

    /**
     * ④定义一个文件过滤器MyFilter的内部类,实现FilenameFilter接口
     * 重写里边accept方法
     */
    class MyFilter implements FilenameFilter {

        @Override
        public boolean accept(File pathname, String fileName) {
            return fileName.endsWith(".amr");
        }
    }

    /**
     * ⑦给两个按钮定义开始和暂停的方法
     */
    private void startRecord() {
        Log.d("MainActivity", "startRecord()");
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频源为手机麦克风
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//设置输出格式3gp
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//设置音频编码为amr格式
        //获取内存卡的根目录，创建临时文件
        try {

//            File file = File.createTempFile("record_", ".amr", sdcardfile);
//            File file = new File(sdcardfile + File.separator + "RecordTest" + File.separator + "record_" + ".amr" );
            File file = new File(sdcardfile +  File.separator + "record_" + ".amr" );
//            file.getParentFile().mkdir();
            file.createNewFile();

            recorder.setOutputFile(file.getAbsolutePath());//设置文件输出路径
            //准备和启动录制音频
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            Log.e("Error startRecord()", "IOException");
            e.printStackTrace();
        }
        //启动后交换两个按钮的可用状态
        btn_start.setEnabled(false);
        btn_stop.setEnabled(true);

    }

    private void stopRcecord() {
        Log.d("MainActivity", "stopRecord()");
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);
        //刷新列表数据
        getFileList();
    }

    public static void verifyPermissions(Activity activity) {
        // Check if we have write permission 手动请求权限
//        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (permission != PackageManager.PERMISSION_GRANTED) {
            //     We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
//        }

    }
        /**
         * ⑨重写onRequestPermissionsResult方法
         * 获取动态权限请求的结果,再开启录制音频
         */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allaccepted = true;
        if(requestCode == 1){
            for(int i=0; i<grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    allaccepted = false;
                }
            }

            if(allaccepted == false){
                Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
//            startRecord();
//        } else {
//            Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * ⑩定义播放音频的方法
     */
    private void play(String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //播放音频需要uri,从文件中获取,文件中需要路径
//        Uri uri = Uri.fromFile(new File(sdcardfile.getAbsoluteFile() + File.separator + fileName));
        File tmpfile = new File(sdcardfile.getAbsoluteFile() + File.separator + fileName);
        //设置播放数据和类型
//        intent.setAction("com.")
        Uri uri = FileProvider.getUriForFile(
                this,
                this.getPackageName() + ".fileprovider",
                tmpfile);
        intent.setDataAndType(uri, "audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setData(uri);
        startActivity(intent);
    }

//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//

}