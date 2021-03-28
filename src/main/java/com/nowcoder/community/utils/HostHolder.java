package com.nowcoder.community.utils;


import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {


    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void  setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
