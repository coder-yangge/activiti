package com.yang.activiti.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yang.activiti.service.WorkflowService;

@Service
public class WorkflowServieImpl implements WorkflowService {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private ProcessEngine processEngine;

	@Override
	public void getProcessInstanceImage(String processInstanceId, OutputStream outputStream) {
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
//		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService
//				.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId())
//				.singleResult();
//		List<String> highLightedFlows = new ArrayList<String>();
//		List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
//				.processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
//		List<String> historicActivityInstanceList = new ArrayList<String>();
//		for (HistoricActivityInstance hai : historicActivityInstances) {
//			historicActivityInstanceList.add(hai.getActivityId());
//		}
//		List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
//		historicActivityInstanceList.addAll(highLightedActivities);
//		for (ActivityImpl activity : processDefinitionEntity.getActivities()) {
//			int index = historicActivityInstanceList.indexOf(activity.getId());
//			if (index >= 0 && index + 1 < historicActivityInstanceList.size()) {
//				List<PvmTransition> pvmTransitionList = activity.getOutgoingTransitions();
//				for (PvmTransition pvmTransition : pvmTransitionList) {
//					String destinationFlowId = pvmTransition.getDestination().getId();
//					if (destinationFlowId.equals(historicActivityInstanceList.get(index + 1))) {
//						highLightedFlows.add(pvmTransition.getId());
//					}
//				}
//			}
//		}
		ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
		ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
//		List<String> activeActivityIds = new ArrayList<String>();
//		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
//		for (org.activiti.engine.task.Task task : tasks) {
//			activeActivityIds.add(task.getTaskDefinitionKey());
//		}
		InputStream inputStream = diagramGenerator.generateDiagram(bpmnModel, "png", "宋体", "宋体", "宋体", null);
//		InputStream inputStream = diagramGenerator.generatePngDiagram(bpmnModel);
		try {
			IOUtils.copy(inputStream, outputStream);
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getProcessInstanceHighLightedImage(String proesssId, OutputStream outputStream) {
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(proesssId).singleResult();
		// 获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
		ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
		Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);

		ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
		ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService
				.getProcessDefinition(processInstance.getProcessDefinitionId());

		List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(proesssId).list();
		// 高亮环节id集合
		List<String> highLightedActivitis = new ArrayList<String>();

		// 高亮线路id集合
		List<String> highLightedFlows = getHighLightedFlows(definitionEntity, highLightedActivitList);

		for (HistoricActivityInstance tempActivity : highLightedActivitList) {
			String activityId = tempActivity.getActivityId();
			highLightedActivitis.add(activityId);
		}
		InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis,highLightedFlows,"宋体","宋体","宋体",null,1.0);
		try {
			IOUtils.copy(imageStream, outputStream);
			outputStream.close();
			imageStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

}
