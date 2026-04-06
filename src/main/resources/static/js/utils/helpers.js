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