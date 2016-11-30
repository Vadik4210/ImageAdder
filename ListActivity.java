package ua.com.curl.web.imageAdder;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by Vadik on 26.10.2016.
 */

public class ListActivity extends AppCompatActivity {
    public static final String MY_JSON = "MY_JSON";
    private static final String JSON_URL = "http://site.com/json.php?limit=";
    private String[] catNames;
    private JSONArray productsArrayJSON;
    private JSONObject jsonProduct;
    private ListView listProducts;
    private Button buttonListLoad;
    private Button buttonNext100;
    private Button buttonPreview100;
    private JSONObject dataJsonObj = null;
    private Spinner spinnerImagesSize;
    private static int limit = 0;
    private TextView textViewlimit;
    private String[] data = {"1", "2", "3", "4", "5"};
    private int fotoSpinner = 1;
    private int selectionList = 0;
    private Intent intent;
    static final private int CHOOSE_THIEF = 10;
    private ArrayList<Product> products = new ArrayList<Product>();
    private ArrayAdapter adapterProducts;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ua.com.curl.web.imageAdder.R.layout.actiity_list);
        createView();
    }
    private void createView(){
        new ParseTask().execute();
        buttonListLoad = (Button) findViewById(ua.com.curl.web.imageAdder.R.id.onList);
        textViewlimit = (TextView) findViewById(ua.com.curl.web.imageAdder.R.id.textViewlimit);
        textViewlimit.setText(limit + "");
        buttonNext100 = (Button) findViewById(ua.com.curl.web.imageAdder.R.id.buttonNext100);
        buttonPreview100 = (Button) findViewById(ua.com.curl.web.imageAdder.R.id.buttonPreview100);
        buttonPreview100.setEnabled(false);
        listProducts = (ListView) findViewById(ua.com.curl.web.imageAdder.R.id.idActiitylist);
        adapterProducts = new ProductAdapter(this);
        spinnerImagesSize = (Spinner) findViewById(ua.com.curl.web.imageAdder.R.id.spinnerImagesSize);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        spinnerImagesSize.setAdapter(adapter);
        spinnerImagesSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getBaseContext(), "Будет загружено " + (i + 1) + " фото", Toast.LENGTH_SHORT).show();
                fotoSpinner = (i + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (isOnline()) {
            buttonListLoad.setEnabled(true);
        }
        else{
            buttonListLoad.setEnabled(false);
            Toast toast = Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_SHORT);
            toast.show();
        }

        listProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                try {
                    jsonProduct = productsArrayJSON.getJSONObject(position);
                    selectionList = position;
                    Integer idInt = Integer.parseInt(jsonProduct.getString("id"));
                    Log.d("LOggggggggg ", idInt + "");
                    intent = new Intent(ListActivity.this, PhotoActivity.class);
                    intent.putExtra("idProd", jsonProduct.getString("id"));
                    intent.putExtra("nameProd", jsonProduct.getString("name"));
                    intent.putExtra("countFotoProd", "");
                    intent.putExtra("fotoSpinner", fotoSpinner);
                    startActivityForResult(intent, CHOOSE_THIEF);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void onClickList(View view) {
        adapterProducts = new ProductAdapter(this);
        listProducts.setAdapter(adapterProducts);
        if (limit == 0) {
            buttonPreview100.setEnabled(false);
        }
    }

    public void onClickNext100(View view) {
        buttonPreview100.setEnabled(true);
        int i = limit;
        limit = i + 100;
        textViewlimit.setText("" + limit);
        new ParseTask().execute();
    }

    public void onClickPrev100(View view) {
        int i = limit;
        limit = i - 100;
        textViewlimit.setText("" + limit);
        new ParseTask().execute();
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {
        private HttpURLConnection urlConnection = null;
        private BufferedReader reader = null;
        private String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(JSON_URL + limit);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            products.clear();
            try {
                dataJsonObj = new JSONObject(strJson);
                productsArrayJSON = dataJsonObj.getJSONArray("resultProd");
                catNames = new String[productsArrayJSON.length()];
                for (int i = 0; i < productsArrayJSON.length(); i++) {
                    JSONObject productJSON = productsArrayJSON.getJSONObject(i);
                    products.add(new Product(productJSON.getString("name"),""));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_THIEF) {
            if (resultCode == RESULT_OK) {
                int photoCount = data.getIntExtra(PhotoActivity.PHOTO_COUNT,0);
                products.set(selectionList, new Product(products.get(selectionList).getName(), photoCount+" фото"));
                listProducts.setAdapter(adapterProducts);
                listProducts.setSelection(selectionList);
            }else {

            }
        }
    }
    private static class Product {
        private   String name;
        private   String countPhoto;
        public Product(String name, String countPhoto) {
            this.name = name;
            this.countPhoto = countPhoto;
        }
        public String getName() {
            return name;
        }
        public String getCountPhoto() {
            return countPhoto;
        }
        public void setCountPhoto(String countPhoto) {
            this.countPhoto = countPhoto;
        }
        public void setName(String name) {
            this.name = name;
        }


    }
    private class ProductAdapter extends ArrayAdapter<Product> {

        public ProductAdapter(Context context) {
            super(context, ua.com.curl.web.imageAdder.R.layout.list_adapter_products, products);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Product product = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(ua.com.curl.web.imageAdder.R.layout.list_adapter_products, null);
            }
            ((TextView) convertView.findViewById(ua.com.curl.web.imageAdder.R.id.textViewName))
                    .setText(product.name);
            ((TextView) convertView.findViewById(ua.com.curl.web.imageAdder.R.id.textViewPhoto))
                    .setText(product.countPhoto);
            return convertView;
        }
    }

}
