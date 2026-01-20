package com.revoktek.services.service;


import com.revoktek.services.model.Group;
import com.revoktek.services.model.GroupMember;
import com.revoktek.services.model.User;
import com.revoktek.services.model.dto.groups.GroupAssignInstructorsDTO;
import com.revoktek.services.model.dto.groups.GroupListDTO;
import com.revoktek.services.model.dto.groups.GroupSaveDTO;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.model.enums.GroupRole;
import com.revoktek.services.repository.GroupRepository;
import com.revoktek.services.repository.UserRepository;
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
public class GroupService {
    private final GroupRepository groupRepository;
    private final CategoryService categoryService;
    private final UtilService utilService;
    private final UserRepository userRepository;

    /**
     * Obtiene un listado preliminar de todos los grupos registrados.
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
     * @return Lista de grupos en formato GroupListDTO
     */
    @Transactional(readOnly = true)
    public List<GroupListDTO> findAll() {

        return groupRepository.findAll()
                .stream()
                .map(group -> GroupListDTO.builder()
                        .idGroup(group.getIdGroup())
                        .name(group.getName())
                        .address(group.getAddress())
                        .phone(group.getPhone())
                        .dayOfWeek(group.getDayOfWeek().name())
                        .hour(group.getHour())
                        .build()
                )
                .toList();
    }

    /**
     * Registra un nuevo grupo en el sistema.
     *
     * Responsabilidades:
     * - Validar el día de la semana recibido desde el frontend
     * - Construir la entidad Group con los datos de negocio
     * - Asociar automáticamente al usuario responsable desde la sesión
     * - Asegurar que la categoría exista o sea creada
     * - Persistir el grupo en base de datos
     *
     * Consideraciones:
     * - dayOfWeek se recibe como String para facilitar el consumo desde el frontend
     * - Se valida y convierte explícitamente al enum DayOfWeek
     * - Se lanza EnumInvalidArgumentException ante valores inválidos
     *
     * @param dto Datos necesarios para registrar el grupo
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no corresponde a DayOfWeek
     */
    public void save(GroupSaveDTO dto) throws EnumInvalidArgumentException {

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

        // Construcción de la entidad Group
        // Solo se setean campos propios del grupo
        Group group = Group.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .dayOfWeek(dayOfWeek)
                .hour(dto.getHour())
                .build();

        // Asignación automática del usuario responsable desde el contexto de seguridad
        group.setUserResponsible(utilService.userInSession());

        // Garantiza que la categoría exista (o se cree) antes de persistir el grupo
        categoryService.save(dto.getNameCategory());

        // Persistencia del grupo
        groupRepository.save(group);

        log.info("🟢 Grupo registrado con éxito: {}", dto.getName());
    }


    /**
     * Asigna instructores a un grupo existente.
     *
     * Flujo:
     * 1. Validar existencia del grupo
     * 2. Validar existencia de usuarios
     * 3. Validar que tengan rol INSTRUCTOR
     * 4. Evitar duplicados (unique constraint)
     * 5. Crear relación GroupMember
     */
    @Transactional
    public void assignInstructors(GroupAssignInstructorsDTO dto)
            throws ModelNotFoundException {

        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new ModelNotFoundException(Group.class, dto.getGroupId()));

        for (Long instructorId : dto.getInstructorIds()) {

            User instructor = userRepository.findById(instructorId)
                    .orElseThrow(() -> new ModelNotFoundException(User.class, instructorId));

            // Validación de rol de seguridad
            if (!instructor.getSimpleAuthorities().contains(Authority.INSTRUCTOR)) {
                log.warn("Usuario {} no tiene rol INSTRUCTOR", instructorId);
                continue; // o lanzar excepción de negocio si lo prefieres
            }

            boolean alreadyAssigned = group.getMembers().stream()
                    .anyMatch(m ->
                            m.getUser().getIdUser().equals(instructorId)
                                    && m.getRole() == GroupRole.INSTRUCTOR
                    );

            if (alreadyAssigned) {
                continue; // idempotente
            }

            GroupMember member = GroupMember.builder()
                    .group(group)
                    .user(instructor)
                    .role(GroupRole.INSTRUCTOR)
                    .build();

            group.getMembers().add(member);
        }

        groupRepository.save(group);
        log.info("Instructores asignados al grupo {}", dto.getGroupId());
    }

    /**
     * Actualiza de forma parcial los datos de un grupo existente.
     *
     * <p>
     * Reglas de negocio:
     * <ul>
     *   <li>El grupo debe existir</li>
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
     * @param groupId ID del grupo a modificar
     * @throws ModelNotFoundException       Si el grupo no existe
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no es válido
     */
    @Transactional
    public void update(GroupSaveDTO dto, Long groupId)
            throws EnumInvalidArgumentException, ModelNotFoundException {

        // 1️⃣ Validar existencia del grupo
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ModelNotFoundException(Group.class, groupId));

        // 2️⃣ Actualizar campos simples solo si vienen en el DTO
        if (dto.getName() != null) {
            group.setName(dto.getName());
        }

        if (dto.getAddress() != null) {
            group.setAddress(dto.getAddress());
        }

        if (dto.getPhone() != null) {
            group.setPhone(dto.getPhone());
        }

        if (dto.getHour() != null) {
            group.setHour(dto.getHour());
        }

        // 3️⃣ Conversión y validación del día de la semana
        if (dto.getDayOfWeek() != null) {
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(
                        dto.getDayOfWeek().toUpperCase(Locale.ROOT)
                );
                group.setDayOfWeek(dayOfWeek);
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
        groupRepository.save(group);

        log.info("🟡 Grupo actualizado con éxito. ID: {}", groupId);
    }




}
