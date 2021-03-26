package com.nowcoder.community.controller;

import com.nowcoder.community.common.Page;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    UserService userService;

    @GetMapping("/")
    public String getIndex(Model model, Page page){

        return "/index";
    }

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        page.setRows(discussPostService.selectDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> discussPosts = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> posts = new ArrayList<Map<String,Object>>();

        if(discussPosts != null){
            for(DiscussPost post : discussPosts){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.selectById(post.getUserId());
                map.put("user",user);
                posts.add(map);
            }

           model.addAttribute("posts", posts);
            return "/index";

        }


        return "/index";
    }
}
