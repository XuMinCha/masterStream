package com.stream.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stream.constant.AttachmentTypeCode;
import com.stream.constant.DocumentType;
import com.stream.model.AttachmentGroup;
import com.stream.model.Attachment;

/**
 * 使用stream操作生成attachments,主要使用curry模式抽象出方法，将众多参数从2个地方传入。
 * author:xumincha
 *
 */
public class AttachmentsGenerator 
{
    private Logger log = LoggerFactory.getLogger(getClass());

	 /**
              * 生成List<AppAttachmentGroup>
     *
     * @param jsonObj
     * @param ah
     * @return
     */
    public List<AttachmentGroup> generateAttachments(JSONObject jsonObj, String baNo) {
        List<AttachmentGroup> attachGroups = new ArrayList<AttachmentGroup>();

        JSONObject identityJson = jsonObj.getJSONObject("identityInfo");
        
        List<Attachment> identityAttaches = new ArrayList<Attachment>();
        AttachmentGroup identityGroup = new AttachmentGroup();
        identityGroup.setCheckDataType(AttachmentTypeCode.IDENTITY);
        identityGroup.setAttaches(identityAttaches);
        attachGroups.add(identityGroup);
        
        String sNo;
        try {
            sNo = UUID.randomUUID().toString();
            log.info("生成serialNo: " + sNo);
        } catch (Exception e) {
            log.error("生成serialNo:", e);
            throw new RuntimeException("生成主键值serialNo失败");
        }

        //Front page
        createAttachment(baNo,"前面","A000301").andThen(identityAttaches::add).apply(identityJson.getString("frontUrl"), AttachmentTypeCode.IDENTITY);
        //Back page
        createAttachment(baNo,"反面","A000301").andThen(identityAttaches::add).apply(identityJson.getString("backUrl"), AttachmentTypeCode.IDENTITY);
        
        JSONArray imgInfos = jsonObj.getJSONArray("imgInfos");
       
        List<String> policyMaterialTypes = Arrays.asList(DocumentType.LU_GRANT_IMG,DocumentType.LU_SCREENSHOT_IMG,DocumentType.POLICY_IMG,DocumentType.POLICY_APP_IMG);
        
        imgInfos.parallelStream().map(ele -> {
        	JSONObject obj = (JSONObject)ele;
        	String type = obj.getString("type");
        	String url = obj.getString("url");
        	if(DocumentType.EMPLOY_IMG.equalsIgnoreCase(type) || DocumentType.ENTERPRISE_IMG.equalsIgnoreCase(type)){
            	return createAttachment(baNo,"工作证明","").apply(url,AttachmentTypeCode.WORK_CERT);
        	}
        	else if(DocumentType.HOUSE_IMG.equalsIgnoreCase(type) || DocumentType.PROPERTY_IMG.equalsIgnoreCase(type)){
            	return createAttachment(baNo,type,"").apply(url,AttachmentTypeCode.RESIDENCE_INFO);
        	}
        	else if(DocumentType.CAR_IMG.equalsIgnoreCase(type) || DocumentType.IC_IMG.equalsIgnoreCase(type)){
            	return createAttachment(baNo,type,"").apply(url,AttachmentTypeCode.ASSET_INFO);
        	}
        	else if(DocumentType.SALARY_IMG.equalsIgnoreCase(type) || DocumentType.INCOME_IMG.equalsIgnoreCase(type) || DocumentType.FUND_IMG.equalsIgnoreCase(type)){
            	return createAttachment(baNo,type,"").apply(url,AttachmentTypeCode.SALARY_INFO);
        	}
        	else if(DocumentType.PBOC_IMG.equalsIgnoreCase(type)){
            	return createAttachment(baNo,"征信图片","").apply(url,AttachmentTypeCode.PBOC_INFO);
        	}
        	else if(policyMaterialTypes.contains(type)){
            	return createAttachment(baNo,"保单信息","").apply(url,AttachmentTypeCode.BAODAN_INFO);
        	}
        	else{
        		log.warn("imgInfos中的type:"+type+"，无法处理");
        		throw new RuntimeException("imgInfos中的type:"+type+"，无法处理");
        	}
        }).collect(Collectors.groupingBy(Attachment::getCheckDataType))
          .forEach((checkDataType,list) -> createAppAttachmentGroup(checkDataType).andThen(attachGroups::add).apply(list));

        return attachGroups;
    }
    
    /*private <T> Predicate<T> distinctBy(Function<? super T,?> extractor){
		Set<Object> set = ConcurrentHashMap.newKeySet();
		return t -> set.add(extractor.apply(t));
	}*/
    
    private Function<List<Attachment>,AttachmentGroup> createAppAttachmentGroup(String type){
    	return attachements -> {
    		 AttachmentGroup group = new AttachmentGroup();
    		 group.setCheckDataType(type);
    		 group.setAttaches(attachements);
    		 return group;
    	};
    }
    
    private BiFunction<String,String,Attachment> createAttachment(String objectNo,String desc,String subType){
    	return (url,type) -> {
    		Attachment attachment = new Attachment();
    		attachment.setObjectNo(objectNo);
    		attachment.setCheckDataType(type);
    		attachment.setSubType(subType);
    		attachment.setCustPageCount(1);
    		attachment.setIsMust(false);
        	attachment.setIsSupply(true);
        	attachment.setFileLocation(url);// 完整的路径
        	attachment.setCheckList(null);
        	attachment.setAuditingResult(null);
        	try {
        		attachment.setSerialNo(UUID.randomUUID().toString());
                log.info("生成主键值成功:" + attachment.getSerialNo());
            } catch (Exception e) {
                log.error("生成主键值失败"+e.getMessage(), e);
                throw new RuntimeException("生成主键值失败"+e.getMessage());
            }
        	return attachment;
    	};
    }
}
