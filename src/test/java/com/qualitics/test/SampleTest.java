package com.qualitics.test;

import com.qualitics.config.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SampleTest extends TestFixtures {

	@Test()
	@DisplayName("Sample Test with Playwright and Extent Reports")
	public void test() {
		page.navigate("https:/wyborcza.pl");
	}

}
