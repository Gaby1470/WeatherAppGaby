package com.example.weatherapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.network.Network;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String currentLocation;
    ImageView imageWeather;
    TextView textLocation;
    private LocationManager locationManager;

    final public String TAG = "GABY DEBUGGING";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        @Override
//        public void onLocationChanged(Location location) {
//            // Get the city name from the location and update the text view
//            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//            try {
//                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                if (addresses.size() > 0) {
//                    String cityName = addresses.get(0).getLocality();
//                    textLocation = binding.locationText;
//                    textLocation.setText(cityName);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//        @Override
//        public void onProviderEnabled(String provider) {}
//
//        @Override
//        public void onProviderDisabled(String provider) {}


        imageWeather = binding.weatherImage;
        currentLocation = intent.getStringExtra("LATITUDE") + "," + intent.getStringExtra("LONGITUDE");

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(Network.openWeatherAPI + "current.json?keys" + Network.openWeatherAPI +
                        "&aqi=no&q=" + currentLocation)
                .build();

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>(){
            @Override
            protected  String doInBackground(Void...params){
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }

            }
            @Override
            protected  void onPostExecute(String s){
                super.onPostExecute(s);
                if (s != null) {
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(s);
                        JSONObject locationObject = jsonResponse.getJSONObject("location");
                        binding.locationText.setText(locationObject.getString("name"));

                        JSONObject currentObj = jsonResponse.getJSONObject("current");
                        String currentTemp = String.valueOf(Math.round(Double.parseDouble(currentObj.getString("temp_c"))));
                        binding.currentText.setText(currentTemp + "°C");

                        JSONObject forecastObj = jsonResponse.getJSONObject("forecast");
                        JSONArray forecastDayObj = forecastObj.getJSONArray("forecastday");
                        JSONObject dayObject = forecastDayObj.getJSONObject(0).getJSONObject("day");

                        String minTemp = String.valueOf(Math.round(Double.parseDouble(dayObject.getString("mintemp_c"))));
                        binding.minimumText.setText("min: " + minTemp + "°C");

                        String maxTemp = String.valueOf(Math.round(Double.parseDouble(dayObject.getString("maxtemp_c"))));
                        binding.maximumText.setText("max: " + maxTemp + "°C");

                        JSONObject condition = currentObj.getJSONObject("condition");
                        String image_url =  "https:" + condition.getString("icon");

                        Picasso.get().load(image_url).resize(100, 100).into(binding.weatherImage);

                    } catch (JSONException e){
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        asyncTask.execute();
    }
}