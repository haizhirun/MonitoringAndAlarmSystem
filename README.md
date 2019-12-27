### 监控报警系统实战

#### 项目概况
本项目首先开发一个简单地爬虫程序，从热门的taptap社区爬取指定游戏的用户评论，
然后利用Spark Streaming 对评论进行过滤分词，最终通过java应用程序将结果归纳汇总，
根据预定规则进行报警。

#### 项目流程
 整个项目中，kafka作为数据总线，整个流程如下：\
（1）利用爬虫程序爬取用户评论并灌输入kafka数据总线中。\
（2）流式应用从kafka中拉去数据，进行统计分析后，再将分析后的数据灌输到kafka的另一个topic中。\
（3）java汇总程序从kafka中拉去分析过后的数据进行归纳汇总，对于达到报警规则限制的情况进行报警，写入数据库中。\
（4）最后针对具体的场景，可以根据数据库中的记录，进行邮件、微信等报警，通知用户。



