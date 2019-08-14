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
	
	/***
	 * 辅助属性，加上@Transient注解
	 */
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



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnArticleEntity other = (ColumnArticleEntity) obj;
		if (articleId == null) {
			if (other.articleId != null)
				return false;
		} else if (!articleId.equals(other.articleId))
			return false;
		if (columnId == null) {
			if (other.columnId != null)
				return false;
		} else if (!columnId.equals(other.columnId))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((articleId == null) ? 0 : articleId.hashCode());
		result = prime * result + ((columnId == null) ? 0 : columnId.hashCode());
		return result;
	}



}
