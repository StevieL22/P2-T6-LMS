package com.revature.lms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "app.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.security.jwt.issuer=https://lms.revature.com",
        "app.security.jwt.expiration=PT15M"
})
@ActiveProfiles("test")
class LmsApplicationTests {

	@Test
	void contextLoads() {
	}

}
