package info.manuelmayer.licensed.test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import javax.servlet.http.HttpServletRequest;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import info.manuelmayer.licensed.boot.LicensingConfiguration;
import info.manuelmayer.licensed.service.LicensingRepository;
import info.manuelmayer.licensed.service.SecurityContext;
import info.manuelmayer.licensed.test.BaseLicensingIT.LicensedTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= { LicensingConfiguration.class, LicensedTestConfiguration.class })
@ActiveProfiles("test")
public abstract class BaseLicensingIT {
	
	public static final String APPLICATION_KEY = "application";
		
	@MockBean
	protected LicensedCode code;
	
	@MockBean
	protected LicensingRepository licensingRepo;
	
	@MockBean
	protected HttpServletRequest request;
	
	@MockBean
	protected SecurityContext securityContext;
	
	@TestConfiguration
	@Profile("test")
	public static class LicensedTestConfiguration {
		
		@Bean
		public TestClock clock() {
			return new TestClock(LocalDate.of(2016, 6, 4).atStartOfDay().toInstant(ZoneOffset.UTC));
		}
		
		@Bean
		@Scope(proxyMode=ScopedProxyMode.TARGET_CLASS)
		public LicensedObject licensedObject(LicensedCode code) {
			return new LicensedObjectImpl(code);
		}
		
	}
}
