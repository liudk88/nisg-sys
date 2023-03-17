package com.hcxinan.sys.power;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hcxinan.core.inte.system.*;
import com.hcxinan.core.inte.util.ITree;
import com.hcxinan.core.util.TreeData;
import com.hcxinan.sys.cache.CacheManager;
import com.hcxinan.sys.model.SysDataPower;
import com.hcxinan.sys.model.SysOrg;
import com.hcxinan.sys.service.IDataPowerBussinessService;
import com.hcxinan.sys.util.Constant;
import com.hcxinan.sys.vo.SysDataPowerVo;
import com.morph.cond.*;
import com.morph.db.IDGenerator;
import com.morph.db.IMoDao;
import com.morph.dml.AbsSelect;
import com.morph.dml.SelectSql;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service("dataPowerService")
public class DataPowerService implements IDataPowerService,IDataPowerBussinessService {
    private IMoDao powerDao;
    
    private IMoDao regionDao;

    private IMoDao deptDao;

    @Autowired
    private IMoDao dao;
    
    @Autowired
    private IUserService userService;
    
    @Autowired(required = false)
    private ISysUtil sysUtil;
    
    @Autowired(required = false)
    private ISysRuleMs sysRuleMs;


    @Override
    public boolean hasAllPower(IUser powerUser, String module) {
        SelectSql dataPowerQuery=new SelectSql("SYS_DATA_POWER", Cond.in("BTYPE",new String[]{module,"all"}).and
                (Cond.eq("POBJ","all")));
        List<SysDataPower> datas=powerDao.queryBeanList(SysDataPower.class,dataPowerQuery);
        if(datas!=null && datas.size()>0){//有所有权限的设置情况
            Set curRoleSet=powerUser.getRoles().stream().map(IRole::getRoleId).collect(Collectors.toSet());
            for(SysDataPower powerData:datas){
                if(powerData.getOwer().equals(powerUser.getOrgId()) || powerData.getOwer().equals(powerUser.getId()) ||curRoleSet.contains(powerData.getOwer())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Map<IUser,Integer> getUserScope(IUser powerUser, String module) {
        Map<IOrg,Integer> orgIntegerMap=getOrgScope(powerUser,module);
        Map<IUser,Integer> userScopeMap=new HashMap<>();
        if(orgIntegerMap!=null){
            String[] powerOrgids=orgIntegerMap.keySet().stream().map(IOrg::getOrgId).toArray(String[]::new);
            List<IUser> users=userService.getUsersByOrgId(powerOrgids);
            //todo: 暂时没有考虑对用户单独权限设置时的权限合并情况
            Map<String,Integer> orgidPvalMap=orgIntegerMap.entrySet().stream().collect(Collectors.toMap(entry->entry.getKey().getOrgId(),Map.Entry::getValue));
            users.forEach(u->{
                Integer orgPowerVal=orgidPvalMap.get(u.getOrgId());
                if(orgPowerVal!=null){
                    userScopeMap.put(u,orgPowerVal);
                }
            });
        }
        userScopeMap.put(powerUser,7);//对自己的数据拥有所有权限
        return userScopeMap;
    }

    @Override
    public Map<IOrg,Integer> getOrgScope(IUser powerUser, String module) {
        /*
         * 数据权限控制
         *  A.角色控制单位。角色->控制的单位
         *  B.单位控制区。（区县单位）单位->区->区的单位
         *  C.用户对单位。用户->控制的单位
         *
         *  根据当前登陆用户，分别以其帐号、所属单位、关联的角色对应上面的点，可以得到当前用户可以控制的单位
         *  通过用户范围查找其对应的业务数据
         * */
        DataPower dataPower=getDataPower(powerUser,module);
        Condition cond=Cond.eq("mark",1);
        if(!dataPower.isHasAllPower()){//不是拥有所有权限
            List<String> powerRegions=dataPower.getPowerRegions();
            Condition powerCond=null;
            BiFunction<Condition,Condition,Condition> condFun=(cond1, cond2)->{
                if(cond1==null){
                    return cond2;
                }else{
                    return cond1.or(cond2);
                }
            };
            if(powerRegions!=null && powerRegions.size()>0){
                powerCond=condFun.apply(powerCond,Cond.in("AREA", powerRegions.toArray(new String[powerRegions.size()])));
            }
            List<String> powerOrgs=dataPower.getPowerOrgs();
            if(powerOrgs!=null && powerOrgs.size()>0){
                powerCond=condFun.apply(powerCond,Cond.in("ID", powerOrgs.toArray(new String[powerOrgs.size()])));
            }
            List<String> userList=dataPower.getPowerUsers();
            if(userList!=null && userList.size()>0){
                powerCond=condFun.apply(powerCond,Cond.in("CREATE_USER", userList.toArray(new String[userList.size()])));
            }
            cond=cond.and(powerCond);
        }
        List<Map> datas= deptDao.queryList(cond,null,null);
        if(datas!=null && datas.size()>0){
            Map<IOrg,Integer> result=new HashMap<>();
            for(Map m:datas){
                SysOrg sysOrg=new SysOrg();
                sysOrg.setId(m.get("ID")+"");
                sysOrg.setName(m.get("NAME")+"");
                result.put(sysOrg,7);//目前默认放开给最高权限
            }
            return result;
        }
        return null;
    }

    @Override
    public Map<String, Integer> getRegionScope(IUser powerUser, String module) {
        /*
        * 数据权限控制,
        * 用户控制区、角色控制区、单位控制区
        * */
        Object[] owerArr=getOwersByUser(powerUser);

        String[] ptypes=new String[]{PowerType.user_region.toString(),PowerType.role_region.toString(),PowerType.org_region.toString()};

        List<Map> allPowerDatas= powerDao.queryList(Cond.eq("BTYPE",module).and(
                Cond.in("POWER_TYPE",ptypes).and(Cond.in("OWER",owerArr))),null,null);
        if(allPowerDatas!=null && allPowerDatas.size()>0){
            Map<String,Integer> regionScopeMap=new HashMap<>();
            allPowerDatas.forEach(m->{
                String region= (String) m.get("POBJ");
                Integer cpower=0;
                Object pv= m.get("PVAL");
                if(pv instanceof Byte){//兼容h2、达梦数据库
                    cpower=((Byte)pv).intValue();
                }else{
                    cpower= (Integer) m.get("PVAL");
                }
                if(regionScopeMap.containsKey(region)){
                    Integer hasPower=regionScopeMap.get(region);
                    regionScopeMap.put(region,hasPower | cpower);
                }else{
                    regionScopeMap.put(region,cpower);
                }
            });
            return regionScopeMap;
        }
        return null;
    }

    public DataPower getDataPower(IUser user, String module) {
        DataPower dataPower=new DataPower();

        Object[] owerArr=getOwersByUser(user);

        SelectSql queryDML=new SelectSql("sys_data_power",
                Cond.in("BTYPE",new String[]{module,"all"}).and(Cond.in("OWER",owerArr)));
        List<Map> powerDatas= dao.queryList(queryDML);

        if(powerDatas!=null && powerDatas.size()>0){//有权限
            Set<String> orgPowers=new HashSet<>();
            Set<String> userPowers=new HashSet<>();
            Iterator<Map> iterator=powerDatas.iterator();
            boolean selfOrgPower=false;//判断是否能看本单位数据
            while (iterator.hasNext()){
                Map cpower=iterator.next();
                String pobj= (String) cpower.get("POBJ");
                if(StringUtils.isNotEmpty(pobj)){
                	if("all".equals(pobj)){//拥有全部权限就不再设置具体单位、区、用户的权限范围了，直接返回
                		dataPower.setHasAllPower(true);
                		return dataPower;
                	}else if(pobj.equals("selforg")){//表示只能看本单位
                		iterator.remove();
                		orgPowers.add(user.getOrgId());
                	}else if(pobj.equals("selfuser")){//表示只能看自己（如果一个用户跨单位或区创建了数据，那么假如后来权限放小，不再管那个区，但也有可能能看到他之前在别的区里创建的数据）
                		iterator.remove();
                		userPowers.add(user.getId());
                	}
                }
            }

            Map<String,List<Map>> typeDataMap=powerDatas.stream().collect(Collectors.groupingBy(m->m.get("POWER_TYPE")+""));

            Set<String> regionPowers=new HashSet<>();
            regionPowers.addAll(getPowerObjVal(typeDataMap,"org_region"));
            regionPowers.addAll(getPowerObjVal(typeDataMap,"user_region"));
            regionPowers.addAll(getPowerObjVal(typeDataMap,"role_region"));
            dataPower.setPowerRegions(new ArrayList<>(regionPowers));

            orgPowers.addAll(getPowerObjVal(typeDataMap,"org_org"));
            orgPowers.addAll(getPowerObjVal(typeDataMap,"user_org"));
            orgPowers.addAll(getPowerObjVal(typeDataMap,"role_org"));

            userPowers.addAll(getPowerObjVal(typeDataMap,"user_user"));
            if(orgPowers.size()>0){
                dataPower.setPowerOrgs(new ArrayList<>(orgPowers));
            }
            if(userPowers.size()>0){
                dataPower.setPowerUsers(new ArrayList<>(userPowers));
            }
        }else{//没有任何权限设置，只能看自己的
            dataPower.setPowerUsers(new ArrayList(){{add(user.getId());}});
        }
        return dataPower;
    }

    private List<Map> getPowerOrgDatas(PowerType powerType,String module,Object ower){
        Cond Cond;
        if(ower instanceof String[]){
            Cond=new Cond("OWER", ower, Opt.in);
        }else if(ower instanceof String){
            Cond=new Cond("OWER", ower, Opt.eq);
        }else{
            throw new IllegalArgumentException("ower只能是字符串或字符串数组");
        }
        SelectSql dataPowerQuery=new SelectSql("SYS_DATA_POWER", Cond.eq("BTYPE",module).and
                (Cond.eq("POWER_TYPE",powerType.toString()).and(Cond))).addQueryColumns("*");
        SelectSql deptQuery=new SelectSql("SYS_DEPT",Cond.eq("MARK",1)).addQueryColumns("ID","NAME");

        TableJoinCond tableJoinCond;
        if(powerType==PowerType.role_org || powerType==PowerType.user_org || powerType==PowerType.org_org){//控制单位的情况，通过单位id关联
            tableJoinCond=TableJoinCond.eq("ID",dataPowerQuery,"POBJ");
        }else if(powerType==PowerType.org_region){//控制区
            tableJoinCond=TableJoinCond.eq("AREA",dataPowerQuery,"POBJ");
        }else{
            throw new IllegalArgumentException("暂不支持"+powerType);
        }
        AbsSelect absQuery=dataPowerQuery.lJoin(deptQuery,tableJoinCond);
        List<Map> datas=powerDao.queryList(absQuery);
        return datas;
    }
    /**
     * @Author liudk by 2022/3/10 下午4:40
     * @description：获取用户的拥有者，包括包含的角色id、用户id、单位id
     *
     * @Param powerUser:用户
     *
     * @Throws
     *
     * @Return
     */
    private Object[] getOwersByUser(IUser powerUser){
        List owerIds=new ArrayList<>();
        List<IRole> roles=powerUser.getRoles();
        if(roles!=null && roles.size()>0){
            owerIds=roles.stream().map(IRole::getRoleId).collect(Collectors.toList());
        }
        owerIds.add(powerUser.getId());
        owerIds.add(powerUser.getOrgId());
        return owerIds.toArray(new String[owerIds.size()]);
    }

    private static Set<String> getPowerObjVal(Map<String,List<Map>> typeDataMap,String typeStr){
        List<Map> org_regoin_powers=typeDataMap.get(typeStr);//通过单位控制区域设置权限
        if(org_regoin_powers!=null){
            Set<String> povs=org_regoin_powers.stream().map(m->m.get("POBJ")+"").collect(Collectors.toSet());
            return povs;
        }
        return new HashSet<>();
    }

    @Autowired
    public void setPowerDao(IMoDao powerDao) {
        this.powerDao = powerDao;
        this.powerDao.setBindTable(()->"SYS_DATA_POWER");
    }
    @Autowired
	public void setRegionDao(IMoDao regionDao) {
		this.regionDao = regionDao;
		this.regionDao.setBindTable(()->"SYS_REGION");
	}
    @Autowired
    public void setDeptDao(IMoDao deptDao) {
        this.deptDao = deptDao;
        this.deptDao.setBindTable(()->"SYS_DEPT");
    }
    @Transactional
    @Override
	public boolean savePowerList(List<SysDataPower> powers ,String id) {
		IUser user = sysUtil.getLoginUser();
		if(powers != null && powers.size() > 0){
			String powerType = "role%";
			if(powers.get(0).getPower_type().toString().indexOf("user") == 0)powerType = "user%";
			this.powerDao.delete(Cond.eq("ower", powers.get(0).getOwer()).and(Cond.like("power_type",powerType)));
			for (SysDataPower sysDataPower : powers) {
				sysDataPower.setSdp_id(IDGenerator.get32UUID());
				sysDataPower.setCreator(user.getAccount());
				sysDataPower.setCdate(new Date());
				sysDataPower.setPval((byte)3);
			}
            this.powerDao.insert(SysDataPower.class, powers);
        }else{
			this.powerDao.delete(Cond.eq("ower",id));
		}
		return false;
	}

	@Override
	public SysDataPowerVo getDataPowerByRoleOrUser(String id,String btype, String power_type) {
		List<IUser> users = null;
		if(power_type.equals("user"))users = this.userService.getUsersByAccount(id);
		IUser user = null;
		if(users != null && users.size()>0)user = users.get(0);
		SysDataPowerVo vo = new SysDataPowerVo();
		SelectSql dataPowerQuery=new SelectSql("SYS_DATA_POWER", Cond.eq("OWER",user==null?id:user.getId()).and(Cond.like("power_type",power_type+"%")));
        List<SysDataPower> datas=powerDao.queryBeanList(SysDataPower.class,dataPowerQuery);
        int allBus = Constant.ONE;
		vo.setUser(user);
		List<SysDataPower> orgList = new ArrayList();
		List<SysDataPower> regionList = new ArrayList();
	   	 
        if(datas!=null && datas.size() > 0){
        	vo.setPowers(datas);
        	//是否所有业务统一
        	 allBus = Constant.ZERO;
	    	 for(SysDataPower powerData:datas){
	             if(powerData.getBtype().equalsIgnoreCase(Constant.ALL)){
	            	 allBus = Constant.ONE;
	             }
	             if(powerData.getPower_type().equals(PowerType.user_org) || powerData.getPower_type().equals(PowerType.org_org)|| powerData.getPower_type().equals(PowerType.role_org))orgList.add(powerData);
	             if(powerData.getPower_type().equals(PowerType.user_region) || powerData.getPower_type().equals(PowerType.org_region)|| powerData.getPower_type().equals(PowerType.role_region))regionList.add(powerData);
	         }
        	 
	    	//设置业务模块
		   	 vo.setBtype(btype);
		   	 List<ISysRule> btypes = CacheManager.getInstance().getRules(Constant.SYS_MOUDLE);
		   	 if(StringUtils.isEmpty(btype) && btypes!=null && btypes.size() >0)vo.setBtype(btypes.get(0).getKey());
		   	 if(StringUtils.isEmpty(vo.getBtype()) && allBus == Constant.ZERO){
				 vo.setBtype(Constant.DEFAULT_MOUDLE);
			 }
        	 //设置范围
        	 SysDataPower temp = null;
        	 if(datas.size() == 1) temp = datas.get(0);
        	 if(datas.size() > 1){
        		 for(SysDataPower powerData:datas){
        			 if(powerData.getBtype().equalsIgnoreCase(vo.getBtype())){
        				 temp = powerData;
        				 break;
        			 }
        		 }
        	 }
        	 if(temp != null){  
        		 if(user != null){
        			 if (user.getId().equalsIgnoreCase(temp.getPobj()) ) {
        				 vo.setScope(PowerScope.self.getStatu());
        			 }else if (user.getOrgId().equalsIgnoreCase(temp.getPobj()) ) {
        				 vo.setScope(PowerScope.org.getStatu());
        			 }else if(Constant.ALL.equalsIgnoreCase(temp.getPobj())){
        				 vo.setScope(PowerScope.all.getStatu());
        			 }else{
        				 vo.setScope(PowerScope.custom.getStatu());
        			 }
        		 }else{
        			 if (temp.getPobj().equalsIgnoreCase("selfuser")) {
        				 vo.setScope(PowerScope.self.getStatu());
        			 }else if (temp.getPobj().equalsIgnoreCase("selforg")) {
        				 vo.setScope(PowerScope.org.getStatu());
        			 }else if(Constant.ALL.equalsIgnoreCase(temp.getPobj())){
        				 vo.setScope(PowerScope.all.getStatu());
        			 }else{
        				 vo.setScope(PowerScope.custom.getStatu());
        			 }
        		 }
        	 }else{
        		 if(allBus == Constant.ONE)vo.setScope(PowerScope.custom.getStatu());
        	 }
        }else{
        	//vo.setScope(PowerScope.self.getStatu());
        	vo.setPowers(null);
        }
        vo.setAllBus(allBus);
        
        //单位权限
        SelectSql deptQuery=new SelectSql("SYS_DEPT").addQueryColumns("ID","NAME","PID");
        deptQuery.addConditoin(Cond.eq("mark",1), CondConnector.AND);
        List<Map> depts = dao.queryList(deptQuery);
        JSONArray deptArr=this.getTreeJson(depts, orgList,vo,Constant.ONE);
        vo.setDeptList(deptArr);
        
        //区域权限
        String sysBaseRegion = null;
        ISysRule regionRule = sysRuleMs.getRule(Constant.SYSCONFIG, Constant.SYS_BASE_REGION);
        if(regionRule!=null ){
        	sysBaseRegion = "%"+regionRule.getVal()+"%";
        }
        SelectSql regionQuery=null;
        if(StringUtils.isNotEmpty(sysBaseRegion)){
        	regionQuery= new SelectSql("SYS_REGION", Cond.like("fdncode",sysBaseRegion));
        }else{
        	regionQuery= new SelectSql("SYS_REGION");
        }
        regionQuery.addQueryColumns("MCODE ID","MNAME NAME","PCODE PID");
        List<Map> regions = dao.queryList(regionQuery);
        JSONArray regionArr=this.getTreeJson(regions, regionList,vo,Constant.ZERO);
        vo.setRegionList(regionArr);		
		return vo ;
	}
	
	/**获取数据权限树
	 * 
	 * @param mapLists
	 * @param powers
	 * @param type 1:单位，0:区域
	 * @return
	 */
	public JSONArray getTreeJson(List<Map> mapLists ,List<SysDataPower> powers,SysDataPowerVo vo,int type){
		if(mapLists != null ){
			List<String> listArr = new ArrayList<String>();
			for(int j=0;j< mapLists.size();j++){
				mapLists.get(j).put("checked", false);
				if(mapLists != null && powers.size() > 0){
					for(int i=0;i<powers.size();i++){
						if(powers.get(i).getPobj().equals(mapLists.get(j).get("ID").toString()) ){
							if(powers.get(i).getBtype().equals(vo.getBtype())  || vo.getAllBus() == Constant.ONE){
								mapLists.get(j).put("checked", true);
								listArr.add(mapLists.get(j).get("ID").toString());
								break;
							}
						}
					}
				}
   			 }
			if(type== Constant.ONE){
				vo.setDeptArr(JSONArray.parseArray(JSON.toJSONString(listArr)));
			}else{
				vo.setRegionArr(JSONArray.parseArray(JSON.toJSONString(listArr)));
			}
   		 }
	   	 List<Map> mainComDatas= mapLists.stream().map(com->{
	            Map rmap=new HashMap();
	            rmap.put("id",com.containsKey("ID")?com.get("ID").toString():null);
	            rmap.put("pid",com.containsKey("PID")?com.get("PID").toString():null);
	            rmap.put("checked",com.containsKey("checked")?com.get("checked"):false);
	            rmap.put("name",com.containsKey("NAME")?com.get("NAME").toString():null);
	            return rmap;
	        }).collect(Collectors.toList());

		List<ITree<Object, Map>> treedatas= TreeData.getTreeData(mainComDatas,
                map->new TreeData(map.get("id"),map.get("pid"),(String) map.get("name")));
        JSONArray jarr=JSONArray.parseArray(JSON.toJSONString(treedatas));
		return jarr;
	}

    @Data
    private class DataPower{
        private boolean hasAllPower = false;

        private List<String> powerRegions;//拥有的区域权限

        private List<String> powerOrgs;//拥有的单位权限

        private List<String> powerUsers;//拥有的用户权限
    }
}
