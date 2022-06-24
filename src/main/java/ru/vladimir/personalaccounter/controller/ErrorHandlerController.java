package ru.vladimir.personalaccounter.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller handle all /error request and depending on HttpStatus.code
 * returns error HTML page with information about Error code.
 * @author Vladimir Afonin
 *
 */
@Controller
public class ErrorHandlerController implements ErrorController {

	/**
	 * Handle /error request
	 * @param request - HttpServletRequest which contain Error code,
	 * @return - page with Error code.
	 */
	@RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
        
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500";
            }
        }
        return "error";
    }
	
	/**
	 * If user trying get resource which he doesn't have access to ,he redirected to this page
	 * @return - simple HTML access-denied page
	 */
	 
	@GetMapping("/access-denied")
	public String accessDeniedPage() {
		return "access-denied";
	}

}
