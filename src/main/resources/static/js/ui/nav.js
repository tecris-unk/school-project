import {entityIcon} from './entityMeta.js';

export const navItems = [
    { key: 'dashboard', label: 'Панель' },
    { key: 'students', label: 'Ученики' },
    { key: 'classes', label: 'Классы' },
    { key: 'subjects', label: 'Предметы' },
    { key: 'teachers', label: 'Учителя' },
    { key: 'grades', label: 'Оценки' },
];

export function renderNav(active, role = 'admin') {
    const teacherRoutes = ['dashboard', 'classes', 'grades'];
    const visibleItems = navItems.filter((item) => role === 'admin' || teacherRoutes.includes(item.key));

    return `
    <nav class="space-y-1.5">
      ${visibleItems.map((item) => {
        const activeClass = item.key === active
            ? 'bg-slate-900 text-white shadow-sm ring-1 ring-slate-900/10'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100';
        return `<a
            href="#/${item.key}"
            title="${item.label}"
            class="group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all ${activeClass}"
          >
            <span class="shrink-0">${entityIcon(item.key)}</span>
            <span>${item.label}</span>
          </a>`;
    }).join('')}
    </nav>
  `;
}