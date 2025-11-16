package com.example.study3.service;
import com.example.study3.domain.MusicEntity;
import com.example.study3.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor

public class SunoResultService {

    private final MusicRepository musicRepository;
    private final GcsFileService gcsFileService;

    public void handleSunoCallback(String taskId, Map<String, Object> callbackData) { // taskId: 음악 생성 요청 시 함께 전달했던 고유 식별자, callbackData: 콜백으로 전달된 전체 JSON 데이터

        /* suno에서 콜백으로 보내는 json 응답(callbackData) 형식:
        {
            "status": "success",
             "data": {
                 "data": [
                     {
                         "id": "abc123",
                         "title": "봄비",
                         "audio_url": "https://suno.com/audio1.mp3",
                         "image_url": "https://suno.com/image1.jpg"
                        },
                        ...
        ]
     }
    }
         */
        Map<String, Object> data = (Map<String, Object>) callbackData.get("data");
        List<Map<String, Object>> trackList = (List<Map<String, Object>>) data.get("data");


        for (Map<String, Object> track : trackList) {
            String audioUrl = (String) track.get("audio_url");
            String id = (String) track.get("id");

            if(audioUrl == null || audioUrl.isBlank() || musicRepository.existsByAudioUrl(audioUrl)) {
                continue;
            }

            String gcsAudioUrl;
            String gcsPath = "music/" + id + ".mp3";
            try {
                gcsAudioUrl = gcsFileService.uploadFromUrl(audioUrl, gcsPath);
            } catch (Exception e) {
                System.err.println("GCS 업로드 실패 (ID: " + id + "): " + e.getMessage());
                continue;
            }

            // MusicEntity 객체 생성 (DB에 저장할 모델)
            MusicEntity music = new MusicEntity(
                    id,
                    (String) track.get("title"),
                    gcsAudioUrl,
                    (String) track.get("image_url")
            );
            music.setTaskId(taskId);
            musicRepository.save(music);
        }


    }

    public List<MusicEntity> findByTaskId(String taksId) {
        return musicRepository.findByTaskIdAndAudioUrlIsNotNull(taksId);
    }
}
