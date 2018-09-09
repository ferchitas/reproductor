package com.apps.ferchu.reproductor;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class ActividadInicial extends AppCompatActivity {

    private static final int PETICION_DE_PERMISOS = 1;

    private Playlist playlist;

    private ListView listView;
    private ArrayAdapter<String> adaptador;
    private ImageButton reproducir;
    private ImageButton siguiente;
    private ImageButton anterior;
    TextView nombreCancion;

    MediaPlayer mediaPlayer;

    private PlayService playService;
    boolean serviceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayService.LocalBinder binder = (PlayService.LocalBinder) service;
            playService = binder.getService();
            serviceBound = true;

            Toast.makeText(ActividadInicial.this, "Servicio enlazado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_inicial);

        if(ContextCompat.checkSelfPermission(ActividadInicial.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(ActividadInicial.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PETICION_DE_PERMISOS);
        }
        else {

            //se queda aqui
            inicializarIU();
            //se mueve al servicio
            obtenerMusica();
            mostrarCancionesEnLista();
            crearMediaPlayer();
            hacerCosas();
        }
    }

    private void inicializarIU() {

        listView = (ListView) findViewById(R.id.lvCanciones);
        nombreCancion = findViewById(R.id.tsNombreCancion);
        reproducir = (ImageButton) findViewById(R.id.btReproducir);
        siguiente = (ImageButton) findViewById(R.id.btSiguiente);
        anterior = (ImageButton) findViewById(R.id.btAnterior);
        //Se mueve al servicio
        playlist = new Playlist(0);
    }

    public void obtenerMusica(){

        ContentResolver contentResolver = getContentResolver();
        Uri cancionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cancionCursor = contentResolver.query(cancionUri, null, null, null, "RANDOM()");

        if(cancionCursor != null && cancionCursor.moveToFirst()) {

            int cancionTitulo = cancionCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int cancionArtista = cancionCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int cancionRuta = cancionCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int cancionDuracion = cancionCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                String tituloActual = cancionCursor.getString(cancionTitulo);
                String artistaActual = cancionCursor.getString(cancionArtista);
                String rutaActual = cancionCursor.getString(cancionRuta);
                String duracionActual = cancionCursor.getString(cancionDuracion);

                Cancion cancion = new Cancion(tituloActual, artistaActual, duracionActual, rutaActual);
                playlist.getCanciones().add(cancion);

            } while (cancionCursor.moveToNext());
        }
    }

    private void crearMediaPlayer() {

        String ruta = playlist.obtenerRutasDeLasCanciones().get(playlist.getCancionActual());
        String tituloCancion = playlist.obtenerNombresDeLasCanciones().get(playlist.getCancionActual());
        mediaPlayer = MediaPlayer.create(ActividadInicial.this,  Uri.parse(ruta));
        nombreCancion.setText(tituloCancion);
    }

    public void hacerCosas(){

        listView.setOnItemClickListener(new ListaCancionesListener());
        reproducir.setOnClickListener(new BtRepoducir());
        siguiente.setOnClickListener(new BtSiguiente());
        anterior.setOnClickListener( new BtAnterior());
        mediaPlayer.setOnCompletionListener(new FinDeCancion());
    }

    private void mostrarCancionesEnLista() {
        
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playlist.obtenerNombresYArtistasDeLasCanciones());
        listView.setAdapter(adaptador);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){

            case PETICION_DE_PERMISOS:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(ContextCompat.checkSelfPermission(ActividadInicial.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(this, "Permiso obtenido", Toast.LENGTH_SHORT).show();
                        hacerCosas();
                    }
                    else {
                        Toast.makeText(this, "Permiso no obtenido", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
            }
        }
    }

    private void checkInicioArray() {

        if(playlist.getCancionActual() >= playlist.getCanciones().size() - 1) {
            playlist.setCancionActual(0);
        }
        else {
            playlist.setCancionActual(playlist.getCancionActual() + 1);
        }
    }

    private void pasarDeCancion(MediaPlayer mediaPlayer) {
        String ruta = playlist.obtenerRutasDeLasCanciones().get(playlist.getCancionActual());
        String tituloCancion = playlist.obtenerNombresYArtistasDeLasCanciones().get(playlist.getCancionActual());
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(ruta);
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        nombreCancion.setText(tituloCancion);
        mediaPlayer.start();
    }

    class FinDeCancion implements MediaPlayer.OnCompletionListener {


        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

            if(mediaPlayer != null) {

                checkInicioArray();
                pasarDeCancion(mediaPlayer);
            }
        }
    }

    class BtAnterior extends ImagenBoton {

        @Override
        public void onClick(View v) {

            if(mediaPlayer != null) {

                checkFinalArray();
                pasarDeCancion(mediaPlayer);
            }
        }

        private void checkFinalArray() {
            if(playlist.getCancionActual() - 1 < 0) {
                playlist.setCancionActual(playlist.getCanciones().size() - 1);
            }
            else {
                playlist.setCancionActual(playlist.getCancionActual() - 1);
            }
        }
    }

    class BtSiguiente extends ImagenBoton {

        @Override
        public void onClick(View view) {

            if(mediaPlayer != null) {

                checkInicioArray();
                pasarDeCancion(mediaPlayer);
            }
        }
    }

    class BtRepoducir extends ImagenBoton {

        @Override
        public void onClick(View view) {

            if(mediaPlayer != null && mediaPlayer.isPlaying()) {

                mediaPlayer.pause();
            }
            else {

                mediaPlayer.start();
            }
        }
    }

    private void playAudio(String media) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, PlayService.class);
            playerIntent.putExtra("media", media);
            playService.
                    startService(playerIntent);
        } else {

            Toast.makeText(ActividadInicial.this, "El servicio no esta disponible", Toast.LENGTH_SHORT).show();
        }
    }

    class ListaCancionesListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            String cancion = (String) playlist.obtenerNombresYArtistasDeLasCanciones().get(i);
            String ruta = (String) playlist.obtenerRutasDeLasCanciones().get(i);

            if (mediaPlayer != null) {

                mediaPlayer.release();
            }
            playlist.setCancionActual(i);
            mediaPlayer = MediaPlayer.create(ActividadInicial.this, Uri.parse(ruta));
            nombreCancion.setText(cancion);
            mediaPlayer.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            playService.stopSelf();
        }
    }

}