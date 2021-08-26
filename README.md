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
     // 构造器直接接收class:Class.forName("com.dzq.TransactionMapper") 获得
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

   6.  DataSourceFactory解析

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
    // 直接退转到解析标签org.apache.ibatis.builder.xml.XMLStatementBuilder#parseStatementNode,解析的属性非常多但比较简单，只知道位置就可以用到在介绍
    // 最终封装在config.mappedStatements中,key：id,value:MappedStatement具体参数可以直接看此类
    configurationElement(parser.evalNode("/mapper"));
    configuration.addLoadedResource(resource);
    // 解析Mapper.java接口
    // 最终封装在config.knownMappers中，key:type(com.dzq.TransactionMapper，Class类型)，value：MapperProxyFactory
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
        private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?>                         resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
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
                    // 创建原始类型的结果
                    return createPrimitiveResultObject(rsw, resultMap, columnPrefix)
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

![序列图](https://raw.githubusercontent.com/dzhiqiang/PicGo-gallery/main/%E6%89%A7%E8%A1%8Csql%E8%BF%87%E7%A8%8B-%E5%BA%8F%E5%88%97.png)
