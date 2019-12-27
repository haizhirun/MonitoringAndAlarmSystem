package com.atguigu.alarm.entity;

/**
 * 对应数据库中rules表格
 * DROP TABLE IF EXISTS `rules`;
 * CREATE TABLE `rules` (
 *   `rule_id` int(11) NOT NULL AUTO_INCREMENT,
 *   `rule_name` varchar(255) NOT NULL,
 *   `game_id` int(11) NOT NULL,
 *   `game_name` varchar(255) NOT NULL,
 *   `threshold` int(11) NOT NULL,
 *   `words` varchar(255) NOT NULL,
 *   `type` int(11) NOT NULL,
 *   `state` int(11) NOT NULL,
 *   PRIMARY KEY (`rule_id`)
 * ) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
 */
public class Rule {
    private int rule_id;
    private String rule_name;
    private int game_id;
    private String game_name;
    private int threshold;
    private String words;
    private int type;  //0按词取平均值，1按词之和
    private int state; //0规则有效，1规则失效

    public Rule(){

    }

    public Rule(Rule rule) {
        this.rule_id = rule.rule_id;
        this.rule_name = rule.rule_name;
        this.game_id = rule.game_id;
        this.game_name = rule.game_name;
        this.threshold = rule.threshold;
        this.words = rule.words;
        this.type = rule.type;
        this.state = rule.state;
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

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    @Override
    public String toString() {
        return String.format("rule_id:%d\trule_name:%s\tgame_id:%d\tgame_name:%s\tthreshold:%d\twords:%s\ttype:%d\tstate:%d",rule_id,rule_name,game_id,game_name,threshold,words,type,state);
    }
}
