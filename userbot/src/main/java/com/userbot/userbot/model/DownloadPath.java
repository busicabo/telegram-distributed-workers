package com.userbot.userbot.model;

import lombok.Getter;

import java.nio.file.Paths;

//Куда будут скачиваться файлы
public class DownloadPath {
    public static final String PATH = String.valueOf(Paths.get(System.getProperty("user.dir"),"download"));
}
