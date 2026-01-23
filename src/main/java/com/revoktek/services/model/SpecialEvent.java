package com.revoktek.services.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSpecialEvent;
    private String name;
    private String address;
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    private LocalTime hour;
    private Integer numberOfSlots;

    @ManyToOne
    @JoinColumn(name = "user_id_responsible", referencedColumnName = "idUser")
    private User userResponsible;

    @OneToMany(mappedBy = "specialEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SpecialEventMember> members = new ArrayList<>();

}
