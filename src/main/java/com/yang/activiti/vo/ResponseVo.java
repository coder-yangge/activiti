package com.yang.activiti.vo;



public class ResponseVo {
	
	private int code;
	
	private String msg;
	
	private boolean result;
	
	private Object data;
	

	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	public boolean isResult() {
		return result;
	}


	public void setResult(boolean result) {
		this.result = result;
	}


	public Object getData() {
		return data;
	}


	public void setData(Object data) {
		this.data = data;
	}


	public static class ResponseBuilder {
		
		public static ResponseVo buildSuccess(int code, String msg, boolean result, Object data){
			ResponseVo responseVo = new ResponseVo();
			responseVo.setCode(code);
			responseVo.setMsg(msg);
			responseVo.setResult(result);
			responseVo.setData(data);
			return responseVo;
		}
		
		public static ResponseVo buildSuccess(int code, String msg, Object data){
			
			return buildSuccess(code, msg, true, data);
		}
		
		public static ResponseVo buildSuccess(int code, Object data){
			
			return buildSuccess(code, "", true, data);
		}
		
		public static ResponseVo buildSuccess(String msg, Object data){
			
			return buildSuccess(200, msg, true, data);
		}
		
		public static ResponseVo buildSuccess(Object data){
			
			return buildSuccess(200, "", true, data);
		}
		
		public static ResponseVo buildSuccess(String msg){
			
			return buildSuccess(200, msg, true, null);
		}
	}
}
