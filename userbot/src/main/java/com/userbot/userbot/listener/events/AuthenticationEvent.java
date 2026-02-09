package com.userbot.userbot.listener.events;

import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
//Нужно пройти аутинтефикацию.
public class AuthenticationEvent {
    private SimpleTelegramClient client;
    @Autowired
    public AuthenticationEvent(SimpleTelegramClient simpleTelegramClient){
        this.client=simpleTelegramClient;
    }
    private Scanner scan = new Scanner(System.in);

    @EventListener
    public void handlerPhoneNumber(TdApi.AuthorizationStateWaitPhoneNumber update){
        System.out.println("Введите номер телефона: ");
        String number = scan.nextLine();
        client.send(new TdApi.SetAuthenticationPhoneNumber(number,null),result ->
        {
            if(result.isError()){
                System.out.println("Ошибка при вводе номера "+result.getError().code+": "+result.getError().message);
            }
        });
    }
    @EventListener
    public void handlerCode(TdApi.AuthorizationStateWaitCode update){
        System.out.println("Введите код: ");
        String code = scan.nextLine();
        System.out.println("Ввели: "+code);
        client.send(new TdApi.CheckAuthenticationCode(code),result ->
        {
            if(result.isError()){
                System.out.println("Ошибка при вводе кода "+result.getError().code+": "+result.getError().message);
                System.out.println("Введите еще раз!");
                String c = scan.nextLine();
                client.send(new TdApi.CheckAuthenticationCode(c),r->{
                    System.out.println(r.getError().message);
                });
            }
        });
    }
    @EventListener
    public void handlerPassword(TdApi.AuthorizationStateWaitPassword update){
        System.out.println("Введите пароль: ");
        String password = scan.nextLine();
        client.send(new TdApi.CheckAuthenticationPassword(password),result ->
        {
            if(result.isError()){
                System.out.println("Ошибка при вводе пароля "+result.getError().code+": "+result.getError().message);
            }
        });
    }
    @EventListener
    public void handlerNotRegistration(TdApi.AuthorizationStateWaitRegistration update){
        System.out.println("Аккаунт не зарегистрирован!");
    }
    @EventListener
    public void handlerOk(TdApi.AuthorizationStateReady update) {
        System.out.println("Успешный вход!");
    }
}
