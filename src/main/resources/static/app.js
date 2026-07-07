const output = document.querySelector("#output");
const messageBox = document.querySelector("#messageBox");
const loginForm = document.querySelector("#loginForm");
const registerForm = document.querySelector("#registerForm");
const userInfo = document.querySelector("#userInfo");
const userName = document.querySelector("#userName");
const logoutBtn = document.querySelector("#logoutBtn");
const loginContainer = document.querySelector("#loginContainer");
const registerContainer = document.querySelector("#registerContainer");
const registerBtn = document.querySelector("#registerBtn");
const backBtn = document.querySelector("#backBtn");
const forgotPasswordLink = document.querySelector("#forgotPasswordLink");

let currentUser = null;

const show = (value) => {
    output.textContent = typeof value === "string" ? value : JSON.stringify(value, null, 2);
};

const showMessage = (message, type = 'success', boxElement = messageBox) => {
    boxElement.textContent = message;
    boxElement.className = `message-box ${type}`;
    
    setTimeout(() => {
        boxElement.classList.add('hidden');
    }, 5000);
};

const showLoginScreen = () => {
    loginContainer.classList.remove('hidden');
    registerContainer.classList.add('hidden');
    userInfo.classList.add('hidden');
    loginForm.reset();
};

const showRegisterScreen = () => {
    loginContainer.classList.add('hidden');
    registerContainer.classList.remove('hidden');
    userInfo.classList.add('hidden');
    registerForm.reset();
};

const showUserScreen = () => {
    loginContainer.classList.add('hidden');
    registerContainer.classList.add('hidden');
    userInfo.classList.remove('hidden');
};

const submitJson = async (url, form) => {
    try {
        const body = Object.fromEntries(new FormData(form).entries());
        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        const originalText = submitBtn.textContent;
        submitBtn.textContent = 'Loading...';
        
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });

        const data = await response.json();
        show(data);

        if (response.ok) {
            if (url.includes('/login')) {
                currentUser = data;
                sessionStorage.setItem('eqfCurrentUser', JSON.stringify(data));
                window.location.href = '/questions.html';
                return;
            } else if (url.includes('/register')) {
                showMessage('Registration successful! You can now login.', 'success', document.querySelector("#messageBox2"));
                setTimeout(() => {
                    showLoginScreen();
                }, 1500);
            }
            form.reset();
        } else {
            const msgBox = url.includes('/register') ? document.querySelector("#messageBox2") : messageBox;
            showMessage(data.message || 'An error occurred', 'error', msgBox);
        }
    } catch (error) {
        show(error.message);
        const msgBox = form.id === 'registerForm' ? document.querySelector("#messageBox2") : messageBox;
        showMessage(`Error: ${error.message}`, 'error', msgBox);
    } finally {
        const submitBtn = form.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = url.includes('/login') ? 'Login' : 'Create account';
    }
};

registerBtn.addEventListener("click", () => {
    showRegisterScreen();
});

backBtn.addEventListener("click", () => {
    showLoginScreen();
});

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await submitJson("/api/login", event.currentTarget);
});

registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await submitJson("/api/register", event.currentTarget);
});

logoutBtn.addEventListener("click", () => {
    currentUser = null;
    showLoginScreen();
    show("Logged out successfully.");
    showMessage("You have been logged out.", 'success', messageBox);
});

forgotPasswordLink.addEventListener("click", (event) => {
    event.preventDefault();
    showMessage("Password recovery feature coming soon. Please contact support.", 'info', messageBox);
});

// Load initial health check
fetch("/api/dashboard/health")
    .then((response) => response.json())
    .then(show)
    .catch((error) => show(error.message));
