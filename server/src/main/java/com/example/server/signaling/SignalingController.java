package com.example.server.signaling;

import com.example.server.connection.ConnectionService;
import com.example.server.doctor.DoctorEntity;
import com.example.server.doctor.DoctorService;
//import com.gargoylesoftware.htmlunit.javascript.host.media.rtc.RTCSessionDescription;
import com.example.server.dto.response.CallDetailsToSeniorDr;
import com.example.server.errorOrSuccessMessageResponse.ErrorMessage;
import com.example.server.jwtToken.JWTTokenReCheck;
import jakarta.persistence.LockModeType;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import com.example.server.webSocket.DoctorStatusScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RestController
@CrossOrigin
public class SignalingController {
//    ArrayList<String> users = new ArrayList<String>();

    private final DoctorService doctorService;
    private final DoctorStatusScheduler doctorStatusScheduler;

    private final JWTTokenReCheck jwtTokenReCheck;
    private final ConnectionService connectionService;
    private final Map<String, List<String>> map = new ConcurrentHashMap<>();

    private Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    public SignalingController(DoctorService doctorService, DoctorStatusScheduler doctorStatusScheduler, JWTTokenReCheck jwtTokenReCheck, ConnectionService connectionService) {
        this.doctorService = doctorService;
        this.doctorStatusScheduler = doctorStatusScheduler;
        this.jwtTokenReCheck = jwtTokenReCheck;
        this.connectionService = connectionService;
    }


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


    @MessageMapping("/userJoin")
    public void UserJoin(String message){

        System.out.println("join room msg: "+message);

        JSONObject msg = new JSONObject(message);
        System.out.println("join room msg: "+message);

        String roomId = msg.getString("roomId");
        String userId = msg.getString("id");
        String userName = msg.getString("displayName");

        System.out.println(roomId+' '+userId+' '+userName);

        roomUsers.computeIfAbsent(roomId, key -> new HashSet<>()).add(userId);

        JSONObject obj = new JSONObject();
        obj.put("userId", userId);
        obj.put("userName", userName);

        Set<String> users = roomUsers.getOrDefault(roomId, Collections.emptySet());

        obj.put("users",users);

        String replyMsg = obj.toString();

        users.forEach(user-> System.out.println(user));

        users.forEach(user -> simpMessagingTemplate.convertAndSendToUser(user,"/topic/userJoin", replyMsg));

    }

//    @MessageMapping("/addUser")
//    public void addUser(String user){
//        System.out.println("Adding User");
//        users.add(user);
//        for (String u :users) {
//            System.out.println(u);
//        }
//        System.out.println("User Added Successfully");
//    }

    @MessageMapping("/call")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public void Call(String call) throws IOException {
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
//            doctorService.changeStatusToInACall(doctorId);
//            doctorStatusScheduler.sendDoctorStatusUpdate();
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


    @MessageMapping("/seniorOffer")
    public void SeniorOffer(String offer){

//        System.out.println("Offer Came");
        JSONObject jsonObject = new JSONObject(offer);
//        System.out.println(jsonObject.get("offer"));
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/seniorOffer",offer);
        System.out.println("Offer Sent");

    }

    @MessageMapping("/seniorAnswer")
    public void SeniorAnswer(String answer){
//        System.out.println("Answer came");
//        System.out.println(answer);
        JSONObject jsonObject = new JSONObject(answer);
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
//        System.out.println(jsonObject.get("answer"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/seniorAnswer",answer);
        System.out.println("Answer Sent");
    }

    @MessageMapping("/seniorJoin")
    public void SeniorJoin(String msg){
//        System.out.println("Answer came");
//        System.out.println(answer);
        JSONObject jsonObject = new JSONObject(msg);
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
//        System.out.println(jsonObject.get("answer"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/seniorJoin",msg);
        System.out.println("notification Sent");
    }

    @MessageMapping("/leave")
    public void leave(String msg){
//        System.out.println("Answer came");
//        System.out.println(answer);
        JSONObject jsonObject = new JSONObject(msg);
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
//        System.out.println(jsonObject.get("answer"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/leave",msg);
//        System.out.println("notification Sent");
    }


    @MessageMapping("/seniorCandidate")
    public void SeniorCandidate(String candidate){
//        System.out.println("Candidate came");
        JSONObject jsonObject = new JSONObject(candidate);
//        System.out.println(jsonObject.get("toUser"));
//        System.out.println(jsonObject.get("fromUser"));
//        System.out.println(jsonObject.get("candidate"));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/seniorCandidate",candidate);
        System.out.println("Candidate Sent");
    }



    @MessageMapping("/acceptCall")
    public void AcceptCall(String call) throws IOException {
        System.out.println(call);
        JSONObject jsonObject = new JSONObject(call);
        JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("initiatedBy"));
        JSONObject jsonObjectFrom = new JSONObject(jsonObject.getString("acceptedBy"));
        String doctorId = jsonObjectFrom.getString("callee");

//        System.out.println(jsonObject.get("acceptedBy"));
//        System.out.println(jsonObject.get("initiatedBy"));
        doctorService.changeStatusToInACall(doctorId);
        doctorStatusScheduler.sendDoctorStatusUpdate();
        simpMessagingTemplate.convertAndSendToUser(jsonObjectTo.getString("caller"), "/topic/acceptCall", call);
    }

    @MessageMapping("/disconnectCall")
    public void DisconnectCall(String call) throws IOException {
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

        System.out.println("disconnect call jsonObject: "+jsonObject);
//
//        System.out.println(jsonObject.get(""));
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("initiatedBy"), "/topic/disconnectCall", call);
        doctorService.changeStatusToActive(doctorId);
        doctorStatusScheduler.sendDoctorStatusUpdate();

        String currentCall = null;
        if(queue!=null && !queue.isEmpty()){
            currentCall = queue.get(0);
            simpMessagingTemplate.convertAndSendToUser(doctorId,"/topic/call",currentCall);
        }

    }

    @MessageMapping("/rejectCall")
    public void RejectCall(String call) {
        JSONObject jsonObject = new JSONObject(call);
        JSONObject jsonObjectTo = new JSONObject(jsonObject.getString("initiatedBy"));

        simpMessagingTemplate.convertAndSendToUser(jsonObjectTo.getString("caller"), "/topic/rejectCall", call);
    }

    @GetMapping("/getCallDetails")
    public ResponseEntity<?> getCallDetails(HttpServletRequest request ) {
        System.out.println("hi");
        DoctorEntity doctorEntity = jwtTokenReCheck.checkJWTAndSessionSeniorDoctor(request);
        if (doctorEntity == null) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorMessage("Your Session has expired. Please Login again");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        List<String> calls = getFirstElementsOfLists();
        List<CallDetailsToSeniorDr> callDetailsToSeniorDrs = doctorService.callDetailsToSeniorDr(doctorEntity, calls);
        doctorService.setLastAccessTime(doctorEntity.getEmail());
        return ResponseEntity.ok(callDetailsToSeniorDrs);
    }

    private List<String> getFirstElementsOfLists() {
        List<String> firstElements = new ArrayList<>();

        for (List<String> list : map.values()) {
            if (!list.isEmpty()) {
                firstElements.add(list.get(0));
            }
        }
        return firstElements;
    }


}