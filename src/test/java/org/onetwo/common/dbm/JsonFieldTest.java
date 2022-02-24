package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.convert.Types;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.dao.JsonCompanyDao;
import org.onetwo.common.dbm.model.hib.entity.CompanyEntity;
import org.onetwo.common.utils.CUtils;
import org.onetwo.dbm.annotation.DbmJsonField;
import org.onetwo.dbm.annotation.DbmRowMapper;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * @author wayshall
 * <br/>
 */
@Rollback(false)
public class JsonFieldTest extends DbmBaseTest {
	
	@Autowired
	private BaseEntityManager baseEntityManager;
	@Autowired
	private JsonCompanyDao jsonCompanyDao;

	@Test
	public void testSave(){
//		baseEntityManager.removeAll(JsonCompanyEntity.class);
		
		JsonCompanyEntity company = new JsonCompanyEntity();
		company.setName(JsonFieldTest.class.getName()+"-扩展公司1");
		
		ExtInfo extInfo = new ExtInfo();
		extInfo.setAddress("公司地址");
		extInfo.setPhones(Arrays.asList("1333333333", "1366666666"));
		extInfo.setProperty(11);
		company.setExtInfo(extInfo);
		company.setBuildAt(LocalDateTime.of(2017, 10, 10, 15, 57));
		company.setLinkPhones(Arrays.asList("13666666666"));
		
		DbmModuleConfigTestVO config = new DbmModuleConfigTestVO();
		config.setValue("1000");
		config.setRemark("测试");
		DbmModuleConfigTestVO config2= new DbmModuleConfigTestVO();
		config2.setValue("test2");
		config2.setRemark("测试2");
		
		company.setConfigData(CUtils.asMap("config1", config, "config2", config2));
		
		company.setExtInfoList(Arrays.asList(extInfo));
		
		baseEntityManager.save(company);
		
		JsonCompanyEntity dbCompany = baseEntityManager.findOne(JsonCompanyEntity.class, "name", company.getName());
		assertThat(dbCompany).isNotNull();
		assertThat(dbCompany.getName()).isEqualTo(company.getName());
		assertThat(dbCompany.getBuildAt()).isEqualTo(company.getBuildAt());
		assertThat(dbCompany.getExtInfo()).isEqualTo(company.getExtInfo());
		assertThat(dbCompany.getLinkPhones().get(0)).isEqualTo(company.getLinkPhones().get(0));
		assertThat(dbCompany.getConfigData().get("config2")).isEqualTo(config2);
		assertThat(dbCompany.getConfigData().get("config1").getTypeValue(Integer.class)).isEqualTo(1000);
		
		company = dbCompany;
		extInfo = new ExtInfo();
		extInfo.setAddress("公司地址");
		extInfo.setPhones(Arrays.asList("1777777777", "1366666666"));
		extInfo.setProperty(222);
		company.setExtInfo(extInfo);
		baseEntityManager.update(company);
		
		dbCompany = baseEntityManager.findOne(JsonCompanyEntity.class, "name", company.getName());
		assertThat(dbCompany).isNotNull();
		assertThat(dbCompany.getName()).isEqualTo(company.getName());
		assertThat(dbCompany.getExtInfo()).isEqualTo(company.getExtInfo());
		assertThat(dbCompany.getExtInfoList().get(0)).isEqualTo(company.getExtInfoList().get(0));
		
		
		dbCompany = jsonCompanyDao.findOne(company.getName());
		assertThat(dbCompany).isNotNull();
		assertThat(dbCompany.getName()).isEqualTo(company.getName());
		assertThat(dbCompany.getExtInfo()).isEqualTo(company.getExtInfo());
		

		JsonCompanyEntity finalCampany = company;
		assertThatExceptionOfType(FileNamedQueryException.class).isThrownBy(()->{
			jsonCompanyDao.findOneNoJdbcMapper(finalCampany.getName());
		})
		//<org.springframework.beans.ConversionNotSupportedException: 
		//Failed to convert property value of type 'java.lang.String' to required type 'org.onetwo.common.dbm.JsonFieldTest$ExtInfo' for property 'extInfo'; nested exception is java.lang.IllegalStateException: Cannot convert value of type 'java.lang.String' to required type 'org.onetwo.common.dbm.JsonFieldTest$ExtInfo' 
		//for property 'extInfo': no matching editors or conversion strategy found>
		.withCauseInstanceOf(ConversionNotSupportedException.class);
	}

	@Entity
	@Table(name="company")
	public static class JsonCompanyEntity extends CompanyEntity {
		@DbmJsonField
		protected ExtInfo extInfo;
		@DbmJsonField(valueType=ExtInfo.class)
		protected List<ExtInfo> extInfoList;
		protected LocalDateTime buildAt;
		@DbmJsonField(storeTyping=true)
		private Map<String, DbmModuleConfigTestVO> configData;
		@DbmJsonField
		protected List<String> linkPhones;

		public ExtInfo getExtInfo() {
			return extInfo;
		}

		public void setExtInfo(ExtInfo extInfo) {
			this.extInfo = extInfo;
		}

		public LocalDateTime getBuildAt() {
			return buildAt;
		}

		public void setBuildAt(LocalDateTime buildAt) {
			this.buildAt = buildAt;
		}

		public Map<String, DbmModuleConfigTestVO> getConfigData() {
			return configData;
		}

		public void setConfigData(Map<String, DbmModuleConfigTestVO> configData) {
			this.configData = configData;
		}

		public List<ExtInfo> getExtInfoList() {
			return extInfoList;
		}

		public void setExtInfoList(List<ExtInfo> extInfoList) {
			this.extInfoList = extInfoList;
		}

		public List<String> getLinkPhones() {
			return linkPhones;
		}

		public void setLinkPhones(List<String> linkPhones) {
			this.linkPhones = linkPhones;
		}
		
	}
	
	@DbmRowMapper
	public static class JdbcMapperJsonCompanyVO extends JsonCompanyEntity {
	}
	public static class NoJdbcMapperJsonCompanyVO extends JsonCompanyEntity {
	}
	
	public static class ExtInfo {
		String address;
		Integer property;
		List<String> phones;
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public Integer getProperty() {
			return property;
		}
		public void setProperty(Integer property) {
			this.property = property;
		}
		public List<String> getPhones() {
			return phones;
		}
		public void setPhones(List<String> phones) {
			this.phones = phones;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((address == null) ? 0 : address.hashCode());
			result = prime * result
					+ ((phones == null) ? 0 : phones.hashCode());
			result = prime * result
					+ ((property == null) ? 0 : property.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExtInfo other = (ExtInfo) obj;
			if (address == null) {
				if (other.address != null)
					return false;
			} else if (!address.equals(other.address))
				return false;
			if (phones == null) {
				if (other.phones != null)
					return false;
			} else if (!phones.equals(other.phones))
				return false;
			if (property == null) {
				if (other.property != null)
					return false;
			} else if (!property.equals(other.property))
				return false;
			return true;
		}
		
	}
	

	static public class DbmModuleConfigTestVO {
	    private String value;
	    private String remark;

	    public <T> T getTypeValue(Class<T> type) {
	        if (value == null) {
	            return null;
	        }
	        return Types.asValue(value, type);
	    }

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getRemark() {
			return remark;
		}

		public void setRemark(String remark) {
			this.remark = remark;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((remark == null) ? 0 : remark.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DbmModuleConfigTestVO other = (DbmModuleConfigTestVO) obj;
			if (remark == null) {
				if (other.remark != null)
					return false;
			} else if (!remark.equals(other.remark))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}
}
