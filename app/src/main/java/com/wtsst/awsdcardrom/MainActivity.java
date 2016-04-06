package com.wtsst.awsdcardrom;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

//该功能主要实现Android系统获取Sd卡路径和SD卡内存的方法，详细的分析了所涉及的类和函数的功能，需要的朋友可以下载参考
// （论程序员的修养）
// @作者：孙顺涛2016.03 。完整已提交到git
//更多请关注个人比较垃圾的博客：www.bgwan.blog.163.com

//说明：android.permission.WRITE_EXTERNAL_STORAGE的权限用来设置了内置sd卡的写权限
//  android.permission.WRITE_MEDIA_STORAGE的权限用来设置了外置sd卡的写权限
public class MainActivity extends AppCompatActivity {
    private TextView romTextView;
    private TextView sdcardTextView;
    private TextView storageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storageTextView = (TextView) findViewById(R.id.storageTextView);
        romTextView = (TextView) findViewById(R.id.romTextView);
        sdcardTextView = (TextView) findViewById(R.id.sdcardTextView);
        String romPath = getRomPath() == null ? "内置SD卡不存在" : getRomPath();
        Log.d("内置SD的路径为：", romPath + "/wtsst");
        romTextView.setText("内置SD卡的路径为：" + romPath + "/wtsst");
        //显示sdcard的路径
        StringBuilder sdCardStringBuilder = new StringBuilder();
        List<String> sdCardPathList = getSDCardPath();
        if (sdCardPathList != null) {
            for (String path : sdCardPathList) {
                sdCardStringBuilder.append(path);
            }
            sdcardTextView.setText("外置SD卡的路径为：" + sdCardStringBuilder.toString() + "/wtsst");
        } else {
            sdcardTextView.setText("外置SD卡的路径为：" + "-不存在");
        }

    }

    //获取内置SD卡路径
    private String getRomPath() {
        String romPath = null;
        boolean isromExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);//判断SD是否存在，也可以不判断
        if (isromExist) {
            romPath = Environment.getExternalStorageDirectory().getPath();//获取根目录
            Log.d("内置SD file文件toString", Environment.getExternalStorageDirectory().toString());
        }
        return romPath;
    }

    //获取外置Sdcard的路径
    public List<String> getSDCardPath() {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return lResult;
    }

    //计算SD卡剩余容量和总容量
    public void OnCalculator(View view) {
        getSize();
    }

    private void getSize() {
        //判断是否有插入存储卡
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            //取得sdcard文件路径
            StatFs statfs = new StatFs(path.getPath());
            //获取block的SIZE
            long blocSize = statfs.getBlockSize();
            //获取BLOCK数量
            long totalBlocks = statfs.getBlockCount();
            //己使用的Block的数量
            long availaBlock = statfs.getAvailableBlocks();
            String[] total = filesize(totalBlocks * blocSize);
            String[] availale = filesize(availaBlock * blocSize);
            //设置进度条的最大值
//            int maxValue = Integer.parseInt(availale[0])*bar.getMax()/Integer.parseInt(total[0]);
//            bar.setProgress(maxValue);
            //显示SD卡的容量信息
            String text = "SD卡总共：" + total[0] + total[1] + "/n；" + "可用：" + availale[0] + availale[1];
            storageTextView.setText(text);
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
            storageTextView.setText("没有SDcard,请插入SD卡后再测试");
        }
    }

    //返回数组，下标1代表大小，下标2代表单位 KB/MB
    private String[] filesize(long size) {
        String str = "";
        if (size >= 1024) {
            str = "KB";
            size /= 1024;
            if (size >= 1024) {
                str = "MB";
                size /= 1024;
            }
        }
        DecimalFormat formatter = new DecimalFormat();
        formatter.setGroupingSize(3);
        String result[] = new String[2];
        result[0] = formatter.format(size);
        result[1] = str;
        return result;
    }
}