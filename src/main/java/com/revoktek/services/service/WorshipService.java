package com.revoktek.services.service;

import com.revoktek.services.model.*;
import com.revoktek.services.model.dto.memberWorkships.WorshipDetailDTO;
import com.revoktek.services.model.dto.memberWorkships.WorshipUserDTO;
import com.revoktek.services.model.dto.workships.WorshipListDTO;
import com.revoktek.services.model.dto.workships.WorshipSaveDTO;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.model.enums.GroupRole;
import com.revoktek.services.repository.WorshipMemberRepository;
import com.revoktek.services.repository.WorshipRepository;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
@Log4j2
public class WorshipService {
    private final UtilService utilService;
    private final WorshipRepository worshipRepository;
    private final WorshipMemberRepository worshipMemberRepository;


    /**
     * Obtiene un listado preliminar de todos los cultos registrados.
     *
     * Uso:
     * - Vista general (pantallas de listado)
     * - No incluye miembros ni relaciones pesadas
     * - Optimizado para consultas rápidas
     *
     * Consideraciones:
     * - readOnly = true para evitar flush innecesarios
     * - Se retorna DTO en lugar de entidad para proteger el modelo
     * - dayOfWeek se expone como String para facilidad de consumo en frontend
     *
     * @return Lista de cultos en formato WorkshipListDTO
     */
    @Transactional(readOnly = true)
    public List<WorshipListDTO> findAll() {

        return worshipRepository.findAll()
                .stream()
                .map(workship -> WorshipListDTO.builder()
                        .idWorship(workship.getIdWorship())
                        .name(workship.getName())
                        .address(workship.getAddress())
                        .phone(workship.getPhone())
                        .dayOfWeek(workship.getDayOfWeek().name())
                        .hour(workship.getHour())
                        .build()
                )
                .toList();
    }


    /**
     * Registra un nuevo culto en el sistema.
     *
     * Responsabilidades:
     * - Validar el día de la semana recibido desde el frontend
     * - Construir la entidad Workship con los datos de negocio
     * - Asociar automáticamente al usuario responsable desde la sesión
     * - Asegurar que la categoría exista o sea creada
     * - Persistir el culto en base de datos
     *
     * Consideraciones:
     * - dayOfWeek se recibe como String para facilitar el consumo desde el frontend
     * - Se valida y convierte explícitamente al enum DayOfWeek
     * - Se lanza EnumInvalidArgumentException ante valores inválidos
     *
     * @param dto Datos necesarios para registrar el culto
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no corresponde a DayOfWeek
     */
    public void save(WorshipSaveDTO dto) throws EnumInvalidArgumentException {

        // Conversión segura del día de la semana recibido desde frontend (String → Enum)
        // Se normaliza a mayúsculas para evitar problemas de formato
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            // Excepción de negocio: el valor no pertenece al enum DayOfWeek
            throw new EnumInvalidArgumentException(
                    "dayOfWeek",
                    dto.getDayOfWeek(),
                    DayOfWeek.class
            );
        }

        // Construcción de la entidad Worship
        // Solo se setean campos propios del culto
        Worship worship = Worship.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .dayOfWeek(dayOfWeek)
                .hour(dto.getHour())
                .build();

        // Asignación automática del usuario responsable desde el contexto de seguridad
        worship.setUserResponsible(utilService.userInSession());

        // Persistencia del culto
        worshipRepository.save(worship);

        log.info("🟢 Culto registrado con éxito: {}", dto.getName());
    }


    /**
     * Actualiza de forma parcial los datos de un culto existente.
     *
     * <p>
     * Reglas de negocio:
     * <ul>
     *   <li>El culto debe existir</li>
     *   <li>Los campos del DTO son opcionales</li>
     *   <li>Solo se actualizan los valores presentes en el DTO</li>
     *   <li>El día de la semana se valida contra el enum {@link DayOfWeek}</li>
     * </ul>
     *
     * <p>
     * Consideraciones técnicas:
     * <ul>
     *   <li>La entidad se obtiene desde base de datos para mantenerla administrada por JPA</li>
     *   <li>No se reconstruye la entidad (evita pérdida de relaciones y PK)</li>
     *   <li>Se aprovecha el dirty checking de Hibernate</li>
     * </ul>
     *
     * @param dto     DTO con los datos a actualizar (parcial)
     * @param workshipId ID del culto a modificar
     * @throws ModelNotFoundException       Si el culto no existe
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no es válido
     */
    @Transactional
    public void update(WorshipSaveDTO dto, Long workshipId)
            throws EnumInvalidArgumentException, ModelNotFoundException {

        // 1️⃣ Validar existencia del culto
        Worship worship = worshipRepository.findById(workshipId)
                .orElseThrow(() -> new ModelNotFoundException(Worship.class, workshipId));

        // 2️⃣ Actualizar campos simples solo si vienen en el DTO
        if (dto.getName() != null) {
            worship.setName(dto.getName());
        }

        if (dto.getAddress() != null) {
            worship.setAddress(dto.getAddress());
        }

        if (dto.getPhone() != null) {
            worship.setPhone(dto.getPhone());
        }

        if (dto.getHour() != null) {
            worship.setHour(dto.getHour());
        }

        // 3️⃣ Conversión y validación del día de la semana
        if (dto.getDayOfWeek() != null) {
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(
                        dto.getDayOfWeek().toUpperCase(Locale.ROOT)
                );
                worship.setDayOfWeek(dayOfWeek);
            } catch (Exception e) {
                throw new EnumInvalidArgumentException(
                        "dayOfWeek",
                        dto.getDayOfWeek(),
                        DayOfWeek.class
                );
            }
        }

        // 4️⃣ Persistencia
        // Hibernate detecta cambios automáticamente (dirty checking)
        worshipRepository.save(worship);

        log.info("🟡 culto actualizado con éxito. ID: {}", workshipId);
    }


    /**
     * Inscribe al usuario autenticado como MIEMBRO en un culto.
     *
     * Reglas:
     * - El usuario debe estar autenticado
     * - El culto debe existir
     * - No permite duplicados
     * - El rol asignado es MEMBER
     */
    @Transactional
    public void joinWorship(Long idWorkshhip) throws ModelNotFoundException {
        User user = utilService.userInSession();

        Worship worship = worshipRepository.findById(idWorkshhip)
                .orElseThrow(() ->
                        new ModelNotFoundException(Worship.class, idWorkshhip)
                );

        // Verificamos si ya está inscrito
        boolean alreadyMember = worship.getMembers().stream()
                .anyMatch(m ->
                        m.getUser().getIdUser().equals(user.getIdUser())
                );

        if (alreadyMember) {
            log.info("Usuario {} ya está inscrito en el culto {}",
                    user.getIdUser(), worship.getIdWorship());
            return; // idempotente
        }

        WorshipMember member = WorshipMember.builder()
                .worship(worship)
                .user(user)
                .role(GroupRole.MEMBER)
                .build();

        worship.getMembers().add(member);
        worshipRepository.save(worship);

        log.info("🟢 Usuario {} inscrito en el culto {}",
                user.getIdUser(), worship.getIdWorship());
    }


    /**
     * Obtiene los cultos en los que el usuario autenticado está inscrito como miembro.
     *
     * Reglas:
     * - El usuario se obtiene desde la sesión
     * - Solo se devuelven cultos donde el rol sea MEMBER
     * - No expone cultos ajenos
     *
     * @return lista de cultos inscritos
     */
    @Transactional(readOnly = true)
    public List<WorshipListDTO> findMyWorships() {

        User user = utilService.userInSession();
        if (user == null) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        List<WorshipMember> memberships =
                worshipMemberRepository.findByUserIdUserAndRole(
                        user.getIdUser(),
                        GroupRole.MEMBER
                );

        return memberships.stream()
                .map(member -> {
                    Worship worship = member.getWorship();
                    return WorshipListDTO.builder()
                            .idWorship(worship.getIdWorship())
                            .name(worship.getName())
                            .address(worship.getAddress())
                            .phone(worship.getPhone())
                            .dayOfWeek(worship.getDayOfWeek().name())
                            .hour(worship.getHour())
                            .build();
                })
                .toList();
    }


    @Transactional(readOnly = true)
    public WorshipDetailDTO findDetailById(Long groupId)
            throws ModelNotFoundException {

        Worship worship = worshipRepository.findById(groupId)
                .orElseThrow(() -> new ModelNotFoundException(Worship.class, groupId));
        

        // Miembros
        List<WorshipUserDTO> members = worship.getMembers().stream()
                .filter(m -> m.getRole() == GroupRole.MEMBER)
                .map(m -> mapToWorkshipUserDTO(m.getUser()))
                .toList();

        return WorshipDetailDTO.builder()
                .idWorhship(worship.getIdWorship())
                .name(worship.getName())
                .address(worship.getAddress())
                .phone(worship.getPhone())
                .hour(worship.getHour())
                .members(members)
                .build();
    }


    private WorshipUserDTO mapToWorkshipUserDTO(User user) {
        return WorshipUserDTO.builder()
                .age(user.getAge())
                .names(user.getNames())
                .phone(user.getPhone())
                .paternalSurname(user.getPaternalSurname())
                .maternalSurname(user.getMaternalSurname())
                .residencyCity(user.getResidencyCity())
                .build();
    }


    /**
     * Permite al usuario autenticado salirse de un culto como MIEMBRO.
     *
     * Reglas:
     * - El usuario debe estar autenticado
     * - El culto debe existir
     * - Solo aplica para rol MEMBER
     * - Operación idempotente
     */
    @Transactional
    public void leaveWorship(Long idWorship) throws ModelNotFoundException {

        User user = utilService.userInSession();

        Worship worship = worshipRepository.findById(idWorship)
                .orElseThrow(() ->
                        new ModelNotFoundException(Worship.class, idWorship)
                );

        // Buscar la relación worshipMember específica
        WorshipMember membership = worshipMemberRepository
                .findByWorshipIdWorshipAndUserIdUserAndRole(
                        worship.getIdWorship(),
                        user.getIdUser(),
                        GroupRole.MEMBER
                )
                .orElse(null);

        // Idempotencia: si no es miembro, no hacemos nada
        if (membership == null) {
            log.info(
                    "Usuario {} no está inscrito como MEMBER en el culto {}",
                    user.getIdUser(),
                    worship.getIdWorship()
            );
            return;
        }

        // Eliminamos la relación
        worshipMemberRepository.delete(membership);

        log.info(
                "🔴 Usuario {} salió del culto {}",
                user.getIdUser(),
                worship.getIdWorship()
        );
    }

    /**
     * Elimina un culto del sistema.
     *
     * Reglas:
     * - Solo ADMIN puede eliminar
     * - El culto puede tener miembros e instructores
     * - Se eliminan todas las relaciones (GroupMember)
     */
    @Transactional
    public void deleteWorship(Long idWorship) throws ModelNotFoundException {

        User admin = utilService.userInSession();

        if (!admin.getSimpleAuthorities().contains(Authority.ADMIN)) {
            throw new IllegalStateException("No autorizado para eliminar cultos");
        }

        Worship worship = worshipRepository.findById(idWorship)
                .orElseThrow(() ->
                        new ModelNotFoundException(Worship.class, idWorship)
                );

        worshipRepository.delete(worship);

        log.info("🔴 Culto {} eliminado por el administrador {}",
                worship.getIdWorship(), admin.getIdUser());
    }

}
