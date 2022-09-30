package org.onetwo.common.hibernate.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.onetwo.common.dbm.model.dao.CompanyDao;
import org.onetwo.common.dbm.model.hib.entity.CompanyEntity;
import org.onetwo.common.dbm.model.hib.entity.DepartmentEntity;
import org.onetwo.common.dbm.model.hib.entity.EmployeeEntity;
import org.onetwo.common.dbm.model.hib.entity.EmployeeEntity.EmployeeGenders;
import org.onetwo.common.dbm.model.vo.CompanyVO;
import org.onetwo.common.dbm.model.vo.DepartmentVO;
import org.onetwo.common.hibernate.HibernateBaseTest;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangOps;
import org.onetwo.jpa.hibernate.SpecificationQuerys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

@Rollback(false)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HibernateNestedMappingTest extends HibernateBaseTest {

	@Autowired
	private CompanyDao companyDao;
	@PersistenceContext
	EntityManager entityManager;
	@Autowired
	CompanyJpaRepository companyJpaRepository;
	@Autowired
	DepartmentJpaRepository departmentJpaRepository;
	@Autowired
	EmployeeJpaRepository employeeJpaRepository;
	
	public void clear(){
		/*dbmEntityManager.removeAll(EmployeeEntity.class);
		dbmEntityManager.removeAll(DepartmentEntity.class);
		dbmEntityManager.removeAll(CompanyEntity.class);*/
		employeeJpaRepository.deleteAll();
		departmentJpaRepository.deleteAll();
		companyJpaRepository.deleteAll();
	}
	
	@Test
	public void test1Saves(){
		this.clear();
		
		List<CompanyEntity> companies = LangOps.ntimesMap(10, i->{
			return createCompany(i);
		});
//		dbmEntityManager.save(companies);
		
		
		List<CompanyEntity> dbcompanies = SpecificationQuerys.from(CompanyEntity.class)
																.getList(companyJpaRepository);
		assertThat(dbcompanies.size()).isEqualTo(companies.size());
		Collection<String> names = ReflectUtils.getProperties(companies, "name");
		Collection<String> dbnames = ReflectUtils.getProperties(dbcompanies, "name");
		assertThat(dbnames).containsAll(names);
		
//		List<CompanyVO> nestedCompanies = companyDao.findNestedCompanies();
	}
	

	@Test
	public void testfindWithoutNestedMapping(){
		String name = "测试公司";//测试公司-
		List<CompanyVO> companies = this.companyDao.findCompaniesByLikeName(name);
		assertThat(companies.size()).isEqualTo(10);
		companies.stream().forEach(company->{
			assertThat(company.getDepartments()).isNull();
		});
		
		companies = this.companyDao.findCompaniesByNames(Collections.emptyList());
		assertThat(companies.size()).isEqualTo(10);
		companies.stream().forEach(company->{
			assertThat(company.getDepartments()).isNull();
		});
		
		companies = this.companyDao.findCompaniesByNames(Arrays.asList("测试公司-1", "测试公司-2"));
		assertThat(companies.size()).isEqualTo(2);
		companies.stream().forEach(company->{
			assertThat(company.getDepartments()).isNull();
		});
	}
	
	@Test
	public void testfindListWithNestedMapping(){
		List<DepartmentVO> departments = companyDao.findDepartmentsWithComapny();
		assertThat(departments.size()).isEqualTo(100);
		departments.stream().forEach(depart->{
			assertThat(depart.getCompany()).isNotNull();
			assertThat(depart.getCompany().getId()).isEqualTo(depart.getCompanyId());
		});
		

		departments = companyDao.findNestedDepartments();
		assertThat(departments.size()).isEqualTo(100);
		departments.stream().forEach(depart->{
			assertThat(depart.getCompany()).isNotNull();
			assertThat(depart.getCompany().getId()).isEqualTo(depart.getCompanyId());

			assertThat(depart.getEmployees()).isNotNull();
			depart.getEmployees().stream().forEach(employee->{
				assertThat(employee.getDepartmentId()).isEqualTo(depart.getId());
			});
		});
		

		departments = companyDao.findNestedDepartmentsWithEmployeeId();
		assertThat(departments.size()).isEqualTo(100);
		departments.stream().forEach(depart->{
			assertThat(depart.getCompany()).isNotNull();
			assertThat(depart.getCompany().getId()).isEqualTo(depart.getCompanyId());

			assertThat(depart.getEmployees()).isNotNull();
			depart.getEmployees().stream().forEach(employee->{
				assertThat(employee.getDepartmentId()).isEqualTo(depart.getId());
			});
		});
		
		List<CompanyVO> companies = companyDao.findNestedCompanies();
		assertThat(companies.size()).isEqualTo(10);
		companies.stream().forEach(company->{
			assertThat(company.getDepartments()).isNotNull();
			assertThat(company.getDepartments().size()).isEqualTo(10);
			
			company.getDepartments().stream().forEach(depart->{
//				assertThat(depart.getCompany()).isNotNull();
				assertThat(company.getId()).isEqualTo(depart.getCompanyId());

				assertThat(depart.getEmployees()).isNotNull();
				assertThat(depart.getEmployees().size()).isEqualTo(10);
				depart.getEmployees().stream().forEach(employee->{
					assertThat(employee.getDepartmentId()).isEqualTo(depart.getId());
				});
			});
		});
		

		LangOps.timeIt("findNestedDepartmentsWithEmployeeId", 1, ()->{
			companyDao.findNestedDepartmentsWithEmployeeId();
		});
		LangOps.timeIt("findNestedDepartments", 1, ()->{
			companyDao.findNestedDepartments();
		});
	}
	

	@Test
	public void testfindMapWithNestedMapping(){
		List<CompanyVO> companies = companyDao.findNestedCompaniesWithDepartmentMap();
		assertThat(companies.size()).isEqualTo(10);
		companies.stream().forEach(company->{
			assertThat(company.getDepartments()).isNull();
			assertThat(company.getDepartmentMap()).isNotNull();
			assertThat(company.getDepartmentMap().size()).isEqualTo(10);
			
			Map<Long, DepartmentVO> departMap = company.getDepartmentMap();
			departMap.entrySet().stream().forEach(entry->{
				DepartmentVO depart = entry.getValue();
//				assertThat(depart.getCompany()).isNotNull();
				assertThat(depart.getId()).isEqualTo(entry.getKey());
				assertThat(company.getId()).isEqualTo(depart.getCompanyId());

				assertThat(depart.getEmployees()).isNotNull();
				assertThat(depart.getEmployees().size()).isEqualTo(10);
				depart.getEmployees().stream().forEach(employee->{
					assertThat(employee.getDepartmentId()).isEqualTo(depart.getId());
				});
			});
		});
	}
	
	public CompanyEntity createCompany(int index){
		int employeeNumber = 10;
		CompanyEntity company = new CompanyEntity();
		company.setName("测试公司-"+index);
		company.setEmployeeNumber(employeeNumber);
		company.setDescription("一个测试公司-"+index);
		entityManager.persist(company);
		
		LangOps.ntimesMap(10, i->{
			return createDepartment(company.getId(), i);
		});
		return company;
	}
	
	public DepartmentEntity createDepartment(Long companyId, int index){
		DepartmentEntity department = new DepartmentEntity();
		department.setName("部门-"+index);
		department.setEmployeeNumber(10);
		department.setCompanyId(companyId);
		entityManager.persist(department);
		List<EmployeeEntity> employees = LangOps.ntimesMap(10, i->{
			return createEmployee(department.getId(), i);
		});
//		entityManager.persist(employees);
		employees.forEach(e->{
			entityManager.persist(e);
		});
		return department;
	}
	
	public EmployeeEntity createEmployee(Long departmentId, int index){
		EmployeeEntity employee = new EmployeeEntity();
		employee.setName("员工-"+index);
		employee.setBirthday(new Date());
		employee.setDepartmentId(departmentId);
		employee.setJoinDate(new Date());
		employee.setGender(EmployeeGenders.MALE);
		return employee;
	}
	
}
