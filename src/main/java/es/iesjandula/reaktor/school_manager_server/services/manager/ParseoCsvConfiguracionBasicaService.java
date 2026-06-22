package es.iesjandula.reaktor.school_manager_server.services.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.school_manager_server.dtos.AlumnoCursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CargaCsvResultDto;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.DatosBrutoAlumnoMatricula;
import es.iesjandula.reaktor.school_manager_server.models.Departamento;
import es.iesjandula.reaktor.school_manager_server.models.EspacioSinDocencia;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdDepartamento;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdEspacio;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDatosBrutoAlumnoMatriculaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IDepartamentoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioFijoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IEspacioSinDocenciaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.CsvParserUtil;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de carga por fichero CSV para la ventana de "Configuración básica" (Crear).
 * <p>
 * Procesa los tres tipos de catálogo (espacios, cursos y etapas, departamentos) reutilizando el mismo parser
 * robusto ({@link CsvParserUtil}) y la detección de codificación que ya usa la carga de horas, y persistiendo con
 * las mismas reglas que el alta manual de cada controlador. El curso académico activo se resuelve internamente
 * (seleccionado = true) mediante {@link CursoAcademicoResolver}; el cliente no lo envía.
 * <p>
 * La carga es idempotente: las filas cuyo elemento ya existe se omiten sin error (no se duplican ni rompen la
 * importación), de forma coherente con el alta manual.
 */
@Slf4j
@Service
public class ParseoCsvConfiguracionBasicaService
{
    /**
     * Valor por defecto del flag ESO/Bachillerato al crear cursos y etapas desde CSV cuando la fila NO trae la
     * tercera columna {@code esoBachillerato} (compatibilidad con CSV antiguos de 2 columnas). Coincide con el
     * valor por defecto del formulario manual de la vista (checkbox marcado por defecto).
     */
    private static final boolean ESO_BACHILLERATO_POR_DEFECTO_CSV = true;

    /**
     * Longitud máxima del nombre de una reducción, acorde a la columna {@code nombre} de la PK embebida
     * {@link IdReduccion} ({@code @Column(length = 100)}). Las filas con nombre más largo se omiten con aviso para
     * no provocar un error de persistencia.
     */
    private static final int LONGITUD_MAXIMA_NOMBRE_REDUCCION = 100;

    @Autowired
    private IEspacioSinDocenciaRepository espacioSinDocenciaRepository;

    @Autowired
    private IEspacioFijoRepository espacioFijoRepository;

    @Autowired
    private ICursoEtapaRepository cursoEtapaRepository;

    @Autowired
    private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository;

    @Autowired
    private IDepartamentoRepository departamentoRepository;

    @Autowired
    private IReduccionRepository reduccionRepository;

    @Autowired
    private IDatosBrutoAlumnoMatriculaRepository datosBrutoAlumnoMatriculaRepository;

    @Autowired
    private CursoAcademicoResolver cursoAcademicoResolver;

    /**
     * Carga espacios (aulas) desde un CSV de 1 columna (la primera fila es la cabecera y se ignora como dato).
     * Cada fila restante es el nombre de un espacio. Reutiliza la persistencia del alta manual de espacios sin
     * docencia. Idempotente: omite los espacios que ya existan (como sin docencia o como fijo).
     *
     * @param archivoCsv el fichero CSV con los nombres de espacios.
     * @return un {@link CargaCsvResultDto} con el resumen de la carga.
     * @throws SchoolManagerServerException si el archivo está vacío, no es .csv o no contiene filas de datos.
     */
    @Transactional
    public CargaCsvResultDto cargarEspaciosDesdeCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        this.validarArchivoCsv(archivoCsv);

        String cursoAcademico = this.cursoAcademicoResolver.resolver();
        String[] lineas = this.obtenerLineasDatos(archivoCsv);

        CargaCsvResultDto resultado = new CargaCsvResultDto();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            resultado.setProcesados(resultado.getProcesados() + 1);

            String[] campos = CsvParserUtil.parseLinea(linea);
            String nombre = CsvParserUtil.limpiarCampo(campos[0]);

            if (nombre.isEmpty())
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": nombre de espacio vacío, se omite");
                continue;
            }

            IdEspacio idEspacio = new IdEspacio(cursoAcademico, nombre);

            // Idempotencia: si el aula ya está en el catálogo (sin docencia o fijo) no la duplicamos
            if (this.espacioSinDocenciaRepository.existsById(idEspacio) || this.espacioFijoRepository.existsById(idEspacio))
            {
                resultado.setOmitidos(resultado.getOmitidos() + 1);
                continue;
            }

            EspacioSinDocencia espacio = new EspacioSinDocencia();
            espacio.setEspacioId(idEspacio);
            this.espacioSinDocenciaRepository.saveAndFlush(espacio);

            resultado.setCreados(resultado.getCreados() + 1);
        }

        log.info("INFO - Carga CSV de espacios para {}: {} creados, {} omitidos, {} procesados",
                cursoAcademico, resultado.getCreados(), resultado.getOmitidos(), resultado.getProcesados());

        return resultado;
    }

    /**
     * Carga cursos y etapas desde un CSV de 3 columnas (la primera fila es la cabecera y se ignora como dato):
     * columna 1 = curso (entero), columna 2 = etapa (texto), columna 3 = {@code esoBachillerato} ({@code true} para
     * ESO/Bachillerato, {@code false} para ciclos formativos). El parseo del booleano es tolerante a
     * mayúsculas/minúsculas y espacios; si la tercera columna no viene (CSV antiguo de 2 columnas) se asume
     * {@code true} por defecto. Reutiliza la persistencia del alta manual de cursos y etapas (entidad
     * {@link CursoEtapa} + fila espejo en el catálogo {@link CursoEtapaGrupo}). Idempotente: omite los
     * cursos/etapas que ya existan.
     *
     * @param archivoCsv el fichero CSV con curso, etapa y esoBachillerato por fila.
     * @return un {@link CargaCsvResultDto} con el resumen de la carga.
     * @throws SchoolManagerServerException si el archivo está vacío, no es .csv o no contiene filas de datos.
     */
    @Transactional
    public CargaCsvResultDto cargarCursosEtapasDesdeCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        this.validarArchivoCsv(archivoCsv);

        String cursoAcademico = this.cursoAcademicoResolver.resolver();
        String[] lineas = this.obtenerLineasDatos(archivoCsv);

        CargaCsvResultDto resultado = new CargaCsvResultDto();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            resultado.setProcesados(resultado.getProcesados() + 1);

            String[] campos = CsvParserUtil.parseLinea(linea);

            if (campos.length < 2)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": se esperaban 2 columnas (curso, etapa), se omite");
                continue;
            }

            String cursoTexto = CsvParserUtil.limpiarCampo(campos[0]);
            String etapa = CsvParserUtil.limpiarCampo(campos[1]);

            if (cursoTexto.isEmpty() || etapa.isEmpty())
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": curso o etapa vacíos, se omite");
                continue;
            }

            int curso;
            try
            {
                curso = Integer.parseInt(cursoTexto);
            }
            catch (NumberFormatException numberFormatException)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": el curso '" + cursoTexto + "' no es un entero, se omite");
                continue;
            }

            // Tercera columna esoBachillerato (true = ESO/Bachillerato, false = ciclos formativos). Si no viene,
            // se asume true por compatibilidad con CSV antiguos de 2 columnas.
            boolean esoBachillerato = ESO_BACHILLERATO_POR_DEFECTO_CSV;
            if (campos.length >= 3)
            {
                esoBachillerato = this.parsearEsoBachillerato(CsvParserUtil.limpiarCampo(campos[2]));
            }

            IdCursoEtapa idCursoEtapa = new IdCursoEtapa(cursoAcademico, curso, etapa);

            // Idempotencia: si el curso/etapa ya existe no lo duplicamos
            if (this.cursoEtapaRepository.existsById(idCursoEtapa))
            {
                resultado.setOmitidos(resultado.getOmitidos() + 1);
                continue;
            }

            CursoEtapa cursoEtapa = new CursoEtapa();
            cursoEtapa.setIdCursoEtapa(idCursoEtapa);
            cursoEtapa.setEsoBachillerato(esoBachillerato);
            this.cursoEtapaRepository.saveAndFlush(cursoEtapa);

            this.crearCursoEtapaGrupoCatalogo(cursoAcademico, curso, etapa, esoBachillerato);

            resultado.setCreados(resultado.getCreados() + 1);
        }

        log.info("INFO - Carga CSV de cursos y etapas para {}: {} creados, {} omitidos, {} procesados",
                cursoAcademico, resultado.getCreados(), resultado.getOmitidos(), resultado.getProcesados());

        return resultado;
    }

    /**
     * Carga departamentos desde un CSV de 1 columna (la primera fila es la cabecera y se ignora como dato). Cada
     * fila restante es el nombre de un departamento. Reutiliza la persistencia del alta manual de departamentos
     * (fila por curso académico + sincronización de la fila global). Idempotente: omite los que ya existan.
     *
     * @param archivoCsv el fichero CSV con los nombres de departamentos.
     * @return un {@link CargaCsvResultDto} con el resumen de la carga.
     * @throws SchoolManagerServerException si el archivo está vacío, no es .csv o no contiene filas de datos.
     */
    @Transactional
    public CargaCsvResultDto cargarDepartamentosDesdeCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        this.validarArchivoCsv(archivoCsv);

        String cursoAcademico = this.cursoAcademicoResolver.resolver();
        String[] lineas = this.obtenerLineasDatos(archivoCsv);

        CargaCsvResultDto resultado = new CargaCsvResultDto();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            resultado.setProcesados(resultado.getProcesados() + 1);

            String[] campos = CsvParserUtil.parseLinea(linea);
            String nombre = CsvParserUtil.limpiarCampo(campos[0]);

            if (nombre.isEmpty())
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": nombre de departamento vacío, se omite");
                continue;
            }

            IdDepartamento idDepartamento = new IdDepartamento(cursoAcademico, nombre);

            // Idempotencia: si el departamento ya existe en el curso académico no lo duplicamos, pero
            // garantizamos igualmente la existencia de la fila global (compatibilidad).
            if (this.departamentoRepository.existsById(idDepartamento))
            {
                this.sincronizarDepartamentoGlobal(nombre);
                resultado.setOmitidos(resultado.getOmitidos() + 1);
                continue;
            }

            Departamento departamento = new Departamento();
            departamento.setCursoAcademico(cursoAcademico);
            departamento.setNombre(nombre);
            this.departamentoRepository.saveAndFlush(departamento);

            this.sincronizarDepartamentoGlobal(nombre);

            resultado.setCreados(resultado.getCreados() + 1);
        }

        log.info("INFO - Carga CSV de departamentos para {}: {} creados, {} omitidos, {} procesados",
                cursoAcademico, resultado.getCreados(), resultado.getOmitidos(), resultado.getProcesados());

        return resultado;
    }

    /**
     * Carga reducciones del tipo "NO tutorías" desde un CSV de 3 columnas (la primera fila es la cabecera y se
     * ignora como dato): columna 1 = nombre del cargo/función (texto), columna 2 = horas (entero), columna 3 =
     * {@code AsignaDireccion} ({@code true}/{@code false}).
     * <p>
     * MODELADO Y DUPLICADOS: la entidad {@link Reduccion} se identifica por la PK embebida {@link IdReduccion}
     * {@code (nombre, horas)} y NO está ligada a curso académico. Por tanto, las filas con el MISMO nombre y horas
     * DISTINTAS (p. ej. {@code Jefatura de Depto.} con 2/3/1/6) son entidades DIFERENTES (PK distinta) y se crean
     * todas sin colisión. Las filas con el mismo nombre Y las mismas horas (p. ej. {@code Jefatura de Depto.,3}
     * repetida) comparten PK: la carga es idempotente y la primera se crea mientras que las repetidas exactas se
     * cuentan como omitidas (no se duplican ni rompen la importación). Se persisten sin docencia
     * ({@code cursoEtapaGrupo = null}).
     * <p>
     * COLUMNA AsignaDireccion: la tercera columna indica si la asignación de la reducción la decide la dirección.
     * Su parseo es tolerante ({@code true}/{@code false}, además de {@code si}/{@code no} y {@code 1}/{@code 0}) y,
     * si la columna falta o no se reconoce, se asume {@code false} por defecto (compatibilidad con CSV antiguos de
     * 2 columnas).
     *
     * @param archivoCsv el fichero CSV con nombre, horas y AsignaDireccion por fila.
     * @return un {@link CargaCsvResultDto} con el resumen de la carga.
     * @throws SchoolManagerServerException si el archivo está vacío, no es .csv o no contiene filas de datos.
     */
    @Transactional
    public CargaCsvResultDto cargarReduccionesNoTutoriasDesdeCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        this.validarArchivoCsv(archivoCsv);

        String cursoAcademico = this.cursoAcademicoResolver.resolver();
        String[] lineas = this.obtenerLineasDatos(archivoCsv);

        CargaCsvResultDto resultado = new CargaCsvResultDto();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            resultado.setProcesados(resultado.getProcesados() + 1);

            String[] campos = CsvParserUtil.parseLinea(linea);

            if (campos.length < 2)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": se esperaban al menos 2 columnas (nombre, horas[, AsignaDireccion]), se omite");
                continue;
            }

            String nombre = CsvParserUtil.limpiarCampo(campos[0]);
            String horasTexto = CsvParserUtil.limpiarCampo(campos[1]);

            if (nombre.isEmpty())
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": nombre de reducción vacío, se omite");
                continue;
            }

            Integer horas = this.parsearEnteroTolerante(horasTexto);
            if (horas == null)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": las horas '" + horasTexto + "' no son un entero, se omite");
                continue;
            }

            // Tercera columna AsignaDireccion (true = la asignación la decide la dirección). Si no viene o no se
            // reconoce, se asume false por defecto (compatibilidad con CSV antiguos de 2 columnas).
            boolean decideDireccion = false;
            if (campos.length >= 3)
            {
                decideDireccion = this.parsearBooleanoTolerante(CsvParserUtil.limpiarCampo(campos[2]), false);
            }

            this.crearReduccionIdempotente(cursoAcademico, nombre, horas, decideDireccion, i, resultado);
        }

        log.info("INFO - Carga CSV de reducciones (no tutorías): {} creados, {} omitidos, {} procesados",
                resultado.getCreados(), resultado.getOmitidos(), resultado.getProcesados());

        return resultado;
    }

    /**
     * Carga reducciones del tipo "TUTORÍAS" desde un CSV de 3 columnas (la primera fila es la cabecera y se ignora
     * como dato): columna 1 = curso (entero), columna 2 = etapa (texto), columna 3 = horas (entero).
     * <p>
     * MODELADO: se reutiliza la misma entidad {@link Reduccion} sin migración de esquema. Como su PK es
     * {@code (nombre, horas)}, se sintetiza un nombre estable y legible a partir del curso y la etapa con el formato
     * {@code "Tutoría <curso>º <etapa>"} (p. ej. {@code "Tutoría 1º ESO"}). Así una tutoría queda identificada por
     * (curso + etapa) vía el nombre y por sus horas, encajando en la PK existente sin añadir columnas. Se persisten
     * sin docencia ({@code cursoEtapaGrupo = null}) y con {@code decideDireccion = true} SIEMPRE, ya que las
     * tutorías las propone el equipo directivo. Estas reducciones a nivel curso/etapa actúan como plantilla: al
     * crear grupos se materializan en una tutoría por grupo (ver {@code Paso3CrearGruposController}). La carga es
     * idempotente: las filas que produzcan el mismo (nombre, horas) ya existente se cuentan como omitidas.
     *
     * @param archivoCsv el fichero CSV con curso, etapa y horas por fila.
     * @return un {@link CargaCsvResultDto} con el resumen de la carga.
     * @throws SchoolManagerServerException si el archivo está vacío, no es .csv o no contiene filas de datos.
     */
    @Transactional
    public CargaCsvResultDto cargarReduccionesTutoriasDesdeCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        this.validarArchivoCsv(archivoCsv);

        String cursoAcademico = this.cursoAcademicoResolver.resolver();
        String[] lineas = this.obtenerLineasDatos(archivoCsv);

        CargaCsvResultDto resultado = new CargaCsvResultDto();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            resultado.setProcesados(resultado.getProcesados() + 1);

            String[] campos = CsvParserUtil.parseLinea(linea);

            if (campos.length < 3)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": se esperaban 3 columnas (curso, etapa, horas), se omite");
                continue;
            }

            String cursoTexto = CsvParserUtil.limpiarCampo(campos[0]);
            String etapa = CsvParserUtil.limpiarCampo(campos[1]);
            String horasTexto = CsvParserUtil.limpiarCampo(campos[2]);

            Integer curso = this.parsearEnteroTolerante(cursoTexto);
            if (curso == null)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": el curso '" + cursoTexto + "' no es un entero, se omite");
                continue;
            }

            if (etapa.isEmpty())
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": etapa vacía, se omite");
                continue;
            }

            Integer horas = this.parsearEnteroTolerante(horasTexto);
            if (horas == null)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": las horas '" + horasTexto + "' no son un entero, se omite");
                continue;
            }

            String nombre = Constants.PREFIJO_REDUCCION_TUTORIA + curso + "º " + etapa;

            // Las tutorías las propone el equipo directivo: decideDireccion es SIEMPRE true.
            this.crearReduccionIdempotente(cursoAcademico, nombre, horas, true, i, resultado);
        }

        log.info("INFO - Carga CSV de reducciones (tutorías): {} creados, {} omitidos, {} procesados",
                resultado.getCreados(), resultado.getOmitidos(), resultado.getProcesados());

        return resultado;
    }

    /**
     * Carga alumnos desde un CSV de 2 columnas (la primera fila es la cabecera y se ignora como dato):
     * columna 1 = {@code nombreApellidos} (nombre y apellidos del alumno), columna 2 = {@code cursoEtapaGrupo}
     * (curso, etapa y grupo del alumno, p. ej. "1 ESO A").
     * <p>
     * MODELADO: el alumno "disponible" del paso de creación de grupos se materializa como una fila
     * {@link DatosBrutoAlumnoMatricula} del curso/etapa con estado {@link Constants#ESTADO_MATRICULADO} y
     * {@code asignado = false} (que es justo lo que pinta la lista de "Alumnos disponibles" de la vista). El curso y
     * la etapa se resuelven desde la columna {@code cursoEtapaGrupo} y deben existir previamente (creados en la
     * configuración básica). El grupo se interpreta y valida, pero la asignación efectiva a un grupo (que requiere
     * matrículas por asignatura) se sigue haciendo desde la propia vista; este CSV solo da de alta a los alumnos.
     * <p>
     * El nombre y los apellidos se separan igual que en la carga de matrículas: si el campo trae una coma se
     * interpreta como "Apellidos, Nombre"; si no, el primer término es el nombre y el resto los apellidos.
     * <p>
     * Idempotente: los alumnos que ya existan en ese curso/etapa (mismo nombre y apellidos) se omiten.
     *
     * @param archivoCsv el fichero CSV con nombreApellidos y cursoEtapaGrupo por fila.
     * @return un {@link CargaCsvResultDto} con el resumen de la carga.
     * @throws SchoolManagerServerException si el archivo está vacío, no es .csv o no contiene filas de datos.
     */
    @Transactional
    public CargaCsvResultDto cargarAlumnosDesdeCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        this.validarArchivoCsv(archivoCsv);

        String cursoAcademico = this.cursoAcademicoResolver.resolver();
        String[] lineas = this.obtenerLineasDatos(archivoCsv);

        CargaCsvResultDto resultado = new CargaCsvResultDto();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            resultado.setProcesados(resultado.getProcesados() + 1);

            String[] campos = CsvParserUtil.parseLinea(linea);

            if (campos.length < 2)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": se esperaban 2 columnas (nombreApellidos, cursoEtapaGrupo), se omite");
                continue;
            }

            String nombreApellidos = CsvParserUtil.limpiarCampo(campos[0]);
            String cursoEtapaGrupo = CsvParserUtil.limpiarCampo(campos[1]);

            if (nombreApellidos.isEmpty())
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": nombre y apellidos vacíos, se omite");
                continue;
            }

            String[] nombreYApellidos = this.separarNombreYApellidos(nombreApellidos);
            String nombre = nombreYApellidos[0];
            String apellidos = nombreYApellidos[1];

            // Parseamos el curso, la etapa y el grupo a partir de la columna cursoEtapaGrupo (p. ej. "1 ESO A")
            String[] partes = cursoEtapaGrupo.trim().split("\\s+");
            if (partes.length < 2)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": cursoEtapaGrupo '" + cursoEtapaGrupo + "' inválido (se espera 'curso etapa grupo'), se omite");
                continue;
            }

            Integer curso = this.parsearEnteroTolerante(partes[0]);
            if (curso == null)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": el curso '" + partes[0] + "' no es un entero, se omite");
                continue;
            }

            // Si hay 3 o más términos, el último es el grupo y los del medio son la etapa; con 2 términos no hay grupo
            String etapa;
            if (partes.length >= 3)
            {
                etapa = String.join(" ", java.util.Arrays.copyOfRange(partes, 1, partes.length - 1));
            }
            else
            {
                etapa = partes[1];
            }

            if (etapa.isEmpty())
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": etapa vacía en '" + cursoEtapaGrupo + "', se omite");
                continue;
            }

            IdCursoEtapa idCursoEtapa = new IdCursoEtapa(cursoAcademico, curso, etapa);
            CursoEtapa cursoEtapa = this.cursoEtapaRepository.findById(idCursoEtapa).orElse(null);

            if (cursoEtapa == null)
            {
                resultado.getErrores().add("Fila " + (i + 1) + ": el curso/etapa '" + curso + " " + etapa + "' no existe, se omite");
                continue;
            }

            // Idempotencia: si el alumno ya está dado de alta en ese curso/etapa, no lo duplicamos
            List<DatosBrutoAlumnoMatricula> existentes =
                    this.datosBrutoAlumnoMatriculaRepository.findByNombreAndApellidosAndCursoEtapa(nombre, apellidos, cursoEtapa);

            if (!existentes.isEmpty())
            {
                resultado.setOmitidos(resultado.getOmitidos() + 1);
                continue;
            }

            DatosBrutoAlumnoMatricula datosBruto = new DatosBrutoAlumnoMatricula();
            datosBruto.setNombre(nombre);
            datosBruto.setApellidos(apellidos);
            datosBruto.setCursoEtapa(cursoEtapa);
            datosBruto.setEstadoMatricula(Constants.ESTADO_MATRICULADO);
            datosBruto.setAsignado(false);
            this.datosBrutoAlumnoMatriculaRepository.saveAndFlush(datosBruto);

            resultado.setCreados(resultado.getCreados() + 1);
        }

        log.info("INFO - Carga CSV de alumnos para {}: {} creados, {} omitidos, {} procesados",
                cursoAcademico, resultado.getCreados(), resultado.getOmitidos(), resultado.getProcesados());

        return resultado;
    }

    /**
     * Parsea el fichero {@code alumnos_cursoEtapaGrupo.csv} de "Creación de grupos" (la primera fila es la cabecera
     * y se ignora como dato): columna 1 = {@code nombreApellidos}, columna 2 = {@code cursoEtapaGrupo} (p. ej.
     * "1 ESO A", donde el último término es el grupo). A diferencia de {@link #cargarAlumnosDesdeCsv(MultipartFile)},
     * este método REQUIERE el grupo y NO persiste nada: solo devuelve las filas parseadas para que el controlador
     * valide contra datos brutos y asigne a los grupos de forma transaccional.
     *
     * @param archivoCsv el fichero CSV con nombreApellidos y cursoEtapaGrupo (con grupo) por fila.
     * @return la lista de filas parseadas (nombre, apellidos, curso, etapa, grupo).
     * @throws SchoolManagerServerException si el archivo está vacío, no es .csv, no contiene filas válidas o alguna
     *                                      fila tiene un formato inválido (faltan columnas, curso no numérico o sin grupo).
     */
    public List<AlumnoCursoEtapaGrupoDto> parsearAlumnosCursoEtapaGrupo(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        this.validarArchivoCsv(archivoCsv);

        String[] lineas = this.obtenerLineasDatos(archivoCsv);

        List<AlumnoCursoEtapaGrupoDto> filas = new ArrayList<>();

        for (int i = 1; i < lineas.length; i++)
        {
            String linea = lineas[i].trim();
            if (linea.isEmpty())
            {
                continue;
            }

            String[] campos = CsvParserUtil.parseLinea(linea);

            if (campos.length < 2)
            {
                String mensajeError = "Fila " + (i + 1) + ": se esperaban 2 columnas (nombreApellidos, cursoEtapaGrupo)";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERR_CARGA_ALUMNOS_FICHERO_FORMATO_CODE, mensajeError);
            }

            String nombreApellidos = CsvParserUtil.limpiarCampo(campos[0]);
            String cursoEtapaGrupo = CsvParserUtil.limpiarCampo(campos[1]);

            if (nombreApellidos.isEmpty())
            {
                String mensajeError = "Fila " + (i + 1) + ": nombre y apellidos vacíos";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERR_CARGA_ALUMNOS_FICHERO_FORMATO_CODE, mensajeError);
            }

            String[] nombreYApellidos = this.separarNombreYApellidos(nombreApellidos);

            String[] partes = cursoEtapaGrupo.trim().split("\\s+");

            // Se requiere 'curso etapa grupo' (al menos 3 términos: el primero curso, el último grupo, el resto etapa)
            if (partes.length < 3)
            {
                String mensajeError = "Fila " + (i + 1) + ": cursoEtapaGrupo '" + cursoEtapaGrupo + "' inválido (se espera 'curso etapa grupo')";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERR_CARGA_ALUMNOS_FICHERO_FORMATO_CODE, mensajeError);
            }

            Integer curso = this.parsearEnteroTolerante(partes[0]);
            if (curso == null)
            {
                String mensajeError = "Fila " + (i + 1) + ": el curso '" + partes[0] + "' no es un entero";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERR_CARGA_ALUMNOS_FICHERO_FORMATO_CODE, mensajeError);
            }

            String grupo = partes[partes.length - 1];
            String etapa = String.join(" ", Arrays.copyOfRange(partes, 1, partes.length - 1));

            if (etapa.isEmpty())
            {
                String mensajeError = "Fila " + (i + 1) + ": etapa vacía en '" + cursoEtapaGrupo + "'";
                log.error(mensajeError);
                throw new SchoolManagerServerException(Constants.ERR_CARGA_ALUMNOS_FICHERO_FORMATO_CODE, mensajeError);
            }

            filas.add(new AlumnoCursoEtapaGrupoDto(nombreYApellidos[0], nombreYApellidos[1], curso, etapa, grupo));
        }

        if (filas.isEmpty())
        {
            String mensajeError = "El fichero no contiene filas de datos válidas";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.ERR_CARGA_ALUMNOS_FICHERO_FORMATO_CODE, mensajeError);
        }

        return filas;
    }

    /**
     * Separa un campo "nombre y apellidos" en nombre y apellidos. Si el campo trae una coma se interpreta como
     * "Apellidos, Nombre" (mismo criterio que la carga de matrículas); si no, el primer término es el nombre y el
     * resto los apellidos.
     *
     * @param nombreApellidos el campo completo.
     * @return un array de 2 posiciones: [nombre, apellidos].
     */
    private String[] separarNombreYApellidos(String nombreApellidos)
    {
        int indiceComa = nombreApellidos.indexOf(',');

        if (indiceComa >= 0)
        {
            String apellidos = nombreApellidos.substring(0, indiceComa).trim();
            String nombre = nombreApellidos.substring(indiceComa + 1).trim();
            return new String[] { nombre, apellidos };
        }

        int indiceEspacio = nombreApellidos.indexOf(' ');
        if (indiceEspacio >= 0)
        {
            String nombre = nombreApellidos.substring(0, indiceEspacio).trim();
            String apellidos = nombreApellidos.substring(indiceEspacio + 1).trim();
            return new String[] { nombre, apellidos };
        }

        return new String[] { nombreApellidos, "" };
    }

    /**
     * Crea una {@link Reduccion} sin docencia de forma idempotente a partir de su PK {@code (nombre, horas)}. Si ya
     * existe una reducción con esa misma PK, NO la duplica y la cuenta como omitida. Si el nombre excede la longitud
     * máxima de la columna ({@value #LONGITUD_MAXIMA_NOMBRE_REDUCCION}), registra un aviso y omite la fila para no
     * provocar un error de persistencia.
     *
     * @param nombre          nombre de la reducción (identificador junto con las horas).
     * @param horas           horas de la reducción.
     * @param decideDireccion si la asignación de la reducción la decide la dirección (las tutorías son siempre
     *                        {@code true}; las no tutorías toman el valor de la columna {@code AsignaDireccion}).
     * @param numeroLinea     índice de la línea en el CSV (0-based) para componer los avisos.
     * @param resultado       acumulador del resumen de la carga.
     */
    private void crearReduccionIdempotente(String cursoAcademico, String nombre, int horas, boolean decideDireccion, int numeroLinea, CargaCsvResultDto resultado)
    {
        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE_REDUCCION)
        {
            resultado.getErrores().add("Fila " + (numeroLinea + 1) + ": el nombre de reducción supera los " + LONGITUD_MAXIMA_NOMBRE_REDUCCION + " caracteres, se omite");
            return;
        }

        IdReduccion idReduccion = new IdReduccion(cursoAcademico, nombre, horas);

        // Idempotencia: si la reducción (nombre + horas) ya existe no la duplicamos
        if (this.reduccionRepository.existsById(idReduccion))
        {
            resultado.setOmitidos(resultado.getOmitidos() + 1);
            return;
        }

        Reduccion reduccion = new Reduccion();
        reduccion.setIdReduccion(idReduccion);
        reduccion.setDecideDireccion(decideDireccion);
        this.reduccionRepository.saveAndFlush(reduccion);

        resultado.setCreados(resultado.getCreados() + 1);
    }

    /**
     * Parsea de forma tolerante (recorta espacios) un entero. Devuelve {@code null} si el valor es nulo, vacío o no
     * representa un entero válido, para que el llamador pueda registrar un aviso no bloqueante y omitir la fila.
     *
     * @param valor texto a convertir.
     * @return el entero parseado o {@code null} si no es válido.
     */
    private Integer parsearEnteroTolerante(String valor)
    {
        if (valor == null)
        {
            return null;
        }

        String limpio = valor.trim();
        if (limpio.isEmpty())
        {
            return null;
        }

        try
        {
            return Integer.parseInt(limpio);
        }
        catch (NumberFormatException numberFormatException)
        {
            return null;
        }
    }

    /**
     * Crea la fila espejo del catálogo en {@link CursoEtapaGrupo} con el grupo de catálogo, igual que el alta manual.
     */
    private void crearCursoEtapaGrupoCatalogo(String cursoAcademico, int curso, String etapa, boolean esoBachillerato)
    {
        IdCursoEtapaGrupo idCursoEtapaGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, Constants.GRUPO_CATALOGO_CURSO_ETAPA);

        if (!this.cursoEtapaGrupoRepository.existsById(idCursoEtapaGrupo))
        {
            CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
            cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
            cursoEtapaGrupo.setEsoBachillerato(esoBachillerato);
            this.cursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);
        }
    }

    /**
     * Parsea de forma tolerante (insensible a mayúsculas/minúsculas y espacios) el valor de la columna
     * {@code esoBachillerato}. Reconoce {@code true}/{@code false} (y sinónimos habituales {@code si}/{@code no},
     * {@code 1}/{@code 0}). Ante un valor vacío o no reconocido se asume {@code true} por defecto.
     *
     * @param valor el texto de la columna esoBachillerato.
     * @return {@code true} si es ESO/Bachillerato; {@code false} si es ciclo formativo.
     */
    /**
     * Parsea de forma tolerante (insensible a mayúsculas/minúsculas y espacios) un valor booleano de una columna
     * CSV. Reconoce {@code true}/{@code false} (y los sinónimos {@code si}/{@code sí}/{@code no}, {@code 1}/{@code 0}).
     * Ante un valor nulo, vacío o no reconocido devuelve el valor por defecto indicado.
     *
     * @param valor       el texto de la columna.
     * @param porDefecto  valor a devolver si el texto es nulo, vacío o no se reconoce.
     * @return el booleano parseado o {@code porDefecto} si no se reconoce.
     */
    private boolean parsearBooleanoTolerante(String valor, boolean porDefecto)
    {
        if (valor == null)
        {
            return porDefecto;
        }

        String normalizado = valor.trim().toLowerCase();

        if (normalizado.equals("false") || normalizado.equals("0") || normalizado.equals("no"))
        {
            return false;
        }

        if (normalizado.equals("true") || normalizado.equals("1") || normalizado.equals("si") || normalizado.equals("sí"))
        {
            return true;
        }

        return porDefecto;
    }

    private boolean parsearEsoBachillerato(String valor)
    {
        if (valor == null)
        {
            return ESO_BACHILLERATO_POR_DEFECTO_CSV;
        }

        String normalizado = valor.trim().toLowerCase();

        if (normalizado.equals("false") || normalizado.equals("0") || normalizado.equals("no"))
        {
            return false;
        }

        if (normalizado.equals("true") || normalizado.equals("1") || normalizado.equals("si") || normalizado.equals("sí"))
        {
            return true;
        }

        return ESO_BACHILLERATO_POR_DEFECTO_CSV;
    }

    /**
     * Mantiene un registro global del departamento para compatibilidad con profesores, asignaturas y reducciones,
     * igual que el alta manual de {@code DepartamentosAdminController}.
     */
    private void sincronizarDepartamentoGlobal(String nombre)
    {
        IdDepartamento idDepartamentoGlobal = new IdDepartamento(Constants.CURSO_ACADEMICO_GLOBAL, nombre);

        if (!this.departamentoRepository.existsById(idDepartamentoGlobal))
        {
            Departamento departamentoGlobal = new Departamento(nombre);
            this.departamentoRepository.saveAndFlush(departamentoGlobal);
        }
    }

    /**
     * Valida que el fichero exista, no esté vacío y tenga extensión .csv (mismas comprobaciones que la carga de horas).
     */
    private void validarArchivoCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        if (archivoCsv == null || archivoCsv.isEmpty())
        {
            String mensajeError = "El archivo importado está vacío";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.ARCHIVO_VACIO, mensajeError);
        }

        String nombreArchivo = archivoCsv.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".csv"))
        {
            String mensajeError = "El archivo debe tener extensión .csv";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
        }
    }

    /**
     * Lee el CSV detectando su codificación y lo separa en líneas. Exige al menos cabecera + 1 fila de datos.
     */
    private String[] obtenerLineasDatos(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        String csvString = this.leerContenidoCsv(archivoCsv);
        String[] lineas = csvString.split("\\R");

        if (lineas.length < 2)
        {
            String mensajeError = "El CSV no contiene filas de datos (debe tener cabecera y al menos una fila)";
            log.error(mensajeError);
            throw new SchoolManagerServerException(Constants.DATOS_NO_PROCESADO, mensajeError);
        }

        return lineas;
    }

    private String leerContenidoCsv(MultipartFile archivoCsv) throws SchoolManagerServerException
    {
        try
        {
            // Decodificación robusta UTF-8 (con manejo de BOM) compartida por todas las cargas CSV del módulo,
            // de modo que caracteres como la "ó" de "Salón" se interpreten correctamente.
            return CsvParserUtil.decodificarContenido(archivoCsv.getBytes());
        }
        catch (Exception exception)
        {
            String mensajeError = "ERROR - No se pudo leer el fichero CSV";
            log.error(mensajeError, exception);
            throw new SchoolManagerServerException(Constants.IO_EXCEPTION, mensajeError, exception);
        }
    }
}
