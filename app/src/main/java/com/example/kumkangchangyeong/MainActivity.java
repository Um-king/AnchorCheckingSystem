package com.example.kumkangchangyeong;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    public int btnNo = 1;
    public int anchorCount = 0;
    public static Context mContext; // PotoListActivity에서 확인버튼 클릭 시 버튼 색 변경을 위한 전달함수
    public Map<String, Button> btnList = new HashMap<String, Button>(); // 버튼 정보를 담는 리스트
    public ArrayList<Integer> savebtnList = new ArrayList<Integer>(); // 저장된 앙카에 대한 리스트
    public Spinner spiner;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 이미지 클릭 시 현황 페이지로 이동
        ImageView imageView = (ImageView) findViewById(R.id.imgKumkang);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PrintPageActivity.class);
                intent.putExtra("locationNo", "1");
                intent.putExtra("dong", "1");
                intent.putExtra("floor", spiner.getSelectedItem().toString());
                intent.putExtra("anchorCount", anchorCount);
                startActivity(intent); // 화면 전환시 AndroidManifest.xml에 전환할 클래스를 등록해줘야한다.. 설정을 안하면 강제종료됨
            }
        });

        mContext = this;

        final String[] floor = {"-층 선택-", "1F", "2F", "3F", "4F", "5F", "6F"};
        spiner = (Spinner) findViewById(R.id.spinner1);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, floor);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiner.setAdapter(adapter);

        spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Selected Floor: " + floor[position], Toast.LENGTH_SHORT).show(); // 선택한 층을 메시지박스로 보여준다.
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK); // 스피너의 선택된 첫번째 인덱스 값 색상 = 블랙

                LinearLayout layout = (LinearLayout) findViewById(R.id.view_btn1);
                layout.removeAllViews(); // 해당 객체의 모든 자식 뷰를 삭제

                layout = (LinearLayout) findViewById(R.id.view_btn2);
                layout.removeAllViews();

                layout = (LinearLayout) findViewById(R.id.view_btn3);
                layout.removeAllViews();

                layout = (LinearLayout) findViewById(R.id.view_btn4);
                layout.removeAllViews();

                layout = (LinearLayout) findViewById(R.id.view_btn5);
                layout.removeAllViews();

                layout = (LinearLayout) findViewById(R.id.view_btn6);
                layout.removeAllViews();

                if(floor[position] != "-층 선택-") {
                    btnList = new HashMap<String, Button>();
                    savebtnList = new ArrayList<Integer>();
                    String spinerText = spiner.getSelectedItem().toString();
                    GetFloorAnchorCount(spinerText); // 해당 현장,동,층에 대한 앙카 개수를 가져온다.

                    //MakeCameraButton(); // 카메라 버튼 생성
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spiner.setSelection(0);




        // 카메라 버튼을 생성한다.
        //MakeCameraButton();

    }




    @Override // 생성한 menu.xml의 요소들을 실제 뷰로 inflate 시켜준다.
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override // 생성한 메뉴들을 누르면 어떤행동을 취할지 설정
    public boolean onOptionsItemSelected(MenuItem item){
        return super.onOptionsItemSelected(item);
    }

    // 카메라 버튼을 생성한다.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void MakeCameraButton() {

        // 버튼 선언
        for(int i = 1; i <= anchorCount; i++) {

            if(i > 0 && i <=5)
                linearLayout = (LinearLayout) findViewById(R.id.view_btn1);
            else if(i > 5 && i <=10)
                linearLayout = (LinearLayout) findViewById(R.id.view_btn2);
            else if(i > 10 && i <=15)
                linearLayout = (LinearLayout) findViewById(R.id.view_btn3);
            else if(i > 15 && i <=20)
                linearLayout = (LinearLayout) findViewById(R.id.view_btn4);
            else if(i > 20 && i <=25)
                linearLayout = (LinearLayout) findViewById(R.id.view_btn5);
            else if(i > 25 && i <=30)
                linearLayout = (LinearLayout) findViewById(R.id.view_btn6);

            Button btn = new Button(this);
            btn.setId(i);

            if(!savebtnList.contains(i)) {
                Drawable roundDrawable = getResources().getDrawable(R.drawable.roundbtn); // xml파일을 통해 원으로 그린다.
                btn.setBackground(roundDrawable);
            }
            else {
                Drawable roundDrawable = getResources().getDrawable(R.drawable.saveroundbtn); // xml파일을 통해 원으로 그린다.
                btn.setBackground(roundDrawable);
            }


            btn.setText(String.valueOf(i));
            linearLayout.addView(btn);


            //원본
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            //params.height = height;
            //params.width = 0;
            params.weight = 1;
            params.setMargins(10,10,10,10);
            btn.setLayoutParams(params);

            btnList.put(String.valueOf(i), btn);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //  앙카가 저장된 정보가 없다면
                    if(!savebtnList.contains(btnNo)) {
                        // 이전 버튼의 색상을 변경
                        Button prebtn = btnList.get(String.valueOf(btnNo));

                        Drawable roundDrawable = getResources().getDrawable(R.drawable.roundbtn); // xml파일을 통해 원으로 그린다. (이전 선택 버튼을 원상태로)
                        prebtn.setBackground(roundDrawable);
                    }

                    else{
                        Button nowbtn = btnList.get(String.valueOf(btnNo));
                        Drawable roundDrawable = getResources().getDrawable(R.drawable.saveroundbtn); // xml파일을 통해 원으로 그린다. (클릭한 버튼을 선택상태로)
                        nowbtn.setBackground(roundDrawable);
                    }

                    btnNo = btn.getId();

                    //// 현재 선택한 앙카가 이미 등록된 앙카번호라면
                    //if(savebtnList.contains(btnNo)) {
                    //    Button nowbtn = btnList.get(String.valueOf(btnNo));
                    //    Drawable roundDrawable = getResources().getDrawable(R.drawable.saveroundbtn); // xml파일을 통해 원으로 그린다. (클릭한 버튼을 선택상태로)
                    //    nowbtn.setBackground(roundDrawable);
                    //}

                    //else {
                    //    // 선택 버튼의 색상을 변경
                    //    Button nowbtn = btnList.get(String.valueOf(btnNo));
                    //    Drawable roundDrawable = getResources().getDrawable(R.drawable.selectroundbtn); // xml파일을 통해 원으로 그린다. (클릭한 버튼을 선택상태로)
                    //    nowbtn.setBackground(roundDrawable);
                    //}

                    // 선택 버튼의 색상을 변경
                    Button nowbtn = btnList.get(String.valueOf(btnNo));
                    Drawable roundDrawable = getResources().getDrawable(R.drawable.selectroundbtn); // xml파일을 통해 원으로 그린다. (클릭한 버튼을 선택상태로)
                    nowbtn.setBackground(roundDrawable);

                    TextView textView = (TextView) findViewById(R.id.txtSelectNo);
                    textView.setText("✔선택한 앙카 번호: " + btnNo + "번");

                    Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // camera Application을 실행 -> Intent를 통해 카메라를 불러온다.
                    startActivityForResult(cameraApp, 101);
                }
            });
        }

        // linerlayout에 남는 공간을 빈칸으로 채워준다. ->weight값 때문에
        int anchorbutton = (30-anchorCount) % 5;
        for(int i = 0; i < anchorbutton; i++) {
            Button btn = new Button(this);
            Drawable roundDrawable = getResources().getDrawable(R.drawable.roundbtn); // xml파일을 통해 원으로 그린다.
            btn.setBackground(roundDrawable);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            params.setMargins(10,10,10,10);
            btn.setLayoutParams(params);
            btn.setVisibility(View.INVISIBLE); // 버튼 숨기기
            linearLayout.addView(btn);
        }
    }

    @Override // 카메라 촬영 시 onActivityResult를 통해 사진을 가져온다.
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            // 이미지를 비트맵으로 변경하여 용량을 줄임
            Bundle bundle = data.getExtras();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8; // 8분의 1크기로 비트맵 객체를 생성
            Bitmap bitmap = (Bitmap) bundle.get("data"); // data에 이미지 정보가 담겨있다.

            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bStream);
            byte[] byteArray = bStream.toByteArray();

            String ImageFile = compressImage2(Base64.encodeToString(byteArray, Base64.DEFAULT));

            Intent intent = new Intent(this, PotoListActivity.class);
            intent.putExtra("img", ImageFile); // putExtra를 통해 호출한 액티비티로 사진 객체를 전송한다. -> 서버에 바이트형식으로 저장된다.
            intent.putExtra("imageView", bitmap); // putExtra를 통해 호출한 액티비티로 사진 객체를 전송한다. -> ImageView 출력을 위한 객체
            intent.putExtra("btnNo", btnNo); // 선택한 버튼의 id값을 넘긴다. -> 서버에 저장 시 버튼 색 변경을 위함
            startActivity(intent);

        } catch (Exception ex) {

            ex.printStackTrace();
        }
        
    }

    public void ChangeButtonColor(int buttonNumber) {

        if(btnList.containsKey(String.valueOf(buttonNumber))) {
            Button button = (Button) btnList.get(String.valueOf(buttonNumber));
            Drawable roundDrawable = getResources().getDrawable(R.drawable.saveroundbtn); // xml파일을 통해 원으로 그린다.
            button.setBackground(roundDrawable);
            savebtnList.add(buttonNumber);
            //button.setBackgroundColor(Color.parseColor("#D9D9D9"));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override // onCreate()는 레이아웃이 그려지기전에 호출되기 때문에 해당 함수를 오버라이딩해서 값을 구해야한다.
    public void onWindowFocusChanged(boolean hasFocus) {
        //height = linearLayout.getHeight();

    }


    private  String compressImage2(String jsonString){
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4; // 1/4배율로 읽어오게 하는 방법

        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

        int targetWidth = 1000; // your arbitrary fixed limit
        int targetHeight = (int) (decodedByte.getHeight() * targetWidth / (double) decodedByte.getWidth());

        Bitmap resized = Bitmap.createScaledBitmap( decodedByte, targetWidth, targetHeight, true );
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80,  bStream);
        byte[] byteArray = bStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // 해당 현장,동,층에 대한 앙카 개수를 가져온다.
    private void GetFloorAnchorCount(String spinerText){
        /*
         * getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
         * GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
         *
         * => 접속할 서버 + 호출한 함수
         * */
        String url=getString(R.string.service_address) + "GetFloorAnchorCount";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        // 호출한 서버의 함수에서 사용할, 어플에서 전송할 데이터를 values객체에 담는다.
        values.put("locationNo", "1");
        values.put("dong", "1");
        values.put("floor", spinerText.replace("F", ""));
        values.put("anchorCount", "0"); // 앙카개수를 초기값으로 보내서 해당 정보가 존재하면 Count를 변경하여 가져온다.
        values.put("anchorNo", "0"); // 등록된 앙카의 정보

        MainActivity.GetFloorAnchorCount gsod = new MainActivity.GetFloorAnchorCount(url, values);
        gsod.execute();

    }

    // 서버 접속 및 데이터 처리
    public class GetFloorAnchorCount extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        GetFloorAnchorCount(String url, ContentValues values){
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

            anchorCount = 0;

            try {
                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject child = jsonArray.getJSONObject(i);
                    anchorCount = child.getInt("anchorCount");
                    savebtnList.add(child.getInt("anchorNo"));
                }

                MakeCameraButton(); // 카메라 버튼 생성
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //progressOFF();
            }

        }
    }

    // 현황 페이지로 이동한다.
    public void printPageOnClick(View v) {

        Intent intent = new Intent(this, PrintPageActivity.class);
        intent.putExtra("locationNo", "1");
        intent.putExtra("dong", "1");
        intent.putExtra("floor", spiner.getSelectedItem().toString());
        startActivity(intent);
    }
}

