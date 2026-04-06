export const navItems = [
    { key: 'dashboard', label: 'Панель' },
    { key: 'students', label: 'Ученики' },
    { key: 'classes', label: 'Классы' },
    { key: 'subjects', label: 'Предметы' },
    { key: 'teachers', label: 'Учителя' },
    { key: 'grades', label: 'Оценки' },
];

export function renderNav(active, role = 'admin') {
    const visibleItems = navItems.filter((item) => role === 'admin' || ['dashboard', 'grades', 'students'].includes(item.key));

    return `
    <nav class="bg-white border border-slate-200 shadow-sm rounded-xl p-2 mb-6 flex flex-wrap gap-2">
      ${visibleItems
        .map(
            (item) => `<a href="#/${item.key}" class="px-4 py-2 rounded-lg text-sm font-medium transition ${
                item.key === active ? 'bg-slate-900 text-white shadow' : 'text-slate-700 hover:bg-slate-100'
            }">${item.label}</a>`,
        )
        .join('')}
      <div class="ml-auto"></div>
      <a href="#/login" data-logout class="px-4 py-2 rounded-lg text-sm font-medium text-rose-600 hover:bg-rose-50">Выйти</a>
    </nav>
  `;
}