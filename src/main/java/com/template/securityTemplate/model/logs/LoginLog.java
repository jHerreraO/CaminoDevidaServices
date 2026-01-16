package com.template.securityTemplate.model.logs;

import com.template.securityTemplate.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLoginLog;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String username;
    private boolean authenticated;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
}
