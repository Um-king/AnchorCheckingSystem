package com.example.kumkangchangyeong;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class PrintAnchorImgActivity extends AppCompatActivity {

    private String locationNo;
    private String dong;
    private String floor;
    private String anchorNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anchorimg);

        // 자신을 호출한 인텐트 얻어오기 -> 자신을 호출한 액티비티의 정보를 받는다.
        Intent intent = getIntent();

        // 호출한 인텐트가 보내온 값을 저장한다.
        locationNo = (String) intent.getExtras().get("locationNo");
        dong = (String) intent.getExtras().get("dong");
        floor = (String) intent.getExtras().get("floor");
        anchorNo = Integer.toString(intent.getIntExtra("anchorCount", 0));

        GetPhotoData(); // 서버에 저장된 사진을 가져온다.
    }

    public void GetPhotoData() {
        /*
         * getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
         * GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
         *
         * => 접속할 서버 + 호출한 함수
         * */
        String url = getString(R.string.service_address) + "GetPhotoData";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        // 호출한 서버의 함수에서 사용할, 어플에서 전송할 데이터를 values객체에 담는다.
        values.put("locationNo", locationNo);
        values.put("dong", dong);
        values.put("floor", floor);
        values.put("anchorNo", anchorNo); // 등록된 앙카의 정보

        PrintAnchorImgActivity.GetPhotoData gsod = new PrintAnchorImgActivity.GetPhotoData(url, values);
        gsod.execute();

    }

    // 서버 접속 및 데이터 처리
    public class GetPhotoData extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        GetPhotoData(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        // 서버에 접속한다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress bar를 보여주는 등등의 행위
            //startProgress();
        }

        // 서버에 접속하여 데이터를 처리.
        @Override
        protected String doInBackground(Void... params) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection(); // RequestHttpURLConnection Class를 통해 접속할 서버에 대한 정보를 설정한다. (그냥 이대로 사용)
            result = requestHttpURLConnection.request(url, values);
            return result; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
        }

        // 서버 접속이 완료되면.
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        protected void onPostExecute(String result) {
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다

            try {
                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject child = jsonArray.getJSONObject(i);

                    SaveAnchorData anchorData = new SaveAnchorData();


                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //progressOFF();
            }

        }
    }
}