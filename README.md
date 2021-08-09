# mybatis-note

### 前期准备

1. #### jdbc

   ```java
   package com.dzq;
   import java.sql.*;
   /**
    * 来源博客
    * https://blog.csdn.net/lihao21/article/details/80694503
    */
   public class JDBCTest {
       public static void main(String[] args) {
           Connection conn = null;
           // 配置项
           String url = "jdbc:mysql://*.*.*.*:3306/test?autoReconnect=true&useSSL=false";
           String driver = "com.mysql.cj.jdbc.Driver";
           String userName = "root";
           String password = "XXXXXXXX";
           Statement stmt = null;
           ResultSet rs = null;
           try {
               // 加载驱动 从配置获取
               Class.forName(driver);
               // 获取数据源 从配置获取
               conn = DriverManager.getConnection(url, userName, password);
               // 得到执行声明 从connection获取
               stmt = conn.createStatement();
               // 获取sql语句
               String sql = "select * from EMPLOYEES";
               // 执行sql
               rs = stmt.executeQuery(sql);
               // 处理结果
               while (rs.next()) {
                   int id = rs.getInt("emp_id");
                   String name = rs.getString("name");
                   System.out.println("id = " + id + ", name = " + name);
               }
               // 关闭
               rs.close();
               stmt.close();
               conn.close();
           } catch (Exception e) {
               e.printStackTrace();
           } finally {
               if (rs != null) {
                   try {
                       rs.close();
                   } catch (SQLException sqlEx) { } // ignore
               }
   
               if (stmt != null) {
                   try {
                       stmt.close();
                   } catch (SQLException sqlEx) { } // ignore
               }
           }
       }
   }
   ```

   ###### Connection 接口关键方法

   ```java
   // 创建Statement
   Statement createStatement() throws SQLException;
   // 根据sql创建预编译sql的Statement：PreparedStatement
   PreparedStatement prepareStatement(String sql)
           throws SQLException;
   // 如果可以设置属性，那么在构造Connection时，框架就要考虑如何设置属性
   // 是否自动提交，事务的关键参数，Connection属性之一
   void setAutoCommit(boolean autoCommit) throws SQLException;
   // 是否是只读，Connection属性之一
   void setReadOnly(boolean readOnly) throws SQLException;
   // 事务级别，属性之一
   void setTransactionIsolation(int level) throws SQLException;
   // jdbc3.0  转载博客:https://www.huaweicloud.com/articles/9556488.html
   // resultSetType决定ResultSet属性
   // ResultSet.TYPE_FORWORD_ONLY 默认的cursor 类型，仅仅支持结果集forward ，不支持backforward ，random ，last ，first 等操作;
   // ResultSet.TYPE_SCROLL_INSENSITIVE 支持结果集backforward ，random ，last ，first 等操作，对其它session 对数据库中数据做出的更改是不敏感的。实现方法：从数据库取出数据后，会把全部数据缓存到cache 中，对结果集的后续操作，是操作的cache 中的数据，数据库中记录发生变化后，不影响cache 中的数据，所以ResultSet 对结果集中的数据是INSENSITIVE 的;
   // ResultSet.TYPE_SCROLL_SENSITIVE 从数据库取出数据后，不是把全部数据缓存到cache 中，而是把每条数据的rowid 缓存到cache 中，对结果集后续操作时，是根据rowid 再去数据库中取数据。所以数据库中记录发生变化后，通过ResultSet 取出的记录是最新的，即ResultSet 是SENSITIVE 的。 但insert 和delete 操作不会影响到ResultSet ，因为insert 数据的rowid 不在ResultSet 取出的rowid 中，所以insert 的数据对ResultSet 是不可见的，而delete 数据的rowid 依旧在ResultSet 中，所以ResultSet 仍可以取出被删除的记录（因为一般数据库的删除是标记删除，不是真正在数据库文件中删除）;
   // ResultSetConcurrency的可选值有2个：
   // ResultSet.CONCUR_READ_ONLY 在ResultSet中的数据记录是只读的，不可以修改;
   // ResultSet.CONCUR_UPDATABLE 在ResultSet中的数据记录可以任意修改，然后更新到数据库，可以插入，删除，修改;
   // ResultSetHoldability 的可选值有2个：
   // HOLD_CURSORS_OVER_COMMIT: 在事务commit 或rollback 后，ResultSet 仍然可用;
   // CLOSE_CURSORS_AT_COMMIT: 在事务commit 或rollback 后，ResultSet 被关闭;
   // com.dzq.JDBCConnectionTest#test_02 测试方法
   Statement createStatement(int resultSetType, int resultSetConcurrency,
                                 int resultSetHoldability) throws SQLException;
   // 还有其他属性，不在列举，重点是设计时如何设置属性的值。
   ```

   ###### Statement 接口关键方法

   > Statement 由Connection 中获取，二种Statement ：Statement，PreparedStatement

   ![Statement](https://raw.githubusercontent.com/dzhiqiang/PicGo-gallery/main/Statement1.png)

   1. Statement:获取方式 Statement createStatement() throws SQLException;

      是基类，能够执行一个或者多个sql

      ```java
      // 执行查询语句select
      ResultSet executeQuery(String sql) throws SQLException;
      // 执行变更语句update,delete,insert
      int executeUpdate(String sql) throws SQLException;
      // 涉及到的属性，超时时间,其他属性省略
      void setQueryTimeout(int seconds) throws SQLException;
      ```

   2. PreparedStatement：因为已经预设了执行语句，所以执行的时候不需要带sql参数，但是需要对语句通配符？赋值

      ```java
      // 得到执行结果
      ResultSet executeQuery() throws SQLException;
      // 执行变更语句update,delete,insert
      int executeUpdate() throws SQLException;
      // 对索引下标赋值 从1开始
      void setInt(int parameterIndex, int x) throws SQLException;
      ```

   ###### 执行结果的处理

   > 执行结果来源是Statement执行sql语句，分为5类，ResultSet查询语句，int变更语句，boolean：执行是否成功，int[]:批次执行：对于mybatis实现是累计转换成int类型，long：大数据变更，long[]:大数据批量变更

2. 动态代理

   > 通过java.lang.reflect.Proxy#newProxyInstance方法创建接口的代理，创建了一个持有InvocationHandler实例的匿名实现类。newProxyInstance三个参数：Class<?>[] interfaces：如果知道接口生成一个类的话，就知道他有哪些方法及参数和返回值，生成的这个类也知道继承了这个接口；ClassLoader loader：生成的类当然需要被加载，此参数规定使用什么样的类加载器加载。InvocationHandler h：主要起到辅助作用，当调用实现类的时候额外调用此方法invoke方法即可，因为newProxyInstance生成的匿名类无法传入**数据**，比如构造方法无参，也就不能把一些**数据**放入到匿名类中，但是匿名类可以调用invoke，通过把**数据**放入到InvocationHandler，比如构造方法的时候，当然InvocationHandler有此属性，可以通过invoke操作此**数据**
   >
   > 代理类的概念：具有被代理类的相同功能（下面的例子打印），但是代理类的核心代码是代理应该做的事情（比如去拿纸）和真正功能毫无关心，真正使用功能，还是被代理类去执行，当然你可以选择不执行真实功能（所以mybatis只有接口，没有实现类）。

   ![proxy](https://raw.githubusercontent.com/dzhiqiang/PicGo-gallery/main/proxy.png)

   ```java
   // 打印，被代理的接口
   public interface Print {
       public void print();
   }
   // 真正打印的类
   public class PrintImpl implements Print{
       public void print() {
           System.out.println("打印。。。");
       }
   }
   // 首先分析，你这个类为什么要被代理，是操作打印之前要干其他事情吗？加上之后的动作要符合代理的特性。
   // 正如上面介绍动态代理所说，通过InvocationHandler的数据引用实现真实调用的。
   // 为什么称之为动态，首先需要拿纸的接口都可以被道理，为什么？
   // 1:实例的引用是Object，可以放入任何实例
   // 2:调用的方式是根据根据invoke传递过来的，可以是任何方法，比如test_02，洗手接口
   // 3:接口是定义的时候才传入的
   public class InvocationHandlerImpl implements InvocationHandler {
       private Object object;
       public InvocationHandlerImpl(Object object) {
           this.object = object;
       }
       public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
           // 代理类的核心代码所做的事情,所以此InvocationHandler，也可叫做“拿纸代理”，打印日志的叫做日志代理：LogInvocationHandler
           // mybatis可以通过method的拿到id,通过Object proxy可以拿到接口名称也就是namespace,如下面的例子
           System.out.println("去拿纸...");
           // 真正使用功能，还是被代理类去执行
           return method.invoke(object, args);
       }
   }
   @Test
   public void test_01() {
       // 创建实例
       Print print = new PrintImpl();
       // 创建代理类
       Print printProxy = (Print) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                                        new Class[]{Print.class}, new InvocationHandlerImpl(print));
       // 代理执行
       printProxy.print();
   }
   
   // 洗手的例子
   public interface Wash {
       public void wash();
   }
   public class WashImpl implements Wash{
       public void wash() {
           System.out.println("洗手。。。");
       }
   }
   @Test
   public void test_02() {
       Wash wash = new WashImpl();
       Wash proxy = (Wash) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                                  new Class[]{Wash.class}, new InvocationHandlerImpl(wash));
       proxy.wash();
   }
   ```

   test_01和test_02结合，首先已经知道需要什么功能的接口， 也已经知道需要什么样的代理，比如我已经知道Wash，那如何创建Wash的代理

   ```java
   // 代理工厂
   public class PaperProxyFactory {
       public static Object createProxy(Object instance) {
           return Proxy.newProxyInstance(instance.getClass().getClassLoader(),
                   instance.getClass().getInterfaces(), new InvocationHandlerImpl(instance));
       }
   }
   @Test
   public void test_03() {
       Wash wash = new WashImpl();
       Wash proxy = (Wash) PaperProxyFactory.createProxy(wash);
       proxy.wash();
   }
   ```

   mybatis实现模拟

   ```java
   package com.dzq;
   public interface TransactionMapper {
       String getFlowKey(long id);
   }
   ```

   ```xml
   <mapper namespace="com.dzq.TransactionMapper">
       <select id="getFlowKey" resultType="string">
           select flow_key from transaction where id = #{id}
       </select>
   </mapper>
   ```

   ```java
   // namespace,id的获取
   Class<?>[] classes = proxy.getClass().getInterfaces();
   for (int i = 0; i < classes.length; i++) {
       // com.dzq.TransactionMapper
       System.out.println(classes[i].getName());
   }
   // getFlowKey也可以拿到返回值，然后根据sql的执行结果，转换此方法的返回值
   System.out.println(method.getName());
   // 可以拿到sql执行，返回结果
   ```

### 源码解读

[官方文档](https://mybatis.org/mybatis-3/)

> 根据前期准备jdbc阶段进行解读
>
> 1. 配置
> 2. 拼接sql语句
> 3. 加载数据源，获取Connection
> 4. 获取Statement
> 5. 执行获取结果
> 6. 结果转换
> 7. 关闭
> 8. 异常处理

##### 示例1：com.dzq.MybatisSessionTest

> 最基本的功能，根据此示例读源码

```java
public static void main(String[] args) throws IOException {
    // 配置文件
    String resource = "mybatis-config.xml";
    // 解析配置文件
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory =
            new SqlSessionFactoryBuilder().build(inputStream);
    // 获取session
    SqlSession sqlSession = sqlSessionFactory.openSession();
    // 根据state找到sql语句并传入参数执行
    String flowKey = sqlSession.selectOne("com.dzq.TransactionMapper.getFlowKey", 10);
    System.out.println(flowKey);
}
```

##### 示例 2： com.dzq.MybatisMapperTest

> 如何生成动态代理Mapper,动态代理都做了什么：如何根据动态代理找到对应的sql

```java
public static void main(String[] args) throws IOException {
    // 配置文件
    String resource = "mybatis-config.xml";
    // 解析配置文件
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory =
            new SqlSessionFactoryBuilder().build(inputStream);
    // 获取session
    SqlSession sqlSession = sqlSessionFactory.openSession();
    // 得到mapper代理
    TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);
    // 执行代理方法,并转换结果
    String flowKey = transactionMapper.getFlowKey(10);
    System.out.println(flowKey);
}
```

配置文件mybatis-config.xml和对应的TransactionMapper.xml

```xml
<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://10.9.224.45:3306/activiti?useUnicode=true&amp;characterEncoding=utf8&amp;serverTimezone=UTC&amp;useSSL=false" />
                <property name="username" value="root" />
                <property name="password" value="root" />
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="mapper/TransactionMapper.xml"/>
    </mappers>
</configuration>
```

```xml
<mapper namespace="com.dzq.TransactionMapper">
    <select id="getFlowKey" resultType="string">
        select flow_key from transaction where id = #{id}
    </select>
</mapper>
```

#### 1. 配置

> 在不清楚mybatis整体设计的时候，对于配置的每个属性和参数太多解读其实没有太多意义，最少咱们通过上面jdbc的预习已经知道一些配置参数，但是目前阶段可以了解
>
> 1. mybatis使用了什么样的配置模式？是properties还是xml,还是支持数据库这种支持。
> 2. 在解析配置是如何加载的？设计到什么样的设计模式？

##### 	配置选型

​    mybatis选择的配置模式，使用的xml文件，xml可以很好的使用标签对应类，也可以很好的描述类之间的关系和依赖。

##### 	加载

```java
String resource = "mybatis-config.xml";
// 在classpath下查找文件，得到输入源：很多文件的解析参数都是InputStream，可以来自于网络：（应用层可以是分布式文件系统，也可以是http/https等等），也可以来源于本地
InputStream inputStream = Resources.getResourceAsStream(resource);
// 拿到数据源，就可以拿到数据，然后进行解析，解析完后生成SqlSessionFactory工厂，SqlSession是直接执行sql的类
// 通过配置生成对应的工厂，因为已经知道“零件”，可以通过零件创建对应的“产品”
SqlSessionFactory sqlSessionFactory =
        new SqlSessionFactoryBuilder().build(inputStream);
```

##### 加载过程

1. 创建专门的解析类XMLConfigBuilder继承BaseBuilder

   ```java
   // inputStream 有input就能得到所有的配置信息，但是还是专门创建一个类负责解析
   // parser存储所有解析后结果configuration和是否已经解析过，专门负责解析xml的工作还是会由XPathParser负责
   XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
   // 构造方法 
   public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
       // 第一个参数XPathParser，使用的xpath解析器
       this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
   }
   ```

2. 解析

   ```java
   public Configuration parse() {
     if (parsed) {
       throw new BuilderException("Each XMLConfigBuilder can only be used once.");
     }
     parsed = true;
     // 解析根标签configuration
     parseConfiguration(parser.evalNode("/configuration"));
     // 返回configuration,用于构建SqlSessionFactory:SqlSessionFactory只有一个属性就是它
     return configuration;
   }
   ```

3. /configuration标签解析

   ```java
   // 对各个节点进行解析
   private void parseConfiguration(XNode root) {
     try { 
       propertiesElement(root.evalNode("properties"));
       Properties settings = settingsAsProperties(root.evalNode("settings"));
       loadCustomVfs(settings);
       typeAliasesElement(root.evalNode("typeAliases"));
       pluginElement(root.evalNode("plugins"));
       objectFactoryElement(root.evalNode("objectFactory"));
       objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
       reflectorFactoryElement(root.evalNode("reflectorFactory"));
       settingsElement(settings);
       // 重点讲解解析环境
       environmentsElement(root.evalNode("environments"));
       databaseIdProviderElement(root.evalNode("databaseIdProvider"));
       typeHandlerElement(root.evalNode("typeHandlers"));
       mapperElement(root.evalNode("mappers"));
     } catch (Exception e) {
       throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
     }
   }
   ```

4. 介绍XNode类设计

   > XNode是节点的封装，里面引用jdk中的node节点，对node节点所有功能进行封装，是node的适配器，已经把node节点的一些值放入到Xnode,比如attributes。
   >
   > 1. 为什么不把所有的值都放入到Xnode？这样就不用在有它的引用了
   >
   >    node的属性比较多，最主要涉及到子节点的获取，子节点可能很多，这个地方用了懒加载。

   ```java
   private final Node node;
   private final String name;
   private final String body;
   private final Properties attributes;
   private final Properties variables;
   private final XPathParser xpathParser;
   
   public XNode(XPathParser xpathParser, Node node, Properties variables) {
     this.xpathParser = xpathParser;
     this.node = node;
     this.name = node.getNodeName();
     this.variables = variables;
     this.attributes = parseAttributes(node);
     this.body = parseBody(node);
   }
   ```

5. environments标签解析

   ```java
   private void environmentsElement(XNode context) throws Exception {
     if (context != null) {
       if (environment == null) {
         environment = context.getStringAttribute("default");
       }
       // 查询所有的子标签environment
       for (XNode child : context.getChildren()) {
         String id = child.getStringAttribute("id");
         if (isSpecifiedEnvironment(id)) {
           // 事务解析
           TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
           // 数据源解析，重点讲解
           DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
           DataSource dataSource = dsFactory.getDataSource();
           // 赋值
           Environment.Builder environmentBuilder = new Environment.Builder(id)
               .transactionFactory(txFactory)
               .dataSource(dataSource);
           // 赋值
           configuration.setEnvironment(environmentBuilder.build());
         }
       }
     }
   }
   ```

   6. DataSourceFactory解析

      ```java
      private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
          String type = context.getStringAttribute("type");
          // 子标签：<property name="driver" value="com.mysql.jdbc.Driver"/>
          // 子标签都是属性，所以直接转换为Properties
          Properties props = context.getChildrenAsProperties();
          // 根据类型返回从Map对象拿到class文件然后创建
          DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
          // 像SqlSessionFactory工厂一样，也是需要“零件”，才能生产“产品”
          // 这里的材料为了适配所有的标签工厂模式，其他比如TransactionFactory，所以用了Properties
          factory.setProperties(props);
          return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
      }
      ```

7. ```java
   new DefaultSqlSessionFactory(config);// DefaultSqlSessionFactory属性只有一个config
   ```

