package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.utils.SensitiveFilter;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Autowired
    DiscussPostMapper discussPostMapper;

    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(userId, offset, limit);
        return discussPosts;
    }

    public int selectDiscussPostRows(@Param("userId") int userId){
        int i = discussPostMapper.selectDiscussPostRows(userId);
        return i;
    }

    public void insertDiscussPot(DiscussPost discussPost){

        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        discussPostMapper.insertDiscussPot(discussPost);


    }

    public DiscussPost selectDiscussPost(int discussPostId){
        DiscussPost discussPost = discussPostMapper.selectDiscussPost(discussPostId);
        return discussPost;
    }

    public void updateCommentCount(int postId,int commentCount){
        discussPostMapper.updateCommentCount(postId, commentCount);

    }
}
