package entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="PARCELITEM")
public class ParcelItem {
	
	@Id
	@Column(name="ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="PARCELID", insertable=true, updatable=true)
	private ImportParcel parcel;
	
	@Column(name="QUANTITY", nullable = false)
	private Long quantity;
	
	@Column(name="COST_RMB", nullable = false)
	private BigDecimal cost_rmb;
	
	@Column(name="COST_VND", nullable = false)
	private BigDecimal cost_vnd;
	
	@Column(name="LASTUPDATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;
	
	@Column(name="REMAIN")
	private Long remain;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="PRODUCTID", insertable=true, updatable=true)
	private Product product;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ImportParcel getParcel() {
		return parcel;
	}

	public void setParcel(ImportParcel parcel) {
		this.parcel = parcel;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getCost_rmb() {
		return cost_rmb;
	}

	public void setCost_rmb(BigDecimal cost_rmb) {
		this.cost_rmb = cost_rmb;
	}

	public BigDecimal getCost_vnd() {
		return cost_vnd;
	}

	public void setCost_vnd(BigDecimal cost_vnd) {
		this.cost_vnd = cost_vnd;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Long getRemain() {
		return remain;
	}

	public void setRemain(Long remain) {
		this.remain = remain;
	}

}
