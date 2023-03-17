package com.hcxinan.sys.service;

import com.hcxinan.core.inte.system.IRole;
import com.hcxinan.core.inte.system.ISysRuleMs;
import com.hcxinan.core.inte.system.IUser;
import com.hcxinan.core.inte.system.IUserService;
import com.hcxinan.sys.model.SysOrg;
import com.hcxinan.sys.model.SysRole;
import com.hcxinan.sys.model.SysUser;
import com.morph.cond.CondConnector;
import com.morph.cond.Condition;
import com.morph.cond.Cond;
import com.morph.cond.TableJoinCond;
import com.morph.db.IMoDao;
import com.morph.dml.AbsSelect;
import com.morph.dml.SelectSql;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liudk
 * @Description:
 * @date 21-9-24 下午7:07
 */
@Service
public class SysUserService implements IUserService {
    private IMoDao dao;
    private IMoDao dictDao;
    private IMoDao roleDao;

    @Autowired
    private ISysRuleMs sysRuleMs;

    @Override
    public List<? extends IUser> queryUsers(Condition userCond, Condition deptCond) {
        SelectSql userSql=new SelectSql("sys_user",userCond);
        SelectSql deptSql=new SelectSql("sys_dept",deptCond);
        AbsSelect query=userSql.lJoin(deptSql,TableJoinCond.eq("id",userSql,"dept_id"));
        List<Map> datas=dao.queryList(query);
        if(datas!=null){
            List<SysUser> users=datas.stream().map(m->{
                SysUser u=new SysUser();
                u.setId(MapUtils.getString(m,"ID"));
                u.setAccount(MapUtils.getString(m,"USERNAME"));
                return u;
            }).collect(Collectors.toList());
            return users;
        }
        return null;
    }

    @Override
    public List<SysUser> queryUsers(Condition condition) {
        return dao.queryBeanList(SysUser.class,condition,null,null);
    }

    @Override
    public List<IUser> getUsersById(String... userId) {
        return getUsers(Cond.in("id",userId),null,null);
    }

    @Override
    public List<IUser> getUsersByAccount(String... account) {
        List<IUser> users=getUsers(Cond.in("username",account),null,null);
        if(users!=null && !users.isEmpty()){
            String[] userIds=users.stream().map(IUser::getId).toArray(String[]::new);
            SelectSql roleQql=new SelectSql("SYS_ROLE").addQueryColumns("CODE","NAME");
            SelectSql userRoleQql=new SelectSql("SYS_USER_ROLE",Cond.in("USER_ID",userIds)).addQueryColumns("USER_ID");
            AbsSelect querySql=userRoleQql.iJoin(roleQql,TableJoinCond.eq("ID",userRoleQql,"ROLE_ID"));
            List<Map> roleDatas=dao.queryList(querySql);
            if(roleDatas!=null){
                Map<String,List<Map>> userIdFindRoles=roleDatas.stream().collect(Collectors.groupingBy(m->m.get("USER_ID")+""));
                for(IUser user:users){
                    List<Map> roleMaps=userIdFindRoles.get(user.getId());
                    if(roleMaps!=null){
                        List<IRole> roles=new ArrayList<>();
                        for(Map mapRole:roleMaps){
                            String roleId= (String) mapRole.get("CODE");
                            String roleName= (String) mapRole.get("NAME");
                            SysRole sysRole=new SysRole();
                            sysRole.setRoleId(roleId);
                            sysRole.setRoleName(roleName);
                            roles.add(sysRole);
                        }
                        user.setRoles(roles);
                    }
                }
            }
        }
        return users;
    }

    @Override
    public List<IUser> getUsersByRoleId(String... roleId) {
        /*
        * 因为sys_role有主键id和角色code，但平台目前不能确定所有的是否统一采用了code作为表示，所以参数roleId不能确定含义是那个
        * 为了兼容，只能两个都查询
        * */
        SelectSql querySql=new SelectSql("SYS_ROLE",Cond.in("code",roleId));
        querySql.addQueryColumns("ID");
        List<Map> roles=roleDao.queryList(querySql);
        List<String> roleIds=new ArrayList<>();
        if(roles!=null && !roles.isEmpty()){
            roleIds=roles.stream().map(map->map.get("ID").toString()).collect(Collectors.toList());
        }
        for(String rid:roleId){
            roleIds.add(rid);
        }
        return getUsers(null,Cond.in("role_id",roleIds.toArray(new String[roleIds.size()])),null);
    }

    @Override
    public List<IUser> getUsersByOrgId(String... orgId) {
        return getUsers(null,null,Cond.in("id",orgId));
    }

    @Transactional
    @Override
    public void updateUser(Map<String, Object> map) {
        dao.updateByPk(map);
        Condition cond=Cond.eq("CODE","SYSCONFIG").and(Cond.eq("SUBCODE","offlineInit"));
        List<Map> dicts = dictDao.queryList(cond, null, null);
        if (dicts != null && dicts.size() > 0) {
            if ("1".equals(dicts.get(0).get("CNAME"))) { //1表示系统是未初始化的离线端
                Map<String, Object> updateDictData = new HashMap<>();
                updateDictData.put("CNAME", "0");
                dictDao.updateByCondition(updateDictData, cond);
                //重载是否初始化离线端
                sysRuleMs.reload("SYSCONFIG");
            }
        }
    }

    private List<IUser> getUsers(Condition userCond, Condition roleCond, Condition deptCond){
        SelectSql userSql=new SelectSql("sys_user", Cond.eq("mark",1).and(Cond.eq("enabled",1)));
        userSql.addQueryColumns("ID USERID","REALNAME NAME","USERNAME ACCOUNT","EMAIL","MOBILE","DEPT_ID ORGID","PARAM1","PARAM2","PARAM3","PASSWORD");
        if(userCond!=null){
            userSql.addConditoin(userCond, CondConnector.AND);
        }
        AbsSelect sql=userSql;
        if(roleCond!=null){
            SelectSql userRoleSql=new SelectSql("sys_user_role");
            userRoleSql.addConditoin(roleCond, CondConnector.AND);
            //todo:这样写会限制死用户的角色范围必须在查询角色条件里的，以后优化
            SelectSql roleSql=new SelectSql("sys_role");
            roleSql.addQueryColumns("NAME ROLENAME","CODE ROLEID");
            sql=userSql.lJoin(userRoleSql, TableJoinCond.eq("user_id",userSql,"id"));
            sql=sql.lJoin(roleSql,TableJoinCond.eq("id",userRoleSql,"role_id"));
        }
        SelectSql deptSql=new SelectSql("sys_dept");
        deptSql.addQueryColumns("ID DEPTID","NAME DEPTNAME");
        if(deptCond!=null){
            deptSql.addConditoin(deptCond, CondConnector.AND);
        }

        sql=sql.lJoin(deptSql,TableJoinCond.eq("id",userSql,"dept_id"));
        List<Map> usermaps=dao.queryList(sql);
        if(usermaps!=null){
            /*
            * 因为一个用户可能存在多个角色的情况，所有当用角色管理的时候，用户数据是会重复的
            * */
            Map<String,SysUser> mapIdToUser=new HashMap<>();//记录用户id到用户的映射关系
            List<IUser> users=new ArrayList<>();
            for(Map map:usermaps){
                String userId=MapUtils.getString(map,"USERID");
                String roleName=MapUtils.getString(map,"ROLENAME");
                String roleId=MapUtils.getString(map,"ROLEID");
                SysRole sysRole=new SysRole();
                sysRole.setRoleId(roleId);
                sysRole.setRoleName(roleName);
                SysUser hasSys=mapIdToUser.get(userId);
                if(hasSys!=null){//说明之前已经有了，那么就不需要重新构建，只需要加入角色即可
                    hasSys.getRoles().add(sysRole);
                    continue;
                }
                List<IRole> roles=new ArrayList<>();
                roles.add(sysRole);
                SysUser user=new SysUser();
                user.setRoles(roles);
                user.setId(userId);
                user.setName(MapUtils.getString(map,"NAME"));
                user.setAccount(MapUtils.getString(map,"ACCOUNT"));
                user.setEmail(MapUtils.getString(map,"EMAIL"));
                user.setOrgId(MapUtils.getString(map,"ORGID"));
                user.setPassword(MapUtils.getString(map,"PASSWORD"));
                String mobile=MapUtils.getString(map,"MOBILE");              
                if(mobile==null){
                    user.setTels(new ArrayList<>());
                }else{
                    user.setTels(Arrays.asList(mobile));
                }
                user.put("param1",MapUtils.getString(map,"PARAM1"));
                user.put("param2",MapUtils.getString(map,"PARAM2"));
                user.put("param3",MapUtils.getString(map,"PARAM3"));

                SysOrg org=new SysOrg();
                org.setId(MapUtils.getString(map,"DEPTID"));
                org.setName(MapUtils.getString(map,"DEPTNAME"));

                user.setOrg(org);
                users.add(user);
            }
            return users;
        }else{
            return null;
        }
    }
    @Autowired
    public void setDao(IMoDao dao) {
        this.dao = dao;
        this.dao.setBindTable(()->"sys_user");
    }
    @Autowired
    public void setDictDao(IMoDao dictDao) {
        this.dictDao = dictDao;
        this.dictDao.setBindTable(()->"sys_dict_data");
    }
    @Autowired
    public void setRoleDao(IMoDao roleDao) {
        this.roleDao = roleDao;
        this.roleDao.setBindTable(()->"sys_role");
    }
}
