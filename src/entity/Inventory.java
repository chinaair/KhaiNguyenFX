package entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Inventory {
	
	@Id
	@Column(name="ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name="PRODUCTID", insertable=true, updatable=false)
	private Product product;
	
	@Column(name="QOH")
	private Long qoh;
	
	@Column(name="LASTUPDATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastupdate;
	
	@Column(name="TOTALVALUE")
	private BigDecimal totalValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getQoh() {
		return qoh;
	}

	public void setQoh(Long qoh) {
		this.qoh = qoh;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Date getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(BigDecimal totalValue) {
		this.totalValue = totalValue;
	}
	
	public String getProductCode() {
		if(product!=null) {
			return product.getCode();
		}
		return null;
	}
	
	public String getProductName() {
		if(product!=null) {
			return product.getName();
		}
		return null;
	}
	
}
