package com.example.server.signaling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gargoylesoftware.htmlunit.javascript.host.media.rtc.RTCSessionDescription;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.socket.TextMessage;
import java.util.ArrayList;

@Controller
public class SignalingController {
    ArrayList<String> users = new ArrayList<String>();

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;


    @RequestMapping(value = "/",method =  RequestMethod.GET)
    public String Index(){
        return "hey";
    }

    @MessageMapping("/testServer")
    @SendTo("/topic/testServer")
    public String testServer(String Test){
        System.out.println("Testing Server");
        return Test;
    }

    @MessageMapping("/addUser")
    public void addUser(String user){
        System.out.println("Adding User");
        users.add(user);
        for (String u :users) {
            System.out.println(u);
        }
        System.out.println("User Added Successfully");
    }

    @MessageMapping("/call")
    public void Call(String call){
        JSONObject jsonObject = new JSONObject(call);

        System.out.println(jsonObject.get("callTo").getClass());
        JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("callTo"));
        JSONObject jsonObjectFrom = new JSONObject(jsonObject.getString("callFrom"));
        System.out.println(jsonObjectTo);
        System.out.println(jsonObjectTo.getString("doctorName"));
//        System.out.println("Calling to: "+jsonObject.get("callTo")+" Call from "+jsonObject.get("callFrom"));
//        System.out.println("Calling to class: "+jsonObject.get("callTo").getClass()+" Call from class "+jsonObject.get("callFrom").getClass());
        simpMessagingTemplate.convertAndSendToUser(jsonObjectTo.getString("remoteId"),"/topic/call",call);
    }

    @MessageMapping("/offer")
    public void Offer(String offer){

//        System.out.println("Offer Came");
        JSONObject jsonObject = new JSONObject(offer);
//        System.out.println(jsonObject.get("offer"));
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/offer",offer);
        System.out.println("Offer Sent");

    }

    @MessageMapping("/answer")
    public void Answer(String answer){
//        System.out.println("Answer came");
//        System.out.println(answer);
        JSONObject jsonObject = new JSONObject(answer);
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
//        System.out.println(jsonObject.get("answer"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/answer",answer);
        System.out.println("Answer Sent");
    }
    @MessageMapping("/candidate")
    public void Candidate(String candidate){
//        System.out.println("Candidate came");
        JSONObject jsonObject = new JSONObject(candidate);
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
//        System.out.println(jsonObject.get("candidate"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/candidate",candidate);
        System.out.println("Candidate Sent");
    }

    @MessageMapping("/acceptCall")
    public void AcceptCall(String call){
        System.out.println(call);
        JSONObject jsonObject = new JSONObject(call);
        JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("initiatedBy"));
//        JSONObject jsonObjectFrom = new JSONObject(jsonObject.getString("acceptedBy"));
//        System.out.println(jsonObject.get("acceptedBy"));
//        System.out.println(jsonObject.get("initiatedBy"));
        simpMessagingTemplate.convertAndSendToUser(jsonObjectTo.getString("caller"), "/topic/acceptCall", call);
    }

    @MessageMapping("/disconnectCall")
    public void DisconnectCall(String call){
        System.out.println(call);
        JSONObject jsonObject = new JSONObject(call);
//        System.out.println(jsonObject.get(""));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("initiatedBy"), "/topic/disconnectCall", call);
    }

    @MessageMapping("/rejectCall")
    public void RejectCall(String call){
        JSONObject jsonObject = new JSONObject(call);
        JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("initiatedBy"));

        simpMessagingTemplate.convertAndSendToUser(jsonObjectTo.getString("caller"), "/topic/rejectCall", call);
    }

}