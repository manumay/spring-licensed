package info.manuelmayer.licensed.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import info.manuelmayer.licensed.boot.LicensingProperties;
import info.manuelmayer.licensed.model.Licensing;

@RunWith(MockitoJUnitRunner.class)
public class LicensingCacheTest {
	
	private static final String LICENSEKEY1 = "lk1", LICENSEKEY2 ="lk2";
	private static final String USER1 = "user1", USER2 = "user2", USER3 = "user3";

	@Mock
	private Licensing l1, l2, l3, l4;
	
	@Mock
	private LicensingProperties config;
	
	@Mock
	private LicensingRepository licensingRepository;
	
	@InjectMocks
	private LicensingCache cache = new LicensingCache();
	
	private List<Licensing> user1Licensings;
	private List<Licensing> user2Licensings;
	private List<Licensing> user3Licensings;
	
	@Before
	public void setup() {	
		user1Licensings = Arrays.asList(l1, l2);
		user2Licensings = Arrays.asList(l3, l4);
		user3Licensings = new ArrayList<>();
		
		when(config.getCacheExpireAfterAccessInS()).thenReturn(60L);
		when(config.getCacheMaximumSize()).thenReturn(10);
		when(config.getCacheRefreshAfterWriteInS()).thenReturn(60L);
		when(l1.getLicenseKey()).thenReturn(LICENSEKEY1);
		when(l2.getLicenseKey()).thenReturn(LICENSEKEY2);
		when(l3.getLicenseKey()).thenReturn(LICENSEKEY1);
		when(l4.getLicenseKey()).thenReturn(LICENSEKEY2);
		when(licensingRepository.findByActiveTrueAndUserLogin(USER1)).thenReturn(user1Licensings);
		when(licensingRepository.findByActiveTrueAndUserLogin(USER2)).thenReturn(user2Licensings);
		when(licensingRepository.findByActiveTrueAndUserLogin(USER3)).thenReturn(user3Licensings);
		
		cache.initCache();
	}
	
	@Test
	public void test_load_and_cache() {
		Map<String,Licensing> licensings1 = cache.get(USER1);
		assertThat(licensings1, aMapWithSize(2));
		assertThat(licensings1.get(LICENSEKEY1), sameInstance(l1));
		assertThat(licensings1.get(LICENSEKEY2), sameInstance(l2));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER1);
		
		Map<String,Licensing> cachedLicensings = cache.get(USER1);
		assertThat(cachedLicensings, sameInstance(licensings1));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER1);
	}
	
	@Test
	public void test_multiple_keys() {
		Map<String,Licensing> licensings1 = cache.get(USER1);
		assertThat(licensings1, aMapWithSize(2));
		assertThat(licensings1.get(LICENSEKEY1), sameInstance(l1));
		assertThat(licensings1.get(LICENSEKEY2), sameInstance(l2));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER1);
		
		Map<String,Licensing> licensings2 = cache.get(USER2);
		assertThat(licensings2, aMapWithSize(2));
		assertThat(licensings2.get(LICENSEKEY1), sameInstance(l3));
		assertThat(licensings2.get(LICENSEKEY2), sameInstance(l4));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER2);
	}
	
	@Test
	public void test_empty() {
		Map<String,Licensing> licensings3 = cache.get(USER3);
		assertThat(licensings3, anEmptyMap());
	}
	
	@Test
	public void test_invalidate() {
		cache.get(USER1);
		cache.get(USER2);
		cache.get(USER3);
		
		assertThat(cache.size(), equalTo(3L));
		verify(licensingRepository, times(3)).findByActiveTrueAndUserLogin(any(String.class));
		
		cache.invalidate(USER2);
		assertThat(cache.isCached(USER1), equalTo(Boolean.TRUE));
		assertThat(cache.isCached(USER2), equalTo(Boolean.FALSE));
		assertThat(cache.isCached(USER3), equalTo(Boolean.TRUE));
		
		cache.invalidate(USER1);
		assertThat(cache.isCached(USER1), equalTo(Boolean.FALSE));
		assertThat(cache.isCached(USER2), equalTo(Boolean.FALSE));
		assertThat(cache.isCached(USER3), equalTo(Boolean.TRUE));
		
		cache.invalidate(USER3);
		assertThat(cache.isCached(USER1), equalTo(Boolean.FALSE));
		assertThat(cache.isCached(USER2), equalTo(Boolean.FALSE));
		assertThat(cache.isCached(USER3), equalTo(Boolean.FALSE));
	}
	
	@Test
	public void test_invalidateAll() {
		cache.get(USER1);
		cache.get(USER2);
		cache.get(USER3);
		
		assertThat(cache.size(), equalTo(3L));
		verify(licensingRepository, times(3)).findByActiveTrueAndUserLogin(any(String.class));
		
		cache.invalidateAll();
		assertThat(cache.isCached(USER1), equalTo(Boolean.FALSE));
		assertThat(cache.isCached(USER2), equalTo(Boolean.FALSE));
		assertThat(cache.isCached(USER3), equalTo(Boolean.FALSE));
	}
	
	@Test
	public void test_isCached() {
		assertThat(cache.isCached(USER1), equalTo(Boolean.FALSE));
		
		cache.get(USER1);
		
		assertThat(cache.isCached(USER1), equalTo(Boolean.TRUE));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER1);
	}
	
	@Test
	public void test_size() {
		assertThat(cache.size(), equalTo(0L));
		
		cache.get(USER1);
		assertThat(cache.size(), equalTo(1L));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER1);
		
		cache.get(USER2);
		assertThat(cache.size(), equalTo(2L));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER2);
		
		cache.get(USER3);
		assertThat(cache.size(), equalTo(3L));
		verify(licensingRepository, times(1)).findByActiveTrueAndUserLogin(USER3);
		
		cache.invalidate(USER1);
		assertThat(cache.size(), equalTo(2L));
		
		cache.invalidateAll();
		assertThat(cache.size(), equalTo(0L));
	}
	
}
