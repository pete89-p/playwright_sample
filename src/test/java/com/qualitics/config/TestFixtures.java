package com.qualitics.config;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.google.gson.Gson;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TestFixtures {
	public static ExtentReports extent;
	public ExtentTest test;
	public Browser browser;
	public Page page;
	public BrowserContext context;

	@BeforeAll
	static void setupReporter() {
		ExtentSparkReporter htmlReporter = new ExtentSparkReporter("target/extentReport.html");
		extent = new ExtentReports();
		extent.attachReporter(htmlReporter);
	}

	@AfterAll
	static void tearDownClass() {
		if (extent != null) {
			extent.flush();
		}
	}

	@BeforeEach
	void setUp(TestInfo testInfo) throws Exception {
		this.test = extent.createTest(testInfo.getDisplayName());

		String browserType = System.getProperty("browser", "chromium");
		String configPath = "src/test/resources/config/browsers/" + browserType.toLowerCase() + ".json";
		String configJson = new String(Files.readAllBytes(Paths.get(configPath)));
		BrowserType.LaunchOptions options = new Gson().fromJson(configJson, BrowserType.LaunchOptions.class);

		Playwright playwright = Playwright.create();
		this.browser = switch (browserType) {
			case "chromium" -> playwright.chromium().launch(options);
			case "firefox" -> playwright.firefox().launch(options);
			case "webkit" -> playwright.webkit().launch(options);
			default -> throw new IllegalArgumentException("Unsupported browser: " + browserType);
		};

		context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
		this.page = context.newPage();
	}

	@AfterEach
	void tearDown(TestInfo testInfo) {
		if (this.page != null) {
			var screenshotName = testInfo.getDisplayName() + ".png";
			var screenshotPath = "screenshots/" + screenshotName;
			var path = Paths.get(screenshotPath);
			page.screenshot(new Page.ScreenshotOptions().setPath(path));
			screenshotPath = screenshotPath.replace(File.separator, "/");
			test.addScreenCaptureFromPath("/" + screenshotPath, "Screenshot for " + testInfo.getDisplayName());
			context.close();
			this.browser.close();
		}

		if (testInfo.getTags().contains("fail")) {
			this.test.fail("Test failed");
		} else {
			this.test.pass("Test passed");
		}
	}
}

