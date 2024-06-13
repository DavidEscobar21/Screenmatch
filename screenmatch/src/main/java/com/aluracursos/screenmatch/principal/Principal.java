package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.modelo.DatosEpisodio;
import com.aluracursos.screenmatch.modelo.DatosSeries;
import com.aluracursos.screenmatch.modelo.DatosTemporada;
import com.aluracursos.screenmatch.modelo.Episodio;
import com.aluracursos.screenmatch.service.ConsumoApi;
import com.aluracursos.screenmatch.service.ConvierteDatos;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner sc = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=e5c8aad";
    private ConvierteDatos convierteDatos = new ConvierteDatos();

    public void muestraElMenu() {

        System.out.println("Por favor escribe el nombre de la serie que deseas buscar: ");
        var nombreSerie = sc.nextLine();
        // Busca los datos generales de la serie
        var Json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        var datos = convierteDatos.obtenerDatos(Json, DatosSeries.class);
        System.out.println(datos);

        // Datos de las temporadas
        List<DatosTemporada> datosTemporadas = new ArrayList<>();
        for (int i = 1; i <= datos.totalDeTemporadas(); i++) {
            Json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + "&Season=" + i + API_KEY);
            var temporadas = convierteDatos.obtenerDatos(Json, DatosTemporada.class);
            datosTemporadas.add(temporadas);
        }
        // datosTemporadas.forEach(System.out::println);

      /*  for (int i = 0; i < datosTemporadas.size(); i++) {
            List<DatosEpisodio> datosEpisodios = datosTemporadas.get(i).episodios();

            for (int j = 0; j < datosEpisodios.size(); j++) {
                System.out.println(datosEpisodios.get(j).titulo());
            }*/
        datosTemporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        // Convertir todas las informaciones a una lista del tipo DatosEpisodio

        List<DatosEpisodio> datosEpisodios = datosTemporadas
                .stream().flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());




        // Top 5 episodios
        System.out.println("Top 5 Episodios");
        datosEpisodios.stream()
                .filter(t -> !t.evaluacion().equalsIgnoreCase("N/A"))
                .peek(t -> System.out.println("Primer filtro (N/A) "+t))
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
                .peek(t -> System.out.println("Segundo ordencación (M>m) "+t))
                .map(e -> e.titulo().toUpperCase())
                .peek(t -> System.out.println("Tercer filtro Mayusculas(m>M) "+t))
                .limit(5)
                .forEach(System.out::println);





        // Convirtiendo los dados a una lista del tipo Episodio
        List<Episodio> episodios = datosTemporadas.stream()
                        .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(),d)))
                        .collect(Collectors.toList());
/*
        episodios.forEach(System.out::println);

        // Busqueda de episodios por fecha
        System.out.println("Ingrese el año a partir del cual deseas ver los episodios");
        var fecha = sc.nextInt();
        sc.nextLine();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fechaBusqueda = LocalDate.of(fecha, 1,1 );

        episodios
                .stream()
                .filter(t -> t.getFechaDeLanzamiento() != null && t.getFechaDeLanzamiento().isAfter(fechaBusqueda) )
                .forEach(e -> System.out.println(
                        "Temporada: "+ e.getTemporada() +
                        " Episodio: "+ e.getTitulo() +
                        " Fecha: "+ e.getFechaDeLanzamiento().format(dtf)));

        // Busca episodios por pedazo del titulo
        System.out.println("Por favor escriba el titulo del episodio que desea ver:");
        var pedazoTitulo = sc.nextLine();

        Optional<DatosEpisodio> episodioBuscado = datosEpisodios.stream()
                .filter(t -> t.titulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
                .findFirst();

        if (episodioBuscado.isPresent()){
            System.out.println("Episodio Encontrado");
            System.out.println("Los datos son: " + episodioBuscado.get());
        }else {
            System.out.println("Episodio No Encontrado");
        }*/

        Map<Integer, Double> evaluacionesPorTemporada = episodios
                .stream()
                .filter(t -> t.getEvaluacion()>0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println(evaluacionesPorTemporada);

        DoubleSummaryStatistics est = episodios
                .stream()
                .filter(t -> t.getEvaluacion()>0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));

        System.out.println(est);
        System.out.println("Media de las evaluaciones: "+ est.getAverage());
        System.out.println("Episodio mejor evaluado: "+ est.getMax());
        System.out.println("Episodio peor evaluado: "+ est.getMin());

    }

}
