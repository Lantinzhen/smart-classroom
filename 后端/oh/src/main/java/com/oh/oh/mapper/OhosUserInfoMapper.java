package com.oh.oh.mapper;

import com.oh.oh.entity.OhosUserInfo;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OhosUserInfoMapper {

    @Insert("INSERT INTO ohos_user_info (name, password) VALUES (#{name}, #{password})")
    int insertUserInfo(OhosUserInfo userInfo);

    @Select("SELECT * FROM ohos_user_info WHERE name = #{name}")
    OhosUserInfo findByCount(String name);

    @Select("SELECT * FROM ohos_user_info WHERE name = #{name} AND password = #{password}")
    OhosUserInfo findByCountAndPassword(@Param("name") String name, @Param("password") String password);

    @Select("SELECT * FROM ohos_user_info")
    List<OhosUserInfo> findAll();

    @Update("UPDATE ohos_user_info SET password = #{password} WHERE name = #{name}")
    int updatePassword(@Param("name") String name, @Param("password") String password);
}