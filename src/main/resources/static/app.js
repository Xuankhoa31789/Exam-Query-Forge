// Trang đăng nhập / đăng ký (index.html).
// Sau khi đăng nhập thành công: lưu { token, userId, fullName, role } vào
// sessionStorage['eqfCurrentUser'] rồi chuyển sang /questions.html.
// auth.js ở các trang trong đọc đúng key này.

const EQF_LOGIN_KEY = 'eqfCurrentUser';

const loginContainer = document.querySelector('#loginContainer');
const registerContainer = document.querySelector('#registerContainer');
const loginForm = document.querySelector('#loginForm');
const registerForm = document.querySelector('#registerForm');
const messageBox = document.querySelector('#messageBox');
const messageBox2 = document.querySelector('#messageBox2');
const registerBtn = document.querySelector('#registerBtn');
const backBtn = document.querySelector('#backBtn');
const forgotPasswordLink = document.querySelector('#forgotPasswordLink');
const healthDot = document.querySelector('#healthDot');
const healthText = document.querySelector('#healthText');

let messageTimer = null;

/* ---------- Thông báo ---------- */

function showMessage(text, type = 'ok', box = messageBox, autoHide = true) {
    box.textContent = text;
    box.className = `msg ${type} show`;
    if (box === messageBox) {
        clearTimeout(messageTimer);
        if (autoHide) {
            messageTimer = setTimeout(() => box.classList.remove('show'), 6000);
        }
    }
}

function hideMessage(box) {
    box.classList.remove('show');
}

/* ---------- Chuyển giữa hai khung ---------- */

function showLoginScreen() {
    loginContainer.classList.remove('hidden');
    registerContainer.classList.add('hidden');
    hideMessage(messageBox2);
    loginForm.reset();
}

function showRegisterScreen() {
    registerContainer.classList.remove('hidden');
    loginContainer.classList.add('hidden');
    hideMessage(messageBox);
    registerForm.reset();
}

/* ---------- Hiện / ẩn mật khẩu ---------- */

document.querySelectorAll('.pw-toggle').forEach((toggle) => {
    toggle.addEventListener('click', () => {
        const input = document.getElementById(toggle.dataset.target);
        const revealed = input.type === 'text';
        input.type = revealed ? 'password' : 'text';
        toggle.textContent = revealed ? '👁' : '🙈';
        toggle.setAttribute('aria-label', revealed ? 'Hiện mật khẩu' : 'Ẩn mật khẩu');
        input.focus();
    });
});

/* ---------- Gửi biểu mẫu ---------- */

async function submitJson(url, form, box) {
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    const body = Object.fromEntries(new FormData(form).entries());

    submitBtn.disabled = true;
    submitBtn.textContent = 'Đang xử lý…';
    hideMessage(box);

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        let data = {};
        try {
            data = await response.json();
        } catch (error) {
            /* phản hồi không phải JSON */
        }

        if (!response.ok) {
            showMessage(data.message || 'Có lỗi xảy ra, vui lòng thử lại.', 'err', box);
            return;
        }

        if (url.includes('/login')) {
            sessionStorage.setItem(EQF_LOGIN_KEY, JSON.stringify(data));
            window.location.href = '/home';
            return;
        }

        form.reset();
        showMessage('Đăng ký thành công! Bạn có thể đăng nhập ngay.', 'ok', box);
        setTimeout(showLoginScreen, 1500);
    } catch (error) {
        showMessage(`Không kết nối được máy chủ: ${error.message}`, 'err', box);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

loginForm.addEventListener('submit', (event) => {
    event.preventDefault();
    submitJson('/api/login', loginForm, messageBox);
});

registerForm.addEventListener('submit', (event) => {
    event.preventDefault();
    submitJson('/api/register', registerForm, messageBox2);
});

registerBtn.addEventListener('click', showRegisterScreen);
backBtn.addEventListener('click', showLoginScreen);

forgotPasswordLink.addEventListener('click', () => {
    showMessage('Tính năng khôi phục mật khẩu chưa có. Vui lòng liên hệ quản trị viên.', 'info', messageBox);
});

/* ---------- Phiên đăng nhập còn hiệu lực ---------- */

(function resumeSession() {
    let saved = null;
    try {
        saved = JSON.parse(sessionStorage.getItem(EQF_LOGIN_KEY));
    } catch (error) {
        sessionStorage.removeItem(EQF_LOGIN_KEY);
    }
    if (!saved || !saved.token) return;

    messageBox.textContent = `Bạn vẫn đang đăng nhập là ${saved.fullName || saved.email}. `;
    const link = document.createElement('a');
    link.href = '/home';
    link.textContent = 'Vào trang chủ →';
    messageBox.appendChild(link);
    messageBox.className = 'msg info show';
})();

/* ---------- Trạng thái máy chủ ---------- */

fetch('/api/dashboard/health')
    .then((response) => {
        if (!response.ok) throw new Error(String(response.status));
        return response.json();
    })
    .then(() => {
        healthDot.className = 'status-dot up';
        healthText.textContent = 'Hệ thống đang hoạt động';
    })
    .catch(() => {
        healthDot.className = 'status-dot down';
        healthText.textContent = 'Không kết nối được máy chủ';
    });
