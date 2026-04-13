import { byId, classLabel, fullName } from '../utils/helpers.js';

function renderCell(entity, column, row, refs, data) {
    const value = row[column.key];
    if (column.render) return column.render(row, refs, data);
    if (column.key === 'schoolClassId') return classLabel(byId(refs.classes, row.schoolClassId));
    if (column.key === 'teacherId') return fullName(byId(refs.teachers, row.teacherId));
    if (column.key === 'studentId') return fullName(byId(data.students, row.studentId));
    if (column.key === 'subjectId') return byId(refs.subjects, row.subjectId)?.name || '—';
    if (column.key === 'gender') return value === 'MALE' ? 'Мужской' : 'Женский';
    return value ?? '—';
}

export function applyQuery(items, config, ui) {
    let result = [...items];

    if (ui.search) {
        const needle = ui.search.toLowerCase();
        result = result.filter((item) =>
            config.columns.some((col) => String(item[col.key] ?? '').toLowerCase().includes(needle)),
        );
    }

    Object.entries(ui.filters || {}).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== '') {
            result = result.filter((item) => String(item[key] ?? '') === String(value));
        }
    });

    if (ui.sort?.key) {
        const { key, dir } = ui.sort;
        result.sort((a, b) => {
            const left = a[key] ?? '';
            const right = b[key] ?? '';
            const cmp = String(left).localeCompare(String(right), 'ru', { numeric: true });
            return dir === 'asc' ? cmp : -cmp;
        });
    }

    return result;
}

export function paginate(items, page = 0, size = 10) {
    const start = page * size;
    return items.slice(start, start + size);
}

function renderSkeleton(cols) {
    return Array.from({length: 6}, (_, rowIndex) => `<tr class="border-b border-slate-100">${Array.from({length: cols}, () => `<td class="px-6 py-4"><div class="h-4 rounded bg-slate-200/80 animate-pulse ${rowIndex % 2 === 0 ? 'w-full' : 'w-4/5'}"></div></td>`).join('')}</tr>`).join('');
}

function emptyState(config, canCreate) {
    return `
    <div class="flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-6 py-14 text-center">
      <div class="mb-4 inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-white text-slate-500 shadow-sm ring-1 ring-slate-200">
        <span class="text-2xl">📄</span>
      </div>
      <h3 class="text-base font-semibold text-slate-900">Пока нет записей</h3>
      <p class="mt-1 max-w-sm text-sm text-slate-500">Нет данных по текущим фильтрам. Измените условия поиска или создайте новую запись.</p>
      ${canCreate ? `<button data-create-entity class="btn-primary mt-5">${config.title === 'Оценки' ? 'Создать запись' : 'Создать'}</button>` : ''}
    </div>`;
}

export function renderTable({ entity, config, rows, refs, data, ui, meta, pageableFromApi, canEdit = true, canDelete = true, loading = false }) {
    const hasRows = rows.length > 0;
    const columnCount = config.columns.length + ((canEdit || canDelete) ? 1 : 0) + (canDelete ? 1 : 0);
    const headers = config.columns.map((col) => {
        const active = ui.sort.key === col.key;
        const arrow = active ? (ui.sort.dir === 'asc' ? '↑' : '↓') : '↕';
        return `<th data-sort="${col.key}" class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 cursor-pointer hover:text-slate-900">${col.label} <span class="text-slate-400">${arrow}</span></th>`;
    }).join('');

    let body = '';
    if (loading) {
        body = renderSkeleton(columnCount);
    } else {
        body = rows.map((row, index) => {
            const isSelected = ui.selectedIds.has(row.id);
            const cells = config.columns.map((column) => `<td class="px-6 py-4 text-sm text-slate-700">${renderCell(entity, column, row, refs, data)}</td>`).join('');
            return `<tr class="border-b border-slate-100 hover:bg-indigo-50/40 transition-colors ${index % 2 ? 'bg-slate-50/20' : ''}">
                ${canDelete ? `<td class="px-6 py-4"><input data-select-id="${row.id}" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" ${isSelected ? 'checked' : ''} /></td>` : ''}
            ${cells}
            ${(canEdit || canDelete) ? `<td class="px-6 py-4"><div class="flex flex-wrap justify-end gap-2">${canEdit && config.linkAction ? `<button data-link-id="${row.id}" class="btn-secondary text-xs">Связать</button>` : ''}${canEdit ? `<button data-edit-id="${row.id}" class="btn-secondary text-xs">Редактировать</button>` : ''}${canDelete ? `<button data-delete-id="${row.id}" class="btn-danger text-xs">Удалить</button>` : ''}</div></td>` : ''}
        </tr>`;
        }).join('');
    }

    const pages = pageableFromApi ? meta.totalPages : Math.max(1, Math.ceil((meta.totalElements || 0) / meta.size));

    return `
                <section class="card-base overflow-hidden">
                <div class="border-b border-slate-200 bg-white px-5 py-5 sm:px-6">
                <div class="grid gap-3 lg:grid-cols-[minmax(0,1fr)_auto_auto] lg:items-center">
                <label class="relative block">
                <span class="pointer-events-none absolute inset-y-0 left-3 inline-flex items-center text-slate-400">⌕</span>
            <input id="search-input" value="${ui.search || ''}" placeholder="Поиск по таблице" class="input-base pl-9" />
        </label>
            <div class="flex flex-col gap-3 sm:flex-row sm:flex-wrap">
                ${config.filters.map((filter) => `<select data-filter-key="${filter.key}" class="input-base min-w-[220px]"><option value="">${filter.label}: все</option>${filter.options(refs, data).map((opt) => `<option ${String(ui.filters[filter.key] || '') === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`).join('')}</select>`).join('')}
            </div>
            <div class="lg:justify-self-end">
                ${canDelete ? `<button id="bulk-delete" class="btn-danger w-full sm:w-auto" ${ui.selectedIds.size ? '' : 'disabled'}>Удалить выбранные (${ui.selectedIds.size})</button>` : ''}
            </div>
        </div>
        </div>
                ${!hasRows && !loading ? `<div class="px-5 py-6 sm:px-6">${emptyState(config, canEdit)}</div>` : `
      <div class="overflow-x-auto">
        <table class="min-w-full table-auto">
          <thead class="border-b border-slate-200 bg-slate-50">
            <tr>
              ${canDelete ? `<th class="px-6 py-4 text-left"><input id="select-all" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" ${hasRows && rows.every((r) => ui.selectedIds.has(r.id)) ? 'checked' : ''}/></th>` : ''}
              ${headers}
              ${(canEdit || canDelete) ? '<th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">Действия</th>' : ''}
            </tr>
          </thead>
          <tbody>${body}</tbody>
        </table>
      </div>`}
                <div class="flex flex-col gap-3 border-t border-slate-200 bg-white px-5 py-4 text-sm text-slate-600 sm:flex-row sm:items-center sm:justify-between sm:px-6">
                    <span>Всего: ${meta.totalElements || 0}</span>
                    <div class="flex items-center gap-2">
                        <button data-page-action="prev" class="btn-secondary" ${meta.page <= 0 ? 'disabled' : ''}>Назад</button>
                        <span class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5">Стр. ${meta.page + 1} / ${pages}</span>
                        <button data-page-action="next" class="btn-secondary" ${meta.page + 1 >= pages ? 'disabled' : ''}>Вперёд</button>
                    </div>
                </div>
            </section>`;
}