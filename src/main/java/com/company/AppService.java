package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class AppService {

	// 如果注入的不是Bean，而是boolean、int、String这样的数据类型，则通过value注入， from:Spring开发--IoC容器--装配Bean
	@Value("1")
	private int version;

	// Spring提供了一个org.springframework.core.io.Resource（注意不是javax.annotation.Resource），
	// 它可以像String、int一样使用@Value注入  from：Spring开发--IoC容器--使用Resource
	@Value("classpath:/logo.txt")
	private Resource resource;

	private String logo;

	@PostConstruct
	public void init() throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			this.logo = reader.lines().collect(Collectors.joining("\n"));
		}
	}

	public void printLogo() {
		System.out.println(logo);
		System.out.println("app.version: " + version);
	}
}
