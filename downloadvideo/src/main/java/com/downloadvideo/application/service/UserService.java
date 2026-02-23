package com.downloadvideo.application.service;

import com.downloadvideo.adapters.out.persistence.entity.UserEntity;
import com.downloadvideo.adapters.out.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserEntity getUser(Long id){
        Optional<UserEntity> user = userRepository.findById(id);
        return user.orElse(null);
    }
    public UserEntity getUser(String username){
        return userRepository.getUser(username);
    }

    @Transactional
    public boolean authenticateUser(Long id,String username){
        UserEntity userEntity = getUser(id);
        if(userEntity==null){
            log.info("Создание нового пользователя! id:{}, username:{}",id,username);
            if(username==null || username.isEmpty()){
                userEntity=create(id);
            } else {
                userEntity=create(id,username);
            }
        }
        if(userEntity==null){
            log.info("Пользователь не был создан по неизвестной причине!");
            return false;
        }
        return !userEntity.isBlocked();
    }
    public void addCountDownloads(Long userId){
        userRepository.addCountDownloads(userId);
    }

    @Transactional
    public UserEntity create(Long id){
        return userRepository.save(new UserEntity(id));
    }
    @Transactional
    public UserEntity create(Long id,String username){
        return userRepository.save(new UserEntity(id,username));
    }

    public List<Long> getIdAllAdmins(){
        return userRepository.getIdAllAdmins();
    }

    public void blockingUserById(Long id, boolean blocking){
        userRepository.blockUserById(id,blocking);
    }

    public void blockingUserByUsername(String username, boolean blocking){
        userRepository.blockUserByUsername(username,blocking);
    }

    public List<UserEntity> getAllUsers(){
        return userRepository.findAll();
    }

    public Long getUserCount(){
        return userRepository.getCountUser();
    }
    public boolean isAdmin(Long id){
        Integer isAdmin = userRepository.isAdmin(id);
        if(isAdmin==null) return false;
        return isAdmin==1;
    }

}
