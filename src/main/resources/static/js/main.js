import {api} from './api.js';
import {resetUiState, setState, setToken, state, subscribe} from './state.js';
import {debounce} from './utils/debounce.js';
import {average, byId, classLabel, fullName} from './utils/helpers.js';
import {formDataToObject, renderDrawer, renderEntityForm, validateForm} from './ui/form.js';
import {navItems, renderNav} from './ui/nav.js';
import {confirmModal, notify} from './ui/notifications.js';
import {applyQuery, paginate, renderTable} from './ui/table.js';

const app = document.getElementById('app');
const SAVE_SUCCESS_MESSAGE = 'Сохранение выполнено';
const DELETE_SUCCESS_MESSAGE = 'Удаление выполнено';

const entityConfigs = {
    students: {
        title: 'Ученики',
        endpoint: 'students',
        columns: [
            {key: 'firstName', label: 'Имя'},
            {key: 'lastName', label: 'Фамилия'},
            {key: 'gender', label: 'Пол'},
            {key: 'email', label: 'Эл. почта'},
            {key: 'schoolClassId', label: 'Класс'}],
        filters: [
            { key: 'schoolClassId', label: 'Класс', options: (refs) => refs.classes.map((item) => ({value: item.id, label: classLabel(item)})) }],
        fields: [
            {key: 'firstName', label: 'Имя', required: true},
            {key: 'lastName', label: 'Фамилия', required: true},
            { key: 'gender', label: 'Пол', type: 'select', required: true, defaultValue: 'MALE', options:
                    [
                        {value: 'MALE', label: 'Мужской'},
                        {value: 'FEMALE', label: 'Женский'}
                    ]},
            {key: 'email', label: 'Эл. почта', type: 'email', required: true},
            { key: 'schoolClassId', label: 'Класс', type: 'ref', ref: 'classes', allowEmpty: true }
        ],
        payload: (v) => ({...v, schoolClassId: v.schoolClassId ? Number(v.schoolClassId) : null}),
        linkAction: {label: 'Привязать класс'}
    },
    classes: {
        title: 'Классы', endpoint: 'classes',
        columns: [{key: 'grade', label: 'Параллель'}, {key: 'letter', label: 'Буква'}], filters: [],
        fields: [{key: 'grade', label: 'Параллель', type: 'number', required: true}, {key: 'letter', label: 'Буква', required: true}],
        payload: (v) => ({grade: Number(v.grade), letter: v.letter}),
        linkAction: {label: 'Привязать предмет'}
    },
    subjects: {
        title: 'Предметы', endpoint: 'subjects',
        columns: [{key: 'name', label: 'Название'}, {key: 'description', label: 'Описание'}, {key: 'teacherId', label: 'Учитель'}],
        filters: [{ key: 'teacherId', label: 'Учитель', options: (refs) => refs.teachers.map((teacher) => ({value: teacher.id, label: fullName(teacher)})) }],
        fields: [{key: 'name', label: 'Название', required: true}, {key: 'description', label: 'Описание', type: 'textarea'}, {key: 'teacherId', label: 'Учитель', type: 'ref', ref: 'teachers', allowEmpty: true}],
        payload: (v) => ({...v, teacherId: v.teacherId ? Number(v.teacherId) : null}),
        linkAction: {label: 'Привязать учителя'}
    },
    teachers: {
        title: 'Учителя', endpoint: 'teachers',
        columns: [{key: 'firstName', label: 'Имя'}, {key: 'lastName', label: 'Фамилия'}, {key: 'email', label: 'Эл. почта'}], filters: [],
        fields: [{key: 'firstName', label: 'Имя', required: true}, {key: 'lastName', label: 'Фамилия', required: true}, {key: 'email', label: 'Эл. почта', type: 'email', required: true}],
        payload: (v) => v,
        linkAction: {label: 'Привязать предмет'}
    },
    grades: {
        title: 'Оценки', endpoint: 'grades',
        columns: [{key: 'score', label: 'Оценка'}, {key: 'date', label: 'Дата'}, {key: 'studentId', label: 'Ученик'}, {key: 'subjectId', label: 'Предмет'}],
        filters: [{ key: 'subjectId', label: 'Предмет', options: (refs) => refs.subjects.map((subject) => ({value: subject.id, label: subject.name})) }, { key: 'studentId', label: 'Ученик', options: (_, data) => data.students.map((student) => ({value: student.id, label: fullName(student)})) }],
        fields: [{key: 'score', label: 'Оценка', type: 'number', required: true}, {key: 'date', label: 'Дата', type: 'date', required: true}, {key: 'studentId', label: 'Ученик', type: 'ref', ref: 'students', required: true}, {key: 'subjectId', label: 'Предмет', type: 'ref', ref: 'subjects', required: true}],
        payload: (v) => ({ ...v, score: Number(v.score), studentId: Number(v.studentId), subjectId: Number(v.subjectId) })
    }
};


function parseRoute() {
    const hash = window.location.hash.replace('#/', '') || 'dashboard';
    return navItems.find((item) => item.key === hash)?.key || (hash === 'login' ? 'login' : 'dashboard');
}

const isTeacherRole = () => state.auth.role === 'teacher';
const canEditEntity = (entity) => state.auth.role === 'admin' || (isTeacherRole() && entity === 'grades');
const canDeleteEntity = () => state.auth.role === 'admin';

function allowedRoutesForRole() {
    if (isTeacherRole()) return new Set(['dashboard', 'classes', 'grades']);
    return new Set(navItems.map((item) => item.key));
}

const getTeacherSubjectIds = () => new Set(state.refs.subjects.map((subject) => subject.id));

function openEntityDrawer(editingId = null) {
    setState((s) => {
        s.ui.editingId = editingId;
        s.ui.drawerOpen = true;
        s.ui.formErrors = {};
        s.ui.relationContext = null;
    });
}

function openRelationDrawer(rowId) {
    setState((s) => {
        s.ui.drawerOpen = true;
        s.ui.editingId = null;
        s.ui.formErrors = {};
        s.ui.relationContext = {entity: s.route, rowId};
    });
}

function closeDrawer() {
    setState((s) => {
        s.ui.drawerOpen = false;
        s.ui.editingId = null;
        s.ui.formErrors = {};
        s.ui.relationContext = null;
    });
}

function relationConfig(entity) {
    if (entity === 'students') return {label: 'Класс', name: 'classId', options: state.refs.classes.map((item) => ({value: item.id, label: classLabel(item)}))};
    if (entity === 'subjects') return {label: 'Учитель', name: 'teacherId', options: state.refs.teachers.map((item) => ({value: item.id, label: fullName(item)}))};
    if (entity === 'classes') return {label: 'Предмет', name: 'subjectId', options: state.refs.subjects.map((item) => ({value: item.id, label: item.name}))};
    if (entity === 'teachers') return {label: 'Предмет', name: 'subjectId', options: state.refs.subjects.map((item) => ({value: item.id, label: item.name}))};
    return null;
}

function relationDrawerTemplate() {
    if (!state.ui.relationContext) return '';
    const cfg = relationConfig(state.ui.relationContext.entity);
    if (!cfg) return '';

    return `
      <form id="relation-form" class="space-y-4">
        <label class="block text-sm font-medium text-slate-700">
          ${cfg.label}
          <select name="${cfg.name}" class="input-base w-full mt-1" required>
            <option value="">Выберите...</option>
            ${cfg.options.map((opt) => `<option value="${opt.value}">${opt.label}</option>`).join('')}
          </select>
        </label>
        <button class="btn-primary" type="submit">Сохранить связь</button>
      </form>`;
}

async function loadRefs(force = false) {
    if (!force && state.refs.classes.length && state.refs.teachers.length && state.refs.subjects.length) return;
    if (!isTeacherRole()) {
        const [classes, teachers, subjects] = await Promise.all([api.classes.list(), api.teachers.list(), api.subjects.list()]);
        setState((s) => {
            s.refs.classes = classes.items;
            s.refs.teachers = teachers.items;
            s.refs.subjects = subjects.items;
            s.data.classes = classes.items;
            s.data.teachers = teachers.items;
            s.data.subjects = subjects.items;
        });
        return;
    }

    const teachers = await api.teachers.list({email: state.auth.email});
    const currentTeacher = teachers.items.find((teacher) => teacher.email === state.auth.email);
    if (!currentTeacher) throw new Error('Учитель с таким email не найден. Попросите администратора добавить вас в систему.');

    const subjects = await api.subjects.list({teacherId: currentTeacher.id});
    const teacherSubjectIds = new Set(subjects.items.map((subject) => subject.id));
    const classes = await api.classes.list();
    const availableClasses = classes.items.filter((schoolClass) => schoolClass.subjectIds?.some((subjectId) => teacherSubjectIds.has(subjectId)));

    setState((s) => {
        s.refs.classes = availableClasses;
        s.refs.teachers = [currentTeacher];
        s.refs.subjects = subjects.items;
        s.data.classes = availableClasses;
        s.data.teachers = [currentTeacher];
        s.data.subjects = subjects.items;
    });
}

async function loadEntity(entity, force = false) {
    const meta = state.meta[entity];
    if (!force && state.data[entity].length > 0 && !meta.pageable) return;

    try {
        const params = meta.pageable ? {page: meta.page, size: meta.size} : {};
        const teacherParams = isTeacherRole() && entity === 'students' ? {teacherEmail: state.auth.email} : {};
        const result = await api[entity].list({...params, ...teacherParams});
        const teacherSubjectIds = getTeacherSubjectIds();
        let filteredItems = result.items;
        if (isTeacherRole() && entity === 'grades') filteredItems = result.items.filter((grade) => teacherSubjectIds.has(grade.subjectId));
        if (isTeacherRole() && entity === 'classes') filteredItems = result.items.filter((schoolClass) => schoolClass.subjectIds?.some((subjectId) => teacherSubjectIds.has(subjectId)));

        setState((s) => {
            s.data[entity] = filteredItems;
            s.meta[entity] = { ...s.meta[entity], ...result.meta, totalElements: result.meta?.totalElements ?? filteredItems.length, totalPages: result.meta?.totalPages ?? Math.max(1, Math.ceil(filteredItems.length / s.meta[entity].size)) };
        });
    } catch (error) {
        notify(error.message, 'error');
    }
}

function getVisibleRows(entity) {
    const config = entityConfigs[entity];
    const meta = state.meta[entity];
    const queried = applyQuery(state.data[entity], config, state.ui);
    if (meta.pageable) return {rows: queried, meta: {...meta, totalElements: meta.totalElements}};
    const paged = paginate(queried, meta.page, meta.size);
    return { rows: paged, allFiltered: queried, meta: { ...meta, totalElements: queried.length, totalPages: Math.max(1, Math.ceil(queried.length / meta.size)) } };
}

function dashboardTemplate() {
    const grades = state.data.grades.map((grade) => grade.score).filter((score) => Number.isFinite(score));
    const avg = average(grades);
    if (isTeacherRole()) {
        return `<section class="grid md:grid-cols-3 gap-4 mb-6"><article class="card-base p-5"><p class="text-sm text-slate-500">Мои классы</p><h2 class="text-3xl font-semibold mt-2">${state.data.classes.length}</h2></article><article class="card-base p-5"><p class="text-sm text-slate-500">Мои предметы</p><h2 class="text-3xl font-semibold mt-2">${state.data.subjects.length}</h2></article><article class="card-base p-5 bg-indigo-600 text-white"><p class="text-sm text-white/80">Средний балл</p><h2 class="text-3xl font-semibold mt-2">${avg.toFixed(2)}</h2></article></section><div class="card-base p-5 text-slate-600">Вам доступны только ваши классы и журнал оценок по вашим предметам.</div>`;
    }

    return `<section class="grid md:grid-cols-3 gap-4 mb-6"><article class="card-base p-5"><p class="text-sm text-slate-500">Ученики</p><h2 class="text-3xl font-semibold mt-2">${state.data.students.length}</h2></article><article class="card-base p-5 bg-indigo-600 text-white"><p class="text-sm text-white/80">Средний балл</p><h2 class="text-3xl font-semibold mt-2">${avg.toFixed(2)}</h2></article><article class="card-base p-5"><p class="text-sm text-slate-500">Учителя</p><h2 class="text-3xl font-semibold mt-2">${state.data.teachers.length}</h2></article></section>`;
}

function loginTemplate() {
    return `<div class="min-h-screen flex items-center justify-center p-4"><form id="login-form" class="w-full max-w-md card-base p-7"><h1 class="text-3xl font-bold mb-1">Панель школы</h1><p class="text-sm text-slate-500 mb-5">Вход в административную панель</p><label class="block text-sm mb-3">Эл. почта<input class="mt-1 w-full input-base" type="email" name="email" required /></label><label class="block text-sm mb-3">Пароль<input class="mt-1 w-full input-base" type="password" name="password" required /></label><button class="w-full btn-primary">Войти</button></form></div>`;
}

function shellTemplate(content) {
    return `
    <div class="min-h-screen">
      <header class="sticky top-0 z-30 bg-white border-b border-slate-200">
        <div class="px-4 py-4 md:px-6">
          <div class="flex items-start justify-between gap-4">
            <div>
              <h1 class="text-xl font-semibold flex items-center gap-2">
               <img src="/favicon.ico" alt="Логотип школы" loading="eager" decoding="async" class="h-[72px] w-[72px] object-contain border border-slate-200 bg-white p-1" />
                <span>Школа №12 г.Витебска</span>
              </h1>
              <p class="text-sm text-slate-500">Администрирование и управление учебными данными.</p>
            </div>
            <a href="#/login" data-logout class="text-sm text-slate-500 hover:text-slate-900">Выйти</a>
          </div>
        </div>
      </header>
      <main class="md:grid md:grid-cols-[auto_1fr] md:min-h-[calc(100vh-113px)]">
        <aside class="group border-b border-slate-200 md:border-b-0 md:border-r border-slate-200 bg-white p-3 md:w-[88px] md:hover:w-[260px] transition-[width] duration-200 overflow-hidden">
          <div id="shell-nav"></div>
        </aside>
        <section class="p-4 md:p-6">
          <div id="shell-content">${content}</div>
        </section>
      </main>
    </div>`;
}

function ensureShell(content = '') {
    if (!document.getElementById('shell-content')) {
        app.innerHTML = shellTemplate(content);
        return;
    }

    const navRoot = document.getElementById('shell-nav');
    const contentRoot = document.getElementById('shell-content');
    if (navRoot) navRoot.innerHTML = renderNav(state.route, state.auth.role);
    if (contentRoot) contentRoot.innerHTML = content;
}


function renderClassSubjectMatrix() {
    const classes = state.data.classes;
    const subjectsById = new Map(state.refs.subjects.map((subject) => [subject.id, subject.name]));

    return `<section class="mt-4 card-base"><div class="p-4 border-b border-slate-100"><h3 class="text-base font-semibold text-slate-900">Соответствие Класс ↔ Предмет</h3><p class="text-sm text-slate-500 mt-1">Таблица показывает, какие предметы закреплены за каждым классом.</p></div><table class="w-full"><thead class="bg-slate-50"><tr><th class="px-3 py-3 text-left text-xs uppercase tracking-wider text-slate-500">Класс</th><th class="px-3 py-3 text-left text-xs uppercase tracking-wider text-slate-500">Предметы</th></tr></thead><tbody>${classes.map((schoolClass) => { const subjectNames = (schoolClass.subjectIds || []).map((subjectId) => subjectsById.get(subjectId)).filter(Boolean); return `<tr class="border-t border-slate-100"><td class="px-3 py-3 text-sm text-slate-700 font-medium">${classLabel(schoolClass)}</td><td class="px-3 py-3 text-sm text-slate-700">${subjectNames.length ? subjectNames.join(', ') : '—'}</td></tr>`; }).join('')}</tbody></table></section>`;
}

function render() {

    if (state.route === 'login' || !state.auth.token) {
        app.innerHTML = loginTemplate();
        bindLogin();
        return;
    }

    if (state.route === 'dashboard') {
        ensureShell(dashboardTemplate());
        const navRoot = document.getElementById('shell-nav');
        if (navRoot) navRoot.innerHTML = renderNav(state.route, state.auth.role);
        bindGlobalHandlers();
        return;
    }

    const config = entityConfigs[state.route];
    const {rows, allFiltered = rows, meta} = getVisibleRows(state.route);
    const editingRow = byId(state.data[state.route], state.ui.editingId);
    const canEdit = canEditEntity(state.route);
    const canDelete = canDeleteEntity(state.route);

    const drawerBody = state.ui.relationContext
        ? relationDrawerTemplate()
        : renderEntityForm(config, state.refs, state.data, editingRow, state.route, state.ui.formErrors);

    const drawerTitle = state.ui.relationContext
        ? `Связь для ${config.title.toLowerCase()}`
        : `${state.ui.editingId ? 'Редактирование' : 'Создание'}: ${config.title}`;

    const classSubjectRelationsSection = state.route === 'classes' ? `<section class="mt-4"><button data-toggle-class-subject-matrix class="btn-secondary">${state.ui.classSubjectMatrixVisible ? 'Скрыть связь класс-предмет' : 'Показать связь класс-предмет'}</button>${state.ui.classSubjectMatrixVisible ? renderClassSubjectMatrix() : ''}</section>` : '';

    const content = `
    <section class="space-y-4">
        <div class="flex items-center justify-between">
          <h2 class="text-2xl font-semibold text-slate-900">${config.title}</h2>
          ${canEdit ? '<button data-create-entity class="btn-primary">Создать запись</button>' : ''}
        </div>
        ${renderTable({ entity: state.route, config, rows, refs: state.refs, data: state.data, ui: state.ui, meta, pageableFromApi: state.meta[state.route].pageable, canEdit, canDelete, loading: false })}
      </section>
      ${classSubjectRelationsSection}
      ${renderDrawer({title: drawerTitle, subtitle: 'Заполните форму справа', body: drawerBody, open: state.ui.drawerOpen})}
    `;

    ensureShell(content);
    const navRoot = document.getElementById('shell-nav');
    if (navRoot) navRoot.innerHTML = renderNav(state.route, state.auth.role);
    bindEntityHandlers(config, allFiltered, rows, meta);
    bindGlobalHandlers();
}

function bindLogin() {
    const form = document.getElementById('login-form');
    form?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const values = Object.fromEntries(new FormData(form).entries());
        const isAdmin = values.email === 'admin@gov.by' && values.password === '1111';
        const role = isAdmin ? 'admin' : 'teacher';

        if (!isAdmin) {
            const teachers = await api.teachers.list({email: values.email});
            const matchedTeacher = teachers.items.find((teacher) => teacher.email === values.email);
            if (!matchedTeacher) return notify('Неверные данные входа', 'error');
        }

        setToken(`fake-jwt-${Date.now()}`, role, values.email);
        window.location.hash = '#/dashboard';
        notify(`Добро пожаловать, ${values.email}`, 'success');
    });
}

function bindGlobalHandlers() {
    document.querySelector('[data-logout]')?.addEventListener('click', () => {
        setToken('');
        notify('Вы вышли из системы', 'info');
    });
}

const debouncedSearch = debounce((value) => {
    setState((s) => {
        s.ui.search = value;
        s.meta[s.route].page = 0;
    });
}, 300);

async function submitRelationForm(values) {
    const ctx = state.ui.relationContext;
    if (!ctx) return;

    if (ctx.entity === 'students') {
        const student = byId(state.data.students, ctx.rowId);
        await api.students.update(ctx.rowId, { firstName: student.firstName, lastName: student.lastName, gender: student.gender, email: student.email, schoolClassId: Number(values.classId) });
        notify('Ученик привязан к классу', 'success');
    }
    if (ctx.entity === 'subjects') {
        const subject = byId(state.data.subjects, ctx.rowId);
        await api.subjects.update(ctx.rowId, {name: subject.name, description: subject.description, teacherId: Number(values.teacherId)});
        notify('Предмет привязан к учителю', 'success');
    }
    if (ctx.entity === 'classes') {
        await api.classes.attachSubject(ctx.rowId, Number(values.subjectId));
        notify('Предмет привязан к классу', 'success');
    }
    if (ctx.entity === 'teachers') {
        const subjectId = Number(values.subjectId);
        const subject = byId(state.data.subjects, subjectId) || byId(state.refs.subjects, subjectId);
        if (!subject) throw new Error('Предмет не найден');
        await api.subjects.update(subjectId, {name: subject.name, description: subject.description, teacherId: ctx.rowId});
        notify('Предмет привязан к учителю', 'success');
    }

    await loadRefs(true);
    await loadEntity(state.route, true);
    closeDrawer();
}

function bindEntityHandlers(config, allFiltered, displayedRows, meta) {
    document.querySelectorAll('[data-drawer-close]').forEach((el) => el.addEventListener('click', closeDrawer));
    document.querySelector('[data-create-entity]')?.addEventListener('click', () => openEntityDrawer(null));

    if (state.route === 'classes') {
        document.querySelector('[data-toggle-class-subject-matrix]')?.addEventListener('click', () => {
            setState((s) => { s.ui.classSubjectMatrixVisible = !s.ui.classSubjectMatrixVisible; });
        });
    }

    if (canEditEntity(state.route)) {
        document.querySelectorAll('[data-link-id]').forEach((element) => {
            element.addEventListener('click', (e) => openRelationDrawer(Number(e.currentTarget.dataset.linkId)));
        });

        document.getElementById('relation-form')?.addEventListener('submit', async (e) => {
            e.preventDefault();
            try {
                await submitRelationForm(formDataToObject(e.target));
            } catch (error) {
                notify(error.message, 'error');
            }
        });
        document.getElementById('entity-form')?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const values = formDataToObject(e.target);
            const errors = validateForm(state.route, values);
            if (Object.keys(errors).length) {
                setState((s) => { s.ui.formErrors = errors; });
                return;
            }

            const payload = config.payload(values);
            const previousData = [...state.data[state.route]];

            try {
                if (state.ui.editingId) {
                    const id = state.ui.editingId;
                    const saved = await api[state.route].update(id, payload);
                    setState((s) => {
                        const idx = s.data[s.route].findIndex((item) => item.id === id);
                        if (idx > -1) s.data[s.route][idx] = saved;
                    });
                    notify(SAVE_SUCCESS_MESSAGE, 'success');
                } else {
                    const created = await api[state.route].create(payload);
                    setState((s) => {
                        s.data[s.route].unshift(created);
                        s.meta[s.route].totalElements += 1;
                    });
                    notify(SAVE_SUCCESS_MESSAGE, 'success');
                }
                closeDrawer();
            } catch (error) {
                setState((s) => { s.data[s.route] = previousData; });
                notify(error.message, 'error');
            }
        });

        document.querySelector('[data-cancel-edit]')?.addEventListener('click', closeDrawer);
    }

    document.getElementById('search-input')?.addEventListener('input', (e) => debouncedSearch(e.target.value));

    document.querySelectorAll('[data-filter-key]').forEach((element) => {
        element.addEventListener('change', (e) => {
            const key = e.target.dataset.filterKey;
            const value = e.target.value || null;
            setState((s) => {
                s.ui.filters[key] = value;
                s.meta[s.route].page = 0;
            });
        });
    });

    document.querySelectorAll('[data-sort]').forEach((element) => {
        element.addEventListener('click', (e) => {
            const key = e.target.dataset.sort;
            setState((s) => {
                const same = s.ui.sort.key === key;
                s.ui.sort = {key, dir: same && s.ui.sort.dir === 'asc' ? 'desc' : 'asc'};
            });
        });
    });

    if (canEditEntity(state.route)) {
        document.querySelectorAll('[data-edit-id]').forEach((element) => {
            element.addEventListener('click', (e) => openEntityDrawer(Number(e.currentTarget.dataset.editId)));
        });
    }

    if (canDeleteEntity(state.route)) {
        document.querySelectorAll('[data-delete-id]').forEach((element) => {
            element.addEventListener('click', (e) => {
                const id = Number(e.currentTarget.dataset.deleteId);
                confirmModal({ title: 'Подтвердите удаление', description: 'Эту операцию нельзя отменить.', onConfirm: async () => {
                        const previous = [...state.data[state.route]];
                        setState((s) => {
                            s.data[s.route] = s.data[s.route].filter((item) => item.id !== id);
                            s.ui.selectedIds.delete(id);
                        });
                        try {
                            await api[state.route].remove(id);
                            notify(DELETE_SUCCESS_MESSAGE, 'success');
                        } catch (error) {
                            setState((s) => { s.data[s.route] = previous; });
                            notify(error.message, 'error');
                        }
                    }});
            });
        });

        document.querySelectorAll('[data-select-id]').forEach((element) => {
            element.addEventListener('change', (e) => {
                const id = Number(e.currentTarget.dataset.selectId);
                setState((s) => {
                    if (e.currentTarget.checked) s.ui.selectedIds.add(id); else s.ui.selectedIds.delete(id);
                });
            });
        });

        document.getElementById('select-all')?.addEventListener('change', (e) => {
            setState((s) => {
                if (e.target.checked) displayedRows.forEach((row) => s.ui.selectedIds.add(row.id)); else displayedRows.forEach((row) => s.ui.selectedIds.delete(row.id));
            });
        });

        document.getElementById('bulk-delete')?.addEventListener('click', () => {
            if (!state.ui.selectedIds.size) return notify('Сначала отметьте записи', 'info');

            confirmModal({
                title: 'Массовое удаление', description: `Будут удалены записи: ${state.ui.selectedIds.size}`,
                onConfirm: async () => {
                    const ids = [...state.ui.selectedIds];
                    const previous = [...state.data[state.route]];
                    setState((s) => {
                        s.data[s.route] = s.data[s.route].filter((item) => !s.ui.selectedIds.has(item.id));
                        s.ui.selectedIds = new Set();
                    });
                    const results = await Promise.allSettled(ids.map((id) => api[state.route].remove(id)));
                    const failed = results.filter((r) => r.status === 'rejected').length;
                    if (failed) {
                        setState((s) => { s.data[s.route] = previous; });
                        notify(`Удаление прервано, ошибок: ${failed}`, 'error');
                    } else {
                        notify('Выбранные записи удалены', 'success');
                    }
                },
            });
        });
    }

    document.querySelectorAll('[data-page-action]').forEach((element) => {
        element.addEventListener('click', async (e) => {
            const action = e.currentTarget.dataset.pageAction;
            const canNext = meta.page + 1 < meta.totalPages;
            setState((s) => {
                if (action === 'prev' && s.meta[s.route].page > 0) s.meta[s.route].page -= 1;
                if (action === 'next' && canNext) s.meta[s.route].page += 1;
            });

            if (state.meta[state.route].pageable) await loadEntity(state.route, true);
        });
    });
}

async function routeChanged() {
    const route = parseRoute();
    if (route !== 'login' && !state.auth.token) {
        window.location.hash = '#/login';
        return;
    }

    setState((s) => {
        s.route = route;
        if (entityConfigs[route]) s.activeEntity = route;
    });

    const allowedRoutes = allowedRoutesForRole();
    if (route !== 'login' && !allowedRoutes.has(route)) {
        window.location.hash = '#/dashboard';
        return;
    }

    if (route !== 'login') {
        try {
            await loadRefs();
            if (route === 'dashboard') {
                if (isTeacherRole()) await loadEntity('grades');
                else await Promise.all([loadEntity('students'), loadEntity('grades'), loadEntity('teachers')]);
            } else if (entityConfigs[route]) {
                if (isTeacherRole() && route === 'grades') await loadEntity('students');
                await loadEntity(route);
            }
        } catch (error) {
            notify(error.message, 'error');
        }
    }

    resetUiState();
}

subscribe(render);
window.addEventListener('hashchange', () => routeChanged());

if (!window.location.hash) window.location.hash = state.auth.token ? '#/dashboard' : '#/login';
else routeChanged();

render();