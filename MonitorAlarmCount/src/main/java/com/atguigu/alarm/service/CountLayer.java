package com.atguigu.alarm.service;

import com.atguigu.alarm.dao.RulesDao;
import com.atguigu.alarm.entity.Record;
import com.atguigu.alarm.entity.Rule;
import com.atguigu.alarm.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计层：主要根据时间窗口及其他业务进行不同维度的统计
 */
public class CountLayer {
    private static Logger log = Logger.getLogger(CountLayer.class);

    //数据结构:[game_id->[rule_id->[word->count]]]
    public Map<Integer,Map<Integer, Map<String,Integer>>> gameRuleWordCount;

    //[rule_id->rule]
    public Map<Integer, Rule> idRule;

    /**
     * 重新加载规则库和游戏库
     */
    public void reload(){
        List<Rule> countRules = RulesDao.getGameRules();
        HashMap<Integer, Map<Integer, Map<String, Integer>>> newGameRuleWordCount = new HashMap<>();
        idRule = new HashMap<Integer, Rule>();
        for (Rule rule : countRules) {
            idRule.put(rule.getRule_id(),rule);
            if(!newGameRuleWordCount.containsKey(rule.getGame_id())){
                newGameRuleWordCount.put(rule.getGame_id(),new HashMap<Integer, Map<String, Integer>>());
            }
            if(! newGameRuleWordCount.containsKey(rule.getGame_id())){
                newGameRuleWordCount.put(rule.getGame_id(),new HashMap<Integer, Map<String, Integer>>());
            }

            for (String word : rule.getWords().split(" ")) {
                if(gameRuleWordCount != null && gameRuleWordCount.containsKey(rule.getGame_id())
                        && gameRuleWordCount.get(rule.getGame_id()).containsKey(rule.getRule_id())
                        && gameRuleWordCount.get(rule.getGame_id()).get(rule.getRule_id()).containsKey(word)){
                    newGameRuleWordCount.get(rule.getGame_id()).get(rule.getRule_id()).put(word,gameRuleWordCount.get(rule.getGame_id()).get(rule.getRule_id()).get(word));
                }else {
                    newGameRuleWordCount.get(rule.getGame_id()).get(rule.getRule_id()).put(word,0);
                }
            }

            //更新
            this.gameRuleWordCount = newGameRuleWordCount;
            log.warn("gameRuleWordCount reload done: " + gameRuleWordCount.size());

        }


    }

    /**
     * 将一条记录按照createTime进行统计
     * [game_id->[rule_id->[word->count]]]
     * @param record
     */
    public void addRecord(Record record){
        int game_id = record.getGame_id();
        if(!gameRuleWordCount.containsKey(game_id)){
            log.error("GameRuleWordCount don't contain gameId: " + game_id);
            return;
        }

        for (Map.Entry<Integer, Map<String, Integer>> ruleWord : gameRuleWordCount.get(game_id).entrySet()) {
            Integer rule_id = ruleWord.getKey();
            for (Map.Entry<String, Integer> wordCount : ruleWord.getValue().entrySet()) {
                String word = wordCount.getKey();
                if(isContain(record,word)){
                    gameRuleWordCount.get(game_id).get(rule_id).put(word,wordCount.getValue()+1);
                }
            }
        }

    }

    /**
     * 判断评论中是否包含指定词,n元组拼接匹配
     * @return
     */
    private boolean isContain(Record record,String word){
        String[] segWords = record.getReview_seg().split("\t");
        for(int i = 0; i < segWords.length; i++){
            for(int j = i+1; j < i + ConfigUtils.getIntValue("ngram") +1 && j <= segWords.length; j++){
                String mkWord = StringUtils.join(Arrays.copyOfRange(segWords, i, j), "");
                if(word.equals(mkWord)){
                    return true;
                }
            }
        }
        return false;
    }


    public static void main(String[] args) {
        String record = "垃圾\t连跪\t坑\t平衡性差\t抄袭";
        String[] segWords = record.split("\t");
        for(int i = 0; i < segWords.length; i++){
            for(int j = i+1; j < i + ConfigUtils.getIntValue("ngram") +1 && j <= segWords.length; j++){
                String mkWord = StringUtils.join(Arrays.copyOfRange(segWords, i, j), "");
                System.out.println(mkWord);
            }
        }
    }
}
