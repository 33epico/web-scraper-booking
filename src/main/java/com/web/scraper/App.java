package com.web.scraper;


import java.io.IOException;
import java.util.HashSet;
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

public class App {

	/**
	 *Primer prototipo para rascar la web de booking y obtener información
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String lugar = "Madrid";
		int number_of_rooms = 1; //en esta versión no se usa
		int offset = 0; //en esta versión no se usa
		String checkIn = "10 mayo 2024";
		String checkOut = "12 mayo 2024";
		String url = "https://www.booking.com/searchresults.es.html";
		//etiqueta html a buscar
		String etiquetaHtml = "a";

		ConsultaBooking consultaBooking = new ConsultaBooking(lugar, number_of_rooms, offset, checkIn, checkOut, url,etiquetaHtml);

		direccionesUrls(consultaBooking);

		recorrerUrls(consultaBooking.getDireccionesUrls());

		consultaBooking.getDireccionesUrls().forEach(urlShow -> {
			System.out.println(urlShow);
		});

	}

	/**
	 * Este método contiene las llamadas a la url de booking con seleniun permitiendo que la aplicación de booking carge todos sus datos
	 * @param consultaBooking
	 * @return
	 */
	private static ConsultaBooking direccionesUrls(ConsultaBooking consultaBooking) {

		HashSet<String> direccionesUrls = new HashSet<String>();

		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

		// Configurar opciones del navegador (por ejemplo, para cambiar el user agent)
		ChromeOptions options = new ChromeOptions();
		options.addArguments(
				"--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");

		// Crear una instancia del controlador de Chrome con las opciones configuradas
		WebDriver driver = new ChromeDriver(options);

		// Abrir la página web en el navegador
		driver.get(consultaBooking.getUrl());

		try {
			Thread.sleep(1000);//Le damos un segundo de respuesta para permitir que el componente responda
			WebDriverWait wait = new WebDriverWait(driver, 10);
			// Encontrar el campo de entrada para el destino y enviar la ubicación
			WebElement checkInField = driver.findElement(By.xpath("//button[@data-testid='date-display-field-start']"));
			checkInField.click();
			Thread.sleep(1000);
			WebElement destinationInput = driver.findElement(By.name("ss"));
			destinationInput.sendKeys(consultaBooking.getLugar());
			Thread.sleep(1000);

			// Ingresa las fechas de entrada y salida
			WebElement button = driver.findElement(By.cssSelector(
					"div[data-testid='searchbox-dates-container'] button[data-testid='date-display-field-end']"));
			Thread.sleep(1000);
			WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody")));
			// Hacer clic en el botón
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

		//Buscamos los hrf que contengan la cadena /hotel/es en su url
		for (Element repository : tags) {
			String href = repository.attr("href");
			if (href.contains("/hotel/es")) {
				direccionesUrls.add(href);
			}
		}
		consultaBooking.setDireccionesUrls(direccionesUrls);

		return consultaBooking;
	}

	private static void recorrerUrls(HashSet<String> direccionesUrls) {

		//Recorremos las distintas urls para obtener la información
		direccionesUrls.forEach(hotel -> {

			try {
				Thread.sleep(500);
				// Obtener el HTML de la página web
				Document doc = Jsoup.connect(hotel).get();

				Element h2Element = doc.select("h2").first();
				if (h2Element != null) {
					String title = h2Element.text();
					System.out.println("Nombre Hotel: " + title);
				} else {
					System.out.println("No se encontró ningún nombre.");
				}

				// Buscar los elementos que contienen la información de las habitaciones
				Elements roomElements = doc.select(".e2e-hprt-table-row");

				// Iterar sobre los elementos de habitación
				for (Element roomElement : roomElements) {
					// Obtener el tipo de habitación
					String roomType = roomElement.select(".hprt-roomtype-icon-link").text();

					// Obtener el número de personas
					int numPersons = roomElement.select(".c-occupancy-icons__adults").size();

					// Obtener el precio
					String priceString = roomElement.select(".prco-valign-middle-helper").text();
					double price = Double.parseDouble(priceString.replaceAll("[^0-9.,]+", ""));

					// Obtener las opciones de la habitación
					String options = roomElement.select(".bui-badge").text();

					// Imprimir los datos
					System.out.println("Tipo de habitación: " + roomType);
					System.out.println("Número de personas: " + numPersons);
					System.out.println("Precio: " + price);
					System.out.println("Opciones: " + options);
					System.out.println();
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		});
	}
	
	//TODO función para pasar a CSV

}
