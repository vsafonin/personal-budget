$(function() {

    $('#login-form-link').click(function(e) {
		$("#login-form").delay(100).fadeIn(100);
 		$("#register-form").fadeOut(100);
		$('#register-form-link').removeClass('active');
		localStorage.setItem('login','true');
		$(this).addClass('active');
		e.preventDefault();
	});
	$('#register-form-link').click(function(e) {
		$("#register-form").delay(100).fadeIn(100);
 		$("#login-form").fadeOut(100);
		$('#login-form-link').removeClass('active');
		localStorage.setItem('login','false');
		$(this).addClass('active');
		e.preventDefault();
	});
	
	window.onload = function() {
		if (localStorage.getItem('login') == 'true') {
					$("#login-form").delay(100).fadeIn(100);
 					$("#register-form").fadeOut(100);
					$('#register-form-link').removeClass('active');
					$('#register-form-link').addClass('active');
					e.preventDefault();
				}
		else {
					$("#register-form").delay(100).fadeIn(100);
 					$("#login-form").fadeOut(100);
					$('#login-form-link').removeClass('active');
					$('#login-form-link').addClass('active');
					e.preventDefault();
		}
	}

});