package com.aluracursos.screenmatch.service;

import com.aluracursos.screenmatch.modelo.DatosSeries;

public interface IConvierteDatos {

    <T> T obtenerDatos(String json, Class<T> clase);

}
