package info.manuelmayer.licensed.application;

import org.springframework.security.core.context.SecurityContextHolder;

import info.manuelmayer.licensed.service.SecurityContext;

public class SecurityContextImpl implements SecurityContext {

	@Override
	public String getUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

}
