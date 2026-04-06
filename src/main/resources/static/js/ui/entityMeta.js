const ENTITY_ICON_MAP = {
    dashboard: '📊',
    students: '🧑‍🎓',
    classes: '🏫',
    subjects: '📘',
    teachers: '🧑‍🏫',
    grades: '📝',
};

export function entityIcon(entityKey) {
    return ENTITY_ICON_MAP[entityKey] || '📌';
}

export function entityLabelWithIcon(entityKey, label) {
    return `${entityIcon(entityKey)} ${label}`;
}