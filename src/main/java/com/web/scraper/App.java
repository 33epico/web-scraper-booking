package com.web.scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.web.scraper.pojo.ConsultaBooking;
import com.web.scraper.pojo.Registro;

public class App {

	/**
	 * Primer prototipo para rascar la web de booking y obtener información
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String lugar = "Plasencia";
		int number_of_rooms = 1; // en esta versión no se usa
		int offset = 0; // en esta versión no se usa
		String checkIn = "10 mayo 2024";
		String checkOut = "12 mayo 2024";
		String url = "https://www.booking.com/searchresults.es.html";
		// etiqueta html a buscar
		String etiquetaHtml = "a";
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");


		ConsultaBooking consultaBooking = new ConsultaBooking(lugar, number_of_rooms, offset, checkIn, checkOut, url,etiquetaHtml);

		direccionesUrls(consultaBooking, options);

		recorrerUrls(consultaBooking.getDireccionesUrls(), options);

	}

	/**
	 * Este método contiene las llamadas a la url de booking con seleniun
	 * permitiendo que la aplicación de booking carge todos sus datos
	 * 
	 * @param consultaBooking
	 * @return
	 */
	private static ConsultaBooking direccionesUrls(ConsultaBooking consultaBooking, ChromeOptions options) {

		HashSet<String> direccionesUrls = new HashSet<String>();

		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

		// Configurar opciones del navegador (por ejemplo, para cambiar el user agent)
		
		// Crear una instancia del controlador de Chrome con las opciones configuradas
		WebDriver driver = new ChromeDriver(options);

		// Abrir la página web en el navegador
		driver.get(consultaBooking.getUrl());

		try {
			Thread.sleep(1000);// Le damos un segundo de respuesta para permitir que el componente responda
			WebDriverWait wait = new WebDriverWait(driver, 10);
			// Encontrar el campo de entrada para el destino y enviar la ubicación
			WebElement checkInField = driver.findElement(By.xpath("//button[@data-testid='date-display-field-start']"));
			checkInField.click();
			WebElement destinationInput = driver.findElement(By.name("ss"));
			destinationInput.sendKeys(consultaBooking.getLugar());
			Thread.sleep(1000);

			// Ingresa las fechas de entrada y salida
			WebElement button = driver.findElement(By.cssSelector(
					"div[data-testid='searchbox-dates-container'] button[data-testid='date-display-field-end']"));
			Thread.sleep(1000);
			WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody")));
			button.click();
			Thread.sleep(1000);
			WebElement fechaIn = table
					.findElement(By.xpath("//span[@aria-label='" + consultaBooking.getCheckIn() + "']"));
			fechaIn.click();
			Thread.sleep(1000);
			WebElement fechaOut = table
					.findElement(By.xpath("//span[@aria-label='" + consultaBooking.getCheckOut() + "']"));
			fechaOut.click();
			Thread.sleep(1000);
			// Encontrar el botón de búsqueda y hacer clic en él
			WebElement searchButton = driver.findElement(By.cssSelector("button[type='submit']"));
			searchButton.click();
			Thread.sleep(5000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Obtener el HTML de la página después de que se haya cargado completamente
		String html = driver.getPageSource();
		// Cerrar el navegador
		driver.quit();

		Document doc = Jsoup.parse(html);

		// Obtenemos el documento html que se cargó previamente con selenium
		Elements tags = doc.select(consultaBooking.getEtiquetaHtml());

		// Buscamos los hrf que contengan la cadena /hotel/es en su url
		for (Element repository : tags) {
			String href = repository.attr("href");
			if (href.contains("/hotel/es")) {
				direccionesUrls.add(href);
			}
		}
		consultaBooking.setDireccionesUrls(direccionesUrls);

		return consultaBooking;
	}

	private static void recorrerUrls(HashSet<String> direccionesUrls,ChromeOptions options) {
		
		
		List<Registro> registrosBooking = new ArrayList<Registro>(); 	
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		
		// Crear una instancia del controlador de Chrome con las opciones configuradas
		WebDriver driver = new ChromeDriver(options);

		// Recorremos las distintas urls para obtener la información
		for (String hotel : direccionesUrls) {

			try {

				// Abrir la página web en el navegador
				driver.get(hotel);

				WebDriverWait wait = new WebDriverWait(driver, 2);

				// Se quita el mapa y el banner
				Thread.sleep(1000);

				try {// no siempre hay banner
					WebElement aceptarButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("onetrust-accept-btn-handler")));
					aceptarButton.click();
				} catch (Exception e) {
					System.out.println("");
				}

				try {// no siempre hay mapa
					WebElement modalMapa = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("map_full_overlay__close")));
					modalMapa.click();
				} catch (Exception e) {
					System.out.println("");
				}

				WebElement fechaFinButton = driver.findElement(By.xpath("(//button[@data-testid='date-display-field-end'])[2]"));
				fechaFinButton.click();
				
				//Esperamos 500 ms para que cargue todos los componentes
				Thread.sleep(500);
				String html = driver.getPageSource();
				Document doc = Jsoup.parse(html);				
				//TODO insertar en base de datos
				registrosBooking.addAll(recorrerCalendario(doc));

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};

		driver.quit();
		
		//Esto es solo para muestra
		AtomicInteger idRegistro = new AtomicInteger(1); //para evitar problemas de concurrencia
		registrosBooking.forEach(registro->{
			System.out.println(idRegistro.getAndIncrement()+";"+registro.getNombreHotel()+";"+registro.getMes()+";"+registro.getDia()+";"+registro.getPrecio());
		});
	}

	private static List<Registro> recorrerCalendario(Document doc){
		// Obtener el nombre del hotel
		Element h2Element = doc.selectFirst("h2.pp-header__title");
		String nombreHotel = h2Element.text();
		Element tabla = doc.selectFirst("[data-testid=searchbox-datepicker-calendar]");
		List<Registro> registros = new ArrayList<Registro>();
		
		if (tabla != null) {
			
			String[] precioSplit;
			String[] meses;
			String mes;
			String dia;
			String precio;
			int cuentaDias = 1;
			
			Elements descMeses = tabla.select("[aria-live=\"polite\"]");
			Elements celdas = tabla.select("[role=gridcell]");			
			meses = descMeses.text().split(" ");
			
			for (Element celda : celdas) {
				String precioDia = celda.text(); // Obtener el texto de la celda
				if (precioDia.contains("€")) {
					precioSplit = precioDia.split("€");
					dia = precioSplit[0];
					precio = precioSplit[1];
				} else {
					dia = precioDia.replaceAll(" —", "");
					precio = "-";
				}
				if (cuentaDias > 28 && dia.contains("1")) {
					mes = meses[2];
				}else {
					mes = meses[0];
				}

				System.out.println(nombreHotel+mes+dia+precio);
				Registro registro = new Registro(nombreHotel,mes,dia,precio);
				registros.add(registro);
				cuentaDias++;
			}
		} else {
			System.out.println("No se encontró la tabla con el ID searchbox-datepicker-calendar");
		}
		return registros;
	}

}
