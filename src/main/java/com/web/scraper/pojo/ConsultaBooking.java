package com.web.scraper.pojo;

import java.util.HashSet;

public class ConsultaBooking {
	
	 String lugar;
     int number_of_rooms;
     int offset = 0;
     String checkIn;
     String checkOut;
     String url;
     HashSet<String> direccionesUrls;
     String etiquetaHtml;
	public String getLugar() {
		return lugar;
	}
	public void setLugar(String lugar) {
		this.lugar = lugar;
	}	
	public int getNumber_of_rooms() {
		return number_of_rooms;
	}
	public void setNumber_of_rooms(int number_of_rooms) {
		this.number_of_rooms = number_of_rooms;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public String getCheckIn() {
		return checkIn;
	}
	public void setCheckIn(String checkIn) {
		this.checkIn = checkIn;
	}
	public String getCheckOut() {
		return checkOut;
	}
	public void setCheckOut(String checkOut) {
		this.checkOut = checkOut;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public HashSet<String> getDireccionesUrls() {
		return direccionesUrls;
	}
	public void setDireccionesUrls(HashSet<String> direccionesUrls) {
		this.direccionesUrls = direccionesUrls;
	}
	public String getEtiquetaHtml() {
		return etiquetaHtml;
	}
	public void setEtiquetaHtml(String etiquetaHtml) {
		this.etiquetaHtml = etiquetaHtml;
	}
	public ConsultaBooking(String lugar, int number_of_rooms, int offset,
			String checkIn2, String checkOut2, String url, String etiquetaHtml) {
		super();
		this.lugar = lugar;
		
		this.number_of_rooms = number_of_rooms;
		this.offset = offset;
		checkIn = checkIn2;
		checkOut = checkOut2;
		this.url = url;
		this.etiquetaHtml = etiquetaHtml;
	}
     
     

}
