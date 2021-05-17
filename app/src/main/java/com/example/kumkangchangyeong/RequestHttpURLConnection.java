package com.example.kumkangchangyeong;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RequestHttpURLConnection {

    public String request(String _url, ContentValues _params) {

        HttpURLConnection urlConn = null;

        /**
         * 2. HttpURLConnection을 통해 web의 데이터를 가져온다.
         * */
        try {
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();
            // [2-1]. urlConn 설정.

            urlConn = (HttpURLConnection) url.openConnection();
            //Request 형식 설정
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-Type", "application/json");

            //request에 JSON data 준비
            urlConn.setDoOutput(true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream()));
            //commands라는 JSONArray를 담을 JSONObject 생성
            JSONObject commands = new JSONObject();

            // URL 뒤에 붙여서 보낼 파라미터.
            // 파라미터 키와 값.
            String key;
            String value;

            for (Map.Entry<String, Object> parameter : _params.valueSet()) {
                key = parameter.getKey();
                value = parameter.getValue().toString();
                commands.put(key, value);
            }


            //request에 쓰기
            bw.write(commands.toString());
            bw.flush();
            bw.close();

            //보내고 결과값 받기
            int responseCode = urlConn.getResponseCode();
            if (responseCode == 400) {
                System.out.println("400:: 해당 명령을 실행할 수 없음 (실행할 수 없는 상태일 때, 엘리베이터 수와 Command 수가 일치하지 않을 때, 엘리베이터 정원을 초과하여 태울 때)");
            } else if (responseCode == 401) {
                System.out.println("401:: X-Auth-Token Header가 잘못됨");
            } else if (responseCode == 500) {
                System.out.println("500:: 서버 에러, 문의 필요");
            }

            // [2-4]. 읽어온 결과물 리턴.
            // 요청한 URL의 출력물을 BufferedReader로 받는다.
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            // 출력물의 라인과 그 합에 대한 변수.
            String line;
            String page = "";

            // 라인을 받아와 합친다.
            while ((line = reader.readLine()) != null) {
                page += line;
            }
            return page;

        } catch (MalformedURLException e) { // for URL.
            e.printStackTrace();
        } catch (IOException e) { // for openConnection().
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }
        return null;
    }
}
