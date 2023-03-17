package com.hcxinan.sys.attachment;

import com.hcxinan.core.inte.system.ICacheAttachmentManager;
import com.hcxinan.sys.constant.SysTables;
import com.morph.cond.Cond;
import com.morph.db.IDGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Service("diskAttachmentManager")
public class DiskAttachmentManager extends AttachmentManager implements ICacheAttachmentManager {
    @Override
    public boolean persistent(String fileToken){
        Map<String,Object> updateColumnAndValue=new HashMap<>();
        updateColumnAndValue.put("temp_flag",0);
        dao.updateByCondition(updateColumnAndValue, Cond.eq("file_token",fileToken));
        return true;
    }

    @Override
    public String[] upload(String fileToken, Map<String, byte[]> attachMap) {
        if(StringUtils.isBlank(fileToken)){
            fileToken= IDGenerator.getDateTimeId();
        }
        String[] resultArr=new String[attachMap.size()+1];
        resultArr[0]=fileToken;
        int index=1;
        SysAttachment[] sysAttachments=new SysAttachment[attachMap.size()];
        for(Map.Entry<String,byte[]> entry:attachMap.entrySet()){
            String aid= IDGenerator.getDateTimeId();
            SysAttachment attachment=new SysAttachment(aid,entry.getKey(),fileToken);
            attachment.setBytes(entry.getValue());
            attachment.setTemp_flag(true);//标识为临时文件，作用等价于标识为缓存
            attachment.setPath(uploadFolder+ File.separator+attachment.getAttachmentId());
            attachment.setCdate(new Date());
            resultArr[index]=aid;
            //加入到新增附件中
            sysAttachments[index-1]=attachment;
            index++;
        }
        addAttachments(sysAttachments);
        return resultArr;
    }

    @Override
    public boolean removeCacheFile(String attachmentId) {
        return removeAttachmentsById(attachmentId);//因为缓存实现基本和真正的存储一样，只是做了数据库标识，所以可以直接按真正的删除来删除
    }

    @Override
    public void clearCache(String fileToken) {
        //找出临时文件后删除
        List<SysAttachment> sysAttachments= dao.setBindTable(SysTables.SYS_ATTACHMENT).
                queryBeanList(SysAttachment.class, Cond.eq("FILE_TOKEN",fileToken).and(Cond.eq("TEMP_FLAG",1)),null,null);
        if(sysAttachments!=null && sysAttachments.size()>0){
            String[] attachmentIds=sysAttachments.stream().map(SysAttachment::getAttachmentId).toArray(String[]::new);
            removeAttachmentsById(attachmentIds);
        }
    }
}
