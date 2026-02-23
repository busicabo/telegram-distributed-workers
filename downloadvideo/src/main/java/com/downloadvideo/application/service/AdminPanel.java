package com.downloadvideo.application.service;

import com.downloadvideo.adapters.out.persistence.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

/*
Сервис для выполнения всех админ команд. Используется в AdminCommandHandler.
Каждый метод выполняет ровно то, что у него и написано.
 */
@Service
@Slf4j
public class AdminPanel {
    @Autowired
    private UserService userService;
    @Autowired
    private VideoDataService videoDataService;
    @Autowired
    private SendTelegram sendTelegram;
    //по умолчанию включена отправка ошибок.
    @Setter
    @Getter
    private boolean sendError = true;

    public List<Long> getAllAdmins(){
        return userService.getIdAllAdmins();
    }

    public void blockUser(Long id){
        userService.blockingUserById(id,true);
    }

    public void blockUser(String username){
        userService.blockingUserByUsername(username,true);
    }

    public void unblockUser(Long id){
        userService.blockingUserById(id,false);
    }

    public void unblockUser(String username){
        userService.blockingUserByUsername(username,false);
    }

    public List<UserEntity> getAllUsers(){
        return userService.getAllUsers();
    }

    public UserEntity getUser(Long id){
        return userService.getUser(id);
    }
    public UserEntity getUser(String username){
        return userService.getUser(username);
    }
    public Long getUserCount(){
        return userService.getUserCount();
    }
    //рассылка всем пользователям
    public void sendMessageUsers(String message){
        List<String> chats = videoDataService.getUserChats();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        for(String chat: chats){
            sendMessage.setChatId(chat);
            sendTelegram.sendMessage(sendMessage);
        }
    }

    public void sendAllMessageAndPhoto(String fileid,String message){
        int i = 0;
        List<String> list = videoDataService.getUserChats();
        for(String chat: list){
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chat);
            sendPhoto.setPhoto(new InputFile(fileid));
            sendPhoto.setCaption(message);
            Message m = sendTelegram.sendPhoto(sendPhoto);
            if(m==null){
                i+=1;
            }
        }
        sendTelegram.sendMessage(new SendMessage("1420197085",i+" неуспешных отправок"));

    }



}
