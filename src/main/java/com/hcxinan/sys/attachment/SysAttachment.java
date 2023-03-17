package com.hcxinan.sys.attachment;

import com.alibaba.fastjson.annotation.JSONField;
import com.hcxinan.core.inte.system.IAttachment;
import com.morph.annotation.IdType;
import com.morph.annotation.TableField;
import com.morph.annotation.TableName;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author liudk
 * @Description: 系统附件类
 * @date 21-9-10 下午5:08
 */
@Data
@TableName("SYS_ATTACHMENT")
public class SysAttachment implements IAttachment {
    /** 附件id */
    //表单组件ID
    @TableField(isPk = true,idType= IdType.DATETIME_ID)
    private String attachment_id ;
    /** 附加名称 */
    private String name ;
    /** 文件路径 */
    private String path ;
    /** 文件令牌 */
    private String file_token ;
    /** 文件大小 */
    private String size ;
    /** 创建人 */
    private String creator ;
    /** 创建时间 */
    private Date cdate ;
    /** 是否临时文件 */
    private Boolean temp_flag;
    @TableField(exist = false)
    private byte[] bytes;//文件的byte数组
    @TableField(exist = false)
    private String cacheType;//判断新增还是删除的标识（配合缓存使用）

    public SysAttachment() {
    }

    public SysAttachment(String attachmentId, String name) {
        this.attachment_id = attachmentId;
        this.name = name;
    }

    public SysAttachment(String attachmentId, String name, String file_token) {
        this.attachment_id = attachmentId;
        this.name = name;
        this.file_token = file_token;
    }


    @Override
    public String getAttachmentId() {
        return attachment_id;
    }

    @Override
    public String getAbsolutePath() {
        if(StringUtils.isBlank(AttachmentManager.ATTACHMENT_ROOT_PATH)){
            return System.getProperty("user.dir")+ File.separator+getPath();
        }else{
            return AttachmentManager.ATTACHMENT_ROOT_PATH+ File.separator+getPath();
        }
    }
    @JSONField(serialize=false)
    @Override
    public FileInputStream getInputStream(){
        try {
            return new FileInputStream(getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getFileToken() {
        return file_token;
    }

    @Override
    public String getUploader() {
        return getCreator();
    }

    @Override
    public Date getUploadTime() {
        return getCdate();
    }
}
