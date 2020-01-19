# so-jpql-template-engine
## :rocket: 复杂多条件SQL语句模板引擎
velocity+xml解析的sql模板引擎，让复杂多条件的sql编写更简单、更快速

![](https://img.shields.io/static/v1?label=release&message=v1.0.0&color=9cf&?style=flat-square)
![](https://img.shields.io/static/v1?label=build&message=pass&color=green&?style=flat-square)
![](https://img.shields.io/static/v1?label=License&message=Apache-2.0&color=blue&?style=flat-square)
![](https://img.shields.io/static/v1?label=spring-boot&message=2.1.6.RELEASE&color=yellow&?style=flat-square)
![](https://img.shields.io/static/v1?label=downloads&message=14&color=orange&?style=flat-square)

## :panda_face: 使用案例
1. 在spring boot项目的resource目录下新建一个`jpql`文件夹，并在`jpql`文件夹下新建一个xml文件用来写sql语句。比如，这里我新建一个`TestJpql.xml`，目录结构如下：
```
...
--> resource
   --> jpql
       --> TestJpql.xml
...
```
2. 在`TestJpql.xml`中编写sql语句。举个例子：
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<jpa module="testJpql">
    <jpql id="customSQL">
        select * from so_tag where 1=1
        #if($title)
        and title like :title
        #end
        #if($readCount)
        and read_count &gt; :readCount
        #end
        #if($tagNames)
        and tag_name in (:tagNames)
        #end
        limit 10
    </jpql>
    <jpql id="xxx">
        ...
    </jpql>
    ...
</jpa>
```
这里解释下编写的语法：
* xml标签基本固定，主要用来定位每个`jpql`标签中的sql语句。
    * `jpa`标签中的`module`属性是这个xml文件的`id`，不同的文件通过这个属性做区分；
    * `jpql`标签中的`id`属性用于标识一个xml文件中不同的sql语句；
* `jpql`标签中的sql语句语法参考[velocity模板引擎](http://velocity.apache.org/)

3. 在代码中使用。此处在Test中通过hibernate框架执行解析后的sql：
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ServiceTest {

    @Autowired
    JpqlParser jpqlParser;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public Session getSession() {
        return entityManagerFactory.unwrap(SessionFactory.class).openSession();
    }

    @Test
    public void test1() {
        Map<String, Object> parameters = new HashMap<>(4);
//        parameters.put("title", "%原则%");
//        parameters.put("readCount", 400);
        parameters.put("tagNames", Arrays.asList("平台", "后台"));
        String sql = jpqlParser.parse(new ParserParameter("testJpql.customSQL", parameters, "mysql")).getExecutableSql();
        Query query = getSession().createNativeQuery(sql).addEntity(TagEntity.class);
        List<TagEntity> result = query.getResultList();
        System.out.println("结果返回个数：" + result.size());
    }
}
```
输出如下：
```
2020-01-19 17:48:46.890  INFO 16228 --- [main] com.sleepy.jpql.ParsedJpql: {"QueryId": "customSQL","SQL": "select * from so_tag where 1=1 and tag_name in ('平台','后台') limit 10"}
结果返回个数：10
```
这里的parameters中的每个元素对应的就是`jpql`标签sql语句中的变量。通过**so-jpql-template-engine**框架，你可以获取到解析后的sql语句，然后可以通过ORM框架或原生JDBC去执行获取查询结果。免去了类似sql需要手动重复拼接的麻烦，通过一条sql模板即可搞定。:stuck_out_tongue_winking_eye:

## :sunny: 使用步骤
1. 下载本项目到本地
```bash
git clone git@github.com:SleepyOcean/so-jpql-template-engine.git
```
2. 将项目安装到本地maven仓库
```bash
mvn clean install
```
3. 在自己的maven项目中的pom.xml中引入依赖
```bash
<dependencies>
       ... ...
       <dependency>
           <groupId>com.sleepy</groupId>
           <artifactId>so-jpql-template-engine</artifactId>
           <version>1.0.0</version>
       </dependency>
       ... ...
</dependencies>
```
4. 剩下的就是上述案例的内容啦~

## :running: 未完待续
本项目刚刚提交，还未发布到Maven中央仓库，后续会发布到中央仓库，简化依赖引入

## :blue_heart: 版权说明
该项目签署了Apache-2.0授权许可，详情请参阅 LICENSE.md
