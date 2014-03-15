package entity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Sale {
	
	@Id
	@Column(name="ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="SALEDATE")
	@Temporal(TemporalType.DATE)
	private Date saleDate;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="CUSTID", insertable=true, updatable=true)
	private Customer customer;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	/**
	 * 0: chưa thu tiền
	 * 1: đã thu tiền
	 * 2: chưa thu đủ
	 */
	@Column(name="STATUS")
	private String status;
	
	@Column(name="LASTUPDATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastupdate;
	
	@OneToMany(targetEntity=SaleItem.class, mappedBy="sale")
	private List<SaleItem> saleItems;
	
	@Column(name="SALEAMOUNT")
	private BigDecimal saleAmount;
	
	@Column(name="UNPAYAMOUNT")
	private BigDecimal unPayAmount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(Date saleDate) {
		this.saleDate = saleDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}

	public List<SaleItem> getSaleItems() {
		return saleItems;
	}

	public void setSaleItems(List<SaleItem> saleItems) {
		this.saleItems = saleItems;
	}
	
	public String getSaleDateString() {
		if(saleDate!=null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			return format.format(saleDate);
		}
		return null;
	}
	
	public String getCustomerName() {
		if(customer!=null) {
			return customer.getName();
		}
		return null;
	}
	
	public String getStatusString() {
		if("0".equals(status)) {
			return "Chưa thu";
		} else if("1".equals(status)) {
			return "Đã thu đủ";
		} else if("2".equals(status)) {
			return "Còn nợ";
		}
		return null;
	}

	public BigDecimal getSaleAmount() {
		return saleAmount;
	}

	public void setSaleAmount(BigDecimal saleAmount) {
		this.saleAmount = saleAmount;
	}

	public BigDecimal getUnPayAmount() {
		return unPayAmount;
	}

	public void setUnPayAmount(BigDecimal unPayAmount) {
		this.unPayAmount = unPayAmount;
	}

}
