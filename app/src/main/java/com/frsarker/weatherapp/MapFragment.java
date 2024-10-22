package com.frsarker.weatherapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng cityLocation;

    public MapFragment(LatLng cityLocation) {
        this.cityLocation = cityLocation; // Inicializa com a localização da cidade
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Não carregue o mapa aqui, isso será feito no onCreateView
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Carregar o mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation(cityLocation); // Atualiza o mapa com a localização inicial
    }

    // Método para atualizar o mapa com novas coordenadas
    public void updateMapLocation(LatLng newLocation) {
        if (mMap != null) {
            mMap.clear(); // Limpa marcadores antigos
            mMap.addMarker(new MarkerOptions().position(newLocation).title("Cidade Selecionada"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 10)); // Move a câmera para a nova localização
        }
    }
}
