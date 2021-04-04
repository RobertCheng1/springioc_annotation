package com.company;


import com.company.service.User;
import com.company.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Configuration
@ComponentScan
public class AppConfig {

	@SuppressWarnings("resource")
	public static void main(String[] args) {

		/**
		 * 使用Spring的IoC容器，实际上就是通过类似XML这样的配置文件，把我们自己的Bean的依赖关系描述出来，然后让容器来创建并装配Bean。
		 * 一旦容器初始化完毕，我们就直接从容器中获取Bean使用它们。
		 * 使用XML配置的优点是所有的Bean都能一目了然地列出来，并通过配置注入能直观地看到每个Bean的依赖。
		 * 它的缺点是写起来非常繁琐，每增加一个组件，就必须把新的Bean配置到XML中。
		 * 有没有其他更简单的配置方式呢？？？
		 * 有！我们可以使用Annotation配置，可以完全不需要XML，让Spring自动扫描Bean并组装它们。
		 * 我们把上一节的示例改造一下，先删除XML配置文件，然后，给UserService和MailService添加几个注解。
		 *
		 * 使用Annotation配合自动扫描能大幅简化Spring的配置，我们只需要保证：
		 *     每个Bean被标注为@Component并正确使用@Autowired注入；
		 *     配置类被标注为@Configuration和@ComponentScan；
		 *     所有Bean均在指定包以及子包内。
		 * 使用 @ComponentScan 非常方便，但是，我们也要特别注意包的层次结构。
		 * 通常来说，启动配置AppConfig位于自定义的顶层包（例如com.itranswarp.learnjava），其他Bean按类别放入子包。
		 *
		 * From:定制 Bean
		 * 对于Spring容器来说，当我们把一个Bean标记为 @Component 后，它就会自动为我们创建一个单例（Singleton），即
		 * 容器初始化时创建Bean，容器关闭前销毁Bean。在容器运行期间，我们调用getBean(Class)获取到的Bean总是同一个实例。
		 * 还有一种Bean，我们每次调用getBean(Class)，容器都返回一个新的实例，这种Bean称为Prototype（原型），
		 * 它的生命周期显然和Singleton不同。声明一个Prototype的Bean时，需要添加一个额外的@Scope注解：
		 * 		@Component
		 * 		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // @Scope("prototype")
		 * 		public class MailSession {
		 *     		...
		 * 		}
		 * Spring对标记为@Bean的方法只调用一次，因此返回的Bean仍然是单例。
		 * 初始化和销毁:
		 * Spring容器会对上述Bean做如下初始化流程：
		 *     调用构造方法创建MailService实例；
		 *     根据@Autowired进行注入；
		 *     调用标记有@PostConstruct的init()方法进行初始化。
		 * 而销毁时，容器会首先调用标记有@PreDestroy的shutdown()方法。 Spring只根据Annotation查找无参数方法，对方法名不作要求。
		 * 使用FactoryBean:
		 * 当一个Bean实现了FactoryBean接口后，Spring会先实例化这个工厂，然后调用getObject()创建真正的Bean。
		 * getObjectType()可以指定创建的Bean的类型，因为指定类型不一定与实际类型一致，可以是接口或抽象类。
		 * 如果定义了一个FactoryBean，要注意Spring创建的Bean实际上是这个FactoryBean的getObject()方法返回的Bean。
		 * 为了和普通Bean区分，我们通常都以XxxFactoryBean命名。
		 */
		ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		UserService userService = context.getBean(UserService.class);
		User user = userService.login("bob@example.com", "password");
		System.out.println(user.getName());
	}
}
