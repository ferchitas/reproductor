package com.apps.ferchu.reproductor;

import java.util.ArrayList;

/**
 * Created by ferch on 12/03/2018.
 */

public class Playlist {

    private ArrayList<Cancion> canciones;
    private int cancionActual;

    public Playlist(int cancionActual) {

        this.setIndiceCancionActual(cancionActual);
        this.setCanciones(new ArrayList<Cancion>());
    }

    public ArrayList<String> obtenerNombresDeLasCanciones() {

        ArrayList nombres = new ArrayList<String>();
        for (Cancion cancion:(ArrayList<Cancion>) this.getCanciones()){

            nombres.add(cancion.getNombre());
        }
        return nombres;
    }

    public ArrayList<String> obtenerArtistasDeLasCanciones() {

        ArrayList artistas = new ArrayList<String>();
        for (Cancion cancion:(ArrayList<Cancion>) this.getCanciones()){

            artistas.add(cancion.getArtista());
        }
        return artistas;
    }

    public ArrayList<String> obtenerNombresYArtistasDeLasCanciones() {

        ArrayList nombresYArtistas = new ArrayList<String>();
        for (Cancion cancion:(ArrayList<Cancion>) this.getCanciones()){

            nombresYArtistas.add(cancion.getNombre() + "\n" + cancion.getArtista());
        }
        return nombresYArtistas;
    }

    public ArrayList<String> obtenerRutasDeLasCanciones() {

        ArrayList rutas = new ArrayList<String>();
        for (Cancion cancion:(ArrayList<Cancion>) this.getCanciones()){

            rutas.add(cancion.getRuta());
        }
        return rutas;
    }

    public Cancion obtenerCancionActual(){ return (Cancion) canciones.get(cancionActual);}

    public ArrayList getCanciones() {
        return canciones;
    }

    public void setCanciones(ArrayList canciones) {
        this.canciones = canciones;
    }

    public int getIndiceCancionActual() {
        return cancionActual;
    }

    public void setIndiceCancionActual(int cancionActual) {
        this.cancionActual = cancionActual;
    }
}
