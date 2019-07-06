package info.manuelmayer.licensed.application;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import info.manuelmayer.licensed.annotation.Licensed;
import info.manuelmayer.licensed.model.Licensing;

@Controller
public class LicensedController {
	
	@Autowired
	private LicensingJPARepository repo;
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody List<Licensing> getLicensing() {
		return repo.findAll();
	}

	@GetMapping("yes")
	@Licensed(key = "licensed", feature = "yes")
	public @ResponseBody String getFeatureYes() {
		return "yes";
	}
	
	@GetMapping("no")
	@Licensed(key = "licensed", feature = "no")
	public @ResponseBody String getFeatureNo() {
		return "no";
	}
	
	@GetMapping("user")
	@Licensed(key = "licensed", currentUser=true)
	public @ResponseBody String getUser() {
		return "user";
	}
	
}
