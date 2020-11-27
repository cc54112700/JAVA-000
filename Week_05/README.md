学习笔记

作业
1.（必做）写代码实现 Spring Bean 的装配，方式越多越好（XML、Annotation 都可以）, 提交到 Github。

作业地址在Week_05/src/main/java/io/cc54112700/springbean下

1).通过annotation方式

2).通过xml方式

3).通过beanfactory方式

4).通过factorybean方式

5).通过register方式

6).通过serviceloader方式



作业2.（必做）给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。

作业地址在Week_05/src/main/java/io/cc54112700/springboot下

*注意初始化bean的时候 给student klass school起别名
*注意打包 不要把BOOT-INF打进去 不然依赖自定义starter的项目 会路径异常
*解决办法: 
a.<plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
          <skip>true</skip>
      </configuration>
  </plugin>
  
b.修改打包插件，不使用spring-boot-maven-plugin插件打包，因为这个打包插件会把jar打包在Boot-INF目录下导致别的项目引用时引用不到，此时改用maven-war-plugin打包，具体配置如下
<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.0</version>
				<configuration>
					<source>8</source>
					<target>8</target>
				</configuration>
			</plugin>
			
			
作业3.（必做）研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法：
    1）使用 JDBC 原生接口，实现数据库的增删改查操作。
    2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。
    3）配置 Hikari 连接池，改进上述操作。提交代码到 Github。
    
    