package com.fivet.buddy.controller;

import com.fivet.buddy.dto.CalDTO;
import com.fivet.buddy.services.CalService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/calendar/")
public class CalController {

    @Autowired
    private CalService calService;

    @Autowired
    private HttpSession session;

    private Logger logger = LoggerFactory.getLogger(CalController.class);

    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e) {
        e.printStackTrace();
        return "error";
    }

    @RequestMapping("insertEvent")
    public String insertEvent(CalDTO calDto) throws Exception{
        calService.insertEvent(calDto);
        return "redirect:/";
    }
    @ResponseBody
    @RequestMapping (value = "selectAll" , produces = "text/html;charset=utf8", method = {RequestMethod.POST})
    public String selectAll(int teamSeq, Model model) throws Exception{
            List<CalDTO> calList = calService.selectAll(teamSeq);
            model.addAttribute("list",calList);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            String list = gson.toJson(calList);
            return list;
    }

    @RequestMapping("deleteEvent")
    public String deleteEvent(int eventSeq) throws Exception{
        calService.deleteEvent(eventSeq);
        return "redirect:/";
    }


    @RequestMapping("updateEvent")
    public String updateEvent(CalDTO calDto) throws Exception {
        calService.updateEvent(calDto);
        return "redirect:/";
    }

}