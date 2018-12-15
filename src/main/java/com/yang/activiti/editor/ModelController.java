package com.yang.activiti.editor;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yang.activiti.model.Page;
import com.yang.activiti.vo.ResponseVo;
import com.yang.activiti.vo.ResponseVo.ResponseBuilder;

@Controller
@RequestMapping("/model")
public class ModelController {

	private static final Logger log = LoggerFactory.getLogger(ModelController.class);

	@Autowired
	ProcessEngine processEngine;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	private RepositoryService repositoryService;

	/**
	 *  新建一个空模型
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/create")
	public void newModel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String modelName = "modelName";
			String modelKey = "modelKey";
			String description = "description";
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode editorNode = objectMapper.createObjectNode();
			editorNode.put("id", "canvas");
			editorNode.put("resourceId", "canvas");
			ObjectNode stencilSetNode = objectMapper.createObjectNode();
			stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
			editorNode.put("stencilset", stencilSetNode);
			Model modelData = repositoryService.newModel();
			ObjectNode modelObjectNode = objectMapper.createObjectNode();
			modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, modelName);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
			modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
			modelData.setMetaInfo(modelObjectNode.toString());
			modelData.setName(modelName);
			modelData.setKey(modelKey); // 保存模型
			repositoryService.saveModel(modelData);
			repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
			response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
		} catch (Exception e) {
			log.error("创建流程失败，{}", e);
		}
	}
	 /**
	  *   发布模型为流程定义
      */
    @RequestMapping("/deploy")
    @ResponseBody
    public ResponseVo deploy(String modelId) throws Exception {

        //获取模型
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return ResponseBuilder.buildSuccess("模型数据为空，请先设计流程并成功保存，再进行发布。");
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return ResponseBuilder.buildSuccess( "数据模型不符要求，请至少设计一条主线流程。");
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        //repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId());
        return ResponseBuilder.buildSuccess("发布成功， 部署ID:" + deployment.getId());
    }
    
    /**
     * model list
     * @return
     */
    @ResponseBody
    @RequestMapping("/list")
    public ResponseVo QueryModel(boolean deployed, int page, int limit) {
    	page = page == 1 ? 0 : page;
    	ModelQuery modelQuery = repositoryService.createModelQuery();
    	if (deployed) {
    		modelQuery = modelQuery.deployed();
    	} else {
    		modelQuery = modelQuery.notDeployed();
    	}
    	List<Model> models = modelQuery.listPage(page, limit);
    	long count = modelQuery.count();
    	return ResponseBuilder.buildSuccess(0, new Page(count, models));
    }
    
    @RequestMapping("/modelmanage")
    public String toModelList() {
    	return "model/modelMange";
    }
    
    /**
     * 无用
    @RequestMapping("/acquire/image/{modelId}")
    public void acquieModelImage(@PathVariable("modelId") String modelId) {
    	byte[] bytes = repositoryService.getModelEditorSource(modelId);
    	if (bytes == null || bytes.length == 0) {
    		log.warn("modelId:{}, source is null", modelId);
    	}
    	String modelXml = new String(bytes);
    	log.info(modelXml);
    }
     */
}
