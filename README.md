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

   2. PreparedStatement：因为已经预设了执行语句，所以执行的时候不需要带sql语句参数，但是需要对语句占位符赋值

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

   mybatis实现模拟：要被代理是不能创建实例的

   ```java
   package com.dzq;
   public interface TransactionMapper {
       String getFlowKey(long id);
   }
   ```

   ```java
   // mybatis 代理工厂源码
   public class MapperProxyFactory<T> {
   
     private final Class<T> mapperInterface;
     private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();
     // 构造器直接接收class:Class.forName("com.dzq.mapper.TransactionMapper") 获得
     public MapperProxyFactory(Class<T> mapperInterface) {
       this.mapperInterface = mapperInterface;
     }
   
     public Class<T> getMapperInterface() {
       return mapperInterface;
     }
   
     public Map<Method, MapperMethod> getMethodCache() {
       return methodCache;
     }
     // 创建代理
     @SuppressWarnings("unchecked")
     protected T newInstance(MapperProxy<T> mapperProxy) {
       return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
     }
     // 创建mapperProxy，有sqlSession引用，只要拿到sql就可以执行
     public T newInstance(SqlSession sqlSession) {
       final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
       return newInstance(mapperProxy);
     }
   
   }
   ```

   ```xml
   <mapper namespace="com.dzq.mapper.TransactionMapper">
       <select id="getFlowKey" resultType="string">
           select flow_key from transaction where id = #{id}
       </select>
   </mapper>
   ```

   ```java
   // namespace,id的获取
   Class<?>[] classes = proxy.getClass().getInterfaces();
   for (int i = 0; i < classes.length; i++) {
       // com.dzq.mapper.TransactionMapper
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
    String flowKey = sqlSession.selectOne("com.dzq.mapper.TransactionMapper.getFlowKey", 10);
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

<mapper namespace="com.dzq.mapper.TransactionMapper">
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

​    mybatis选择的配置模式是使用的xml文件，xml可以很好的使用标签对应类，也可以很好的描述类之间的关系和依赖。

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

 7. 创建DefaultSqlSessionFactory

    ```java
    new DefaultSqlSessionFactory(config);// DefaultSqlSessionFactory属性只有一个config
    ```

#### 2. 拼接sql

> mybatis是要写全部sql语句的，只是变量是占位符的，所有的sql都在配置文件中，所以在解析配置文件时就已经解析完成。再看解析文件。

```java
// 解析mappers标签,原配置文件，引用了一个mapper的xml文件，再次跳转到解析xml文件
mapperElement(root.evalNode("mappers"));
```

解析mapper.xml文件

```java
// org.apache.ibatis.builder.xml.XMLMapperBuilder
public void parse() {
  if (!configuration.isResourceLoaded(resource)) {
    // 解析mapper标签，比较关键
    // 直接退转到解析标签org.apache.ibatis.builder.xml.XMLStatementBuilder#parseStatementNode,解析的属性非常多但比较简单，只知道位置就可以
    // 最终封装在config.mappedStatements中,key：id,value:MappedStatement具体参数可以直接看此类
    configurationElement(parser.evalNode("/mapper"));
    configuration.addLoadedResource(resource);
    // 解析Mapper.java接口
    // 最终封装在config.knownMappers中，key:type(com.dzq.mapper.TransactionMapper，Class类型)，value：MapperProxyFactory
    bindMapperForNamespace();
  }

  parsePendingResultMaps();
  parsePendingCacheRefs();
  parsePendingStatements();
}
```

#### 3. 加载数据源，获取Connection

```java
// 执行类型，来源于config,事务级别,是否自动提交
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
  Transaction tx = null;
  try {
    // 得到环境
    final Environment environment = configuration.getEnvironment();
    // 事务工厂
    final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
    // 放入dataSource，dataSource是可以得到Connection
    tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
    // 执行器，里面有tx,而且是根据configuration调用，那么能够拿到其他配置
    final Executor executor = configuration.newExecutor(tx, execType);
    // 创建session ，配置，执行器，是否自动执行
    // config哪里都有
    return new DefaultSqlSession(configuration, executor, autoCommit);
  } catch (Exception e) {
    closeTransaction(tx); // may have fetched a connection so lets call close()
    throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
  } finally {
    ErrorContext.instance().reset();
  }
}
```

#### 4. 获取Statement

> 执行的开始，在获取到SqlSession之后，就可以进行执行，SqlSession是执行的入口，先看看此接口

```java
// 查询
<T> T selectOne(String statement);
<E> List<E> selectList(String statement);
// 修改
int update(String statement);
// 插入
int insert(String statement);
// 删除
int delete(String statement);
```

先查看selectList源码，试想一下过程：根据statement得到sql语句返回值和参数，根据executor执行selectList语句，得到结果，根据返回值封装返回。

关键的2个类MappedStatement，Executor

```java
// SqlSession类主要属性和方法
// 主要属性，configuration，executor，autoCommit是构造时创建的，dirty，cursorList是执行是的中间状态
// 配置的引用，能够拿到所有配置
private final Configuration configuration;
// 执行器，真实执行的类
private final Executor executor;
// 是否自动提交
private final boolean autoCommit;
// 有脏数据
private boolean dirty;
// 存储所有游标信息，当调用public <T> Cursor<T> selectCursor(String statement)，会把所有的返回值存储
private List<Cursor<?>> cursorList;
@Override
public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
  try {
    // 从config中得到MappedStatement，通过config解析得来，MappedStatement是解析配置的时候赋值的
    MappedStatement ms = configuration.getMappedStatement(statement);
    // 执行，再看执行方法
    return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
  } catch (Exception e) {
    throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
  } finally {
    ErrorContext.instance().reset();
  }
}
```

```java
// Executor关键属性和方法
// 封装执行器，自带的适配器模式
protected Executor delegate;
// 事务，里面有dataSource
protected Transaction transaction;
// 配置
protected Configuration configuration;
// 查询，ms:mapper属性，parameter：参数，rowBounds:分页，ResultHandler:结果处理
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
   // 根据参数拼接组装sql,简简单单的一个sql也是需要封装的，看看如何封装的
   // BoundSql关键属性，sql：sql语句，带通配符， parameterMappings：参数，id,类型，parameterObject：参数值
   BoundSql boundSql = ms.getBoundSql(parameter);
   CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
   return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
}
```

```java
@Override
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
      throws SQLException {
    Cache cache = ms.getCache();
    // cache为空，直接看delegate的query方法，还是一种适配模式delegate也是一种Executor
    if (cache != null) {
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) {
        ensureNoOutParams(ms, boundSql);
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          list = delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
          tcm.putObject(cache, key, list); // issue #578 and #116
        }
        return list;
      }
    }
    // delegate真正的executor执行
    return delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```

```java
// SimpleExecutor
@Override
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
  Statement stmt = null;
  try {
    // 得到config
    Configuration configuration = ms.getConfiguration();
    // 得到statementHandler,有config创建statementHandler,同样也是适配器模式
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
    /** configuration.newStatementHandler 方法介绍 start
    //StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);以下是RoutingStatementHandler关键属性和构造方法，真实调用使用delegate调用
    //private final StatementHandler delegate;
    //public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    // switch (ms.getStatementType()) {
    //  case STATEMENT:
    //    delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    //    break;
    //  case PREPARED:
    //    delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    //    break;
    //  case CALLABLE:
    //    delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    //    break;
    //  default:
    //    throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
    // }

    //}
    configuration.newStatementHandler 方法介绍 end
    **/
    //生成jdbc的statement
    stmt = prepareStatement(handler, ms.getStatementLog());
    /**prepareStatement 内部方法
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
      Statement stmt;
      // 生成connection
      Connection connection = getConnection(statementLog);
      // prepare 模板模式，下面代码介绍
      stmt = handler.prepare(connection, transaction.getTimeout());
      // 赋值参数，不同statement不同的效果
      handler.parameterize(stmt);
      return stmt;
    }
    **/
    // statement 执行query
    // statementHandler查询，处理stmt
    return handler.<E>query(stmt, resultHandler);
  } finally {
    // 关闭stmt
    closeStatement(stmt);
  }
}
```

org.apache.ibatis.executor.statement.BaseStatementHandler#prepare模板方法

```java
public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
  ErrorContext.instance().sql(boundSql.getSql());
  Statement statement = null;
  try {
    // 生成statement,核心二种：PreparedStatementHandler，SimpleStatementHandler，instantiateStatement抽象方法
    statement = instantiateStatement(connection);
    /**PreparedStatementHandler的instantiateStatement方法
    protected Statement instantiateStatement(Connection connection) throws SQLException {
      String sql = boundSql.getSql();
      // 没有设计模式，根据不同的情况调用不同的方法生成statement.
      if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
        String[] keyColumnNames = mappedStatement.getKeyColumns();
        if (keyColumnNames == null) {
          return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
          return connection.prepareStatement(sql, keyColumnNames);
        }
      } else if (mappedStatement.getResultSetType() != null) {
        return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
      } else {
        return connection.prepareStatement(sql);
      }
    }
    **/
    // 设置超时时间
    setStatementTimeout(statement, transactionTimeout);
    // 设置每次获取个数
    setFetchSize(statement);
    return statement;
  } catch (SQLException e) {
    closeStatement(statement);
    throw e;
  } catch (Exception e) {
    closeStatement(statement);
    throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
  }
}
```

5. ####  执行获取结果handler.<E>query(stmt, resultHandler);

   ```java
   // org.apache.ibatis.executor.statement.PreparedStatementHandler查询结果
   public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
     PreparedStatement ps = (PreparedStatement) statement;
     // 执行，下一步对结果进行处理
     ps.execute();
     // resultSetHandler 是PreparedStatementHandler其中一个属性
     return resultSetHandler.<E> handleResultSets(ps);
   }
   ```

####     6. 结果转换resultSetHandler.<E> handleResultSets(ps);

> ​	ResultSetHandler类创建，是通过创建StatementHandler时创建的resultSetHandler的属性
>
> ```java
> configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
> ```

org.apache.ibatis.executor.resultset.DefaultResultSetHandler#handleResultSets

```java
@Override
public List<Object> handleResultSets(Statement stmt) throws SQLException {
  ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

  final List<Object> multipleResults = new ArrayList<Object>();

  int resultSetCount = 0;
  // 获取ResultSetWrapper
  // ResultSetWrapper设计：首先rs还有其他一些属性，如果只是持有rs的引用，调用其他方法时，需要一些其他通过rs获取的属性就还要重复获取，那么在得到rs的时候就可以直接转换成ResultSetWrapper的属性,以下是对rs的封装
  //public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
  //  super();
  //  其他属性的封装，typeHandlerRegistry,还有通过rs转换的一些属性
  //  this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
  //  this.resultSet = rs;
  //  final ResultSetMetaData metaData = rs.getMetaData();
  //  final int columnCount = metaData.getColumnCount();
  //  for (int i = 1; i <= columnCount; i++) {
  //    返回的列名
  //    columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
  //    返回的jdbc类型：比如varchar
  //    jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
  //    返回的java类型：比如java.lang.String
  //    classNames.add(metaData.getColumnClassName(i));
  //  }
  //}
  // 拿出第一个rs
  ResultSetWrapper rsw = getFirstResultSet(stmt);
  // 根据配置文件记录的返回类型
  // 配置文件对应的resultType,resultMap
  List<ResultMap> resultMaps = mappedStatement.getResultMaps();
  int resultMapCount = resultMaps.size();
  validateResultMapsCount(rsw, resultMapCount);
  while (rsw != null && resultMapCount > resultSetCount) {
    // 取出resultMap，把对应的result的值放入到multipleResults中
    ResultMap resultMap = resultMaps.get(resultSetCount);
    // 放入结构到multipleResults中
    handleResultSet(rsw, resultMap, multipleResults, null);
    // 得到下一个结果
    rsw = getNextResultSet(stmt);
    cleanUpAfterHandlingResultSet();
    resultSetCount++;
  }

  String[] resultSets = mappedStatement.getResultSets();
  if (resultSets != null) {
    while (rsw != null && resultSetCount < resultSets.length) {
      ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
      if (parentMapping != null) {
        String nestedResultMapId = parentMapping.getNestedResultMapId();
        ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
        handleResultSet(rsw, resultMap, null, parentMapping);
      }
      rsw = getNextResultSet(stmt);
      cleanUpAfterHandlingResultSet();
      resultSetCount++;
    }
  }
  // 返回单个结果
  // multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults
  return collapseSingleResultList(multipleResults);
}
```

处理handleResultSet结果

org.apache.ibatis.executor.resultset.DefaultResultSetHandler#handleResultSet

```java
private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
  try {
    if (parentMapping != null) {
      handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
    } else {
      if (resultHandler == null) {// 默认为空
        // 得到默认的接口处理器
        DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
        // 处理结果
        handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
        /**
        调用方法：handleRowValuesForSimpleResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
        private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?>           resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
          DefaultResultContext<Object> resultContext = new DefaultResultContext<Object>();
          // skip条数
          skipRows(rsw.getResultSet(), rowBounds);
          // 判断是否停止和不能超过配置的条数
          while (shouldProcessMoreRows(resultContext, rowBounds) && rsw.getResultSet().next()) {
            // 因为resultMap.getDiscriminator()为空，则直接返回resultMap自己
            ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(), resultMap, null);
              // 解析数据库的值
              Object rowValue = getRowValue(rsw, discriminatedResultMap);
                // getRowValue内方法
                Object rowValue = createResultObject(rsw, resultMap, lazyLoader, null);
                  // createResultObject内方法
                  Object resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
                    // 创建原始类型的结果,判断返回的类型在基本数据类型中
                    return createPrimitiveResultObject(rsw, resultMap, columnPrefix)
                    //2021-10-25 补充实体类获取resultObject方法，在下一个代码中讲解如何赋值
                    return objectFactory.create(resultType);// objectFactory的作用创建实例对账，返回后在赋值
                      // 根据resultType得到类型处理类，从rsw中得到，可以进行一个缓存
                      final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
                      // 对应的类型处理
                      return typeHandler.getResult(rsw.getResultSet(), columnName);
              // 把字段值放入到resultContext中，同时也放入到resultHandler中
              storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
          }
        }
        **/
        // 取出结果放入到multipleResults结果集中
        multipleResults.add(defaultResultHandler.getResultList());
      } else {
        handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
      }
    }
  } finally {
    // issue #228 (close resultsets)
    closeResultSet(rsw.getResultSet());
  }
}
```

**2021-10-25 增加对非原始类进行赋值内容**

```java
// 得到实例
Object rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix);
// 继续创建createResultObject
// Object resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
// objectFactory.create(resultType);通过objectFactory根据反射创建实例
// 如果存在非默认构造器
// createByConstructorSignature(rsw, resultType, constructorArgTypes, constructorArgs);
// final Constructor<?> defaultConstructor = findDefaultConstructor(constructors);
// 继续调用createUsingConstructor(rsw, resultType, constructorArgTypes, constructorArgs, defaultConstructor);
/**
private Object createUsingConstructor(ResultSetWrapper rsw, Class<?> resultType, List<Class<?>> constructorArgTypes, 			List<Object> constructorArgs, Constructor<?> constructor) throws SQLException {
    boolean foundValues = false;
    // 循环参数列表，根据下标获取到rsw中的列名
    for (int i = 0; i < constructor.getParameterTypes().length; i++) {
      Class<?> parameterType = constructor.getParameterTypes()[i];
      String columnName = rsw.getColumnNames().get(i);
      TypeHandler<?> typeHandler = rsw.getTypeHandler(parameterType, columnName);
      // 根据列名取值
      Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
      constructorArgTypes.add(parameterType);
      // 放入到构造参数数组中
      constructorArgs.add(value);
      foundValues = value != null || foundValues;
    }
    return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
}
**/
// 得到实体之后，判断不是基本数据类型，进入判断语句再次赋值
if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
    // 创建元数据对象，
    final MetaObject metaObject = configuration.newMetaObject(rowValue);
    // 创建MetaObject
    /**
    new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    //不是Array也不是List,所以直接创建BeanWrapper，主要包含这个Class内部get,set方法信息
    this.objectWrapper = new BeanWrapper(this, object);
    **/
    boolean foundValues = this.useConstructorMappings;
    if (shouldApplyAutomaticMappings(resultMap, false)) {
        foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
    }
    // 对实例赋值
    foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) || foundValues;
    foundValues = lazyLoader.size() > 0 || foundValues;
    rowValue = foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
}
```



![序列图](https://raw.githubusercontent.com/dzhiqiang/PicGo-gallery/main/%E6%89%A7%E8%A1%8Csql%E8%BF%87%E7%A8%8B-%E5%BA%8F%E5%88%97.png)

#### 7. 关闭

Statement关闭：可以看到生成是从Executor生成的，statement的处理都在其他类中处理，所以也在此类中关闭

```java
// SimpleExecutor
@Override
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
  // 生成
  Statement stmt = null;
  try {
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
    stmt = prepareStatement(handler, ms.getStatementLog());
    return handler.<E>query(stmt, resultHandler);
  } finally {
    // 关闭
    closeStatement(stmt);
  }
}
```

ResultSet关闭：ResultSet是从org.apache.ibatis.executor.resultset.DefaultResultSetHandler#handleResultSets得到封装到ResultSetWrapper，取值是在handleResultSet，然后就取下一个ResultSet了，则在handleResultSet中close

```java
// 已经封装到ResultSetWrapper中
private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
  try {
    if (parentMapping != null) {
      handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
    } else {
      if (resultHandler == null) {
        DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
        handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
        multipleResults.add(defaultResultHandler.getResultList());
      } else {
        handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
      }
    }
  } finally {
    // 取值完后关闭
    closeResultSet(rsw.getResultSet());
  }
}
```

Connection关闭：是在创建SqlSession时，再创建executor时提前创建Transaction，所以间接由SqlSession保管，由SqlSession关闭。

Statement和ResultSet是每次执行的时候产生的，每次都需要关闭，Connection是链接，所以不在使用的时候可以关闭。

#### 8. 异常处理

> 加载文件：需要处理异常IOException:IO异常是mybatis的提前准备的异常，还没真正到链接数据库的操作。文件异常处理不了。需要在编写时考虑。
>
> 开始执行：不需要处理异常，直接进行执行，执行时查错。需重点分析。JDBC操作关键异常SQLException,是需要捕获的异常，如何处理的？
>
> 开始执行时有2步进程，1：前期准备工作，mybatis自定义异常，2：涉及到JDBC，SQLException

前期准备的异常都是RuntimeException，不需要捕获，出错时处理，比如

```java
@Override
public <T> T selectOne(String statement, Object parameter) {
  // Popular vote was to return null on 0 results and throw exception on too many.
  List<T> list = this.<T>selectList(statement, parameter);
  if (list.size() == 1) {
    return list.get(0);
  } else if (list.size() > 1) {
    // 查询数据过多时的异常处理
    throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
  } else {
    return null;
  }
}
```

涉及到JDBC的异常全部没有处理，而是在最外层捕获了异常

```java
// 开始涉及到jdbc的异常，全部抛出
@Override
 public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
   BoundSql boundSql = ms.getBoundSql(parameter);
   CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
   return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
}
```

```java
@Override
public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
  try {
    MappedStatement ms = configuration.getMappedStatement(statement);
    // 调用上方代码
    return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
  } catch (Exception e) {
    // 捕获所有异常，进行封装,返回自定义异常
    /**
    public static RuntimeException wrapException(String message, Exception e) {
    	return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
    }
    **/
    throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
  } finally {
    ErrorContext.instance().reset();
  }
}
```

对于空指针的校验，尽量封装到方法中，比如上面代码

MappedStatement ms = configuration.getMappedStatement(statement); // 是从配置中拿到MappedStatement没有校验为空，如果为空则NullPointerException,那校验在什么地方呢？查看configuration.getMappedStatement(statement);

想到另外一个问题**数据结构的一定要合理**

调用代码

```java
configuration.getMappedStatement(statement);
////进入
this.getMappedStatement(id, true);
////进入
//从configuration属性mappedStatements拿到值，还没校验异常
//protected final Map<String, MappedStatement> mappedStatements = new StrictMap<MappedStatement>("Mapped Statements collection");
return mappedStatements.get(id);
//StrictMap类获取，重写了get方法，在最底层抛出异常，而不是在业务层
public V get(Object key) {
   V value = super.get(key);
   if (value == null) {
      // 如果为空抛出异常，而且异常信息提示也很精准。name是StrictMap属性，为map命名。
      throw new IllegalArgumentException(name + " does not contain value for " + key);
   }
   if (value instanceof Ambiguity) {
      throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
            + " (try using the full name including the namespace, or rename one of the entries)");
   }
   return value;
}
```

自己写的例子

```java
public class Student {
    private String name;
    // 学生有多地址
    private List<Address> addressList;
    public List<Address> getAddressList() {
        if (addressList == null || addressList.size() == 0) {
            throw new RuntimeException(name + " 没有填写家庭地址");
        }
        return addressList;
    }
    public static void main(String[] args) {
        Student student = new Student();
        // 不影响业务代码美观
        List<Address> addressList = student.getAddressList();
        //TODO
    }
}
```

### Mapper.class接口调用解读

```java
public static void select() throws IOException {
    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory =
            new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    // 根据类型得到接口，接口直接调用
    TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);
    // 代理类调用，得到返回值与sqlSession对比
    // String flowKey = sqlSession.selectOne("com.dzq.mapper.TransactionMapper.getFlowKey", 10);
    // SqlSession调用知道namespace和参数，那么接口调用如何知道呢，通过动态代理的object和method方法拼接，在根据方法对应的标签找到是insert还是update
    // 然后再得到参数
    String flowKey = transactionMapper.getFlowKey(10);
    System.out.println(flowKey);
}
```

上面已经讲到InvoceHandler接口的方法，那什么时候加入到配置中的呢

org.apache.ibatis.builder.xml.XMLMapperBuilder#parse

```java
public void parse() {
  if (!configuration.isResourceLoaded(resource)) {
    // 解析mapper文件
    configurationElement(parser.evalNode("/mapper"));
    configuration.addLoadedResource(resource);
    // 找到对应的namespace的接口进行解析
    bindMapperForNamespace();
  }

  parsePendingResultMaps();
  parsePendingCacheRefs();
  parsePendingStatements();
}
```

```java
private void bindMapperForNamespace() {
  // 得到namespace
  String namespace = builderAssistant.getCurrentNamespace();
  if (namespace != null) {
    Class<?> boundType = null;
    try {
      // 转换到class类
      boundType = Resources.classForName(namespace);
    } catch (ClassNotFoundException e) {
      //ignore, bound type is not required
    }
    if (boundType != null) {
      if (!configuration.hasMapper(boundType)) {
        // Spring may not know the real resource name so we set a flag
        // to prevent loading again this resource from the mapper interface
        // look at MapperAnnotationBuilder#loadXmlResource
        configuration.addLoadedResource("namespace:" + namespace);
        // 加入到config
        configuration.addMapper(boundType);
      }
    }
  }
}
```

```java
public <T> void addMapper(Class<T> type) {
  // 校验工作
  if (type.isInterface()) {
    if (hasMapper(type)) {
      throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
    }
    boolean loadCompleted = false;
    try {
      // 放入到mappers,new MapperProxyFactory<T>(type)创建动态代理工厂，如上面复习的一样
      knownMappers.put(type, new MapperProxyFactory<T>(type));
      MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
      parser.parse();
      loadCompleted = true;
    } finally {
      if (!loadCompleted) {
        knownMappers.remove(type);
      }
    }
  }
}
```

下面讲如何获取

```java
TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);
```

```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
  // 得到对应的动态代理工厂
  final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
  if (mapperProxyFactory == null) {
    throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
  }
  try {
    // 创建代理，如最开始的复习，执行主要看MapperProxy的invoke方法
    return mapperProxyFactory.newInstance(sqlSession);
  } catch (Exception e) {
    throw new BindingException("Error getting mapper instance. Cause: " + e, e);
  }
}
```

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
  // 调用了Object方法和接口的默认方法，不是重点
  try {
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    } else if (isDefaultMethod(method)) {
      return invokeDefaultMethod(proxy, method, args);
    }
  } catch (Throwable t) {
    throw ExceptionUtil.unwrapThrowable(t);
  }
  // 根据方法生成MapperMethod方法，后面比较简单，就是创建MapperMethod
  final MapperMethod mapperMethod = cachedMapperMethod(method);
  return mapperMethod.execute(sqlSession, args);
}
```

```java
mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
```

MapperMethod关键属性和方法

```
// command：sql命令，记录着命令类型：insert，update等，还有namespace
// method：记录返回值，参数等
private final SqlCommand command;
private final MethodSignature method;
```

```java
// 执行，在创建之后就执行了
public Object execute(SqlSession sqlSession, Object[] args) {
  Object result;
  // 根据不同的类型调用不同的sqlSession方法
  switch (command.getType()) {
    case INSERT: {
    //根据method组装参数，因为sqlSession只有一个参数
    //command.getName()=namespace
    Object param = method.convertArgsToSqlCommandParam(args);
      // rowCountResult 根据sqlSession执行的结果转换返回值，返回值类型在method记录
      result = rowCountResult(sqlSession.insert(command.getName(), param));
      break;
    }
    case UPDATE: {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.update(command.getName(), param));
      break;
    }
    case DELETE: {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.delete(command.getName(), param));
      break;
    }
    case SELECT:
      if (method.returnsVoid() && method.hasResultHandler()) {
        executeWithResultHandler(sqlSession, args);
        result = null;
      } else if (method.returnsMany()) {
        result = executeForMany(sqlSession, args);
      } else if (method.returnsMap()) {
        result = executeForMap(sqlSession, args);
      } else if (method.returnsCursor()) {
        result = executeForCursor(sqlSession, args);
      } else {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = sqlSession.selectOne(command.getName(), param);
      }
      break;
    case FLUSH:
      result = sqlSession.flushStatements();
      break;
    default:
      throw new BindingException("Unknown execution method for: " + command.getName());
  }
  if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
    throw new BindingException("Mapper method '" + command.getName() 
        + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
  }
  return result;
}
```

### 与Spring结合

> 为了能够结合Spring，mybatis特意扩展开发mybatis-spring.
>
> mybatis能够实现SqlSession获取到Mapper类，那么就看看如何注入到springbean中。

##### spring复习

1. spring bean存在的2种形式：普通的bean和工厂bean

**普通的bean**:直接获取对应类的示例，比如以下，dataSource直接获取DriverManagerDataSource属性

```xml
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"></property>
    <property name="url" value="jdbc:mysql://10.9.224.45:3306/activiti?useUnicode=true&amp;characterEncoding=utf8&amp;serverTimezone=UTC&amp;useSSL=false"></property>
    <property name="username" value="root"></property>
    <property name="password" value="root"></property>
</bean>
```

**工厂bean**：获取对应bean的工厂，通过getObject()获取，比如以下，sqlSessionFactory获取SqlSessionFactoryBean通过getObject()获得。

```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource" />
    <property name="mapperLocations" value="classpath:mapper/TransactionMapper.xml" />
</bean>
```

为什么需要FactoryBean，有一些配置不仅仅是一个属性那么简单，需要大量的解析操作（代码量），通过上面的源码知道SqlSessionFactory是持有Config的，config是通过解析文件来获得的。那么什么时候解析呢？通常和InitializingBean配合，实现afterPropertiesSet，当属性赋值完成后，可以根据属性值完成解析工作。解析完成后创建SqlSessionFactory，然后通过FactoryBean接口就可以获取了。

```java
// 属性赋值完成后，通过属性进行下一步操作
@Override
public void afterPropertiesSet() throws Exception {
  notNull(dataSource, "Property 'dataSource' is required");
  notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
  state((configuration == null && configLocation == null) || !(configuration != null && configLocation != null),
      "Property 'configuration' and 'configLocation' can not specified with together");
  // 构建sqlSessionFactory，buildSqlSessionFactory不在介绍
  this.sqlSessionFactory = buildSqlSessionFactory();
}
```

```java
// 通过上面的构建，这里就直接获取了
@Override
public SqlSessionFactory getObject() throws Exception {
  if (this.sqlSessionFactory == null) {
    afterPropertiesSet();
  }

  return this.sqlSessionFactory;
}
```

2.  spring对BeanDefinition处理扩展，继承BeanDefinitionRegistryPostProcessor，实现postProcessBeanDefinitionRegistry

> BeanDefinition是对bean注解的解析，还没有生成bean对象，可以通过postProcessBeanDefinitionRegistry进行处理，获取进行其他处理逻辑，相当于spring的一个监听器，做一些额外的事情。比如：org.mybatis.spring.mapper.MapperScannerConfigurer，其实并不是为了使用这个类对象，只是为了把对应的Mapper接口注册到spring。任何类都可以，只要符合业务逻辑的类名即可。

> 回忆下mybatis，Mapper代理是可以通过SqlSeesion对象通过类获取到的，那么把这个对象注入到bean就可以了，bean的2种形式已经介绍过了，mybatis是通过工厂bean的方式注入的，MapperFactoryBean，那看看源码是如何通过postProcessBeanDefinitionRegistry注入的吧？

```java
@Override
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
  if (this.processPropertyPlaceHolders) {
    processPropertyPlaceHolders();
  }
  // 通过ClassPathMapperScanner扫描包
  ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
  scanner.setAddToConfig(this.addToConfig);
  scanner.setAnnotationClass(this.annotationClass);
  scanner.setMarkerInterface(this.markerInterface);
  scanner.setSqlSessionFactory(this.sqlSessionFactory);
  scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
  scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
  scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
  scanner.setResourceLoader(this.applicationContext);
  scanner.setBeanNameGenerator(this.nameGenerator);
  // 赋值MapperFactoryBeanClass类
  //public void setMapperFactoryBeanClass(Class<? extends MapperFactoryBean> mapperFactoryBeanClass) {
  //  赋值的时候为空，默认值MapperFactoryBean
  //  this.mapperFactoryBeanClass = mapperFactoryBeanClass == null ? MapperFactoryBean.class : mapperFactoryBeanClass;
  //}
  scanner.setMapperFactoryBeanClass(this.mapperFactoryBeanClass);
  if (StringUtils.hasText(lazyInitialization)) {
    scanner.setLazyInitialization(Boolean.valueOf(lazyInitialization));
  }
  if (StringUtils.hasText(defaultScope)) {
    scanner.setDefaultScope(defaultScope);
  }
  scanner.registerFilters();
  // 开始解析，那么就看看生成什么样子的BeanDefinition
  scanner.scan(
      StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
}
```

开始解析

```java
// 开始解析
public int scan(String... basePackages) {
   int beanCountAtScanStart = this.registry.getBeanDefinitionCount();
   // ClassPathMapperScanner重新了这个方法，一定要看重写的方法
   doScan(basePackages);

   // Register annotation config processors, if necessary.
   if (this.includeAnnotationConfig) {
      AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
   }

   return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
}
```

子类重写的方法

```java
@Override
public Set<BeanDefinitionHolder> doScan(String... basePackages) {
  // 还是调用父类获取到解析的beanDefinitions
  Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

  if (beanDefinitions.isEmpty()) {
    LOGGER.warn(() -> "No MyBatis mapper was found in '" + Arrays.toString(basePackages)
        + "' package. Please check your configuration.");
  } else {
    // 开始操作BeanDefinition
    processBeanDefinitions(beanDefinitions);
  }

  return beanDefinitions;
}
```

对BeanDefinition进行二次加工

```java
private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
  AbstractBeanDefinition definition;
  BeanDefinitionRegistry registry = getRegistry();
  for (BeanDefinitionHolder holder : beanDefinitions) {
    definition = (AbstractBeanDefinition) holder.getBeanDefinition();
    boolean scopedProxy = false;
    if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
      definition = (AbstractBeanDefinition) Optional
          .ofNullable(((RootBeanDefinition) definition).getDecoratedDefinition())
          .map(BeanDefinitionHolder::getBeanDefinition).orElseThrow(() -> new IllegalStateException(
              "The target bean definition of scoped proxy bean not found. Root bean definition[" + holder + "]"));
      scopedProxy = true;
    }
    String beanClassName = definition.getBeanClassName();
    LOGGER.debug(() -> "Creating MapperFactoryBean with name '" + holder.getBeanName() + "' and '" + beanClassName
        + "' mapperInterface");

    // MapperFactoryBean构造函数唯一的参数就是Mapper本身
    definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
    // BeanClass替换成mapperFactoryBeanClass：MapperFactoryBean
    definition.setBeanClass(this.mapperFactoryBeanClass);

    definition.getPropertyValues().add("addToConfig", this.addToConfig);

    // Attribute for MockitoPostProcessor
    // https://github.com/mybatis/spring-boot-starter/issues/475
    definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClassName);

    boolean explicitFactoryUsed = false;
    if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
      definition.getPropertyValues().add("sqlSessionFactory",
          new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
      explicitFactoryUsed = true;
    } else if (this.sqlSessionFactory != null) {
      definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
      explicitFactoryUsed = true;
    }

    if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
      if (explicitFactoryUsed) {
        LOGGER.warn(
            () -> "Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
      }
      definition.getPropertyValues().add("sqlSessionTemplate",
          new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
      explicitFactoryUsed = true;
    } else if (this.sqlSessionTemplate != null) {
      if (explicitFactoryUsed) {
        LOGGER.warn(
            () -> "Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
      }
      definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
      explicitFactoryUsed = true;
    }
	// 上面的代码把属性值放入到bean定义中，通过byType方式赋值属性
    // public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    //   if (this.sqlSessionTemplate == null || sqlSessionFactory != this.sqlSessionTemplate.getSqlSessionFactory()) {
    //     this.sqlSessionTemplate = createSqlSessionTemplate(sqlSessionFactory);
    //   }
    // }
    if (!explicitFactoryUsed) {
      LOGGER.debug(() -> "Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
      definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
    }

    definition.setLazyInit(lazyInitialization);

    if (scopedProxy) {
      continue;
    }

    if (ConfigurableBeanFactory.SCOPE_SINGLETON.equals(definition.getScope()) && defaultScope != null) {
      definition.setScope(defaultScope);
    }

    if (!definition.isSingleton()) {
      BeanDefinitionHolder proxyHolder = ScopedProxyUtils.createScopedProxy(holder, registry, true);
      if (registry.containsBeanDefinition(proxyHolder.getBeanName())) {
        registry.removeBeanDefinition(proxyHolder.getBeanName());
      }
      registry.registerBeanDefinition(proxyHolder.getBeanName(), proxyHolder.getBeanDefinition());
    }

  }
}
```

至此Mybatis如何与spring结合讲解完成。
