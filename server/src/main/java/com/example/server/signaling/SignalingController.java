package com.example.server.signaling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gargoylesoftware.htmlunit.javascript.host.media.rtc.RTCSessionDescription;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.socket.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class SignalingController {
    ArrayList<String> users = new ArrayList<String>();

    private final Map<String, List<String>> map = new ConcurrentHashMap<>();

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

        System.out.println("Call: "+call);

        JSONObject jsonObject = new JSONObject(call);

        System.out.println(jsonObject.get("callTo").getClass());
        JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("callTo"));
        JSONObject jsonObjectFrom = new JSONObject(jsonObject.getString("callFrom"));
        System.out.println(jsonObjectTo);
        System.out.println(jsonObjectTo.getString("doctorName"));
        String doctorId = jsonObjectTo.getString("remoteId");
        String patientId = jsonObjectFrom.getString("localId");
        String patientName = jsonObjectFrom.getString("patientName");
        String doctorName = jsonObjectTo.getString("doctorName");

//        simpMessagingTemplate.convertAndSendToUser(doctorId,"/topic/call",call);

        map.computeIfAbsent(doctorId,k->new ArrayList<>()).add(call);

        List<String> queue = map.get(doctorId);
        String currentCall = null;
        if(queue!=null && !queue.isEmpty()){
            currentCall = queue.get(0);
        }

        if(currentCall.equals(call)){
            System.out.println("equals");
            simpMessagingTemplate.convertAndSendToUser(doctorId,"/topic/call",call);
        }
        else{
            System.out.println("still in queue");
            simpMessagingTemplate.convertAndSendToUser(patientId,"/topic/call",queue.size());
        }

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

        String localId = jsonObject.getString("acceptedBy");
        String remoteId = jsonObject.getString("initiatedBy");
        String role = jsonObject.getString("role");

        String patientId=null,doctorId=null;

        if(role.equals("doctor")){
            patientId = remoteId;
            doctorId = localId;
        }
        else{
            patientId = localId;
            doctorId = remoteId;
        }
//
        List<String> queue = map.get(doctorId);
        queue.remove(0);


//
//        System.out.println(jsonObject.get(""));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("initiatedBy"), "/topic/disconnectCall", call);

        String currentCall = null;
        if(queue!=null && !queue.isEmpty()){
            currentCall = queue.get(0);
            simpMessagingTemplate.convertAndSendToUser(doctorId,"/topic/call",currentCall);
        }

    }



    @MessageMapping("/rejectCall")
    public void RejectCall(String call){
        JSONObject jsonObject = new JSONObject(call);
        JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("initiatedBy"));

        simpMessagingTemplate.convertAndSendToUser(jsonObjectTo.getString("caller"), "/topic/rejectCall", call);
    }



}