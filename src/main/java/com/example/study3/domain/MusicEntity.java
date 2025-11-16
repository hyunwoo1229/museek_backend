package com.example.study3.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "music_entity")
public class MusicEntity {
    @Id
    @Column(name = "music_id", unique = true, nullable = false)
    private String id;

    private String title;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "taskid")
    private String taskId;




    public MusicEntity(String id, String title, String audioUrl, String imageUrl) {

        this.id = id;
        this.title = title;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
