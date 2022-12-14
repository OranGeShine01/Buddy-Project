package com.fivet.buddy.mapper;

import com.fivet.buddy.dto.ChatMemberDTO;
import com.fivet.buddy.dto.ChatRoomDTO;
import com.fivet.buddy.dto.TeamDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatRoomMapper {

    void createTeam(Map<String, String> param);

    List<ChatRoomDTO> chatRoomList(Map<String, Integer> param);

    void insertSelfChat(Map<String, String> param);

    String selectChatRoomSeq(Map<String, String> param);

    void updatePlusMemberCount(Map<String, String> param);
    void insertTopic(ChatRoomDTO chatRoomDto);
    List<ChatRoomDTO> selectTopic(int teamSeq);
    int countTopic (int teamSeq);
    void insertNormalChat(ChatRoomDTO chatRoomDto);
    void delChatRoom(int chatRoomSeq);
    void updateChatTitle(ChatRoomDTO chatRoomDto);
    void delChatMember(int chatRoomSeq);
    void delChatRoomCountZero();
    void delOnlyOneChatRoom(int memberSeq);
    int selectChatRoom(ChatMemberDTO chatMemberDto);
    //채팅방 이름 출력
    String selectChatRoomName(ChatRoomDTO chatRoomDto);
    void delTeam(int teamSeq);

}
