package com.ieolympicstickets.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Column(name="event_datetime", nullable = false)
    private LocalDateTime eventDateTime;
    private String location;
    private String description;

        @Column(name= "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    private boolean featured;

    // Getters & Setters done via Lombok

}
