package com.revoktek.services.service;

import com.revoktek.services.model.User;
import com.revoktek.services.model.dto.users.UserSaveDTO;
import com.revoktek.services.model.enums.Authority;
import com.revoktek.services.model.logs.LoginLog;
import com.revoktek.services.repository.UserRepository;
import com.revoktek.services.repository.logs.LoginLogRepository;
import com.revoktek.services.rulesException.DuplicateModelException;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.specification.UserSpecification;
import com.revoktek.services.utils.LocaleUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;


@RequiredArgsConstructor
@Service
@Log4j2
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginLogRepository loginLogRepository;
    private final UtilService utilService;


    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException(String.format("Username %s not found", username));
        return user;
    }

    public User save(User user) {
        if (user.getPassword() != null)
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Cambia el estado (habilitado/deshabilitado) de un usuario.
     *
     * @param id ID del usuario.
     * @return String con el nuevo estado.
     * @throws ModelNotFoundException Si el usuario no existe.
     */
    public String changeStatus(Long id) throws ModelNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(User.class, id));

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        String estatus = user.isEnabled() ? "habilitado" : "deshabilitado";
        log.info("🟡 Usuario con ID {} está {}", id, estatus);
        return estatus;
    }

    public void logLogin(User user, HttpServletRequest request) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUser(user);
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginTime(LocalDateTime.now(LocaleUtil.defaultZoneId));
        loginLog.setIpAddress(request.getRemoteAddr());
        loginLog.setUserAgent(request.getHeader("User-Agent"));
        loginLog.setAuthenticated(true);
        log.info("User: {} is logged from: {}", user.getUsername(), request.getRemoteAddr());
        loginLogRepository.save(loginLog);
    }

    public boolean notExistsByUsername(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Este método:
     * <ul>
     *   <li>Construye la entidad {@link User} a partir del DTO {@link UserSaveDTO} usando el patrón Builder</li>
     *   <li>Encripta la contraseña antes de persistirla (BCrypt)</li>
     *   <li>Normaliza textos de entrada para evitar problemas de codificación
     *       (acentos y caracteres especiales)</li>
     *   <li>Asigna el rol del usuario en base al enum {@link Authority}</li>
     *   <li>Guarda el usuario en base de datos</li>
     *   <li>Registra quién realizó la operación</li>
     * </ul>
     *
     * <p><strong>Consideraciones de seguridad:</strong>
     * <ul>
     *   <li>La contraseña nunca se almacena en texto plano</li>
     *   <li>El rol debe corresponder a un valor válido del enum {@link Authority}</li>
     * </ul>
     *
     * @param dto Objeto de transferencia con la información necesaria para registrar un usuario
     *
     * @throws IllegalArgumentException si el rol proporcionado no coincide con ningún valor del enum {@link Authority}
     */
    public void save(UserSaveDTO dto) throws DuplicateModelException {
        if (!notExistsByUsername(dto.getUsername())) {
            throw new DuplicateModelException(User.class, dto.getUsername(), "username");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .age(dto.getAge())
                .phone(dto.getPhone())
                .names(dto.getNames())
                .paternalSurname(dto.getPaternalSurname())
                .maternalSurname(dto.getMaternalSurname())
                .residencyCity(dto.getResidenceCity())
                .dependents(dto.getDependents())
                .authorities(List.of(Authority.valueOf(dto.getRole())))
                .build();
        user.setUserRegister(utilService.userNameInSession() != null ? utilService.userNameInSession() : null);
        userRepository.save(user);

        log.info("Usuario registrado con exito con el nombre de usuario " + dto.getUsername());
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario.
     * @return Usuario encontrado.
     * @throws java.util.NoSuchElementException si no se encuentra el usuario.
     */
    public User findById(Long id) throws ModelNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException(User.class, id));
    }

    /**
     * Obtiene una página de usuarios filtrados y paginados según los parámetros opcionales.
     *
     * @param authority       (Opcional) Filtro por rol del usuario.
     * @param username        (Opcional) Filtro por nombre de usuario (contiene).
     * @param names           (Opcional) Filtro por nombres (contiene).
     * @param paternalSurname (Opcional) Filtro por apellido paterno (contiene).
     * @param maternalSurname (Opcional) Filtro por apellido materno (contiene).
     * @param residencyCity   (Opcional) Filtro por ciudad de residencia (contiene).
     * @param age             (Opcional) Filtro por edad.
     * @param enabled         (Opcional) Filtro por estado habilitado.
     * @param pageable        Información de paginación.
     * @return Página de usuarios filtrada y paginada.
     */
    public Page<User> findUsers(
            Authority authority,
            String username,
            String names,
            String paternalSurname,
            String maternalSurname,
            String residencyCity,
            Integer age,
            Boolean enabled,
            Pageable pageable) {

        Specification<User> spec = Specification.where(UserSpecification.hasAuthority(authority))
                .and(UserSpecification.usernameLike(username))
                .and(UserSpecification.namesLike(names))
                .and(UserSpecification.paternalSurnameLike(paternalSurname))
                .and(UserSpecification.maternalSurnameLike(maternalSurname))
                .and(UserSpecification.residencyCityLike(residencyCity))
                .and(UserSpecification.hasAge(age))
                .and(UserSpecification.isEnabled(enabled));

        return userRepository.findAll(spec, pageable);
    }

    /**
     * Actualiza los datos de negocio de un usuario existente.
     * <p>
     * Solo se modifican los campos que sean distintos de null,
     * preservando la información existente.
     *
     * @param idUser           ID del usuario a actualizar.
     * @param username         (Opcional) Nuevo username/email.
     * @param age              (Opcional) Nueva edad.
     * @param names            (Opcional) Nuevos nombres.
     * @param paternalSurname  (Opcional) Nuevo apellido paterno.
     * @param maternalSurname  (Opcional) Nuevo apellido materno.
     * @param residenceCity    (Opcional) Nueva ciudad de residencia.
     * @param dependents       (Opcional) Información de dependientes.
     *
     * @throws DuplicateModelException si el username ya existe.
     * @throws IllegalArgumentException si el usuario no existe.
     */
    public void updateUser(Long idUser,
                           String username,
                           Integer age,
                           String names,
                           String paternalSurname,
                           String maternalSurname,
                           String residenceCity,
                           String phone,
                           String dependents)
            throws DuplicateModelException {

        // 1. Buscar usuario
        User user = userRepository.findById(idUser)
                .orElseThrow(() ->
                        new IllegalArgumentException("User with ID " + idUser + " not found"));

        // 2. Actualizar username (validando duplicados)
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new DuplicateModelException(User.class, username, "username");
            }
            user.setUsername(username);
        }

        if (phone != null) {
            user.setPhone(phone);
        }

        // 3. Actualizar edad
        if (age != null) {
            user.setAge(age);
        }

        //  4. Actualizar nombres
        if (names != null) {
            user.setNames(names);
        }

        //  5. Apellidos
        if (paternalSurname != null) {
            user.setPaternalSurname(paternalSurname);
        }

        if (maternalSurname != null) {
            user.setMaternalSurname(maternalSurname);
        }

        // 6. Ciudad de residencia
        if (residenceCity != null) {
            user.setResidencyCity(residenceCity);
        }

        // 7. Dependientes
        if (dependents != null) {
            user.setDependents(dependents);
        }

        // 8. Guardar cambios
        userRepository.save(user);

        log.info("🟡 Usuario {} (ID: {}) actualizado con éxito", user.getUsername(), user.getIdUser());
    }

    public List<User> findAllInstructors(){
        return userRepository.findByAuthoritiesContaining(Authority.INSTRUCTOR);
    }













}


