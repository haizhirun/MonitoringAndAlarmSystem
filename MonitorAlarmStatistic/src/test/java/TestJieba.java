import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.util.List;

/**
 * 测试结巴分词
 */
public class TestJieba {
    public static void main(String[] args) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        String[] sentences =
                new String[] {"这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。"};
//        List<SegToken> segTokens = segmenter.process(sentences[0], JiebaSegmenter.SegMode.SEARCH);
        List<SegToken> segTokens = segmenter.process(sentences[0], JiebaSegmenter.SegMode.INDEX);
        int len = segTokens.size();
        for(int i = 0; i < len; i++){
            System.out.print(segTokens.get(i).word + "\t" );
        }
        System.out.println();



    }
}
