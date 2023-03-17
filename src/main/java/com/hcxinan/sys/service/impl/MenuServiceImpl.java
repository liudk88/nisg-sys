package com.hcxinan.sys.service.impl;

import com.hcxinan.sys.mapper.MenuDao;
import com.hcxinan.sys.model.NisgMenu;
import com.hcxinan.sys.service.MenuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("menuServiceNisgImpl")
public class MenuServiceImpl implements MenuService {
    @Resource
    private MenuDao menuDao;

    public NisgMenu getMenuName(String id){
        NisgMenu menu = menuDao.selectById(id);

        return menu;
    }
}
