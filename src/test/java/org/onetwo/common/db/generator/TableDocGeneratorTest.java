package org.onetwo.common.db.generator;

import java.util.Comparator;

import org.junit.Test;
import org.onetwo.common.db.generator.meta.ColumnMeta;
import org.onetwo.common.db.generator.meta.TableMeta;

/**
 * @author wayshall
 * <br/>
 */
public class TableDocGeneratorTest {
	
	@Test
	public void testGenerateTableFields(){
		DbmGenerator g = DbmGenerator.createWithDburl("jdbc:mysql://localhost:3306/apidoc?&useSSL=false&characterEncoding=UTF-8", 
														"root", 
														"root");
		StringBuilder columnNames = new StringBuilder();
		StringBuilder columnComments = new StringBuilder();
		TableMeta tableMeta = g.dbGenerator().table("api_swagger_model")
						.metaConfig()
						.tableMeta();
		ColumnMeta pk = tableMeta.getPrimaryKey();
		columnNames.append(pk.getName()).append("、\n");
		columnComments.append(pk.getComment()).append("、\n");
		tableMeta.getColumns()
						.stream()
						.sorted(Comparator.comparing(col->col.getName()))
						.forEach(column->{
			if(!column.isPrimaryKey()){
				columnNames.append(column.getName()).append("、\n");
				columnComments.append(column.getComment()).append("、\n");
			}
		});
		System.out.println(columnNames);
		System.out.println(columnComments);
	}

}
