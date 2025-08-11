    /* ====================================================================
       UTILITY & UI FUNCTIONS (SHOW MESSAGE, CARD HEIGHT, LOADER)
    ====================================================================
    */
   
	function clearLoginForm() {
	    const loginForm = document.getElementById('loginForm');
	    const usernameInput = document.getElementById('username');
	    const passwordInput = document.getElementById('password');

	    if (loginForm && usernameInput && passwordInput) {
	        usernameInput.value = '';
	        passwordInput.value = '';
	        loginForm.reset();
	    }
	}

    function showMessage(type, text) {
        const iconMap = { success: 'check_circle', error: 'error', info: 'info' };
        const overlay = document.getElementById('customMessageOverlay');
        const content = document.getElementById('customMessageContent');
        const iconEl = document.getElementById('customMessageIcon');
        const textEl = document.getElementById('customMessageText');
        const okBtn = document.getElementById('customMessageOkBtn');

        if (!overlay || !content || !iconEl || !textEl || !okBtn) {
            console.error("Custom message overlay elements not found in DOM!");
            return;
        }

        iconEl.textContent = iconMap[type] || 'info';
        content.classList.remove('success', 'error', 'info');
        content.classList.add(type);
        textEl.textContent = text;

        okBtn.style.backgroundColor = getComputedStyle(document.documentElement).getPropertyValue(`--${type}`);
        overlay.classList.add('show');

        const okButtonListener = () => {
            overlay.classList.remove('show');
            const cleanURL = window.location.origin + window.location.pathname;
            history.replaceState(null, '', cleanURL);
            okBtn.removeEventListener('click', okButtonListener);
        };
        okBtn.addEventListener('click', okButtonListener);
    }

    function setCardHeight(card) {
        if (card) {
            card.style.maxHeight = card.scrollHeight + 'px';
        }
    }

    function toggleRoleSelector(show) {
        const roleSelector = document.getElementById('segmentedbutton');
        if (roleSelector) {
            roleSelector.style.display = show ? 'flex' : 'none';
        }
    }

    function showLoader(message = 'Please wait...') {
        const overlay = document.getElementById("loadingOverlay");
        if (overlay) {
            overlay.style.display = "flex";
        }
    }
//vdf//
    function hideLoader() {
        const overlay = document.getElementById("loadingOverlay");
        if (overlay) {
            overlay.style.display = "none";
        }
    }

    /* ====================================================================
       VIEW SWITCHING LOGIC (LOGIN, REGISTER, FORGOT PASSWORD)
    ====================================================================
    */

    function switchToRegister() {
        const loginCard = document.querySelector('.login-card');
        const title = loginCard.querySelector('h6.poppins-title');
        const loginForm = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const forgotPasswordSection = document.getElementById('forgotPasswordSection');

        if (title) title.classList.add('title-hidden');
        if (loginForm) loginForm.style.display = 'none';
        if (forgotPasswordSection) forgotPasswordSection.style.display = 'none';
        if (registerForm) registerForm.style.display = 'block';

        toggleRoleSelector(true);
        loginCard.classList.add('scrollable');
        loginCard.style.maxHeight = '85vh';
    }

    function switchToLogin() {
        const loginCard = document.querySelector('.login-card');
        const title = loginCard.querySelector('h6.poppins-title');
        const loginForm = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const forgotPasswordSection = document.getElementById('forgotPasswordSection');

        if (registerForm) registerForm.style.display = 'none';
        if (forgotPasswordSection) forgotPasswordSection.style.display = 'none';
        if (title) title.classList.remove('title-hidden');
        if (loginForm) loginForm.style.display = 'block';

        toggleRoleSelector(true);
        loginCard.classList.remove('scrollable');
        setCardHeight(loginCard);
    }

    function showForgotPasswordForm() {
        const loginForm = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const forgotPasswordSection = document.getElementById('forgotPasswordSection');
        
        if (loginForm) loginForm.style.display = 'none';
        if (registerForm) registerForm.style.display = 'none';
        if (forgotPasswordSection) forgotPasswordSection.style.display = 'block';

        toggleRoleSelector(false);

        document.getElementById('forgotEmailStep').style.display = 'block';
        document.getElementById('forgotOtpStep').style.display = 'none';
        document.getElementById('setNewPasswordStep').style.display = 'none';

        // Clear previous inputs
        document.getElementById('forgotEmail').value = '';
        document.querySelectorAll('.otp-input').forEach(input => input.value = '');
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmNewPassword').value = '';

        setCardHeight(document.querySelector('.login-card'));
    }

    /* ====================================================================
       FORGOT PASSWORD FEATURE FUNCTIONS (AJAX)
    ====================================================================
    */

    function sendOtp() {
        const emailInput = document.getElementById('forgotEmail');
        const email = emailInput.value.trim().toLowerCase();
        
        // **RESTORED: Email domain validation**
        if (!email.endsWith('.com') && !email.endsWith('.in') && !email.endsWith('.edu')) {
            showMessage('error', 'Please enter a valid email ending with .com, .in, or .edu.');
            return;
        }

        showLoader('Sending OTP...');
        fetch('ForgotPasswordServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=sendOTP&email=${encodeURIComponent(email)}`
        })
        .then(res => res.json())
        .then(data => {
            hideLoader();
            showMessage(data.status, data.message);
            if (data.status === 'success') {
                document.getElementById('forgotEmailStep').style.display = 'none';
                document.getElementById('forgotOtpStep').style.display = 'block';
                setCardHeight(document.querySelector('.login-card'));
            }
        })
        .catch(() => {
            hideLoader();
            showMessage('error', 'A network error occurred. Please try again.');
        });
    }


    function verifyOtp() {
        const otp = Array.from(document.querySelectorAll('.otp-input')).map(i => i.value).join('');
        if (otp.length !== 6) {
            showMessage('error', 'Please enter the full 6-digit OTP.');
            return;
        }
        showLoader('Verifying OTP...');
        fetch('ForgotPasswordServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=verifyOTP&otp=${otp}`
        })
        .then(res => res.json())
        .then(data => {
            hideLoader();
            showMessage(data.status, data.message);
            if (data.status === 'success') {
                document.getElementById('forgotOtpStep').style.display = 'none';
                document.getElementById('setNewPasswordStep').style.display = 'block';
                setCardHeight(document.querySelector('.login-card'));
            }
        })
        .catch(() => {
            hideLoader();
            showMessage('error', 'A network error occurred. Please try again.');
        });
    }

    function resetPassword() {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmNewPassword').value;

        if (!newPassword || newPassword.length < 6) {
            showMessage('error', 'Password must be at least 6 characters long.');
            return;
        }
        if (newPassword !== confirmPassword) {
            showMessage('error', 'New passwords do not match.');
            return;
        }
        showLoader('Resetting Password...');
        fetch('ForgotPasswordServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=resetPassword&newPassword=${encodeURIComponent(newPassword)}&confirmPassword=${encodeURIComponent(confirmPassword)}`
        })
        .then(res => res.json())
        .then(data => {
            hideLoader();
            showMessage(data.status, data.message);
            if (data.status === 'success') {
                switchToLogin();
            }
        })
        .catch(() => {
            hideLoader();
            showMessage('error', 'A network error occurred. Please try again.');
        });
    }


    /* ====================================================================
       DOM CONTENT LOADED - MAIN SCRIPT LOGIC
    ====================================================================
    */

    document.addEventListener('DOMContentLoaded', () => {
		

		window.addEventListener('pageshow', function(event) {
		       // This event fires every single time the login page is displayed.
		       const loginForm = document.getElementById('loginForm');
		       if (loginForm) {
		           // Reset the form, clearing the username and password fields.
		           loginForm.reset();
		       }

		       // Hide any loading overlays that might have gotten stuck.
		       const loadingOverlay = document.getElementById('loadingOverlay');
		       if (loadingOverlay) {
		           loadingOverlay.style.display = 'none';
		       }
		   });
		   
		   const urlParams = new URLSearchParams(window.location.search);
		      if (urlParams.get('status') === 'logged_out') {
		          // This removes the "?status=logged_out" from the URL bar
		          // and, more importantly, it replaces the current history entry.
		          // This effectively deletes the "forward" path to the dashboard.
		          history.replaceState(null, document.title, window.location.pathname);
		          console.log("Forward history cleared after logout.");
		      }
	

        const loginCard = document.querySelector('.login-card');
        const pwField = document.getElementById('password');
        const toggle = document.getElementById('togglePw');
        const loginRoleInput = document.getElementById('loginRoleInput');
        const registerRoleInput = document.getElementById('registerRoleInput');
        const loginForm = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const sendOtpButton = document.getElementById('sendOtpBtn');
        const verifyOtpButton = document.getElementById('verifyOtpBtn');
        const resetPasswordButton = document.getElementById('resetPasswordBtn');
        const newPwToggle = document.getElementById('toggleNewPw');
        const confirmNewPwToggle = document.getElementById('toggleConfirmNewPw');
        const newPasswordField = document.getElementById('newPassword');
        const confirmNewPasswordField = document.getElementById('confirmNewPassword');
        const otpInputs = document.querySelectorAll('.otp-input');
		

        // --- Event Listeners ---
        if (toggle && pwField) {
            toggle.addEventListener('click', () => {
                pwField.type = pwField.type === 'password' ? 'text' : 'password';
                toggle.textContent = pwField.type === 'password' ? 'visibility_off' : 'visibility';
            });
        }
        if (newPwToggle && newPasswordField) {
            newPwToggle.addEventListener('click', () => {
                newPasswordField.type = newPasswordField.type === 'password' ? 'text' : 'password';
                newPwToggle.textContent = newPasswordField.type === 'password' ? 'visibility_off' : 'visibility';
            });
        }
        if (confirmNewPwToggle && confirmNewPasswordField) {
            confirmNewPwToggle.addEventListener('click', () => {
                confirmNewPasswordField.type = confirmNewPasswordField.type === 'password' ? 'text' : 'password';
                confirmNewPwToggle.textContent = confirmNewPasswordField.type === 'password' ? 'visibility_off' : 'visibility';
            });
        }

        if (loginForm) loginForm.addEventListener("submit", () => showLoader('Logging In...'));
        if (registerForm) registerForm.addEventListener("submit", () => showLoader('Registering...'));

        if (sendOtpButton) sendOtpButton.addEventListener('click', sendOtp);
        if (verifyOtpButton) verifyOtpButton.addEventListener('click', verifyOtp);
        if (resetPasswordButton) resetPasswordButton.addEventListener('click', resetPassword);

        otpInputs.forEach((input, index) => {
            input.addEventListener('input', () => {
                if (input.value.length === 1 && index < otpInputs.length - 1) {
                    otpInputs[index + 1].focus();
                }
            });
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && input.value.length === 0 && index > 0) {
                    otpInputs[index - 1].focus();
                }
            });
        });

        function setRole(role) {
            document.getElementById('container').classList.toggle('active', role === 'faculty');
            document.getElementById('container2').classList.toggle('active', role === 'student');
            loginRoleInput.value = role;
            registerRoleInput.value = role;
            const dynamicRegUsernameLabel = document.querySelector('#registerForm label[for="regUsername"]');
            if (dynamicRegUsernameLabel) {
                dynamicRegUsernameLabel.textContent = (role === 'faculty') ? 'USERNAME:' : 'USERID:';
            }
            setTimeout(() => setCardHeight(loginCard), 0);
        }

        const facultySegment = document.querySelector('[data-role="faculty"]');
        const studentSegment = document.querySelector('[data-role="student"]');
        if (facultySegment) facultySegment.addEventListener('click', () => setRole('faculty'));
        if (studentSegment) studentSegment.addEventListener('click', () => setRole('student'));

        // --- URL Parameter Handling ---
        const p = new URLSearchParams(window.location.search);
        const loginStatus = p.get("login");
        const regStatus = p.get("reg");
        const forgotStatus = p.get("forgot");
		if (document.referrer.includes('logout')) {
		       clearLoginForm();
		   }
	

        if (loginStatus === "fail" || regStatus === "fail" || forgotStatus === "fail") {
            let reason = p.get("reason");
            let errorMessage = "An error occurred.";

            if (loginStatus) {
                errorMessage = "Login failed!";
                switch (reason) {
                    case "missingfields": errorMessage = "Please enter both username and password."; break;
                    case "invalid": errorMessage = "Invalid username or password. Please try again."; break;
                    case "pendingapproval": errorMessage = "Your faculty account is pending admin approval."; break;
                }
            } else if (regStatus) {
                errorMessage = "Registration failed!";
                switch (reason) {
                    case "missingfields": errorMessage = "Please fill in all required fields."; break;
                    case "nomatch": errorMessage = "Passwords do not match."; break;
                    case "invalidid": errorMessage = "Invalid Student ID format (must be a number)."; break;
                    case "studentnotfound": errorMessage = "Student ID not found or has not been registered!! \nKindly Contact Your Faculty."; break;
                    case "alreadyregistered": errorMessage = "An account for this student already exists."; break;
                    case "usernametaken": errorMessage = "This username is already taken."; break;
                    case "emailtaken": errorMessage = "This email is already registered."; break;
                    case "invalidemaildomain": errorMessage = "Invalid email domain. Must be .com, .in, or .edu."; break;
                    case "invalidfacultyusername": errorMessage = "Faculty username cannot be purely numeric."; break;
                    case "dberror": errorMessage = "A server error occurred. Please try again later."; break;
                }
                switchToRegister();
            } else if (forgotStatus) {
                errorMessage = "Password reset failed!";
                // Add specific forgot password reasons if your servlet provides them
                showForgotPasswordForm();
            }
            showMessage('error', errorMessage);
        } else if (regStatus === "success") {
            showMessage('success', 'Registration successful! You can now log in.');
        }

        // --- Dark Mode ---
        const darkModeToggle = document.getElementById('darkModeToggle');
        const darkModeIcon = document.getElementById('darkModeIcon');
        const body = document.body;
        const savedTheme = localStorage.getItem('theme');
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        if (savedTheme === 'dark' || (savedTheme === null && prefersDark)) {
            body.classList.add('dark-mode');
            if (darkModeIcon) darkModeIcon.textContent = 'wb_sunny';
        } else {
            if (darkModeIcon) darkModeIcon.textContent = 'brightness_2';
        }
        if (darkModeToggle) {
            darkModeToggle.addEventListener('click', () => {
                body.classList.toggle('dark-mode');
                localStorage.setItem('theme', body.classList.contains('dark-mode') ? 'dark' : 'light');
                darkModeIcon.textContent = body.classList.contains('dark-mode') ? 'wb_sunny' : 'brightness_2';
            });
        }

        // Initial setup
        setRole('faculty');
        setCardHeight(loginCard);
    });