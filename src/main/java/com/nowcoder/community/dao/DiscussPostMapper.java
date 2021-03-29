package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    int selectDiscussPostRows(@Param("userId") int userId);


    int insertDiscussPot(DiscussPost discussPost);

    DiscussPost selectDiscussPost(int discussPostId);

    @Update({
            "update discuss_post set comment_count = #{commentCount}",
            "where id = #{postId}"
    })
    int updateCommentCount(int postId,int commentCount);

}
