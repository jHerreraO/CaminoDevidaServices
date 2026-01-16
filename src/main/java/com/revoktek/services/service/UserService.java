package com.revoktek.services.service;

import com.revoktek.services.model.User;
import com.revoktek.services.model.logs.LoginLog;
import com.revoktek.services.repository.UserRepository;
import com.revoktek.services.repository.logs.LoginLogRepository;
import com.revoktek.services.rulesException.ModelNotFoundException;
import com.revoktek.services.utils.LocaleUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@RequiredArgsConstructor
@Service
@Log4j2
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginLogRepository loginLogRepository;


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

    public void changeStatus(Long id) throws ModelNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new ModelNotFoundException(User.class, id));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public Boolean validatePassword(String username, String password) {
        User userFind = userRepository.findFirstByUsernameOrderByDateRegisterDesc(username);
        System.out.println(userFind);
        if (userFind == null)
            return false;
        return bCryptPasswordEncoder.matches(password, userFind.getPassword());
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



}


