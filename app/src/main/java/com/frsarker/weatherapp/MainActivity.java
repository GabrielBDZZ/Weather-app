package com.frsarker.weatherapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LatLng cityLocation;  // Coordenadas da cidade obtida pela API
    private MapFragment mapFragment; // Referência ao MapFragment

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Definir as coordenadas da cidade carregada pela API (exemplo de coordenadas)
        cityLocation = new LatLng(-23.5505, -46.6333); // Coordenadas de São Paulo, por exemplo

        // Configurar o adaptador do ViewPager
        viewPager.setAdapter(new FragmentAdapter(this));

        // Conectar o TabLayout com o ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Previsão");
                    break;
                case 1:
                    tab.setText("Mapa");
                    break;
            }
        }).attach();
        ImageButton threeDotsButton = findViewById(R.id.threeDotsButton);
        threeDotsButton.setOnClickListener(v -> showPopup(v));
    }

    // Método para exibir o pop-up
    private void showPopup(View anchorView) {
        // Infla o layout do pop-up
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_profile, null);

        // Criar o PopupWindow
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // Exibe o popup à direita do botão de três pontos
        popupWindow.showAsDropDown(anchorView);

        // Fecha o pop-up ao clicar fora dele
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
    }

    // Método para atualizar o mapa com as coordenadas da cidade
    public void updateMapForCity(double latitude, double longitude) {
        cityLocation = new LatLng(latitude, longitude); // Atualiza as coordenadas

        if (mapFragment != null) {
            mapFragment.updateMapLocation(cityLocation); // Atualiza o mapa
        }
    }

    // Adaptador para alternar entre os fragmentos
    private class FragmentAdapter extends FragmentStateAdapter {
        public FragmentAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new WeatherFragment(MainActivity.this); // Passa a referência da MainActivity
                case 1:
                    mapFragment = new MapFragment(cityLocation); // Fragmento do mapa
                    return mapFragment;
                default:
                    return new WeatherFragment(MainActivity.this);
            }
        }

        @Override
        public int getItemCount() {
            return 2;  // Duas abas (Previsão e Mapa)
        }
    }
}
