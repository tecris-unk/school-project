/* global React, ReactDOM, antd */
const {useEffect, useMemo, useState} = React;
const {
    Layout,
    Typography,
    Tabs,
    Card,
    Table,
    Form,
    Input,
    Select,
    Button,
    Space,
    Popconfirm,
    message,
    Spin,
    Alert,
} = antd;

const {Header, Content, Footer} = Layout;
const {Title, Text} = Typography;

const entityConfigs = {
    students: {
        title: 'Ученики',
        endpoint: '/api/students',
        listParser: (payload) => payload?.content || [],
        fields: [
            {key: 'firstName', label: 'Имя', type: 'input', required: true},
            {key: 'lastName', label: 'Фамилия', type: 'input', required: true},
            {
                key: 'gender',
                label: 'Пол',
                type: 'select',
                options: [
                    {label: 'Мужской', value: 'MALE'},
                    {label: 'Женский', value: 'FEMALE'},
                ],
                required: true,
            },
            {key: 'email', label: 'Email', type: 'input', required: true},
            {key: 'schoolClassId', label: 'Класс', type: 'ref', ref: 'classes', allowEmpty: true},
        ],
        payload: (v) => ({
            firstName: v.firstName,
            lastName: v.lastName,
            gender: v.gender,
            email: v.email,
            schoolClassId: v.schoolClassId ? Number(v.schoolClassId) : null,
        }),
    }, classes: {
        title: 'Классы',
        endpoint: '/api/classes',
        listParser: (payload) => payload || [],
        fields: [
            {key: 'grade', label: 'Параллель', type: 'input', required: true},
            {key: 'letter', label: 'Буква', type: 'input', required: true},
        ],
        payload: (v) => ({grade: Number(v.grade), letter: v.letter}),
    }, subjects: {
        title: 'Предметы',
        endpoint: '/api/subjects',
        listParser: (payload) => payload || [],
        fields: [
            {key: 'name', label: 'Название', type: 'input', required: true},
            {key: 'description', label: 'Описание', type: 'textarea'},
            {key: 'teacherId', label: 'Учитель', type: 'ref', ref: 'teachers', allowEmpty: true},
        ],
        payload: (v) => ({
            name: v.name,
            description: v.description,
            teacherId: v.teacherId ? Number(v.teacherId) : null
        }),
    },
    teachers: {
        title: 'Учителя',
        endpoint: '/api/teachers',
        listParser: (payload) => payload || [],
        fields: [
            {key: 'firstName', label: 'Имя', type: 'input', required: true},
            {key: 'lastName', label: 'Фамилия', type: 'input', required: true},
            {key: 'email', label: 'Email', type: 'input', required: true},
        ],
        payload: (v) => ({firstName: v.firstName, lastName: v.lastName, email: v.email}),
    }, grades: {
        title: 'Оценки',
        endpoint: '/api/grades',
        listParser: (payload) => payload || [],
        fields: [
            {key: 'score', label: 'Оценка (2-10)', type: 'input', required: true},
            {key: 'date', label: 'Дата', type: 'date', required: true},
            {key: 'studentId', label: 'Ученик', type: 'ref', ref: 'students', required: true},
            {key: 'subjectId', label: 'Предмет', type: 'ref', ref: 'subjects', required: true},
        ],
        payload: (v) => ({
            score: Number(v.score),
            date: v.date,
            studentId: Number(v.studentId),
            subjectId: Number(v.subjectId)
        }),
    },
};

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: {'Content-Type': 'application/json', ...(options.headers || {})}, ...options,
    });

    if (response.status === 204) return null;

    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw new Error(payload?.message || payload?.error || `Ошибка ${response.status}`);
    }

    return payload;
}

function App() {
    const [active, setActive] = useState('students');
    const [data, setData] = useState({students: [], classes: [], subjects: [], teachers: [], grades: []});
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [error, setError] = useState('');
    const [form] = Form.useForm();

    const labelHelpers = useMemo(() => {
        const classMap = new Map(data.classes.map((item) => [item.id, `${item.grade}${item.letter}`]));
        const teacherMap = new Map(data.teachers.map((item) => [item.id, `${item.firstName} ${item.lastName}`]));
        const studentMap = new Map(data.students.map((item) => [item.id, `${item.firstName} ${item.lastName}`]));
        const subjectMap = new Map(data.subjects.map((item) => [item.id, item.name]));

        return {
            className: (id) => classMap.get(id) || '—',
            teacherName: (id) => teacherMap.get(id) || '—',
            studentName: (id) => studentMap.get(id) || '—',
            subjectName: (id) => subjectMap.get(id) || '—',
        };
    }, [data]);

    async function reload() {
        setLoading(true);
        setError('');
        try {
            const [students, classes, subjects, teachers, grades] = await Promise.all([
                api('/api/students'),
                api('/api/classes/with-subjects'),
                api('/api/subjects'),
                api('/api/teachers'),
                api('/api/grades'),
            ]);

            setData({
                students: students?.content || [],
                classes: classes || [],
                subjects: subjects || [],
                teachers: teachers || [],
                grades: grades || [],
            });
        } catch (e) {
            setError(`Не удалось загрузить данные: ${e.message}`);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        void reload();
    }, []);

    useEffect(() => {
        setEditingId(null);
        form.resetFields();
        if (active === 'students') {
            form.setFieldsValue({gender: 'MALE'});
        }
    }, [active, form]);

    const config = entityConfigs[active];
    const list = data[active] || [];

    const refLabel = (ref, item) => {
        if (ref === 'classes') return `${item.grade}${item.letter}`;
        if (ref === 'teachers') return `${item.firstName} ${item.lastName}`;
        if (ref === 'students') return `${item.firstName} ${item.lastName}`;
        if (ref === 'subjects') return item.name;
        return String(item.id);
    };

    const getColumns = () => {
        const actions = {
            title: 'Действия',
            key: 'actions',
            render: (_, row) => (
                <div className="actions-cell">
                    <Button size="small" onClick={() => startEdit(row)}>Изменить</Button>
                    <Popconfirm title="Удалить запись?" okText="Да" cancelText="Нет"
                                onConfirm={() => void removeRow(row.id)}>
                        <Button danger size="small">Удалить</Button>
                    </Popconfirm>
                </div>
            ),
        };

        if (active === 'students') {
            return [
                {title: 'Имя', dataIndex: 'firstName'},
                {title: 'Фамилия', dataIndex: 'lastName'},
                {title: 'Пол', render: (_, r) => (r.gender === 'MALE' ? 'Мужской' : 'Женский')},
                {title: 'Email', dataIndex: 'email'},
                {title: 'Класс', render: (_, r) => labelHelpers.className(r.schoolClassId)},
                actions,
            ];
        }
        if (active === 'classes') {
            return [
                {title: 'Класс', render: (_, r) => `${r.grade}${r.letter}`},
                {title: 'Учеников', render: (_, r) => (r.studentIds || []).length},
                {title: 'Предметов', render: (_, r) => (r.subjectIds || []).length},
                actions,
            ];
        }
        if (active === 'subjects') {
            return [
                {title: 'Название', dataIndex: 'name'},
                {title: 'Описание', render: (_, r) => r.description || '—'},
                {title: 'Учитель', render: (_, r) => labelHelpers.teacherName(r.teacherId)},
                actions,
            ];
        }
        if (active === 'teachers') {
            return [
                {title: 'Имя', dataIndex: 'firstName'},
                {title: 'Фамилия', dataIndex: 'lastName'},
                {title: 'Email', dataIndex: 'email'},
                {title: 'Предметов', render: (_, r) => (r.subjects || []).length},
                actions,
            ];
        }
        return [
            {title: 'Оценка', dataIndex: 'score'},
            {title: 'Дата', dataIndex: 'date'},
            {title: 'Ученик', render: (_, r) => labelHelpers.studentName(r.studentId)},
            {title: 'Предмет', render: (_, r) => labelHelpers.subjectName(r.subjectId)},
            actions,
        ];
    };

    const startEdit = (row) => {
        setEditingId(row.id);
        form.setFieldsValue({
            ...row,
            schoolClassId: row.schoolClassId || undefined,
            teacherId: row.teacherId || undefined
        });
    };

    const removeRow = async (id) => {
        try {
            await api(`${config.endpoint}/${id}`, {method: 'DELETE'});
            message.success('Запись удалена');
            if (editingId === id) {
                setEditingId(null);
                form.resetFields();
            }

            await reload();
        } catch (e) {
            message.error(e.message);
        }
    };
    const onSubmit = async (values) => {
        setSaving(true);
        try {
            const payload = config.payload(values);
            const method = editingId ? 'PUT' : 'POST';
            const url = editingId ? `${config.endpoint}/${editingId}` : config.endpoint;
            await api(url, {method, body: JSON.stringify(payload)});
            message.success(editingId ? 'Запись обновлена' : 'Запись создана');
            setEditingId(null);
            form.resetFields();
            if (active === 'students') {
                form.setFieldsValue({gender: 'MALE'});
            }
            await reload();
        } catch (e) {
            message.error(e.message);
        } finally {
            setSaving(false);
        }
    };
    const renderField = (field) => {
        if (field.type === 'textarea') {
            return (
                <Form.Item key={field.key} name={field.key} label={field.label}
                           rules={[{required: !!field.required, message: 'Заполните поле'}]}>
                    <Input.TextArea rows={4}/>
                </Form.Item>
            );
        }

        if (field.type === 'select') {
            return (
                <Form.Item key={field.key} name={field.key} label={field.label}
                           rules={[{required: !!field.required, message: 'Выберите значение'}]}>
                    <Select options={field.options}/>
                </Form.Item>
            );
        }


        if (field.type === 'ref') {
            const options = (data[field.ref] || []).map((item) => ({value: item.id, label: refLabel(field.ref, item)}));
            return (
                <Form.Item key={field.key} name={field.key} label={field.label}
                           rules={[{required: !!field.required, message: 'Выберите значение'}]}>
                    <Select allowClear={!!field.allowEmpty} options={options} placeholder="Выберите"/>
                </Form.Item>
            );
        }

        if (field.type === 'date') {
            return (
                <Form.Item key={field.key} name={field.key} label={field.label}
                           rules={[{required: !!field.required, message: 'Укажите дату'}]}>
                    <Input type="date"/>
                </Form.Item>
            );
        }

        return (
            <Form.Item key={field.key} name={field.key} label={field.label}
                       rules={[{required: !!field.required, message: 'Заполните поле'}]}>
                <Input/>
            </Form.Item>
        );
    };

    const tabs = Object.entries(entityConfigs).map(([key, item]) => ({key, label: item.title}));

    return (
        <Layout className="school-layout">
            <Header className="school-header">
                <Title level={1}>Школьный портал</Title>
                <Text>Сделано по образцу calorie-counter: чистый интерфейс, вкладки и удобные CRUD-формы.</Text>
            </Header>

            <Content className="school-content">
                {error && <Alert type="error" message={error} showIcon style={{marginBottom: 12}}/>}

                <Tabs className="portal-tabs" items={tabs} activeKey={active} onChange={setActive}/>

                <div className="entity-grid">
                    <Card title={`${config.title}: список`}>
                        {loading ? (
                            <Spin/>
                        ) : (
                            <Table
                                rowKey="id"
                                dataSource={list}
                                columns={getColumns()}
                                pagination={{pageSize: 7}}
                                scroll={{x: 600}}
                            />
                        )}
                    </Card>

                    <Card title={`${config.title}: ${editingId ? 'изменить' : 'создать'}`}>
                        <Form form={form} layout="vertical" onFinish={onSubmit}>
                            {config.fields.map((field) => renderField(field))}

                            <Space>
                                <Button type="primary" htmlType="submit" loading={saving}>
                                    {editingId ? 'Сохранить' : 'Создать'}
                                </Button>
                                <Button
                                    onClick={() => {
                                        setEditingId(null);
                                        form.resetFields();
                                        if (active === 'students') {
                                            form.setFieldsValue({gender: 'MALE'});
                                        }
                                    }}
                                >
                                    Очистить
                                </Button>
                            </Space>
                        </Form>
                    </Card>
                </div>
            </Content>

            <Footer className="contact-footer">
                <h3>Контакты школы</h3>
                <p><strong>Адрес:</strong> г. Витебск, ул. Школьная, д. 12</p>
                <p><strong>Телефон:</strong> +375 (29) 000-00-00</p>
                <p><strong>Email:</strong> school12@example.by</p>
                <p><strong>Время работы:</strong> пн–пт, 08:00–18:00</p>
            </Footer>
        </Layout>
    );
}

    const root = ReactDOM.createRoot(document.getElementById('root'));
    root.render(<App />);