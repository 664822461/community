package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.model.IModel;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {


    @Autowired
    UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;



    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @PostMapping("/register")
    public String register(Model model, User user) throws IllegalAccessException {

        if (user == null) {
            throw new IllegalAccessException("参数错误！");
        }
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {

            model.addAttribute("msg", "注册成功,我们向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }

        model.addAttribute("usernameMsg", map.get("usernameMsg"));
        model.addAttribute("passwordMsg", map.get("passwordMsg"));
        model.addAttribute("emailMsg", map.get("emailMsg"));
        return "/site/register";
    }

    @GetMapping("/activation/{id}/{code}")
    public String checkActivation(
            @PathVariable("id") int id,
            @PathVariable("code") String code,
            Model model) {
        int i = userService.checkActivation(id, code);

        if (i == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "您的账号已经成功激活！");
            model.addAttribute("target", "/site/login");

        }else if(i == ACTIVATION_REPEAT){
            model.addAttribute("msg", "重复激活操作！");
            model.addAttribute("target", "/site/login");
        }else{
            model.addAttribute("msg", "激活失败，请稍后再试！");
            model.addAttribute("target", "/site/login");
        }

        return "/site/operate-result";
    }

    @RequestMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        session.setAttribute("kaptcha", text);

        // 将突图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }


    }


    @PostMapping("/login")
    public String login(String username, String password, String verifycode, boolean rememberme,
                        HttpSession session, Model model,
                        HttpServletResponse response){

        //检查验证码
        String kaptcha = (String)session.getAttribute("kaptcha");

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(verifycode) || !kaptcha.equalsIgnoreCase(verifycode)){
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号,密码

        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/site/login";
    }



}