package com.hcxinan.sys.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_rms_menuinfo")
public class NisgMenu {
    @TableId
    private String cdid;

    private String pcdid;

    private String cdmc;
}
