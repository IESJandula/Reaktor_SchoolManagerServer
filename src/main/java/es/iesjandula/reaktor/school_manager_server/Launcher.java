package es.iesjandula.reaktor.school_manager_server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import es.iesjandula.reaktor.school_manager_server.generator.core.CreadorSesiones;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultados;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorResultadosParams;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreads;
import es.iesjandula.reaktor.school_manager_server.generator.core.manejadores.ManejadorThreadsParams;
import es.iesjandula.reaktor.school_manager_server.generator.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.generator.models.Profesor;
import es.iesjandula.reaktor.school_manager_server.generator.models.RestriccionHoraria;
import es.iesjandula.reaktor.school_manager_server.generator.models.Sesion;
import es.iesjandula.reaktor.school_manager_server.generator.models.enums.Conciliacion;
import es.iesjandula.reaktor.school_manager_server.generator.models.enums.TipoHorario;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;

public class Launcher
{
    public static void main(String[] args) throws SchoolManagerServerException
    {
        // Creamos una instancia de CreadorSesiones para añadir las asignatura y profesor a la sesión específica
        CreadorSesiones creadorSesiones = new CreadorSesiones() ;

        // Crear un mapa de correlacionador de cursos (esto debe ser sustituido por datos reales)
        Map<String, Integer> mapCorrelacionadorCursosMatutinos   = Map.of("2ESOB", 0, "3ESOB", 5, "4ESOB", 10); 
        Map<String, Integer> mapCorrelacionadorCursosVespertinos = Map.of("1DAW", 0, "2DAW", 5);

        // Basado en el último CSV, asignar sesiones a sesionesSinRestricciones
        Profesor profesorMatematicas     = new Profesor("profesor10@example.com", "MAN", "AMA", Conciliacion.ENTRAR_DESPUES_SEGUNDA_HORA);
        Profesor profesorHistoria        = new Profesor("profesor11@example.com", "LUI", "MED", Conciliacion.SALIR_ANTES_QUINTA_HORA);
        Profesor profesorFisicaQuimica   = new Profesor("profesor12@example.com", "ANA", "SAN", Conciliacion.SALIR_ANTES_QUINTA_HORA);
        Profesor profesorInformatica     = new Profesor("profesor13@example.com", "PAC", "BEN", Conciliacion.ENTRAR_DESPUES_SEGUNDA_HORA);
        Profesor profesorLengua          = new Profesor("profesor14@example.com", "JUA", "EXP", null);
        Profesor profesorReligion        = new Profesor("profesor15@example.com", "JER", "REL", null);
        Profesor profesorDibujo          = new Profesor("profesor16@example.com", "ANA", "COR", null);
        Profesor profesorEducacionFisica = new Profesor("profesor17@example.com", "ALV", "MAR", null);
        Profesor profesorFrances         = new Profesor("profesor19@example.com", "JES", "AST", null);
        Profesor profesorPes1            = new Profesor("profesor20@example.com", "JOS", "PRO", null) ;
        Profesor profesorSai1            = new Profesor("profesor21@example.com", "CAR", "SIS", null) ;
        Profesor profesorPes2            = new Profesor("profesor22@example.com", "JOF", "PRE", null) ;
        Profesor profesorSai2            = new Profesor("profesor23@example.com", "LAR", "SIE", null) ;

        Asignatura asignaturaMatematicas2esoB     = new Asignatura("MAT", "2ESOB", true, 5); // 5
        Asignatura asignaturaHistoria2esoB        = new Asignatura("HIS", "2ESOB", true, 5); // 10
        Asignatura asignaturaFisicaQuimica2esoB   = new Asignatura("FyQ", "2ESOB", true, 5); // 15
        Asignatura asignaturaInformatica2esoB     = new Asignatura("INF", "2ESOB", true, 3) ; // 18
        Asignatura asignaturaLengua2esoB          = new Asignatura("LEN", "2ESOB", true, 4); // 22
        Asignatura asignaturaReligion2esoB        = new Asignatura("REL", "2ESOB", true, 2); // 24
        Asignatura asignaturaDibujo2esoB          = new Asignatura("DIB", "2ESOB", true, 3); // 27
        Asignatura asignaturaEducacionFisica2esoB = new Asignatura("EDF", "2ESOB", true, 3); // 30
        Asignatura asignaturaFrances2esoB         = new Asignatura("FRA", "2ESOB", true, 3) ; // 33
        
        asignaturaInformatica2esoB.setBloqueOptativas(Arrays.asList(new Asignatura[] {asignaturaFrances2esoB})) ;
        asignaturaFrances2esoB.setBloqueOptativas(Arrays.asList(new Asignatura[] {asignaturaInformatica2esoB})) ;

        List<RestriccionHoraria> restriccionesHorariasReligion2esoB = new ArrayList<RestriccionHoraria>() ;
        restriccionesHorariasReligion2esoB.add(new RestriccionHoraria.Builder(mapCorrelacionadorCursosMatutinos.get(asignaturaReligion2esoB.getCursoEtapaGrupo()))
                                                                     .asignarUnDiaConcreto(Constants.DIA_SEMANA_LUNES)
        	                                                         .build()) ;
        restriccionesHorariasReligion2esoB.add(new RestriccionHoraria.Builder(mapCorrelacionadorCursosMatutinos.get(asignaturaReligion2esoB.getCursoEtapaGrupo()))
                                                                     .asignarUnDiaTramoConcreto(Constants.DIA_SEMANA_MARTES, Constants.TRAMO_HORARIO_SEGUNDA_HORA)
        	                                                         .build()) ;

        creadorSesiones.crearSesiones(asignaturaMatematicas2esoB, profesorMatematicas, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaHistoria2esoB, profesorHistoria, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaFisicaQuimica2esoB, profesorFisicaQuimica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaInformatica2esoB, profesorInformatica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaLengua2esoB, profesorLengua, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaReligion2esoB, profesorReligion, TipoHorario.MATUTINO, restriccionesHorariasReligion2esoB);
        creadorSesiones.crearSesiones(asignaturaDibujo2esoB, profesorDibujo, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaEducacionFisica2esoB, profesorEducacionFisica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaFrances2esoB, profesorFrances, TipoHorario.MATUTINO, null);

        Asignatura asignaturaMatematicas3esoB     = new Asignatura("MAT", "3ESOB", true, 5) ; // 5
        Asignatura asignaturaHistoria3esoB        = new Asignatura("HIS", "3ESOB", true, 5) ; // 10
        Asignatura asignaturaFisicaQuimica3esoB   = new Asignatura("FyQ", "3ESOB", true, 5) ; // 15
        Asignatura asignaturaInformatica3esoB     = new Asignatura("INF", "3ESOB", true, 3) ; // 18
        Asignatura asignaturaLengua3esoB          = new Asignatura("LEN", "3ESOB", true, 4) ; // 22
        Asignatura asignaturaReligion3esoB        = new Asignatura("REL", "3ESOB", true, 2) ; // 24
        Asignatura asignaturaDibujo3esoB          = new Asignatura("DIB", "3ESOB", true, 3) ; // 27
        Asignatura asignaturaEducacionFisica3esoB = new Asignatura("EDF", "3ESOB", true, 3) ; // 30
        Asignatura asignaturaFrances3esoB         = new Asignatura("FRA", "3ESOB", true, 3) ; // 33
        
        asignaturaInformatica3esoB.setBloqueOptativas(Arrays.asList(new Asignatura[] {asignaturaFrances3esoB})) ;
        asignaturaFrances3esoB.setBloqueOptativas(Arrays.asList(new Asignatura[] {asignaturaInformatica3esoB})) ;
        
        creadorSesiones.crearSesiones(asignaturaMatematicas3esoB, profesorMatematicas, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaHistoria3esoB, profesorHistoria, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaFisicaQuimica3esoB, profesorFisicaQuimica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaInformatica3esoB, profesorInformatica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaLengua3esoB, profesorLengua, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaReligion3esoB, profesorReligion, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaDibujo3esoB, profesorDibujo, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaEducacionFisica3esoB, profesorEducacionFisica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaFrances3esoB, profesorFrances, TipoHorario.MATUTINO, null);
        
        Asignatura asignaturaMatematicas4esoB     = new Asignatura("MAT", "4ESOB", true, 5); // 5
        Asignatura asignaturaHistoria4esoB        = new Asignatura("HIS", "4ESOB", true, 5); // 10
        Asignatura asignaturaFisicaQuimica4esoB   = new Asignatura("FyQ", "4ESOB", true, 5); // 15
        Asignatura asignaturaInformatica4esoB     = new Asignatura("INF", "4ESOB", true, 3) ; // 18
        Asignatura asignaturaLengua4esoB          = new Asignatura("LEN", "4ESOB", true, 4); // 22
        Asignatura asignaturaReligion4esoB        = new Asignatura("REL", "4ESOB", true, 2); // 24
        Asignatura asignaturaDibujo4esoB          = new Asignatura("DIB", "4ESOB", true, 3); // 27
        Asignatura asignaturaEducacionFisica4esoB = new Asignatura("EDF", "4ESOB", true, 3); // 30
        Asignatura asignaturaFrances4esoB         = new Asignatura("FRA", "4ESOB", true, 3) ; // 33
        
        asignaturaInformatica4esoB.setBloqueOptativas(Arrays.asList(new Asignatura[] {asignaturaFrances4esoB})) ;
        asignaturaFrances4esoB.setBloqueOptativas(Arrays.asList(new Asignatura[] {asignaturaInformatica4esoB})) ;
        
         
        creadorSesiones.crearSesiones(asignaturaMatematicas4esoB, profesorMatematicas, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaHistoria4esoB, profesorHistoria, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaFisicaQuimica4esoB, profesorFisicaQuimica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaInformatica4esoB, profesorInformatica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaLengua4esoB, profesorLengua, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaReligion4esoB, profesorReligion, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaDibujo4esoB, profesorDibujo, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaEducacionFisica4esoB, profesorEducacionFisica, TipoHorario.MATUTINO, null);
        creadorSesiones.crearSesiones(asignaturaFrances4esoB, profesorFrances, TipoHorario.MATUTINO, null);
        
        // Asignaturas 1DAW
        Asignatura asignaturaSistemasInformaticos1daw = new Asignatura("SI",    "1DAW", false, 6) ; // 6
        Asignatura asignaturaBBDD1daw                 = new Asignatura("BBDD",  "1DAW", false, 6) ; // 12
        Asignatura asignaturaProgramacion1daw         = new Asignatura("PROG",  "1DAW", false, 8) ; // 20
        Asignatura asignaturaLenguajesMarcas1daw      = new Asignatura("LMSGI", "1DAW", false, 5) ; // 25
        Asignatura asignaturaEntornos1daw             = new Asignatura("ED",    "1DAW", false, 3) ; // 28
        Asignatura asignaturaFOL1daw                  = new Asignatura("FOL",   "1DAW", false, 2) ; // 30
 
        // Crear sesiones para 1DAW
        
        creadorSesiones.crearSesiones(asignaturaSistemasInformaticos1daw, profesorSai1, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaBBDD1daw, profesorInformatica, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaProgramacion1daw, profesorPes1, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaLenguajesMarcas1daw, profesorPes1, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaEntornos1daw, profesorPes2, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaFOL1daw, profesorSai1, TipoHorario.VESPERTINO, null) ;
        
        // Asignaturas 2DAW
        Asignatura asignaturaCliente2daw              = new Asignatura("DWEC", "2DAW", false, 6) ; // 6
        Asignatura asignaturaServidor2daw             = new Asignatura("DWES", "2DAW", false, 8) ; // 14
        Asignatura asignaturaDespliegue2daw           = new Asignatura("DA",   "2DAW", false, 5) ; // 19
        Asignatura asignaturaInterfaces2daw           = new Asignatura("DI",   "2DAW", false, 6) ; // 25
        Asignatura asignaturaEmpresa2daw              = new Asignatura("EIE",  "2DAW", false, 3) ; // 28
        Asignatura asignaturaProyecto2daw             = new Asignatura("PDAW", "2DAW", false, 2) ; // 30

        // Crear sesiones para 2DAW

        
        creadorSesiones.crearSesiones(asignaturaCliente2daw, profesorPes2, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaServidor2daw, profesorPes2, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaDespliegue2daw, profesorSai1, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaInterfaces2daw, profesorSai2, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaEmpresa2daw, profesorSai2, TipoHorario.VESPERTINO, null) ;
        creadorSesiones.crearSesiones(asignaturaProyecto2daw, profesorPes1, TipoHorario.VESPERTINO, null) ;
        
        ManejadorResultadosParams manejadorResultadosParams = 
        	    new ManejadorResultadosParams.Builder()
        	        .setUmbralMinimoSolucion(Constants.UMBRAL_MINIMO_SOLUCION)         // Umbral mínimo de solución
        	        .setUmbralMinimoError(Constants.UMBRAL_MINIMO_ERROR)               // Umbral mínimo de error
        	        .build();                                              // Construcción final del objeto

        		
        // Crear el manejador de resultados con los umbrales definidos en Constants
        ManejadorResultados manejadorResultados = new ManejadorResultados(manejadorResultadosParams) ;
        
        // Obtenemos el número de cursos matutinos que tenemos en el instituto
        int numeroCursosMatutinos		  	    = mapCorrelacionadorCursosMatutinos.size() ;
        int numeroCursosVespertinos		  		= mapCorrelacionadorCursosVespertinos.size() ;
        
        ManejadorThreadsParams manejadorThreadsParams = 
        		new ManejadorThreadsParams.Builder()
        			.setNumeroCursosMatutinos(numeroCursosMatutinos)
                    .setNumeroCursosVespertinos(numeroCursosVespertinos)
	    	        .setFactorNumeroSesionesInsertadas(Constants.FACTOR_NUMERO_SESIONES_INSERTADAS) // Factor de puntuación por número de sesiones insertadas
	    	        .setFactorSesionesConsecutivasProfesor(Constants.FACTOR_SESIONES_CONSECUTIVAS_PROFESOR) // Factor de puntuación por sesiones consecutivas de profesor
                    .setFactorSesionesConsecutivasProfesorMatVes(Constants.FACTOR_SESIONES_CONSECUTIVAS_PROFESOR_MAT_VES) // Factor de puntuación por sesiones consecutivas de profesor en la primera hora vespertina
        			.setMapCorrelacionadorCursosMatutinos(mapCorrelacionadorCursosMatutinos) // Mapa de correlacionador de cursos (debe ser rellenado con los datos reales)
        			.setMapCorrelacionadorCursosVespertinos(mapCorrelacionadorCursosVespertinos) // Mapa de correlacionador de cursos (debe ser rellenado con los datos reales)
	    	        .setPoolSize(Constants.THREAD_POOL_SIZE)                     // Tamaño del pool
	    	        .setNumeroThreadPorIteracion(Constants.THREAD_POR_ITERACION) // Número de threads por iteración
	    	        .setManejadorResultados(manejadorResultados)
	    	        .build() ;
        
        // Obtenemos la lista de listas de sesiones
        List<List<Sesion>> listaListaSesiones = creadorSesiones.getListaDeListaSesiones() ;
        			
        ManejadorThreads manejadorThreads = new ManejadorThreads(manejadorThreadsParams, listaListaSesiones) ;
        
    	// Lanzamos nuevos threads para procesar la siguiente clase
        manejadorThreads.iniciarProceso();
    }
}