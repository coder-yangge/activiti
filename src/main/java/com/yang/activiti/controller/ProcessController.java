package com.yang.activiti.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.yang.activiti.model.Page;
import com.yang.activiti.vo.ProcessDefinationVo;
import com.yang.activiti.vo.ResponseVo;
import com.yang.activiti.vo.ResponseVo.ResponseBuilder;

@Controller
@RequestMapping("/process")
public class ProcessController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private RuntimeService runtimeService;
	
	
	@RequestMapping("/listPage")
	public String toProcessPage() {
		return "processDefination/processList";
	}

	@ResponseBody
	@RequestMapping("/list")
	public ResponseVo getProcessList(int page, int limit) {
		page = page == 1 ? 0 : page;
		long count = repositoryService.createProcessDefinitionQuery().count();
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().listPage(page, 10);	
		LOGGER.info("count:{}", count);
		List<ProcessDefinationVo> voList = new ArrayList<>();
		ProcessDefinationVo processDefinationVo = null;
		if (!list.isEmpty()) {
			for (ProcessDefinition processDefinition : list) {
				processDefinationVo = new ProcessDefinationVo();
				BeanUtils.copyProperties(processDefinition, processDefinationVo);
				voList.add(processDefinationVo);
			}
		}
		return ResponseBuilder.buildSuccess(0, new Page(count, voList));
	}
	
	@SuppressWarnings("deprecation")
	@RequestMapping("/acquireImage")
	public void acquireDefinationImage(String deploymentId, String resourceName, HttpServletResponse response) {
		if (StringUtils.isAnyBlank(deploymentId, resourceName)) {
			return;
		}
		resourceName = URLDecoder.decode(resourceName);
		LOGGER.info("reourceName:{}", resourceName);
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			inputStream = repositoryService.getResourceAsStream(deploymentId, resourceName);
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("IOException:{}", e.getMessage());
		} catch (ActivitiObjectNotFoundException e) {
			LOGGER.error("activiti process not found, check your request params");
		} finally {
			try {
				if (outputStream != null) {outputStream.close();}
				if (inputStream != null) {inputStream.close();}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@RequestMapping("/acquireImage2")
	public void acquireDefinationImage2(String deploymentId, String resourceName, HttpServletResponse response) {
		if (StringUtils.isAnyBlank(deploymentId, resourceName)) {
			return;
		}
		resourceName = URLDecoder.decode(resourceName);
		LOGGER.info("reourceName:{}", resourceName);
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			inputStream = new FileInputStream(resourceName);
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("IOException:{}", e.getMessage());
		} finally {
			try {
				if (outputStream != null) {outputStream.close();}
				if (inputStream != null) {inputStream.close();}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@RequestMapping("/start")
	@ResponseBody
	public ResponseVo startProcessInstance(String processDefinitionKey) {
		try {
			ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey);
			return ResponseBuilder.buildSuccess(200, instance.getId());
		} catch (ActivitiObjectNotFoundException  e) {
			return ResponseBuilder.buildSuccess("流程实例未找到，请检查");
		}
		
	}
	
	@RequestMapping("/suspend")
	@ResponseBody
	public ResponseVo suspendPorcess(@RequestParam(required = false) String processDefinitionKey,
			@RequestParam(required = true) String processDefinitionId,
			boolean suspendInstances) {
		StringBuilder msg = new StringBuilder("");
		try {
			if (StringUtils.isNotBlank(processDefinitionKey)) {
				repositoryService.suspendProcessDefinitionByKey(processDefinitionKey, suspendInstances, null);
			} else {
				repositoryService.suspendProcessDefinitionById(processDefinitionId, suspendInstances, null);
			} 
		} catch (ActivitiObjectNotFoundException e) {
			LOGGER.error("processDefinitionKey:{} or processDefinitionId:{} not found", processDefinitionId, processDefinitionKey);
			msg.append("流程未找到");
		} catch (ActivitiException e) {
			LOGGER.error("activiti execption:", e.getMessage());
			msg.append("操作失败，原因："+ e.getMessage());
		}
		msg.append(msg.length() > 0 ? "" : "操作成功");
		return ResponseBuilder.buildSuccess(msg.toString());
	}
	
	@RequestMapping("/active")
	@ResponseBody
	public ResponseVo activePorcess(@RequestParam(required = false) String processDefinitionKey,
			@RequestParam(required = true) String processDefinitionId,
			boolean suspendInstances) {
		StringBuilder msg = new StringBuilder("");
		try {
			if (StringUtils.isNotBlank(processDefinitionKey)) {
				repositoryService.activateProcessDefinitionByKey(processDefinitionKey, suspendInstances, null);
			} else {
				repositoryService.activateProcessDefinitionById(processDefinitionId, suspendInstances, null);
			} 
		} catch (ActivitiObjectNotFoundException e) {
			LOGGER.error("processDefinitionKey:{} or processDefinitionId:{} not found", processDefinitionId, processDefinitionKey);
			msg.append("流程未找到");
		} catch (ActivitiException e) {
			LOGGER.error("activiti execption:", e.getMessage());
			msg.append("操作失败，原因："+ e.getMessage());
		}
		msg.append(msg.length() > 0 ? "" : "操作成功");
		return ResponseBuilder.buildSuccess(msg.toString());
	}
}
