package com.fivet.buddy.mapper;

import com.fivet.buddy.dto.ChatRoomDTO;
import com.fivet.buddy.dto.TeamDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatRoomMapper {

    void createTeam(Map<String, String> param);

    List<ChatRoomDTO> chatRoomList(Map<String, Integer> param);

}