package com.example.kumkangchangyeong;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PhotoListActivity extends AppCompatActivity{

    public int btnNo;
    public Bitmap bitmap;
    public String imageFile;
    public String locationNo;
    public String dongNo;
    public String floor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potolist);

        // 자신을 호출한 인텐트 얻어오기 -> 자신을 호출한 액티비티의 정보를 받는다.
        Intent intent = getIntent();

        // 호출한 인텐트가 보내온 이미지를 저장한다.
        bitmap = (Bitmap) intent.getExtras().get("imageView");
        btnNo = (int) intent.getExtras().get("btnNo");
        imageFile = (String) intent.getExtras().get("img");
        locationNo = (String) intent.getExtras().get("locationNo");
        dongNo = (String) intent.getExtras().get("dongNo");
        floor = (String) intent.getExtras().get("floor");

        ImageView imageView = (ImageView) findViewById(R.id.anchorimg);
        imageView.setImageBitmap(bitmap);
    }

    // 확인 버튼 클릭 시 DB에 이미지를 저정한다.
    public void onOKButtonClick(View view) {

        // 서버 호출
        SetPicture();

        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);

        ((MainActivity)MainActivity.mContext).ChangeButtonColor(btnNo);

        this.finish();
    }

    // 취소 버튼 클릭 시 초기 화면으로 돌아간다.
    public void onCancleButtonClick(View view) {
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);

        this.finish();
    }

    // 확인버튼 클릭 시 서버에 사진정보를 저장한다.
    private void SetPicture() {

        TelephonyManager systemService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Users users = new Users();

        // 유저 정보를 저장한다. -> 전화번호
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                //권한이없으면 여기
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {// 이전에 권한 요청 거절을 했는지 안했는지 검사: 이전에도 했으면 true
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
                else {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
            }
            else {//권한이 있으면, 번호받기
                try {
                    users.PhoneNumber = systemService.getLine1Number();//없으면 null이들어갈수도있다 -> if(Users.PhoneNumber==null) 으로 활용가능
                    if (users.PhoneNumber == null)
                        users.PhoneNumber = "010-9655-7322";
                    else
                        users.PhoneNumber = users.PhoneNumber.replace("+82", "0");
                }
                catch (Exception e) {
                        /*String str=e.getMessage();
                        String str2=str;*/
                }
                finally {
                }
            }
        }
        else {//낮은 버전이면 바로 번호 받기가능
            try {
                users.PhoneNumber = systemService.getLine1Number();//없으면 null이들어갈수도있다 -> if(Users.PhoneNumber==null) 으로 활용가능
                if (users.PhoneNumber == null)
                    users.PhoneNumber = "010-9655-7322";
                else
                    users.PhoneNumber = users.PhoneNumber.replace("+82", "0");
            }
            catch (Exception e) {

            }
            finally {
            }
        }

        if(users.PhoneNumber == "")
            users.PhoneNumber = "010-9655-7322";

        /*
         * getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
         * SetPicture 서버에 등록되어있는 사용할 함수이름
         *
         * => 접속할 서버 + 호출한 함수
         * */
        String url=getString(R.string.service_address) + "SetPicture";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        // 호출한 서버의 함수에서 사용할, 어플에서 전송할 데이터를 values객체에 담는다.
        values.put("Image", imageFile);
        values.put("PhoneNumber", users.PhoneNumber);
        values.put("AnchorNo", btnNo);
        values.put("LocationNo", locationNo);
        values.put("DongNo", dongNo);
        if(floor.contains("(완료)"))
            floor = floor.replace("(완료)", "");
        values.put("Floor", floor);


        SetPicture gsod = new SetPicture(url, values);
        gsod.execute();
    }

    // 서버 접속 및 데이터 처리
    public class SetPicture extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        SetPicture(String url, ContentValues values){
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
        @Override
        protected void onPostExecute(String result) {
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다

            try {
                JSONArray jsonArray = new JSONArray(result);
                String ErrorCheck = "";

                for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject child = jsonArray.getJSONObject(i);
                            if (!child.getString("ErrorCheck").equals("null")) {//문제가 있을 시, 에러 메시지 호출 후 종료
                                ErrorCheck = child.getString("ErrorCheck");
                                Toast.makeText(getBaseContext(), ErrorCheck, Toast.LENGTH_SHORT).show();
                       return;
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //progressOFF();
            }

        }
    }



}
