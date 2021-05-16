package com.company;


import com.company.service.User;
import com.company.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.time.ZoneId;

@Configuration //表示该类是一个配置类，因为我们创建ApplicationContext时，使用的实现类是AnnotationConfigApplicationContext，必须传入一个标注了@Configuration的类名。
@ComponentScan //告诉容器，自动搜索当前类所在的包以及子包，把所有标注为 @Component 的Bean自动创建出来，并根据 @Autowired 进行装配。必须合理设计包的层次结构，才能发挥@ComponentScan的威力。
@PropertySource("app.properties") //表示读取classpath的app.properties；Spring容器看到@PropertySource("app.properties")注解后，自动读取这个配置文件，然后，我们使用@Value正常注入
public class AppConfig {          //注意区分其和 AppService.java 中的 private Resource resource; 的使用场景
	// 定制 Bean--创建第三方Bean:
	// 如果一个Bean不在我们自己的package管理之内，例如ZoneId，如何创建它？
	// 答案是我们自己在 @Configuration 类中编写一个Java方法(方法名没要求)创建并返回它，注意给方法标记一个@Bean注解：
	// Spring对标记为@Bean的方法只调用一次，因此返回的Bean仍然是单例。
	// 实操：联系 MailService.java 中用注解 Autowired 修饰的 zoneId；
	// 		如果不注释下面的代码，则在MailService.java 的 sendLoginMail 中打印的登陆时间就是 UTC 的时间
	// 		如果注释下面的代码，则在MailService.java 的 sendLoginMail 中打印的登陆时间就是系统的时间即东八区的时间
	// @Bean
	// ZoneId createZoneId() {
	// 	return ZoneId.of("Z");
	// }
	//
	// 进阶版1：from：Spring开发--IoC容器--注入配置
	// Spring容器还提供了一个更简单的 @PropertySource 来自动读取配置文件。我们只需要在@Configuration配置类上再添加一个注解.
	// Spring容器看到@PropertySource("app.properties")注解后，自动读取这个配置文件，然后，我们使用@Value正常注入：
	// 注意注入的字符串语法，它的格式如下：
	//		"${app.zone}"表示读取key为app.zone的value，如果key不存在，启动将报错；
	// 		"${app.zone:Z}"表示读取key为app.zone的value，但如果key不存在，就使用默认值Z。
	@Value("${app.zone:Z}")
	String zoneId;

	@Bean
	ZoneId createZoneId() {
		return ZoneId.of(zoneId);
	}
	// 进阶版2：还可以把注入的注解写到方法参数中：
	// @Bean
	// ZoneId createZoneId(@Value("${app.zone:Z}") String zoneId) {
	// 	return ZoneId.of(zoneId);
	// }


	@SuppressWarnings("resource")
	public static void main(String[] args) {
		/**
		 * 从 Spring开发--IoC容器--使用 Annotation 配置 开始：
		 * 使用Spring的IoC容器，实际上就是通过类似XML这样的配置文件，把我们自己的Bean的依赖关系描述出来，然后让容器来创建并装配Bean。
		 * 一旦容器初始化完毕，我们就直接从容器中获取Bean使用它们。
		 * 使用XML配置的优点是所有的Bean都能一目了然地列出来，并通过配置注入能直观地看到每个Bean的依赖。
		 * 它的缺点是写起来非常繁琐，每增加一个组件，就必须把新的Bean配置到XML中。
		 * 有没有其他更简单的配置方式呢？？？
		 * 有！我们可以使用Annotation配置，可以完全不需要XML，让Spring自动扫描Bean并组装它们。
		 * 我们把上一节的示例改造一下，先删除XML配置文件，然后，给 UserService 和 MailService 添加几个注解。
		 * 		1. 给 MailService 添加一个@Component注解:
		 * 			@Component注解就相当于定义了一个Bean，它有一个可选的名称，默认是 mailService，即小写开头的类名。 ===术语===
		 *		 	联系：Spring开发--IoC容器--定制 Bean 的使用别名，这个联系启发于Spring开发--IoC容器--注入配置的 ===连点成线===
		 * 		    一个Class名为 SmtpConfig 的Bean，它在Spring容器中的默认名称就是 smtpConfig，除非用 @Qualifier 指定了名称。
		 *		2. 给 UserService 添加一个 @Component 注解和一个 @Autowired 注解：
		 *			使用 @Autowired 就相当于把指定类型的Bean注入到指定的字段中。
		 * 和XML配置相比，@Autowired大幅简化了注入，因为它不但可以写在set()方法上，还可以直接写在字段上，甚至可以写在构造方法（的方法参数）中：
		 * ===和springioc_xml工程中提到的 依赖（的）注入方式 有点类似===
		 * 		@Component
		 * 		public class UserService {
		 * 		    @Autowired
		 * 		    MailService mailService;
		 *
		 * 		    ...
		 * 		}
		 *      或者
		 *      @Component
		 * 		public class UserService {
		 * 		    MailService mailService;
		 *
		 * 		    public UserService(@Autowired MailService mailService) {
		 * 		        this.mailService = mailService;
		 * 		    }
		 * 		    ...
		 * 		}
		 * 使用 Annotation 配合自动扫描能大幅简化Spring的配置，我们只需要保证：
		 *     1. 每个Bean被标注为 @Component 并正确使用 @Autowired 注入；
		 *     2. 配置类被标注为 @Configuration 和 @ComponentScan；
		 *     3. 所有Bean均在指定包以及子包内。
		 * 使用 @ComponentScan 非常方便，但是，我们也要特别注意包的层次结构。
		 * 通常来说，启动配置AppConfig位于自定义的顶层包（例如com.itranswarp.learnjava），其他Bean按类别放入子包。
		 *
		 *
		 * IoC容器--定制 Bean：
		 * 1. 对于Spring容器来说，当我们把一个Bean标记为 @Component 后，它就会自动为我们创建一个单例（Singleton），即
		 *    容器初始化时创建Bean，容器关闭前销毁Bean。
		 *    ===和IoC容器--IoC原理提到的： IoC容器要负责实例化所有的组件 遥相呼应===
		 *    ===springdb工程中提到的： ((ConfigurableApplicationContext) context).close() 大概就是说的容器关闭，===
		 *    ===进而发散到猜测 new AnnotationConfigApplicationContext(AppConfig.class) 大概就是容器的初始化，
		 * 	     根据IoC容器--装配Bean中的 ApplicationContext 段 和 小结内容来看，猜测完全正确===
		 *    在容器运行期间，我们调用getBean(Class)获取到的Bean总是同一个实例。
		 * 2. 还有一种Bean，我们每次调用getBean(Class)，容器都返回一个新的实例，这种Bean称为Prototype（原型），
		 *    它的生命周期显然和Singleton不同。声明一个Prototype的Bean时，需要添加一个额外的@Scope注解：
		 * 			@Component
		 * 			@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // @Scope("prototype")
		 * 			public class MailSession {
		 *     			...
		 * 			}
		 * 3. Spring对标记为@Bean的方法只调用一次，因此返回的Bean仍然是单例。
		 * 
		 * 定制 Bean 小节中的 注入List:
		 * 		有些时候，我们会有一系列接口相同，不同实现类的Bean。
		 * 		例如，注册用户时，我们要对email、password和name这3个变量进行验证。为了便于扩展，我们先定义验证接口：
		 * 			public interface Validator {
		 * 		    	void validate(String email, String password, String name);
		 * 			}
		 * 		然后，分别使用3个Validator对用户参数进行验证：
		 * 		  	@Component
		 * 			public class EmailValidator implements Validator {
		 * 			    public void validate(String email, String password, String name) {
		 * 			        if (!email.matches("^[a-z0-9]+\\@[a-z0-9]+\\.[a-z]{2,10}$")) {
		 * 			            throw new IllegalArgumentException("invalid email: " + email);
		 * 			        }
		 * 			    }
		 * 			}
		 *
		 * 			@Component
		 * 			public class PasswordValidator implements Validator {
		 * 			    public void validate(String email, String password, String name) {
		 * 			        if (!password.matches("^.{6,20}$")) {
		 * 			            throw new IllegalArgumentException("invalid password");
		 * 			        }
		 * 			    }
		 * 			}
		 * 		最后，我们通过一个Validators作为入口进行验证：
		 * 			@Component
		 * 			public class Validators {
		 * 			    @Autowired
		 * 			    List<Validator> validators;
		 * 		
		 * 			    public void validate(String email, String password, String name) {
		 * 			        for (var validator : this.validators) {
		 * 			            validator.validate(email, password, name);
		 * 			        }
		 * 			    }
		 * 			}
		 * 		注意到Validators被注入了一个List<Validator>，Spring会自动把所有类型为Validator的Bean装配为一个List注入进来，
		 * 		这样一来，我们每新增一个Validator类型，就自动被Spring装配到Validators中了，非常方便。
		 * 		因为Spring是通过扫描classpath获取到所有的Bean，而List是有序的，要指定List中Bean的顺序，可以加上@Order注解：
		 *
		 * 定制 Bean 小节中的 初始化和销毁:
		 * 		Spring容器会对上述Bean做如下初始化流程：
		 * 		    a.调用构造方法创建 MailService 实例；
		 * 		    b.根据@Autowired进行注入；
		 * 		    c.调用标记有 @PostConstruct 的 init() 方法进行初始化。
		 * 		而销毁时，容器会首先调用标记有@PreDestroy的shutdown()方法。 Spring只根据Annotation查找无参数方法，对方法名不作要求。
		 * 定制 Bean 小节中的 使用FactoryBean:
		 * 		当一个Bean实现了FactoryBean接口后，Spring会先实例化这个工厂，然后调用getObject()创建真正的Bean。
		 * 		getObjectType()可以指定创建的Bean的类型，因为指定类型不一定与实际类型一致，可以是接口或抽象类。
		 * 		如果定义了一个FactoryBean，要注意Spring创建的Bean实际上是这个FactoryBean的getObject()方法返回的Bean。
		 * 		为了和普通Bean区分，我们通常都以XxxFactoryBean命名。
		 *
		 * IoC容器--定制 Bean 的小结：
		 * 		Spring默认使用Singleton创建Bean，也可指定Scope为Prototype；
		 * 		可将相同类型的Bean注入List；
		 * 		可用@Autowired(required=false)允许可选注入；
		 * 		可用带@Bean标注的方法创建Bean；
		 * 		可使用@PostConstruct和@PreDestroy对Bean进行初始化和清理；
		 * 		相同类型的Bean只能有一个指定为@Primary，其他必须用@Quanlifier("beanName")指定别名；
		 * 			默认情况下，对一种类型的Bean，容器只创建一个实例。
		 * 			但有些时候，我们需要对一种类型的Bean创建多个实例。这个时候，需要给每个Bean添加不同的名字：===正好和定制Bean章节中的 Scope呼应===
		 *			a.可以用@Bean("name")指定别名，也可以用@Bean+@Qualifier("name")指定别名。
		 * 			b.还有一种方法是把其中某个Bean指定为@Primary;这样在注入时如果没有指出Bean的名字，Spring会注入标记有@Primary的Bean。
		 * 		注入时，可通过别名@Quanlifier("beanName")指定某个Bean；
		 * 		可以定义FactoryBean来使用工厂模式创建Bean。
		 * IoC容器--注入配置的小结：
		 * 		Spring容器可以通过@PropertySource自动读取配置，并以@Value("${key}")的形式注入；
		 * 		可以通过${key:defaultValue}指定默认值；
		 * 		以#{bean.property}形式注入时，Spring容器自动把指定Bean的指定属性值注入。
		 */

		// AppConfig标注了@Configuration，表示它是一个配置类，因为我们创建ApplicationContext时：
		// ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		// 使用的实现类是AnnotationConfigApplicationContext，必须传入一个标注了@Configuration的类名。
		// 此外，AppConfig还标注了@ComponentScan，
		// 它告诉容器，自动搜索当前类所在的包以及子包，把所有标注为@Component的Bean自动创建出来，并根据@Autowired进行装配。
		ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		UserService userService = context.getBean(UserService.class);
		User user = userService.login("bob@example.com", "password");
		System.out.println(user.getName());

		// 测试 IoC容器--使用Resource：
		AppService appService = context.getBean(AppService.class);
		appService.printLogo();
	}
}

