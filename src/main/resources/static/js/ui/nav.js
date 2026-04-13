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
    <nav class="flex md:flex-col items-center gap-2 overflow-x-auto">
      ${visibleItems.map((item) => {
        const activeClass = item.key === active ? 'bg-slate-900 text-white border-slate-900' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100 border-transparent';
        return `
          <a href="#/${item.key}" title="${item.label}" class="group relative h-14 w-14 shrink-0 border flex items-center justify-center ${activeClass}">
            ${entityIcon(item.key)}
            <span class="sr-only">${item.label}</span>
            <span class="hidden md:block absolute left-[calc(100%+10px)] top-1/2 -translate-y-1/2 whitespace-nowrap bg-slate-900 px-3 py-2 text-sm text-white opacity-0 pointer-events-none transition-opacity duration-150 group-hover:opacity-100 group-focus-visible:opacity-100 z-20">
              ${item.label}
            </span>
          </a>`;
    }).join('')}
    </nav>
  `;
}