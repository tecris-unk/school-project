const ICONS = {
    dashboard: '<path d="M3 10.5 12 3l9 7.5"/><path d="M5.25 9.75V20h13.5V9.75"/>',
    students: '<path d="M16 20v-1a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v1"/><circle cx="9.5" cy="7" r="3"/><path d="M22 20v-1a4 4 0 0 0-3-3.87"/><path d="M16 4.13a4 4 0 0 1 0 7.75"/>',
    classes: '<rect x="4" y="5" width="16" height="14" rx="2"/><path d="M4 10h16"/><path d="M9 5v14"/>',
    subjects: '<path d="M4 19.5V6.75A2.75 2.75 0 0 1 6.75 4H20"/><path d="M8 20h11"/><path d="M8 4v16"/><path d="M12 8h6"/><path d="M12 12h6"/>',
    teachers: '<circle cx="12" cy="7" r="4"/><path d="M5.5 20a6.5 6.5 0 0 1 13 0"/><path d="M19.5 11.5 22 14l-2.5 2.5"/><path d="M22 14h-4"/>',
    grades: '<path d="M4 19h16"/><path d="M7 16V9"/><path d="M12 16V5"/><path d="M17 16v-4"/>',
};

export function entityIcon(entityKey) {
    const paths = ICONS[entityKey] || ICONS.dashboard;
    return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" class="entity-inline-icon" aria-hidden="true">${paths}</svg>`;
}

export function entityLabelWithIcon(entityKey, label) {
    return `
      <span class="entity-label-with-icon">
         ${entityIcon(entityKey)}
        <span>${label}</span>
      </span>
    `;
}