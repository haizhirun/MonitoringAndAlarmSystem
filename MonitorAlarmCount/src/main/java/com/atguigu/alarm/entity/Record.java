package com.atguigu.alarm.entity;

/**
 * spark过滤后的json记录
 */
public class Record {
    private int game_id;
    private String review;
    private String review_seg;
    private String game_name;

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReview_seg() {
        return review_seg;
    }

    public void setReview_seg(String review_seg) {
        this.review_seg = review_seg;
    }

    public String getGame_name() {
        return game_name;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public String toString(){
        return String.format("game_id:%d\treview:%s\treview_seg:%s\tgame_name:%s",game_id,review,review_seg,game_name);
    }
}
