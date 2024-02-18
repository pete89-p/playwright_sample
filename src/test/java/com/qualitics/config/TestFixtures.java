package com.qualitics.config;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.google.gson.Gson;
import com.microsoft.playwright.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestFixtures {
	protected Playwright playwright;
	protected Browser browser;
	protected Browser.NewContextOptions contextOptions;
	protected ExtentReports extent;
	protected ExtentTest test;
	protected BrowserContext context;
	protected Page page;

	@SneakyThrows
	@BeforeAll
	void launchBrowser() {
		ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/extentReport.html");
		extent = new ExtentReports();
		extent.attachReporter(htmlReporter);

		var browserType = System.getProperty("browser");
		if (browserType == null) {
			throw new IllegalArgumentException("Browser property variable is not set.");
		}

		var configPath = "src/test/resources/config/browsers/" + browserType.toLowerCase() + ".json";
		var configJson = new String(Files.readAllBytes(Paths.get(configPath)));

		var gson = new Gson();
		var options = gson.fromJson(configJson, BrowserType.LaunchOptions.class);

		var playwright = Playwright.create();
		browser = switch (browserType) {
			case "chromium" -> playwright.chromium().launch(options);
			case "firefox" -> playwright.firefox().launch(options);
			case "webkit" -> playwright.webkit().launch(options);
			default -> throw new IllegalArgumentException("Unsupported browser: " + browserType);
		};

		contextOptions = new Browser.NewContextOptions();
		contextOptions.setViewportSize(null); // Workaround for window maximizing in Chrome

	}

	@BeforeEach
	void createContextAndPage(TestInfo testInfo) {
		test = extent.createTest(testInfo.getDisplayName());
		context = browser.newContext(contextOptions);
		page = context.newPage();
	}

	@SneakyThrows
	@AfterEach
	void afterMethod(TestInfo testInfo) {
		if (testInfo.getTags().contains("fail")) {
			var screenshotName = "screenshot.png";
			var screenshotPath = "screenshots/"+screenshotName;
			var path = Paths.get(screenshotPath);
			page.screenshot(new Page.ScreenshotOptions().setPath(path));
			screenshotPath = screenshotPath.replace(File.separator, "/");
			test.addScreenCaptureFromPath("/"+screenshotPath, "Screenshot for " + testInfo.getDisplayName());
			test.log(Status.FAIL,   "Test failed");
		} else {
			test.log(Status.PASS, "Test passed");
		}

		extent.flush();

		if (context != null) {
			context.close();
		}
	}

	@AfterAll
	void closeBrowser() {
		if (playwright != null) {
			playwright.close();
		}
	}

}
