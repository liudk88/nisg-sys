package com.hcxinan.sys.trigger;

import com.hcxinan.core.inte.system.ISysRuleMs;
import com.morph.db.ITrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class DeptTrigger implements ITrigger<Map<String, Object>> {
    @Autowired
    private ISysRuleMs sysRuleMs;

    @Override
    public Object doBeforeInsert(List<Map<String, Object>> datas) {
        return null;
    }

    @Override
    public Object doAfterInsert(List<Map<String, Object>> datas) {
        sysRuleMs.reload("ORGS");
        return null;
    }

    @Override
    public Object doBeforeUpdate(List<Map<String, Object>> datas) {
        return null;
    }

    @Override
    public Object doAfterUpdate(List<Map<String, Object>> datas) {
        sysRuleMs.reload("ORGS");
        return null;
    }

    @Override
    public Object doBeforeDelete(Serializable... ids) {
        return null;
    }

    @Override
    public Object doAfterDelete(Serializable... ids) {
        return null;
    }

    @Override
    public Object doBeforeSelect(Serializable id) {
        return null;
    }

    @Override
    public Object doAfterSelect(Map<String, Object> data) {
        return null;
    }

    @Override
    public Function<String, Object> doBeforeQuery(List<String> selfQueryCols) {
        return null;
    }

    @Override
    public Object doAfterQuery(List<Map> datas) {
        return null;
    }
}
