# so-jpql-template-engine
## :rocket: 复杂多条件SQL语句模板引擎
velocity+xml解析的sql模板引擎，让复杂多条件的sql编写更简单、更快速

![](https://img.shields.io/static/v1?label=release&message=v1.1.0&color=9cf&?style=flat-square)
![](https://img.shields.io/static/v1?label=build&message=pass&color=green&?style=flat-square)
![](https://img.shields.io/static/v1?label=License&message=Apache-2.0&color=blue&?style=flat-square)
![](https://img.shields.io/static/v1?label=spring-boot&message=2.1.6.RELEASE&color=yellow&?style=flat-square)
![](https://img.shields.io/static/v1?label=downloads&message=14&color=orange&?style=flat-square)

## :paperclip:版本更新历史
* 2020-04-21
版本：**1.1.0.RELEASE**
更新内容：
	1. 简化jpql语句编写。去除jpql分页语句块，改为引擎内部组织分页语句；
	2. 简化并支持多种查询结果接收类的定义。默认将数据库中以下划线"_"组织的字段名转为接收类的驼峰命名，也可通过JpqlCol注解自定义匹配，也可通过jpql中select出as定义接收类的字段名；
	3. 简化引擎内部代码。默认转为sql语句为mysql类型（后期会做配置项进行扩展）；
	4. 接收类字段类型兼容转换。对查询结果的字段类型做了常见类型的兼容，目前支持String、Integer、Long、Float、Double。
* 2020-02-29
版本：**1.0.1.RELEASE**
更新内容：
	1. 新增jpql执行器，以component的形式注入使用；
	2. 新增jpql执行结果包装类，始终返回结果总数，便于分页


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
    <jpql id="customPageSQL">
        select tag_name, article_ids from so_tag where 1=1
        #if($name)
        and tag_name like :name
        #end
        #if($tagNames)
        and tag_name in (:tagNames)
        #end
    </jpql>
    <jpql id="customCountSQL">
        select count(*) as articleAmount, sum(hot_rate) as hotRateSum from so_article where 1=1
        #if($startTime)
        and create_time &gt; :startTime
        #end
        #if($endTime)
        and create_time &lt; :endTime
        #end
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
    JpqlExecutor jpqlExecutor;

    @Test
    public void customPageSqlTest() {
        // 分页sql查询方式
        JpqlResultSet<ChartOfBarDTO> result = null;
        result = jpqlExecutor.execPageable("testJpql.customPageSQL",
                CommonTools.getCustomMap(new MapModel("tagNames", Arrays.asList("数据库", "数据分析", "大数据"))),
                TestDTO.class, PageRequest.of(0, 30));
        List<ChartOfBarDTO> list = result.getResultList();
        Map<String, Object> resultMap = CommonTools.getCustomMap(new MapModel("result", list), new MapModel("total", result.getTotal()));
        System.out.println(JSON.toJSONString(resultMap));
    }

    @Test
    private Map<String, Object> customCountSqlTest() {
        // 别名sql查询
        JpqlResultSet<ChartOfBarDTO> result = null;
        result = jpqlExecutor.exec("testJpql.customCountSQL",
                CommonTools.getCustomMap(new MapModel("startTime", "2020-01-08 00:00:00")),
                TestDTO.class);
        List<ChartOfBarDTO> list = result.getResultList();
        Map<String, Object> resultMap = CommonTools.getCustomMap(new MapModel("result", list), new MapModel("total", result.getTotal()));
        System.out.println(JSON.toJSONString(resultMap));
    }
}
```

```java
@Data
public class TestDTO {
    private String id;
    private String tagName;
    @JpqlCol("article_ids")
    private String articleId;
    private String articleAmount;
    private Long hotRateSum;
    private String createTime;
    private String title;
}
```
输出如下：
```
customPageSqlTest:
{
    result: [{
        id: null,
        tagName: "数据库",
        articleId: "alewti25125h123kjh1243251",
        ...
    },
    ...]
    total: 32
}

customCountSqlTest:
{
    result: [{
        id: null,
        ...
        articleAmount: "23",
        hotRateSum: 52412,
        ...
    },
    ...]
    total: 1
}

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
4. 在自己的spring boot**启动类**中的包扫描注解中加入`com.sleepy.jpql`包名
```java
@SpringBootApplication(scanBasePackages = {"com.sleepy.blog", "com.sleepy.jpql"})
...
public class SoBlogServiceApplication extends SpringBootServletInitializer {
   ...
}
```
5. 剩下的就是上述案例的内容啦~

## :running: maven私服引入
私服为本人搭建，简化依赖引入，由于服务器性能较弱，不能保证始终提供服务。若无法访问私服，可通过本地编译的方式导入，详细见上面的**使用步骤**
```bash
<dependencies>
       ... ...
       <dependency>
           <groupId>com.sleepy</groupId>
           <artifactId>so-jpql-template-engine</artifactId>
           <version>1.1.0.RELEASE</version>
       </dependency>
       ... ...
</dependencies>
... ...
<repositories>
    ... ...
    <repository>
        <id>oceanmaven</id>
        <name>ocean maven</name>
        <url>http://nexus.sleepyocean.cn/repository/maven-ocean/</url>
    </repository>
    ... ...
</repositories>
```

## :blue_heart: 版权说明
该项目签署了Apache-2.0授权许可，详情请参阅 LICENSE.md
