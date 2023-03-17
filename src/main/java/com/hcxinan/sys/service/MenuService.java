package com.hcxinan.sys.service;

import com.hcxinan.sys.mapper.MenuDao;
import com.hcxinan.sys.model.NisgMenu;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


public interface MenuService {
    NisgMenu getMenuName(String id);
}
