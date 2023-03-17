package com.hcxinan.sys.power;

import com.hcxinan.core.inte.system.IOrg;
import com.hcxinan.core.inte.system.IRole;
import com.hcxinan.core.inte.system.IUser;
import com.hcxinan.core.inte.system.IUserService;
import lombok.extern.log4j.Log4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration({"classpath*:applicationContext.xml"})
@Transactional(transactionManager = "transactionManager")
@Rollback(true)
@Log4j
public class DataPowerServiceTest {

    @Autowired
    private DataPowerService dataPowerService;
    @MockBean
    private IUserService userService;
    @Mock
    private IUser user1;
    @Mock
    private IUser user2;
    @Mock
    private IRole role1;
    @Mock
    private IRole role2;
    @Mock
    private IRole role3;

    @BeforeEach
    @Before
    public void beforeTest() {
        //Mock IOC
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn("role001").when(role1).getRoleId();
        Mockito.doReturn("role002").when(role2).getRoleId();
        Mockito.doReturn("role003").when(role3).getRoleId();
        List<IRole> zhangRoles=new ArrayList(){{
            add(role1);
            add(role2);
        }};
        List<IRole> lisiRoles=new ArrayList(){{
            add(role2);
            add(role3);
        }};

        Mockito.doReturn("zhangsan").when(user1).getId();
        Mockito.doReturn("org001").when(user1).getOrgId();
        Mockito.doReturn(zhangRoles).when(user1).getRoles();

        Mockito.doReturn("lisi").when(user2).getId();
        Mockito.doReturn("org002").when(user2).getOrgId();
        Mockito.doReturn(lisiRoles).when(user2).getRoles();
    }

    @Test
    public void hasAllPower(){
        boolean hasAllPower=dataPowerService.hasAllPower(user1,"btype1");
        assertThat(hasAllPower).isTrue();
        //check no set power case
        hasAllPower=dataPowerService.hasAllPower(user1,"btype1_Test");
        assertThat(hasAllPower).isFalse();
        //no belong to zhangsan
        hasAllPower=dataPowerService.hasAllPower(user1,"btype4");
        assertThat(hasAllPower).isFalse();

        hasAllPower=dataPowerService.hasAllPower(user1,"btype2");
        assertThat(hasAllPower).isTrue();
        hasAllPower=dataPowerService.hasAllPower(user1,"btype3");
        assertThat(hasAllPower).isTrue();

        hasAllPower=dataPowerService.hasAllPower(user1,"btype5");
        assertThat(hasAllPower).isTrue();
    }

    @Test
    public void getRegionScope(){
        Map<String, Integer> regionScope = dataPowerService.getRegionScope(user1,"b_btyp1");
        assertThat(regionScope.size()).isEqualTo(1);

        regionScope = dataPowerService.getRegionScope(user1,"b_btyp2");
        assertThat(regionScope==null).isTrue();

        regionScope = dataPowerService.getRegionScope(user1,"b_btyp3");
        assertThat(regionScope.size()).isEqualTo(1);

        regionScope = dataPowerService.getRegionScope(user1,"b_btyp4");
        assertThat(regionScope==null).isTrue();

        regionScope = dataPowerService.getRegionScope(user1,"b_btyp5");
        assertThat(regionScope.size()).isEqualTo(1);
        regionScope = dataPowerService.getRegionScope(user1,"b_btyp6");
        assertThat(regionScope==null).isTrue();
    }

    @Test
    public void getOrgScope(){
        Map<IOrg,Integer> orgScope = dataPowerService.getOrgScope(user1,"c_btyp1");
        assertThat(orgScope.size()).isEqualTo(2);

        orgScope = dataPowerService.getOrgScope(user1,"c_btyp2");
        assertThat(orgScope.size()).isEqualTo(3);

        //by user_region power to org
        orgScope = dataPowerService.getOrgScope(user1,"d_btyp1");
        assertThat(orgScope.size()).isEqualTo(2);

        //by role_region power to org
        orgScope = dataPowerService.getOrgScope(user1,"e_btyp1");
        assertThat(orgScope.size()).isEqualTo(6);

        //mutil
        orgScope = dataPowerService.getOrgScope(user1,"f_btyp1");
        assertThat(orgScope.size()).isEqualTo(6);
    }
}