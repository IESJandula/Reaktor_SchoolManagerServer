package es.iesjandula.reaktor.school_manager_server.services.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.school_manager_server.dtos.CopiarCursoAcademicoDto;
import es.iesjandula.reaktor.school_manager_server.dtos.CursoEtapaGrupoDto;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import es.iesjandula.reaktor.school_manager_server.models.Bloque;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.CursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ProfesorReduccion;
import es.iesjandula.reaktor.school_manager_server.models.Reduccion;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdAsignatura;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapa;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdCursoEtapaGrupo;
import es.iesjandula.reaktor.school_manager_server.models.ids.IdReduccion;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IBloqueRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaGrupoRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.ICursoEtapaRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IImpartirRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IProfesorReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.repositories.IReduccionRepository;
import es.iesjandula.reaktor.school_manager_server.utils.Constants;
import es.iesjandula.reaktor.school_manager_server.utils.SchoolManagerServerException;
import lombok.extern.slf4j.Slf4j;

/**
 * Copia configuración de un curso académico a otro, machacando el destino según las opciones elegidas.
 * <p>
 * <b>Resolución de prerrequisitos (dependencias entre opciones)</b>: existen dependencias jerárquicas entre las
 * entidades copiables:
 * <ul>
 *   <li>Las <b>asignaturas</b> cuelgan de un curso/etapa/grupo, por lo que requieren que existan los
 *       <b>cursos/etapas</b> (y sus grupos) en el destino.</li>
 *   <li>Las <b>reducciones</b> con docencia (tutorías de un curso/etapa/grupo concreto) también requieren que
 *       existan los <b>cursos/etapas/grupos</b> correspondientes en el destino.</li>
 * </ul>
 * Por ello, aunque el usuario no marque explícitamente {@code cursos_etapas}, al copiar {@code asignaturas} o
 * {@code reducciones} se garantiza que el catálogo curso/etapa del origen existe en el destino. El orden de
 * ejecución es siempre: 1) cursos/etapas, 2) asignaturas, 3) reducciones. Toda la operación es
 * {@link Transactional transaccional} e <b>idempotente</b>: si {@code cursos_etapas} se marca de forma explícita se
 * machaca (sobrescribe) el destino; si solo se incluye de forma implícita por dependencia, se garantiza su
 * existencia sin borrar ni duplicar lo ya presente.
 * </p>
 */
@Slf4j
@Service
public class CopiarCursoAcademicoService
{
	@Autowired
	private ICursoEtapaRepository cursoEtapaRepository;

	@Autowired
	private ICursoEtapaGrupoRepository cursoEtapaGrupoRepository;

	@Autowired
	private IAsignaturaRepository asignaturaRepository;

	@Autowired
	private IBloqueRepository bloqueRepository;

	@Autowired
	private IImpartirRepository impartirRepository;

	@Autowired
	private IReduccionRepository reduccionRepository;

	@Autowired
	private IProfesorReduccionRepository profesorReduccionRepository;

	@Transactional
	public void copiar(String cursoAcademicoOrigen, String cursoAcademicoDestino, CopiarCursoAcademicoDto copiarDto) throws SchoolManagerServerException
	{
		boolean copiarCursosEtapas = this.incluyeOpcion(copiarDto, Constants.OPCION_COPIAR_CURSOS_ETAPAS);
		boolean copiarAsignaturas = this.incluyeOpcion(copiarDto, Constants.OPCION_COPIAR_ASIGNATURAS);
		boolean copiarReducciones = this.incluyeOpcion(copiarDto, Constants.OPCION_COPIAR_REDUCCIONES);

		if (!copiarCursosEtapas && !copiarAsignaturas && !copiarReducciones)
		{
			log.error(Constants.ERR_COPIAR_SIN_OPCIONES_MESSAGE);
			throw new SchoolManagerServerException(Constants.ERR_COPIAR_SIN_OPCIONES_CODE, Constants.ERR_COPIAR_SIN_OPCIONES_MESSAGE);
		}

		List<CursoEtapaGrupoDto> catalogoOrigen = this.cursoEtapaRepository.findAllDtoByCursoAcademico(cursoAcademicoOrigen);
		List<CursoEtapaGrupoDto> catalogoDestino = this.cursoEtapaRepository.findAllDtoByCursoAcademico(cursoAcademicoDestino);

		// Prerrequisito: asignaturas y reducciones cuelgan de cursos/etapas/grupos. Resolvemos esa dependencia antes
		// de copiarlas. Si el usuario marca cursos_etapas, se machaca el destino; si solo se incluye de forma
		// implícita (por copiar asignaturas/reducciones), se garantiza su existencia sin borrar lo ya presente.
		if (copiarCursosEtapas)
		{
			this.machacarCatalogoCursosEtapas(cursoAcademicoDestino, catalogoOrigen);
		}
		else if (copiarAsignaturas || copiarReducciones)
		{
			this.asegurarCatalogoCursosEtapas(cursoAcademicoDestino, catalogoOrigen);
		}

		if (copiarAsignaturas)
		{
			Set<String> paresCursoEtapa = this.unirParesCursoEtapa(catalogoDestino, catalogoOrigen);

			for (String par : paresCursoEtapa)
			{
				int curso = Integer.parseInt(par.substring(0, par.indexOf('|')));
				String etapa = par.substring(par.indexOf('|') + 1);
				this.purgarAsignaturasCursoEtapa(cursoAcademicoDestino, curso, etapa);
			}

			for (CursoEtapaGrupoDto cursoEtapaDto : catalogoOrigen)
			{
				this.copiarAsignaturasCursoEtapa(cursoAcademicoOrigen, cursoAcademicoDestino, cursoEtapaDto.getCurso(), cursoEtapaDto.getEtapa());
			}
		}

		if (copiarReducciones)
		{
			this.copiarReducciones(cursoAcademicoOrigen, cursoAcademicoDestino);
		}
	}

	private boolean incluyeOpcion(CopiarCursoAcademicoDto copiarDto, String opcion)
	{
		return copiarDto != null && copiarDto.getOpciones() != null && copiarDto.getOpciones().contains(opcion);
	}

	private void machacarCatalogoCursosEtapas(String cursoAcademicoDestino, List<CursoEtapaGrupoDto> catalogoOrigen)
	{
		this.cursoEtapaGrupoRepository.deleteAllCatalogoByCursoAcademico(cursoAcademicoDestino);
		this.cursoEtapaRepository.deleteAllByCursoAcademico(cursoAcademicoDestino);

		for (CursoEtapaGrupoDto cursoEtapaDto : catalogoOrigen)
		{
			IdCursoEtapa idCursoEtapaDestino = new IdCursoEtapa(cursoAcademicoDestino, cursoEtapaDto.getCurso(), cursoEtapaDto.getEtapa());

			CursoEtapa cursoEtapa = new CursoEtapa();
			cursoEtapa.setIdCursoEtapa(idCursoEtapaDestino);
			cursoEtapa.setEsoBachillerato(Boolean.TRUE.equals(cursoEtapaDto.getEsBachillerato()));
			this.cursoEtapaRepository.saveAndFlush(cursoEtapa);

			this.crearCursoEtapaGrupoCatalogo(cursoAcademicoDestino, cursoEtapaDto);
		}
	}

	/**
	 * Garantiza, de forma idempotente y sin borrar nada, que el catálogo curso/etapa del origen existe en el destino.
	 * <p>
	 * Se usa cuando {@code cursos_etapas} no se marca explícitamente pero se copian {@code asignaturas} o
	 * {@code reducciones}, que dependen de que existan los cursos/etapas (y su fila espejo en el catálogo de grupos).
	 * Si una fila ya existe, se respeta (no se duplica ni se sobrescribe).
	 * </p>
	 */
	private void asegurarCatalogoCursosEtapas(String cursoAcademicoDestino, List<CursoEtapaGrupoDto> catalogoOrigen)
	{
		for (CursoEtapaGrupoDto cursoEtapaDto : catalogoOrigen)
		{
			IdCursoEtapa idCursoEtapaDestino = new IdCursoEtapa(cursoAcademicoDestino, cursoEtapaDto.getCurso(), cursoEtapaDto.getEtapa());

			if (!this.cursoEtapaRepository.existsById(idCursoEtapaDestino))
			{
				CursoEtapa cursoEtapa = new CursoEtapa();
				cursoEtapa.setIdCursoEtapa(idCursoEtapaDestino);
				cursoEtapa.setEsoBachillerato(Boolean.TRUE.equals(cursoEtapaDto.getEsBachillerato()));
				this.cursoEtapaRepository.saveAndFlush(cursoEtapa);
			}

			IdCursoEtapaGrupo idCatalogo = this.resolverIdCursoEtapaGrupoCatalogo(cursoAcademicoDestino, cursoEtapaDto);

			if (!this.cursoEtapaGrupoRepository.existsById(idCatalogo))
			{
				CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
				cursoEtapaGrupo.setIdCursoEtapaGrupo(idCatalogo);
				cursoEtapaGrupo.setEsoBachillerato(cursoEtapaDto.getEsBachillerato());
				this.cursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);
			}
		}
	}

	private void purgarAsignaturasCursoEtapa(String cursoAcademicoDestino, int curso, String etapa)
	{
		this.impartirRepository.borrarPorCursoYEtapa(cursoAcademicoDestino, curso, etapa);
		this.asignaturaRepository.borrarPorCursoYEtapa(cursoAcademicoDestino, curso, etapa);
		this.bloqueRepository.deleteBloquesSinAsignaturas();
	}

	private void copiarAsignaturasCursoEtapa(String cursoAcademicoOrigen, String cursoAcademicoDestino, int curso, String etapa)
	{
		List<Asignatura> asignaturasOrigen = this.asignaturaRepository.findAllByCursoAndEtapa(cursoAcademicoOrigen, curso, etapa);

		if (asignaturasOrigen.isEmpty())
		{
			return;
		}

		Map<Long, Bloque> bloquesNuevos = new HashMap<>();

		for (Asignatura asignaturaOrigen : asignaturasOrigen)
		{
			if (asignaturaOrigen.getBloqueId() != null)
			{
				Long bloqueOrigenId = asignaturaOrigen.getBloqueId().getId();
				bloquesNuevos.computeIfAbsent(bloqueOrigenId, id ->
				{
					Bloque bloqueNuevo = new Bloque();
					return this.bloqueRepository.saveAndFlush(bloqueNuevo);
				});
			}
		}

		for (Asignatura asignaturaOrigen : asignaturasOrigen)
		{
			CursoEtapaGrupo grupoOrigen = asignaturaOrigen.getIdAsignatura().getCursoEtapaGrupo();
			IdCursoEtapaGrupo idGrupoOrigen = grupoOrigen.getIdCursoEtapaGrupo();

			CursoEtapaGrupo grupoDestino = this.obtenerOCrearGrupo(
					cursoAcademicoDestino,
					idGrupoOrigen.getCurso(),
					idGrupoOrigen.getEtapa(),
					idGrupoOrigen.getGrupo(),
					grupoOrigen.getEsoBachillerato(),
					grupoOrigen.getHorarioMatutino());

			Asignatura asignaturaDestino = new Asignatura();
			asignaturaDestino.setIdAsignatura(new IdAsignatura(grupoDestino, asignaturaOrigen.getIdAsignatura().getNombre()));
			asignaturaDestino.setHoras(asignaturaOrigen.getHoras());
			asignaturaDestino.setEsoBachillerato(asignaturaOrigen.isEsoBachillerato());
			asignaturaDestino.setSinDocencia(asignaturaOrigen.isSinDocencia());
			asignaturaDestino.setDesdoble(asignaturaOrigen.isDesdoble());
			asignaturaDestino.setEsAdHoc(asignaturaOrigen.isEsAdHoc());
			asignaturaDestino.setDepartamentoPropietario(asignaturaOrigen.getDepartamentoPropietario());
			asignaturaDestino.setDepartamentoReceptor(asignaturaOrigen.getDepartamentoReceptor());

			if (asignaturaOrigen.getBloqueId() != null)
			{
				asignaturaDestino.setBloqueId(bloquesNuevos.get(asignaturaOrigen.getBloqueId().getId()));
			}

			this.asignaturaRepository.saveAndFlush(asignaturaDestino);
		}
	}

	/**
	 * Copia TODAS las reducciones del curso académico origen al destino: tanto las TUTORÍAS como las NO tutorías, y
	 * tanto las que tienen docencia como las globales.
	 * <p>
	 * <b>Dos naturalezas de reducción</b>:
	 * <ul>
	 *   <li><b>Con docencia</b>: vinculadas (relación {@code cursoEtapaGrupo}) a un grupo de un curso académico
	 *       concreto (p. ej. las tutorías por grupo "Tutoría 1º ESO A"). Son "del curso académico" y se copian
	 *       re-apuntándolas al grupo equivalente del destino.</li>
	 *   <li><b>Globales (sin docencia, {@code cursoEtapaGrupo} nulo)</b>: las reducciones cargadas por CSV (tutorías
	 *       a nivel curso/etapa "plantilla" y no tutorías como jefaturas o coordinaciones). Como la PK de
	 *       {@link Reduccion} es {@code (nombre, horas)} SIN curso académico, estas filas son compartidas por todos
	 *       los cursos académicos: copiarlas se reduce a garantizar su existencia (idempotente). Es CLAVE no
	 *       desreferenciar {@code getCursoEtapaGrupo()} en estas (sería {@code null} y provocaría un NPE).</li>
	 * </ul>
	 * <b>Purga del destino</b>: solo se purgan las reducciones CON docencia del destino (y sus asignaciones
	 * profesor-reducción), NUNCA las globales, ya que estas son compartidas y borrarlas afectaría a todos los cursos
	 * académicos. Como la PK de {@link Reduccion} es global {@code (nombre, horas)}, si al copiar una con docencia ya
	 * existe ese par nombre/horas se omite (no se puede duplicar) y se deja constancia en el log.
	 * </p>
	 */
	private void copiarReducciones(String cursoAcademicoOrigen, String cursoAcademicoDestino)
	{
		// Purga del destino: primero las asignaciones profesor-reducción (FK) y después las reducciones CON docencia
		// del destino. Las globales (sin docencia) NO se purgan: son compartidas por todos los cursos académicos.
		List<ProfesorReduccion> asignacionesDestino = this.profesorReduccionRepository.findAllByCursoAcademico(cursoAcademicoDestino);
		if (!asignacionesDestino.isEmpty())
		{
			this.profesorReduccionRepository.deleteAll(asignacionesDestino);
			this.profesorReduccionRepository.flush();
		}

		List<Reduccion> reduccionesDestino = this.reduccionRepository.findAllByCursoAcademico(cursoAcademicoDestino);
		if (!reduccionesDestino.isEmpty())
		{
			this.reduccionRepository.deleteAll(reduccionesDestino);
			this.reduccionRepository.flush();
		}

		// Copia origen -> destino. Recuperamos TODAS las reducciones del origen: las globales (sin docencia) y las
		// con docencia de ese curso académico (findAllParaListadoByCursoAcademico incluye ambas; findAllByCursoAcademico
		// se quedaría solo con las que tienen docencia).
		List<Reduccion> reduccionesOrigen = this.reduccionRepository.findAllParaListadoByCursoAcademico(cursoAcademicoOrigen);

		for (Reduccion reduccionOrigen : reduccionesOrigen)
		{
			CursoEtapaGrupo grupoOrigen = reduccionOrigen.getCursoEtapaGrupo();

			// Reducción GLOBAL (sin docencia): tutorías "plantilla" y no tutorías cargadas por CSV. Son compartidas
			// por todos los cursos académicos (PK sin curso académico); basta con garantizar su existencia. NO se
			// puede desreferenciar grupoOrigen aquí (es null) para no provocar un NPE.
			if (grupoOrigen == null)
			{
				this.copiarReduccionGlobal(cursoAcademicoDestino, reduccionOrigen);
				continue;
			}

			IdCursoEtapaGrupo idGrupoOrigen = grupoOrigen.getIdCursoEtapaGrupo();

			CursoEtapaGrupo grupoDestino = this.obtenerOCrearGrupo(
					cursoAcademicoDestino,
					idGrupoOrigen.getCurso(),
					idGrupoOrigen.getEtapa(),
					idGrupoOrigen.getGrupo(),
					grupoOrigen.getEsoBachillerato(),
					grupoOrigen.getHorarioMatutino());

			IdReduccion idReduccionDestino = new IdReduccion(cursoAcademicoDestino, reduccionOrigen.getIdReduccion().getNombre(), reduccionOrigen.getIdReduccion().getHoras());

			// Cada reducción está scoped por curso académico (PK con cursoAcademico): si ya existe en destino no se duplica
			if (this.reduccionRepository.existsById(idReduccionDestino))
			{
				log.warn("WARN - La reducción '{}' ({}h) ya existe; se omite al copiar de {} a {}",
						idReduccionDestino.getNombre(), idReduccionDestino.getHoras(), cursoAcademicoOrigen, cursoAcademicoDestino);
				continue;
			}

			Reduccion reduccionDestino = new Reduccion();
			reduccionDestino.setIdReduccion(idReduccionDestino);
			reduccionDestino.setDecideDireccion(reduccionOrigen.isDecideDireccion());
			reduccionDestino.setCursoEtapaGrupo(grupoDestino);

			this.reduccionRepository.saveAndFlush(reduccionDestino);
		}
	}

	/**
	 * Garantiza, de forma idempotente y sin docencia, la existencia de una reducción GLOBAL (tutoría plantilla o no
	 * tutoría cargada por CSV) en la copia. Como la PK {@code (nombre, horas)} es compartida por todos los cursos
	 * académicos, lo habitual es que ya exista (mismo registro) y la operación sea un no-op; se crea únicamente si,
	 * por cualquier motivo, no estuviera presente. Nunca desreferencia el grupo (es {@code null} en las globales).
	 *
	 * @param reduccionGlobalOrigen la reducción global del origen a garantizar en destino.
	 */
	private void copiarReduccionGlobal(String cursoAcademicoDestino, Reduccion reduccionGlobalOrigen)
	{
		IdReduccion idReduccionGlobal = new IdReduccion(cursoAcademicoDestino, reduccionGlobalOrigen.getIdReduccion().getNombre(), reduccionGlobalOrigen.getIdReduccion().getHoras());

		if (this.reduccionRepository.existsById(idReduccionGlobal))
		{
			return;
		}

		Reduccion reduccionGlobal = new Reduccion();
		reduccionGlobal.setIdReduccion(idReduccionGlobal);
		reduccionGlobal.setDecideDireccion(reduccionGlobalOrigen.isDecideDireccion());
		reduccionGlobal.setCursoEtapaGrupo(null);

		this.reduccionRepository.saveAndFlush(reduccionGlobal);
	}

	private CursoEtapaGrupo obtenerOCrearGrupo(String cursoAcademico, int curso, String etapa, String grupo, Boolean esoBachillerato, Boolean horarioMatutino)
	{
		// Aseguramos que existe la fila CursoEtapa del curso académico destino (necesaria para los grupos docentes)
		IdCursoEtapa idCursoEtapa = new IdCursoEtapa(cursoAcademico, curso, etapa);
		if (!this.cursoEtapaRepository.existsById(idCursoEtapa))
		{
			CursoEtapa cursoEtapa = new CursoEtapa();
			cursoEtapa.setIdCursoEtapa(idCursoEtapa);
			cursoEtapa.setEsoBachillerato(Boolean.TRUE.equals(esoBachillerato));
			this.cursoEtapaRepository.saveAndFlush(cursoEtapa);
		}

		IdCursoEtapaGrupo idGrupo = new IdCursoEtapaGrupo(cursoAcademico, curso, etapa, grupo);
		Optional<CursoEtapaGrupo> grupoExistente = this.cursoEtapaGrupoRepository.findById(idGrupo);

		if (grupoExistente.isPresent())
		{
			return grupoExistente.get();
		}

		CursoEtapaGrupo grupoNuevo = new CursoEtapaGrupo();
		grupoNuevo.setIdCursoEtapaGrupo(idGrupo);
		grupoNuevo.setEsoBachillerato(esoBachillerato);
		grupoNuevo.setHorarioMatutino(horarioMatutino);
		return this.cursoEtapaGrupoRepository.saveAndFlush(grupoNuevo);
	}

	private Set<String> unirParesCursoEtapa(List<CursoEtapaGrupoDto> catalogoDestino, List<CursoEtapaGrupoDto> catalogoOrigen)
	{
		Set<String> pares = new HashSet<>();

		for (CursoEtapaGrupoDto dto : catalogoDestino)
		{
			pares.add(this.claveCursoEtapa(dto.getCurso(), dto.getEtapa()));
		}

		for (CursoEtapaGrupoDto dto : catalogoOrigen)
		{
			pares.add(this.claveCursoEtapa(dto.getCurso(), dto.getEtapa()));
		}

		return pares;
	}

	private String claveCursoEtapa(int curso, String etapa)
	{
		return curso + "|" + etapa;
	}

	private void crearCursoEtapaGrupoCatalogo(String cursoAcademico, CursoEtapaGrupoDto cursoEtapaGrupoDto)
	{
		IdCursoEtapaGrupo idCursoEtapaGrupo = this.resolverIdCursoEtapaGrupoCatalogo(cursoAcademico, cursoEtapaGrupoDto);

		CursoEtapaGrupo cursoEtapaGrupo = new CursoEtapaGrupo();
		cursoEtapaGrupo.setIdCursoEtapaGrupo(idCursoEtapaGrupo);
		cursoEtapaGrupo.setEsoBachillerato(cursoEtapaGrupoDto.getEsBachillerato());
		this.cursoEtapaGrupoRepository.saveAndFlush(cursoEtapaGrupo);
	}

	private IdCursoEtapaGrupo resolverIdCursoEtapaGrupoCatalogo(String cursoAcademico, CursoEtapaGrupoDto cursoEtapaGrupoDto)
	{
		String grupo = cursoEtapaGrupoDto.getGrupo();

		if (grupo == null || grupo.isEmpty())
		{
			grupo = Constants.GRUPO_CATALOGO_CURSO_ETAPA;
		}

		return new IdCursoEtapaGrupo(cursoAcademico, cursoEtapaGrupoDto.getCurso(), cursoEtapaGrupoDto.getEtapa(), grupo);
	}
}
