package com.fivet.buddy.dao;

import com.fivet.buddy.dto.NoticeBoardDTO;
import com.fivet.buddy.mapper.NoticeBoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class NoticeBoardDAO {

    @Autowired
    private NoticeBoardMapper noticeBoardMapper;

    // 공지 목록 출력
    public List<NoticeBoardDTO> selectNotice() throws Exception {
        return noticeBoardMapper.selectNotice();
    }

    // 공지글 쓰기
    public void insertNotice(NoticeBoardDTO noticeBoardDto) {
        noticeBoardMapper.insertNotice(noticeBoardDto);
    }

    // 공지글 삭제
    public void deleteNotice(int noticeSeq) { noticeBoardMapper.deleteNotice(noticeSeq); }

    // 공지글 보기
    public NoticeBoardDTO noticeDetail(int noticeSeq) {
        return noticeBoardMapper.noticeDetail(noticeSeq);
    }

    // 공지 수정
    public void updateNotice(NoticeBoardDTO noticeBoardDto) { noticeBoardMapper.updateNotice(noticeBoardDto); }

    // 공지글 페이지에 맞춰 출력
    public List<NoticeBoardDTO> selectNoticePage(Map<String, Integer> param) {
        return noticeBoardMapper.selectNoticePage(param);
    };

    // 공지글 전체수 출력
    public int totalCount() {
        return noticeBoardMapper.totalCount();
    }
}
