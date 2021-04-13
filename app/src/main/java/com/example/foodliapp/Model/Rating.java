package com.example.foodliapp.Model;

public class Rating {
    private  String userPhone;
    private  String foodId;
    private  String ratingValue;
    private  String userComment;

    public Rating() {
    }

    public Rating(String userPhone, String foodId, String ratingValue, String userComment) {
        this.userPhone = userPhone;
        this.foodId = foodId;
        this.ratingValue = ratingValue;
        this.userComment = userComment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(String ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }
}
