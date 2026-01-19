package com.revoktek.services.controller;

import com.revoktek.services.model.User;
import com.revoktek.services.model.dto.users.UserSaveDTO;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.rulesException.DuplicateModelException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.service.UserService;
import com.revoktek.services.utils.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UsersController {

    private final UserService userService;

    /**
     * Registra un nuevo usuario y actualiza casas asociadas.
     *
     * @param saveUserDTO DTO con datos para crear el usuario.
     * @return Mensaje de éxito si el registro fue exitoso.
     * @throws DuplicateModelException     Si el email o teléfono ya existen.
     */
    @PostMapping("/save")
    public ResponseEntity<Message> save(@RequestBody @Valid UserSaveDTO saveUserDTO)
            throws DuplicateModelException {
        userService.save(saveUserDTO);
        return ResponseEntity.ok(new Message(true, "Registro completdo con exito, inicie sesion para acceder a su cuenta"));
    }

    /**
     * Obtiene un usuario específico por su identificador único.
     * <p>
     * Este endpoint permite recuperar la información completa de un usuario a partir de su ID.
     * Si el ID no existe en la base de datos, se lanza una excepción {@link java.util.NoSuchElementException}.
     * </p>
     *
     * @param id Identificador único del usuario a buscar.
     * @return ResponseEntity con los datos del usuario en caso de éxito.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Message> getUserById(@PathVariable Long id) throws ModelNotFoundException {
        User user = userService.findById(id);
        return ResponseEntity.ok(new Message(true, "Usuario encontrado correctamente", user));
    }

    /**
     * Obtiene una página de usuarios filtrados y paginados.
     *
     * @param page            Número de página (base 0).
     * @param size            Tamaño de la página.
     * @param descendant      True para ordenar descendente, false ascendente.
     * @param authority       (Opcional) Filtro por rol del usuario.
     * @param username        (Opcional) Filtro por nombre de usuario (contiene).
     * @param names           (Opcional) Filtro por nombres (contiene).
     * @param paternalSurname (Opcional) Filtro por apellido paterno (contiene).
     * @param maternalSurname (Opcional) Filtro por apellido materno (contiene).
     * @param residencyCity   (Opcional) Filtro por ciudad de residencia (contiene).
     * @param age             (Opcional) Filtro por edad.
     * @param enabled         (Opcional) Filtro por estado habilitado.
     * @return Página de usuarios filtrada y paginada.
     */
    @GetMapping
    public ResponseEntity<Page<User>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean descendant,
            @RequestParam(required = false) Authority authority,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String names,
            @RequestParam(required = false) String paternalSurname,
            @RequestParam(required = false) String maternalSurname,
            @RequestParam(required = false) String residencyCity,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Boolean enabled) {

        Sort sort = Sort.by(descendant ? Sort.Direction.DESC : Sort.Direction.ASC, "idUser");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userService.findUsers(
                authority, username, names, paternalSurname, maternalSurname, residencyCity, age, enabled, pageable);

        return ResponseEntity.ok(userPage);
    }

    /**
     * Cambia el estado habilitado/deshabilitado de un usuario.
     *
     * @param idUser ID del usuario cuyo estado cambiará.
     * @return Mensaje con el nuevo estado del usuario.
     * @throws ModelNotFoundException Si el usuario no existe.
     */
    @PatchMapping("/{idUser}")
    public ResponseEntity<Message> changeStatus(@PathVariable Long idUser) throws ModelNotFoundException {
        String estatus = userService.changeStatus(idUser);
        return ResponseEntity.ok(new Message(true, "El usuario ahora está " + estatus));
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param idUser          ID del usuario a actualizar.
     * @param username        (Opcional) Nuevo username/email.
     * @param age             (Opcional) Nueva edad.
     * @param names           (Opcional) Nuevos nombres.
     * @param paternalSurname (Opcional) Nuevo apellido paterno.
     * @param maternalSurname (Opcional) Nuevo apellido materno.
     * @param residenceCity   (Opcional) Nueva ciudad de residencia.
     * @param dependents      (Opcional) Dependientes.
     *
     * @return Mensaje de confirmación.
     *
     * @throws DuplicateModelException Si el username ya existe.
     */
    @PutMapping("/{idUser}")
    public ResponseEntity<Message> updateById(
            @PathVariable Long idUser,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String names,
            @RequestParam(required = false) String paternalSurname,
            @RequestParam(required = false) String maternalSurname,
            @RequestParam(required = false) String residenceCity,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String dependents
    ) throws DuplicateModelException {

        userService.updateUser(
                idUser,
                username,
                age,
                names,
                paternalSurname,
                maternalSurname,
                residenceCity,
                phone,
                dependents
        );

        return ResponseEntity.ok(new Message(true, "Usuario actualizado con éxito"));
    }








}
