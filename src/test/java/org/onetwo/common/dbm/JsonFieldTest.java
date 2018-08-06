package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.dao.JsonCompanyDao;
import org.onetwo.common.dbm.model.entity.CompanyEntity;
import org.onetwo.dbm.annotation.DbmJsonField;
import org.onetwo.dbm.annotation.DbmRowMapper;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.onetwo.dbm.mapping.JdbcRowEntryImpl;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wayshall
 * <br/>
 */
public class JsonFieldTest extends DbmBaseTest {
	
	@Autowired
	private BaseEntityManager baseEntityManager;
	@Autowired
	private JsonCompanyDao jsonCompanyDao;

	@Test
	public void testSave(){
		baseEntityManager.removeAll(JsonCompanyEntity.class);
		
		JsonCompanyEntity company = new JsonCompanyEntity();
		company.setName(JsonFieldTest.class.getName()+"-扩展公司1");
		
		ExtInfo extInfo = new ExtInfo();
		extInfo.setAddress("公司地址");
		extInfo.setPhones(Arrays.asList("1333333333", "1366666666"));
		extInfo.setProperty(11);
		company.setExtInfo(extInfo);
		company.setBuildAt(LocalDateTime.of(2017, 10, 10, 15, 57));
		
		baseEntityManager.save(company);
		
		JsonCompanyEntity dbCompany = baseEntityManager.findOne(JsonCompanyEntity.class, "name", company.getName());
		assertThat(dbCompany).isNotNull();
		assertThat(dbCompany.getName()).isEqualTo(company.getName());
		assertThat(dbCompany.getBuildAt()).isEqualTo(company.getBuildAt());
		assertThat(dbCompany.getExtInfo()).isEqualTo(company.getExtInfo());
		
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
		protected LocalDateTime buildAt;

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
		
	}
	
	@DbmRowMapper(JdbcRowEntryImpl.class)
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
}
