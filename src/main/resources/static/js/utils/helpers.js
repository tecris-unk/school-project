export function byId(items, id) {
    return items.find((item) => item.id === id);
}

export function fullName(person) {
    return person ? `${person.firstName} ${person.lastName}` : '—';
}

export function classLabel(item) {
    return item ? `${item.grade}${item.letter}` : '—';
}

export function average(values) {
    if (!values.length) return 0;
    return values.reduce((sum, value) => sum + value, 0) / values.length;
}

export function formatDate(value) {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat('ru-RU', { day: '2-digit', month: 'short', year: 'numeric' }).format(date);
}

export function initials(person) {
    if (!person) return '—';
    return `${person.firstName?.[0] || ''}${person.lastName?.[0] || ''}`.toUpperCase();
}