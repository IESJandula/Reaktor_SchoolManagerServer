package es.iesjandula.reaktor.school_manager_server.services.manager;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import es.iesjandula.reaktor.school_manager_server.repositories.IAsignaturaRepository;
import es.iesjandula.reaktor.school_manager_server.models.Asignatura;
import java.util.List;


@Service
public class AsignaturaService
{
    @Autowired
    private IAsignaturaRepository asignaturaRepository ;

    public List<Asignatura> buscaOptativasRelacionadas(Asignatura asignatura)
    {
        return this.asignaturaRepository.buscaOptativasRelacionadas(asignatura);
    }
}
