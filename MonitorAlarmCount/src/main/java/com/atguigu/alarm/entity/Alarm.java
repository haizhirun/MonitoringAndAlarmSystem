package com.atguigu.alarm.entity;

/**
 * 输出的警报项
 * CREATE TABLE `alarms` (
 *   `alarm_id` int(11) NOT NULL AUTO_INCREMENT,
 *   `game_id` int(11) NOT NULL,
 *   `game_name` varchar(255) NOT NULL,
 *   `words` varchar(255) NOT NULL,
 *   `words_freq` varchar(255) NOT NULL,
 *   `rule_id` int(11) NOT NULL,
 *   `rule_name` varchar(255) NOT NULL,
 *   `has_sent` int(11) NOT NULL,
 *   `is_problem` int(11) NOT NULL,
 *   `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   PRIMARY KEY (`alarm_id`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
 */
public class Alarm {
    private int game_id;
    private String game_name;
    private String words;
    private String words_freq;
    private int rule_id;
    private String rule_name;
    private int has_sent;    //是否已经发送，默认值为0
    private int is_problem;  //是否真有问题，默认-1：未确认；0：不是；1：是

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public String getGame_name() {
        return game_name;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getWords_freq() {
        return words_freq;
    }

    public void setWords_freq(String words_freq) {
        this.words_freq = words_freq;
    }

    public int getRule_id() {
        return rule_id;
    }

    public void setRule_id(int rule_id) {
        this.rule_id = rule_id;
    }

    public String getRule_name() {
        return rule_name;
    }

    public void setRule_name(String rule_name) {
        this.rule_name = rule_name;
    }

    public int getHas_sent() {
        return has_sent;
    }

    public void setHas_sent(int has_sent) {
        this.has_sent = has_sent;
    }

    public int getIs_problem() {
        return is_problem;
    }

    public void setIs_problem(int is_problem) {
        this.is_problem = is_problem;
    }

    @Override
    public String toString() {
        return String.format("[Alarm] 游戏%s报警: %s (rule_id: %d)", game_name, rule_name, rule_id);
    }
}
