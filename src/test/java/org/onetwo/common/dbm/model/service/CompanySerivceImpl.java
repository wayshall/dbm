package org.onetwo.common.dbm.model.service;

import org.onetwo.common.dbm.model.hib.entity.CompanyEntity;
import org.onetwo.dbm.core.internal.DbmCrudServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanySerivceImpl extends DbmCrudServiceImpl<CompanyEntity, Long>{
	
	public CompanyEntity findByNameWithInvoke4Times(String name){
		CompanyEntity dbCompany = findOne("name", name);
		dbCompany = findOne("name", name);
		dbCompany = findOne("name", name);
		dbCompany = findOne("name", name);
		return dbCompany;
	}
}
