package com.revoktek.services.service;

import com.revoktek.services.model.*;
import com.revoktek.services.model.dto.specialEventMembers.SpecialEventsDetailDTO;
import com.revoktek.services.model.dto.specialEventMembers.SpecialEventsUserDTO;
import com.revoktek.services.model.dto.specialEvents.SpecialEventsListDTO;
import com.revoktek.services.model.dto.specialEvents.SpecialEventsSaveDTO;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.model.enums.GroupRole;
import com.revoktek.services.repository.SpecialEventMemberRepository;
import com.revoktek.services.repository.SpecialEventRepository;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.rulesException.SpecialEventExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
@Log4j2
public class SpecialEventService {
    private final UtilService utilService;
    private final SpecialEventRepository specialEventRepository;
    private final SpecialEventMemberRepository specialEventMemberRepository;

    /**
     * Obtiene un listado preliminar de todos los eventosEspecialess registrados.
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
     * @return Lista de eventosEspecialess en formato WorkshipListDTO
     */
    @Transactional(readOnly = true)
    public List<SpecialEventsListDTO> findAll() {

        return specialEventRepository.findAll()
                .stream()
                .map(specialEvent -> SpecialEventsListDTO.builder()
                        .idSpecialEvent(specialEvent.getIdSpecialEvent())
                        .name(specialEvent.getName())
                        .address(specialEvent.getAddress())
                        .phone(specialEvent.getPhone())
                        .dayOfWeek(specialEvent.getDayOfWeek().name())
                        .hour(specialEvent.getHour())
                        .slotsRemaining(specialEvent.getSlotsRemaining())
                        .numberOfSlots(specialEvent.getNumberOfSlots())
                        .build()
                )
                .toList();
    }

    /**
     * Registra un nuevo eventosEspeciales en el sistema.
     *
     * Responsabilidades:
     * - Validar el día de la semana recibido desde el frontend
     * - Construir la entidad SpecialEvent con los datos de negocio
     * - Asociar automáticamente al usuario responsable desde la sesión
     * - Asegurar que la categoría exista o sea creada
     * - Persistir el eventosEspeciales en base de datos
     *
     * Consideraciones:
     * - dayOfWeek se recibe como String para facilitar el consumo desde el frontend
     * - Se valida y convierte explícitamente al enum DayOfWeek
     * - Se lanza EnumInvalidArgumentException ante valores inválidos
     *
     * @param dto Datos necesarios para registrar el eventosEspeciales
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no corresponde a DayOfWeek
     */
    public void save(SpecialEventsSaveDTO dto) throws EnumInvalidArgumentException {

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

        // Construcción de la entidad SpecialEvent
        // Solo se setean campos propios del eventosEspeciales
        SpecialEvent specialEvent = SpecialEvent.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .dayOfWeek(dayOfWeek)
                .hour(dto.getHour())
                .numberOfSlots(dto.getNumberOfSlots())
                .slotsRemaining(dto.getNumberOfSlots())
                .build();

        // Asignación automática del usuario responsable desde el contexto de seguridad
        specialEvent.setUserResponsible(utilService.userInSession());

        // Persistencia del eventosEspeciales
        specialEventRepository.save(specialEvent);

        log.info("🟢 Evento Especial registrado con éxito: {}", dto.getName());
    }

    /**
     * Actualiza de forma parcial los datos de un eventosEspeciales existente.
     *
     * <p>
     * Reglas de negocio:
     * <ul>
     *   <li>El eventosEspeciales debe existir</li>
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
     * @param specialEventId ID del eventosEspeciales a modificar
     * @throws ModelNotFoundException       Si el eventosEspeciales no existe
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no es válido
     */
    @Transactional
    public void update(SpecialEventsSaveDTO dto, Long specialEventId)
            throws EnumInvalidArgumentException, ModelNotFoundException {

        // 1️⃣ Validar existencia del eventosEspeciales
        SpecialEvent specialEvent = specialEventRepository.findById(specialEventId)
                .orElseThrow(() -> new ModelNotFoundException(SpecialEvent.class, specialEventId));

        // 2️⃣ Actualizar campos simples solo si vienen en el DTO
        if (dto.getName() != null) {
            specialEvent.setName(dto.getName());
        }

        if (dto.getAddress() != null) {
            specialEvent.setAddress(dto.getAddress());
        }

        if (dto.getPhone() != null) {
            specialEvent.setPhone(dto.getPhone());
        }

        if (dto.getHour() != null) {
            specialEvent.setHour(dto.getHour());
        }

        if (dto.getNumberOfSlots() != null && dto.getNumberOfSlots() >= (specialEvent.getNumberOfSlots() - specialEvent.getSlotsRemaining())) {
            specialEvent.setNumberOfSlots(dto.getNumberOfSlots());
        }

        // 3️⃣ Conversión y validación del día de la semana
        if (dto.getDayOfWeek() != null) {
            try {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(
                        dto.getDayOfWeek().toUpperCase(Locale.ROOT)
                );
                specialEvent.setDayOfWeek(dayOfWeek);
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
        specialEventRepository.save(specialEvent);

        log.info("🟡 Evento Especial actualizado con éxito. ID: {}", specialEventId);
    }


    /**
     * Inscribe al usuario autenticado como MIEMBRO en un eventosEspeciales.
     *
     * Reglas:
     * - El usuario debe estar autenticado
     * - El eventosEspeciales debe existir
     * - No permite duplicados
     * - El rol asignado es MEMBER
     */
    @Transactional
    public void joinWorship(Long idSpecialEvent) throws ModelNotFoundException {
        User user = utilService.userInSession();

        SpecialEvent specialEvent = specialEventRepository.findById(idSpecialEvent)
                .orElseThrow(() ->
                        new ModelNotFoundException(SpecialEvent.class, idSpecialEvent)
                );

        // Verificamos si ya está inscrito
        boolean alreadyMember = specialEvent.getMembers().stream()
                .anyMatch(m ->
                        m.getUser().getIdUser().equals(user.getIdUser())
                );

        if (alreadyMember) {
            log.info("Usuario {} ya está inscrito en el evento {}",
                    user.getIdUser(), specialEvent.getIdSpecialEvent());
            return; // idempotente
        }

        int slotsRemaining = specialEvent.getSlotsRemaining() - (user.getNumberDependents() + 1);

        if(specialEvent.getSlotsRemaining() <= 0 || slotsRemaining <= 0){
            throw new SpecialEventExpiredException("Ya no quedan cupos para inscribirse en este evento");
        }

        SpecialEventMember member = SpecialEventMember.builder()
                .specialEvent(specialEvent)
                .user(user)
                .role(GroupRole.MEMBER)
                .build();

        specialEvent.getMembers().add(member);
        specialEvent.setSlotsRemaining(slotsRemaining);
        specialEventRepository.save(specialEvent);

        log.info("🟢 Usuario {} inscrito en el Evento Especial {}",
                user.getIdUser(), specialEvent.getIdSpecialEvent());
    }

    /**
     * Obtiene los eventosEspecialess en los que el usuario autenticado está inscrito como miembro.
     *
     * Reglas:
     * - El usuario se obtiene desde la sesión
     * - Solo se devuelven eventosEspecialess donde el rol sea MEMBER
     * - No expone eventosEspecialess ajenos
     *
     * @return lista de eventosEspecialess inscritos
     */
    @Transactional(readOnly = true)
    public List<SpecialEventsListDTO> findMySpecialEvents() {

        User user = utilService.userInSession();
        if (user == null) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        List<SpecialEventMember> memberships =
                specialEventMemberRepository.findByUserIdUserAndRole(
                        user.getIdUser(),
                        GroupRole.MEMBER
                );

        return memberships.stream()
                .map(member -> {
                    SpecialEvent specialEvent = member.getSpecialEvent();
                    return SpecialEventsListDTO.builder()
                            .idSpecialEvent(specialEvent.getIdSpecialEvent())
                            .name(specialEvent.getName())
                            .address(specialEvent.getAddress())
                            .phone(specialEvent.getPhone())
                            .dayOfWeek(specialEvent.getDayOfWeek().name())
                            .hour(specialEvent.getHour())
                            .numberOfSlots(specialEvent.getNumberOfSlots())
                            .slotsRemaining(specialEvent.getSlotsRemaining())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public SpecialEventsDetailDTO findDetailById(Long groupId)
            throws ModelNotFoundException {

        SpecialEvent specialEvent = specialEventRepository.findById(groupId)
                .orElseThrow(() -> new ModelNotFoundException(Worship.class, groupId));


        // Miembros
        List<SpecialEventsUserDTO> members = specialEvent.getMembers().stream()
                .filter(m -> m.getRole() == GroupRole.MEMBER)
                .map(m -> mapToSpecialEventUserDTO(m.getUser()))
                .toList();

        return SpecialEventsDetailDTO.builder()
                .idSpecialEvent(specialEvent.getIdSpecialEvent())
                .name(specialEvent.getName())
                .address(specialEvent.getAddress())
                .phone(specialEvent.getPhone())
                .hour(specialEvent.getHour())
                .members(members)
                .numberOfSlots(specialEvent.getNumberOfSlots())
                .slotsRemaining(specialEvent.getSlotsRemaining())
                .build();
    }

    private SpecialEventsUserDTO mapToSpecialEventUserDTO(User user) {
        return SpecialEventsUserDTO.builder()
                .age(user.getAge())
                .names(user.getNames())
                .phone(user.getPhone())
                .paternalSurname(user.getPaternalSurname())
                .maternalSurname(user.getMaternalSurname())
                .residencyCity(user.getResidencyCity())
                .build();
    }

    /**
     * Permite al usuario autenticado salirse de un eventosEspeciales como MIEMBRO.
     *
     * Reglas:
     * - El usuario debe estar autenticado
     * - El eventosEspeciales debe existir
     * - Solo aplica para rol MEMBER
     * - Operación idempotente
     */
    @Transactional
    public void leaveSpecialEvent(Long idSpecialEvent) throws ModelNotFoundException {

        User user = utilService.userInSession();

        SpecialEvent specialEvent = specialEventRepository.findById(idSpecialEvent)
                .orElseThrow(() ->
                        new ModelNotFoundException(SpecialEvent.class, idSpecialEvent)
                );

        // Buscamos la membresía del usuario
        SpecialEventMember membership = specialEventMemberRepository
                .findBySpecialEventIdSpecialEventAndUserIdUserAndRole(
                        specialEvent.getIdSpecialEvent(),
                        user.getIdUser(),
                        GroupRole.MEMBER
                )
                .orElse(null);

        // Idempotencia: si no está inscrito, no hacemos nada
        if (membership == null) {
            log.info(
                    "Usuario {} no está inscrito como MEMBER en el evento especial {}",
                    user.getIdUser(),
                    specialEvent.getIdSpecialEvent()
            );
            return;
        }

        // Cupos que este usuario ocupó
        int slotsToRestore = (user.getNumberDependents() != null
                ? user.getNumberDependents()
                : 0) + 1;

        // Restauramos cupos sin pasar el máximo permitido
        int updatedSlots = specialEvent.getSlotsRemaining() + slotsToRestore;

        if (updatedSlots > specialEvent.getNumberOfSlots()) {
            updatedSlots = specialEvent.getNumberOfSlots();
        }

        // Eliminamos la relación
        specialEventMemberRepository.delete(membership);

        // Actualizamos cupos
        specialEvent.setSlotsRemaining(updatedSlots);
        specialEventRepository.save(specialEvent);

        log.info(
                "🔴 Usuario {} salió del evento especial {} | Cupos restaurados: {} | Disponibles: {}",
                user.getIdUser(),
                specialEvent.getIdSpecialEvent(),
                slotsToRestore,
                updatedSlots
        );
    }


    /**
     * Elimina un eventosEspeciales del sistema.
     *
     * Reglas:
     * - Solo ADMIN puede eliminar
     * - El eventosEspeciales puede tener miembros e instructores
     * - Se eliminan todas las relaciones (GroupMember)
     */
    @Transactional
    public void deleteSpecialEvent(Long idSpecialEvent) throws ModelNotFoundException {

        User admin = utilService.userInSession();

        if (!admin.getSimpleAuthorities().contains(Authority.ADMIN)) {
            throw new IllegalStateException("No autorizado para eliminar eventos Especialess");
        }

        SpecialEvent specialEvent = specialEventRepository.findById(idSpecialEvent)
                .orElseThrow(() ->
                        new ModelNotFoundException(SpecialEvent.class, idSpecialEvent)
                );

        specialEventRepository.delete(specialEvent);

        log.info("🔴 Evento Especial {} eliminado por el administrador {}",
                specialEvent.getIdSpecialEvent(), admin.getIdUser());
    }



}
