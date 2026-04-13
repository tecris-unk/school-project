import {api} from './api.js';
import {resetUiState, setState, setToken, state, subscribe} from './state.js';
import {debounce} from './utils/debounce.js';
import {average, byId, classLabel, fullName} from './utils/helpers.js';
import {formDataToObject, renderEntityForm, validateForm} from './ui/form.js';
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
        inlineEditable: ['firstName', 'lastName', 'email'],
        columns: [{key: 'firstName', label: 'Имя'}, {
            key: 'lastName',
            label: 'Фамилия'
        }, {key: 'gender', label: 'Пол'}, {key: 'email', label: 'Эл. почта'}, {key: 'schoolClassId', label: 'Класс'},],
        filters: [{
            key: 'schoolClassId',
            label: 'Класс',
            options: (refs) => refs.classes.map((item) => ({value: item.id, label: classLabel(item)})),
        },],
        fields: [{key: 'firstName', label: 'Имя', required: true}, {
            key: 'lastName',
            label: 'Фамилия',
            required: true
        }, {
            key: 'gender',
            label: 'Пол',
            type: 'select',
            required: true,
            defaultValue: 'MALE',
            options: [{value: 'MALE', label: 'Мужской'}, {value: 'FEMALE', label: 'Женский'},],
        }, {key: 'email', label: 'Эл. почта', type: 'email', required: true}, {
            key: 'schoolClassId',
            label: 'Класс',
            type: 'ref',
            ref: 'classes',
            allowEmpty: true
        },],
        payload: (v) => ({...v, schoolClassId: v.schoolClassId ? Number(v.schoolClassId) : null}),
        linkAction: {label: 'Привязать класс'},
    }, classes: {
        title: 'Классы',
        endpoint: 'classes',
        inlineEditable: ['grade', 'letter'],
        columns: [{key: 'grade', label: 'Параллель'}, {key: 'letter', label: 'Буква'},],
        filters: [],
        fields: [{key: 'grade', label: 'Параллель', type: 'number', required: true}, {
            key: 'letter',
            label: 'Буква',
            required: true
        },],
        payload: (v) => ({grade: Number(v.grade), letter: v.letter}),
        linkAction: {label: 'Привязать предмет'},
    }, subjects: {
        title: 'Предметы',
        endpoint: 'subjects',
        inlineEditable: ['name', 'description'],
        columns: [{key: 'name', label: 'Название'}, {
            key: 'description',
            label: 'Описание'
        }, {key: 'teacherId', label: 'Учитель'},],
        filters: [{
            key: 'teacherId',
            label: 'Учитель',
            options: (refs) => refs.teachers.map((teacher) => ({value: teacher.id, label: fullName(teacher)})),
        },],
        fields: [{key: 'name', label: 'Название', required: true}, {
            key: 'description',
            label: 'Описание',
            type: 'textarea'
        }, {key: 'teacherId', label: 'Учитель', type: 'ref', ref: 'teachers', allowEmpty: true},],
        payload: (v) => ({...v, teacherId: v.teacherId ? Number(v.teacherId) : null}),
        linkAction: {label: 'Привязать учителя'},
    }, teachers: {
        title: 'Учителя',
        endpoint: 'teachers',
        inlineEditable: ['firstName', 'lastName', 'email'],
        columns: [{key: 'firstName', label: 'Имя'}, {
            key: 'lastName',
            label: 'Фамилия'
        }, {key: 'email', label: 'Эл. почта'},],
        filters: [],
        fields: [{key: 'firstName', label: 'Имя', required: true}, {
            key: 'lastName',
            label: 'Фамилия',
            required: true
        }, {key: 'email', label: 'Эл. почта', type: 'email', required: true},],
        payload: (v) => v,
        linkAction: {label: 'Привязать предмет'},
    }, grades: {
        title: 'Оценки',
        endpoint: 'grades',
        inlineEditable: ['score', 'date'],
        columns: [{key: 'score', label: 'Оценка'}, {
            key: 'date',
            label: 'Дата'
        }, {key: 'studentId', label: 'Ученик'}, {key: 'subjectId', label: 'Предмет'},],
        filters: [{
            key: 'subjectId',
            label: 'Предмет',
            options: (refs) => refs.subjects.map((subject) => ({value: subject.id, label: subject.name}))
        }, {
            key: 'studentId',
            label: 'Ученик',
            options: (_, data) => data.students.map((student) => ({value: student.id, label: fullName(student)}))
        },],
        fields: [{key: 'score', label: 'Оценка', type: 'number', required: true}, {
            key: 'date',
            label: 'Дата',
            type: 'date',
            required: true
        }, {key: 'studentId', label: 'Ученик', type: 'ref', ref: 'students', required: true}, {
            key: 'subjectId',
            label: 'Предмет',
            type: 'ref',
            ref: 'subjects',
            required: true
        },],
        payload: (v) => ({
            ...v, score: Number(v.score), studentId: Number(v.studentId), subjectId: Number(v.subjectId)
        }),
    },
};

function parseRoute() {
    const hash = window.location.hash.replace('#/', '') || 'dashboard';
    return navItems.find((item) => item.key === hash)?.key || (hash === 'login' ? 'login' : 'dashboard');
}

function isTeacherRole() {
    return state.auth.role === 'teacher';
}

function canEditEntity(entity) {
    return state.auth.role === 'admin' || (isTeacherRole() && entity === 'grades');
}

function canDeleteEntity() {
    return state.auth.role === 'admin';
}

function allowedRoutesForRole() {
    if (isTeacherRole()) {
        return new Set(['dashboard', 'classes', 'grades']);
    }
    return new Set(navItems.map((item) => item.key));
}

function getTeacherSubjectIds() {
    return new Set(state.refs.subjects.map((subject) => subject.id));
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
    if (!currentTeacher) {
        throw new Error('Учитель с таким email не найден. Попросите администратора добавить вас в систему.');
    }

    const subjects = await api.subjects.list({teacherId: currentTeacher.id});
    const teacherSubjectIds = new Set(subjects.items.map((subject) => subject.id));
    const classes = await api.classes.list();
    const availableClasses = classes.items.filter((schoolClass) =>
        schoolClass.subjectIds?.some((subjectId) => teacherSubjectIds.has(subjectId)),
    );

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

    setState((s) => {
        s.loading = true;
    });

    try {
        const params = meta.pageable ? {page: meta.page, size: meta.size} : {};
        const teacherParams = isTeacherRole() && entity === 'students' ? {teacherEmail: state.auth.email} : {};
        const result = await api[entity].list({...params, ...teacherParams});
        const teacherSubjectIds = getTeacherSubjectIds();
        let filteredItems = result.items;
        if (isTeacherRole() && entity === 'grades') {
            filteredItems = result.items.filter((grade) => teacherSubjectIds.has(grade.subjectId));
        }
        if (isTeacherRole() && entity === 'classes') {
            filteredItems = result.items.filter((schoolClass) =>
                schoolClass.subjectIds?.some((subjectId) => teacherSubjectIds.has(subjectId)),
            );
        }

        setState((s) => {
            s.data[entity] = filteredItems;
            s.meta[entity] = {
                ...s.meta[entity], ...result.meta,
                totalElements: result.meta?.totalElements ?? filteredItems.length,
                totalPages: result.meta?.totalPages ?? Math.max(1, Math.ceil(filteredItems.length / s.meta[entity].size)),
            };
            s.loading = false;
        });
    } catch (error) {
        setState((s) => {
            s.loading = false;
        });
        notify(error.message, 'error');
    }
}

function getVisibleRows(entity) {
    const config = entityConfigs[entity];
    const meta = state.meta[entity];
    const queried = applyQuery(state.data[entity], config, state.ui);

    if (meta.pageable) {
        return {rows: queried, meta: {...meta, totalElements: meta.totalElements}};
    }

    const paged = paginate(queried, meta.page, meta.size);
    return {
        rows: paged, allFiltered: queried, meta: {
            ...meta, totalElements: queried.length, totalPages: Math.max(1, Math.ceil(queried.length / meta.size)),
        },
    };
}

function dashboardTemplate() {
    const grades = state.data.grades.map((grade) => grade.score).filter((score) => Number.isFinite(score));
    const avg = average(grades);
    if (isTeacherRole()) {
        return `
        <section class="grid md:grid-cols-3 gap-4 mb-6">
          <article class="bg-white rounded-xl shadow-sm border border-slate-200 p-5"><p class="text-sm text-slate-500">Мои классы</p><h2 class="text-3xl font-semibold mt-2">${state.data.classes.length}</h2></article>
          <article class="bg-white rounded-xl shadow-sm border border-slate-200 p-5"><p class="text-sm text-slate-500">Мои предметы</p><h2 class="text-3xl font-semibold mt-2">${state.data.subjects.length}</h2></article>
          <article class="bg-white rounded-xl shadow-sm border border-slate-200 p-5"><p class="text-sm text-slate-500">Средний балл</p><h2 class="text-3xl font-semibold mt-2">${avg.toFixed(2)}</h2></article>
        </section>
        <div class="bg-white rounded-xl border border-slate-200 p-5 text-slate-600">Вам доступны только ваши классы и журнал оценок по вашим предметам.</div>
      `;
    }

    return `
    <section class="grid md:grid-cols-3 gap-4 mb-6">
        <article class="bg-white rounded-xl shadow-sm border border-slate-200 p-5"><p class="text-sm text-slate-500">Ученики</p><h2 class="text-3xl font-semibold mt-2">${state.data.students.length}</h2></article>
        <article class="bg-white rounded-xl shadow-sm border border-slate-200 p-5"><p class="text-sm text-slate-500">Средний балл</p><h2 class="text-3xl font-semibold mt-2">${avg.toFixed(2)}</h2></article>
        <article class="bg-white rounded-xl shadow-sm border border-slate-200 p-5"><p class="text-sm text-slate-500">Учителя</p><h2 class="text-3xl font-semibold mt-2">${state.data.teachers.length}</h2></article>
    </section>
        `;
}

function loginTemplate() {
    return `
    <div class="min-h-screen flex items-center justify-center bg-slate-100 p-4">
      <form id="login-form" class="w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-lg p-6">
        <h1 class="text-2xl font-semibold text-slate-900 mb-1">Панель школы</h1>
        <p class="text-sm text-slate-500 mb-5">Вход в административную панель</p>
        <label class="block text-sm mb-3">Эл. почта
          <input class="mt-1 w-full rounded-lg border-slate-300" type="email" name="email" required />
        </label>
        <label class="block text-sm mb-3">Пароль
          <input class="mt-1 w-full rounded-lg border-slate-300" type="password" name="password" required />
        </label>
        <button class="w-full py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-700">Войти</button>
      </form>
    </div>
  `;
}

function shellTemplate(content) {
    return `
    <div class="min-h-screen bg-slate-100">
      <header class="bg-white/95 sticky top-0 backdrop-blur border-b border-slate-200">
        <div class="max-w-7xl mx-auto px-4 py-4">
          <h1 class="text-xl font-semibold flex items-center gap-2">
            <img src="/favicon.ico" alt="Логотип школы" class="h-14 w-14 rounded-md object-contain border border-slate-200 bg-white p-1" />
            <span>Школа №12 г.Витебска</span>
          </h1>
          <p class="text-sm text-slate-500">На этой страничке вы можете создать, просмотреть, изменить и удалить данные об этой школе</p>
        </div>
      </header>
      <main class="max-w-7xl mx-auto p-4">
        ${renderNav(state.route, state.auth.role)}
        ${content}
      </main>
      <div class="fixed right-4 top-4 z-50 transition-opacity duration-200 ${state.loading ? 'opacity-100' : 'opacity-0 pointer-events-none'}">
        <div class="flex items-center gap-2 rounded-lg bg-slate-900 text-white px-3 py-2 shadow-lg">
          <span class="inline-block h-3 w-3 rounded-full border-2 border-white/40 border-t-white animate-spin"></span>
          <span class="text-xs">Загрузка...</span>
        </div>
      </div>
    </div>
  `;
}

function renderClassSubjectMatrix() {
    const classes = state.data.classes;
    const subjectsById = new Map(state.refs.subjects.map((subject) => [subject.id, subject.name]));

    return `
    <section class="mt-4 bg-white border border-slate-200 rounded-xl shadow-sm">
      <div class="p-4 border-b border-slate-100">
        <h3 class="text-base font-semibold text-slate-900"> Соответствие Класс ↔ Предмет</h3>
        <p class="text-sm text-slate-500 mt-1">Таблица показывает, какие предметы закреплены за каждым классом.</p>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full">
          <thead class="bg-slate-50">
            <tr>
              <th class="px-3 py-3 text-left text-xs uppercase tracking-wider text-slate-500">Класс</th>
              <th class="px-3 py-3 text-left text-xs uppercase tracking-wider text-slate-500">Предметы</th>
            </tr>
          </thead>
          <tbody>
            ${classes.map((schoolClass) => {
        const subjectNames = (schoolClass.subjectIds || [])
            .map((subjectId) => subjectsById.get(subjectId))
            .filter(Boolean);
        return `<tr class="border-t border-slate-100">
                    <td class="px-3 py-3 text-sm text-slate-700 font-medium">${classLabel(schoolClass)}</td>
                    <td class="px-3 py-3 text-sm text-slate-700">${subjectNames.length ? subjectNames.join(', ') : '—'}</td>
                </tr>`;
    }).join('')}
          </tbody>
        </table>
      </div>
    </section>
  `;
}

function captureFocusSnapshot() {
    const active = document.activeElement;
    if (!active || !app.contains(active)) return null;
    if (!(active instanceof HTMLInputElement || active instanceof HTMLTextAreaElement || active instanceof HTMLSelectElement)) return null;

    return {
        id: active.id || null,
        name: active.getAttribute('name'),
        selectionStart: typeof active.selectionStart === 'number' ? active.selectionStart : null,
        selectionEnd: typeof active.selectionEnd === 'number' ? active.selectionEnd : null,
    };
}

function restoreFocusSnapshot(snapshot) {
    if (!snapshot) return;

    let candidate = null;
    if (snapshot.id) {
        candidate = document.getElementById(snapshot.id);
    }

    if (!candidate && snapshot.name) {
        candidate = app.querySelector(`[name="${snapshot.name}"]`);
    }

    if (!(candidate instanceof HTMLElement)) return;

    candidate.focus({preventScroll: true});

    if (
        candidate instanceof HTMLInputElement ||
        candidate instanceof HTMLTextAreaElement
    ) {
        if (typeof snapshot.selectionStart === 'number' && typeof snapshot.selectionEnd === 'number') {
            candidate.setSelectionRange(snapshot.selectionStart, snapshot.selectionEnd);
        }
    }
}

function render() {
    const focusSnapshot = captureFocusSnapshot();

    if (state.route === 'login' || !state.auth.token) {
        app.innerHTML = loginTemplate();
        bindLogin();
        restoreFocusSnapshot(focusSnapshot);
        return;
    }

    if (state.route === 'dashboard') {
        app.innerHTML = shellTemplate(dashboardTemplate());
        bindGlobalHandlers();
        restoreFocusSnapshot(focusSnapshot);
        return;
    }

    const config = entityConfigs[state.route];
    const {rows, allFiltered = rows, meta} = getVisibleRows(state.route);
    const editingRow = byId(state.data[state.route], state.ui.editingId);
    const canEdit = canEditEntity(state.route);
    const canDelete = canDeleteEntity(state.route);

    const classSubjectRelationsSection = state.route === 'classes' ? `
    <section class="mt-4">
      <button data-toggle-class-subject-matrix class="px-3 py-2 rounded-lg border border-slate-300 text-sm hover:bg-slate-100">
        ${state.ui.classSubjectMatrixVisible ? 'Скрыть связь класс-предмет' : 'Показать связь класс-предмет'}
      </button>
      ${state.ui.classSubjectMatrixVisible ? renderClassSubjectMatrix() : ''}
    </section>` : '';

    const content = `
    <section class="grid lg:grid-cols-12 gap-4">
      <div class="lg:col-span-8">
        ${renderTable({
        entity: state.route,
        config,
        rows,
        refs: state.refs,
        data: state.data,
        ui: state.ui,
        meta,
        pageableFromApi: state.meta[state.route].pageable,
        canEdit,
        canDelete,
    })}
      </div>
     ${canEdit ? `<div class="lg:col-span-4">${renderEntityForm(config, state.refs, state.data, editingRow, state.route)}</div>` : `<div class="lg:col-span-4"><div class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm text-sm text-slate-600">Изменение данных в этом разделе доступно только администратору.</div></div>`}
    </section>
    ${classSubjectRelationsSection}
  `;

    app.innerHTML = shellTemplate(content);
    bindEntityHandlers(config, allFiltered, rows, meta);
    bindGlobalHandlers();
    restoreFocusSnapshot(focusSnapshot);
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
            if (!matchedTeacher) {
                notify('Неверные данные входа', 'error');
                return;
            }
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

function requestIdFromUser(title, options) {
    const optionsText = options.map((item) => `${item.id} — ${item.label}`).join('\n');
    const entered = window.prompt(`${title}\n\n${optionsText}\n\nВведите ID:`);
    if (!entered) return null;
    const id = Number(entered);
    if (!Number.isFinite(id)) return null;
    return id;
}

function bindEntityHandlers(config, allFiltered, displayedRows  ) {
    if (state.route === 'classes') {
        document.querySelector('[data-toggle-class-subject-matrix]')?.addEventListener('click', () => {
            setState((s) => {
                s.ui.classSubjectMatrixVisible = !s.ui.classSubjectMatrixVisible;
            });
        });
    }

    if (canEditEntity(state.route)) {
        document.querySelectorAll('[data-link-id]').forEach((element) => {
            element.addEventListener('click', async (e) => {
                const rowId = Number(e.currentTarget.dataset.linkId);
                try {
                    if (state.route === 'students') {
                        const classId = requestIdFromUser(
                            'Выберите класс для ученика',
                            state.refs.classes.map((item) => ({id: item.id, label: classLabel(item)})),
                        );
                        if (!classId) return;
                        const student = byId(state.data.students, rowId);
                        await api.students.update(rowId, {
                            firstName: student.firstName,
                            lastName: student.lastName,
                            gender: student.gender,
                            email: student.email,
                            schoolClassId: classId,
                        });
                        notify('Ученик привязан к классу', 'success');
                    } else if (state.route === 'subjects') {
                        const teacherId = requestIdFromUser(
                            'Выберите учителя для предмета',
                            state.refs.teachers.map((item) => ({id: item.id, label: fullName(item)})),
                        );
                        if (!teacherId) return;
                        const subject = byId(state.data.subjects, rowId);
                        await api.subjects.update(rowId, {
                            name: subject.name,
                            description: subject.description,
                            teacherId,
                        });
                        notify('Предмет привязан к учителю', 'success');
                    } else if (state.route === 'classes') {
                        const subjectId = requestIdFromUser(
                            'Выберите предмет для класса',
                            state.refs.subjects.map((item) => ({id: item.id, label: item.name})),
                        );
                        if (!subjectId) return;
                        await api.classes.attachSubject(rowId, subjectId);
                        notify('Предмет привязан к классу', 'success');
                    } else if (state.route === 'teachers') {
                        const subjectId = requestIdFromUser(
                            'Выберите предмет для учителя',
                            state.refs.subjects.map((item) => ({id: item.id, label: item.name})),
                        );
                        if (!subjectId) return;
                        const subject = byId(state.data.subjects, subjectId) || byId(state.refs.subjects, subjectId);
                        if (!subject) {
                            notify('Предмет не найден', 'error');
                            return;
                        }
                        await api.subjects.update(subjectId, {
                            name: subject.name,
                            description: subject.description,
                            teacherId: rowId,
                        });
                        notify('Предмет привязан к учителю', 'success');
                    }

                    await loadRefs(true);
                    await loadEntity(state.route, true);
                } catch (error) {
                    notify(error.message, 'error');
                }
            });
        });
        document.getElementById('entity-form')?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const values = formDataToObject(e.target);
            const errors = validateForm(state.route, values);
            if (errors.length) {
                notify(errors[0], 'error');
                return;
            }

            const payload = config.payload(values);
            const previousData = [...state.data[state.route]];

            try {
                if (state.ui.editingId) {
                    const id = state.ui.editingId;
                    setState((s) => {
                        const idx = s.data[s.route].findIndex((item) => item.id === id);
                        if (idx > -1) s.data[s.route][idx] = {...s.data[s.route][idx], ...payload};
                    });
                    const saved = await api[state.route].update(id, payload);
                    setState((s) => {
                        const idx = s.data[s.route].findIndex((item) => item.id === id);
                        if (idx > -1) s.data[s.route][idx] = saved;
                        s.ui.editingId = null;
                    });
                    notify(SAVE_SUCCESS_MESSAGE, 'success');
                } else {
                    const tempId = Date.now() * -1;
                    setState((s) => {
                        s.data[s.route].unshift({id: tempId, ...payload});
                    });
                    const created = await api[state.route].create(payload);
                    setState((s) => {
                        s.data[s.route] = s.data[s.route].map((item) => (item.id === tempId ? created : item));
                        s.meta[s.route].totalElements += 1;
                    });
                    notify(SAVE_SUCCESS_MESSAGE, 'success');
                }
                e.target.reset();
            } catch (error) {
                setState((s) => {
                    s.data[s.route] = previousData;
                });
                notify(error.message, 'error');
            }
        });

        document.querySelector('[data-cancel-edit]')?.addEventListener('click', () => {
            setState((s) => {
                s.ui.editingId = null;
            });
        });
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
            element.addEventListener('click', async (e) => {
                const id = Number(e.currentTarget.dataset.editId);
                if (state.ui.editingId === id) {
                    const rowElement = e.currentTarget.closest('tr');
                    const inlineValues = {};
                    rowElement.querySelectorAll('[data-inline-input]').forEach((input) => {
                        inlineValues[input.dataset.inlineInput] = input.value;
                    });
                    try {
                        const patch = config.payload({...byId(state.data[state.route], id), ...inlineValues});
                        const saved = await api[state.route].update(id, patch);
                        setState((s) => {
                            const idx = s.data[s.route].findIndex((item) => item.id === id);
                            if (idx > -1) s.data[s.route][idx] = saved;
                            s.ui.editingId = null;
                        });
                        notify(SAVE_SUCCESS_MESSAGE, 'success');
                    } catch (error) {
                        notify(error.message, 'error');
                    }
                } else {
                    setState((s) => {
                        s.ui.editingId = id;
                    });
                }
            });
        });
    }

    if (canDeleteEntity(state.route)) {
        document.querySelectorAll('[data-delete-id]').forEach((element) => {
            element.addEventListener('click', (e) => {
                const id = Number(e.currentTarget.dataset.deleteId);
                confirmModal({
                    title: 'Подтвердите удаление',
                    description: 'Эту операцию нельзя отменить.',
                    onConfirm: async () => {
                        const previous = [...state.data[state.route]];
                        setState((s) => {
                            s.data[s.route] = s.data[s.route].filter((item) => item.id !== id);
                            s.ui.selectedIds.delete(id);
                        });
                        try {
                            await api[state.route].remove(id);
                            notify(DELETE_SUCCESS_MESSAGE, 'success');
                        } catch (error) {
                            setState((s) => {
                                s.data[s.route] = previous;
                            });
                            notify(error.message, 'error');
                        }
                    },
                });
            });
        });
    }

    if (canDeleteEntity(state.route)) {
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
                title: 'Массовое удаление',
                description: `Будут удалены записи: ${state.ui.selectedIds.size}`,
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
                        setState((s) => {
                            s.data[s.route] = previous;
                        });
                        notify(`Удаление прервано, ошибок: ${failed}`, 'error');
                    } else {
                        notify('Выбранные записи удалены', 'success');
                    }
                },
            });
        });
    }
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
                if (isTeacherRole()) {
                    await loadEntity('grades', true);
                } else {
                    await Promise.all([loadEntity('students', true), loadEntity('grades', true), loadEntity('teachers', true)]);
                }
            } else if (entityConfigs[route]) {
                if (isTeacherRole() && route === 'grades') {
                    await loadEntity('students', true);
                }
                await loadEntity(route, true);
            }
        } catch (error) {
            notify(error.message, 'error');
        }
    }

    resetUiState();
}

subscribe(render);
window.addEventListener('hashchange', () => {
    routeChanged();
});

if (!window.location.hash) {
    window.location.hash = state.auth.token ? '#/dashboard' : '#/login';
} else {
    routeChanged();
}

render();