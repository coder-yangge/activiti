package com.yang.activiti.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yang.activiti.model.Page;
import com.yang.activiti.service.WorkflowService;
import com.yang.activiti.vo.ResponseVo;
import com.yang.activiti.vo.ResponseVo.ResponseBuilder;

@Controller
@RequestMapping("/instance")
public class ProcessInstanceController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceController.class);
	
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private HistoryService HistoryService;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@RequestMapping("/toInstanceList")
	public String instancePage() {
		return "instance/instanceList";
	}
	
	@RequestMapping("/list")
	@ResponseBody
	public ResponseVo queryInstance(int page, int limit) {
		page = page == 1 ? 0 : page;
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		List<ProcessInstance> instances = query.listPage(page, limit);
		//ExecutionEntity entity = (ExecutionEntity)instances.get(0);
		return ResponseBuilder.buildSuccess(0, instances);
	}
	
	@RequestMapping("/listHistory")
	@ResponseBody
	public ResponseVo queryHistroyInstance(int page, int limit) {
		page = page == 1 ? 0 : page;
		HistoricProcessInstanceQuery query = HistoryService.createHistoricProcessInstanceQuery();
		List<HistoricProcessInstance> instances = query.listPage(page, limit);
		long count = query.count();
		return ResponseBuilder.buildSuccess(0, new Page(count, instances));
	}
	
	@RequestMapping(value = "/acquire/process/image/{id}")
	public void acquireInstanceDifiImage(@PathVariable("id") String processInstanceId, HttpServletResponse response) {
		
		OutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
			workflowService.getProcessInstanceImage(processInstanceId, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("server IOException {}", e.getMessage());
		} catch (ActivitiException e) {
			LOGGER.error("ActivitiException {}", e.getMessage());
		}
		
	}
	
	@RequestMapping(value = "/acquire/instance/image/{id}")
	public void acquireInstanceImage(@PathVariable("id") String processInstanceId, HttpServletResponse response) {
		
		OutputStream outputStream;
		try {
			outputStream = response.getOutputStream();
			workflowService.getProcessInstanceHighLightedImage(processInstanceId, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("server IOException {}", e.getMessage());
		} catch (ActivitiException e) {
			LOGGER.error("ActivitiException {}", e.getMessage());
		}
		
	}
	
	@RequestMapping(value = "/acquire/process/image/v/{processDefinitionId}/{id}")
	public void acquireInstanceImage2(@PathVariable("processDefinitionId") String processDefinitionId,
			@PathVariable("id") String processInstanceId, HttpServletResponse response) {
		OutputStream outputStream = null;
		InputStream input = null;
		try {
			outputStream = response.getOutputStream();
			HistoricProcessInstance historicProcessInstance = HistoryService.createHistoricProcessInstanceQuery()
			.processDefinitionId(processDefinitionId).processInstanceId(processInstanceId)
			.singleResult();
			input = repositoryService.getProcessDiagram(historicProcessInstance.getProcessDefinitionId());
			IOUtils.copy(input, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("server IOException {}", e.getMessage());
		} catch (ActivitiException e) {
			LOGGER.error("ActivitiException {}", e.getMessage());
		} finally {
			try {
				if (outputStream != null) {outputStream.close();}
				if (input != null) {input.close();}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	@RequestMapping("/suspend/{id}")
	@ResponseBody
	public ResponseVo suspend(@PathVariable("id") String processInstanceId) {
		try {
			runtimeService.suspendProcessInstanceById(processInstanceId);
		} catch (ActivitiObjectNotFoundException e) {
			LOGGER.error("Process instance :id={} not found", processInstanceId);
			return ResponseBuilder.buildSuccess("该流程实例未找到");
		} catch (ActivitiException e) {
			LOGGER.error("activiti execption :{}", e.getMessage());
			return ResponseBuilder.buildSuccess("系统异常，{}" + e.getMessage());
		}
		return ResponseBuilder.buildSuccess("操作成功");
	}
	
	@RequestMapping("/active/{id}")
	@ResponseBody
	public ResponseVo active(@PathVariable("id") String processInstanceId) {
		try {
			runtimeService.activateProcessInstanceById(processInstanceId);
		} catch (ActivitiObjectNotFoundException e) {
			LOGGER.error("Process instance :id={} not found", processInstanceId);
			return ResponseBuilder.buildSuccess("该流程实例未找到");
		} catch (ActivitiException e) {
			LOGGER.error("activiti execption :{}", e.getMessage());
			return ResponseBuilder.buildSuccess("系统异常，{}" +  e.getMessage());
		}
		return ResponseBuilder.buildSuccess("操作成功");
	}
	
}
