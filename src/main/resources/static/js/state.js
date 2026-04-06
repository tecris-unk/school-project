const listeners = new Set();

export const state = {
  auth: {
    token: localStorage.getItem('jwt_token') || '',
    role: localStorage.getItem('user_role') || 'admin',
  },
  route: 'login',
  loading: false,
  activeEntity: 'students',
  refs: {
    classes: [],
    teachers: [],
    subjects: [],
  },
  data: {
    students: [],
    classes: [],
    subjects: [],
    teachers: [],
    grades: [],
  },
  meta: {
    students: { totalElements: 0, totalPages: 1, page: 0, size: 10, pageable: true },
    classes: { totalElements: 0, totalPages: 1, page: 0, size: 10, pageable: false },
    subjects: { totalElements: 0, totalPages: 1, page: 0, size: 10, pageable: false },
    teachers: { totalElements: 0, totalPages: 1, page: 0, size: 10, pageable: false },
    grades: { totalElements: 0, totalPages: 1, page: 0, size: 10, pageable: false },
  },
  ui: {
    search: '',
    filters: {},
    sort: { key: 'id', dir: 'desc' },
    selectedIds: new Set(),
    editingId: null,
    toastQueue: [],
  },
};

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function setState(mutator) {
  mutator(state);
  listeners.forEach((listener) => listener(state));
}

export function resetUiState() {
  setState((s) => {
    s.ui.search = '';
    s.ui.filters = {};
    s.ui.sort = { key: 'id', dir: 'desc' };
    s.ui.selectedIds = new Set();
    s.ui.editingId = null;
  });
}

export function setToken(token, role = 'admin') {
  if (token) {
    localStorage.setItem('jwt_token', token);
    localStorage.setItem('user_role', role);
  } else {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_role');
  }

  setState((s) => {
    s.auth.token = token;
    s.auth.role = role;
  });
}