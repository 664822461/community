package com.nowcoder.community.controller;


import com.nowcoder.community.common.Page;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    LikeService likeService;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;



    @RequestMapping("/add")
    @ResponseBody
    public String addDiscussPost(DiscussPost discussPost) throws IllegalAccessException {

        User user = hostHolder.getUser();
        if(user == null){
            throw  new IllegalAccessException("登录验证失败！");
        }
        discussPost.setCreateTime(new Date());
        discussPost.setUserId(user.getId());

        discussPostService.insertDiscussPot(discussPost);

        return CommunityUtil.getJSONString(0,"发布成功！");

    }


    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @RequestMapping("/detail/{id}")
    public String selectDiscussPost(
            @PathVariable("id") int discussPostId,
            Model model,
            Page page){

        //帖子
        DiscussPost discussPost = discussPostService.selectDiscussPost(discussPostId);
        model.addAttribute("post",discussPost);
        //作者
        User user = userService.selectById(discussPost.getUserId());
        model.addAttribute("user",user);


        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(1, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), 1, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        //分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPost.getId());
        page.setRows(discussPost.getCommentCount());

        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        //帖子的回复列表
        List<Comment> commentList = commentService.getComments(
                1, discussPost.getId(), page.getOffset(), page.getLimit());
        if (commentList != null) {
            //遍历单条评论
            for (Comment comment:commentList){
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.selectById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(2, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), 2, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复列表
                List<Comment> replyList = commentService.getComments(
                        2, comment.getId(), 0, Integer.MAX_VALUE);

                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.selectById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(2, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), 2, reply.getId());
                        replyVo.put("likeStatus", likeStatus);


                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.getCommentCount(2, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }


        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";

    }


    @GetMapping("/addComment/{postId}")
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public String addComment(
            @PathVariable("postId") int postId,
            Comment comment,
            String current
            ){

        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(hostHolder.getUser().getId());
        commentService.insertComment(comment);
        if(comment.getEntityType() == 1){


            int commentCount = commentService.getCommentCount(1, postId);
            discussPostService.updateCommentCount(comment.getEntityId(),commentCount);
        }

        return "redirect:/discuss/detail/" + postId+"?current="+current;
    }




}
