import { state, setToken } from './state.js';

const BASE_HEADERS = { 'Content-Type': 'application/json' };

function buildUrl(path, params = {}) {
    const url = new URL(path, window.location.origin);
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, String(value));
        }
    });
    return `${url.pathname}${url.search}`;
}

export async function request(path, options = {}) {
    const headers = {
        ...BASE_HEADERS,
        ...(options.headers || {}),
    };

    if (state.auth.token) {
        headers.Authorization = `Bearer ${state.auth.token}`;
    }

    let response;
    try {
        response = await fetch(path, {...options, headers});
    } catch (error) {
        if (error.name === 'TypeError') {
            throw new Error('Проблема с сетью. Проверьте соединение и попробуйте снова.');
        }
        throw error;
    }
    if (response.status === 401) {
        setToken('');
        window.location.hash = '#/login';
        throw new Error('Сессия истекла. Выполните вход повторно.');
    }

    if (response.status === 204) return null;

    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw new Error(payload?.message || payload?.error || `Ошибка ${response.status}`);
    }

    return payload;
}

export const api = {
    students: {
        async list(params = {}) {
            const payload = await request(buildUrl('/api/students', params));
            return {
                items: payload?.content || [],
                meta: {
                    totalElements: payload?.totalElements || 0,
                    totalPages: payload?.totalPages || 1,
                    page: payload?.number || 0,
                    size: payload?.size || params.size || 10,
                    pageable: true,
                },
            };
        },
        create: (body) => request('/api/students', { method: 'POST', body: JSON.stringify(body) }),
        update: (id, body) => request(`/api/students/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        remove: (id) => request(`/api/students/${id}`, { method: 'DELETE' }),
    },
    classes: {
        async list(params = {}) {
            const payload = await request(buildUrl('/api/classes/with-subjects', params));
            return { items: payload || [], meta: { pageable: false } };
        },
        create: (body) => request('/api/classes', { method: 'POST', body: JSON.stringify(body) }),
        update: (id, body) => request(`/api/classes/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        remove: (id) => request(`/api/classes/${id}`, { method: 'DELETE' }),
        attachSubject: (classId, subjectId) => request(`/api/classes/${classId}/subjects/${subjectId}`, { method: 'PUT' }),
    },
    subjects: {
        async list(params = {}) {
            const payload = await request(buildUrl('/api/subjects', params));
            return { items: payload || [], meta: { pageable: false } };
        },
        create: (body) => request('/api/subjects', { method: 'POST', body: JSON.stringify(body) }),
        update: (id, body) => request(`/api/subjects/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        remove: (id) => request(`/api/subjects/${id}`, { method: 'DELETE' }),
    },
    teachers: {
        async list(params = {}) {
            const payload = await request(buildUrl('/api/teachers', params));
            return { items: payload || [], meta: { pageable: false } };
        },
        create: (body) => request('/api/teachers', { method: 'POST', body: JSON.stringify(body) }),
        update: (id, body) => request(`/api/teachers/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        remove: (id) => request(`/api/teachers/${id}`, { method: 'DELETE' }),
    },
    grades: {
        async list(params = {}) {
            const payload = await request(buildUrl('/api/grades', params));
            return { items: payload || [], meta: { pageable: false } };
        },
        create: (body) => request('/api/grades', { method: 'POST', body: JSON.stringify(body) }),
        update: (id, body) => request(`/api/grades/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        remove: (id) => request(`/api/grades/${id}`, { method: 'DELETE' }),
    },
};