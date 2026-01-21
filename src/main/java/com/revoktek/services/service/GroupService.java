package com.revoktek.services.service;


import com.revoktek.services.model.Group;
import com.revoktek.services.model.GroupMember;
import com.revoktek.services.model.User;
import com.revoktek.services.model.dto.groups.GroupAssignInstructorsDTO;
import com.revoktek.services.model.dto.groups.GroupListDTO;
import com.revoktek.services.model.dto.groups.GroupSaveDTO;
import com.revoktek.services.model.dto.groups.PublicGroupDTO;
import com.revoktek.services.model.dto.memberGroups.GroupDetailDTO;
import com.revoktek.services.model.dto.memberGroups.GroupUserDTO;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.model.enums.GroupRole;
import com.revoktek.services.repository.GroupMemberRepository;
import com.revoktek.services.repository.GroupRepository;
import com.revoktek.services.repository.UserRepository;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
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
    private final GroupMemberRepository groupMemberRepository;

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

    /**
     * Obtiene los grupos asignados al instructor autenticado.
     *
     * Reglas de negocio:
     * - Solo usuarios con rol INSTRUCTOR pueden acceder
     * - Solo se retornan grupos donde el usuario esté asignado como INSTRUCTOR
     * - Se devuelve información básica del grupo (DTO), evitando ciclos y proxies Hibernate
     *
     * @return lista de grupos asignados al instructor en sesión
     */
    @Transactional(readOnly = true)
    public List<GroupListDTO> findMyInstructorGroups() {

        User instructor = utilService.userInSession();

        // Validación de rol a nivel negocio
        if (!instructor.getSimpleAuthorities().contains(Authority.INSTRUCTOR)) {
            throw new IllegalStateException("El usuario no tiene rol INSTRUCTOR");
        }

        return groupMemberRepository
                .findByUserIdUserAndRole(
                        instructor.getIdUser(),
                        GroupRole.INSTRUCTOR
                )
                .stream()
                .map(member -> {
                    Group group = member.getGroup();

                    return GroupListDTO.builder()
                            .idGroup(group.getIdGroup())
                            .name(group.getName())
                            .address(group.getAddress())
                            .phone(group.getPhone())
                            .dayOfWeek(group.getDayOfWeek().name())
                            .hour(group.getHour())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicGroupDTO> findPublicGroupsByCategory(String categoryName) {

        // Buscamos grupos cuyo nombre contenga el nombre de la categoría
        List<Group> groups =
                groupRepository.findByNameContainingIgnoreCase(categoryName);

        return groups.stream()
                .map(group -> {

                    List<String> instructors =
                            group.getMembers() == null
                                    ? List.of()
                                    : group.getMembers().stream()
                                    .filter(m -> m.getRole() == GroupRole.INSTRUCTOR)
                                    .map(m -> m.getUser().getNames() + " " + m.getUser().getPaternalSurname() + " " +m.getUser().getMaternalSurname() ) // o nombre público
                                    .toList();

                    return PublicGroupDTO.builder()
                            .idGroup(group.getIdGroup())
                            .name(group.getName())
                            .address(group.getAddress())
                            .phone(group.getPhone())
                            .dayOfWeek(group.getDayOfWeek().name())
                            .hour(group.getHour())
                            .instructors(instructors) // vacío si no hay
                            .build();
                })
                .toList();
    }

    /**
     * Inscribe al usuario autenticado como MIEMBRO en un grupo.
     *
     * Reglas:
     * - El usuario debe estar autenticado
     * - El grupo debe existir
     * - No permite duplicados
     * - El rol asignado es MEMBER
     */
    @Transactional
    public void joinGroup(Long idGroup) throws ModelNotFoundException {
        User user = utilService.userInSession();

        Group group = groupRepository.findById(idGroup)
                .orElseThrow(() ->
                        new ModelNotFoundException(Group.class, idGroup)
                );

        // Verificamos si ya está inscrito
        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(m ->
                        m.getUser().getIdUser().equals(user.getIdUser())
                );

        if (alreadyMember) {
            log.info("Usuario {} ya está inscrito en el grupo {}",
                    user.getIdUser(), group.getIdGroup());
            return; // idempotente
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupRole.MEMBER)
                .build();

        group.getMembers().add(member);
        groupRepository.save(group);

        log.info("🟢 Usuario {} inscrito en el grupo {}",
                user.getIdUser(), group.getIdGroup());
    }

    /**
     * Obtiene los grupos en los que el usuario autenticado está inscrito como miembro.
     *
     * Reglas:
     * - El usuario se obtiene desde la sesión
     * - Solo se devuelven grupos donde el rol sea MEMBER
     * - No expone grupos ajenos
     *
     * @return lista de grupos inscritos
     */
    @Transactional(readOnly = true)
    public List<GroupListDTO> findMyGroups() {

        User user = utilService.userInSession();
        if (user == null) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        List<GroupMember> memberships =
                groupMemberRepository.findByUserIdUserAndRole(
                        user.getIdUser(),
                        GroupRole.MEMBER
                );

        return memberships.stream()
                .map(member -> {
                    Group group = member.getGroup();
                    return GroupListDTO.builder()
                            .idGroup(group.getIdGroup())
                            .name(group.getName())
                            .address(group.getAddress())
                            .phone(group.getPhone())
                            .dayOfWeek(group.getDayOfWeek().name())
                            .hour(group.getHour())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupDetailDTO findDetailById(Long groupId)
            throws ModelNotFoundException {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ModelNotFoundException(Group.class, groupId));

        // Instructores
        List<GroupUserDTO> instructors = group.getMembers().stream()
                .filter(m -> m.getRole() == GroupRole.INSTRUCTOR)
                .map(m -> mapToGroupUserDTO(m.getUser()))
                .toList();

        // Miembros
        List<GroupUserDTO> members = group.getMembers().stream()
                .filter(m -> m.getRole() == GroupRole.MEMBER)
                .map(m -> mapToGroupUserDTO(m.getUser()))
                .toList();

        return GroupDetailDTO.builder()
                .idGroup(group.getIdGroup())
                .name(group.getName())
                .address(group.getAddress())
                .phone(group.getPhone())
                .hour(group.getHour())
                .instructors(instructors)
                .members(members)
                .build();
    }


    private GroupUserDTO mapToGroupUserDTO(User user) {
        return GroupUserDTO.builder()
                .age(user.getAge())
                .names(user.getNames())
                .phone(user.getPhone())
                .paternalSurname(user.getPaternalSurname())
                .maternalSurname(user.getMaternalSurname())
                .residencyCity(user.getResidencyCity())
                .build();
    }

    /**
     * Permite al usuario autenticado salirse de un grupo como MIEMBRO.
     *
     * Reglas:
     * - El usuario debe estar autenticado
     * - El grupo debe existir
     * - Solo aplica para rol MEMBER
     * - Operación idempotente
     */
    @Transactional
    public void leaveGroup(Long idGroup) throws ModelNotFoundException {

        User user = utilService.userInSession();

        Group group = groupRepository.findById(idGroup)
                .orElseThrow(() ->
                        new ModelNotFoundException(Group.class, idGroup)
                );

        // Buscar la relación GroupMember específica
        GroupMember membership = groupMemberRepository
                .findByGroupIdGroupAndUserIdUserAndRole(
                        group.getIdGroup(),
                        user.getIdUser(),
                        GroupRole.MEMBER
                )
                .orElse(null);

        // Idempotencia: si no es miembro, no hacemos nada
        if (membership == null) {
            log.info(
                    "Usuario {} no está inscrito como MEMBER en el grupo {}",
                    user.getIdUser(),
                    group.getIdGroup()
            );
            return;
        }

        // Eliminamos la relación
        groupMemberRepository.delete(membership);

        log.info(
                "🔴 Usuario {} salió del grupo {}",
                user.getIdUser(),
                group.getIdGroup()
        );
    }

    /**
     * Elimina un grupo del sistema.
     *
     * Reglas:
     * - Solo ADMIN puede eliminar
     * - El grupo puede tener miembros e instructores
     * - Se eliminan todas las relaciones (GroupMember)
     */
    @Transactional
    public void deleteGroup(Long idGroup) throws ModelNotFoundException {

        User admin = utilService.userInSession();

        if (!admin.getSimpleAuthorities().contains(Authority.ADMIN)) {
            throw new IllegalStateException("No autorizado para eliminar grupos");
        }

        Group group = groupRepository.findById(idGroup)
                .orElseThrow(() ->
                        new ModelNotFoundException(Group.class, idGroup)
                );

        groupRepository.delete(group);

        log.info("🔴 Grupo {} eliminado por el administrador {}",
                group.getIdGroup(), admin.getIdUser());
    }

    /**
     * Inicializa los grupos base del sistema.
     *
     * Este método se ejecuta al arranque de la aplicación y:
     * - Verifica si los grupos ya existen en BD
     * - Crea únicamente los grupos faltantes
     * - No asigna instructores ni miembros
     * - Garantiza idempotencia (puede ejecutarse múltiples veces sin duplicar datos)
     */
    @Transactional
    public void initializeDefaultGroups() {
        List<Group> defaultGroups = List.of(
                Group.builder()
                        .name("Alpha")
                        .address("CDEV")
                        .phone("5517871515")
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .hour(LocalTime.of(19, 0))
                        .build(),

                Group.builder()
                        .name("Alpha Jóvenes")
                        .address("CDEV")
                        .phone("7773639508")
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .hour(LocalTime.of(19, 0))
                        .build(),

                Group.builder()
                        .name("Teología 1")
                        .address("Nueva Tabachín #24, Col. Tlaltenango")
                        .phone("7775643882")
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .hour(LocalTime.of(19, 0))
                        .build(),

                Group.builder()
                        .name("Teología 1")
                        .address("Plaza Novum Local 61, Jiutepec")
                        .phone("7772405143")
                        .dayOfWeek(DayOfWeek.THURSDAY)
                        .hour(LocalTime.of(19, 0))
                        .build(),

                Group.builder()
                        .name("Teología 2")
                        .address("CDEV")
                        .phone("7771033362")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .hour(LocalTime.of(18, 30))
                        .build(),

                Group.builder()
                        .name("Teología 2")
                        .address("Elvert 1 Col. Lomas de Jiutepec")
                        .phone("7775654251")
                        .dayOfWeek(DayOfWeek.THURSDAY)
                        .hour(LocalTime.of(19, 30))
                        .build(),

                Group.builder()
                        .name("Crecimiento Espiritual")
                        .address("Cafetería CDEV")
                        .phone("")
                        .dayOfWeek(DayOfWeek.THURSDAY)
                        .hour(LocalTime.of(18, 30))
                        .build(),

                Group.builder()
                        .name("Matrimonios")
                        .address("Copalera Esq. con Cuauhtémoc, Col. Lomas de Cortés")
                        .phone("7773701681")
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .hour(LocalTime.of(19, 30))
                        .build(),

                Group.builder()
                        .name("Matrimonios")
                        .address("San Gaspar 1, Villas Arosa, Pedregal de las Fuentes, Jiutepec")
                        .phone("5551939147")
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .hour(LocalTime.of(19, 0))
                        .build(),

                Group.builder()
                        .name("Escuela para Padres")
                        .address("Cafetería de CDEV")
                        .phone("7771842099")
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .hour(LocalTime.of(19, 0))
                        .build(),

                Group.builder()
                        .name("Hombres")
                        .address("CDEV")
                        .phone("5544498397")
                        .dayOfWeek(DayOfWeek.SATURDAY)
                        .hour(LocalTime.of(7, 0))
                        .build(),

                Group.builder()
                        .name("Mujeres")
                        .address("CDEV")
                        .phone("7773757879")
                        .dayOfWeek(DayOfWeek.SATURDAY)
                        .hour(LocalTime.of(8, 0))
                        .build(),

                Group.builder()
                        .name("GP Fit")
                        .address("Parque Chapultepec")
                        .phone("7773843556")
                        .dayOfWeek(DayOfWeek.SATURDAY)
                        .hour(LocalTime.of(8, 0))
                        .build()
        );


        for (Group group : defaultGroups) {

            boolean exists = groupRepository.existsByNameAndDayOfWeek(
                    group.getName(),
                    group.getDayOfWeek()
            );

            if (exists) {
                log.info("🟡 Grupo ya existe, se omite inicialización: {}", group.getName());
                continue;
            }

            groupRepository.save(group);
            log.info("🟢 Grupo inicializado: {}", group.getName());
        }
    }



}
