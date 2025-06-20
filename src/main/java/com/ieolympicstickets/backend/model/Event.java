package com.ieolympicstickets.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Column(name="event_datetime", nullable = false)
    @NotNull
    private LocalDateTime eventDateTime;

    @NotBlank
    @Size(max = 200)
    private String location;

    @Size(max = 1000)
    private String description;

    @Column(name= "image_url", columnDefinition = "TEXT")
    @Size(max = 2048)
    private String imageUrl;

    private boolean featured;

    // Getters & Setters done via Lombok

}
