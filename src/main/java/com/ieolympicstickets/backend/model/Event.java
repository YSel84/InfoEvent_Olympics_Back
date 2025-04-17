package com.ieolympicstickets.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    private String location;
    private String date;
    private String description;

    // Getters & Setters done via Lombok

}
