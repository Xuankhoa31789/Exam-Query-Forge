// Shared JWT helpers — dùng chung cho questions.html / exams.html / voting.html.
// Nạp file này TRƯỚC script riêng của từng trang.
const EQF_LOGIN_KEY = 'eqfCurrentUser';

/** Đọc thông tin đăng nhập đã lưu (JSON: { token, userId, fullName, role, ... }). */
function eqfSavedLogin() {
  try {
    return JSON.parse(sessionStorage.getItem(EQF_LOGIN_KEY));
  } catch (error) {
    sessionStorage.removeItem(EQF_LOGIN_KEY);
    return null;
  }
}

/** Lấy userId từ response đăng nhập / login đã lưu. Không còn parse từ token. */
function eqfUserId(data) {
  const id = data && Number(data.userId);
  return Number.isInteger(id) && id > 0 ? id : null;
}

/**
 * fetch() có gắn "Authorization: Bearer <token>" nếu đã đăng nhập.
 * Nếu API trả 401 (token thiếu / sai / hết hạn): xóa login và quay về trang đăng nhập.
 */
async function apiFetch(url, options = {}) {
  const saved = eqfSavedLogin();
  const headers = Object.assign({}, options.headers);
  if (saved && saved.token) {
    headers['Authorization'] = 'Bearer ' + saved.token;
  }
  const response = await fetch(url, Object.assign({}, options, { headers }));
  if (response.status === 401) {
    sessionStorage.removeItem(EQF_LOGIN_KEY);
    window.location.href = '/index.html';
    throw new Error('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại');
  }
  return response;
}
