package com.product.test.model;

public class Stock {
	// @Id
	 private int id;
	 private String stockId;
	 private String producId;
	 private long quantity;
	public Stock(int id, String stockId,String producId, long quantity) {
		super();
		this.id = id;
		this.stockId = stockId;
		this.producId = producId;
		this.quantity = quantity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStockId() {
		return stockId;
	}

	public void setStockId(String stockId) {
		this.stockId = stockId;
	}

	public String getProducId() {
		return producId;
	}
	public void setProducId(String producId) {
		this.producId = producId;
	}
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}	 
}

