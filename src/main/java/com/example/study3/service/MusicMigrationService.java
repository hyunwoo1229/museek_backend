package com.example.study3.service;

import com.example.study3.domain.MusicEntity;
import com.example.study3.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicMigrationService {

    private final MusicRepository musicRepository;
    private final GcsFileService gcsFileService;

    // GCS로 마이그레이션 실행
    @Transactional
    public int migrateExistingMusicToGcs() {

        List<MusicEntity> existingMusicList = musicRepository.findAll(); // 모든 항목을 가져온다고 가정

        int successCount = 0;

        for (MusicEntity music : existingMusicList) {
            String currentUrl = music.getAudioUrl();

            if (currentUrl == null || currentUrl.contains("storage.googleapis.com")) {
                continue;
            }

            String gcsPath = "music/" + music.getId() + ".mp3";
            String gcsAudioUrl = null;

            try {
                // 1. Suno URL에서 파일을 다운로드하여 GCS에 업로드
                gcsAudioUrl = gcsFileService.uploadFromUrl(currentUrl, gcsPath);

                // 2. DB 업데이트
                music.setAudioUrl(gcsAudioUrl);
                musicRepository.save(music);
                successCount++;

            } catch (Exception e) {
                System.err.println("마이그레이션 실패 (ID: " + music.getId() + "): " + e.getMessage());
            }
        }
        return successCount;
    }
}