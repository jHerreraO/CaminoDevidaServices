package com.revoktek.services.controller;

import com.revoktek.services.model.dto.memberWorkships.WorshipDetailDTO;
import com.revoktek.services.model.dto.specialEventMembers.SpecialEventsDetailDTO;
import com.revoktek.services.model.dto.specialEvents.SpecialEventsListDTO;
import com.revoktek.services.model.dto.specialEvents.SpecialEventsSaveDTO;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.service.SpecialEventService;
import com.revoktek.services.utils.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/specialEvent")
public class SpecialEventCotroller {
    private final SpecialEventService specialEventService;


    /**
     * Obtiene el detalle completo de un Evento Especial:
     * - Información básica del Evento Especial
     * - Instructores asignados
     * - Miembros inscritos
     *
     * Endpoint pensado para vista detallada.
     */
    @GetMapping("/{specialEventId}")
    public ResponseEntity<Message> findSpecialEventDetail(
            @PathVariable Long specialEventId
    ) throws ModelNotFoundException {
        SpecialEventsDetailDTO data = specialEventService.findDetailById(specialEventId);

        return ResponseEntity.ok(
                new Message(
                        true,
                        "Detalles del Evento Especial",
                        data
                )
        );
    }

    /**
     * Obtiene el listado preliminar de todos los Evento Especials.
     *
     * @return ResponseEntity con un Message que contiene la lista de Evento Especials
     */
    @GetMapping
    public ResponseEntity<Message> findAll() {

        return ResponseEntity.ok(
                new Message(
                        true,
                        "Lista de Eventos Especiales registrados",
                        specialEventService.findAll()
                )
        );
    }


    /**
     * Crea un nuevo Evento Especial.
     *
     * Responsabilidades del endpoint:
     * - Recibe los datos mínimos necesarios para la creación del Evento Especial.
     * - Delega toda la lógica de negocio y validaciones al servicio.
     * - No maneja errores ni transforma excepciones (Single Responsibility).
     *
     * Flujo general:
     * 1. El DTO llega desde el frontend (datos de presentación).
     * 2. El servicio valida y persiste la entidad.
     * 3. Las excepciones de dominio se propagan y son manejadas por el
     *    GlobalExceptionHandler (@ControllerAdvice).
     *
     * @param dto Datos necesarios para crear el Evento Especial.
     * @return ResponseEntity<Message> con confirmación de operación exitosa.
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no coincide
     *         con los valores permitidos del enum (validación de dominio).
     */
    @PostMapping("/save")
    public ResponseEntity<Message> create(@RequestBody SpecialEventsSaveDTO dto) throws EnumInvalidArgumentException {
        specialEventService.save(dto);

        return ResponseEntity.ok(
                new Message(true, "Evento Especial registrado con exito")
        );
    }

    /**
     * Actualiza parcialmente los datos de un Evento Especial existente.
     *
     * <p>
     * Los campos enviados en el cuerpo de la petición son opcionales.
     * Solo los valores presentes serán modificados.
     * </p>
     *
     * <p>
     * Las validaciones de existencia y negocio son delegadas al servicio.
     * </p>
     *
     * @param specialEventId ID del Evento Especial a actualizar
     * @param dto     Datos a modificar
     * @return Mensaje de confirmación de la operación
     * @throws ModelNotFoundException       Si el Evento Especial no existe
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no es válido
     */
    @PutMapping("/{specialEventId}")
    public ResponseEntity<Message> update(
            @PathVariable Long specialEventId,
            @RequestBody SpecialEventsSaveDTO dto
    ) throws ModelNotFoundException, EnumInvalidArgumentException {

        specialEventService.update(dto, specialEventId);

        return ResponseEntity.ok(
                new Message(true, "Evento Especial actualizado con exito")
        );
    }

    /**
     * 📌 Obtiene los Evento Especials en los que el usuario autenticado está inscrito.
     *
     * - Usa la sesión del usuario
     * - No requiere parámetros
     */
    @GetMapping("/findMemberSpecialEvent")
    public ResponseEntity<Message> findMySpecialEvents() {

        List<SpecialEventsListDTO> specialEvent = specialEventService.findMySpecialEvents();

        return ResponseEntity.ok(
                new Message(
                        true,
                        "Evento Especial a los que estas suscrito",
                        specialEvent
                )
        );
    }

    /**
     * 📌 Inscribe al usuario autenticado al Evento Especial indicado.
     *
     * - El usuario se obtiene desde la sesión
     * - El Evento Especial se recibe como request param
     * - No permite duplicados
     */
    @PostMapping("/join")
    public ResponseEntity<Message> joinWorship(
            @RequestParam Long idSpecialEvent
    ) throws ModelNotFoundException {
        specialEventService.joinWorship(idSpecialEvent);
        return ResponseEntity.ok(
                new Message(true, "Inscripcion realizada","")
        );
    }

    /**
     * Permite al usuario autenticado salirse de un culto como MIEMBRO.
     *
     * Reglas:
     * - El usuario debe estar autenticado
     * - Solo se permite salir si el rol es MEMBER
     * - Operación idempotente (si no pertenece al culto, no falla)
     *
     * @param idSpecialEvent identificador del culto
     * @return mensaje de confirmación
     */
    @DeleteMapping("/leave")
    public ResponseEntity<Message> leave(
            @RequestParam Long idSpecialEvent
    ) throws ModelNotFoundException {

        specialEventService.leaveSpecialEvent(idSpecialEvent);

        Message message = new Message(
                true,
                "Has salido del evento correctamente"
        );

        return ResponseEntity.ok(message);
    }

    /**
     * Elimina un evento del sistema (ADMIN).
     */
    @DeleteMapping("/{idSpecialEvent}")
    public ResponseEntity<Message> deleteWorship(
            @PathVariable Long idSepcialEvent
    ) throws ModelNotFoundException {
        specialEventService.deleteSpecialEvent(idSepcialEvent);

        Message message = new Message(
                true,
                "Evento eliminado correctamente"
        );

        return ResponseEntity.ok(message);
    }


    
}
