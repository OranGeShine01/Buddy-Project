package com.fivet.buddy.controller;

import com.fivet.buddy.dao.ChatMemberDAO;
import com.fivet.buddy.dao.ChatRoomDAO;
import com.fivet.buddy.dto.*;
import com.fivet.buddy.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/chatRoom/")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatMsgService chatMsgService;

    @Autowired
    private HttpSession session;

    @Autowired
    private TeamMemberService teamMemberService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ChatMemberService chatMemberService;

    @Autowired
    private MemberService memberService;

    //채팅방 입장
    @Transactional
    @RequestMapping("join")
    public String insertChatMsg(ChatMemberDTO chatMemberDto, ChatRoomDTO chatRoomDto, TeamMemberDTO teamMemberDto,int chatRoomSeq, Model model) throws Exception {
        //채팅방에 실 참여자인지 여부 체크
        chatMemberDto.setMemberSeq((int)session.getAttribute("memberSeq"));
        int selectChatRoom = chatRoomService.selectChatRoom(chatMemberDto);

        if(selectChatRoom==1) {
            teamMemberDto.setTeamSeq((int) session.getAttribute("teamSeq"));
            teamMemberDto.setMemberSeq((int) session.getAttribute("memberSeq"));
            //teamSeq와 memberSeq를 담아 서비스 및 sql문에 전달할 Map
            Map<String, Integer> param = new HashMap<>();
            param.put("teamSeq", teamMemberDto.getTeamSeq());
            param.put("memberSeq", teamMemberDto.getMemberSeq());
            List<ChatRoomDTO> topicList = chatRoomService.selectTopic(param.get("teamSeq"));
            // 채팅방 입장시, 해당 팀 해당 회원의 채팅방 목록 출력
            List<ChatRoomDTO> chatRoomList = chatRoomService.chatRoomList(param);
            // 회원 번호를 이용하여 팀 DTO값을 불러옴.
            teamMemberDto = teamMemberService.selectOne(teamMemberDto);
            model.addAttribute("teamMemberInfo", teamMemberDto);

            chatRoomDto.setTeamSeq((int) session.getAttribute("teamSeq"));
            String chatRoomName = chatRoomService.selectChatRoomName(chatRoomDto);
            model.addAttribute("chatRoomName",chatRoomName);
            model.addAttribute("topicList", topicList);
            model.addAttribute("chatRoomSeq",chatRoomSeq);
            model.addAttribute("chatMsgList", chatMsgService.selectChatMsg(chatRoomSeq));
            model.addAttribute("chatMemberList",teamMemberService.selectChatMember(chatRoomSeq));
            model.addAttribute("chatRoomList", chatRoomList);

        //프로필 이미지 출력
        String memberImgSysName = memberService.selectProfileImg(String.valueOf(session.getAttribute("memberSeq")));
        if(memberImgSysName.equals("/static/img/defaultProfileImg.png")){
            model.addAttribute("memberImgSysName",memberImgSysName);
        }else{
            memberImgSysName = "/member/selectProfileImg/"+memberImgSysName;
            memberImgSysName=memberImgSysName.replaceAll("\\s", "");
            model.addAttribute("memberImgSysName",memberImgSysName);
        }
        //토픽 수 출력
        int topicCount = chatRoomService.countTopic(param.get("teamSeq"));
        model.addAttribute("topicCount", topicCount);

            return "team/teamChating";
        }else{
            return "error";
        }
    }

    // 채팅방 목록 출력
    @ResponseBody
    @PostMapping("chatRoomList")
    public String chatRoomList() {
        Map<String, Integer> param = new HashMap<>();
        param.put("memberSeq", (int)session.getAttribute("memberSeq"));
        param.put("teamSeq", (int)session.getAttribute("teamSeq"));

        Gson g = new Gson();
        return g.toJson(chatRoomService.chatRoomList(param));
    }

    // 토픽 생성
    @ResponseBody
    @PostMapping("insertTopic")
    public void insertTopic(ChatRoomDTO chatRoomDto) {
        TeamDTO teamDto = teamService.selectTeamOne(session.getAttribute("teamSeq").toString());

        //ChatRoomDTO 영역 (chatRoom테이블에 topic 생성)

        chatRoomDto = chatRoomService.insertTopic(teamDto, chatRoomDto);

        //ChatMember 영역(각 회원을 토픽에 가입)
        chatMemberService.insertTopicMember(chatRoomDto);

    }

    // 일반채팅방 생성
    @Transactional
    @ResponseBody
    @PostMapping("insertChat")
    public String insertChat(@RequestBody MakeChatDTO makeChatDto) {

        TeamDTO teamDto = teamService.selectTeamOne(session.getAttribute("teamSeq").toString());
        ChatRoomDTO chatRoomDto = new ChatRoomDTO(
                0,
                makeChatDto.getChatTitle(),
                teamDto.getTeamSeq(),
                (int)session.getAttribute("memberSeq"),
                "normal",
                makeChatDto.getMakeChat().length,
                null
                );
        chatRoomService.insertNormalChat(chatRoomDto);
        chatMemberService.insertNormalChatMember(chatRoomDto, makeChatDto.getMakeChat());

        return "redirect:/team/goTeamAgain";
    }


    //채팅방 멤버 출력
    @ResponseBody
    @RequestMapping("selectChatMember")
    public String selectChatMember(ChatMemberListDTO chatMemberListDto){
        chatMemberListDto.setTeamSeq((int) session.getAttribute("teamSeq"));
        List chatMemberList =  chatMemberService.selectChatMember(chatMemberListDto);
        Gson g = new Gson();
        return g.toJson(chatMemberList);
    }

    // 채팅방 삭제
    @Transactional
    @ResponseBody
    @PostMapping("delChatRoom")
    public String delChatRoom(int chatRoomSeq) {
        chatRoomService.delChatRoom(chatRoomSeq);
        chatMemberService.delChatRoom(chatRoomSeq);
        chatMsgService.delChatRoom(chatRoomSeq);
        return String.valueOf(chatRoomSeq);
    }

    // 채팅방 제목 변경
    @ResponseBody
    @PostMapping("updateChatTitle")
    public String updateChatTitle(ChatRoomDTO chatRoomDto) {
        chatRoomService.updateChatTitle(chatRoomDto);
        return String.valueOf(chatRoomDto.getChatRoomSeq());
    }

    // 일반채팅방 나가기
    @Transactional
    @ResponseBody
    @PostMapping("chatRoomOut")
    public void chatRoomOut(ChatMemberDTO chatMemberDto) {
        chatMemberDto.setMemberSeq((int)session.getAttribute("memberSeq"));
        chatMemberService.delChatMember(chatMemberDto);
        chatRoomService.delChatMember(chatMemberDto.getChatRoomSeq());
        chatRoomService.delChatRoomCountZero();

    }

}
