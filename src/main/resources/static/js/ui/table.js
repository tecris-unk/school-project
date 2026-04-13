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
    return Array.from({length: 6}, (_, rowIndex) => `<tr class="border-b border-slate-100">${Array.from({length: cols}, () => `<td class="px-4 py-3"><div class="h-4 rounded bg-slate-200/80 animate-pulse ${rowIndex % 2 === 0 ? 'w-full' : 'w-4/5'}"></div></td>`).join('')}</tr>`).join('');
}

export function renderTable({ entity, config, rows, refs, data, ui, meta, pageableFromApi, canEdit = true, canDelete = true, loading = false }) {
    const hasRows = rows.length > 0;
    const columnCount = config.columns.length + ((canEdit || canDelete) ? 1 : 0) + (canDelete ? 1 : 0);
    const headers = config.columns.map((col) => {
        const active = ui.sort.key === col.key;
        const arrow = active ? (ui.sort.dir === 'asc' ? '↑' : '↓') : '';
        return `<th data-sort="${col.key}" class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 cursor-pointer hover:text-slate-900">${col.label} ${arrow}</th>`;
    }).join('');

    let body = '';
    if (loading) {
        body = renderSkeleton(columnCount);
    } else if (!hasRows) {
        body = `<tr><td colspan="${columnCount}" class="px-4 py-10 text-center"><p class="text-sm text-slate-500">Нет данных по текущим фильтрам</p></td></tr>`;
    } else {
        body = rows.map((row, index) => {
            const isSelected = ui.selectedIds.has(row.id);
            const cells = config.columns.map((column) => `<td class="px-4 py-3 text-sm text-slate-700">${renderCell(entity, column, row, refs, data)}</td>`).join('');
            return `<tr class="border-b border-slate-100 hover:bg-slate-50 ${index % 2 ? 'bg-slate-50/40' : ''}">
              ${canDelete ? `<td class="px-4 py-3"><input data-select-id="${row.id}" type="checkbox" ${isSelected ? 'checked' : ''} /></td>` : ''}
              ${cells}
              ${(canEdit || canDelete) ? `<td class="px-4 py-3"><div class="flex justify-end gap-2">${canEdit && config.linkAction ? `<button data-link-id="${row.id}" class="btn-secondary text-xs">${config.linkAction.label}</button>` : ''}${canEdit ? `<button data-edit-id="${row.id}" class="btn-secondary text-xs">Редактировать</button>` : ''}${canDelete ? `<button data-delete-id="${row.id}" class="btn-danger text-xs">Удалить</button>` : ''}</div></td>` : ''}
            </tr>`;
        }).join('');
    }

    const pages = pageableFromApi ? meta.totalPages : Math.max(1, Math.ceil((meta.totalElements || 0) / meta.size));

    return `
    <section class="card-base overflow-hidden">
      <div class="flex flex-wrap items-center gap-3 p-4 border-b border-slate-200">
        <input id="search-input" value="${ui.search || ''}" placeholder="Поиск" class="input-base w-full md:max-w-sm" />
        ${config.filters.map((filter) => `<select data-filter-key="${filter.key}" class="input-base w-full md:w-auto min-w-[220px]"><option value="">${filter.label}: все</option>${filter.options(refs, data).map((opt) => `<option ${String(ui.filters[filter.key] || '') === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`).join('')}</select>`).join('')}
        <div class="ml-auto"></div>
        ${canDelete ? `<button id="bulk-delete" class="btn-danger">Удалить выбранные (${ui.selectedIds.size})</button>` : ''}
      </div>
     <div>
        <table class="w-full table-fixed">
          <thead class="bg-white sticky top-0 z-10 border-b border-slate-200">
            <tr>
              ${canDelete ? `<th class="px-4 py-3 text-left"><input id="select-all" type="checkbox" ${hasRows && rows.every((r) => ui.selectedIds.has(r.id)) ? 'checked' : ''}/></th>` : ''}
              ${headers}
              ${(canEdit || canDelete) ? '<th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">Действия</th>' : ''}
            </tr>
          </thead>
          <tbody>${body}</tbody>
        </table>
      </div>
      <div class="flex items-center justify-between p-4 border-t border-slate-200 text-sm text-slate-600">
        <span>Всего: ${meta.totalElements || 0}</span>
        <div class="flex items-center gap-2">
          <button data-page-action="prev" class="btn-secondary" ${meta.page <= 0 ? 'disabled' : ''}>Назад</button>
          <span>Стр. ${meta.page + 1} / ${pages}</span>
          <button data-page-action="next" class="btn-secondary" ${meta.page + 1 >= pages ? 'disabled' : ''}>Вперёд</button>
        </div>
      </div>
    </section>`;
}