package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {


    @Select({
            "select id,user_id,entity_type,entity_id,target_id,content,status,create_time",
            "from comment where entity_type=#{entityType} and entity_id=#{entityId} limit #{offset},#{limit}"
    })
    List<Comment> getComments(int entityType,int entityId,int offset,int limit);

    @Select({
            "select count(id) from comment where entity_type=#{entityType} and entity_id=#{entityId}",
    })
    int getCommentCount(int entityType,int entityId);


    @Insert({
            "insert into comment(user_id,entity_type,entity_id,target_id,content,status,create_time) ",
            "values(#{userId},#{entityType},#{entityId},#{targetId},#{content},#{status},#{createTime})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(Comment comment);

}
