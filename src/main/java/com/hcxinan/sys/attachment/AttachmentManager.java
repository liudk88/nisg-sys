package com.hcxinan.sys.attachment;

import com.hcxinan.core.inte.system.*;
import com.hcxinan.sys.constant.SysTables;
import com.morph.cond.Cond;
import com.morph.db.IDGenerator;
import com.morph.db.IMoDao;

import com.morph.model.OrderBy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * @author liudk
 * @Description：：
 * @date 21-9-10 下午5:58
 */
@Service("attachmentManager")
public class AttachmentManager implements IAttachmentManager {
    @Autowired
    private ISysUtil sysUtil;
    @Autowired
    private ISysRuleMs ruleMs;

    protected IMoDao dao;

    public static File hcuploads=null;//存放系统附件的目录

    public static String ATTACHMENT_ROOT_PATH = "";

    protected String uploadFolder = "hcuploads";

    @Autowired
    public void setDao(IMoDao dao) {
        this.dao = dao;
        dao.setBindTable(SysTables.SYS_ATTACHMENT);
    }
    @PostConstruct
    public void init(){
        if(ruleMs!=null){
            ATTACHMENT_ROOT_PATH=Optional.ofNullable(ruleMs.getRule("SYSCONFIG","attachmentPath")).
                    map(r->r.getVal()).orElse("");

            if(StringUtils.isNoneBlank(ATTACHMENT_ROOT_PATH)){
                hcuploads=new File(ATTACHMENT_ROOT_PATH+File.separator+uploadFolder);
            }
        }
        if(hcuploads==null){
            hcuploads=new File("hcuploads");
        }
        if(!hcuploads.exists()){
            hcuploads.mkdirs();
        }
    }

    @Override
    public String[] addAttachments(String[] flieNames, InputStream[] inputStreams) throws IOException {
        String fileToken=IDGenerator.getDateTimeId();
        String[] attIds=addAttachmentsToFileToken(fileToken,flieNames,inputStreams);
        String[] result=new String[inputStreams.length+1];
        result[0]=fileToken;
        System.arraycopy(attIds, 0, result, 1, inputStreams.length);
        return result;
    }

    @Override
    public String[] addAttachments(IAttachment... sysAttachment){
        IUser loginUser=null;
        if(sysUtil!=null){
            loginUser=sysUtil.getLoginUser();
        }
        List<SysAttachment> sysAttachments=new ArrayList<>();
        if(sysAttachment!=null && sysAttachment.length>0){
            for(IAttachment iatt:sysAttachment){
                SysAttachment att= (SysAttachment) iatt;
                if(loginUser!=null)att.setCreator(loginUser.getId()+"");
                File saveFile=new File(hcuploads,att.getAttachmentId());
                try {
                    FileOutputStream fout = new FileOutputStream(saveFile);
                    fout.write(att.getBytes());
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                att.setSize(FileUtils.byteCountToDisplaySize(saveFile.length()));
                sysAttachments.add(att);
            }
            return (String[]) dao.setBindTable(SysTables.SYS_ATTACHMENT).insert(SysAttachment.class, sysAttachments);
        }
        return null;
    }

    @Override
    public String[] addAttachmentsToFileToken(String fileToken,String[] flieNames, InputStream... inputStreams) throws IOException {
        IUser loginUser=null;
        if(sysUtil!=null){
            loginUser=sysUtil.getLoginUser();
        }
        SysAttachment[] attachments=new SysAttachment[inputStreams.length];
        int i=0;
        for(InputStream in:inputStreams){
            String aid= IDGenerator.getDateTimeId();
            SysAttachment attachment=new SysAttachment(aid,flieNames[i],fileToken);
            attachment.setCacheType("add");
            attachment.setCdate(new Date());
            if(loginUser!=null){
                attachment.setCreator(loginUser.getId());
            }
            attachment.setTemp_flag(false);//非临时文件
            attachment.setPath(uploadFolder+ File.separator+attachment.getAttachmentId());
            File saveFile=new File(hcuploads,attachment.getAttachmentId());
            FileOutputStream fout=new FileOutputStream(saveFile);
            StreamUtils.copy(in,fout);
            attachment.setSize(FileUtils.byteCountToDisplaySize(saveFile.length()));
            attachments[i++]=attachment;
        }
        return (String[]) dao.setBindTable(SysTables.SYS_ATTACHMENT).insert(SysAttachment.class, Arrays.asList(attachments));
    }

    @Override
    public boolean removeAttachmentsByToken(String... fileToken) {
        List<SysAttachment> sysAttachments= sysAttachments = dao.setBindTable(SysTables.SYS_ATTACHMENT).
                queryBeanList(SysAttachment.class, Cond.in("FILE_TOKEN",fileToken),null,null);
        if(sysAttachments!=null && sysAttachments.size()>0){
            String[] attachmentIds=sysAttachments.stream().map(SysAttachment::getAttachmentId).toArray(String[]::new);
            return removeAttachmentsById(attachmentIds);
        }
        return false;
    }

    @Override
    public boolean removeAttachmentsById(String... attachmentId) {
        dao.setBindTable(SysTables.SYS_ATTACHMENT).deleteByPk(attachmentId);
        for(String aid:attachmentId){
            File file=new File(hcuploads,aid);
            if(file.isFile()){
                file.delete();
            }
        }
        return true;
    }

    @Override
    public List<IAttachment> getAttachmentsByToken(String fileToken) {
        return dao.setBindTable(SysTables.SYS_ATTACHMENT).queryBeanList(SysAttachment.class
                , Cond.eq("FILE_TOKEN",fileToken).and(Cond.eq("TEMP_FLAG",0))
                ,new OrderBy().asc("CDATE"),null);
    }

    @Override
    public List<IAttachment> getAttachmentsByTokens(String... fileToken) {
        return dao.setBindTable(SysTables.SYS_ATTACHMENT).queryBeanList(SysAttachment.class
                , Cond.in("FILE_TOKEN",fileToken).and(Cond.eq("TEMP_FLAG",0))
                ,new OrderBy().asc("CDATE"),null);
    }

    @Override
    public IAttachment getAttachmentById(String attachmentId) {
        return dao.setBindTable(SysTables.SYS_ATTACHMENT).select(SysAttachment.class,attachmentId);
    }

    @Override
    public void mergeFileToken(String toFileToken, String... mergedFileTokens) {
        Map<String,Object> updateColumnAndValue=new HashMap<>();
        updateColumnAndValue.put("FILE_TOKEN",toFileToken);
        dao.updateByCondition(updateColumnAndValue,Cond.in("FILE_TOKEN",mergedFileTokens));
    }
}
