# dbm
------
基于spring jdbc实现的轻量级orm   

项目github地址：[ dbm ]( https://github.com/wayshall/dbm )



交流群：  604158262



## 目录
- [特色](#特色)
- [示例项目](#示例项目)
- [要求](#要求)
- [maven配置](#maven)
- [一行代码启用](#一行代码启用)
- [实体映射](#实体映射)
- [id策略](#id策略)
- [复合主键映射](#复合主键映射)
- [其它特有的映射](#其它特有的映射)
- [BaseEntityManager接口和QueryDSL](#BaseEntityManager接口和QueryDSL)
- [CrudEntityManager接口](#crudentitymanager接口)
- [DbmRepository动态sql查询接口](#DbmRepository动态sql查询接口)
- [sql片段支持](#sql片段支持)
- [动态sql查询的语法和指令](#动态sql查询的语法和指令)
- [用DbmRepository执行脚本](#用DbmRepository执行脚本)
- [DbmRepository接口的多数据源支持](#dbmrepository接口的多数据源支持)
- [DbmRepository接口对其它orm框架的兼容](#dbmrepository接口对其它orm框架的兼容)
- [查询映射](#查询映射)
- [复杂的嵌套查询映射](#复杂的嵌套查询映射)
- [自定义实现DbmRepository接口](#自定义实现dbmrepository接口)
- [枚举处理](#枚举处理)
- [json映射](#json映射)
- [敏感字段映射](#敏感字段映射)
- [字段绑定](#字段绑定)
- [从其它地方加载DbmRepository接口的sql](#从其它地方加载DbmRepository接口的sql)
- [直接传入要执行的sql作为参数](#直接传入要执行的sql作为参数)
- [其它映射特性](#其它映射特性)
- [批量插入](#批量插入)
- [充血模型支持](#充血模型支持)
- [参数配置](#参数配置)
- [代码生成器](#代码生成器)
- [辅助工具：导出表结构为excel](#辅助工具：导出表结构为excel)
- [捐赠](#捐赠)


## 特色
- 基本的实体增删改查（单表）不需要生成样板代码和sql文件。

- 返回结果不需要手动映射，会根据字段名称自动映射。

- 支持sql语句和接口绑定风格的DAO，但sql不是写在丑陋的xml里，而是直接写在sql文件里，这样用eclipse或者相关支持sql的编辑器打开时，就可以语法高亮，更容易阅读。

- 支持sql脚本修改后重新加载

- 内置支持分页查询。

- 接口支持批量插入

- 使用Java8新增的编译特性，不需要使用类似@Param 的注解标注参数,当然你可以显式使用注解标注参数。

- Repository接口（用注解@DbmRepository标注了的接口）支持默认方法

- 支持多数据源绑定，可以为每个Repository接口指定具体的数据源

- 支持不同的数据库绑定，Repository接口会根据当前绑定的数据源自动绑定加载对应数据库后缀的sql文件

- 提供充血模型支持

- 支持json映射，直接把数据库的json或者varchar类型（存储内容为json数据）的列映射为Java对象

- 支持非int和String类型的枚举映射

- 内置支持SnowFlake id生成算法

- 支持敏感字段映射
- Repository接口支持执行sql脚本

   
## 示例项目   
单独使用dbm的示例项目
[boot-dbm-sample](https://github.com/wayshall/boot-dbm-sample)


## 要求
JDK 1.8+
spring 4.0+

## maven
当前snapshot版本：4.7.4-SNAPSHOT

若使用snapshot版本，请添加snapshotRepository仓储：
```xml
<repository>
     <id>oss</id>
     <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>   
```

添加依赖：   
```xml

<dependency>
    <groupId>org.onetwo4j</groupId>
    <artifactId>onetwo-dbm</artifactId>
    <version>4.8.0-SNAPSHOT</version>
</dependency>

```
spring的依赖请自行添加。

## 一行代码启用
在已配置好数据源的前提下，只需要在spring配置类（即有@Configuration注解的类）上加上注解@EnableDbm即可。
```java     
  
	@EnableDbm
	@Configuration
	public class SpringContextConfig {
	}   
   
```

## 实体映射
```java   
@Entity   
@Table(name="TEST_USER_AUTOID")   
public class UserAutoidEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@Column(name="ID")
	protected Long id;
	@Length(min=1, max=50)
	protected String userName;
	@Length(min=0, max=50)
	@Email
	protected String email;
	protected String mobile;
	protected UserStatus status;

	//省略getter和setter
}   
```
### 注意这里用到了一些jpa的注解，含义和jpa一致：
- @Entity，表示这是一个映射到数据库表的实体
- @Table，表示这个实体映射的表
- @Id，表示这是一个主键字段
- @GeneratedValue(strategy=GenerationType.IDENTITY)，表示这个主键的值用数据库自增的方式生成，dbm目前只支持IDENTITY和SEQUENCE两种方式      
- @Column，表示映射到表的字段，一般用在java的字段名和表的字段名不对应的时候   

java的字段名使用驼峰的命名风格，而数据库使用下划线的风格，dbm会自动做转换   
注意dbm并没有实现jpa规范，只是借用了几个jpa的注解，纯属只是为了方便。。。
后来为了证明我也不是真的很懒，也写了和@Entity、@Table、@Column对应的注解，分别是：@DbmEntity（@Entity和@Table合一），@DbmColumn。。。


- 注意：为了保持简单和轻量级，dbm的实体映射只支持单表，不支持多表级联映射。复杂的查询和映射请使用[DbmRepository接口](#dbmrepository接口)

## id策略
dbm支持jpa的GenerationType的id策略，此外还提供了通过@DbmIdGenerator自定义的策略：
- GenerationType.IDENTITY   
  使用数据库本身的自增策略
- GenerationType.SEQUENCE   
  使用数据库的序列策略（只支持oracle）
- GenerationType.TABLE   
  使用自定义的数据库表管理序列
- GenerationType.AUTO   
  目前的实现是：如果是mysql，则等同于GenerationType.IDENTITY，如果是oracle，则等同于GenerationType.SEQUENCE   
- DbmIdGenerator   
  dbm提供id生成注解，可通过配置 generatorClass 属性，配置自定义的id实现类，实现类必须实现CustomIdGenerator接口。dbm首先会通过尝试在spring context查找generatorClass类型的bean，如果找不到则通过反射创建实例。


### 详细使用
#### GenerationType.IDENTITY
```Java
@Entity
@Table(name="t_user")
public class UserEntity implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	protected Long id;
}
```


#### GenerationType.TABLE
```Java
@Entity
@Table(name="t_user")
public class UserEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator="tableIdGenerator")  
	@TableGenerator(name = "tableIdGenerator",  
	    table="gen_ids",  
	    pkColumnName="gen_name",  
	    valueColumnName="gen_value",  
	    pkColumnValue="seq_test_user",  
	    allocationSize=50
	)
	protected Long id;
}
```


#### GenerationType.SEQUENCE
```Java
@Entity
@Table(name="t_user")
public class UserEntity implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seqGenerator") 
	@SequenceGenerator(name="seqGenerator", sequenceName="SEQ_TEST_USER")
	protected Long id;
}
```

### DbmIdGenerator
比如使用了dbm集成的snowflake策略，下面的配置使用了默认配置的snowflake，如果需要配置不同的datacenter和machine，建议自己实现CustomIdGenerator接口。
```Java
@Entity
@Table(name="t_user")
public class UserEntity implements Serializable {

	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO, generator="snowflake") 
	@DbmIdGenerator(name="snowflake", generatorClass=SnowflakeGenerator.class)
	protected Long id;
}
```


### @SnowFlakeId 注解

4.7.4 版本后，使用内置的snowFlakeId生成主键id时，可直接使用 @SnowflakeId 简化配置：

```Java
@Entity
@Table(name="t_user")
public class UserEntity {
	@SnowflakeId 
	protected Long id;
}
```






## 复合主键映射
jpa支持三种复合主键映射策略，dbm目前只支持一种： @IdClass 映射。
映射方法如下：
假设有一个表有两个主键：id1，id2。
实体的Java代码如下：
```Java
@Data
@Entity
@Table(name="composite_table")
@IdClass(CompositeId.class)
public class CompositeEntity {

	@Id  
	Long id1;
	@Id
	Long id2;

	@Transient
	CompositeId id;

	public CompositeId getId() {
		return new CompositeId(id1, id2);
	}
	
	public void setId(CompositeId id) {
		this.id1 = id.getId1();
		this.id2= id.getId2();
	}
	
	//....其它属性

	@Data
	public static class CompositeId implements Serializable {
		Long id1;
		Long id2;
	}
}

```
解释：
- 把需要映射为主键的实体属性都用 @Id 注解标注   
- 另外创建一个复合主键的Pojo类CompositeId，属性为实体需要映射为主键的属性，名称类型一一对应，并实现 java.io.Serializable 接口   
- 在实体类里用 @IdClass 注解标注为复合主键类为 CompositedId 类   
- 实体的CompositeId属性不是必须的，只是为了更方便使用组合id，而且无需持久化，所以如果写的话，需要用 @Transient 注解标注


复合主键实体的查找方法为：
```Java
CompositedId cid = new CompositedId(1, 1);
CompositeEntity entity = baseEntityManager.load(CompositeEntity.class, cid);

int deleteCount = baseEntityManager.removeById(CompositeEntity.class, entity.getId());
```

## 枚举处理

### 枚举映射
dbm支持jpa的@Enumerated枚举映射注解，使用方法和jpa一样，默认为EnumType.ORDINAL int值类型映射，可以通过注解属性指定为EnumType.STRING名称映射。

但是，当枚举为EnumType.ORDINAL映射的时候，ordinal的值是从0开始根据定义时的先后顺序决定，这使得我们开发的时候很不方便，比如我有一个枚举类型，是需要映射为int类型，但是值并不是从0开始的，这时候就相当的尴尬，因为你既不能用默认为EnumType.ORDINAL,也不能用EnumType.STRING。

所以dbm还另外增加了自定义的int值映射接口DbmEnumValueMapping，只要枚举类型实现了这个接口，就可以自定义返回实际的映射值，比如：
```Java
@Entity
@Table(name="TEST_USER")
public class UserEntity {
	@Id
	Long id;
	@Enumerated(EnumType.ORDINAL)
	UserGenders gender;

	public static enum UserGenders {
		FEMALE("女性"),
		MALE("男性");
		
		final private String label;
		private UserGenders(String label) {
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
	}
}
```
如果按照jpa的做法，枚举类型映射为@Enumerated(EnumType.ORDINAL)后，用户实体的gender属性对应的数据库列只能是0（FEMALE）和1（MALE）。
在dbm里，你可以通过实现DbmEnumValueMapping接口，返回自定义的映射值，比如10（FEMALE）和11（MALE）。
```Java
@Entity
@Table(name="TEST_USER")
public class UserEntity {
	@Id
	Long id;
	@Enumerated(EnumType.ORDINAL)
	UserGenders gender;

	public static enum UserGenders implements DbmEnumValueMapping<Integer> {
		FEMALE("女性", 10),
		MALE("男性", 11);
		
		final private String label;
		final private int value;
		private UserGenders(String label, int value) {
			this.label = label;
			this.value = value;
		}
		public String getLabel() {
			return label;
		}
		@Override
		public Integer getEnumMappingValue() {
			return value;
		}
		
	}
}
```

### 非int和String类型的枚举映射支持
在jpa里，@Enumerated 注解支持int和String两种枚举值类型。
在dbm里，只要属性的类型是枚举类型，并且实现了DbmEnumValueMapping接口，dbm就会自动处理枚举类型，不需要@Enumerated注解标记。
而DbmEnumValueMapping是个泛型接口，可以支持任意类型的枚举值，只要数据值从数据库取回时可以和getEnumMappingValue()返回的值匹配上（eqauls）即可。
比如项目比较奇葩，需要把枚举类型映射到Double类型：
```java
@Entity
@Table(name="TEST_USER")
@Data
public class UserEntity {
    @SnowflakeId
    Long id;
    UserGenders gender;
}
static enum UserGenders implements DbmEnumValueMapping<Double> {
        FEMALE("女性", 0),
        LADYBOY("人妖", 0.5),
        MALE("男性", 1);
        
        final private String label;
        final private double value;
        private UserGenders(String label, double value) {
            this.label = label;
            this.value = value;
        }
        public String getLabel() {
            return label;
        }
        @Override
        public Double getEnumMappingValue() {
            return value;
        }
}
```


### 枚举属性查询时的处理

- 如果枚举实现了 DbmEnumValueMapping 接口，则取DbmEnumValueMapping#getMappingValue()方法所得的值
- 通过Querys 和 BaseEntityManager 的api查询时，一般直接取枚举的name()方法所得的值
- 如果是@DbmRepository 接口，并且用@Param注解指定了enumType属性，则根据配置的取相应的值，但是DbmEnumValueMapping接口优先级更高



## json映射

有时候，我们需要在数据库的某个字段里存储json格式的数据，又想在获取到数据后转为java对象使用，这时你可以使用 @DbmJsonField 注解，这个注解会在保存实体的时候把对象转化为json字符串，然后在取出数据的时候自动把字符串转化为对象。
示例：

```Java
class SimpleEntity {
	@DbmJsonField
	private ExtInfo extInfo;

	
	public static class ExtInfo {
		String address;
		List<String> phones;
	}
}
```

如果该字段是泛型，需要保存类型信息，可以设置storeTyping属性为true

```Java
class SimpleEntity {
	@DbmJsonField(storeTyping=true)
	private Map<String, ConfigData> configData;

	
	public static class ExtInfo {
		String address;
		List<String> phones;
	}
}
```

需要添加依赖：

```xml
    <dependency>
      <groupId>org.onetwo4j</groupId>
      <artifactId>onetwo-jackson</artifactId>
    </dependency>
```




## 敏感字段映射

### 加解密映射
对于一些不适宜明文存储的字段信息，比如api密钥，存储的时候自动加密，获取的时候自动解密，此时可以使用@DbmEncryptField 注解。
```Java
@Entity
@Table(name="TEST_MERCHANT")
public class MerchantEntity implements Serializable {
	

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@Column(name="ID")
	protected Long id;
	
	@DbmEncryptField
	protected String apikey;
}
```
在@DbmRepository 使用这个功能时，可以在插入的参数后面加上后缀函数：
```sql
/*****
 * @name: batchInsert
 * 批量插入     */
    insert 
    into
        test_merchant
        (id, apikey) 
    values
        (:id, :apikey?encrypt)
```



**注意**

- dbm的敏感字段加密功能依赖jasypt

- 你可以通过下面属性配置jasypt的StandardPBEStringEncryptor 

  ```yaml
  dbm: 
      encrypt: 
          algorithm: PBEWithMD5AndTripleDES #默认加密算法
          password: test #密钥
  ```

### 脱敏映射
对于另一些字段，我们可能并不需要加解密，而只是在存储或者获取的时候，按照一定的规则脱敏。比如手机号码取出的时候自动对后面四位打上星号，或者邮件地址只显示第一个字符和@后面的字符，则可以使用 @DbmSensitiveField 注解进行脱敏映射。
```Java
@Entity
@Table(name="TEST_USER")
public class UserEntity implements Serializable {
	

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@Column(name="ID")
	private Long id;
	
	private String mobile;
	
        @DbmBindValueToField(name="mobile") //查询实体时，此字段的值来自mobile字段
        @Transient //此字段无需保存到数据库
	@DbmSensitiveField(leftPlainTextSize=7, on=SensitiveOns.SELECT)
	// 保留手机号码只显示左边7位，如13612345678，取出脱敏后mobile的值为：1361234****
	private String mobileUnsensitive;
	
	@DbmSensitiveField(leftPlainTextSize=1, sensitiveIndexOf="@",  on=SensitiveOns.SELECT)
	// 邮件地址左边保留一个长度的字符，@后面的字符都保留，其余用星号代替，如test@gmail.com，取出脱敏后为：t***@gmail.com
	private String email;
}
```

**解释**

DbmSensitiveField 属性解释如下：
- on: 表示进行脱敏的时机，有两个选择：STORE（保存到数据库的时候），SELECT（从数据库获取出来转换为java对象的时候）
- leftPlainTextSize: 脱敏时需要左边保持明文的字符长度
- rightPlainTextSize: 脱敏时需要右边保持明文的字符长度
- sensitiveIndexOf: 当不想整个字段进行脱敏的时候，此属性表示某个指定的字符索引作为脱敏的结束索引。比如邮件脱敏，@字符后面的保留时，此属性值可以写为"@"
- replacementString: 替换敏感数据的字符串，默认为星号



**注意**

此功能从 4.7.4 版本开始支持




### 字段绑定
@DbmBindValueToField 注解可以帮某个字段的值绑定到另一个字段，绑定后，实体查询时，此字段的值将会取自绑定的值。例子可以参考 [脱敏映射](#脱敏映射) 

**注意**

此功能从 4.7.4 版本开始支持




## 其它特有的映射



### @DbmField注解
@DbmField 注解可自定义一个值转换器，用于从数据库表获取的字段值转换为Java对象的属性值，和把Java对象的属性值转换为数据库表的字段值。   
@DbmJsonField 注解实际上是包装了@DbmField注解实现的。




## BaseEntityManager接口和QueryDSL
大多数数据库操作都可以通过BaseEntityManager接口来完成。   
BaseEntityManager可直接注入。   

先来个简单的使用例子：
```java    

	
	@Resource
	private BaseEntityManager entityManager;

	@Test
	public void testSample(){
		UserAutoidEntity user = new UserAutoidEntity();
		user.setUserName("dbm");
		user.setMobile("1333333333");
		user.setEmail("test@test.com");
		user.setStatus(UserStatus.NORMAL);
		
		//save
		Long userId = entityManager.save(user).getId();
		assertThat(userId, notNullValue());
		
		//update
		String newMobile = "13555555555";
		user.setMobile(newMobile);
		entityManager.update(user);
		
		//fetch by id
		user = entityManager.findById(UserEntity.class, userId); 
		assertThat(user.getMobile(), is(newMobile));
		
		//通过实体属性查找，下面的调用相当于sql条件： where mobile='13555555555' and status IN ('NORMAL', 'DELETE') and age>18
		user = entityManager.findOne(UserAutoidEntity.class, 
										"mobile", newMobile,
										"status:in", Arrays.asList(UserStatus.NORMAL, UserStatus.DELETE),
										"age:>", 18);
		assertThat(user.getId(), is(userId));

		//下面的调用相当于sql条件： where registerTime>=:date1 and registerTime<:date2
		entityManager.findList(UserEntity.class, "registerTime:date in", new Object[]{date1, date2})
		
		
	}
```
BaseEntityManager对象的find开头的接口，可变参数一般都是按键值对传入，相当于一个Map，键是实体对应的属性(+冒号+操作符，可选，不加默认就是=)，值是对应属性的条件值：   
```Java
entityManager.findOne(entityClass, propertyName1, value1, propertyName2, value2......);   
entityManager.findList(entityClass, propertyName1, value1, propertyName2, value2......);
```
key，value形式的参数最终会被and操作符连接起来。

其中属性名和值都可以传入数组或者List类型的参数，这些多值参数最终会被or操作符连接起来，比如：
- 属性名参数传入一个数组： 
```Java   
entityManager.findList(entityClass, new String[]{propertyName1, propertyName2}, value1, propertyName3, value3);
```
最终生成的sql语句大概是：
```sql
select t.* from table t where (t.property_name1=:value1 or t.property_name2=:value1) and t.property_name3=:value3
```

- 属性值参数传入一个数组： 
```Java   
entityManager.findList(entityClass, propertyName1, new Object[]{value1, value2}, propertyName3, value3);
```
最终生成的sql语句大概是：
```sql
select t.* from table t where (t.property_name1=:value1 or t.property_name1=:value2) and t.property_name3=:value3
```

- find 风格的api会对一些特殊参数做特殊的处理，比如 K.IF_NULL 属性是告诉dbm当查询值查找的属性对应的值为null或者空时，该如何处理，IfNull.Ignore表示忽略这个条件。 **
比如：
```Java   
entityManager.findList(entityClass, propertyName1, new Object[]{value1, value2}, propertyName3, value3, K.IF_NULL, IfNull.Ignore);
```
那么，当value3（或者任何一个属性对应的值）为nul时，最终生成的sql语句大概是：
```sql
select t.* from table t where (t.property_name1=:value1 or t.property_name1=:value2) 
```
property_name3条件被忽略了。

### 操作符
BaseEntityManager的属性查询支持如下操作符：   
=, >, <, !=, in, not in, date in, is null, like, not like

### Query DSL API
dbm还提供了一个专门用于构建查询的dsl api
```Java

//使用 querys dsl api
UserAutoidEntity queryUser = Querys.from(entityManager, UserAutoidEntity.class)
									.where()
										.field("mobile").is(newMobile)
										.field("status").is(UserStatus.NORMAL)
									.end()
									.toQuery()
									.one();
assertThat(queryUser, is(user));
```

注意：
4.7.3后，query dsl api 已集成到 BaseEntityManager 接口，可以通过 BaseEntityManager 直接创建查询：
```Java
public Optional<User> findBy(String month, Long userId) {
		return baseEntityManager.from(User.class)
								.where()
									.field("month").is(month)
									.field("userId").is(userId)
								.toQuery()
								.optionalOne();
	}
```

通过链式api和Java8 的 Stream api，你可以创建出这样的查询代码：
```Java
public List<User> findList(String month, Long userId) {
	return baseEntityManager.from(DuesDetailEntity.class)
						.where()
							.field("duesMonth").is(month)
							.field("userId").is(userId)
						.toQuery()
						.list()
						.stream()
						.map(user -> user.asBean(UserVO.class)) //把实体转换为VO
						.collect(Collectors.toList());
}
```

动态条件和or 查询：
```Java
// 下面代码生成的sql条件：(age = 12 and userName like %test%) or (email like %qq.com and mobile=136666666) 
public Optional<User> findBy(String month, Long userId) {
		return baseEntityManager.from(User.class)
				.where()
                                .field("age").is(12)
                                .field("userName").when(()->userName!=null).like(userName) // userName不为null的时候，userName条件才会被生成
                                .or()
                                    .field("email").prelike("qq.com")
                                    .field("mobile").is("13666666666")
				.toQuery()
				.optionalOne();
	}
```



## CrudEntityManager接口
CrudEntityManager是在BaseEntityManager基础上封装crud的接口，是给喜欢简单快捷的人使用的。   
CrudEntityManager实例可在数据源已配置的情况下通过简单的方法获取：

```java   
@Entity   
@Table(name="TEST_USER_AUTOID")   
public class UserAutoidEntity {

	final static public CrudEntityManager<UserAutoidEntity, Long> crudManager = Dbms.obtainCrudManager(UserAutoidEntity.class);

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@Column(name="ID")
	protected Long id;
	@Length(min=1, max=50)
	protected String userName;

	//省略getter和setter
}   
```
然后通过静态变量直接访问crud接口：   
```Java    

	UserAutoidEntity.crudManager.save(entity);
	UserAutoidEntity user = UserAutoidEntity.crudManager.findOne("userName", userName);

```



## DbmRepository动态sql查询接口
DbmRepository接口支持类似mybatis的sql语句与接口绑定，但sql文件不是写在丑陋的xml里，而是直接写在sql文件里，这样用eclipse或者相关支持sql的编辑器打开时，就可以语法高亮，更容易阅读。

### 1、定义一个接口   
包名：test.dao   
```java   
@DbmRepository
public interface UserAutoidDao {

	@ExecuteUpdate
	public int removeByUserName(String userName);
}

```
### 2、定义一个.jfish.sql文件
在resource源码代码文件下新建一个目录：sql
然后在sql目录里新建一个UserAutoidDao全类名的.jfish.sql文件，完整路径和文件为：
sql/test.dao.UserAutoidDao.jfish.sql
文件内容为：    

```sql
/*****
 * @name: removeByUserName
 * 批量删除
 */
    delete from test_user_autoid 
        where 1=1 
		---这里的userName变量就是接口里的userName参数
        [#if userName?has_content]
			---这里的userName命名查询参数也是接口里的userName参数
         and user_name like :userName
        [/#if]
```


解释：   
- dbm会根据sql文件名去掉.jfish.sql后缀后作为类名，绑定对应的接口类，此处为：test.dao.UserAutoidDao    
- @name: 表示此sql绑定的方法，此处表示会绑定到UserAutoidDao.removeByUserName方法    
- \[\#if\]...\[/\#if\]，是freemarker的语法，表示条件判断。此处表示，如果userName的值不为空，才生成“user_name like ？” 这个条件   
- :userName，spring jdbc的命名参数，和接口的方法参数绑定 
- @ExecuteUpdate注解表示这个方法会以jdbc的executeUpdate方法执行，实际上可以忽略，因为dbm会识别update，insert，delete等前缀的方法名来判断。

### 3、调用   
```java

@Service   
@Transactional   
public class UserAutoidServiceImpl {

	@Resource
	private UserAutoidDao userAutoidDao;

	public int removeByUserName(){
		return this.userAutoidDao.removeByUserName("%userName%");
	}
}

```

`
   提示：如果你不想传入 "%userName%"，可以把sql文件里的命名参数“:userName”改成“:userName?likeString”试试，后面的?likeString是调用dbm内置的likeString方法，该方法会自动在传入的参数前后加上'%'。
`
`
   注意：从4.7.3开始，dbm的 DbmRepository接口 支持Java8接口默认方法。
`
### 通过@Query直接在代码里写sql
虽然本人不喜欢不推荐在代码里写sql，但实际开发中经常遇到很多人都是喜欢简单粗暴，直接在代码里通过注解写sql，所以，新版（4.5.2-SNAPSHOT+）的dbm提供了@Query来支持在代码里写sql。

使用示例：
```Java
@DbmRepository //标记这是一个dbm的Repository接口
public interface UserDao {
	
	@Query("insert into test_user (id, email, gender, mobile, nick_name, password, status, user_name) "
			+ " values (:id, :email, :gender, :mobile, :nickName, :password, :status, :userName)")
	int batchSaveUsers(List<UserEntity> users);
	
	@Query(value="select t.* from test_user t where 1=1 "
			+ "[#if userName?has_content] "
				+ "and t.user_name like :userName?likeString "
			+ "[/#if]")
	Page<UserEntity> findUserPage(Page<UserEntity> page, String userName);

}
```

## sql片段支持
有时候，两个查询方法的sql里，大部分是相同的（比如查询条件），只有小部分不同，如果写两份，就需要维护两份sql。这时候，你可以使用sql片段@fragment

比如有两个sql查询

sql1是findUserListLikeName:

```sql
/***
 * @name: findUserListLikeName
 */
select 
    usr.*
from 
    test_user usr
where 
    user_name like :userName?likeString
```

sql2是countUserLikeName:

```sql
/***
 * @name: countUserLikeName
 */
select 
    count(1)
from 
    test_user usr
where 
    user_name like :userName?likeString
```

两个方法的查询条件是一样的，只是select的数据不同，此时可以把相同的部分抽取出来：

```sql
/***
 * @name: queryUser
 * @fragment: subWhere
 */
from 
    test_user usr
where 
    user_name like :userName?likeString

```

findUserListLikeName的sql可以改写为：

```sql
/***
 * @name: findUserListLikeName
 */
select 
    usr.*
${fragment['queryUser.fragment.subWhere']}
```

countUserLikeName改写为：

```sql
/***
 * @name: countUserLikeName
 */
select 
    count(1)
${fragment['queryUser.fragment.subWhere']}
```






## 动态sql查询的语法和指令

### 常用指令
sql模板使用的实际上是freemarker模板引擎，因此freemarker支持的语法都可以使用。
一般比较常用到的指令如下：
- if 指令
```sql
[#if 条件表达式]
......
[/#if]
```
- list 迭代指令
```sql
[#list 可迭代的变量 as item]
......t.column_name = ${item.property1}
[/#list]
```
条件表达式除了通常的逻辑判断外，还有一些比较常用到的表达式：
- 变量??,双问号，用于判断一个变量是否存在
- 变量?has_content，用于判断变量是有内容，比如字符串的话，相等于判断是否为空。

### dbm扩展指令
另外增加了一些特定的指令以帮助处理sql，包括：

- @foreach
- @str
- @where
- @set
- @dateRange

#### foreach指令
foreach 遍历指令

可以在sql，循环可遍历的参数，并用joiner连接起来，比如当传入ids是个列表，我们需要在sql进入in查询时：

```sql
/***
 * @name: findPermissions
 * @parser: template
 * 
 */
  select 
      t.*
    from 
        data t
   [#if ids??]
    where
        t.id in (
	        [@foreach list=ids joiner=', '; id, index]
	            #{id}
	        [/@foreach]
        )
   [/#if]
```
- list 属性：可遍历的参数
- joiner 属性：连接字符
- id：遍历的时候，引用每个正在遍历的元素的变量名
- index：当前遍历的索引
当然，这里只是为了演示foreach指令的用法，实际上，dbm的sql参数可以直接支持list参数类型，当传入的参数是个列表的时候，会自动分解参数。
上面的语句实际上可直接写成： 
```sql
select 
      t.*
    from 
        data t
   [#if ids??]
    where
        t.id in ( :ids )
   [/#if]
```

#### str指令
@str 字符串指令

可以在sql动态生成条件查询时，自动插入指定字符，同时去掉头尾多余的字符，比如动态插入where和去掉多余的and或者or：

```sql
/****
 * @name: findUsers
 */
    select
        *
    from
        TEST_USER u
    [@str insertPrefix='where' trimPrefixs='and | or' trimSuffixs='and | or']
        [#if query.userName?has_content]
            u.user_name = :query.userName
        [/#if]
        [#if query.age??]
            and u.age = :query.age
        [/#if]
        [#if query.status??]
            and u.status = :query.status or 
        [/#if]
    [/@str]
```
- insertPrefix 属性：当指令里面的sql条件不为空的时候，会自动把insertPrefix属性的字符串插入，这里就是where

- trimPrefixs 属性：如果生成的sql片段以trimPrefixs指定的单词开始时，则会自动被去掉。支持指定多个单词，|为分隔符。

- trimSuffixs 属性：如果生成的sql片段以trimSuffixs指定的单词结束时，则会自动被去掉。支持指定多个单词，|为分隔符。


### where指令
where指令可以在sql动态生成条件查询时，自动加上where，或者去掉多余的and或者or关键字，它是@str指令的包装。
@str指令一节里的sql可以用where指令写成这样：

```sql
/****
 * @name: findUsersWithWhere
 */
    select
        *
    from
        TEST_USER u
    [@where]
        [#if query.userName?has_content]
            u.user_name = :query.userName
        [/#if]
        [#if query.age??]
            and u.age = :query.age
        [/#if]
        [#if query.status??]
            and u.status = :query.status or 
        [/#if]
    [/@where]
```
### set指令
set  指令与where指令类似，只是@str指令的包装，用于sql更新语句：
```sql
/***
 * @name: updateUsersWithSet
 */
    update
        TEST_USER 
    [@set]
        [#if query.userName?has_content]
            user_name = :query.userName, 
        [/#if]
        [#if query.age??]
            age = :query.age, 
        [/#if]
        [#if query.status??]
            status = :query.status,
        [/#if]
    [/@set]
    where 
        id = :query.id
```

### dateRange

```sql
        // 以天（date）为间隔，遍历输出从10月1日到11日（不包含）的日期，日期按照format格式化为字符串，format参数不写，则dateVar为Date类型对象
   [@dateRange from='2014-10-01' to='2014-10-11' type='date' format='yyyyMMdd' joiner=' or '; dateVar, index]
        t.date = '${dateVar}'
   [/@dateRange]
```

### 其他特性


- 支持通过特殊的注解参数进行查询分派：
```Java
@DbmRepository
public interface UserDao {

	public List<UserVO> findUserList(@QueryDispatcher String type);

}
```
dbm会根据QueryDispatcher注解标记的特殊参数的值，分派到不同的sql。
如果type==inner时，那么这个查询会被分派到findUserList(inner)；
如果type==outer时，那么这个查询会被分派到findUserList(inner)
sql文件：
```sql
/***
 * @name: findUserList(inner)
 */
select 
    usr.*
from 
    inner_user usr


/***
 * @name: findUserList(outer)
 */
select 
    usr.*
from 
    outer_user usr
```

- in条件可以传入 Collection 类型的值，会自动解释为多个in参数
DbmRepository接口：   
```Java
@DbmRepository
public interface UserDao {

	public List<UserVO> findUser(List<String> userNames);

}
```
sql文件：   
```sql
/***
 * @name: findUser
 */
select 
    usr.*
from 
    t_user usr
where 
	usr.user_name in ( :userNames )

```
注意：必须是Collection类型，不支持数组类型。

- dbm默认会注入一些辅助函数以便在sql文件中调用，可通过_func前缀引用，比如${_func.dateAs(date, "yyyy-MM-dd")}格式化日期。通过QueryConfig注解扩展在sql文件使用的辅助函数集。
sql文件：   
```sql
/***
 * @name:
 *  findUser
 */
select 
    usr.*
from 
    t_user usr
where 
	usr.birthday=${_func.dateAs(date, "yyyy-MM-dd")}

```

- 支持Optional类型的返回值

## 用DbmRepository执行脚本
4.8.0版本后，DbmRepository支持执行sql脚本。
只需要在DbmRepository的方法上加上注解@SqlScript，方法对应的sql即会被当做sql脚本执行。
但需要注意：
- 执行脚本必须返回void
- 脚本无法设置 jdbc 参数

如：
```Java
@DbmRepository
public interface SqlScriptDao {
    @SqlScript
    void createTables();
}
```
对应的sql文件：
```sql
/**
 * @name: createTables
 */
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for wx_access_token
-- ----------------------------
DROP TABLE IF EXISTS `test_table`;
CREATE TABLE `test_table`  (
  `id` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `create_at` datetime(0) NOT NULL,
  `update_at` datetime(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
```

## DbmRepository接口的多数据源支持
DbmRepository 查询接口还可以通过注解支持绑定不同的数据源，dataSource的值为spring bean的名称：
```Java
@DbmRepository(dataSource="dataSourceName1")
public interface Datasource1Dao {
}

@DbmRepository(dataSource="dataSourceName2")
public interface Datasource2Dao {
}
```



## DbmRepository接口对其它orm框架的兼容
DbmRepository 的查询接口是可以独立于dbm使用的，其它orm框架可以通过实现QueryProvideManager接口，然后通过 @DbmRepository 注解的queryProviderName或queryProviderClass属性指定特定的QueryProvideManager实现类。从而让DbmRepository查询接口使用其它orm框架，避免不同orm框架共存带来的一些副作用。    

dbm内置了JPA（Hibernate）实现的QueryProvideManager。   
但一个一个地把DbmRepository接口设置成相同的实现的QueryProvideManager实现的是不明智，只是没有意义的重复劳动，所以dbm另外提供了@EnableDbmRepository注解，单独激活和配置DbmRepository默认的QueryProvideManager。
```Java
@EnableDbmRepository(value="org.onetwo.common.hibernate.dao", 
						defaultQueryProviderClass=HibernateJPAQueryProvideManager.class,
						autoRegister=true)
	public static class HibernateTestConfig {
}
```


## 查询映射
DbmRepository的查询映射无需任何xml配置，只需要遵循规则即可：   
- 1、  Java类的属性名与sql查询返回的列名一致(不区分大小写)   
- 2、  或者Java类的属性名采用驼峰命名，而列明采用下划线的方式分隔。如：userName对应user_name   
默认的映射规则实际上和使用了@DbmRowMapper注解下的SMART_PROPERTY模式一致。
详见：[注解@DbmRowMapper](#注解dbmrowmapper)

举例：   
### 创建一个DbmRepository接口
```Java

@DbmRepository
public interface CompanyDao {
	List<CompanyVO> findCompaniesByLikeName(String name);
	List<CompanyVO> findCompaniesByNames(Collection<String> names);
}

public class CompanyVO {
	protected Long id;
	protected String name;
	protected String description;
	protected int employeeNumber;

	//省略getter和setter
}
```

### 对应的sql文件CompanyDao.jfish.sql
内容如下：   
```sql
/****
 * @name: findCompaniesByLikeName
 */
select 
    comp.id,
    comp.name,
    comp.description,
    comp.employee_number
from
    company comp
where
    comp.name like :name?likeString
    

/****
 * @name: findCompaniesByNames
 */
select 
    comp.id,
    comp.name,
    comp.description,
    comp.employee_number
from
    company comp
[#if names?? && names?size>0]
where
    comp.name in (:names)
[/#if]
```


### 调用代码
```Java
List<CompanyVO> companies = this.companyDao.findCompaniesByLikeName("测试公司");
companies = this.companyDao.findCompaniesByNames(Collections.emptyList());
companies = this.companyDao.findCompaniesByNames(Arrays.asList("测试公司-1", "测试公司-2"));
```


## 复杂的嵌套查询映射
有时，我们会使用join语句，查询出一个复杂的数据列表，比如包含了company、department和employee三个表。
返回的结果集中，一个company对应多条department数据，而一条department数据又对应多条employee数据，我们希望把多条数据这样的数据最终只映射到一个VO对象里。这时候，你需要使用@DbmResultMapping和@DbmNestedResult两个注解，以指定VO的那些属性需要进行复杂的嵌套映射。

举例如下：
### 创建一个DbmRepository接口和相应的VO
```Java

@DbmRepository
public interface CompanyDao {

	@DbmResultMapping({
			@DbmNestedResult(property="departments.employees", columnPrefix="emply_", nestedType=NestedType.COLLECTION),
			@DbmNestedResult(property="departments", id="id", nestedType=NestedType.COLLECTION)
	})
	List<CompanyVO> findNestedCompanies();
}

public class CompanyVO {
	protected Long id;
	protected String name;
	protected String description;
	protected int employeeNumber;
	protected List<DepartmentVO> departments;

	//省略getter和setter
}

public class DepartmentVO {
	protected Long id;
	protected String name;
	protected Integer employeeNumber;
	protected Long companyId;
	protected List<EmployeeVO> employees;
	//省略getter和setter
}

public class EmployeeVO  {
	protected Long id;
	protected String name;
	protected Date joinDate;
	//省略getter和setter
}
```
解释：   
- @DbmResultMapping 注解表明，查询返回的结果需要复杂的嵌套映射
- @DbmNestedResult 注解告诉dbm，返回的CompanyVO对象中，哪些属性是需要复杂的嵌套映射的。property用于指明具体的属性名称，columnPrefix用于指明，需要把返回的结果集中，哪些前缀的列都映射到property指定的属性里，默认会使用property。nestedType标识该属性的嵌套类型，有三个值，ASSOCIATION表示一对一的关联对象，COLLECTION表示一对多的集合对象，MAP也是一对多，但该属性的类型是个Map类型。id属性可选，配置了可一定程度上加快映射速度。

### 对应的sql
```sql
/*****
 * @name: findNestedCompanies
 */
select 
    comp.*,
    depart.id as departments_id,
    depart.company_id as departments_company_id,
    depart.`name` as departments_name,
    emply.name as emply_name,
    emply.join_date as emply_join_date,
    emply.department_id as emply_department_id
from 
    company comp
left join 
    department depart on comp.id=depart.company_id
left join
    employee emply on emply.department_id=depart.id
```


### 调用
```Java
List<CompanyVO> companies = companyDao.findNestedCompanies();
```

- 注意：若嵌套类型为NestedType.COLLECTION，而容器的元素为简单类型，则把@DbmNestedResult 注解的id属性设置为“value”即可。




## 自定义实现DbmRepository接口
dbm的Repository查询接口采用了流行的只有接口没有实现类的风格，但有时你需要的查询，可能不只是写一条sql查询出来即可的，尽管你可以把这种逻辑处理定义到Service，但你又觉得这些是数据处理逻辑并不属于Service，并且你希望把这种实现也挂载到已经存在的Repository查询接口，没问题，dbm支持这种做法。
比如，你已经有了一个名叫UserDao的Repository查询接口，然后你可以自顶一个CustomerUserDao接口：
```Java
public interface CustomUserDao {
	
	int batchInsert(List<UserTableIdEntity> users);

}

```
在同一个包路径下，你需要写一个CustomUserDao的实现类，实现类的命名规则是：自定义接口类名+Impl，即：CustomUserDaoImpl：
```Java
@Component
public class CustomUserDaoImpl implements CustomUserDao {
	@Autowired
	private BaseEntityManager baseEntityManager;

	@Override
	public int batchInsert(List<UserTableIdEntity> users) {
		Collection<UserTableIdEntity> dbusers = baseEntityManager.saves(users);
		return dbusers.size();
	}

}
```
然后再让UserDao继承你的扩展接口：
```Java
@DbmRepository
public interface UserDao extends CustomUserDao {
	
	List<UserTableIdEntity> findByUserNameLike(String userName);

}

```
这样，当你注入UserDao，并调用batchInsert方法时，实际调用的就会是CustomUserDaoImpl的batchInsert方法了：
```Java
public class CustomDaoTest {
	
	@Autowired
	private UserDao userDao;
	
	@Test
	public void test(){
		int total = 100;
		List<UserTableIdEntity> users = createUsers(total);
		int res = this.userDao.batchInsert(users);
		assertThat(res).isEqualTo(total);
	}

}
```



## 批量插入

在mybatis里，批量插入非常麻烦，我见过有些人甚至使用for循环生成value语句来批量插入的，这种方法插入的数据量如果很大，生成的sql语句以吨计，如果用jdbc接口执行这条语句，系统必挂无疑。   
在dbm里，批量插入有几种方式。

- 注意，批量操作不会触发 DbmEntityListener 接口的回调

### 使用session接口的批量插入接口
```java   
List<UserEntity> userList = new ArrayList<>();
userList.add(user);
...
baseEntityManager.getSessionFactory().getSession().batchInsert(userList)

```

### 使用DbmRepository查询批量插入
在dbm里，使用编写sql的方式批量接口很简单。   

定义接口：   
```java   

public interface UserAutoidDao {

	public int batchInsert(List<UserAutoidEntity> users);
}

```

然后定义sql：     

```sql

/*****
 * @name: batchInsert
 * 批量插入     */
    insert 
    into
        test_user_autoid
        (birthday, email, gender, mobile, nick_name, password, status, user_name) 
    values
        (:birthday, :email, :gender, :mobile, :nickName, :password?encrypt, :status.value, :userName)


```
**说明**
- 方法名称以batchInsert、batchUpdate、batchSave开头命名即被视为批量操作，否则需要使用@ExecuteUpdate(isBatch=true)来注明该方法是批量操作
- 因为是批量操作，第一个必须是Collection集合类型，若有多个参数且第一个参数不是集合类型，则必须使用@BatchObject标记集合类型的参数
- values语句里面的userName等字段对应集合里面元素（对象）的属性

### 批量插入或更新
dbm也利用了mysql的on duplicate key update语法，支持批量插入或更新：
```java   
List<UserEntity> userList = new ArrayList<>();
userList.add(user);
...
baseEntityManager.getSessionFactory().getSession().batchInsertOrUpdate(userList, 10000)

```

### 从其它地方加载DbmRepository接口的sql
4.8.0 版本后DbmRepository接口的sql可以自定义加载方式。
DbmRepository接口默认是自动绑定接口名称对应的 ".jfish.sql"后缀的sql文件的，但有些场景，我们需要从其它地方加载sql。   
这时，你可以通过@QueryName注解，标注命名查询的参数，动态设置查询名称（正常情况下，名称是类名+方法名），   
使用@QuerySqlTemplateParser注解配置加载和解释sql的具体过程：
```java
@DbmRepository
public interface SqlExecutor {
    @QuerySqlTemplateParser(SimpleSqlTemplateParser.class) // 使用SimpleSqlTemplateParser解释命名查询
    <T> T executeSql(@QueryName String name, // 标记此参数是命名查询的名字参数
                    UserStatus status, 
                    @QueryResultType Class<T> resultType);//对应查询的结果返回的类型

}

public class SimpleSqlTemplateParser implements SqlTemplateParser {

    @Override
    public String parseSql(String name, Object context) {
        if ("countDisabledUser".equals(name)) {
            return "select count(1) from test_user t where  t.status = :status";
        }
        return name;
    }
    
}


SqlExecutor.executeSql("countDisabledUser", // 执行名称为"countDisabledUser"的查询，而这个命名查询的sql就是SimpleSqlTemplateParser返回的sql
                    UserStatus.DISABLED, 
                    Long.class); // countDisabledUser实际执行的是一条统计sql，所以这里返回Long类型
```

### 直接传入要执行的sql作为参数
4.8.0 版本支持。
在一些更加复杂和需要动态化的场景，sql可能不是从某个地方加载的，而是需要从参数传入，然后直接执行，类似于原始的jdbc的execteSql功能，但同时又需要使用dbm的sql解释和参数化功能，也是没问题的。

```java
@DbmRepository
public interface SqlExecutor {
    
    <T> T executeSql(@Sql String sql,
                    UserStatus status, 
                    @QueryResultType Class<T> resultType, //对应查询的结果返回的类型
                    @QueryParseContext Map<String, Object> ctx);

}

Map<String, Object> ctx = Maps.newHashMap();
ctx.put("now", new NiceData());
SqlExecutor.executeSql("select count(1) from test_user t where  t.status = :status and t.birthDay=${now.format('yyyy-MM-dd')}", 
                    UserStatus.DISABLED, 
                    Long.class); // countDisabledUser实际执行的是一条统计sql，所以这里返回Long类型
```

## 其它映射特性



### 注解@DbmRowMapper

用于配置DbmRepository类的数据映射器，配置指定的mapper，默认为ENTITY模式。
由于标注为实体的映射规则和Pojo默认的映射规则不一致，导致有时候某些查询返回需要用到两种规则时无法兼容，使用此注解的MIXTURE 混合模式可以兼容两种规则。

- ENTITY模式
使用EntryRowMapper映射器。
EntryRowMapper会使用实体的风格映射，即：
如果有@Column注解，则按照注解的映射匹配；
如果没有使用注解，则把属性名称转为下划线匹配；

- SMART_PROPERTY模式：
使用DbmBeanPropertyRowMapper映射属性，即：
自动把bean的属性名称转为小写和下划线两种方式去匹配sql返回的列值。
此模式和不使用@DbmRowMapper注解时一致。

- MIXTURE 混合模式：
先匹配ENTITY模式，如果没有，则匹配SMART_PROPERTY模式


## 充血模型支持   

dbm对充血模型提供一定的api支持，如果觉得好玩，可尝试使用。   
使用充血模型，需要下面几个步骤：
### 1、需要在Configuration类配置model所在的包位置
单独使用dbm的项目，只要model类在@EnableDbm注解所在的配置类的包（包括子包）下面即可，dbm会自动扫描。
```Java

@EnableDbm
public class DbmSampleApplication {
}  
```

### 2、继承RichModel类
```Java

@Entity
@Table(name="web_user")
public class User extends RichModel<User, Long> {
}
   
```

### 3、使用api
```Java   
//根据id查找实体   
User user = User.findById(id);   
//保存实体   
new User().save();   
//统计
int count = User.count().intValue();   
//查找, K.IF_NULL属性是告诉dbm当查询值userName为null或者空时，该如何处理。IfNull.Ignore表示忽略
List<User> users = User.findList("userName", userName, K.IF_NULL, IfNull.Ignore);
   
```

## 参数配置
- logSql：是否打印执行的sql和时间，默认为true；
需要同时配置日志文件：
```xml
	<!-- print dbm sql-->
    <logger name="org.onetwo.dbm.core.internal.LogSqlInterceptor" level="TRACE">
        <appender-ref ref="logFile" />
    </logger>
```
- watchSqlFile：是否监视sql文件，如果有修改则重新加载，默认为true；
- useBatchOptimize：是否使用批量优化save和insert等api的操作，为true并且插入数量超过了设定的阈值，则会把此类api的循环插入优化为jdbc的批量插入，默认为true；
- useBatchThreshold：批量插入的阈值，调用save和insert等api时，如果传入的集合数量超过了阈值，则自动转为批量插入，否则循环插入，默认为50；
- processSizePerBatch：批量插入时，每次提交的数量，默认为10000；
- enableSessionCache：是否启用会话缓存，默认为false；


## 代码生成器
dbm内置了一个代码生成器，可以根据模板生成下列文件：
- 实体
- service
- controller
- 基于freemarker的增删改查页面
- 基于element-ui(vue)的增删改查页面

使用示例：
```Java
DbmGenerator.createWithDburl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8", "root", "root")
					.javaBasePackage("com.test.order")//基础包名
					.pluginProjectDir("OrderPlugin")//插件项目名称
					.webadminGenerator("t_order")//要生成的表名
						.generateEntity()
						.generateServiceImpl()//配置在service.impl包下生成service类
						.generateController(BaseController.class)//配置在controller包下生成controller，并制定controller基类
						.generatePage()//生成freemarker的增删改查页面，配置在src/main/resources/META-INF/resources/webftls/${pluginName}下生成crud页面
						.generateVueCrud()//生成基于element-ui(vue)的增删改查页面，
					.end()
					.build()
					.generate();//生成文件
```

## 辅助工具导出表结构为excel
可以通过ExcelExporter类导出表结构为excel

```Java
    TableExportParam params = new TableExportParam();
    params.setExportFilePath("f:/test/表字段说明.xls"); // 导出的excel文件保存路径
    params.addTable("table1", // 导出的表名
            "table2");
    params.setConfigurer(it -> {
        // 满足条件的字段才会被导出
        it.setCondition("#column.name!='create_at' && #column.name!='update_at'");
    });
    ExcelExporter export = ExcelExporter.create(dataSource);
    export.exportTableShema(params); // 导出
```


## 待续。。。



## 捐赠
如果你觉得这个项目帮到了你，请用支付宝打赏一杯咖啡吧~~~   

![支付宝](doc/alipay2.jpg)
