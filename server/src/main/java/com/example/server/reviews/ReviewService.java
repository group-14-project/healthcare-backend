package com.example.server.reviews;

import com.example.server.connection.ConnectionEntity;
import com.example.server.dto.response.ViewReviewsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepo;

    public ReviewService(ReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    public ReviewEntity addReview(ConnectionEntity connection, String review){
        ReviewEntity reviewEntity1 = reviewRepo.findReviewByConnection(connection);
        if(reviewEntity1==null){
            ReviewEntity reviewEntity = new ReviewEntity();
            reviewEntity.setReview(review);
            reviewEntity.setConn1(connection);
            reviewEntity.setLocalDateTime(LocalDateTime.now());
            return reviewRepo.save(reviewEntity);
        }
        reviewEntity1.setReview(review);
        reviewEntity1.setLocalDateTime(LocalDateTime.now());
        return reviewRepo.save(reviewEntity1);
    }

    public List<ViewReviewsResponse> viewReviewsByConnection(List<ConnectionEntity> connectionEntities) {
        List<ReviewEntity> reviewEntities = reviewRepo.findAllByConnection(connectionEntities);
        List<ViewReviewsResponse> viewReviewsResponses = new ArrayList<>();
        for(ReviewEntity reviewEntity : reviewEntities){
            ViewReviewsResponse viewReviewsResponse = new ViewReviewsResponse();
            viewReviewsResponse.setReview(reviewEntity.getReview());
            viewReviewsResponse.setDateTime(reviewEntity.getLocalDateTime());
            viewReviewsResponse.setDoctorFirstName(reviewEntity.getConn1().getDoctor().getFirstName());
            viewReviewsResponse.setDoctorLastName(reviewEntity.getConn1().getDoctor().getLastName());
            viewReviewsResponse.setPatientFirstName(reviewEntity.getConn1().getPatient().getFirstName());
            viewReviewsResponse.setPatientLastName(reviewEntity.getConn1().getPatient().getLastName());

            viewReviewsResponses.add(viewReviewsResponse);
        }
        return viewReviewsResponses;
    }
}
