// js/theme.js

// This listener ensures the entire script waits until the HTML document is fully loaded.
document.addEventListener('DOMContentLoaded', () => {

    /**
     * Applies the theme by adding or removing the 'dark' class from the body.
     * @param {string} theme - The theme to apply ('dark' or 'light').
     */
    function applyTheme(theme) {
        const body = document.body;
        const modeText = document.querySelector('.mode-text'); // For faculty dashboard
        const loginIcon = document.getElementById('darkModeIcon'); // For login page
        
        // This handles the student dashboard's light/dark mode CSS
        if (theme === 'dark') {
            body.classList.add('dark');
            body.classList.remove('light-mode');
        } else {
            body.classList.remove('dark');
            body.classList.add('light-mode');
        }

        // This handles the text/icon changes on the different toggles
        if (modeText) modeText.innerText = (theme === 'dark') ? 'Light mode' : 'Dark mode';
        if (loginIcon) loginIcon.innerText = (theme === 'dark') ? 'wb_sunny' : 'brightness_2';
    }

    /**
     * Saves the chosen theme to localStorage and the server session.
     * @param {string} theme - The theme to save ('dark' or 'light').
     */
    function saveThemePreference(theme) {
        localStorage.setItem('theme', theme);
        fetch('ThemeServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'theme=' + theme
        }).catch(error => console.error('Error saving theme to session:', error));
    }

    /**
     * Toggles the theme, applies it, and saves the new preference.
     */
    function toggleTheme() {
        // We use 'dark' as our standard class for checking the theme
        const isCurrentlyDark = document.body.classList.contains('dark');
        const newTheme = isCurrentlyDark ? 'light' : 'dark';
        applyTheme(newTheme);
        saveThemePreference(newTheme);
    }

    // --- INITIAL THEME SETUP ---
    // This code now runs safely after the body has been loaded.
    const serverTheme = document.body.classList.contains('dark') ? 'dark' : 'light';
    const localTheme = localStorage.getItem('theme') || 'light';

    if (serverTheme !== localTheme) {
        applyTheme(localTheme);
        saveThemePreference(localTheme);
    }
    
    // --- EVENT LISTENER USING DELEGATION ---
    // This listens for clicks on the entire page.
    document.addEventListener('click', function(event) {
        // Check for faculty dashboard toggle
        if (event.target.closest('.sidebar .mode .toggle-switch')) {
            toggleTheme();
            return;
        }
        // Check for login page toggle
        if (event.target.closest('#darkModeToggle')) {
            toggleTheme();
            return;
        }
        // Check for student dashboard toggle
        if (event.target.closest('.theme-toggle')) {
            toggleTheme();
            return;
        }
    });
});