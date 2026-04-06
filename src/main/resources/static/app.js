const appState = {
    activeEntity: 'students', data: {
        students: [], classes: [], subjects: [], teachers: [], grades: [],
    }, editId: null,
};

const entities = {
    students: {
        title: 'Ученики',
        endpoint: '/api/students',
        listParser: (payload) => payload?.content || [],
        fields: [{key: 'firstName', label: 'Имя', type: 'text', required: true}, {
            key: 'lastName',
            label: 'Фамилия',
            type: 'text',
            required: true
        }, {
            key: 'gender',
            label: 'Пол',
            type: 'select',
            options: [{value: 'MALE', text: 'Мужской'}, {value: 'FEMALE', text: 'Женский'}]
        }, {key: 'email', label: 'Email', type: 'email', required: true}, {
            key: 'schoolClassId',
            label: 'Класс',
            type: 'select-ref',
            ref: 'classes',
            emptyText: 'Без класса'
        },],
        columns: ['Имя', 'Фамилия', 'Пол', 'Email', 'Класс', 'Действия'],
        row: (item) => [item.firstName, item.lastName, item.gender === 'MALE' ? 'Мужской' : 'Женский', item.email, classNameById(item.schoolClassId)],
        payload: (form) => ({
            firstName: form.firstName,
            lastName: form.lastName,
            gender: form.gender,
            email: form.email,
            schoolClassId: form.schoolClassId ? Number(form.schoolClassId) : null,
        }),
    }, classes: {
        title: 'Классы',
        endpoint: '/api/classes',
        listParser: (payload) => payload || [],
        fields: [{
            key: 'grade',
            label: 'Параллель (1-11)',
            type: 'number',
            min: 1,
            max: 11,
            required: true
        }, {key: 'letter', label: 'Буква', type: 'text', required: true},],
        columns: ['Класс', 'Учеников', 'Предметов', 'Действия'],
        row: (item) => [`${item.grade}${item.letter}`, String((item.studentIds || []).length), String((item.subjectIds || []).length)],
        payload: (form) => ({grade: Number(form.grade), letter: form.letter}),
    }, subjects: {
        title: 'Предметы',
        endpoint: '/api/subjects',
        listParser: (payload) => payload || [],
        fields: [{key: 'name', label: 'Название', type: 'text', required: true}, {
            key: 'description',
            label: 'Описание',
            type: 'textarea'
        }, {key: 'teacherId', label: 'Учитель', type: 'select-ref', ref: 'teachers', emptyText: 'Без учителя'},],
        columns: ['Название', 'Описание', 'Учитель', 'Действия'],
        row: (item) => [item.name, item.description || '—', teacherNameById(item.teacherId)],
        payload: (form) => ({
            name: form.name, description: form.description, teacherId: form.teacherId ? Number(form.teacherId) : null
        }),
    }, teachers: {
        title: 'Учителя',
        endpoint: '/api/teachers',
        listParser: (payload) => payload || [],
        fields: [{key: 'firstName', label: 'Имя', type: 'text', required: true}, {
            key: 'lastName',
            label: 'Фамилия',
            type: 'text',
            required: true
        }, {key: 'email', label: 'Email', type: 'email', required: true},],
        columns: ['Имя', 'Фамилия', 'Email', 'Предметов', 'Действия'],
        row: (item) => [item.firstName, item.lastName, item.email, String((item.subjects || []).length)],
        payload: (form) => ({firstName: form.firstName, lastName: form.lastName, email: form.email}),
    }, grades: {
        title: 'Оценки',
        endpoint: '/api/grades',
        listParser: (payload) => payload || [],
        fields: [{key: 'score', label: 'Оценка (2-10)', type: 'number', min: 2, max: 10, required: true}, {
            key: 'date',
            label: 'Дата',
            type: 'date',
            required: true
        }, {key: 'studentId', label: 'Ученик', type: 'select-ref', ref: 'students', required: true}, {
            key: 'subjectId',
            label: 'Предмет',
            type: 'select-ref',
            ref: 'subjects',
            required: true
        },],
        columns: ['Оценка', 'Дата', 'Ученик', 'Предмет', 'Действия'],
        row: (item) => [String(item.score), item.date, studentNameById(item.studentId), subjectNameById(item.subjectId)],
        payload: (form) => ({
            score: Number(form.score),
            date: form.date,
            studentId: Number(form.studentId),
            subjectId: Number(form.subjectId),
        }),
    },
};

const mainNav = document.getElementById('mainNav');
const entityTitle = document.getElementById('entityTitle');
const formTitle = document.getElementById('formTitle');
const tableHead = document.getElementById('tableHead');
const tableBody = document.getElementById('tableBody');
const entityForm = document.getElementById('entityForm');
const emptyState = document.getElementById('emptyState');
const notice = document.getElementById('notice');
const todayDate = document.getElementById('todayDate');

function setDate() {
    const date = new Date();
    todayDate.textContent = date.toLocaleDateString('ru-RU', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });
}

function showNotice(text, kind = 'success') {
    notice.textContent = text;
    notice.className = `notice ${kind}`;
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: {'Content-Type': 'application/json', ...(options.headers || {})}, ...options,
    });

    if (response.status === 204) {
        return null;
    }

    const raw = await response.text();
    const payload = raw ? JSON.parse(raw) : null;

    if (!response.ok) {
        const message = payload?.message || payload?.error || `Ошибка ${response.status}`;
        throw new Error(message);
    }

    return payload;
}

async function loadData() {
    try {
        const [studentsRes, classesRes, teachersRes, subjectsRes, gradesRes] = await Promise.all([api('/api/students'), api('/api/classes/with-subjects'), api('/api/teachers'), api('/api/subjects'), api('/api/grades'),]);

        appState.data.students = studentsRes?.content || [];
        appState.data.classes = classesRes || [];
        appState.data.teachers = teachersRes || [];
        appState.data.subjects = subjectsRes || [];
        appState.data.grades = gradesRes || [];
    } catch (error) {
        showNotice(`Не получилось загрузить данные: ${error.message}`, 'error');
    }
}

function classNameById(id) {
    const found = appState.data.classes.find((item) => item.id === id);
    return found ? `${found.grade}${found.letter}` : '—';
}

function teacherNameById(id) {
    const found = appState.data.teachers.find((item) => item.id === id);
    return found ? `${found.firstName} ${found.lastName}` : '—';
}

function studentNameById(id) {
    const found = appState.data.students.find((item) => item.id === id);
    return found ? `${found.firstName} ${found.lastName}` : '—';
}

function subjectNameById(id) {
    const found = appState.data.subjects.find((item) => item.id === id);
    return found ? found.name : '—';
}

function labelByRef(refName, item) {
    if (refName === 'classes') return `${item.grade}${item.letter}`;
    if (refName === 'students') return `${item.firstName} ${item.lastName}`;
    if (refName === 'teachers') return `${item.firstName} ${item.lastName}`;
    if (refName === 'subjects') return item.name;
    return String(item.id);
}

function renderNav() {
    const entries = Object.keys(entities);
    mainNav.innerHTML = entries.map((key) => {
        const activeClass = key === appState.activeEntity ? 'active' : '';
        return `<button class="tab-btn ${activeClass}" data-entity="${key}">${entities[key].title}</button>`;
    }).join('');

    mainNav.querySelectorAll('button').forEach((btn) => {
        btn.addEventListener('click', () => {
            appState.activeEntity = btn.dataset.entity;
            appState.editId = null;
            render();
        });
    });
}

function renderTable() {
    const config = entities[appState.activeEntity];
    const list = appState.data[appState.activeEntity] || [];

    entityTitle.textContent = `${config.title} — список`;
    tableHead.innerHTML = `<tr>${config.columns.map((col) => `<th>${col}</th>`).join('')}</tr>`;

    if (!list.length) {
        tableBody.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }

    emptyState.classList.add('hidden');
    tableBody.innerHTML = list.map((item) => {
        const values = config.row(item).map((value) => `<td>${value}</td>`).join('');
        const actions = `
            <td>
                <button class="edit-btn" data-action="edit" data-id="${item.id}">Изменить</button>
                <button class="delete-btn" data-action="delete" data-id="${item.id}">Удалить</button>
            </td>`;
        return `<tr>${values}${actions}</tr>`;
    }).join('');

    tableBody.querySelectorAll('button[data-action="edit"]').forEach((btn) => {
        btn.addEventListener('click', () => {
            appState.editId = Number(btn.dataset.id);
            renderForm();
        });
    });

    tableBody.querySelectorAll('button[data-action="delete"]').forEach((btn) => {
        btn.addEventListener('click', async () => {
            const id = Number(btn.dataset.id);
            if (!window.confirm('Точно удалить запись?')) return;
            try {
                await api(`${config.endpoint}/${id}`, {method: 'DELETE'});
                await loadData();
                showNotice('Запись удалена.');
                render();
            } catch (error) {
                showNotice(error.message, 'error');
            }
        });
    });
}

        function renderField(field, value) {
            if (field.type === 'textarea') {
                return `<label>${field.label}<textarea name="${field.key}">${value || ''}</textarea></label>`;
            }

            if (field.type === 'select') {
                const options = field.options.map((opt) => {
                    const selected = value === opt.value ? 'selected' : '';
                    return `<option value="${opt.value}" ${selected}>${opt.text}</option>`;
                }).join('');
                return `<label>${field.label}<select name="${field.key}">${options}</select></label>`;
            }

            if (field.type === 'select-ref') {
                const list = appState.data[field.ref] || [];
                const top = field.emptyText ? `<option value="">${field.emptyText}</option>` : '<option value="">Выберите</option>';
                const options = list.map((item) => {
                    const optionValue = String(item.id);
                    const selected = String(value || '') === optionValue ? 'selected' : '';
                    return `<option value="${optionValue}" ${selected}>${labelByRef(field.ref, item)}</option>`;
                }).join('');
                return `<label>${field.label}<select name="${field.key}" ${field.required ? 'required' : ''}>${top}${options}</select></label>`;
            }

            return `<label>${field.label}<input type="${field.type}" name="${field.key}" value="${value || ''}" ${field.required ? 'required' : ''} ${field.min ? `min="${field.min}"` : ''} ${field.max ? `max="${field.max}"` : ''}></label>`;
        }

        function formValuesFromItem(config, item) {
            const result = {};
            config.fields.forEach((field) => {
                result[field.key] = item?.[field.key] ?? '';
            });
            return result;
        }

        function renderForm() {
            const config = entities[appState.activeEntity];
            const editingItem = appState.editId ? (appState.data[appState.activeEntity] || []).find((item) => item.id === appState.editId) : null;

            formTitle.textContent = editingItem ? `${config.title} — изменить` : `${config.title} — создать`;

            const values = formValuesFromItem(config, editingItem);
            const fieldsHtml = config.fields.map((field) => renderField(field, values[field.key])).join('');

            entityForm.innerHTML = `
        ${fieldsHtml}
        <div class="form-actions">
            <button type="submit" class="primary-btn">${editingItem ? 'Сохранить' : 'Создать'}</button>
            <button type="button" id="cancelBtn" class="secondary-btn">Очистить</button>
        </div>
    `;

            entityForm.onsubmit = async (event) => {
                event.preventDefault();
                const formData = new FormData(entityForm);
                const raw = Object.fromEntries(formData.entries());

                try {
                    const payload = config.payload(raw);
                    const method = editingItem ? 'PUT' : 'POST';
                    const path = editingItem ? `${config.endpoint}/${editingItem.id}` : config.endpoint;
                    await api(path, {method, body: JSON.stringify(payload)});
                    appState.editId = null;
                    await loadData();
                    showNotice(editingItem ? 'Запись изменена.' : 'Запись создана.');
                    render();
                } catch (error) {
                    showNotice(error.message, 'error');
                }
            };

            document.getElementById('cancelBtn').addEventListener('click', () => {
                appState.editId = null;
                renderForm();
            });
        }

        function render() {
            renderNav();
            renderTable();
            renderForm();
        }

        async function startApp() {
            setDate();
            await loadData();
            render();
        }

        void startApp();