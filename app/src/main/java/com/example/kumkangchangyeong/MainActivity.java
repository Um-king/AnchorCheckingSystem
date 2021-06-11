// ScrollView
package com.example.kumkangchangyeong;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
    public ArrayList<String> floorList;
    public int floorListIndex; // 현재 선택한 층 리스트의 인덱스번호를 저장

    public AnchorListViewAdapter listAdapter;
    public ArrayAdapter spinnerAdapter;

    //리스트뷰
    public ArrayList<ArrayList<Integer>> viewList = new ArrayList<>();
    public ListView listView;

    public Button finishBtn; // 작업완료

    SharedPreferences _pref;
    Boolean isShortcut = false;//아이콘의 생성

    /*
   버전다운로드 관련 변수
    */
    DownloadManager mDm;
    long mId = 0;
    //Handler mHandler;
    String serverVersion;
    String downloadUrl;
    ProgressDialog mProgressDialog;
    //버전 변수 끝

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_main3);

        _pref = getSharedPreferences("kumkang", MODE_PRIVATE);//sharedPreferences 이름: "kumkang"에 저장
        isShortcut = _pref.getBoolean("isShortcut", false);//"isShortcut"에 들어있는값을 가져온다.

        if (!isShortcut)//App을 처음 깔고 시작했을때 이전에 깐적이 있는지없는지 검사하고, 이름과 아이콘을 설정한다.
        {
            addShortcut(this);
        }

        setContentView(R.layout.activity_main4);

        checkServerVersion();//버전을 체크-> 안쪽에 권한

        ////정보입력
        //ImageView imageView = (ImageView) findViewById(R.id.imgKumkang);
        //imageView.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Intent intent = new Intent(getApplicationContext(), InputActivity.class);
        //        startActivity(intent);
        //    }
        //});

        // 작업완료버튼 클릭 시 알림 출력
        Button button1 = (Button) findViewById(R.id.finishBtn);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("작업 완료");
                builder.setMessage("관리자에게 작업완료 보고를 전송하겠습니까?");
                builder.setPositiveButton("전송", null);
                builder.setNegativeButton("취소", null);
                builder.create().show();
            }
        });


        // 현황버튼 클릭 시 현황 페이지로 이동
        Button button = (Button) findViewById(R.id.printBtn);
        button.setOnClickListener(new View.OnClickListener() {
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
        finishBtn = (Button) findViewById(R.id.finishBtn);
        listView = (ListView) findViewById(R.id.listView);

        floorList = new ArrayList<String>();
        floorList.add("-층 선택-");
        GetFloorDataAll(); // 해당 현장과 동에 대한 저장된 모든 층 정보를 호출한다.

        spiner = (Spinner) findViewById(R.id.spinner1);

        spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, floorList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiner.setAdapter(spinnerAdapter);

        spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Selected Floor: " + floorList.get(position), Toast.LENGTH_SHORT).show(); // 선택한 층을 메시지박스로 보여준다.
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK); // 스피너의 선택된 첫번째 인덱스 값 색상 = 블랙
                floorListIndex = position;

                TextView textView = (TextView) findViewById(R.id.txtSelectNo);
                textView.setText("✔선택한 앙카 번호: ");

                if(floorList.get(position) != "-층 선택-") {
                    btnList = new HashMap<String, Button>();
                    savebtnList = new ArrayList<Integer>();
                    String spinerText = spiner.getSelectedItem().toString();
                    GetFloorAnchorCount(spinerText); // 해당 현장,동,층에 대한 앙카 개수를 가져온다.
                }
                else{
                    viewList.clear();
                    //listAdapter.notifyDataSetChanged();
                    listAdapter = new AnchorListViewAdapter(MainActivity.this, R.layout.listview_button_row, viewList);
                    listView.setAdapter(listAdapter);
                    finishBtn.setVisibility(View.INVISIBLE);
                    textView = (TextView) findViewById(R.id.txtSelectNo);
                    textView.setText("✔선택한 앙카 번호: ");

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

    private void checkServerVersion() {
        String url = getString(R.string.service_address) + "checkAppVersion";
        ContentValues values = new ContentValues();
        values.put("AppCode", getString(R.string.app_code));
        CheckAppVersion cav = new CheckAppVersion(url, values);
        cav.execute();

    }


    public class CheckAppVersion extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;

        CheckAppVersion(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress bar를 보여주는 등등의 행위
            //startProgress();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values);
            return result; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
        }

        @Override
        protected void onPostExecute(String result) {
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다

            try {
                if (result.equals("")) {
                    Toast.makeText(MainActivity.this, "서버연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    ActivityCompat.finishAffinity(MainActivity.this);
                }
                JSONArray jsonArray = new JSONArray(result);

                JSONObject child = jsonArray.getJSONObject(0);
                downloadUrl = child.getString("Message");
                serverVersion = child.getString("ResultCode");

                if (result.equals(""))
                    finish();
                else {
                    if (Double.parseDouble(serverVersion) > getCurrentVersion()) {//좌측이 DB에 있는 버전
                        newVersionDownload();
                    } else {
                        // finish();
                    }
                }
                //CheckPermission();
            } catch (Exception er) {

            } finally {
                //progressOFF();
            }
        }
    }

    private void newVersionDownload() {
        new android.app.AlertDialog.Builder(MainActivity.this).setMessage("새로운 버전이 있습니다. 다운로드 할까요?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mProgressDialog = ProgressDialog.show(MainActivity.this, "다운로드", "잠시만 기다려주세요");

                Toast.makeText(MainActivity.this, downloadUrl, Toast.LENGTH_SHORT).show();

                Uri uri = Uri.parse(downloadUrl);
                DownloadManager.Request req = new DownloadManager.Request(uri);
                req.setTitle("창녕공장 매립앙카 어플리케이션 다운로드");
                req.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, "KUMKANG.apk");

                //req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pathSegments.get(pathSegments.size() - 1));
                //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();


                req.setDescription("창녕공장 매립앙카 어플리케이션 설치파일을 다운로드 합니다.");
                req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                mId = mDm.enqueue(req);
                IntentFilter filter = new IntentFilter();
                filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);


                registerReceiver(mDownComplete2, filter);

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "최신버전으로 업데이트 하시기 바랍니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.finishAffinity(MainActivity.this);
            }
        }).show();
    }

    /**
     * 다운로드 완료 이후의 작업을 처리한다.(다운로드 파일 열기)
     */
    BroadcastReceiver mDownComplete2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            Toast.makeText(context, "다운로드 완료", Toast.LENGTH_SHORT).show();

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(mId);
            Cursor cursor = mDm.query(query);
            if (cursor.moveToFirst()) {

                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);

                //String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                //int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    String fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    openFile(fileUri);
                }
            }
        }
    };

    protected void openFile(String uri) {

        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri)).toString());
        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        Intent open = new Intent(Intent.ACTION_VIEW);
        open.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        /*if(open.getFlags()!=Intent.FLAG_GRANT_READ_URI_PERMISSION){//권한 허락을 안한다면
            Toast.makeText(getBaseContext(), "Look!", Toast.LENGTH_LONG).show();
            finish();
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//누가 버전 이상이라면 FileProvider를 사용한다.
            uri = uri.substring(7);
            File file = new File(uri);
            Uri u = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            open.setDataAndType(u, mimetype);
        } else {
            open.setDataAndType(Uri.parse(uri), mimetype);
        }

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }


        Toast.makeText(getBaseContext(), "설치 완료 후, 어플리케이션을 다시 시작하여 주십시요.", Toast.LENGTH_LONG).show();
        startActivity(open);
        // finish();//startActivity 전일까 후일까 잘판단

    }

    public int getCurrentVersion() {

        int version;

        try {
            mDm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            PackageInfo i = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            version = i.versionCode;
            //Users.CurrentVersion = version;

            return version;

        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private void addShortcut(Context context) {

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.setClassName(context, getClass().getName());
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //FLAG_ACTIVITY_NEW_TASK: 실행한 액티비티와 관련된 태스크가 존재하면 동일한 태스크내에서 실행하고, 그렇지 않으면 새로운 태스크에서 액티비티를 실행하는 플래그
        //FLAG_ACTIVITY_RESET_TASK_IF_NEEDED: 사용자가 홈스크린이나 "최근 실행 액티비티목록"에서 태스크를 시작할 경우 시스템이 설정하는 플래그, 이플래그는 새로 태스크를
        //시작하거나 백그라운드 태스크를 포그라운드로 가지고 오는 경우가 아니라면 영향을 주지 않는다, "최근 실행 액티비티 목록":  홈 키를 오랫동안 눌렀을 떄 보여지는 액티비티 목록

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);//putExtra(이름, 실제값)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "KUMKANG");
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.img_kumkang));
        //intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.logo2));
        //Intent.ShortcutIconResource.fromContext(context, R.drawable.img_kumkang);
        intent.putExtra("duplicate", false);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        sendBroadcast(intent);
        SharedPreferences.Editor editor = _pref.edit();
        editor.putBoolean("isShortcut", true);

        editor.commit();
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



    // 해당 현장,동,층에 대한 모든 층 정보를 가져온다.
    private void GetFloorDataAll(){
        //
        // getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
        // GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
        //
        // => 접속할 서버 + 호출한 함수
        //
        String url=getString(R.string.service_address) + "GetFloorDataAll";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        // 호출한 서버의 함수에서 사용할, 어플에서 전송할 데이터를 values객체에 담는다.
        values.put("locationNo", "1");
        values.put("dong", "1");


        MainActivity.GetFloorDataAll gsod = new MainActivity.GetFloorDataAll(url, values);
        gsod.execute();

    }

    // 서버 접속 및 데이터 처리
    public class GetFloorDataAll extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        GetFloorDataAll(String url, ContentValues values){
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
                    String floor = child.getString("floor") + "F";
                    String flag = child.getString("flag");

                    if(flag.equals("1")) {
                        floor = floor + "(완료)";
                    }

                    floorList.add(floor);
                }

                MakeCameraButton(); // 카메라 버튼 생성
                //MakeCameraButton2(); // 카메라 버튼 생성
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //progressOFF();
            }

        }
    }






    // 카메라 버튼을 생성한다.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void MakeCameraButton() {

        viewList = new ArrayList<>();
        ArrayList<Integer> list = new ArrayList<>();

        for(int i = 1; i <= anchorCount; i++) {

            if ((i - 1) % 5 == 0) {
                list = new ArrayList<>();
                viewList.add(list);
            }

            list.add(i);

        }

        listAdapter = new AnchorListViewAdapter(MainActivity.this, R.layout.listview_button_row, viewList);
        listView.setAdapter(listAdapter);

    }





    @Override // 카메라 촬영 시 onActivityResult를 통해 사진을 가져온다.
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { super.onActivityResult(requestCode, resultCode, data);

        try {

            // 이미지를 비트맵으로 변경하여 용량을 줄임
            Bundle bundle = data.getExtras();
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 8; // 8분의 1크기로 비트맵 객체를 생성
            //options.inSampleSize = 4; // 4분의 1크기로 비트맵 객체를 생성
            Bitmap bitmap = (Bitmap) bundle.get("data"); // data에 이미지 정보가 담겨있다.

            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bStream); // 두번째 파라미터 -> 압축정도 : 90%로 압축하겠다.
            byte[] byteArray = bStream.toByteArray();

            String ImageFile = compressImage2(Base64.encodeToString(byteArray, Base64.DEFAULT));

            Intent intent = new Intent(this, PhotoListActivity.class);
            intent.putExtra("img", ImageFile); // putExtra를 통해 호출한 액티비티로 사진 객체를 전송한다. -> 서버에 바이트형식으로 저장된다.
            intent.putExtra("imageView", bitmap); // putExtra를 통해 호출한 액티비티로 사진 객체를 전송한다. -> ImageView 출력을 위한 객체
            intent.putExtra("btnNo", btnNo); // 선택한 버튼의 id값을 넘긴다. -> 서버에 저장 시 버튼 색 변경을 위함
            intent.putExtra("locationNo", "1");
            intent.putExtra("dongNo", "1");
            intent.putExtra("floor", spiner.getSelectedItem().toString().replace("F", ""));
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

            if(!savebtnList.contains(buttonNumber)) {
                savebtnList.add(buttonNumber);

                if (savebtnList.size() == anchorCount) {
                    // 해당 층에 대해 모든 앙카를 등록했다면 완료된 작업으로 Master 정보를 수정한다(Flag : 0 -> 1)
                    UpdateEndAnchor();
                    floorList.clear();
                    floorList.add("-층 선택-");
                    GetFloorDataAll();

                    spiner.setSelection(floorListIndex);
                    finishBtn.setVisibility(View.VISIBLE); // 버튼 활성화
                }
                else
                    finishBtn.setVisibility(View.INVISIBLE); // 버튼 비활성화

                //button.setBackgroundColor(Color.parseColor("#D9D9D9"));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override // onCreate()는 레이아웃이 그려지기전에 호출되기 때문에 해당 함수를 오버라이딩해서 값을 구해야한다.
    public void onWindowFocusChanged(boolean hasFocus) {
        //height = linearLayout.getHeight();

    }


    private  String compressImage2(String jsonString){

        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2; // 1/2배율로 읽어오게 하는 방법

        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

        int targetWidth = 1000; // your arbitrary fixed limit
        int targetHeight = (int) (decodedByte.getHeight() * targetWidth / (double) decodedByte.getWidth());

        Bitmap resized = Bitmap.createScaledBitmap( decodedByte, targetWidth, targetHeight, true );
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 90,  bStream);
        byte[] byteArray = bStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // 해당 현장,동,층에 대한 앙카 개수를 가져온다.
    private void GetFloorAnchorCount(String spinerText){
        //
        // getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
        // GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
        //
        // => 접속할 서버 + 호출한 함수
        //
        String url=getString(R.string.service_address) + "GetFloorAnchorCount";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        if(spinerText.contains("(완료)"))
            spinerText = spinerText.replace("(완료)", "");

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

                if(savebtnList.size() == anchorCount)
                    finishBtn.setVisibility(View.VISIBLE); // 버튼 활성화
                else
                    finishBtn.setVisibility(View.INVISIBLE); // 버튼 비활성화

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



    public class AnchorListViewAdapter extends ArrayAdapter {

        Context context;
        int layoutRsourceId;
        ArrayList data;
        LinearLayout layout;
        private ArrayList<ArrayList<Integer>> listViewList;



        public AnchorListViewAdapter(Context context, int layoutResourceID, ArrayList data) {

            super(context, layoutResourceID, data);
            this.context = context;
            this.layoutRsourceId = layoutResourceID;
            this.data = data;

            this.listViewList = new ArrayList<ArrayList<Integer>>();
            this.listViewList.addAll(data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            View row = convertView;
            if (row == null) {

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                //row = inflater.inflate(R.layout.listview_button_row, null);

                row = LayoutInflater.from(context).inflate(R.layout.listview_button_row, parent,false);

                viewHolder = new ViewHolder();
                viewHolder.button1 = row.findViewById(R.id.btn1);
                viewHolder.button2 = row.findViewById(R.id.btn2);
                viewHolder.button3 = row.findViewById(R.id.btn3);
                viewHolder.button4 = row.findViewById(R.id.btn4);
                viewHolder.button5 = row.findViewById(R.id.btn5);

                row.setTag(viewHolder);

            }
            else
                viewHolder = (ViewHolder) convertView.getTag();


            List<Integer> item = (List<Integer>) data.get(position);
            if (item != null) {

                for (int i = 0; i < item.size(); i++) {

                    Button btn = new Button(MainActivity.this);
                    switch (i){
                        case 0:
                            btn = (Button) row.findViewById(R.id.btn1);
                            //viewHolder.button1 = row.findViewById(R.id.btn1);
                            break;
                        case 1:
                            btn = (Button) row.findViewById(R.id.btn2);
                            //viewHolder.button2 = row.findViewById(R.id.btn2);
                            break;
                        case 2:
                            btn = (Button) row.findViewById(R.id.btn3);
                            //viewHolder.button3 = row.findViewById(R.id.btn3);
                            break;
                        case 3:
                            btn = (Button) row.findViewById(R.id.btn4);
                            //viewHolder.button4 = row.findViewById(R.id.btn4);
                            break;
                        case 4:
                            btn = (Button) row.findViewById(R.id.btn5);
                            //viewHolder.button5 = row.findViewById(R.id.btn5);
                            break;
                    }

                    int value = item.get(i);
                    //btn.setId(value);

                    if (!savebtnList.contains(value)) {
                        Drawable roundDrawable = getResources().getDrawable(R.drawable.roundbtn); // xml파일을 통해 원으로 그린다.
                        btn.setBackground(roundDrawable);
                    } else {
                        Drawable roundDrawable = getResources().getDrawable(R.drawable.saveroundbtn); // xml파일을 통해 원으로 그린다.
                        btn.setBackground(roundDrawable);
                    }

                    btn.setText(String.valueOf(value));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.weight = 1;
                    params.setMargins(10, 10, 10, 10);
                    btn.setLayoutParams(params);

                    btnList.put(String.valueOf(value), btn);

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //  앙카가 저장된 정보가 없다면
                            if (!savebtnList.contains(btnNo)) {
                                // 이전 버튼의 색상을 변경
                                Button prebtn = btnList.get(String.valueOf(btnNo));

                                Drawable roundDrawable = getResources().getDrawable(R.drawable.roundbtn); // xml파일을 통해 원으로 그린다. (이전 선택 버튼을 원상태로)
                                prebtn.setBackground(roundDrawable);
                            } else {
                                Button nowbtn = btnList.get(String.valueOf(btnNo));
                                Drawable roundDrawable = getResources().getDrawable(R.drawable.saveroundbtn); // xml파일을 통해 원으로 그린다. (클릭한 버튼을 선택상태로)
                                nowbtn.setBackground(roundDrawable);
                            }

                            btnNo = value;

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

                    layout = row.findViewById(R.id.viewList);
                    //layout.addView(btn);

                }
            }


            if(item.size() != 5){

                int num = 5 - item.size();

                for(int i = 0; i < num; i++){
                    Button btn = new Button(MainActivity.this);

                    switch (5 - i){
                        case 1:
                            btn = (Button) row.findViewById(R.id.btn1);
                            //viewHolder.button1 = row.findViewById(R.id.btn1);
                            break;
                        case 2:
                            btn = (Button) row.findViewById(R.id.btn2);
                            //viewHolder.button2 = row.findViewById(R.id.btn2);
                            break;
                        case 3:
                            btn = (Button) row.findViewById(R.id.btn3);
                            //viewHolder.button3 = row.findViewById(R.id.btn3);
                            break;
                        case 4:
                            btn = (Button) row.findViewById(R.id.btn4);
                            //viewHolder.button4 = row.findViewById(R.id.btn4);
                            break;
                        case 5:
                            btn = (Button) row.findViewById(R.id.btn5);
                            //viewHolder.button5 = row.findViewById(R.id.btn5);
                            break;
                    }


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.weight = 1;
                    params.setMargins(10, 10, 10, 10);
                    btn.setLayoutParams(params);

                    layout = row.findViewById(R.id.viewList);
                    btn.setVisibility(View.INVISIBLE); // 버튼 숨기기
                    //layout.addView(btn);
                }
            }

            viewHolder.button1 = row.findViewById(R.id.btn1);
            viewHolder.button2 = row.findViewById(R.id.btn2);
            viewHolder.button3 = row.findViewById(R.id.btn3);
            viewHolder.button4 = row.findViewById(R.id.btn4);
            viewHolder.button5 = row.findViewById(R.id.btn5);

            return row;
        }



    }

    // 리스트뷰 홀더
    private class ViewHolder{
        Button button1;
        Button button2;
        Button button3;
        Button button4;
        Button button5;
        LinearLayout layout;
    }

    // 해당 층에 대한 모든 앙카 등록 작업이 끝났다면 마스터 정보를 변경한다.
    private void UpdateEndAnchor(){

        String url=getString(R.string.service_address) + "UpdateEndAnchor";
        ContentValues values = new ContentValues();

        String spinerText = spiner.getSelectedItem().toString();

        values.put("LocationNo", "1");
        values.put("DongNo", "1");
        values.put("Floor", spinerText.replace("F", ""));

        MainActivity.UpdateEndAnchor gsod = new MainActivity.UpdateEndAnchor(url, values);
        gsod.execute();

    }

    // 서버 접속 및 데이터 처리
    public class UpdateEndAnchor extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        UpdateEndAnchor(String url, ContentValues values){
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

            try {
                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject child = jsonArray.getJSONObject(i);
                }

                String floorInfo = floorList.get(floorListIndex);
                floorList.set(floorListIndex, floorInfo + "(완료)");
                spiner.setSelection(floorListIndex);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //progressOFF();
            }

        }

       
    }


}









/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

// 가장 최근 최종본 (ViewPager 사용 -> activity_main3)
/*
package com.example.kumkangchangyeong;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

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
import android.util.Log;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

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
    public ArrayList<String> floorList;

    public Button finishBtn;


    // 뷰페이지
    public int tabCount = 0;
    private ArrayList<BlankFragment> mData;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_main3);
        setContentView(R.layout.activity_main4);

        ImageView imageView = (ImageView) findViewById(R.id.imgKumkang);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InputActivity.class);
                startActivity(intent);
            }
        });


        // 현황버튼 클릭 시 현황 페이지로 이동
        Button button = (Button) findViewById(R.id.printBtn);
        button.setOnClickListener(new View.OnClickListener() {
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
        finishBtn = (Button) findViewById(R.id.finishBtn);

        floorList = new ArrayList<String>();
        floorList.add("-층 선택-");
        GetFloorDataAll(); // 해당 현장과 동에 대한 저장된 모든 층 정보를 호출한다.

        spiner = (Spinner) findViewById(R.id.spinner1);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, floorList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiner.setAdapter(adapter);

        spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Selected Floor: " + floorList.get(position), Toast.LENGTH_SHORT).show(); // 선택한 층을 메시지박스로 보여준다.
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK); // 스피너의 선택된 첫번째 인덱스 값 색상 = 블랙

                if(floorList.get(position) != "-층 선택-") {
                    btnList = new HashMap<String, Button>();
                    savebtnList = new ArrayList<Integer>();
                    String spinerText = spiner.getSelectedItem().toString();
                    GetFloorAnchorCount(spinerText); // 해당 현장,동,층에 대한 앙카 개수를 가져온다.


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



    // 해당 현장,동,층에 대한 모든 층 정보를 가져온다.
    private void GetFloorDataAll(){
        //
         // getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
         // GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
         //
         // => 접속할 서버 + 호출한 함수
         // //
        String url=getString(R.string.service_address) + "GetFloorDataAll";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        // 호출한 서버의 함수에서 사용할, 어플에서 전송할 데이터를 values객체에 담는다.
        values.put("locationNo", "1");
        values.put("dong", "1");


        MainActivity.GetFloorDataAll gsod = new MainActivity.GetFloorDataAll(url, values);
        gsod.execute();

    }

    // 서버 접속 및 데이터 처리
    public class GetFloorDataAll extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        GetFloorDataAll(String url, ContentValues values){
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
                    String floor = child.getString("floor") + "F";
                    String flag = child.getString("flag");

                    if(flag.equals("1")) {
                        floor = floor + "(완료)";
                    }

                    floorList.add(floor);
                }

                MakeCameraButton(); // 카메라 버튼 생성
                //MakeCameraButton2(); // 카메라 버튼 생성
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //progressOFF();
            }

        }
    }






    // 카메라 버튼을 생성한다.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void MakeCameraButton() {

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        FragmentAdapter madpter = new FragmentAdapter(getSupportFragmentManager());
        mPager.setAdapter(madpter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mPager);


        int btnDrowCount = 0;
        BlankFragment fragment = new BlankFragment();
        int fCount = 0;

        // 버튼 선언

        for(int i = 1; i <= anchorCount; i++) {

            if((i - btnDrowCount) == 1){
                fragment = mData.get(fCount++);
                btnDrowCount += 20;
            }

//            if((i + 19 - btnDrowCount)/5 == 0)
//                linearLayout = (LinearLayout) fragment.getView().findViewById(R.id.fragment_btn1);
//            else if((i + 19 - btnDrowCount)/5 == 1)
//                linearLayout = (LinearLayout) fragment.getView().findViewById(R.id.fragment_btn2);
//            else if((i + 19 - btnDrowCount)/5 == 2)
//                linearLayout = (LinearLayout) fragment.getView().findViewById(R.id.fragment_btn3);
//            else if((i + 19 - btnDrowCount)/5 == 3)
//                linearLayout = (LinearLayout) fragment.getView().findViewById(R.id.fragment_btn4);

            if((i + 19 - btnDrowCount)/5 == 0)
                linearLayout = fragment.linearLayout;
            else if((i + 19 - btnDrowCount)/5 == 1)
                linearLayout = fragment.linearLayout1;
            else if((i + 19 - btnDrowCount)/5 == 2)
                linearLayout = fragment.linearLayout2;
            else if((i + 19 - btnDrowCount)/5 == 3)
                linearLayout = fragment.linearLayout3;



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
        int anchorbutton = 20 - (anchorCount % 20);
        for(int i = anchorbutton; i > 0; i--) {
            if((i-1)/5 == 3)
                linearLayout = fragment.linearLayout;
            else if((i-1)/5 == 2)
                linearLayout = fragment.linearLayout1;
            else if((i-1)/5 == 1)
                linearLayout = fragment.linearLayout2;
            else if((i-1)/5 == 0)
                linearLayout = fragment.linearLayout3;

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { super.onActivityResult(requestCode, resultCode, data);

        try {

            // 이미지를 비트맵으로 변경하여 용량을 줄임
            Bundle bundle = data.getExtras();
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 8; // 8분의 1크기로 비트맵 객체를 생성
            //options.inSampleSize = 4; // 4분의 1크기로 비트맵 객체를 생성
            Bitmap bitmap = (Bitmap) bundle.get("data"); // data에 이미지 정보가 담겨있다.

            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bStream); // 두번째 파라미터 -> 압축정도 : 20%로 압축하겠다.
            byte[] byteArray = bStream.toByteArray();

            String ImageFile = compressImage2(Base64.encodeToString(byteArray, Base64.DEFAULT));

            Intent intent = new Intent(this, PotoListActivity.class);
            intent.putExtra("img", ImageFile); // putExtra를 통해 호출한 액티비티로 사진 객체를 전송한다. -> 서버에 바이트형식으로 저장된다.
            intent.putExtra("imageView", bitmap); // putExtra를 통해 호출한 액티비티로 사진 객체를 전송한다. -> ImageView 출력을 위한 객체
            intent.putExtra("btnNo", btnNo); // 선택한 버튼의 id값을 넘긴다. -> 서버에 저장 시 버튼 색 변경을 위함
            intent.putExtra("locationNo", "1");
            intent.putExtra("dongNo", "1");
            intent.putExtra("floor", spiner.getSelectedItem().toString().replace("F", ""));
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

            if(!savebtnList.contains(buttonNumber)) {
                savebtnList.add(buttonNumber);

                if (savebtnList.size() == anchorCount)
                    finishBtn.setVisibility(View.VISIBLE); // 버튼 활성화
                else
                    finishBtn.setVisibility(View.INVISIBLE); // 버튼 비활성화

                //button.setBackgroundColor(Color.parseColor("#D9D9D9"));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override // onCreate()는 레이아웃이 그려지기전에 호출되기 때문에 해당 함수를 오버라이딩해서 값을 구해야한다.
    public void onWindowFocusChanged(boolean hasFocus) {
        //height = linearLayout.getHeight();

    }


    private  String compressImage2(String jsonString){

        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2; // 1/2배율로 읽어오게 하는 방법

        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

        int targetWidth = 1000; // your arbitrary fixed limit
        int targetHeight = (int) (decodedByte.getHeight() * targetWidth / (double) decodedByte.getWidth());

        Bitmap resized = Bitmap.createScaledBitmap( decodedByte, targetWidth, targetHeight, true );
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 90,  bStream);
        byte[] byteArray = bStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // 해당 현장,동,층에 대한 앙카 개수를 가져온다.
    private void GetFloorAnchorCount(String spinerText){
         //
         // getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
         // GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
         //
         // => 접속할 서버 + 호출한 함수
         //
        String url=getString(R.string.service_address) + "GetFloorAnchorCount";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        if(spinerText.contains("(완료)"))
            spinerText = spinerText.replace("(완료)", "");

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

                if(savebtnList.size() == anchorCount)
                    finishBtn.setVisibility(View.VISIBLE); // 버튼 활성화
                else
                    finishBtn.setVisibility(View.INVISIBLE); // 버튼 비활성화

                MakeCameraButton(); // 카메라 버튼 생성
                //MakeCameraButton2(); // 카메라 버튼 생성
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



    public class FragmentAdapter extends FragmentStatePagerAdapter  {

        public FragmentAdapter(@NonNull FragmentManager fm) {
            super(fm);

            mData = new ArrayList<>();

            tabCount = anchorCount / 20;
            if(anchorCount % 20 != 0)
                tabCount += 1;

            for(int i = 0; i < tabCount; i++) {
                mData.add(new BlankFragment());
            }
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            BlankFragment fragment = mData.get(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            int startNo = (position * 20) + 1;
            int endNo = startNo + 19;
            return startNo + " ~ " + endNo;
        }
    }

}


*/

/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/


// 가장 처음
/*
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
import android.widget.TableLayout;
import android.widget.TableRow;
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
        //setContentView(R.layout.activity_main3);

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



    // 카메라 버튼을 생성한다.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void MakeCameraButton2() {

        TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
        TableRow tableRow = null;

        // 버튼 선언
        for(int i = 1; i <= anchorCount; i++) {

            if(i % 5 == 1){
                tableRow = new TableRow(this);
                TableLayout.LayoutParams params = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1;
                params.setMargins(10,0,10,0);
                tableRow.setLayoutParams(params);

                tableLayout.addView(tableRow);
            }

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
            tableRow.addView(btn);


            //원본
            TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
//        int anchorbutton = (30-anchorCount) % 5;
//        for(int i = 0; i < anchorbutton; i++) {
//            Button btn = new Button(this);
//            Drawable roundDrawable = getResources().getDrawable(R.drawable.roundbtn); // xml파일을 통해 원으로 그린다.
//            btn.setBackground(roundDrawable);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//            params.weight = 1;
//            params.setMargins(10,10,10,10);
//            btn.setLayoutParams(params);
//            btn.setVisibility(View.INVISIBLE); // 버튼 숨기기
//            linearLayout.addView(btn);
//        }
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
        
         // getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
         // GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
         //
         // => 접속할 서버 + 호출한 함수
         // 
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
                //MakeCameraButton2(); // 카메라 버튼 생성
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

*/