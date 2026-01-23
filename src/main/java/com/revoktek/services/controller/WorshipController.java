package com.revoktek.services.controller;


import com.revoktek.services.model.dto.memberWorkships.WorshipDetailDTO;
import com.revoktek.services.model.dto.workships.WorshipListDTO;
import com.revoktek.services.model.dto.workships.WorshipSaveDTO;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.service.WorshipService;
import com.revoktek.services.utils.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/worship")
public class WorshipController {
    private final WorshipService worshipService;


    /**
     * Obtiene el detalle completo de un culto:
     * - Información básica del culto
     * - Instructores asignados
     * - Miembros inscritos
     *
     * Endpoint pensado para vista detallada.
     */
    @GetMapping("/{worshipId}")
    public ResponseEntity<Message> findWorshipDetail(
            @PathVariable Long worshipId
    ) throws ModelNotFoundException {
        WorshipDetailDTO data = worshipService.findDetailById(worshipId);

        return ResponseEntity.ok(
                new Message(
                        true,
                        "Detalles del culto",
                        data
                )
        );
    }


    /**
     * Obtiene el listado preliminar de todos los cultos.
     *
     * @return ResponseEntity con un Message que contiene la lista de cultos
     */
    @GetMapping
    public ResponseEntity<Message> findAll() {

        return ResponseEntity.ok(
                new Message(
                        true,
                        "Lista de cultos registrados",
                        worshipService.findAll()
                )
        );
    }

    /**
     * Crea un nuevo culto.
     *
     * Responsabilidades del endpoint:
     * - Recibe los datos mínimos necesarios para la creación del culto.
     * - Delega toda la lógica de negocio y validaciones al servicio.
     * - No maneja errores ni transforma excepciones (Single Responsibility).
     *
     * Flujo general:
     * 1. El DTO llega desde el frontend (datos de presentación).
     * 2. El servicio valida y persiste la entidad.
     * 3. Las excepciones de dominio se propagan y son manejadas por el
     *    GlobalExceptionHandler (@ControllerAdvice).
     *
     * @param dto Datos necesarios para crear el culto.
     * @return ResponseEntity<Message> con confirmación de operación exitosa.
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no coincide
     *         con los valores permitidos del enum (validación de dominio).
     */
    @PostMapping("/save")
    public ResponseEntity<Message> create(@RequestBody WorshipSaveDTO dto) throws EnumInvalidArgumentException {
        worshipService.save(dto);

        return ResponseEntity.ok(
                new Message(true, "Culto registrado con exito")
        );
    }


    /**
     * Actualiza parcialmente los datos de un culto existente.
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
     * @param worshipId ID del culto a actualizar
     * @param dto     Datos a modificar
     * @return Mensaje de confirmación de la operación
     * @throws ModelNotFoundException       Si el culto no existe
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no es válido
     */
    @PutMapping("/{worshipId}")
    public ResponseEntity<Message> update(
            @PathVariable Long worshipId,
            @RequestBody WorshipSaveDTO dto
    ) throws ModelNotFoundException, EnumInvalidArgumentException {

        worshipService.update(dto, worshipId);

        return ResponseEntity.ok(
                new Message(true, "Culto actualizado con exito")
        );
    }

    /**
     * 📌 Obtiene los cultos en los que el usuario autenticado está inscrito.
     *
     * - Usa la sesión del usuario
     * - No requiere parámetros
     */
    @GetMapping("/findMemberWorships")
    public ResponseEntity<Message> findMyWorships() {

        List<WorshipListDTO> worships = worshipService.findMyWorships();

        return ResponseEntity.ok(
                new Message(
                        true,
                        "Cultos a los que estas suscrito",
                        worships
                )
        );
    }

    /**
     * 📌 Inscribe al usuario autenticado al culto indicado.
     *
     * - El usuario se obtiene desde la sesión
     * - El culto se recibe como request param
     * - No permite duplicados
     */
    @PostMapping("/join")
    public ResponseEntity<Message> joinWorship(
            @RequestParam Long idWorship
    ) throws ModelNotFoundException {
        worshipService.joinWorship(idWorship);
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
     * @param idWorship identificador del culto
     * @return mensaje de confirmación
     */
    @DeleteMapping("/leave")
    public ResponseEntity<Message> leaveWorship(
            @RequestParam Long idWorship
    ) throws ModelNotFoundException {

        worshipService.leaveWorship(idWorship);

        Message message = new Message(
                true,
                "Has salido del culto correctamente"
        );

        return ResponseEntity.ok(message);
    }

    /**
     * Elimina un culto del sistema (ADMIN).
     */
    @DeleteMapping("/{idWorship}")
    public ResponseEntity<Message> deleteWorship(
            @PathVariable Long idWorship
    ) throws ModelNotFoundException {
        worshipService.deleteWorship(idWorship);

        Message message = new Message(
                true,
                "Culto eliminado correctamente"
        );

        return ResponseEntity.ok(message);
    }
    
    
    
}
