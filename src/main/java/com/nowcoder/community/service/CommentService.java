package com.nowcoder.community.service;


import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.SensitiveFilter;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    public List<Comment> getComments(int entityType, int entityId, int offset, int limit){
       return  commentMapper.getComments(entityType,entityId,offset,limit);
    }


   public int getCommentCount(int entityType,int entityId){
      return  commentMapper.getCommentCount(entityType,entityId);
    }

    public int insertComment(Comment comment){
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        return commentMapper.insertComment(comment);
    }
}
