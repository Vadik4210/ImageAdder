package ua.com.curl.web.imageAdder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText APIloginEditText;
    private EditText APIpassEditText;
    private EditText ftpServerEditText;
    private EditText ftpLoginEditText;
    private EditText ftpPasswordEditText;
    private TextView textViewCheckFTP;
    private Button buttonAddImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ua.com.curl.web.imageAdder.R.layout.activity_main);
        createMainField();
    }
    private void createMainField(){
        SharedPreferences pref = getSharedPreferences("main", MODE_PRIVATE);
        APIloginEditText =(EditText)findViewById(ua.com.curl.web.imageAdder.R.id.loginPHPET);
        APIloginEditText.setText(pref.getString("APIlogin", "").toString());
        APIpassEditText =(EditText)findViewById(ua.com.curl.web.imageAdder.R.id.passPHPET);
        APIpassEditText.setText(pref.getString("APIpass", "").toString());
        ftpServerEditText =(EditText)findViewById(ua.com.curl.web.imageAdder.R.id.ftpSeverText);
        ftpServerEditText.setText(pref.getString("FTPserver", "").toString());
        ftpLoginEditText =(EditText)findViewById(ua.com.curl.web.imageAdder.R.id.ftpLoginText);
        ftpLoginEditText.setText(pref.getString("FTPlogin", "").toString());
        ftpPasswordEditText =(EditText)findViewById(ua.com.curl.web.imageAdder.R.id.ftpPasswordText);
        ftpPasswordEditText.setText(pref.getString("FTPpass", "").toString());
        textViewCheckFTP=(TextView)findViewById(ua.com.curl.web.imageAdder.R.id.textViewCheckFTP);
        buttonAddImage=(Button)findViewById(ua.com.curl.web.imageAdder.R.id.buttonAddImage);
        if(logicButtonAdd()==true){
            buttonAddImage.setEnabled(true);
            textViewCheckFTP.setText("Ок");
        }
        else {
            buttonAddImage.setEnabled(false);
            textViewCheckFTP.setText("Заполните все поля");
        }
    }

    public void onClickSinch(View view) {
        SharedPreferences pref = getSharedPreferences("main", MODE_PRIVATE);
        SharedPreferences.Editor editPref = pref.edit();
        editPref.putString("APIlogin", APIloginEditText.getText().toString());
        editPref.putString("APIpass", APIpassEditText.getText().toString());
        editPref.putString("FTPserver", ftpServerEditText.getText().toString());
        editPref.putString("FTPlogin", ftpLoginEditText.getText().toString());
        editPref.putString("FTPpass", ftpPasswordEditText.getText().toString());
        editPref.commit();
        if(logicButtonAdd()==true){
            buttonAddImage.setEnabled(true);
            textViewCheckFTP.setText("Ок. Настройки сохранены");
        }
        else {
            buttonAddImage.setEnabled(false);
            textViewCheckFTP.setText("Заполните все поля");
        }

    }
    public void onClickAddImages(View view) {
        Intent intent = new Intent(MainActivity.this, ListActivity.class);
        startActivity(intent);
    }
    private boolean logicButtonAdd(){
        if(APIloginEditText.getText().length()!=0&&APIpassEditText.getText().length()!=0
                &&ftpServerEditText.getText().length()!=0&&ftpLoginEditText.getText().length()!=0
                &&ftpPasswordEditText.getText().length()!=0){
            return true;
        }
        return false;
    }

}
