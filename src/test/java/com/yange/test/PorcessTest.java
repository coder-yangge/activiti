package com.yange.test;

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.yang.ActivitiApplication;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = ActivitiApplication.class)
public class PorcessTest {
	

	@Autowired
	private TaskService taskService;
	
	@Autowired
	private RuntimeService runtimeService;
	
	public static final String process = "leiren_process";
	
	@Test
	public void test() {
		
		System.out.println("======start a process instance======");
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(process);
		System.out.println("======process start, processInstance id :" + processInstance.getId());
		System.out.println();
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		while (task != null) {
			System.out.println("==========task===========");
			System.out.println("taskID:" + task.getId());
			System.out.println("taskName:" + task.getName());
			System.out.println("startTime:" + task.getCreateTime().toString());
			System.out.println("==========task===========");
			taskService.complete(task.getId());
			System.out.println("==========completed task =========" );
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.singleResult();
		}
		
		System.out.println("===========process instance " + processInstance.getId() + " finished");
	}
}
