package com.duckstar.schedule;

import com.duckstar.service.EpisodeCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EpisodeStartupInitializer {

    private final EpisodeCommandService episodeCommandService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        episodeCommandService.updateAllEpisodeStates();
    }
}