package com.web.scraper.pojo;

public class Registro {
	
	String nombreHotel;
	String mes;
	String dia;
	String precio;
	public String getNombreHotel() {
		return nombreHotel;
	}
	public void setNombreHotel(String nombreHotel) {
		this.nombreHotel = nombreHotel;
	}
	public String getMes() {
		return mes;
	}
	public void setMes(String mes) {
		this.mes = mes;
	}
	public String getDia() {
		return dia;
	}
	public void setDia(String dia) {
		this.dia = dia;
	}
	public String getPrecio() {
		return precio;
	}
	public void setPrecio(String precio) {
		this.precio = precio;
	}
	public Registro(String nombreHotel, String mes, String dia, String precio) {
		super();
		this.nombreHotel = nombreHotel;
		this.mes = mes;
		this.dia = dia;
		this.precio = precio;
	}
	
	

	
}
