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

    const desktopLabels = visibleItems.map((item) => {
        const activeClass = item.key === active ? 'font-semibold text-slate-900' : 'text-slate-600';
        return `<span class="h-14 flex items-center px-4 whitespace-nowrap ${activeClass}">${item.label}</span>`;
    }).join('');

    return `
    <nav class="group relative">
      <div class="flex md:flex-col items-center gap-2">
      ${visibleItems.map((item) => {
        const activeClass = item.key === active ? 'bg-slate-900 text-white border-slate-900' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100 border-transparent';
        return `<a href="#/${item.key}" title="${item.label}" class="h-14 w-14 shrink-0 border flex items-center justify-center ${activeClass}">
            ${entityIcon(item.key)}
            <span class="sr-only">${item.label}</span>
          </a>`;
    }).join('')}
      </div>
      <div class="hidden md:flex absolute left-[calc(100%+12px)] top-0 flex-col border border-slate-200 bg-white opacity-0 pointer-events-none transition-opacity duration-150 group-hover:opacity-100 group-focus-within:opacity-100 z-20">
        ${desktopLabels}
      </div>
    </nav>
  `;
}