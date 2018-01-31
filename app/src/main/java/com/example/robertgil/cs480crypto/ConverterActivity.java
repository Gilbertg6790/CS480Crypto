package com.example.robertgil.cs480crypto;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConverterActivity extends AppCompatActivity {

    EditText fromText;
    TextView toText;
    Button convertButton;
    Button refreshButton;
    ProgressBar progressBar;
    TextView responseView;
    Spinner spinnerFrom;
    Spinner spinnerTo;

    final String API_URL = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=BTC,USD,EUR";
    static double[] rates = new double[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);
        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);
        convertButton = findViewById(R.id.convertButton);
        refreshButton = findViewById(R.id.refreshButton);
        progressBar = findViewById(R.id.progressBar);
        responseView = findViewById(R.id.responseView);
        spinnerFrom = findViewById(R.id.spinner1);
        spinnerTo = findViewById(R.id.spinner2);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fromText.getText().toString().equals("")) {
                    Double fromVal = Double.parseDouble(fromText.getText().toString());
                    String fromCoin = spinnerFrom.getSelectedItem().toString();
                    String toCoin = spinnerTo.getSelectedItem().toString();
                    int fromIndex = spinnerFrom.getSelectedItemPosition();
                    int toIndex = spinnerTo.getSelectedItemPosition();

                    Toast.makeText(ConverterActivity.this, "From: " + fromCoin + "\n" +
                            "To: " + toCoin, Toast.LENGTH_SHORT).show();

                    Double result;
                    // Calc val from Coin to USD
                    result = fromVal / rates[fromIndex];
                    // Calc val USD to Coin
                    result = result * rates[toIndex];
                    // Assign result to toVal
                    result = Math.round(result * 100.0) / 100.0;
                    toText.setText(result.toString());

                } else {
                    Toast.makeText(ConverterActivity.this, "Empty \"From\" value.\nPut something there!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RetrieveFeedTask().execute();
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {
        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        @Override
        protected String doInBackground(Void... urls) {
            String fsym = "USD";
            String tsym = "EUR,BTC";

            try {
                URL url = new URL(API_URL + "fsym=" + fsym + "&" +
                        "tsyms=" + tsym);
                HttpURLConnection urlCOnnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlCOnnection.getInputStream()));
                    StringBuilder strBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] sample = line.split(",");
                        for (int i = 0; i < sample.length; i++) {
                            String[] coinEntry = sample[i].split(":");
                            Log.i("CoinRates from internet", coinEntry[0].replace("{", "") + " " + coinEntry[1].replace("}", ""));
                            rates[i + 1] = Double.parseDouble(coinEntry[1].replace("}", ""));
                        }
                        strBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return strBuilder.toString();
                } finally {
                    urlCOnnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "Update Failed.";
            }
            progressBar.setVisibility(View.GONE);
            Toast.makeText(ConverterActivity.this, "Refreshed Rates!", Toast.LENGTH_SHORT).show();
        }
    }
}

