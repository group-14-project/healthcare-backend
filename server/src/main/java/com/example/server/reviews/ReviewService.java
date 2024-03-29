package com.example.server.reviews;

import com.example.server.connection.ConnectionEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepo;

    public ReviewService(ReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    public ReviewEntity addReview(ConnectionEntity connection, String review,  Integer rating){

        Optional<ReviewEntity> currReview = reviewRepo.findByConn1(connection);
        if(currReview.isPresent()){
            ReviewEntity review1 = currReview.get();
            review1.setRating(rating);
            review1.setReview(review);
            return reviewRepo.save(review1);
        }
        ReviewEntity newReview = new ReviewEntity();
        newReview.setReview(review);
        newReview.setRating(rating);
        newReview.setConn1(connection);

        return reviewRepo.save(newReview);
    }

}
