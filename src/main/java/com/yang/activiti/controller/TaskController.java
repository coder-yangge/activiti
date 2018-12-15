package com.yang.activiti.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yang.activiti.vo.ResponseVo;
import com.yang.activiti.vo.ResponseVo.ResponseBuilder;
import com.yang.activiti.vo.TaskVo;

@RequestMapping("/task")
@Controller
public class TaskController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private HistoryService historyService;
	
	@RequestMapping("/list")
	public String task(String processInstanceId, ModelMap model) {
		model.addAttribute("processInstanceId", processInstanceId);
		return "task/taskList";
	}

	@RequestMapping("/query/{processInstanceId}")
	@ResponseBody
	public ResponseVo queryTask(@PathVariable("processInstanceId") String processInstanceId) {
		
		List<Task> tasks = taskService.createTaskQuery()
		.processInstanceId(processInstanceId).list();
		List<TaskVo> taskVos = new ArrayList<>();
		TaskVo vo = null;
		if (tasks !=null && !tasks.isEmpty()) {
			for (Task task : tasks) {
				vo = new TaskVo();
				BeanUtils.copyProperties(task, vo);
				taskVos.add(vo);
			}
		}
		return ResponseBuilder.buildSuccess(0, taskVos);
	}
	
	@RequestMapping("/query/tasks/{processInstanceId}")
	@ResponseBody
	public ResponseVo queryTasks(@PathVariable("processInstanceId") String processInstanceId) {
		
//		List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
//				.processInstanceId(processInstanceId)
//				.list();
		List<HistoricActivityInstance> activitiList = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId)
				.list();
		return ResponseBuilder.buildSuccess(0, activitiList);
	}
	
	@RequestMapping("/complete")
	@ResponseBody
	public ResponseVo complete(@RequestParam(required = true) String taskId,
			@RequestParam(required = false)String variables) {
		
		variables = Optional.ofNullable(variables).orElse("");
		try {
			if (StringUtils.isBlank(variables)) {
				taskService.complete(taskId);
			} else {
				@SuppressWarnings("unchecked")
				Map<String, Object> variable = JSON.parseObject(variables, HashMap.class);
				taskService.complete(taskId, variable);
			} 
		} catch (ActivitiObjectNotFoundException e) {
			return ResponseBuilder.buildSuccess("taskId:{} 未找到" + taskId);
		} catch (JSONException e) {
			LOGGER.error("参数错误，variables:{}", variables);
			return ResponseBuilder.buildSuccess("参数错误");
		}
		return ResponseBuilder.buildSuccess("操作成功");
	}
	
}
