package cn.edu.gdmec.s07150808.sqldemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Handler queryHandler=null;
    EditText qy_id,s_id;
    Button add;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qy_id = (EditText) findViewById(R.id.qy_id);
        s_id = (EditText) findViewById(R.id.s_id);
        add = (Button) findViewById(R.id.btn1);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetThread a = new GetThread();
                a.start();
            }
        });

    }
    //子线程：通过GET方法向服务器发送用户名、密码的信息
    class GetThread extends Thread {


        @Override
        public void run() {
            //用HttpClient发送请求，分为五步
            //第一步：创建HttpClient对象

            HttpClient httpClient = new DefaultHttpClient();
            //注意，下面这一行中，我之前把链接中的"test"误写成了"text"，导致调BUG调了半天没弄出来，真是浪费时间啊
            String url = "http://127.0.0.1/tp/index.php/Home/Index/get_data?qy_id=" +qy_id.getText().toString()+
                    "&=s_id="+s_id.getText().toString() ;
            //第二步：创建代表请求的对象,参数是访问的服务器地址
            HttpGet httpGet = new HttpGet(url);
            try {
                //第三步：执行请求，获取服务器发还的相应对象
                HttpResponse response = httpClient.execute(httpGet);
                //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                if (response.getStatusLine().getStatusCode() == 200) {
                    //第五步：从相应对象当中取出数据，放到entity当中
                    HttpEntity entity = response.getEntity();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(entity.getContent()));
                    String result = reader.readLine();
                    Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    //子线程：使用POST方法向服务器发送用户名、密码等数据
    class PostThread extends Thread {

        String name;
        String pwd;

        public PostThread(String name, String pwd) {
            this.name = name;
            this.pwd = pwd;
        }

        @Override
        public void run() {
            HttpClient httpClient = new DefaultHttpClient();
            String url = "http://192.168.2.111:87/tp323/index.php/Home/Index/app_data";
            //第二步：生成使用POST方法的请求对象
            HttpPost httpPost = new HttpPost(url);
            //NameValuePair对象代表了一个需要发往服务器的键值对
            NameValuePair pair1 = new BasicNameValuePair("name", name);
            NameValuePair pair2 = new BasicNameValuePair("password", pwd);
            //将准备好的键值对对象放置在一个List当中
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(pair1);
            pairs.add(pair2);
            try {
                //创建代表请求体的对象（注意，是请求体）
                HttpEntity requestEntity = new UrlEncodedFormEntity(pairs);
                //将请求体放置在请求对象当中
                httpPost.setEntity(requestEntity);
                //执行请求对象
                try {
                    //第三步：执行请求对象，获取服务器发还的相应对象
                    HttpResponse response = httpClient.execute(httpPost);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = response.getEntity();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(entity.getContent()));
                        String result = reader.readLine();
                        Log.d("HTTP", "POST:" + result);
                        Message msg=new Message();
                        msg.obj=result;
                        queryHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
             queryHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    JSONArray jsonObjs = null;
                    try {
                        jsonObjs = new JSONArray(msg.obj.toString());
                        String result = "";
                        for(int i=0;i<jsonObjs.length();i++){
                            JSONObject jsonObject = jsonObjs.getJSONObject(i);
                            result+= jsonObject.get("id")+":"+jsonObject.get("qy_id")+":"+jsonObject.get("s_id")+":"+"\n";

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
        }
    }

}
