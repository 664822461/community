package com.nowcoder.community.controller;


import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.model.IModel;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    LikeService likeService;
    @LoginRequired
    @GetMapping("/site/setting")
    public String getSettingPage(){

        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";

        }
        //获取文件原始名字
        String originalFilename = headerImage.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        if(StringUtils.isBlank(substring)){
            model.addAttribute("error","文件格式不正确!");
            return "/site/setting";
        }

        //生成随机文件名
        originalFilename = CommunityUtil.generateUUID() + substring;
        //确认文件的存放位置
        File file = new File(uploadPath + "/" + hostHolder.getUser().getId()+"/"+originalFilename);
        File file2 = new File(uploadPath + "/" + hostHolder.getUser().getId());
        //如果文件夹不存在则创建
        if (!file2.isDirectory())
        {
            file2 .mkdir();
        }



        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        //更新当前用户头像路径
        User user = hostHolder.getUser();
        //localhost:8080/community/user/header/101/xxxxxxx.xx
        String headerUrl = domain + contextPath + "/user/header/" + hostHolder.getUser().getId()+"/"+originalFilename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";

    }


    @RequestMapping(path = "/header/{userId}/{fileName}", method = RequestMethod.GET)
    public void getHeader(
            @PathVariable("userId") int userId,
            @PathVariable("fileName") String fileName,
            HttpServletResponse response
    ){

        //真实存放路径
        fileName = uploadPath+"/"+userId+"/"+fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }

    }

    @GetMapping("/site/profile/{id}")
    public String getUserTotalLike(@PathVariable int id,
                                   Model model){
        User user = userService.selectById(id);
        if (user == null){
            throw new RuntimeException("用户不存在！");
        }
        model.addAttribute("user",user);

        int entittyUserTotal = likeService.findEntittyUserTotal(id);
        model.addAttribute("entittyUserTotal",entittyUserTotal);



        // 关注数量
        long followeeCount = followService.findFolloweeCount(id, 3);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(3, id);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), 3, id);
        }
        model.addAttribute("hasFollowed", hasFollowed);


        return "/site/profile";
    }





}
