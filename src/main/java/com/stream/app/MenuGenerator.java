package com.stream.app;

import java.util.Objects;
import java.util.function.Function;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 需要往集合中添加多个对象，并且每个对象中(比如map或json object)需要添加多个key,value值时可以使用curry模式精简代码。
 * author:xumincha
 *
 */
public class MenuGenerator {

	/**
	 * 获取菜单集合
	 * 
	 * @return
	 */
	public JSONArray generateMenu() {
		JSONArray ja = new JSONArray();
		// 使用java8新特性和curry模式精简为几行代码
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8001", "目录管理",  "/catalog/getProductCatalog");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8002", "申请",     "/apply/getProductApply");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8003", "复核-运营", "/check/check?status=review1");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8013", "复核-风控", "/check/check?status=review2");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8023", "审批", 	  "/check/check?status=approve");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("801111", "发布", 	  "/check/publish");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8005", "费率配置", 	  "/rateconfig/creditLoan-index");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8006", "放款渠道配置",   "/getLendChannelConfig");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("8007", "合同配置",      "/contractConfig/gotoContractModelIndex");
		createTriFunction("mySystem","我的系统","ENABLED").andThen(ja::add).apply("800123", "后台管理",    "/job/index");
		
		return ja;
	}
	
	private TriFunction<String, String, String, ?> createTriFunction(String appCode,String appName,String enable) {
		return (code,name,path) -> {
			JSONObject obj = new JSONObject();
			obj.put("resourceCode", code);
			obj.put("resourceName", name);
			obj.put("resourcePath", path);
			obj.put("appCode", appCode);
			obj.put("appName", appName);
			obj.put("enable", enable);
			return obj;
		};
	}
	
	/**
	 * jdk1.8只提供了BiFunction,有时不满足业务需求，故创建 TriFunction
	 */
	public interface TriFunction<T,U,V,R> {
		
		R apply(T t, U u, V v);
		
		default <S> TriFunction<T,U,V,S> andThen(Function<? super R, ? extends S> after) {
			Objects.requireNonNull(after);
			return (t,u,v) -> after.apply(apply(t,u,v));
		}
	}
}
