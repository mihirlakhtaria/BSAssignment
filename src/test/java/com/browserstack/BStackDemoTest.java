package com.browserstack;

import com.browserstack.SeleniumTest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class BStackDemoTest extends SeleniumTest {
    @Test
    public void Assignment() throws Exception {
		Map<String, Integer> wordAnalysis = new HashMap<>();
		
		// Open the given website
		driver.get("https://elpais.com/");
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		// Accept the cookie message when appear
		List<WebElement> CookieButton = driver.findElements(By.id("didomi-notice-agree-button"));
		if (CookieButton.size() > 0) {
			wait.until(ExpectedConditions.elementToBeClickable(By.id("didomi-notice-agree-button")));
			CookieButton.get(0).click();
		}

		// Verify that the current page language is Spanish
		String languageText = "ESPAÑA";
		String actualText = driver.findElement(By.className("ed_a")).getText();
		assertTrue(actualText. equalsIgnoreCase(languageText));

		// Click on the Opinion Section
		driver.findElement(By.xpath("//a[@href='https://elpais.com/opinion/']")).click();

		// Get the first 5 Opinions
		wait.until(ExpectedConditions.elementToBeClickable(By.tagName("article")));
		List<WebElement> articles = driver.findElements(By.tagName("article"));

		for (int i = 0; i < articles.size(); i++) {
			if (i == 0) {
				System.out.println("*********************************************************");
			}
			System.out.println("This Article number: " + (i + 1));
			System.out.println("Article Title: " + articles.get(i).findElement(By.tagName("h2")).getText());
			System.out.println("Article Content: " + articles.get(i).findElement(By.tagName("p")).getText());
			
			// If image is available, save the image to machine
			List<WebElement> hasImage = articles.get(i).findElements(By.tagName("img"));
			if (hasImage.size() > 0) {
				String url = hasImage.get(0).getDomProperty("src");
				String filename = "downloaded_image" + i + ".jpg";

				try {
					URL imageUrl = new URL(url);
					BufferedImage image = ImageIO.read(imageUrl);
					ImageIO.write(image, "jpg", new File(filename));
					System.out.println("Image downloaded with name: " + filename);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//HTTP Request to translate the titles - Commenting since out of tokens!
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://rapid-translate-multi-traduction.p.rapidapi.com/t"))
					.header("x-rapidapi-key", "bb6a451ee0msh7b1bad88dbbbe5fp1db8a1jsn8f4b08780276")
					.header("x-rapidapi-host", "rapid-translate-multi-traduction.p.rapidapi.com")
					.header("Content-Type", "application/json")
					.method("POST", HttpRequest.BodyPublishers.ofString("{\"from\":\"es\",\"to\":\"en\",\"q\":\""
							+ articles.get(i).findElement(By.tagName("h2")).getText() + "\"}"))
					.build();
			HttpResponse<String> response;
			try {
				response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

				//Since the response of API is in ["Response_Body"] format, need to parse string for printing
				JSONArray responseJSON = new JSONArray(response.body());
				
				System.out.println("Translation of Title is: " + responseJSON.getString(0));
				
				String[] words = responseJSON.getString(0).toLowerCase().split("\\W+");
				
	            for (String word : words) {
	                if (!word.isEmpty()) {
	                    // Update the word count in the map
	                    wordAnalysis.put(word, wordAnalysis.getOrDefault(word, 0) + 1);
	                }
	            }
				
			} catch (Exception e) {
				System.out.println("Error while translating: " + e);
				e.printStackTrace();
			}
			
				System.out.println("*********************************************************");
			// We only want top 5 articles, so skipping the rest
			if (i == 4) {
				break;
			}
		}
		
		//Print the Word Analysis - Commenting this as well
		System.out.println("List of words having count more than 2 (2 is not considered): ");
		boolean emptyCount = true;
        for (Map.Entry<String, Integer> entry : wordAnalysis.entrySet()) {
        	// Only print words that occur more than 2 times
            if (entry.getValue() > 2) {
            	emptyCount = false;
            System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        if (emptyCount) {System.out.println("No words having count more than 2");}
		
		// Close the browser
		driver.quit();
    }
}
