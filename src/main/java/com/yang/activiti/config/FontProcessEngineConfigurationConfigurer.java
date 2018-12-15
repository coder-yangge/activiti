package com.yang.activiti.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FontProcessEngineConfigurationConfigurer implements ProcessEngineConfigurationConfigurer{

	private static final Logger LOGGER = LoggerFactory.getLogger(FontProcessEngineConfigurationConfigurer.class);
	
	@Override
	public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
		// TODO Auto-generated method stub
		processEngineConfiguration.setActivityFontName("宋体");
		processEngineConfiguration.setLabelFontName("宋体");
		processEngineConfiguration.setAnnotationFontName("宋体");
		LOGGER.warn("activiti font has been set :{}", "宋体");
	}

}
