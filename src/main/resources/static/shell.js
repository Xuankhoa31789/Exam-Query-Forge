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

/** Chỉ hiện với ADMIN — xem eqfNavItems(). */
const EQF_ADMIN_NAV = { key: 'admin', href: '/admin', label: 'Quản trị' };

const EQF_ROLE_LABEL = {
    TEACHER: 'Giáo viên',
    DEPARTMENT_HEAD: 'Tổ trưởng bộ môn',
    ADMIN: 'Quản trị viên'
};

/** Danh sách nav theo vai trò của người đang đăng nhập. */
function eqfNavItems() {
    const saved = eqfSavedLogin() || {};
    return saved.role === 'ADMIN' ? EQF_NAV.concat([EQF_ADMIN_NAV]) : EQF_NAV;
}

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

// Icon Tabler (outline) — dùng cho navbar bản Tabler.
const EQF_NAV_ICONS = {
    home: '<path d="M5 12l-2 0l9 -9l9 9l-2 0" /><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7" /><path d="M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6" />',
    questions: '<path d="M5 5a1 1 0 0 1 1 -1h2a1 1 0 0 1 1 1v14a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1l0 -14" /><path d="M9 5a1 1 0 0 1 1 -1h2a1 1 0 0 1 1 1v14a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1l0 -14" /><path d="M5 8h4" /><path d="M9 16h4" /><path d="M13.803 4.56l2.184 -.53c.562 -.135 1.133 .19 1.282 .732l3.695 13.418a1.02 1.02 0 0 1 -.634 1.219l-.133 .041l-2.184 .53c-.562 .135 -1.133 -.19 -1.282 -.732l-3.695 -13.418a1.02 1.02 0 0 1 .634 -1.219l.133 -.041" /><path d="M14 9l4 -1" /><path d="M16 16l3.923 -.98" />',
    exams: '<path d="M12 21h-7a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v7" /><path d="M3 10h18" /><path d="M10 3v18" /><path d="M17.001 19a2 2 0 1 0 4 0a2 2 0 1 0 -4 0" /><path d="M19.001 15.5v1.5" /><path d="M19.001 21v1.5" /><path d="M22.032 17.25l-1.299 .75" /><path d="M17.27 20l-1.3 .75" /><path d="M15.97 17.25l1.3 .75" /><path d="M20.733 20l1.3 .75" />',
    voting: '<path d="M9 11l3 3l8 -8" /><path d="M20 12v6a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2h9" />',
    admin: '<path d="M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37c1 .608 2.296 .07 2.572 -1.065" /><path d="M9 12a3 3 0 1 0 6 0a3 3 0 0 0 -6 0" />'
};

function eqfIcon(paths) {
    return '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24"'
        + ' fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
        + '<path stroke="none" d="M0 0h24v24H0z" fill="none"/>' + paths + '</svg>';
}

/** Chữ cái đầu của họ và tên, dùng cho avatar. */
function eqfInitials(name) {
    const parts = String(name || '').trim().split(/\s+/).filter(Boolean);
    if (!parts.length) return '?';
    const first = parts[0][0];
    const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
    return (first + last).toUpperCase();
}

/**
 * Dựng navbar theo bộ giao diện Tabler.
 * Cần <div id="appShell"></div> và <div id="pageHead"></div> như bản thường.
 */
function eqfRenderTablerShell(options) {
    const config = options || {};
    const saved = eqfSavedLogin() || {};
    const name = saved.fullName || saved.email || 'Người dùng';
    const role = EQF_ROLE_LABEL[saved.role] || saved.role || '';

    const items = eqfNavItems().map((item) => {
        const active = item.key === config.active ? ' active' : '';
        return `<li class="nav-item${active}">
                  <a class="nav-link" href="${item.href}">
                    <span class="nav-link-icon d-md-none d-lg-inline-block">${eqfIcon(EQF_NAV_ICONS[item.key] || '')}</span>
                    <span class="nav-link-title">${eqfEscape(item.label)}</span>
                  </a>
                </li>`;
    }).join('');

    const bar = document.getElementById('appShell');
    if (bar) {
        bar.innerHTML =
            `<header class="navbar navbar-expand-md d-print-none">
               <div class="container-xl">
                 <h1 class="navbar-brand pe-0 pe-md-3">
                   <a href="/home" class="text-decoration-none">Exam Query Forge</a>
                 </h1>
                 <div class="navbar-nav flex-row order-md-last align-items-center gap-2">
                   <span class="avatar avatar-sm bg-primary-lt">${eqfEscape(eqfInitials(name))}</span>
                   <div class="d-none d-md-block lh-sm">
                     <div>${eqfEscape(name)}</div>
                     <div class="mt-1 small text-secondary">${eqfEscape(role)}</div>
                   </div>
                   <button type="button" class="btn btn-sm ms-2" id="eqfLogoutBtn">Đăng xuất</button>
                 </div>
               </div>
             </header>
             <header class="navbar-expand-md">
               <div class="collapse navbar-collapse">
                 <div class="navbar">
                   <div class="container-xl">
                     <ul class="navbar-nav">${items}</ul>
                   </div>
                 </div>
               </div>
             </header>`;

        document.getElementById('eqfLogoutBtn').addEventListener('click', eqfLogout);
    }

    const head = document.getElementById('pageHead');
    if (head && config.title) {
        head.innerHTML =
            `<div class="page-header d-print-none">
               <div class="container-xl">
                 ${config.pretitle ? `<div class="page-pretitle">${eqfEscape(config.pretitle)}</div>` : ''}
                 <h2 class="page-title">${eqfEscape(config.title)}</h2>
                 ${config.subtitle ? `<div class="text-secondary mt-1">${eqfEscape(config.subtitle)}</div>` : ''}
               </div>
             </div>`;
    }
}

/** Dựng thanh điều hướng + tiêu đề trang. */
function eqfRenderShell(options) {
    const config = options || {};
    const saved = eqfSavedLogin() || {};
    const name = saved.fullName || saved.email || 'Người dùng';
    const role = EQF_ROLE_LABEL[saved.role] || saved.role || '';

    const bar = document.getElementById('appShell');
    if (bar) {
        const links = eqfNavItems().map((item) => {
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
