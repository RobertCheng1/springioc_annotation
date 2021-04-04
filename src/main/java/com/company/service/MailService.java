package com.company.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MailService {

	// IoC容器--定制 Bean 的可选注入:
	// 默认情况下，当我们标记了一个@Autowired后，Spring如果没有找到对应类型的Bean，它会抛出NoSuchBeanDefinitionException异常。
	// 可以给@Autowired增加一个required = false的参数：
	// 参考 AppConfig.java 中是否存在一个叫 ZoneId 的 Bean，如果有则以其值为准，如果没有则用系统的默认值
	@Autowired(required = false)
	ZoneId zoneId = ZoneId.systemDefault();

	public void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
	}

	// IoC容器--定制 Bean 的初始化和销毁:
	// 有些时候，一个Bean在注入必要的依赖后，需要进行初始化（监听消息等）。在容器关闭时，有时候还需要清理资源（关闭连接池等）。
	// 我们通常会定义一个init()方法进行初始化，定义一个shutdown()方法进行清理，然后，引入JSR-250定义的Annotation：
	@PostConstruct
	public void init() {
		System.out.println("In the MailService:Init mail service with zoneId = " + this.zoneId);
	}

	@PreDestroy
	public void shutdown() {
		System.out.println("In the MailService:Shutdown mail service");
	}

	public String getTime() {
		return ZonedDateTime.now(this.zoneId).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
	}

	public void sendLoginMail(User user) {
		System.err.println(String.format("Hi, %s! You are logged in at %s", user.getName(), getTime()));
	}

	public void sendRegistrationMail(User user) {
		System.err.println(String.format("Welcome, %s!", user.getName()));
	}
}

