// Khung vỏ dùng chung (app shell) cho các trang sau đăng nhập.
// Nạp SAU auth.js và TRƯỚC script riêng của từng trang.
//
// Mỗi trang chỉ cần:
//   <div id="appShell"></div>          ← thanh điều hướng trên cùng
//   <div id="pageHead"></div>          ← tiêu đề trang (nằm trong .wrap)
//   eqfRenderShell({ title: '…', subtitle: '…', active: 'questions' });
//
// Sửa nav/thương hiệu ở ĐÂY, không sửa trong từng trang.

const EQF_NAV = [
    { key: 'home',      href: '/home',      label: 'Trang chủ' },
    { key: 'questions', href: '/questions', label: 'Ngân hàng câu hỏi' },
    { key: 'exams',     href: '/exams',     label: 'Kỳ thi & ma trận' },
    { key: 'voting',    href: '/voting',    label: 'Bình chọn & chốt đề' }
];

const EQF_ROLE_LABEL = {
    TEACHER: 'Giáo viên',
    DEPARTMENT_HEAD: 'Tổ trưởng bộ môn',
    ADMIN: 'Quản trị viên'
};

function eqfEscape(value) {
    return String(value == null ? '' : value)
        .replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

/** Xoá phiên đăng nhập và quay về trang đăng nhập. */
function eqfLogout() {
    sessionStorage.removeItem(EQF_LOGIN_KEY);
    window.location.href = '/';
}

/**
 * Bắt buộc phải đăng nhập mới xem được trang.
 * Trả về thông tin đăng nhập, hoặc chuyển hướng về trang đăng nhập và trả null.
 */
function eqfRequireLogin() {
    const saved = eqfSavedLogin();
    if (!saved || !saved.token || !eqfUserId(saved)) {
        sessionStorage.removeItem(EQF_LOGIN_KEY);
        window.location.href = '/';
        return null;
    }
    return saved;
}

/** Dựng thanh điều hướng + tiêu đề trang. */
function eqfRenderShell(options) {
    const config = options || {};
    const saved = eqfSavedLogin() || {};
    const name = saved.fullName || saved.email || 'Người dùng';
    const role = EQF_ROLE_LABEL[saved.role] || saved.role || '';

    const bar = document.getElementById('appShell');
    if (bar) {
        const links = EQF_NAV.map((item) => {
            const current = item.key === config.active ? ' aria-current="page"' : '';
            return `<a href="${item.href}"${current}>${eqfEscape(item.label)}</a>`;
        }).join('');

        bar.className = 'app-bar';
        bar.innerHTML =
            `<div class="app-bar-inner">
               <a class="app-brand" href="/home">
                 <span class="diamond" aria-hidden="true"></span>Exam Query Forge
               </a>
               <nav class="nav-links" aria-label="Điều hướng chính">${links}</nav>
               <div class="user-chip">
                 <span class="user-meta">
                   <b>${eqfEscape(name)}</b>
                   ${role ? `<em>${eqfEscape(role)}</em>` : ''}
                 </span>
                 <button type="button" class="btn-ghost btn-small" id="eqfLogoutBtn">Đăng xuất</button>
               </div>
             </div>`;

        document.getElementById('eqfLogoutBtn').addEventListener('click', eqfLogout);
    }

    const head = document.getElementById('pageHead');
    if (head && config.title) {
        head.className = 'page-head';
        head.innerHTML =
            `<h1>${eqfEscape(config.title)}</h1>` +
            (config.subtitle ? `<p class="page-sub">${eqfEscape(config.subtitle)}</p>` : '');
    }

    document.body.classList.add('has-app-bar');
}
