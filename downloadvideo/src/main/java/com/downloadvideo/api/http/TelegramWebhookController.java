package com.downloadvideo.api.http;

import com.downloadvideo.infrastructure.telegram.bot.MainBot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final MainBot bot;

    @PostMapping("/telegram")
    @ResponseStatus(HttpStatus.OK)
    public void listener(@RequestBody Update update) {
        bot.onWebhookUpdateReceived(update);
    }
}
