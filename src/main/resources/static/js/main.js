import {api} from './api.js';
import {resetUiState, setState, setToken, state, subscribe} from './state.js';
import {debounce} from './utils/debounce.js';
import {average, byId, classLabel, formatDate, fullName, initials} from './utils/helpers.js';
import {formDataToObject, renderDrawer, renderEntityForm, validateForm} from './ui/form.js';
import {navItems, renderNav} from './ui/nav.js';
import {confirmModal, notify} from './ui/notifications.js';
import {applyQuery, paginate, renderTable} from './ui/table.js';

const app = document.getElementById('app');
const SAVE_SUCCESS_MESSAGE = 'Сохранение выполнено';
const DELETE_SUCCESS_MESSAGE = 'Удаление выполнено';
const CREATE_ACTION_LABEL = 'Создать запись';

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

function topSubjectsByAverage(limit = 5) {
    const gradesBySubject = new Map();
    state.data.grades.forEach((grade) => {
        const list = gradesBySubject.get(grade.subjectId) || [];
        list.push(grade.score);
        gradesBySubject.set(grade.subjectId, list);
    });

    return [...gradesBySubject.entries()]
        .map(([subjectId, scores]) => ({
            subjectId,
            name: byId(state.refs.subjects, subjectId)?.name || 'Предмет',
            averageScore: average(scores),
            count: scores.length,
        }))
        .sort((a, b) => b.averageScore - a.averageScore)
        .slice(0, limit);
}

function recentActivity(limit = 6) {
    const studentsById = new Map(state.data.students.map((student) => [student.id, student]));
    const subjectsById = new Map(state.refs.subjects.map((subject) => [subject.id, subject]));
    return [...state.data.grades]
        .sort((left, right) => new Date(right.date) - new Date(left.date))
        .slice(0, limit)
        .map((grade) => ({
            ...grade,
            student: studentsById.get(grade.studentId),
            subject: subjectsById.get(grade.subjectId),
        }));
}

function dashboardTemplate() {
    const grades = state.data.grades.map((grade) => grade.score).filter((score) => Number.isFinite(score));
    const avg = average(grades);
    const classesCount = state.data.classes.length || 1;
    const topSubjects = topSubjectsByAverage();
    const recent = recentActivity();
    const schoolInfo = `<section class="grid gap-5 xl:grid-cols-3"><article class="card-base p-6 xl:col-span-2"><h3 class="text-lg font-semibold text-slate-900">Средняя школа №12 "Солнечные детки"</h3><p class="mt-2 text-sm text-slate-600">Официальный сайт школы: успеваемость, ключевые показатели учебного процесса.</p><div class="mt-5 grid gap-3 sm:grid-cols-2"><div class="rounded-xl bg-slate-50 p-4"><p class="text-xs uppercase tracking-wide text-slate-500">Классов в школе</p><p class="mt-1 text-sm font-semibold text-slate-800">${state.data.classes.length}</p></div><div class="rounded-xl bg-slate-50 p-4"><p class="text-xs uppercase tracking-wide text-slate-500">Учебных предметов</p><p class="mt-1 text-sm font-semibold text-slate-800">${state.data.subjects.length}</p></div><div class="rounded-xl bg-slate-50 p-4"><p class="text-xs uppercase tracking-wide text-slate-500">Обучающихся</p><p class="mt-1 text-sm font-semibold text-slate-800">${state.data.students.length}</p></div><div class="rounded-xl bg-slate-50 p-4"><p class="text-xs uppercase tracking-wide text-slate-500">Оценок в журнале</p><p class="mt-1 text-sm font-semibold text-slate-800">${state.data.grades.length}</p></div></div></article><article class="card-base p-6"><h3 class="text-base font-semibold text-slate-900">Показатели по предметам</h3><ul class="mt-4 space-y-3 text-sm">${topSubjects.length ? topSubjects.map((subject) => `<li class="rounded-xl bg-slate-50 p-3"><div class="flex items-center justify-between"><p class="font-medium text-slate-800">${subject.name}</p><span class="text-xs text-slate-500">${subject.count} оценок</span></div><div class="mt-2 h-2.5 overflow-hidden rounded-full bg-slate-200"><div class="h-full rounded-full bg-indigo-500" style="width:${Math.min(100, (subject.averageScore / 10) * 100)}%"></div></div><p class="mt-1 text-xs text-slate-600">Средний балл: <span class="font-semibold">${subject.averageScore.toFixed(2)}</span></p></li>`).join('') : '<li class="rounded-xl bg-slate-50 p-3 text-slate-500">Пока нет данных об успеваемости.</li>'}</ul></article></section>`;
    if (isTeacherRole()) {
        return `<section class="grid gap-5 lg:grid-cols-3 mb-6"><article class="card-base p-6 lg:col-span-2"><div class="flex items-center justify-between"><h3 class="text-base font-semibold text-slate-900">События учебного дня</h3><span class="text-xs text-slate-500">Последние ${recent.length} записей</span></div><ul class="mt-4 space-y-3">${recent.length ? recent.map((item) => `<li class="flex items-center gap-3 rounded-xl border border-slate-100 px-3 py-2.5"><span class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-indigo-100 text-xs font-semibold text-indigo-700">${initials(item.student)}</span><div class="min-w-0 flex-1"><p class="truncate text-sm font-medium text-slate-800">${fullName(item.student)}</p><p class="truncate text-xs text-slate-500">${item.subject?.name || 'Предмет'} · ${formatDate(item.date)}</p></div><span class="rounded-lg bg-emerald-50 px-2 py-1 text-sm font-semibold text-emerald-700">${item.score}</span></li>`).join('') : '<li class="rounded-xl bg-slate-50 p-3 text-sm text-slate-500">Пока нет новых записей в электронном журнале.</li>'}</ul></article><article class="card-base p-6"><h3 class="text-sm font-semibold uppercase tracking-wider text-slate-500">Средний балл по моим предметам</h3><p class="mt-2 text-2xl font-semibold text-slate-900">${avg.toFixed(2)}</p><div class="mt-3 h-3 overflow-hidden rounded-full bg-slate-200"><div class="h-full rounded-full bg-indigo-500" style="width:${Math.min(100, (avg / 10) * 100)}%"></div></div><p class="mt-3 text-xs text-slate-500">Охват классов: ${Math.min(100, Math.round((state.data.subjects.length / classesCount) * 100))}%.</p></article></section>${schoolInfo}`;
    }

    return `<section class="grid gap-5 lg:grid-cols-3 mb-6"><article class="card-base p-6 lg:col-span-2"><div class="flex items-center justify-between"><h3 class="text-base font-semibold text-slate-900">События и отметки за день</h3><span class="text-xs text-slate-500">Последние ${recent.length} записей</span></div><ul class="mt-4 space-y-3">${recent.length ? recent.map((item) => `<li class="flex items-center gap-3 rounded-xl border border-slate-100 px-3 py-2.5"><span class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-indigo-100 text-xs font-semibold text-indigo-700">${initials(item.student)}</span><div class="min-w-0 flex-1"><p class="truncate text-sm font-medium text-slate-800">${fullName(item.student)}</p><p class="truncate text-xs text-slate-500">${item.subject?.name || 'Предмет'} · ${formatDate(item.date)}</p></div><span class="rounded-lg bg-emerald-50 px-2 py-1 text-sm font-semibold text-emerald-700">${item.score}</span></li>`).join('') : '<li class="rounded-xl bg-slate-50 p-3 text-sm text-slate-500">Журнал пока пуст. Добавьте оценки, чтобы видеть динамику.</li>'}</ul></article><article class="card-base p-6"><h3 class="text-sm font-semibold uppercase tracking-wider text-slate-500">Распределение успеваемости</h3><div class="mt-4 space-y-3">${[10,9,8,7,6].map((score) => { const count = state.data.grades.filter((grade) => grade.score === score).length; const percent = state.data.grades.length ? Math.round((count / state.data.grades.length) * 100) : 0; return `<div><div class="mb-1 flex items-center justify-between text-xs text-slate-600"><span>${score} баллов</span><span>${count}</span></div><div class="h-2 overflow-hidden rounded-full bg-slate-200"><div class="h-full rounded-full bg-indigo-500" style="width:${percent}%"></div></div></div>`; }).join('')}<p class="mt-3 text-xs text-slate-500">Общий средний балл: ${avg.toFixed(2)}</p></div></article></section>${schoolInfo}`;
}

function loginTemplate() {
    return `<div class="min-h-screen flex items-center justify-center p-4 sm:p-6"><form id="login-form" class="w-full max-w-md card-base p-8 sm:p-10 space-y-5"><div><h1 class="text-3xl font-bold tracking-tight mb-1">Панель школы</h1><p class="text-sm text-slate-500">Вход в административную панель</p></div><label class="block text-sm text-slate-700">Эл. почта<input class="mt-1.5 w-full input-base" type="email" name="email" required /></label><label class="block text-sm text-slate-700">Пароль<input class="mt-1.5 w-full input-base" type="password" name="password" required /></label><button class="w-full btn-primary">Войти</button></form></div>`;
}

function shellTemplate(content) {
    return `
    <div class="h-screen overflow-hidden bg-slate-100 flex flex-col">
      <header class="z-30 border-b border-slate-200 bg-white/90 backdrop-blur">
        <div class="mx-auto flex max-w-[1600px] items-center justify-between gap-4 px-4 py-4 md:px-6 lg:px-8">
          <div class="flex items-center gap-3">
            <img src="/favicon.ico" alt="Логотип школы" loading="eager" decoding="async" class="h-11 w-11 rounded-xl border border-slate-200 bg-white p-1.5 shadow-sm" />
            <div>
              <h1 class="text-lg font-semibold tracking-tight text-slate-900">Школьная административная панель</h1>
              <p class="text-sm text-slate-500">Администрирование и управление учебными данными.</p>
            </div>
            </div>
          <div class="flex items-center gap-3">
            <div class="hidden rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-right sm:block">
              <p class="text-xs text-slate-500">Пользователь</p>
              <p class="text-sm font-medium text-slate-700">${state.auth.email || 'admin@gov.by'}</p>
            </div>
            <a href="#/login" data-logout class="btn-secondary">Выйти</a>
          </div>
        </div>
      </header>
       <main class="mx-auto grid min-h-0 w-full max-w-[1600px] flex-1 gap-0 md:grid-cols-[260px_minmax(0,1fr)]">
        <aside class="h-full overflow-y-auto border-r border-slate-200 bg-white p-4 md:p-5">
          <div id="shell-nav"></div>
        </aside>
         <section class="min-w-0 overflow-y-auto p-4 md:p-6 lg:p-8">
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

    return `<section class="mt-5 card-base overflow-hidden"><div class="p-5 border-b border-slate-100"><h3 class="text-base font-semibold text-slate-900">Соответствие Класс ↔ Предмет</h3><p class="text-sm text-slate-500 mt-1">Таблица показывает, какие предметы закреплены за каждым классом.</p></div><table class="w-full"><thead class="bg-slate-50"><tr><th class="px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-500">Класс</th><th class="px-4 py-3 text-left text-xs uppercase tracking-wider text-slate-500">Предметы</th></tr></thead><tbody>${classes.map((schoolClass) => { const subjectNames = (schoolClass.subjectIds || []).map((subjectId) => subjectsById.get(subjectId)).filter(Boolean); return `<tr class="border-t border-slate-100"><td class="px-4 py-3 text-sm text-slate-700 font-medium">${classLabel(schoolClass)}</td><td class="px-4 py-3 text-sm text-slate-700">${subjectNames.length ? subjectNames.join(', ') : '—'}</td></tr>`; }).join('')}</tbody></table></section>`;
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

    const classSubjectRelationsSection = state.route === 'classes' ? `<section class="mt-5"><button data-toggle-class-subject-matrix class="btn-secondary">${state.ui.classSubjectMatrixVisible ? 'Скрыть связь класс-предмет' : 'Показать связь класс-предмет'}</button>${state.ui.classSubjectMatrixVisible ? renderClassSubjectMatrix() : ''}</section>` : '';

    const content = `
    <section class="space-y-6">
      <div class="card-base p-6">
        <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 class="text-2xl font-semibold tracking-tight text-slate-900">${config.title}</h2>
            <p class="mt-1 text-sm text-slate-500">Создавайте, изменяйте, удаляйте данные.</p>
          </div>
          ${canEdit ? `<button data-create-entity class="btn-primary" aria-label="${CREATE_ACTION_LABEL}">${CREATE_ACTION_LABEL}</button>` : ''}
        </div>
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
    const logoutLink = document.querySelector('[data-logout]');
    if (!logoutLink) return;

    logoutLink.onclick = () => {
        setToken('');
        notify('Вы вышли из системы', 'info');
    };
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
    document.querySelectorAll('[data-create-entity]').forEach((el) => el.addEventListener('click', () => openEntityDrawer(null)));

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

    document.querySelectorAll('[data-view-id]').forEach((element) => {
        element.addEventListener('click', (e) => {
            const rowId = Number(e.currentTarget.dataset.viewId);
            const row = byId(state.data[state.route], rowId);
            infoModal({
                title: `Просмотр: ${config.title}`,
                body: renderEntityDetails(state.route, row),
            });
        });
    });


    document.getElementById('search-input')?.addEventListener('input', (e) => debouncedSearch(e.target.value));

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