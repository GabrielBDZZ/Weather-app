package com.frsarker.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {

    private String CITY = "sao paulo,br"; // Cidade padrão
    private final String API = "8118ed6ee68db2debfaaa5a44c832918"; // API Key
    private MainActivity mainActivity; // Referência à MainActivity
    private LinearLayout qrButton;

    public WeatherFragment(MainActivity activity) {
        this.mainActivity = activity; // Inicializa a referência
    }

    // Definição dos TextViews
    private TextView addressTxt, updated_atTxt, statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt,
            sunsetTxt, windTxt, pressureTxt, humidityTxt;
    private View loader, mainContainer, errorText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);

        // Inicializa os TextViews
        initializeViews(rootView);

        qrButton = rootView.findViewById(R.id.qr);

        // Campo de entrada para a cidade
        EditText cityInput = rootView.findViewById(R.id.cityInput);
        cityInput.setHint("Digite a cidade");

        // Configura o listener para a tecla
        cityInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                String inputCity = cityInput.getText().toString();
                if (!inputCity.isEmpty()) {
                    CITY = inputCity; // Atualiza a cidade
                    new WeatherTask().execute(); // Executa a busca
                    hideKeyboard(v); // Oculta o teclado
                    return true; // Indica que a ação foi tratada
                }
            }
            return false; // Não tratou a ação
        });

        // Botão de pesquisa
        Button searchButton = rootView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            String inputCity = cityInput.getText().toString();
            if (!inputCity.isEmpty()) {
                CITY = inputCity; // Atualiza a cidade se o campo não estiver vazio
                new WeatherTask().execute(); // Executa a busca
                hideKeyboard(v); // Oculta o teclado
            }
        });

        // Inicia a requisição de clima ao criar o fragmento
        new WeatherTask().execute();

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicializar o QR code scanner
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(WeatherFragment.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);  // Definir o tipo de código
                integrator.setPrompt("Escaneie o QR code");                     // Texto exibido ao usuário
                integrator.setCameraId(0);                                      // Selecionar a câmera
                integrator.setBeepEnabled(true);                                // Som ao escanear
                integrator.initiateScan();                                      // Iniciar o scanner
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                String qrCodeData = result.getContents();
                // Tratar o resultado do QR code escaneado (ex: alterar a cidade com base no QR code)
                // Você pode usar a String `qrCodeData` para atualizar a cidade
            }
        }
    }

    private void initializeViews(View view) {
        addressTxt = view.findViewById(R.id.address);
        updated_atTxt = view.findViewById(R.id.updated_at);
        statusTxt = view.findViewById(R.id.status);
        tempTxt = view.findViewById(R.id.temp);
        temp_minTxt = view.findViewById(R.id.temp_min);
        temp_maxTxt = view.findViewById(R.id.temp_max);
        sunriseTxt = view.findViewById(R.id.sunrise);
        sunsetTxt = view.findViewById(R.id.sunset);
        windTxt = view.findViewById(R.id.wind);
        pressureTxt = view.findViewById(R.id.pressure);
        humidityTxt = view.findViewById(R.id.humidity);

        loader = view.findViewById(R.id.loader);
        mainContainer = view.findViewById(R.id.mainContainer);
        errorText = view.findViewById(R.id.errorText);
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Exibe o carregador e oculta a interface principal
            loader.setVisibility(View.VISIBLE);
            mainContainer.setVisibility(View.GONE);
            errorText.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... args) {
            // Faz a requisição para a API
            return HttpRequest.excuteGet(
                    "https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric&appid=" + API
            );
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    updateUI(jsonObj);

                    // Atualiza o mapa com as coordenadas da nova cidade
                    double latitude = jsonObj.getJSONObject("coord").getDouble("lat");
                    double longitude = jsonObj.getJSONObject("coord").getDouble("lon");
                    mainActivity.updateMapForCity(latitude, longitude); // Chama o método para atualizar o mapa

                } catch (JSONException e) {
                    e.printStackTrace();
                    showError();
                }
            } else {
                showError();
            }
        }

        private void updateUI(JSONObject jsonObj) throws JSONException {
            JSONObject main = jsonObj.getJSONObject("main");
            JSONObject sys = jsonObj.getJSONObject("sys");
            JSONObject wind = jsonObj.getJSONObject("wind");
            JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

            Long updatedAt = jsonObj.getLong("dt");
            String updatedAtText = "Atualizado em: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
            String temp = main.getString("temp") + "°C";
            String tempMin = "Temp Min: " + main.getString("temp_min") + "°C";
            String tempMax = "Temp Max: " + main.getString("temp_max") + "°C";
            String pressure = main.getString("pressure");
            String humidity = main.getString("humidity");

            Long sunrise = sys.getLong("sunrise");
            Long sunset = sys.getLong("sunset");
            String windSpeed = wind.getString("speed");
            String weatherDescription = weather.getString("description");

            String address = jsonObj.getString("name") + ", " + sys.getString("country");

            // Atualiza os TextViews
            addressTxt.setText(address);
            updated_atTxt.setText(updatedAtText);
            statusTxt.setText(weatherDescription.toUpperCase());
            tempTxt.setText(temp);
            temp_minTxt.setText(tempMin);
            temp_maxTxt.setText(tempMax);
            sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
            sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
            windTxt.setText(windSpeed);
            pressureTxt.setText(pressure);
            humidityTxt.setText(humidity + '%');

            loader.setVisibility(View.GONE);
            mainContainer.setVisibility(View.VISIBLE);
        }

        private void showError() {
            loader.setVisibility(View.GONE);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    // Método para ocultar o teclado
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
