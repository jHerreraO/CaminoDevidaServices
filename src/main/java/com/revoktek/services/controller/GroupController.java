package com.revoktek.services.controller;

import com.revoktek.services.model.dto.groups.GroupAssignInstructorsDTO;
import com.revoktek.services.model.dto.groups.GroupSaveDTO;
import com.revoktek.services.rulesException.EnumInvalidArgumentException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.service.GroupService;
import com.revoktek.services.utils.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
public class GroupController {
    private final GroupService groupService;

    /**
     * Obtiene el listado preliminar de todos los grupos.
     *
     * @return ResponseEntity con un Message que contiene la lista de grupos
     */
    @GetMapping
    public ResponseEntity<Message> findAll() {

        return ResponseEntity.ok(
                new Message(
                        true,
                        "Groups retrieved successfully",
                        groupService.findAll()
                )
        );
    }

    /**
     * Crea un nuevo grupo.
     *
     * Responsabilidades del endpoint:
     * - Recibe los datos mínimos necesarios para la creación del grupo.
     * - Delega toda la lógica de negocio y validaciones al servicio.
     * - No maneja errores ni transforma excepciones (Single Responsibility).
     *
     * Flujo general:
     * 1. El DTO llega desde el frontend (datos de presentación).
     * 2. El servicio valida y persiste la entidad.
     * 3. Las excepciones de dominio se propagan y son manejadas por el
     *    GlobalExceptionHandler (@ControllerAdvice).
     *
     * @param dto Datos necesarios para crear el grupo.
     * @return ResponseEntity<Message> con confirmación de operación exitosa.
     * @throws EnumInvalidArgumentException Si el valor de dayOfWeek no coincide
     *         con los valores permitidos del enum (validación de dominio).
     */
    @PostMapping("/save")
    public ResponseEntity<Message> create(@RequestBody GroupSaveDTO dto) throws EnumInvalidArgumentException {
        groupService.save(dto);

        return ResponseEntity.ok(
                new Message(true, "Group created successfully")
        );
    }

    /**
     * Asigna uno o varios instructores a un grupo.
     *
     * @param dto DTO con ID del grupo y lista de instructores.
     * @return Mensaje de confirmación.
     * @throws ModelNotFoundException si el grupo o algún usuario no existe.
     */
    @PostMapping("/assign-instructors")
    public ResponseEntity<Message> assignInstructors(
            @RequestBody GroupAssignInstructorsDTO dto
    ) throws ModelNotFoundException {

        groupService.assignInstructors(dto);

        return ResponseEntity.ok(
                new Message(true, "Instructores asignados correctamente al grupo")
        );
    }





}
