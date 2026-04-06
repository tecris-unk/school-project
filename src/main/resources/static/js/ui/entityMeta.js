const ENTITY_ICON_MAP = {
    dashboard: '/assets/entity-icons/dashboard.png',
    students: '/assets/entity-icons/students.png',
    classes: '/assets/entity-icons/classes.png',
    subjects: '/assets/entity-icons/subjects.png',
    teachers: '/assets/entity-icons/teachers.png',
    grades: '/assets/entity-icons/grades.jpg',
};

export function entityIcon(entityKey) {
    return ENTITY_ICON_MAP[entityKey] || '/favicon.ico';
}

export function entityLabelWithIcon(entityKey, label) {
    const icon = entityIcon(entityKey);
    return `
      <span class="entity-label-with-icon">
        <img
          src="${icon}"
          alt="${label}"
          class="entity-inline-icon"
          onerror="this.onerror=null;this.src='/favicon.ico';"
        />
        <span>${label}</span>
      </span>
    `;
}