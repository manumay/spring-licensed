package info.manuelmayer.licensed.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.annotation.AnnotationUtils;

import info.manuelmayer.licensed.annotation.Licensed;
import info.manuelmayer.licensed.boot.LicensingProperties;
import info.manuelmayer.licensed.model.Licensing;
import info.manuelmayer.licensed.service.LicensingService;
import info.manuelmayer.licensed.violation.LicenseChecker;
import info.manuelmayer.licensed.violation.LicenseViolation;
import info.manuelmayer.licensed.violation.LicenseViolationException;

public class LicensedInterceptor implements MethodInterceptor,BeanFactoryAware {

    private BeanFactory beanFactory;
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (beanFactory == null) {
            return invocation.proceed();
        }
        Method method = invocation.getMethod();
        Licensed licensed = AnnotationUtils.findAnnotation(method, info.manuelmayer.licensed.annotation.Licensed.class);
        
        if (licensed == null) {
            licensed = AnnotationUtils.findAnnotation(method.getDeclaringClass(), Licensed.class);
        }

        if (licensed != null) {
        	String key = licensed.key();
            List<LicenseViolation> violations = new ArrayList<>();
            LicenseChecker licenseChecker = beanFactory.getBean(LicenseChecker.class);
            if (licensed.currentDevice()) {
                Optional<LicenseViolation> violation =  licenseChecker.getViolationCurrentDevice(key);
                if (violation.isPresent()) {
                    LicensingProperties config = beanFactory.getBean(LicensingProperties.class);
                    if (config.isAutoAssignDeviceLicenses()) {
                        LicensingService licensingService = beanFactory.getBean(LicensingService.class);
                        Licensing licensing = licensingService.assignLicenseToCurrentDevice(key);
                        if (licensing == null || Boolean.FALSE.equals(licensing.getActive())) {
                            violations.add(violation.get());
                        }
                    } else {
                        violations.add(violation.get());
                    }
                }
            }
            if (licensed.currentUser()) {
                Optional<LicenseViolation> violation = licenseChecker.getViolationCurrentUser(key);                
                if (violation.isPresent()) {
                    LicensingProperties config = beanFactory.getBean(LicensingProperties.class);
                    if (config.isAutoAssignUserLicenses()) {
                        LicensingService licensingService = beanFactory.getBean(LicensingService.class);
                        Licensing licensing = licensingService.assignLicenseToCurrentUser(key);
                        if (licensing == null || Boolean.FALSE.equals(licensing.getActive())) {
                            violations.add(violation.get());
                        }
                    } else {
                        violations.add(violation.get());
                    }
                }
            }
            if (licensed.devices()) {
                Optional<LicenseViolation> violation = licenseChecker.getViolationMaxDevices(key);
                if (violation.isPresent()) {
                    violations.add(violation.get());
                }
            }
            if (licensed.host()) {
                Optional<LicenseViolation> violation = licenseChecker.getViolationHost(key);
                if (violation.isPresent()) {
                    violations.add(violation.get());
                }
            }
            if (licensed.period()) {
                Optional<LicenseViolation> violation = licenseChecker.getViolationPeriod(key);
                if (violation.isPresent()) {
                    violations.add(violation.get());
                }
            }
            if (licensed.users()) {
                Optional<LicenseViolation> violation = licenseChecker.getViolationMaxUsers(key);
                if (violation.isPresent()) {
                    violations.add(violation.get());
                }
            }
            if (licensed.version()) {
                Optional<LicenseViolation> violation = licenseChecker.getViolationVersion(key);
                if (violation.isPresent()) {
                    violations.add(violation.get());
                }
            }
            
            String feature = licensed.feature();
            if (feature != null && feature.length() > 0) {
                Optional<LicenseViolation> violation = licenseChecker.getViolationFeature(key, feature);
                if (violation.isPresent()) {
                    violations.add(violation.get());
                }
            }
            
            if (!violations.isEmpty()) {
                throw new LicenseViolationException(violations);
            }
        }
        
        return invocation.proceed();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
