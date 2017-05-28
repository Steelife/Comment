package com.example.steel.comment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steel.comment.adapter.AdapterComment;
import com.example.steel.comment.model.Comment;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView comment;
    private TextView hide_down;
    private EditText comment_content;
    private Button comment_send;

    private LinearLayout rl_enroll;
    private RelativeLayout rl_comment;

    private ListView comment_list;
    private AdapterComment adapterComment;
    private List<Comment> data;


    /********************后期广播进来***************************************************/


    private String username;
    private String storeid;

    /*******************************************************************/


    private List<SelectComment.OneComment> com;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        storeid="ab873b1ffc1dc62903ff2d0d";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        // 初始化评论列表
        comment_list = (ListView) findViewById(R.id.comment_list);
        // 初始化数据
        data = new ArrayList<>();
        // 初始化适配器
        adapterComment = new AdapterComment(getApplicationContext(), data);
        // 为评论列表设置适配器
        comment_list.setAdapter(adapterComment);

        comment = (ImageView) findViewById(R.id.comment);
        hide_down = (TextView) findViewById(R.id.hide_down);
        comment_content = (EditText) findViewById(R.id.comment_content);
        comment_send = (Button) findViewById(R.id.comment_send);

        rl_enroll = (LinearLayout) findViewById(R.id.rl_enroll);
        rl_comment = (RelativeLayout) findViewById(R.id.rl_comment);

        setListener();

        selectAll select=new selectAll();
        select.execute(storeid);
    }

    /**
     * 设置监听
     */
    public void setListener(){
        comment.setOnClickListener(this);

        hide_down.setOnClickListener(this);
        comment_send.setOnClickListener(this);
    }

    @Override

    public void onClick(View v) {
        getComment();
        switch (v.getId()) {
            case R.id.comment:
                // 弹出输入法
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                // 显示评论框
                rl_enroll.setVisibility(View.GONE);
                rl_comment.setVisibility(View.VISIBLE);
                break;
            case R.id.hide_down:
                // 隐藏评论框
                rl_enroll.setVisibility(View.VISIBLE);
                rl_comment.setVisibility(View.GONE);
                // 隐藏输入法，然后暂存当前输入框的内容，方便下次使用
                InputMethodManager im = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(comment_content.getWindowToken(), 0);
                break;
            case R.id.comment_send:
                sendComment();
                break;
            default:
                break;
        }
    }

    /**
     * 发送评论
     */

    Comment commentt;
    public void getComment(){

        for(int i=0;i<com.size();i++){
            commentt = new Comment();

            String comm=com.get(i).content;
            String user=com.get(i).user.nickname;
            commentt.setName(user+"：");
            commentt.setContent(comm);
            myThread thread = new myThread();
            thread.execute(comm,user);
        }
    }
    public void sendComment() {
        if (comment_content.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "评论不能为空！", Toast.LENGTH_SHORT).show();
        } else {
            // 生成评论数据
            commentt = new Comment();
            commentt.setName("评论者" + (data.size()) + "：");
            String s = comment_content.getText().toString();
            commentt.setContent(comment_content.getText().toString());
            myThread thread = new myThread();
            thread.execute(s);

        }

    }



    InsertComment insertComment;
    SelectComment selectComment;
    class selectAll extends AsyncTask<Object, Object, List<SelectComment.OneComment>> {
        @Override
        protected void onPostExecute(List<SelectComment.OneComment> comments) {
            com=comments;
            getComment();
        }

        @Override
        protected List<SelectComment.OneComment> doInBackground(Object... strings) {
            selectComment=new SelectComment(storeid);
            return selectComment.GetConmment();
        }
    }
    class myThread extends AsyncTask<String,Intent,String>{
        @Override
        protected void onPostExecute(String s) {
            if(s=="true"){
                adapterComment.addComment(commentt);
                // 发送完，清空输入框
                comment_content.setText("");
                Toast.makeText(getApplicationContext(), "评论成功！", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "评论失败！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            insertComment=new InsertComment("root",strings[0],"32");
            return insertComment.getResponse();
        }
    }
    class SelectComment{
        private String storeId,response;
        private final String Url="http://118.89.231.48:8080/projectmanagement/pm/store/search";
        private OkHttpClient okhttpClient=new OkHttpClient();
        private Gson gson=new Gson();
        private SelectSuccess str;
        private Response Replay=null;
        private String type="byid";
        public final MediaType FORM_CONTENT_TYPE
                = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        public SelectComment(String StoreId) {
            storeId = StoreId;
            response = onConnect();
        }
        private String onConnect(){
            RequestBody body=new FormBody.Builder()
                    .add("type",type)
                    .add("keyword", storeId)
                    .build();
            Request request=new Request.Builder()
                    .url(Url)
                    .post(body)
                    .build();
            try {
                Replay =okhttpClient.newCall(request).execute();
                if(Replay.code()==200){
                    JSONSelecter(Replay.body().string());
                    return "sucess";
                }else{
                    return Replay.message();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "Fail,the client crashed.";
            }
        }
        public String getResponse(){
            return response;
        }
        public String getInfo(){
            return str.info;
        }
        private void JSONSelecter(String JSON){
            str=gson.fromJson(JSON, SelectSuccess.class);
        }

        class User{
            String username,password,email,sex,nickname,age;
        }
        class OneComment{
            String content,id,storeid,time;
            User user;
        }
        class Data{
            String storeID,storename,storetag,address;
            List<OneComment> comments;
        }
        class SelectSuccess{
            String success,info;
            List<Data> data;
        }
        List<OneComment> GetConmment(){
            return str.data.get(0).comments;
        }

    }
    class InsertComment{
        private String content,storeId,response,username;
        private final String Url="http://118.89.231.48:8080/projectmanagement/pm/comment/";
        private OkHttpClient okhttpClient=new OkHttpClient();
        private Gson gson=new Gson();
        private SelectSuccess str;
        private Response Replay=null;
        public final MediaType FORM_CONTENT_TYPE
                = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        public InsertComment(String Username,String Content,String StoreId){
            content=Content;
            storeId=StoreId;
            username=Username;
            response=onConnect();
        }

        private String onConnect(){
            RequestBody body = RequestBody.create(FORM_CONTENT_TYPE,
                    "username="+username+"&"+"content="+content+"&"+"storeid="+storeId);
            Request request=new Request.Builder()
                    .url(Url)
                    .post(body)
                    .build();
            try {
                Replay =okhttpClient.newCall(request).execute();
                if(Replay.code()==200){
                    return JSONSelecter(Replay.body().string());
                }else{
                    return Replay.message();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "Fail,the client crashed.";
            }
        }

        public String getResponse(){
            return response;
        }
        public String getInfo(){
            return str.info;
        }

        private String JSONSelecter(String JSON){
            str=gson.fromJson(JSON, SelectSuccess.class);
            return str.toString();
        }

        private class SelectSuccess{
            String success;
            String info;
            @Override
            public String toString(){
                return success;
            }
        }
    }
}
