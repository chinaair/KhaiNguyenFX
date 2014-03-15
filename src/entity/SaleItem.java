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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class SaleItem {
	
	@Id
	@Column(name="ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="SALEID", insertable=true, updatable=true)
	private Sale sale;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="PRODUCTID", insertable=true, updatable=true)
	private Product product;
	
	@Column(name="QUANTITY")
	private int quantity;
	
	@Column(name="SALEPRICE")
	private BigDecimal salePrice;
	
	@Column(name="LASTUPDATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastupdate;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="PARCELITEMID", insertable=true, updatable=true)
	private ParcelItem parcelItem;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Sale getSale() {
		return sale;
	}

	public void setSale(Sale sale) {
		this.sale = sale;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}

	public Date getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}
	
	public String getProductName() {
		String pName = "";
		if(product!=null) {
			pName = product.getName();
		}
		return pName;
	}

	public ParcelItem getParcelItem() {
		return parcelItem;
	}

	public void setParcelItem(ParcelItem parcelItem) {
		this.parcelItem = parcelItem;
	}

}
