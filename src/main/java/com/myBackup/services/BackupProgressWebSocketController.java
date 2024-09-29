package com.myBackup.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BackupProgressWebSocketController {
    private static final Logger logger = LogManager.getLogger(BackupProgressWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;

    public BackupProgressWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleBackupProgressEvent(BackupTaskEvent event) {
        // Broadcast progress to clients via WebSocket
        logger.debug("Received event from source: {}", event.getSource());
        messagingTemplate.convertAndSend("/topic/backup-progress", event);
    }
}
