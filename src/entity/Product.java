package entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Product {
	
	@Id
	@Column(name="ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="CODE")
	private String code;
	
	@Column(name="NAME")
	private String name;
	
	@Column(name="TYPE")
	private String type;
	
	@Column(name="ENABLE")
	private boolean enable;
	
	@Column(name="LASTUPDATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastupdate;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Override
	public String toString() {
		return "Product [code=" + code + ", name=" + name + ", description=" +description
		        + "]";
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public Date getLastupdate() {
		return lastupdate;
	}
	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
