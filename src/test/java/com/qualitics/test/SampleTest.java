package com.qualitics.test;

import com.aventstack.extentreports.Status;
import com.qualitics.config.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SampleTest extends TestFixtures {

	@Test()
	@DisplayName("natemat")
	public void sevenTest() {
		test.log(Status.PASS, "Opening https://natemat.pl page");
		page.navigate("https://natemat.pl");
	}

	@Test()
	@DisplayName("interia")
	public void eightTest() {
		test.log(Status.PASS, "Opening https://interia.pl page");
		page.navigate("https://interia.pl");
	}
}

