package com.downloadvideo.controller;

import com.downloadvideo.config.bot.MainBot;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@Slf4j
public class Controller {
    @Autowired
    private MainBot bot;

    @PostMapping("/telegram")
    @ResponseStatus(HttpStatus.OK)
    public void listener(@RequestBody Update update) {
        bot.onWebhookUpdateReceived(update);
    }


}
