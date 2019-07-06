package info.manuelmayer.licensed.violation;

import java.util.Collection;
import java.util.stream.Collectors;

public class LicenseViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Collection<LicenseViolation> reasons;
    
    public LicenseViolationException(Collection<LicenseViolation> reasons) {
        super();
        this.reasons = reasons;
    }

    @Override
    public String getMessage() {
        return "license violated: " + reasons.stream().map(LicenseViolation::getMessage).collect(Collectors.joining(","));
    }

}
