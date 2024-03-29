package com.example.server.reviews;

import com.example.server.connection.ConnectionEntity;
import com.example.server.connection.ConnectionService;
import com.example.server.dto.request.AddReviewRequest;
import com.example.server.patient.PatientController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/review")
public class ReviewController {
    private final ReviewService reviewService;

    private final ConnectionService connection;

    public ReviewController(ReviewService reviewService, ConnectionService connection) {
        this.reviewService = reviewService;
        this.connection = connection;
    }

    @PostMapping("/addReview")
    public ResponseEntity<Void> addReview(@RequestBody AddReviewRequest reviewRequest){
        ConnectionEntity connectionEntity = connection.findConnection(reviewRequest.getDoctorEmail(), reviewRequest.getPatientEmail());
        if(connectionEntity==null){
            throw new PatientController.UnexpectedErrorException();
        }
        ReviewEntity review = reviewService.addReview(connectionEntity, reviewRequest.getReview(), reviewRequest.getRating());
        if(review==null){
            throw new PatientController.UnexpectedErrorException();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
