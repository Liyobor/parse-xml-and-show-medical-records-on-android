package com.example.myapplication;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Xml;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    ArrayList info = new ArrayList();
    ArrayList formatList = new ArrayList(Arrays.asList("姓名:","醫事機構:","醫師姓名:","醫事機構:","科別:"));
    ArrayList procedureFormatList =new ArrayList();
    String procedure =new String();
    ArrayList prescriptionFormatList =new ArrayList();
    String prescription =new String();
    InputStream data;
    private void initData(){
        info.add(11,"病情摘要");
        ListView listView = (ListView)findViewById(R.id.listViewAaa);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,info);
        listView.setAdapter(adapter);
    }





    private void parseXml(InputStream raw){



        AssetManager assetManager = getAssets();
        XmlPullParser xmlPullParser = Xml.newPullParser();
        try {
//            data = assetManager.open(fileName);
            data = raw;
//            System.out.println(assetManager.open(fileName));
            xmlPullParser.setInput(data,"utf-8");
            int eventType = xmlPullParser.getEventType();
            int nameCount = 0;
            int effectiveTimeCount = 0;
            int formatListCount = 0;
            int resultCount =0;
            int count1 = 0;
            int count2 = 0;
            while (eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("recordTarget")){
                        }
                        else if(xmlPullParser.getName().equals("name")){
                            if(nameCount<=4){
                                String name = xmlPullParser.nextText();
                                info.add(formatList.get(nameCount)+name);
                                nameCount +=1;
                            }
                        }else if(xmlPullParser.getName().equals("birthTime")){
                            String birthTime = xmlPullParser.getAttributeValue(null,"value");
                            info.add("出生日期 : "+birthTime);

                        }else if(xmlPullParser.getName().equals("effectiveTime")){
                            if(effectiveTimeCount==0){
                                String effectiveTime = xmlPullParser.getAttributeValue(null,"value");
                                info.add("列印日期 : "+effectiveTime);
                                effectiveTimeCount +=1;
                            }else{
                                String effectiveTime = xmlPullParser.getAttributeValue(null,"value");
                                info.add("門診日期 : "+effectiveTime);
                            }

                        }else if(xmlPullParser.getName().equals("administrativeGenderCode")){
                            String gender = xmlPullParser.getAttributeValue(null,"displayName");
//                            Log.e("data",gender);
                            if(gender.equals("Male")){
                                info.add("性別:男");
                            }else{
                                info.add("性別:女");
                            }
                        }else if(xmlPullParser.getName().equals("patient")){
                            if(xmlPullParser.getAttributeValue(null,"classCode").equals("PSN")){
                                xmlPullParser.nextTag();
                                String id = xmlPullParser.getAttributeValue(null,"extension");
                                info.add("身分證字號 : "+id);
                            }

                        }else if(xmlPullParser.getName().equals("time")){
                            String time = xmlPullParser.getAttributeValue(null,"value");
                            info.add("醫事紀錄時間:"+time);
                        }else if(xmlPullParser.getName().equals("title")){
                            switch (xmlPullParser.nextText()){
                                case "診斷":
                                    String diagnosis = new String();
                                    while(true){
                                        xmlPullParser.nextTag();
                                        if(xmlPullParser.getEventType() == XmlPullParser.END_TAG){
                                            break;
                                        }else if(xmlPullParser.getName().equals("paragraph")){
                                            diagnosis += xmlPullParser.nextText();
                                            diagnosis += "\n\n";
                                        }
                                    }
                                    info.add("診斷:"+diagnosis);
                                    break;
                                case "主觀描述":
                                    String subjective =new String();
                                    while(true){
                                        xmlPullParser.nextTag();
                                        if(xmlPullParser.getEventType() == XmlPullParser.END_TAG){
                                            break;
                                        }else if(xmlPullParser.getName().equals("paragraph")){
                                            subjective += xmlPullParser.nextText();
                                            subjective += "\n\n";
                                        }
                                    }
                                    info.add("主觀描述:\n"+subjective);
                                    break;
                                case "客觀描述":
                                    String Objective = new String();
                                    while(true){
                                        xmlPullParser.nextTag();
                                        if(xmlPullParser.getEventType() == XmlPullParser.END_TAG){
                                            break;
                                        }else if(xmlPullParser.getName().equals("paragraph")){
                                            Objective += xmlPullParser.nextText();
                                            Objective += "\n\n";
                                        }
                                    }
                                    info.add("客觀描述:\n"+Objective);
                            }
                        }
                        else if(xmlPullParser.getName().equals("thead")){
                            xmlPullParser.nextTag();
                            xmlPullParser.nextTag();
                            while(!xmlPullParser.getName().equals("tr")){
                                if(formatListCount==0){
                                    String text = new String(xmlPullParser.nextText());
                                    Log.e("Procedure",text);
                                    procedureFormatList.add(text);
                                }else if(formatListCount ==1){
                                    String text = new String(xmlPullParser.nextText());
                                    Log.e("Prescription",text);
                                    prescriptionFormatList.add(text);
                                }
                                xmlPullParser.nextTag();
                            }
                            formatListCount = 1;
                        }
                        else if(xmlPullParser.getName().equals("tbody")){
                            if(resultCount == 0){
                                info.add("處置項目");
                            }else{
                                info.add("處方");
                            }
                            xmlPullParser.nextTag();
                            xmlPullParser.nextTag();
                            while (!xmlPullParser.getName().equals("tr")){
                                if(xmlPullParser.getName().equals("td")){
                                    if(resultCount==0){
                                        String text =new String(xmlPullParser.nextText());
                                        Log.e("Procedure",text);
                                        procedure += procedureFormatList.get(count1)+" : "+text +"\n";
                                        count1+=1;
                                    }else{

                                        String text =new String(xmlPullParser.nextText());
                                        prescription += prescriptionFormatList.get(count2)+" : "+text+"\n";
                                        Log.e("Procedure",text);
                                        count2+=1;
                                    }

                                }
                                xmlPullParser.nextTag();
                            }
                            if(resultCount == 0){
                                info.add(procedure);
                            }else{
                                info.add(prescription);
                            }
                            resultCount = 1;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }

            data.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT>9){
            StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
//        getAssetsStream("sampleMR.xml");

//        System.out.println("AAAA");

        try {
            data = Get_HttpURLConnection.main();
            parseXml(data);
            initData();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public static class Get_HttpURLConnection {

        private static HttpURLConnection get_connection;

        public static InputStream main() throws IOException {

            String url = "http://140.136.151.70/Project/Download/download.php";

            try {
                // We retrieve the contents of our webpage.
                URL myurl = new URL(url);
                get_connection = (HttpURLConnection) myurl.openConnection();
                // Here we specify the connection type
                get_connection.setRequestMethod("GET");
                StringBuilder webpage_content;

                try (BufferedReader webpage = new BufferedReader(
                        new InputStreamReader(get_connection.getInputStream()))) {

                    String webpage_line;
                    webpage_content = new StringBuilder();

                    while ((webpage_line = webpage.readLine()) != null) {

                        webpage_content.append(webpage_line);
                        webpage_content.append(System.lineSeparator());
                    }
                }

                InputStream stream = new ByteArrayInputStream(webpage_content.toString().getBytes(StandardCharsets.UTF_8));
//                System.out.println(webpage_content);


                return stream;



            } finally {
                //Disconnect the connection
                get_connection.disconnect();

            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

    }
}

