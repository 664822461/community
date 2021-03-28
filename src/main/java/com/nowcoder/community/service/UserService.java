package com.nowcoder.community.service;



import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {


    @Autowired
    UserMapper userMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;



    public User selectById(int userId){
       return  userMapper.selectById(userId);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }

        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        if(userMapper.selectByName(user.getUsername()) != null){
            map.put("usernameMsg","账户已存在!");
            return map;
        }

        if(userMapper.selectByEmail(user.getEmail()) != null){
            map.put("emailMsg","邮箱已存在!");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setCreateTime(new Date());
        user.setType(0);
        user.setStatus(0);
        user.setPassword(CommunityUtil.md5(user.getPassword(),user.getSalt()));
        user.setHeaderUrl("http://images.nowcoder.com/head/"+ new Random().nextInt(1000) +"t.png");

        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;

    }


    public int checkActivation(int id,String code){

        User user = userMapper.selectById(id);
        if(user == null){
            return ACTIVATION_FAILURE;
        }

        if(user.getActivationCode().equals(code)){

            userMapper.updateStatus(id,1);
            return ACTIVATION_SUCCESS;
        }

        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }

        return ACTIVATION_FAILURE;
    }


    public Map<String,Object> login(String username,String password,int expiredSeconds){

        Map<String,Object> map = new HashMap<>();

        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        User user = userMapper.selectByName(username);


        if(user == null){
            map.put("usernameMsg","用户不存在！");
            return map;
        }

        if(user.getStatus() == 0){
            map.put("usernameMsg","对不起，账号尚未激活！");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password,user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码错误！");
            return map;
        }

        //验证没问题，生成凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;


    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket selectByTicket(String ticket){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        return loginTicket;
    }


    public void updateHeader(int id, String headerUrl) {
        userMapper.updateHeader(id,headerUrl);

    }
}
