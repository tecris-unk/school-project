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
    <nav class="flex md:flex-col items-center md:items-stretch gap-2">
      ${visibleItems.map((item) => {
        const activeClass = item.key === active ? 'bg-slate-900 text-white border-slate-900' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100 border-transparent';
        const activeLabelClass = item.key === active ? 'text-white' : 'text-slate-700';
        return `<a href="#/${item.key}" title="${item.label}" class="h-14 w-14 md:w-full shrink-0 border flex items-center justify-center md:justify-start md:px-4 gap-3 ${activeClass}">
            <span class="shrink-0">${entityIcon(item.key)}</span>
            <span class="sr-only">${item.label}</span>
            <span class="hidden md:block max-w-0 opacity-0 overflow-hidden whitespace-nowrap transition-all duration-200 group-hover:max-w-[170px] group-hover:opacity-100 group-focus-within:max-w-[170px] group-focus-within:opacity-100 ${activeLabelClass}">
              ${item.label}
            </span>
          </a>`;
    }).join('')}
    </nav>
  `;
}