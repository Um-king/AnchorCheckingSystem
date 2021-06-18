package com.example.kumkangchangyeong;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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


    //////////pdf
    public Button pdfBtn;

    private File root;
    private AssetManager assetManager;
    private PDFont font;

    ////////////





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_print);
        setContentView(R.layout.activity_print2);

        pdfBtn = (Button) findViewById(R.id.filedownload);

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

        CreatePDF();
        //ClickCreatePDF2();
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


                    saveAnchorList.add(anchorData);


                    //if(anchorData.anchorNo == "0")
                    //    break;
                    //MakePrintData(anchorData.anchorNo, anchorData.createDate, anchorData.userName);

                }

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) findViewById(R.id.txtSelectNo);
                textView.setText("✔선택한 앙카 번호: " + saveAnchorList.get(position).anchorNo + "번");
                anchorNumber = saveAnchorList.get(position).anchorNo;
            }
        });
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

            String num = anchor.anchorNo;

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

    // PDF를 만든다.
    private void CreatePDF(){
        pdfBtn.setOnClickListener(new View.OnClickListener(){

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                int recHeight = (48 * saveAnchorList.size()) + 330;

                // 타이틀을 출력한다(첫 페이지)
                //int A4_width = (int) PDRectangle.A4.getWidth();
                //int A4_height = (int) PDRectangle.A4.getHeight();
                Bitmap bitmap, scaledbmp, logoBitmap, logoScaledbmp, bgBitmap, bgScaledbmp;
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pdftitle);
                scaledbmp = Bitmap.createScaledBitmap(bitmap, 900, 300, false); // 사이즈

                logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kumkanglogo);
                logoScaledbmp = Bitmap.createScaledBitmap(logoBitmap, 600, 200, false);

                //bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b2);
                bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.viewsubtitle);
                bgScaledbmp = Bitmap.createScaledBitmap(bgBitmap, 1200, 130, false);

                PdfDocument pdfDocument = new PdfDocument();
                Paint paint = new Paint();
                Paint titlePaint = new Paint();

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200, 2000, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawBitmap(scaledbmp,150,350,paint); // scaledbmp의 이미지를 0,50에 위치시킨다.
//                titlePaint.setTextAlign(Paint.Align.CENTER);
//                titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//                titlePaint.setTextSize(70);
//                canvas.drawText("금강공업", 1200/2, 150, paint);

                //paint .setColor(Color.rgb(0, 113, 188));
                paint.setTextSize(40f);
                paint.setStrokeWidth(10);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("현장: 금강공업(주) 서울사무소", 1200/2, 1000, paint);

                paint.setTextSize(40f);
                paint.setStrokeWidth(10);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("담당자: 엄 영 철", 1200/2, 1070, paint);

                paint.setTextSize(40f);
                paint.setStrokeWidth(10);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Tel) 010-9655-7322", 1200/2, 1140, paint);

                canvas.drawBitmap(logoScaledbmp,300,1500,paint); // scaledbmp의 이미지를 0,50에 위치시킨다.

                pdfDocument.finishPage(page);


                // 테이블 표를 출력한다.
                PdfDocument.PageInfo pageInfo1 = null;
                PdfDocument.Page page1 = null;
                Canvas canvas1 = null;


                int y = 300; // 테이블 row의 height 값
                int pageNum = 2; // 출력할 pdf 페이지 Number
                int pageCnt = 0; // 새로 만들 페이지를 계산하는 count 변수 (출력값이 30이 넘어가면 새로운 페이지에 데이터 출력)
                paint.setTextSize(30f);
                for(int i = 0; i < saveAnchorList.size(); i++){

                    if(pageCnt % 30 == 0) {
                        // 테이블 표를 출력한다.
                        pageInfo1 = new PdfDocument.PageInfo.Builder(1200, 2000, pageNum).create();
                        page1 = pdfDocument.startPage(pageInfo1); // 해당페이지 넘버에 데이터 작성을 시작한다.
                        canvas1 = page1.getCanvas();


                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(2);
                        canvas1.drawRect(100,230,1100,recHeight, paint); //1770

                        paint.setStyle(Paint.Style.FILL);
                        canvas1.drawText("번호", 250, 280, paint);
                        canvas1.drawText("등록 날짜", 600, 280, paint);
                        canvas1.drawText("작업자", 950, 280, paint);
                        canvas1.drawLine(400,230,400,recHeight, paint);
                        canvas1.drawLine(800,230,800,recHeight, paint);
                        canvas1.drawLine(100,300,1100,300, paint);

                        canvas1.drawBitmap(bgScaledbmp,0,0,paint);

                        logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kumkanglogo);
                        logoScaledbmp = Bitmap.createScaledBitmap(logoBitmap, 200, 100, false);
                        canvas1.drawBitmap(logoScaledbmp,50,1830,paint);

                        pageNum++;
                    }
                    String[] date = saveAnchorList.get(i).createDate.split(" ");
                    String[] time = date[2].split(":");
                    String str = "AM";
                    if(date[1].equals("오후"))
                        str = "PM";
                    String createDate = date[0] + " " + time[0] + ":" + time[1] + " " + str;
                    canvas1.drawText(saveAnchorList.get(i).anchorNo, 250, y +48, paint);
                    canvas1.drawText(createDate, 600, y +48, paint);
                    //canvas1.drawText(saveAnchorList.get(i).createDate, 600, y +30, paint);
                    canvas1.drawText(saveAnchorList.get(i).userName, 950, y +48, paint);

                    y+=48;
                    pageCnt++;

                    if(pageCnt % 30 == 0) {
                        pdfDocument.finishPage(page1); // 해당 페이지를 종료한다. -> 해당 페이지를 끝내야 다음 페이지를 작성할 수 있다.
                    }
                }

                if(pageCnt % 30 != 0)
                    pdfDocument.finishPage(page1);

                // 원본
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(2);
//                canvas1.drawRect(150,100,1050,1700, paint);
//
//                paint.setStyle(Paint.Style.FILL);
//                canvas1.drawText("번호", 300, 150, paint);
//                canvas1.drawText("등록 날짜", 600, 150, paint);
//                canvas1.drawText("작업자", 900, 150, paint);
//                canvas1.drawLine(450,100,450,1700, paint);
//                canvas1.drawLine(750,100,750,1700, paint);
//                canvas1.drawLine(150,180,1050,180, paint);

//                PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(1200, 2000, 3).create();
//                PdfDocument.Page page2 = pdfDocument.startPage(pageInfo2);
//                Canvas canvas2 = page2.getCanvas();
//                canvas2.drawText("test1", 1200/2, 1140, paint);
//                pdfDocument.finishPage(page2);


                //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/test.pdf");
                //File file = new File(Environment.getExternalStorageDirectory(), "/test.pdf");

                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test" + floor + ".pdf");
                //File file = new File(getExternalFilesDir(null), "test.pdf");


//                PDDocument document = new PDDocument();
//                String path = file.getAbsolutePath();
//                try {
//                    Toast.makeText(PrintPageActivity.this, path+"에 PDF 파일로 저장했습니다.", Toast.LENGTH_LONG).show();
//                    document.save(path);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                try{
                    pdfDocument.writeTo(new FileOutputStream(file));
                    Toast.makeText(PrintPageActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    pdfDocument.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(PrintPageActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(PrintPageActivity.this, "에러", Toast.LENGTH_SHORT).show();
                }


                pdfDocument.close();
            }
        });
    }


}
