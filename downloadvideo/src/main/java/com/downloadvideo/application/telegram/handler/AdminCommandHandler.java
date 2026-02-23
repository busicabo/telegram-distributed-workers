package com.downloadvideo.application.telegram.handler;

import com.downloadvideo.adapters.in.telegram.dto.MessageEvent;
import com.downloadvideo.adapters.out.persistence.entity.UserEntity;
import com.downloadvideo.application.service.AdminPanel;
import com.downloadvideo.application.service.SendTelegram;
import com.downloadvideo.application.telegram.handler.main.MessageHandlers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

/*
Обработка Админ команд. Роль пользователя должна быть админ.
Проверяется доступ к команде через aop. com.downloadvideo.infrastructure.aop.CheckAdminGroup
Основной класс по выполнения админ команд AdminPanel
 */

/*
Команды:
(блокировка пользователя) /block (id(по айди)/username(по юсернейм)) (true(поставить)/false(убрать))
(Рассылка ошибок всем админам) /error (stop/start)
(получить количество пользователей) /getCountUsers
(получить информацию и пользователе) /getUser (id/username) (его айди/его юсернейм)
(отправить всем пользователям) /sendMessage (сообщение)
 */

@Service
@Slf4j
public class AdminCommandHandler implements MessageHandlers {
    @Autowired
    private AdminPanel adminPanel;
    @Autowired
    private SendTelegram sendTelegram;
    //Список всех доступных команд
    private final List<String> commands = List.of("/block","/error","/getUser","/getCountUsers","/sendMessage","/post");
    @Override
    public boolean isValid(String message) {
        for(String command: commands){
            if(message.startsWith(command)){
                return true;
            }
        }
        return false;
    }

    //Проверяет к какому типу относиться команда.
    @Override
    public void handle(MessageEvent event) throws NumberFormatException{
        Update update = event.getUpdate();
        String command = update.getMessage().getText();
        if(command==null){
            command=update.getMessage().getCaption();
        }
         if(command.equalsIgnoreCase("/block")){
            String[] str = command.split(" ");
            if(str.length!=4){
                return;
            } else{
                if(str[1].equalsIgnoreCase("id")){
                    if(Boolean.parseBoolean(str[3])){
                        adminPanel.blockUser(update.getMessage().getFrom().getId());
                    } else{
                        adminPanel.unblockUser(update.getMessage().getFrom().getId());
                    }
                } else if(str[1].equalsIgnoreCase("username")){
                    if(Boolean.parseBoolean(str[3])){
                        adminPanel.blockUser(update.getMessage().getFrom().getUserName());
                    } else{
                        adminPanel.unblockUser(update.getMessage().getFrom().getUserName());
                    }
                }
            }
        } else if(command.startsWith("/error")){
             String[] str = command.split(" ");
             if(str.length==2){
                 if(str[1].equalsIgnoreCase("start")){
                     adminPanel.setSendError(true);
                 } else if(str[1].equalsIgnoreCase("stop")){
                     adminPanel.setSendError(false);
                 }
             }
         } else if(command.startsWith("/getCountUsers")){
             Long count = adminPanel.getUserCount();
             if(count==null){
                 return;
             }
             sendTelegram.sendMessage(new SendMessage(String.valueOf(update.getMessage().getChatId()),String.valueOf(count)));

         }
         else if(command.startsWith("/getUser")){
             String[] str = command.split(" ");
             if(str.length!=3){
                 return;
             }
             UserEntity userEntity = null;
             if(str[1].equalsIgnoreCase("id")){
                 userEntity=adminPanel.getUser(Long.parseLong(str[2]));
             } else if(str[1].equalsIgnoreCase("username")){
                 userEntity=adminPanel.getUser(str[2]);
             }
             if(userEntity==null) return;
             sendTelegram.sendMessage(new SendMessage(String.valueOf(update.getMessage().getChatId()),userEntity.toString()));

         }
         else if(command.startsWith("/sendMessage")){
             String[] str = command.split("-");
             if(str.length!=2){
                 return;
             }
             adminPanel.sendMessageUsers(str[1]);
         } else if(command.startsWith("/post")){
             String fileid = event.getUpdate().getMessage().getPhoto().get(event.getUpdate().getMessage().getPhoto().size() - 1).getFileId();
             String caption = event.getUpdate().getMessage().getCaption();
             if(caption==null || caption.isEmpty()){
                 return;
             }
             if(caption.startsWith("/post")){
                 String[] str = caption.split("-");
                 if(str.length!=2){
                     return;
                 }
                 adminPanel.sendAllMessageAndPhoto(fileid,str[1]);
             }
         }

    }
}
