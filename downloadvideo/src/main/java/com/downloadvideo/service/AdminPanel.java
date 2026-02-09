package com.downloadvideo.service;

import com.downloadvideo.model.postgresql.UserEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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

}
