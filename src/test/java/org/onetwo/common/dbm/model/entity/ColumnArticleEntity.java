package org.onetwo.common.dbm.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.onetwo.common.dbm.model.entity.ColumnArticleEntity.ColumnArticleId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author weishao zeng
 * <br/>
 */
@Data
@Entity
@Table(name="test_column_article")
@IdClass(ColumnArticleId.class)
public class ColumnArticleEntity {

	@Id  
	Long columnId;
	@Id
	Long articleId;
	
	@Column(name="is_headline")
	boolean headline;
	
	@Transient
	ColumnArticleId id;
	
	public ColumnArticleId getId() {
		return new ColumnArticleId(columnId, articleId);
	}
	
	public void setId(ColumnArticleId id) {
		this.columnId = id.getColumnId();
		this.articleId = id.getArticleId();
	}

	@SuppressWarnings("serial")
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ColumnArticleId implements Serializable {
		Long columnId;
		Long articleId;
	}
}
