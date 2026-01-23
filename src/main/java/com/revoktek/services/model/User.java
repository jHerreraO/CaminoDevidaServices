package com.revoktek.services.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.revoktek.services.model.enums.Authority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    //Datos base de Plantilla
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;
    private String username;
    private String password;
    private LocalDateTime dateRegister;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "authority")
    private List<Authority> authorities = new ArrayList<>();
    private boolean enabled;
    private String userRegister;

    //Datos de Negocio
    private Integer age;
    private String names;
    private String phone;
    private String paternalSurname;
    private String maternalSurname;
    private String residencyCity;
    private Integer numberDependents;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String dependents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupMember> groups = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<WorshipMember> worships = new ArrayList<>();



    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream().map(permiso -> new SimpleGrantedAuthority(permiso.name())).toList();
    }
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public List<Authority> getSimpleAuthorities() {
         return authorities;
    }

    public List<String> getSimpleAuthoritiesStr() {
        return authorities.stream()
                .map(Authority::name) // Asumiendo que Authority es un enum
                .collect(Collectors.toList());
    }

    @PrePersist
    public void prePersist() {
        dateRegister = LocalDateTime.now();
        enabled = true;
    }

}
