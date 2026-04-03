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
    const [studentFilters, setStudentFilters] = useState({ teacherEmail: '', subjectName: '', minScore: '' });
    const [gradeFilter, setGradeFilter] = useState('');
    const [message, setMessage] = useState({ type: '', text: '' });
    const [loading, setLoading] = useState(false);

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

    useEffect(() => { loadAll(); }, []);

    const referenceMaps = useMemo(() => ({
        classById: Object.fromEntries(data.classes.map((item) => [item.id, item])),
        teacherById: Object.fromEntries(data.teachers.map((item) => [item.id, item])),
        subjectById: Object.fromEntries(data.subjects.map((item) => [item.id, item])),
        studentById: Object.fromEntries(data.students.map((item) => [item.id, item])),
    }), [data]);

    const stats = [
        ['Ученики', data.students.length],
        ['Классы', data.classes.length],
        ['Учителя', data.teachers.length],
        ['Предметы', data.subjects.length],
        ['Оценки', data.grades.length],
    ];

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

    const searchStudents = async () => {
        try {
            const params = new URLSearchParams();
            if (studentFilters.teacherEmail.trim()) params.set('teacherEmail', studentFilters.teacherEmail.trim());
            if (studentFilters.subjectName.trim()) params.set('subjectName', studentFilters.subjectName.trim());
            if (studentFilters.minScore) params.set('minScore', studentFilters.minScore);
            const page = await api(`/api/students${params.toString() ? `?${params.toString()}` : ''}`);
            setData((current) => ({ ...current, students: page?.content || [] }));
            setMessage({ type: 'success', text: 'Фильтрация учеников выполнена.' });
        } catch (error) {
            setMessage({ type: 'error', text: error.message });
        }
    };

    const connectSubjectToClass = async () => {
        try {
            const { classId, subjectId } = forms.relation;
            await api(`/api/classes/${classId}/subjects/${subjectId}`, { method: 'PUT' });
            setMessage({ type: 'success', text: 'Связь Many-to-Many обновлена.' });
            setForms((current) => ({ ...current, relation: initialForms.relation }));
            await loadAll();
        } catch (error) {
            setMessage({ type: 'error', text: error.message });
        }
    };

    const filteredGrades = useMemo(() => (
        data.grades.filter((grade) => !gradeFilter || String(grade.studentId) === gradeFilter || String(grade.subjectId) === gradeFilter)
    ), [data.grades, gradeFilter]);

    return (
        <div className="container">
            <section className="hero">
                <div>
                    <h1>School SPA client</h1>
                    <p>
                        Единый React SPA для работы с REST API школы: CRUD-операции, фильтрация учеников,
                        отображение связей One-to-Many (ученики → оценки, учителя → предметы) и Many-to-Many
                        (классы ↔ предметы).
                    </p>
                </div>
                <div className="stats">
                    {stats.map(([label, value]) => <div className="stat-card" key={label}><span>{label}</span><strong>{value}</strong></div>)}
                </div>
                <div className="inline-actions">
                    <button onClick={loadAll} disabled={loading}>{loading ? 'Загрузка…' : 'Обновить данные'}</button>
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
                    <h2>Управление сущностями</h2>
                    <EntityForm
                        title="Ученик"
                        active={activeTab === 'students'}
                        editing={editing.entity === 'student'}
                        onSubmit={() => handleSubmit('student', '/api/students', (form) => ({ ...form, schoolClassId: form.schoolClassId || null }))}
                        onReset={() => resetEntityForm('student')}
                    >
                        <div className="form-grid two">
                            <label>Имя<input value={forms.student.firstName} onChange={(e) => updateForm('student', 'firstName', e.target.value)} /></label>
                            <label>Фамилия<input value={forms.student.lastName} onChange={(e) => updateForm('student', 'lastName', e.target.value)} /></label>
                            <label>Пол<select value={forms.student.gender} onChange={(e) => updateForm('student', 'gender', e.target.value)}><option>MALE</option><option>FEMALE</option></select></label>
                            <label>Email<input type="email" value={forms.student.email} onChange={(e) => updateForm('student', 'email', e.target.value)} /></label>
                            <label>Класс<select value={forms.student.schoolClassId} onChange={(e) => updateForm('student', 'schoolClassId', e.target.value)}><option value="">Без класса</option>{data.classes.map((item) => <option key={item.id} value={item.id}>{item.grade}{item.letter}</option>)}</select></label>
                        </div>
                    </EntityForm>

                    <EntityForm title="Класс" active={activeTab === 'classes'} editing={editing.entity === 'class'} onSubmit={() => handleSubmit('class', '/api/classes', (form) => ({ ...form, grade: Number(form.grade) }))} onReset={() => resetEntityForm('class')}>
                        <div className="form-grid two">
                            <label>Параллель<input type="number" min="1" max="11" value={forms.class.grade} onChange={(e) => updateForm('class', 'grade', e.target.value)} /></label>
                            <label>Буква<input value={forms.class.letter} onChange={(e) => updateForm('class', 'letter', e.target.value)} /></label>
                        </div>
                    </EntityForm>

                    <EntityForm title="Учитель" active={activeTab === 'teachers'} editing={editing.entity === 'teacher'} onSubmit={() => handleSubmit('teacher', '/api/teachers', (form) => form)} onReset={() => resetEntityForm('teacher')}>
                        <div className="form-grid two">
                            <label>Имя<input value={forms.teacher.firstName} onChange={(e) => updateForm('teacher', 'firstName', e.target.value)} /></label>
                            <label>Фамилия<input value={forms.teacher.lastName} onChange={(e) => updateForm('teacher', 'lastName', e.target.value)} /></label>
                            <label>Email<input type="email" value={forms.teacher.email} onChange={(e) => updateForm('teacher', 'email', e.target.value)} /></label>
                        </div>
                    </EntityForm>

                    <EntityForm title="Предмет" active={activeTab === 'subjects'} editing={editing.entity === 'subject'} onSubmit={() => handleSubmit('subject', '/api/subjects', (form) => ({ ...form, teacherId: form.teacherId || null }))} onReset={() => resetEntityForm('subject')}>
                        <div className="form-grid two">
                            <label>Название<input value={forms.subject.name} onChange={(e) => updateForm('subject', 'name', e.target.value)} /></label>
                            <label>Учитель<select value={forms.subject.teacherId} onChange={(e) => updateForm('subject', 'teacherId', e.target.value)}><option value="">Без учителя</option>{data.teachers.map((item) => <option key={item.id} value={item.id}>{item.firstName} {item.lastName}</option>)}</select></label>
                        </div>
                        <label>Описание<textarea value={forms.subject.description} onChange={(e) => updateForm('subject', 'description', e.target.value)} /></label>
                    </EntityForm>

                    <EntityForm title="Оценка" active={activeTab === 'grades'} editing={editing.entity === 'grade'} onSubmit={() => handleSubmit('grade', '/api/grades', (form) => ({ ...form, score: Number(form.score), studentId: Number(form.studentId), subjectId: Number(form.subjectId) }))} onReset={() => resetEntityForm('grade')}>
                        <div className="form-grid two">
                            <label>Балл<input type="number" min="2" max="10" value={forms.grade.score} onChange={(e) => updateForm('grade', 'score', e.target.value)} /></label>
                            <label>Дата<input type="date" value={forms.grade.date} onChange={(e) => updateForm('grade', 'date', e.target.value)} /></label>
                            <label>Ученик<select value={forms.grade.studentId} onChange={(e) => updateForm('grade', 'studentId', e.target.value)}>{data.students.map((item) => <option key={item.id} value={item.id}>{item.firstName} {item.lastName}</option>)}</select></label>
                            <label>Предмет<select value={forms.grade.subjectId} onChange={(e) => updateForm('grade', 'subjectId', e.target.value)}>{data.subjects.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
                        </div>
                    </EntityForm>

                    <div className="panel">
                        <h3>Связь Many-to-Many: класс ↔ предмет</h3>
                        <div className="form-grid two">
                            <label>Класс<select value={forms.relation.classId} onChange={(e) => updateForm('relation', 'classId', e.target.value)}><option value="">Выберите класс</option>{data.classes.map((item) => <option key={item.id} value={item.id}>{item.grade}{item.letter}</option>)}</select></label>
                            <label>Предмет<select value={forms.relation.subjectId} onChange={(e) => updateForm('relation', 'subjectId', e.target.value)}><option value="">Выберите предмет</option>{data.subjects.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
                        </div>
                        <div className="actions"><button onClick={connectSubjectToClass} disabled={!forms.relation.classId || !forms.relation.subjectId}>Добавить предмет в класс</button></div>
                    </div>
                </section>

                <section className="panel section-stack">
                    {activeTab === 'students' && (
                        <>
                            <h2>Ученики и связь One-to-Many с оценками</h2>
                            <div className="filters-grid two">
                                <label>Email учителя<input value={studentFilters.teacherEmail} onChange={(e) => setStudentFilters((current) => ({ ...current, teacherEmail: e.target.value }))} /></label>
                                <label>Предмет<input value={studentFilters.subjectName} onChange={(e) => setStudentFilters((current) => ({ ...current, subjectName: e.target.value }))} /></label>
                                <label>Минимальный балл<input type="number" min="0" value={studentFilters.minScore} onChange={(e) => setStudentFilters((current) => ({ ...current, minScore: e.target.value }))} /></label>
                            </div>
                            <div className="inline-actions">
                                <button onClick={searchStudents}>Фильтровать через API</button>
                                <button className="secondary" onClick={() => { setStudentFilters({ teacherEmail: '', subjectName: '', minScore: '' }); loadAll(); }}>Сбросить</button>
                            </div>
                            <StudentTable students={data.students} classLabel={classLabel} subjectLabel={subjectLabel} onEdit={(item) => startEdit('student', item)} onDelete={(id) => handleDelete(`/api/students/${id}`)} />
                        </>
                    )}

                    {activeTab === 'classes' && <ClassTable classes={data.classes} subjectLabel={subjectLabel} studentLabel={studentLabel} onEdit={(item) => startEdit('class', item)} onDelete={(id) => handleDelete(`/api/classes/${id}`)} />}
                    {activeTab === 'teachers' && <TeacherTable teachers={data.teachers} onEdit={(item) => startEdit('teacher', item)} onDelete={(id) => handleDelete(`/api/teachers/${id}`)} />}
                    {activeTab === 'subjects' && <SubjectTable subjects={data.subjects} teacherLabel={teacherLabel} classLabel={classLabel} onEdit={(item) => startEdit('subject', item)} onDelete={(id) => handleDelete(`/api/subjects/${id}`)} />}
                    {activeTab === 'grades' && (
                        <>
                            <h2>Оценки</h2>
                            <label>Быстрый фильтр по ID ученика или предмета<input value={gradeFilter} onChange={(e) => setGradeFilter(e.target.value)} placeholder="Например, 1" /></label>
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
        <div>
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
        <div className="table-wrap"><table><thead><tr><th>ID</th><th>Ученик</th><th>Класс</th><th>Email</th><th>Оценки</th><th></th></tr></thead><tbody>
        {students.map((student) => (
            <tr key={student.id}><td>{student.id}</td><td><strong>{student.firstName} {student.lastName}</strong><div className="muted">{student.gender}</div></td><td>{classLabel(student.schoolClassId)}</td><td>{student.email}</td><td>{student.grades?.length ? <div className="badges">{student.grades.map((grade) => <span key={grade.id} className="badge">{subjectLabel(grade.subjectId)}: {grade.score}</span>)}</div> : <span className="muted">Нет оценок</span>}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(student)}>Изменить</button><button className="danger" onClick={() => onDelete(student.id)}>Удалить</button></div></td></tr>
        ))}
        </tbody></table></div>
    );
}

function ClassTable({ classes, subjectLabel, studentLabel, onEdit, onDelete }) {
    if (!classes.length) return <div className="empty">Классы не найдены.</div>;
    return (
        <><h2>Классы и связь Many-to-Many с предметами</h2><div className="table-wrap"><table><thead><tr><th>ID</th><th>Класс</th><th>Ученики</th><th>Предметы</th><th></th></tr></thead><tbody>
        {classes.map((item) => <tr key={item.id}><td>{item.id}</td><td><strong>{item.grade}{item.letter}</strong></td><td>{item.studentIds?.length ? <div className="badges">{item.studentIds.map((id) => <span key={id} className="badge">{studentLabel(id)}</span>)}</div> : <span className="muted">Нет учеников</span>}</td><td>{item.subjectIds?.length ? <div className="badges">{item.subjectIds.map((id) => <span key={id} className="badge">{subjectLabel(id)}</span>)}</div> : <span className="muted">Нет предметов</span>}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div></>
    );
}

function TeacherTable({ teachers, onEdit, onDelete }) {
    if (!teachers.length) return <div className="empty">Учителя не найдены.</div>;
    return (
        <><h2>Учителя и предметы</h2><div className="table-wrap"><table><thead><tr><th>ID</th><th>Учитель</th><th>Email</th><th>Предметы</th><th></th></tr></thead><tbody>
        {teachers.map((item) => <tr key={item.id}><td>{item.id}</td><td><strong>{item.firstName} {item.lastName}</strong></td><td>{item.email}</td><td>{item.subjects?.length ? <div className="badges">{item.subjects.map((subject) => <span key={subject.id} className="badge">{subject.name}</span>)}</div> : <span className="muted">Нет предметов</span>}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div></>
    );
}

function SubjectTable({ subjects, teacherLabel, classLabel, onEdit, onDelete }) {
    if (!subjects.length) return <div className="empty">Предметы не найдены.</div>;
    return (
        <><h2>Предметы</h2><div className="table-wrap"><table><thead><tr><th>ID</th><th>Название</th><th>Учитель</th><th>Классы</th><th>Описание</th><th></th></tr></thead><tbody>
        {subjects.map((item) => <tr key={item.id}><td>{item.id}</td><td><strong>{item.name}</strong></td><td>{teacherLabel(item.teacherId)}</td><td>{item.schoolClassIds?.length ? <div className="badges">{item.schoolClassIds.map((id) => <span key={id} className="badge">{classLabel(id)}</span>)}</div> : <span className="muted">Не назначен</span>}</td><td>{item.description || '—'}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div></>
    );
}

function GradeTable({ grades, studentLabel, subjectLabel, onEdit, onDelete }) {
    if (!grades.length) return <div className="empty">Оценки не найдены.</div>;
    return (
        <div className="table-wrap"><table><thead><tr><th>ID</th><th>Балл</th><th>Дата</th><th>Ученик</th><th>Предмет</th><th></th></tr></thead><tbody>
        {grades.map((item) => <tr key={item.id}><td>{item.id}</td><td>{item.score}</td><td>{item.date}</td><td>{studentLabel(item.studentId)}</td><td>{subjectLabel(item.subjectId)}</td><td><div className="inline-actions"><button className="secondary" onClick={() => onEdit(item)}>Изменить</button><button className="danger" onClick={() => onDelete(item.id)}>Удалить</button></div></td></tr>)}
        </tbody></table></div>
    );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);