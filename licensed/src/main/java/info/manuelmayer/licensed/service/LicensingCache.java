package info.manuelmayer.licensed.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import info.manuelmayer.licensed.boot.LicensingProperties;
import info.manuelmayer.licensed.model.Licensing;

public class LicensingCache {

	@Autowired
	private LicensingProperties config;
	
	@Autowired
	private LicensingRepository licensingRepository;
	
	private LoadingCache<String,Map<String,Licensing>> cache;

    private final Log log = LogFactory.getLog(getClass());
    
    public Map<String,Licensing> get(String key) {
    	checkCacheState();
    	
    	try {
			return cache.get(key);
		} catch (ExecutionException e) {
			log.error("couldn't cache value for key " + key, e);
			throw new RuntimeException(e);
		}
    }
    
    public Map<String,Licensing> getIfPresent(String key) {
    	checkCacheState();
    	return cache.getIfPresent(key);
    }
    
    public void invalidate(String key) {
    	checkCacheState();

    	cache.invalidate(key);
    	log.debug("invalidated cache value for key " + key);
    }
    
    public void invalidateAll() {
    	checkCacheState();
    	cache.invalidateAll();
    	log.debug("invalidated cache");
    }
    
    public boolean isCached(String key) {
    	checkCacheState();
    	return getIfPresent(key) != null;
    }
        
    public long size() {
    	checkCacheState();
    	return cache.size();
    }
    
    @PostConstruct
    public final void initCache() {
    	CacheLoader<String,Map<String,Licensing>> cacheLoader = new CacheLoader<>() {
			@Override
			public Map<String,Licensing> load(String key) throws Exception {
				return LicensingCache.this.load(key);
			}
		};
    	
    	this.cache = CacheBuilder.newBuilder()
        		.maximumSize(getMaximumSize())
        		.expireAfterAccess(getExpireAfterAccessInS(), TimeUnit.SECONDS)
        		.refreshAfterWrite(getRefreshAfterWriteInS(), TimeUnit.SECONDS)
        		.build(cacheLoader);
    }
        
    protected boolean isAutoInit() {
    	return false;
    }
    
    private void checkCacheState() {
    	if (cache == null) {
    		if (!isAutoInit()) {
    			throw new IllegalStateException("cache has not been initialized");
    		}
    		initCache();
    	}
    }
    
    protected Map<String, Licensing> load(String userLogin) {
    	List<Licensing> licensings = licensingRepository.findByActiveTrueAndUserLogin(userLogin);
        return licensings.stream()
        		.collect(Collectors.toMap(Licensing::getLicenseKey, l -> l));
    }
    
    protected int getMaximumSize() {
    	return config.getCacheMaximumSize();
    }
    
    protected long getExpireAfterAccessInS() {
    	return config.getCacheExpireAfterAccessInS();
    }
    
    protected long getRefreshAfterWriteInS() {
    	return config.getCacheRefreshAfterWriteInS();
    }

}
