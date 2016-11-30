package ua.com.curl.web.imageAdder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mikelau.croperino.Croperino;
import com.mikelau.croperino.CroperinoConfig;
import com.mikelau.croperino.CroperinoFileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import android.content.Context;



public class PhotoActivity extends AppCompatActivity {
    private ImageView imageView;
    private String folderToSave = Environment.getExternalStorageDirectory().toString();
    private Uri uriImageCrop;
    private int countImages = 0;
    private TextView textViewCount;
    private TextView textViewId;
    private TextView textViewNameProd;
    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private ContentValues contentValues;
    private Cursor cursor;
    private Integer idProd;
    private String nameProd;
    private int fotoCount = 1;
    private Button buttonSave;
    private Button buttonUpload;
    private String imagename;
    private ProgressDialog mDialog;
    private int fotoAllcount;
    private String ApiLogin;
    private String ApiPass;
    private Context context;
    private ProgressBar progressBar;
    public final static String PHOTO_COUNT = "position";
    private GoogleApiClient client2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ua.com.curl.web.imageAdder.R.layout.activity_photo);
        createPhotoView();
    }
    private void createPhotoView(){
        textViewId = (TextView) findViewById(ua.com.curl.web.imageAdder.R.id.textViewId);
        Intent intent = getIntent();
        String id = intent.getStringExtra("idProd");
        idProd = Integer.parseInt(id);
        nameProd = intent.getStringExtra("nameProd");
        fotoCount = intent.getIntExtra("fotoSpinner", 1);
        new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/MikeLau/Pictures", "/sdcard/MikeLau/Pictures");
        CroperinoFileUtil.verifyStoragePermissions(PhotoActivity.this);
        CroperinoFileUtil.setupDirectory(PhotoActivity.this);
        dbHelper = new DBHelper(this);
        imageView = (ImageView) findViewById(ua.com.curl.web.imageAdder.R.id.imageView);
        textViewCount = (TextView) findViewById(ua.com.curl.web.imageAdder.R.id.textViewCount);
        textViewCount.setText("К-во " + countImages);
        textViewNameProd = (TextView) findViewById(ua.com.curl.web.imageAdder.R.id.textViewNameProd);
        textViewId.setText("id " + idProd + "\n" + fotoCount + " фото");
        textViewNameProd.setText(nameProd);
        buttonSave = (Button) findViewById(ua.com.curl.web.imageAdder.R.id.buttonSave);
        buttonUpload = (Button) findViewById(ua.com.curl.web.imageAdder.R.id.buttonUpload);
        buttonSave.setEnabled(false);
        database = dbHelper.getWritableDatabase();
        contentValues = new ContentValues();
        textViewCount.setText("Фото:\n" + countSQLiteRows());
        uploadButtonEnabled();
        SharedPreferences pref = getSharedPreferences("main", MODE_PRIVATE);
        ApiLogin = pref.getString("APIlogin", "").toString();
        ApiPass = pref.getString("APIpass", "").toString();
        context = getApplicationContext();
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        progressBar = (ProgressBar) findViewById(ua.com.curl.web.imageAdder.R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
    }
    public void onClickCamera(View view) {
        try {
            imageViewClear();
            Croperino.prepareCamera(PhotoActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickGallery(View view) {
        imageViewClear();
        Croperino.prepareGallery(PhotoActivity.this
        );
    }

    public void onClickUpLoad(View view) {
        if (isOnline()) {
            readDb();
            imageViewClear();
            Toast toastUploadOK = Toast.makeText(getApplicationContext(), "Загружено " + fotoAllcount + " фото", Toast.LENGTH_SHORT);
            toastUploadOK.show();
            database.delete(DBHelper.TABLE_CONTACTS, null, null);
            database.close();
            countImages = 0;
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onClickSave(View view) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImageCrop);
            contentValues.put(DBHelper.KEY_POSITION, countImages);
            contentValues.put(DBHelper.KEY_ID_PRODUCT, idProd);
            imagename = savePicture(bitmap, folderToSave, idProd, countImages++);
            contentValues.put(DBHelper.KEY_NAME_PHOTO, imagename);
            database.insert(DBHelper.TABLE_CONTACTS, null, contentValues);
            textViewCount.setText("Фото:\n" + countSQLiteRows());
            toastShow("Фото " + imagename + " записано");
            if (fotoCount == countImages) {
                Intent answerIntent = new Intent();
                answerIntent.putExtra(PHOTO_COUNT, getIntent().getIntExtra("position", countImages));
                setResult(RESULT_OK, answerIntent);
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        uploadButtonEnabled();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CroperinoConfig.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), PhotoActivity.this, true, 1, 1, 0, 0);
                    buttonSave.setEnabled(true);
                }
                break;
            case CroperinoConfig.REQUEST_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    CroperinoFileUtil.newGalleryFile(data, PhotoActivity.this);
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), PhotoActivity.this, true, 1, 1, 0, 0);
                    buttonSave.setEnabled(true);
                }
                break;
            case CroperinoConfig.REQUEST_CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getmFileTemp());
                    imageView.setImageURI(i);
                    uriImageCrop = i;
                    buttonSave.setEnabled(true);
                }
                break;
            default:
                break;
        }
    }

    private void imageViewClear() {
        imageView.setImageResource(0);
    }

    private String savePicture(Bitmap bitmap, String folderToSave, int idProd, int position) {
        OutputStream fOut = null;
        Time time = new Time();
        time.setToNow();
        String fileName = "imgCrop_" + "id_" + idProd + "_" + position + ".jpg";
        File file;
        try {
            file = new File(folderToSave, fileName);
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return fileName;
    }

    private void readDb() {
        mDialog=new ProgressDialog(this);
        mDialog.setMessage("Загрузка");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setMax(countSQLiteRows());
        mDialog.show();
        Thread myThready = new Thread(new Runnable() {
            public void run() //Этот метод будет выполняться в побочном потоке
            {
                cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                    int namePhotoIndex = cursor.getColumnIndex(DBHelper.KEY_NAME_PHOTO);
                    int positionIndex = cursor.getColumnIndex(DBHelper.KEY_POSITION);
                    int idProcurtIndex = cursor.getColumnIndex(DBHelper.KEY_ID_PRODUCT);
                    int counter = 0;
                    do {
                        uploadFTP(cursor.getString(namePhotoIndex));
                        String idProdut = cursor.getInt(idProcurtIndex) + "";
                        mDialog.setProgress(counter);
                        counter++;
                    } while (cursor.moveToNext());
                    cursor.close();
                } else {
                    Log.d("mLog", "0 rows");
                }
                mDialog.dismiss();
            }
        });
        myThready.start();
    }

    private void toastShow(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void uploadFTP(final String imageName) {

        SharedPreferences pref = getSharedPreferences("main", MODE_PRIVATE);
        String server = pref.getString("FTPserver", "").toString();
        String login = pref.getString("FTPlogin", "").toString();
        String pass = pref.getString("FTPpass", "").toString();
        FTPClient client = new FTPClient();
        try {
            client.connect(server);
            client.login(login, pass);
            client.changeDirectory("/files/originals/");
            File fileUpload = new File(folderToSave + "/" + imageName);
            client.setType(FTPClient.TYPE_BINARY);
            client.setPassive(true);
            client.noop();
            client.upload(fileUpload);
            client.disconnect(true);
        } catch (FTPException | FileNotFoundException | FTPDataTransferException | FTPAbortedException | FTPIllegalReplyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int countSQLiteRows() {
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);
        int i = cursor.getCount();
        fotoAllcount = i;
        cursor.close();
        return i;
    }

    private void uploadButtonEnabled() {
        if (countSQLiteRows() == 0)
            buttonUpload.setEnabled(false);
        else {
            buttonUpload.setEnabled(true);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


}
