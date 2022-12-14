package com.fivet.buddy.services;

import com.fivet.buddy.dao.BasicFolderDAO;
import com.fivet.buddy.dao.PersonalFileDAO;
import com.fivet.buddy.dao.PersonalFolderDAO;
import com.fivet.buddy.dto.MemberDTO;
import com.fivet.buddy.dto.PersonalFolderDTO;
import com.fivet.buddy.dto.TeamDTO;
import com.fivet.buddy.util.FileUtil;
import com.fivet.buddy.util.RandomKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;


@Service
public class PersonalFolderService {

    @Autowired
    private RandomKeyUtil randomKeyUtil;

    @Autowired
    private PersonalFolderDAO personalFolderDao;

    @Autowired
    private PersonalFileDAO personalFileDao;

    @Autowired
    private BasicFolderDAO basicFolderDao;

    @Value("${drive.save.path}")
    private String uploadFilePath;

    // (개인)회원 별 기본폴더의 하위폴더 및 파일 불러오기
    public List<PersonalFolderDTO> selectMyFolders(int memberSeq) throws Exception{
        String basicKey = basicFolderDao.myBasicFolder(memberSeq);

        Map<String,Object> map = new HashMap<>();
        map.put("memberSeq",memberSeq);
        map.put("basicKey",basicKey);

        return personalFolderDao.selectMyFolders(map);
    }

    // 폴더 생성
    public void insertNewFolder(String folderName, String parentKey, String uploadFilePath,int memberSeq,String isTeam) throws Exception{
        PersonalFolderDTO personalFolderDto = new PersonalFolderDTO();

        // 폴더 이름, 멤버seq값, 부모폴더 key값, 폴더 저장경로 세팅
        personalFolderDto.setPersonalFolderName(folderName);
        personalFolderDto.setPersonalFolderMemberSeq(memberSeq);
        personalFolderDto.setPersonalFolderParentKey(parentKey);
        personalFolderDto.setPersonalFolderPath(uploadFilePath+folderName+"/");
        personalFolderDto.setPersonalFolderKey(randomKeyUtil.folderKey());
        if(isTeam == null){
            personalFolderDto.setPersonalFolderType("user");
        }else{
            personalFolderDto.setPersonalFolderType(isTeam);
        }
        personalFolderDao.insertNewFolder(personalFolderDto);
    }

    // 폴더 존재 유무 체크
    public boolean isFolderExists(String fileName,int memberSeq) throws Exception{
        // view에서 넘어온 폴더 이름, 소유한 멤버 번호 Map에 setting
        Map<String, Object> folderCheck = new HashMap<>();
        folderCheck.put("personalFolderName",fileName);
        folderCheck.put("personalFolderMemberSeq",memberSeq);
        return personalFolderDao.isFolderExists(folderCheck);
    }

    // 회원가입 시 personal folder 테이블에 기본폴더 생성
    public void newPersonalFolder(MemberDTO memberDto) throws Exception{
        // 회원가입 시 회원전용 폴더 생성
        // 최초 업로드 경로(서버 열리면 바꾸기)
        String newKey = randomKeyUtil.folderKey();
        File file = new File(uploadFilePath + newKey + memberDto.getMemberName());
        file.mkdir();

        // 넘겨줘야 하는 값 : 유저이름, 유저번호,기본 폴더 번호, path
        Map<String, Object> map = new HashMap<>();
        map.put("newKey",newKey);
        map.put("memberName",memberDto.getMemberName());
        map.put("memberSeq",memberDto.getMemberSeq());
        map.put("basicFolderKey",basicFolderDao.myBasicFolder(memberDto.getMemberSeq()));
        map.put("path",uploadFilePath + newKey + memberDto.getMemberName()+"/");

        personalFolderDao.newPersonalFolder(map);
    }

    // 팀 생성 시 personal folder 테이블에 insert
    public void newTeamSubFolder(TeamDTO teamDto) throws Exception{
        String newKey = randomKeyUtil.folderKey();
        File file = new File(uploadFilePath + newKey + teamDto.getTeamName());
        file.mkdir();

        Map<String, Object> map = new HashMap<>();
        map.put("newKey",newKey);
        map.put("teamName",teamDto.getTeamName());
        map.put("memberSeq",teamDto.getTeamOwnerSeq());
        map.put("basicFolderKey",basicFolderDao.myTeamFolderKey(teamDto.getTeamSeq()));
        map.put("path",uploadFilePath + newKey + teamDto.getTeamName()+"/");
        map.put("teamSeq",teamDto.getTeamSeq());

        personalFolderDao.newTeamSubFolder(map);
    }

    // 부모 폴더 경로 찾아오기(폴더 생성,파일 첨부)
    public String searchPath(String parentKey) throws Exception{
        return personalFolderDao.searchPath(parentKey);
    }

    // personal_folder 테이블에서 내 기본 폴더번호 추출
    public String myBasicFolder(int memberSeq) throws Exception{
        return personalFolderDao.myBasicFolder(memberSeq);
    }

    // 하위 폴더들 조회
    public List<PersonalFolderDTO> selectChildFolders(String resourceKey) {
        return personalFolderDao.selectChildFolders(resourceKey);
    }

    // 현재 폴더 이름
    public String nowFolder(String resourceKey) {
        return personalFolderDao.nowFolder(resourceKey);
    }

    // 폴더 삭제
    public void deleteFolder(List<Map<String, String>> folders) throws Exception{
        FileUtil fileUtil = new FileUtil();

        for(Map<String, String> map : folders){
            String key = map.get("key");
            String path = personalFolderDao.myPath(key);
            // basic에서 용량 제거
            long thisFolderByte = personalFolderDao.getMyByte(key);
            Map<String, Object> sendMap = new HashMap<>();
            sendMap.put("byte",thisFolderByte);
            sendMap.put("memberSeq",map.get("memberSeq"));
            basicFolderDao.deleteFolderByte(sendMap);

            // 실제 경로에서 폴더 삭제
            fileUtil.deleteFolder(path);
            // 하위 파일 DB에서 제거
            personalFileDao.deleteByPath(path);
            // 하위 폴더 DB에서 제거(path로)
            personalFolderDao.deleteFolderByPath(path);
        }

        //personalFolderDao.deleteFolder(folders);
    }

    // 폴더 밑에 하위폴더가 있는지 확인
    public boolean subIsExist(String folderKey) {
        return personalFolderDao.subIsExist(folderKey);
    }

    // 폴더 경로 찾아오기
    public String myPath(String key) throws Exception{
        return personalFolderDao.myPath(key);
    }

    // 폴더 주인 시퀀스
    public PersonalFolderDTO getFolderOwner(String resourceKey) {
        return personalFolderDao.getFolderOwner(resourceKey);
    }

    // 사용 권한
    public void accessStatus(String access, String key) {
        Map<String,String> map = new HashMap<>();
        map.put("access",access);
        map.put("key",key);
        personalFolderDao.accessStatus(map);
    }

    // personal_folder 테이블에 update
    public void updateMyFolderByte(String attachFolder, long fileSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("key",attachFolder);
        map.put("fileSize",fileSize);
        personalFolderDao.updateMyFolderByte(map);
    }

    // 폴더 정보 가져오기
    public PersonalFolderDTO myFolderInfo(String key) {
        return personalFolderDao.myFolderInfo(key);
    }

    // 폴더 삭제 하기
    public void memberOut(int memberSeq) {
        personalFolderDao.memberOut(memberSeq);
    }

    // 팀 폴더 가져오기
    public List<PersonalFolderDTO> getRootTeamFolder(List<Map<String, Integer>> teamSeqList) {
        return personalFolderDao.getRootTeamFolder(teamSeqList);
    }

    // 팀인지 개인인지 확인
    public boolean isTeam(String parentKey) {
        return personalFolderDao.isTeam(parentKey);
    }

    public PersonalFolderDTO pathAndType(String thisFolderKey) {
        return personalFolderDao.pathAndType(thisFolderKey);
    }

    // 팀 seq값 가져오기
    public int getTeamSeq(String resourceKey) {
        return personalFolderDao.getTeamSeq(resourceKey);
    }

    // team root의 key값 가져오기
    public String getRootTeamKey(int folderTeamSeq) {
        return personalFolderDao.getRootTeamKey(folderTeamSeq);
    }

    // 부모 key값 가져오기
    public String getParentKey(String searchKey) {
        return personalFolderDao.getParentKey(searchKey);
    }

    // 폴더 중복 유무
    public List<Map<String, String>> nameList(String parentKey) {
        return personalFolderDao.nameList(parentKey);
    }

    // 모든 팀 폴더 키 가져오기(해당하는 팀만)
    public List<Map<String,String>> allTeamKeys(String teamBasicKey) {
        // keyList = 상위폴더
        List<Map<String,String>> keyList = new ArrayList<>();
        Map<String,String> rootTeamKey = new HashMap<>();
        rootTeamKey.put("key",teamBasicKey);
        keyList.add(rootTeamKey);

        // 결과
        List<Map<String,String>> resultKeyList = new ArrayList<>();

        while(true){
            if(keyList.isEmpty()){
                break;
            }
            List<Map<String,String>> folderKey = keyList;
            for(Map<String,String> map : keyList){
                map.get("key");
                resultKeyList.add(map);
            }
            keyList = personalFolderDao.getTeamKeys(keyList);

        }
        // 하위 폴더들의 key
        return resultKeyList;
    }

    // 팀 탈퇴시 폴더들 전부 삭제
    public void teamOut(List<Map<String, String>> allKeys) {
        personalFolderDao.teamOut(allKeys);
    }
}


