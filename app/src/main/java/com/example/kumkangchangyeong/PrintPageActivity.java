package com.example.kumkangchangyeong;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.List;

public class PrintPageActivity extends AppCompatActivity{

    public String locationNo;
    public String dong;
    public String floor;
    public String anchorCount;
    public String anchorNumber = "0";

    public ArrayList<SaveAnchorData> saveAnchorList;

    public TableRow preTableRow; // 이전 테이블 로우의 정보를 가진다.

    public String imageFile;

    public ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_print);
        setContentView(R.layout.activity_print2);

        // 자신을 호출한 인텐트 얻어오기 -> 자신을 호출한 액티비티의 정보를 받는다.
        Intent intent = getIntent();

        // 호출한 인텐트가 보내온 값을 저장한다.
        locationNo = (String) intent.getExtras().get("locationNo");
        dong = (String) intent.getExtras().get("dong");
        floor = (String) intent.getExtras().get("floor");
        anchorCount = Integer.toString( intent.getIntExtra("anchorCount",0));

        preTableRow = new TableRow(this);
        listView = (ListView) findViewById(R.id.listView);

        if(floor.contains("(완료)"))
            floor = floor.replace("(완료)", "");

//        TextView txtSaveAnchorCount = (TextView) findViewById(R.id.txtSaveAnchorCount);
//        txtSaveAnchorCount.setText("등록 앙카 개수: " + anchorCount);

        TextView txtdong = (TextView) findViewById(R.id.txtDong);
        txtdong.setText(" ① 동 : " + dong + "동");

        TextView txtFloor = (TextView) findViewById(R.id.txtFloor);
        txtFloor.setText(" ② 층 : " + floor.replace("F", "층"));

        // 해당 현장,동,층에 등록된 앙카를 가져온다.
        GetSaveAnchorData();
    }

    public void GetSaveAnchorData() {
    /*
     * getString(R.string.service_address) : strings.xml 파일에 등록한 접속할 서버 정보 -> Port번호까지 작성
     * GetFloorAnchorCount 서버에 등록되어있는 사용할 함수이름
     *
     * => 접속할 서버 + 호출한 함수
     * */
    String url=getString(R.string.service_address) + "GetSaveAnchorData";  // 211.245.239.53:3334
    ContentValues values = new ContentValues();

    // 호출한 서버의 함수에서 사용할, 어플에서 전송할 데이터를 values객체에 담는다.
        values.put("locationNo", "1");
        values.put("dong", "1");
        values.put("floor", floor.replace("F", ""));
        values.put("anchorCount", "0"); // 앙카개수를 초기값으로 보내서 해당 정보가 존재하면 Count를 변경하여 가져온다.
        values.put("anchorNo", "0"); // 등록된 앙카의 정보

        PrintPageActivity.GetSaveAnchorData gsod = new PrintPageActivity.GetSaveAnchorData(url, values);
        gsod.execute();

    }

        // 서버 접속 및 데이터 처리
        public class GetSaveAnchorData extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        GetSaveAnchorData(String url, ContentValues values){
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
                saveAnchorList = new ArrayList<SaveAnchorData>();
                JSONArray jsonArray = new JSONArray(result);
                SaveAnchorData anchorData;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject child = jsonArray.getJSONObject(i);

                    anchorData = new SaveAnchorData();

                    anchorData.anchorNo = child.getString("anchorNo");
                    anchorData.createDate = child.getString("createDate");
                    anchorData.userName = child.getString("userName");


                    String a = anchorData.anchorNo;
                    String b =  anchorData.createDate;
                    String c =  anchorData.userName;


                    saveAnchorList.add(anchorData);

                    String a1 = saveAnchorList.get(i).anchorNo;
                    String b1 =  saveAnchorList.get(i).createDate;
                    String c1 =  saveAnchorList.get(i).userName;

                    //if(anchorData.anchorNo == "0")
                    //    break;
                    //MakePrintData(anchorData.anchorNo, anchorData.createDate, anchorData.userName);

                }

                String a1 = saveAnchorList.get(0).anchorNo;
                String b1 =  saveAnchorList.get(0).createDate;
                String c1 =  saveAnchorList.get(0).userName;

                TextView txtNotSaveAnchorCount = (TextView) findViewById(R.id.txtNotSaveAnchorCount);
                int notSaveCount = Integer.parseInt(anchorCount) - saveAnchorList.size();
                txtNotSaveAnchorCount.setText(" ④ 미등록 앙카 개수: " + notSaveCount);

                TextView txtSaveAnchorCount = (TextView) findViewById(R.id.txtSaveAnchorCount);
                txtSaveAnchorCount.setText(" ③ 등록 앙카 개수: " + saveAnchorList.size());

                String content = txtNotSaveAnchorCount.getText().toString();
                SpannableString spannableString = new SpannableString(content);
                // 특정문자열의 시작위치와 끝위치 얻어오기
                String word = String.valueOf(notSaveCount);
                int start = content.indexOf(word);
                int end = start + word.length();
                // 특정 문자열 색상 변경
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), start,end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                txtNotSaveAnchorCount.setText(spannableString);


                for(int i = 0; i < saveAnchorList.size(); i++){
                    String a = saveAnchorList.get(i).anchorNo;
                    String b = saveAnchorList.get(i).createDate;
                    String c = saveAnchorList.get(i).userName;
                }
                MakePrintData2(saveAnchorList);

                //MakePrintData(); // 화면에 출력한다.
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //progressOFF();
            }

        }
    }


    public void MakePrintData2(ArrayList<SaveAnchorData> _list){

        ArrayList<SaveAnchorData> list = _list;
        PrintAnchorListViewAdapter adapter = new PrintAnchorListViewAdapter(PrintPageActivity.this, R.layout.listview_print_row, list);
        listView.setAdapter(adapter);
    }

    public class PrintAnchorListViewAdapter extends ArrayAdapter {

        Context context;
        int layoutRsourceId;
        ArrayList data;
        private ArrayList<SaveAnchorData> list;

        public PrintAnchorListViewAdapter(Context context, int layoutResourceID, ArrayList data) {

            super(context, layoutResourceID, data);
            this.context = context;
            this.layoutRsourceId = layoutResourceID;
            this.data = data;

            this.list = new ArrayList<SaveAnchorData>();
            this.list.addAll(data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder viewHolder;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(R.layout.listview_print_row, null);

                viewHolder = new ViewHolder();
                viewHolder.txt1 = row.findViewById(R.id.txt1);
                viewHolder.txt2 = row.findViewById(R.id.txt2);
                viewHolder.txt3 = row.findViewById(R.id.txt3);

                row.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            SaveAnchorData anchor = (SaveAnchorData) data.get(position);

            TextView textViewAnchor = (TextView) row.findViewById(R.id.txt1);
            TextView textViewAnchor2 = (TextView) findViewById(R.id.anchorNumber);
            int width = textViewAnchor2.getWidth();
            textViewAnchor.setWidth(width);
            textViewAnchor.setText(anchor.anchorNo);

            TextView textViewDate = (TextView) row.findViewById(R.id.txt2);
            TextView textViewDate2 = (TextView) findViewById(R.id.date);
            width = textViewDate2.getWidth();
            textViewDate.setWidth(width);
            textViewDate.setText(anchor.createDate);

            TextView textViewUser = (TextView) row.findViewById(R.id.txt3);
            TextView textViewUser2 = (TextView) findViewById(R.id.userName);
            width = textViewUser2.getWidth();
            textViewUser.setWidth(width);
            textViewUser.setText(anchor.userName);


            return row;
        }
    }

        // 리스트뷰 홀더
        private class ViewHolder{
            TextView txt1;
            TextView txt2;
            TextView txt3;
        }


        public void MakePrintData(String anchorNo, String createDate, String userName){

        /*
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
        //tableLayout.removeAllViews();

        Drawable roundDrawable = getResources().getDrawable(R.drawable.border_row1); // xml파일을 통해 원으로 그린다.

        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preTableRow.setBackground(getResources().getDrawable(R.drawable.border_table));
                TextView textView = (TextView) findViewById(R.id.txtSelectNo);
                textView.setText("✔선택한 앙카 번호: " + anchorNo + "번");
                anchorNumber = anchorNo;
                v.setBackground(getResources().getDrawable(R.drawable.border_row2));
                preTableRow = (TableRow) v;
            }
        });



        //tableRow.setBackground(roundDrawable);

        TextView textView = new TextView(this);
        textView.setText(anchorNo);
        //textView.setText(i);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);
        textView.setBackground(roundDrawable);
        tableRow.addView(textView);

        TextView textView1 = new TextView(this);
        //textView.setText(saveAnchorData.createDate.substring(0, 10));
        textView1.setText(createDate);
        textView1.setTextColor(Color.BLACK);
        textView1.setGravity(Gravity.CENTER);
        textView1.setBackground(roundDrawable);
        tableRow.addView(textView1);

        TextView textView2 = new TextView(this);
        textView2.setText(userName);
        textView2.setTextColor(Color.BLACK);
        textView2.setGravity(Gravity.CENTER);
        textView2.setBackground(roundDrawable);
        tableRow.addView(textView2);

        //원본
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.weight = 1;
//        tableRow.setLayoutParams(params);
        tableLayout.addView(tableRow, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

         */

    }

    // 취소 버튼 클릭 시 DB에 이미지를 저정한다.
    public void onCancleButtonClick(View view) {

        this.finish();
    }

    // 확인 버튼 클릭 시 DB에 이미지를 저정한다.
    public void onOKButtonClick(View view) {

        //Intent intent = new Intent(this, PrintAnchorImgActivity.class);
        //intent.putExtra("locationNo", locationNo);
        //intent.putExtra("dong", dong);
        //intent.putExtra("anchorNo", anchorNumber);
        //intent.putExtra("floor", floor.replace("F",""));
        //startActivity(intent);

        String url=getString(R.string.service_address) + "GetAnchorIamgeData";  // 211.245.239.53:3334
        ContentValues values = new ContentValues();

        // 호출한 서버의 함수에서 사용할, 어플에서 전송할 데이터를 values객체에 담는다.
        values.put("locationNo", locationNo);
        values.put("dong", dong);
        values.put("anchorNo", anchorNumber);
        values.put("floor", floor.replace("F",""));


        PrintPageActivity.GetAnchorIamgeData gsod = new PrintPageActivity.GetAnchorIamgeData(url, values);
        gsod.execute();

    }

    // 서버 접속 및 데이터 처리
    public class GetAnchorIamgeData extends AsyncTask<Void, Void, String> {
        String url;
        ContentValues values;
        GetAnchorIamgeData(String url, ContentValues values){
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
                    imageFile = child.getString("Imagefile");
                    ViewData(imageFile);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                //progressOFF();
            }

        }

        /**
         *  그림을 보인다.
         * @param imageString
         */
        private void ViewData(String imageString){

            String photostring = imageString;

            try {
                byte[] array5 = Base64.decode(photostring, Base64.DEFAULT);
                Dialog dialog = new Dialog(PrintPageActivity.this);
                dialog.setTitle("Image");
                //dialog.setContentView(R.layout.activity_anchorimg);
                //ImageView imageView = (ImageView) dialog.findViewById(R.id.viewImage);
                dialog.setContentView(R.layout.dialog_image);
                ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView1);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(array5, 0, array5.length)); // 바이트 배열, 시작지점, decode할 바이트 배열의 길이

            /*
            Button saveButton = (Button)dialog.findViewById(R.id.buttonImageSave);
            saveButton.setText("이미지 저장");
            saveButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    byte[] byteArray = Base64.decode(currentImage.ImageFile, Base64.DEFAULT);
                    saveBitmaptoJpeg( BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length), "KUMKANG", currentImage.ImageName);
                }

            });
            */

                dialog.show();

            } catch (Exception ex) {

                Log.e("에러", "비트맵 에러 " + ex.getMessage().toString());
            }
        }
    }


}
