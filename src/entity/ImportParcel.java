package entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="IMPORTPARCEL")
public class ImportParcel {
	
	@Id
	@Column(name="ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="CODE", unique = true, nullable = false)
	private String code;
	
	@Column(name="IMPORT_DATE", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date importDate;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Column(name="LASTUPDATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;
	
	@Column(name="RATE", nullable = false)
	private BigDecimal rate;
	
	@Column(name="IMPORTVALUE")
	private BigDecimal importValue;
	
	@OneToMany(targetEntity=ParcelItem.class, mappedBy="parcel")
	private List<ParcelItem> parcelItems;

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

	public Date getImportDate() {
		return importDate;
	}

	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public List<ParcelItem> getParcelItems() {
		return parcelItems;
	}

	public void setParcelItems(List<ParcelItem> parcelItems) {
		this.parcelItems = parcelItems;
	}

	public BigDecimal getImportValue() {
		return importValue;
	}

	public void setImportValue(BigDecimal importValue) {
		this.importValue = importValue;
	}

}
