package com.atguigu.alarm.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 垃圾过滤工具类
 */
public class TrashFilterUtils {

    static Logger log = Logger.getLogger(TrashFilterUtils.class);

    static final int TYPE_FORUM_MAIN = 1;
    static final int TPYE_FORUM_SUB = 2;

    static final int TOO_LONG_TEXT_LEN = 10000;

    static Pattern patternChinese = Pattern.compile("[\\u4E00-\\u9FBF]+");
    static Pattern patternHtml = Pattern.compile("<[^>]+>");
    static String pattenFilePath = "/";

    static ArrayList<Pattern> storePatternList = null;
    static ArrayList<Pattern> forumPatternList = null;

    public static void reload() {
        ArrayList<Pattern> newStorePatternList = new ArrayList<Pattern>();
        ArrayList<Pattern> newForumPatternList = new ArrayList<Pattern>();
        parsePatternFile("patterns_appstore.txt", newStorePatternList);
        parsePatternFile("patterns_forum.txt", newForumPatternList);
        storePatternList = newStorePatternList;
        forumPatternList = newForumPatternList;
    }

    private static void parsePatternFile(String fileName,ArrayList<Pattern> patternList){
        Pattern patternSymbol = Pattern.compile("<[^>]+>");
        InputStream is = null;
        try {
            is = TrashFilterUtils.class.getResourceAsStream(pattenFilePath+fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 1024);

            int stage = 0;

            HashMap<String, String> symbolMap = new HashMap<>();

            for(String line = reader.readLine();line != null; line = reader.readLine()){
                if("".equals(line.trim())){
                    continue;
                }
                if("NonTerminal-Rule".equals(line)){
                    stage = 1;
                }else if("Terminal-Rule".equals(line)){
                    stage = 2;
                }else{
                    //<星级>:一|二|三|四|五|[0-9]
                    if(stage == 1){
                        String[] splits = line.split(":");
                        if(splits.length != 2){
                            log.error("symbol line: " + line + "splits is not 2");
                            continue;
                        }
                        log.info("symbol:" + splits[0] + " ,content: " + splits[1]);
                        symbolMap.put(splits[0],splits[1]);
                    }else if(stage == 2){
                        //提取出"key":"value"中的key值
                        String oldpatternStr = line.substring(1, line.lastIndexOf(":") - 1);
                        String newpatternStr = oldpatternStr;
                        boolean replaceSucc = true;
                        Matcher matcher = patternSymbol.matcher(oldpatternStr);
                        while(matcher.find()){
                            String symbol = matcher.group();
                            if(!symbolMap.containsKey(symbol)){
                                log.debug("get symbol:"+symbol + " fails for pattern: " + oldpatternStr);
                                replaceSucc = false;
                                break;
                            }
                            newpatternStr = newpatternStr.replace(symbol,symbolMap.get(symbol));
                        }
                        if(replaceSucc){
                            log.info("old pattern: " + oldpatternStr + " ,new pattern: " + newpatternStr);
                            try {
                                Pattern pattern = Pattern.compile(newpatternStr);
                                if(pattern!=null){
                                    patternList.add(pattern);
                                }
                            }catch (Exception e){
                                log.error("compile pattern fails: " + newpatternStr);
                            }
                        }

                    }
                }
            }
            log.info("pattern num : " + patternList.size());
        }catch (Exception e){
            log.error("parse pattern file error, ex: "+ ExceptionUtils.getMessage(e));
        }finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * 判断对应文本是否包含中文
     * @param text
     * @return
     */
    private static boolean isChinese(String text){
        return patternChinese.matcher(text).find();
    }

    /**
     * 将文本中包含html符号的文本替换为空字符串
     * @param text
     * @return
     */
    private static String filterHtml(String text){
        Matcher matcher = patternHtml.matcher(text);
        if(matcher.find()){
            return patternHtml.matcher(text).replaceAll("").trim();
        }
        return text;
    }

    /**
     * 判断对应文本是否属于垃圾信息
     * @return
     */
    public static boolean isTrash(String text){
        if(!isChinese(text)){
            return true;
        }

        String filterText = filterHtml(text);
        if(filterText.length()>=TOO_LONG_TEXT_LEN){
            return true;
        }
        for (Pattern pattern : storePatternList) {
            if(pattern.matcher(text).find()){
                return true;
            }
        }
        for (Pattern pattern : forumPatternList) {
            if(pattern.matcher(text).find()){
                return true;
            }
        }
        return false;
    }



    public static void main(String[] args) {
        System.out.println("呵呵 is chinese:" + isChinese("呵呵"));
        System.out.println("。 is chinese:" + isChinese("，"));
        System.out.println("， is chinese:" + isChinese("。"));
        System.out.println("112fsafdasf~!@#$%^&*()_+11123454606;k;, is chinese:"
                + isChinese("112fsafdasf~!@#$%^&*()_+11123454606;k;,"));
        System.out.println(" is chinese:" + isChinese(""));

        System.out.println(
                " <hehe haha> asfsdf </hehe haha> filter is:" + filterHtml("  <hehe haha> asfsdf </hehe haha>  "));
        System.out.println("不是html filter is:" + filterHtml(" 不是html "));

        System.out.println("ahaha测试长度 length is:" + "ahaha测试长度".length());

        String text = "42398485dasdfah";
        System.out.println(text + " isTrash:" + isTrash(text));
        text = "42398485dasdfahasfdsafsadfa啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊"
                + "222222222222啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊"
                + "啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊222222222222啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊"
                + "啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊"
                + "啊啊啊222222222222啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊"
                + "啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊222222222222啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊"
                + "啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊";
        System.out.println(text + " isTrash:" + isTrash(text));

        text = "加我QQ00312601";
        System.out.println(text + " isTrash:" + isTrash(text));
        text = "哈哈没人都给发3元红包";
        System.out.println(text + " isTrash:" + isTrash(text));
    }

}
