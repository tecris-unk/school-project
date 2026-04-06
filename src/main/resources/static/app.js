/* global React, ReactDOM */
const { useEffect, useMemo, useState } = React;

const TABS = [
    { id: 'students', label: 'Ученики' },
    { id: 'classes', label: 'Классы' },
    { id: 'teachers', label: 'Учителя' },
    { id: 'subjects', label: 'Предметы' },
    { id: 'grades', label: 'Оценки' },
];

const initialForms = {
    student: { firstName: '', lastName: '', gender: 'MALE', email: '', schoolClassId: '' },
    class: { grade: '', letter: '' },
    teacher: { firstName: '', lastName: '', email: '' },
    subject: { name: '', description: '', teacherId: '' },
    grade: { score: '', date: '', studentId: '', subjectId: '' },
    relation: { classId: '', subjectId: '' },
};

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options,
    });

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (!response.ok) {
        const message = payload?.message || payload?.error || `HTTP ${response.status}`;
        throw new Error(message);
    }

    return payload;
}

function App() {
    const [activeTab, setActiveTab] = useState('students');
    const [data, setData] = useState({ students: [], classes: [], teachers: [], subjects: [], grades: [] });
    const [forms, setForms] = useState(initialForms);
    const [editing, setEditing] = useState({ entity: null, id: null });
    const [search, setSearch] = useState({ students: '', classes: '', teachers: '', subjects: '', grades: '' });
    const [showRelations, setShowRelations] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });

    const loadAll = async () => {
        setLoading(true);
        try {
            const [studentsPage, classesRaw, teachersRaw, subjectsRaw, gradesRaw, classesWithSubjectsRaw] = await Promise.all([
                api('/api/students'),
                api('/api/classes'),
                api('/api/teachers'),
                api('/api/subjects'),
                api('/api/grades'),
                api('/api/classes/with-subjects'),
            ]);

            const classesWithSubjects = classesWithSubjectsRaw || classesRaw || [];
            setData({
                students: studentsPage?.content || [],
                classes: classesWithSubjects,
                teachers: teachersRaw || [],
                subjects: subjectsRaw || [],
                grades: gradesRaw || [],
            });
        } catch (error) {
            setMessage({ type: 'error', text: `Не удалось загрузить данные: ${error.message}` });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { void loadAll(); }, []);

    const referenceMaps = useMemo(() => ({
        classById: Object.fromEntries(data.classes.map((item) => [item.id, item])),
        teacherById: Object.fromEntries(data.teachers.map((item) => [item.id, item])),
        subjectById: Object.fromEntries(data.subjects.map((item) => [item.id, item])),
        studentById: Object.fromEntries(data.students.map((item) => [item.id, item])),
    }), [data]);

    const classLabel = (id) => {
        const schoolClass = referenceMaps.classById[id];
        return schoolClass ? `${schoolClass.grade}${schoolClass.letter}` : '—';
    };
    const teacherLabel = (id) => {
        const teacher = referenceMaps.teacherById[id];
        return teacher ? `${teacher.firstName} ${teacher.lastName}` : '—';
    };
    const subjectLabel = (id) => referenceMaps.subjectById[id]?.name || '—';
    const studentLabel = (id) => {
        const student = referenceMaps.studentById[id];
        return student ? `${student.firstName} ${student.lastName}` : '—';
    };

    const updateForm = (name, field, value) => {
        setForms((current) => ({ ...current, [name]: { ...current[name], [field]: value } }));
    };

    const resetEntityForm = (entity) => {
        setForms((current) => ({ ...current, [entity]: initialForms[entity] }));
        setEditing({ entity: null, id: null });
    };

    const handleSubmit = async (entity, path, buildPayload) => {
        try {
            setMessage({ type: '', text: '' });
            const isEditing = editing.entity === entity;
            await api(isEditing ? `${path}/${editing.id}` : path, {
                method: isEditing ? 'PUT' : 'POST',
                body: JSON.stringify(buildPayload(forms[entity])),
            });
            setMessage({ type: 'success', text: `${entity.toUpperCase()} успешно ${isEditing ? 'обновлён' : 'создан'}.` });
            resetEntityForm(entity);
            await loadAll();
        } catch (error) {
            setMessage({ type: 'error', text: error.message });
        }
    };

    const handleDelete = async (path) => {
        if (!window.confirm('Удалить запись?')) {
            return;
        }
        try {
            await api(path, { method: 'DELETE' });
            setMessage({ type: 'success', text: 'Запись удалена.' });
            await loadAll();
        } catch (error) {
            setMessage({ type: 'error', text: error.message });
        }
    };

    const submitGrade = async () => {
        await handleSubmit('grade', '/api/grades', (form) => ({
            ...form,
            score: Number(form.score),
            studentId: Number(form.studentId),
            subjectId: Number(form.subjectId),
        }));
    };

    const startEdit = (entity, item) => {
        const payloads = {
            student: {
                firstName: item.firstName || '', lastName: item.lastName || '', gender: item.gender || 'MALE',
                email: item.email || '', schoolClassId: item.schoolClassId || ''
            },
            class: { grade: item.grade || '', letter: item.letter || '' },
            teacher: { firstName: item.firstName || '', lastName: item.lastName || '', email: item.email || '' },
            subject: { name: item.name || '', description: item.description || '', teacherId: item.teacherId || '' },
            grade: { score: item.score || '', date: item.date || '', studentId: item.studentId || '', subjectId: item.subjectId || '' },
        };
        setForms((current) => ({ ...current, [entity]: payloads[entity] }));
        setEditing({ entity, id: item.id });
        setActiveTab(entity === 'class' ? 'classes' : `${entity}s`);
    };

    const connectSubjectToClass = async () => {
        try {
            const { classId, subjectId } = forms.relation;
            await api(`/api/classes/${classId}/subjects/${subjectId}`, { method: 'PUT' });
            setMessage({ type: 'success', text: 'Предмет добавлен в класс.' });
            setForms((current) => ({ ...current, relation: initialForms.relation }));
            await loadAll();
        } catch (error) {
            setMessage({ type: 'error', text: error.message });
        }
    };

    const filteredStudents = useMemo(() => {
        const q = search.students.trim().toLowerCase();
        if (!q) return data.students;
        return data.students.filter((student) => `${student.firstName} ${student.lastName}`.toLowerCase().includes(q));
    }, [data.students, search.students]);

    const filteredTeachers = useMemo(() => {
        const q = search.teachers.trim().toLowerCase();
        if (!q) return data.teachers;
        return data.teachers.filter((teacher) => `${teacher.firstName} ${teacher.lastName} ${teacher.email}`.toLowerCase().includes(q));
    }, [data.teachers, search.teachers]);

    const filteredSubjects = useMemo(() => {
        const q = search.subjects.trim().toLowerCase();
        if (!q) return data.subjects;
        return data.subjects.filter((subject) => `${subject.name} ${subject.description || ''} ${teacherLabel(subject.teacherId)}`.toLowerCase().includes(q));
    }, [data.subjects, search.subjects, referenceMaps.teacherById]);

    const filteredClasses = useMemo(() => {
        const q = search.classes.trim().toLowerCase();
        if (!q) return data.classes;
        return data.classes.filter((schoolClass) => {
            const label = `${schoolClass.grade}${schoolClass.letter}`.toLowerCase();
            const studentsText = (schoolClass.studentIds || []).map((id) => studentLabel(id).toLowerCase()).join(' ');
            const subjectsText = (schoolClass.subjectIds || []).map((id) => subjectLabel(id).toLowerCase()).join(' ');
            return `${label} ${studentsText} ${subjectsText}`.includes(q);
        });
    }, [data.classes, search.classes, referenceMaps.studentById, referenceMaps.subjectById]);

    const filteredGrades = useMemo(() => {
        const q = search.grades.trim().toLowerCase();
        if (!q) return data.grades;
        return data.grades.filter((grade) => `${studentLabel(grade.studentId)} ${subjectLabel(grade.subjectId)} ${grade.date || ''}`.toLowerCase().includes(q));
    }, [data.grades, search.grades, referenceMaps.studentById, referenceMaps.subjectById]);

    return (
        <div className="container">
            <section className="hero">
                <div>
                    <h1>Школа №12 г.Витебска "Солнечные детки"</h1>
                    <p>
                        Наша школа лучшая на районе, да и в городе тоже лучшая, мы вообще крутые ребята
                    </p>
                </div>
            </section>

            {message.text && <div className={message.type === 'error' ? 'error' : 'success'}>{message.text}</div>}

            <div className="tabs">
                {TABS.map((tab) => (
                    <button key={tab.id} className={`tab ${activeTab === tab.id ? 'active' : ''}`} onClick={() => setActiveTab(tab.id)}>
                        {tab.label}
                    </button>
                ))}
            </div>

            <div className="grid">
                <section className="panel section-stack">
                    <h2>Колонки для записи</h2>
                    <EntityForm
                        title="Ученик"
                        active={activeTab === 'students'}
                        editing={editing.entity === 'student'}
                        onSubmit={() => handleSubmit('student', '/api/students', (form) => ({ ...form, schoolClassId: form.schoolClassId || null }))}
                        onReset={() => resetEntityForm('student')}
                    >
                        <div className="form-grid two">
                            <label>Имя<input value={forms.student.firstName} onChange={(e) => updateForm('student', 'firstName', e.target.value)} placeholder="Например, Иван" /></label>
                            <label>Фамилия<input value={forms.student.lastName} onChange={(e) => updateForm('student', 'lastName', e.target.value)} placeholder="Например, Петров" /></label>
                            <label>Пол<select value={forms.student.gender} onChange={(e) => updateForm('student', 'gender', e.target.value)}><option>MALE</option><option>FEMALE</option></select></label>
                            <label>Email<input type="email" value={forms.student.email} onChange={(e) => updateForm('student', 'email', e.target.value)} placeholder="ivan.petrov@school.ru" /></label>
                            <label>Класс<select value={forms.student.schoolClassId} onChange={(e) => updateForm('student', 'schoolClassId', e.target.value)}><option value="">Без класса</option>{data.classes.map((item) => <option key={item.id} value={item.id}>{item.grade}{item.letter}</option>)}</select></label>
                        </div>
                    </EntityForm>

                    <EntityForm title="Класс" active={activeTab === 'classes'} editing={editing.entity === 'class'} onSubmit={() => handleSubmit('class', '/api/classes', (form) => ({ ...form, grade: Number(form.grade) }))} onReset={() => resetEntityForm('class')}>
                        <p className="helper-text">Создайте класс: параллель и буква (например, 7А).</p>
                        <div className="form-grid two">
                            <label>Параллель<input type="number" min="1" max="11" value={forms.class.grade} onChange={(e) => updateForm('class', 'grade', e.target.value)} /></label>
                            <label>Буква<input value={forms.class.letter} onChange={(e) => updateForm('class', 'letter', e.target.value)} placeholder="А, Б, В..." /></label>
                        </div>
                    </EntityForm>

                    <EntityForm title="Учитель" active={activeTab === 'teachers'} editing={editing.entity === 'teacher'} onSubmit={() => handleSubmit('teacher', '/api/teachers', (form) => form)} onReset={() => resetEntityForm('teacher')}>
                        <p className="helper-text">Добавьте учителя и рабочий email.</p>
                        <div className="form-grid two">
                            <label>Имя<input value={forms.teacher.firstName} onChange={(e) => updateForm('teacher', 'firstName', e.target.value)} placeholder="Мария" /></label>
                            <label>Фамилия<input value={forms.teacher.lastName} onChange={(e) => updateForm('teacher', 'lastName', e.target.value)} placeholder="Иванова" /></label>
                            <label>Email<input type="email" value={forms.teacher.email} onChange={(e) => updateForm('teacher', 'email', e.target.value)} placeholder="m.ivanova@school.ru" /></label>
                        </div>
                    </EntityForm>

                    <EntityForm title="Предмет" active={activeTab === 'subjects'} editing={editing.entity === 'subject'} onSubmit={() => handleSubmit('subject', '/api/subjects', (form) => ({ ...form, teacherId: form.teacherId || null }))} onReset={() => resetEntityForm('subject')}>
                        <p className="helper-text">Создайте предмет и закрепите его за учителем (необязательно).</p>
                        <div className="form-grid two">
                            <label>Название<input value={forms.subject.name} onChange={(e) => updateForm('subject', 'name', e.target.value)} placeholder="Математика" /></label>
                            <label>Учитель<select value={forms.subject.teacherId} onChange={(e) => updateForm('subject', 'teacherId', e.target.value)}><option value="">Без учителя</option>{data.teachers.map((item) => <option key={item.id} value={item.id}>{item.firstName} {item.lastName}</option>)}</select></label>
                        </div>
                        <label>Описание<textarea value={forms.subject.description} onChange={(e) => updateForm('subject', 'description', e.target.value)} placeholder="Кратко: что проходят на уроках" /></label>
                    </EntityForm>

                    <EntityForm title="Оценка" active={activeTab === 'grades'} editing={editing.entity === 'grade'} onSubmit={submitGrade} onReset={() => resetEntityForm('grade')}>
                        <p className="helper-text">Выберите ученика, предмет и поставьте оценку.</p>
                        <div className="form-grid two">
                            <label>Балл<input type="number" min="2" max="10" value={forms.grade.score} onChange={(e) => updateForm('grade', 'score', e.target.value)} /></label>
                            <label>Дата<input type="date" value={forms.grade.date} onChange={(e) => updateForm('grade', 'date', e.target.value)} /></label>
                            <label>Ученик<select value={forms.grade.studentId} onChange={(e) => updateForm('grade', 'studentId', e.target.value)}><option value="">Выберите ученика</option>{data.students.map((item) => <option key={item.id} value={item.id}>{item.firstName} {item.lastName}</option>)}</select></label>
                            <label>Предмет<select value={forms.grade.subjectId} onChange={(e) => updateForm('grade', 'subjectId', e.target.value)}><option value="">Выберите предмет</option>{data.subjects.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
                        </div>
                    </EntityForm>

                    {(activeTab === 'classes' || activeTab === 'subjects') && (
                        <div className="panel">
                            <h3>Связь «Класс ↔ Предмет»</h3>
                            <p className="helper-text">Здесь можно назначить предмет конкретному классу.</p>
                            <div className="form-grid two">
                                <label>Класс<select value={forms.relation.classId} onChange={(e) => updateForm('relation', 'classId', e.target.value)}><option value="">Выберите класс</option>{data.classes.map((item) => <option key={item.id} value={item.id}>{item.grade}{item.letter}</option>)}</select></label>
                                <label>Предмет<select value={forms.relation.subjectId} onChange={(e) => updateForm('relation', 'subjectId', e.target.value)}><option value="">Выберите предмет</option>{data.subjects.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
                            </div>
                            <div className="actions"><button onClick={connectSubjectToClass} disabled={!forms.relation.classId || !forms.relation.subjectId}>Добавить предмет в класс</button></div>
                        </div>
                    )}
                </section>

                <section className="panel section-stack">
                    <div className="inline-actions">
                        <button className="secondary tiny-button" onClick={() => setShowRelations((value) => !value)}>
                            {showRelations ? 'Скрыть связи' : 'Показать связи'}
                        </button>
                    </div>
                    {showRelations && (
                        <RelationshipOverview
                            teachers={data.teachers}
                            students={data.students}
                            classes={data.classes}
                            subjects={data.subjects}
                            classLabel={classLabel}
                            subjectLabel={subjectLabel}
                        />
                    )}
                    {activeTab === 'students' && (
                        <>
                            <h2>Список учеников</h2>
                            <label>Поиск по имени или фамилии
                                <input value={search.students} onChange={(e) => setSearch((current) => ({ ...current, students: e.target.value }))} placeholder="Например, Андрей" />
                            </label>
                            <StudentTable students={filteredStudents} classLabel={classLabel} subjectLabel={subjectLabel} onEdit={(item) => startEdit('student', item)} onDelete={(id) => handleDelete(`/api/students/${id}`)} />
                        </>
                    )}

                    {activeTab === 'classes' && (
                        <>
                            <h2>Список классов</h2>
                            <label>Поиск по классу, ученику или предмету<input value={search.classes} onChange={(e) => setSearch((current) => ({ ...current, classes: e.target.value }))} placeholder="Например, 10Б или Математика" /></label>
                            <ClassTable classes={filteredClasses} subjectLabel={subjectLabel} studentLabel={studentLabel} onEdit={(item) => startEdit('class', item)} onDelete={(id) => handleDelete(`/api/classes/${id}`)} />
                        </>
                    )}
                    {activeTab === 'teachers' && (
                        <>
                            <h2>Список учителей</h2>
                            <label>Поиск по имени, фамилии или email<input value={search.teachers} onChange={(e) => setSearch((current) => ({ ...current, teachers: e.target.value }))} placeholder="Например, Мария" /></label>
                            <TeacherTable teachers={filteredTeachers} onEdit={(item) => startEdit('teacher', item)} onDelete={(id) => handleDelete(`/api/teachers/${id}`)} />
                        </>
                    )}
                    {activeTab === 'subjects' && (
                        <>
                            <h2>Предметы</h2>
                            <label>Поиск по названию, описанию или учителю<input value={search.subjects} onChange={(e) => setSearch((current) => ({ ...current, subjects: e.target.value }))} placeholder="Например, алгебра" /></label>
                            <SubjectTable subjects={filteredSubjects} teacherLabel={teacherLabel} classLabel={classLabel} onEdit={(item) => startEdit('subject', item)} onDelete={(id) => handleDelete(`/api/subjects/${id}`)} />
                        </>
                    )}
                    {activeTab === 'grades' && (
                        <>
                            <h2>Оценки</h2>
                            <label>Поиск по имени ученика или предмету
                                <input value={search.grades} onChange={(e) => setSearch((current) => ({ ...current, grades: e.target.value }))} placeholder="Например, Андрей или Математика" />
                            </label>
                            <GradeTable grades={filteredGrades} studentLabel={studentLabel} subjectLabel={subjectLabel} onEdit={(item) => startEdit('grade', item)} onDelete={(id) => handleDelete(`/api/grades/${id}`)} />
                        </>
                    )}
                </section>
            </div>
        </div>
    );
}

function EntityForm({ title, active, editing, onSubmit, onReset, children }) {
    if (!active) return null;
    return (
        <div className="form-block">
            <h3>{editing ? `Редактировать: ${title}` : `Создать: ${title}`}</h3>
            {children}
            <div className="actions">
                <button onClick={onSubmit}>{editing ? 'Сохранить изменения' : 'Создать запись'}</button>
                <button className="secondary" onClick={onReset}>Очистить</button>
            </div>
        </div>
    );
}

function StudentTable({ students, classLabel, subjectLabel, onEdit, onDelete }) {
    if (!students.length) return <div className="empty">Ученики не найдены.</div>;
    return (
        <div className="table-wrap"><table><thead><tr><th>Ученик</th><th>Класс</th><th>Email</th><th>Оценки</th><th></th></tr></thead><tbody>
        {students.map((student) => (
            <tr key={student.id}><td><strong>{student.firstName} {student.lastName}</strong><div className="muted">{student.gender}</div></td><td>{classLabel(student.schoolClassId)}</td><td>{student.email}</td><td>{student.grades?.length ? <div className="badges">{student.grades.map((grade) => <span key={grade.id} className="badge">{subjectLabel(grade.subjectId)}: {grade.score}</span>)}</div> : <span className="muted">Нет оценок</span>}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(student)}>Изменить</button><button className="danger" onClick={() => onDelete(student.id)}>Удалить</button></div></td></tr>
        ))}
        </tbody></table></div>
    );
}

function ClassTable({ classes, subjectLabel, studentLabel, onEdit, onDelete }) {
    if (!classes.length) return <div className="empty">Классы не найдены.</div>;
    return (
        <><div className="table-wrap"><table><thead><tr><th>Класс</th><th>Ученики</th><th>Предметы</th><th></th></tr></thead><tbody>
        {classes.map((item) => <tr key={item.id}><td><strong>{item.grade}{item.letter}</strong></td><td>{item.studentIds?.length ? <div className="badges">{item.studentIds.map((id) => <span key={id} className="badge">{studentLabel(id)}</span>)}</div> : <span className="muted">Нет учеников</span>}</td><td>{item.subjectIds?.length ? <div className="badges">{item.subjectIds.map((id) => <span key={id} className="badge">{subjectLabel(id)}</span>)}</div> : <span className="muted">Нет предметов</span>}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div></>
    );
}

function TeacherTable({ teachers, onEdit, onDelete }) {
    if (!teachers.length) return <div className="empty">Учителя не найдены.</div>;
    return (
        <><div className="table-wrap"><table><thead><tr><th>Учитель</th><th>Email</th><th>Предметы</th><th></th></tr></thead><tbody>
        {teachers.map((item) => <tr key={item.id}><td><strong>{item.firstName} {item.lastName}</strong></td><td>{item.email}</td><td>{item.subjects?.length ? <div className="badges">{item.subjects.map((subject) => <span key={subject.id} className="badge">{subject.name}</span>)}</div> : <span className="muted">Нет предметов</span>}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div></>
    );
}

function SubjectTable({ subjects, teacherLabel, classLabel, onEdit, onDelete }) {
    if (!subjects.length) return <div className="empty">Предметы не найдены.</div>;
    return (
        <><div className="table-wrap"><table><thead><tr><th>Название</th><th>Учитель</th><th>Классы</th><th>Описание</th><th></th></tr></thead><tbody>
        {subjects.map((item) => <tr key={item.id}><td><strong>{item.name}</strong></td><td>{teacherLabel(item.teacherId)}</td><td>{item.schoolClassIds?.length ? <div className="badges">{item.schoolClassIds.map((id) => <span key={id} className="badge">{classLabel(id)}</span>)}</div> : <span className="muted">Не назначен</span>}</td><td>{item.description || '—'}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div></>
    );
}

function GradeTable({ grades, studentLabel, subjectLabel, onEdit, onDelete }) {
    if (!grades.length) return <div className="empty">Оценки не найдены.</div>;
    return (
        <div className="table-wrap"><table><thead><tr><th>Оценка</th><th>Дата</th><th>Ученик</th><th>Предмет</th><th></th></tr></thead><tbody>
        {grades.map((item) => <tr key={item.id}><td>{item.score}</td><td>{item.date}</td><td>{studentLabel(item.studentId)}</td><td>{subjectLabel(item.subjectId)}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div>
    );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);

function RelationshipOverview({ teachers, students, classes, subjects, classLabel, subjectLabel }) {
    return (
        <div className="panel relation-overview">
            <h3>Кто с кем связан</h3>
            <div className="relation-grid">
                <article className="relation-card">
                    <h4>Какие предметы ведут учителя</h4>
                    {teachers.length ? (
                        <ul className="relation-list">
                            {teachers.slice(0, 4).map((teacher) => (
                                <li key={teacher.id}>
                                    <strong>{teacher.firstName} {teacher.lastName}</strong>
                                    {teacher.subjects?.length ? (
                                        <div className="badges">
                                            {teacher.subjects.map((subject) => <span key={subject.id} className="badge">{subject.name}</span>)}
                                        </div>
                                    ) : <span className="muted"> Нет предметов</span>}
                                </li>
                            ))}
                        </ul>
                    ) : <div className="empty">Нет данных по учителям.</div>}
                </article>

                <article className="relation-card">
                    <h4>Успеваемость учеников</h4>
                    {students.length ? (
                        <ul className="relation-list">
                            {students.slice(0, 4).map((student) => (
                                <li key={student.id}>
                                    <strong>{student.firstName} {student.lastName}</strong>
                                    <span className="muted"> ({classLabel(student.schoolClassId)})</span>
                                    {student.grades?.length ? (
                                        <div className="badges">
                                            {student.grades.map((grade) => <span key={grade.id} className="badge">{subjectLabel(grade.subjectId)}: {grade.score}</span>)}
                                        </div>
                                    ) : <span className="muted"> Нет оценок</span>}
                                </li>
                            ))}
                        </ul>
                    ) : <div className="empty">Нет данных по ученикам.</div>}
                </article>

                <article className="relation-card">
                    <h4>Какие предметы в классах</h4>
                    {classes.length ? (
                        <ul className="relation-list">
                            {classes.slice(0, 4).map((schoolClass) => (
                                <li key={schoolClass.id}>
                                    <strong>{schoolClass.grade}{schoolClass.letter}</strong>
                                    {schoolClass.subjectIds?.length ? (
                                        <div className="badges">
                                            {schoolClass.subjectIds.map((id) => <span key={id} className="badge">{subjectLabel(id)}</span>)}
                                        </div>
                                    ) : <span className="muted"> Предметы не назначены</span>}
                                </li>
                            ))}
                            {subjects.some((subject) => subject.schoolClassIds?.length) && (
                                <li>
                                    <strong>Предметы и где они есть</strong>
                                    <div className="badges">
                                        {subjects
                                            .filter((subject) => subject.schoolClassIds?.length)
                                            .slice(0, 4)
                                            .map((subject) => (
                                                <span key={subject.id} className="badge">
                                                    {subject.name}: {subject.schoolClassIds.map((id) => classLabel(id)).join(', ')}
                                                </span>
                                            ))}
                                    </div>
                                </li>
                            )}
                        </ul>
                    ) : <div className="empty">Нет данных по классам.</div>}
                </article>
            </div>
        </div>
    );
}