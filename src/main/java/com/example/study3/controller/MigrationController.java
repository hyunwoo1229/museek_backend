package com.example.study3.controller;

// 임시 MigrationController.java (마이그레이션 완료 후 반드시 제거해야 합니다!)

import com.example.study3.service.MusicMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class MigrationController {

    private final MusicMigrationService migrationService;

    @PostMapping("/migrate-music")
    public String startMigration() {
        int count = migrationService.migrateExistingMusicToGcs();
        return "마이그레이션이 완료되었습니다. 총 " + count + "개의 파일이 GCS로 이동되었습니다.";
    }
}

