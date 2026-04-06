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

export function renderTable({ entity, config, rows, refs, data, ui, meta, pageableFromApi, canManage = true }) {
    const hasRows = rows.length > 0;
    const headers = config.columns
        .map((col) => {
            const active = ui.sort.key === col.key;
            const arrow = active ? (ui.sort.dir === 'asc' ? '↑' : '↓') : '';
            return `<th data-sort="${col.key}" class="px-3 py-3 text-left text-xs uppercase tracking-wider text-slate-500 cursor-pointer hover:text-slate-900">${col.label} ${arrow}</th>`;
        })
        .join('');

    const body = hasRows
        ? rows
            .map((row) => {
                const isSelected = ui.selectedIds.has(row.id);
                const cells = config.columns
                    .map((column) => {
                        const editable = config.inlineEditable?.includes(column.key);
                        const editing = ui.editingId === row.id && editable;
                        const value = row[column.key] ?? '';
                        return `<td class="px-3 py-3 text-sm text-slate-700">${
                            editing
                                ? `<input data-inline-input="${column.key}" value="${value}" class="w-full rounded border-slate-300 text-sm"/>`
                                : renderCell(entity, column, row, refs, data)
                        }</td>`;
                    })
                    .join('');

                return `
            <tr class="border-t border-slate-100 hover:bg-slate-50">
             ${canManage ? `<td class="px-3 py-3"><input data-select-id="${row.id}" type="checkbox" ${isSelected ? 'checked' : ''} /></td>` : ''}
              ${cells}
              ${
                    canManage
                        ? `<td class="px-3 py-3 text-right whitespace-nowrap">
                       <button data-edit-id="${row.id}" class="px-3 py-1 rounded border border-slate-300 hover:bg-slate-100 text-sm">${ui.editingId === row.id ? 'Сохранить' : 'Ред.'}</button>
                       <button data-delete-id="${row.id}" class="ml-2 px-3 py-1 rounded bg-rose-600 text-white hover:bg-rose-700 text-sm">Удалить</button>
                     </td>`
                        : ''
                }
            </tr>
          `;
            })
            .join('')
        : `<tr><td colspan="${config.columns.length + (canManage ? 2 : 0)}" class="p-6 text-center text-slate-500">Нет данных</td></tr>`;

    const pages = pageableFromApi ? meta.totalPages : Math.max(1, Math.ceil((meta.totalElements || 0) / meta.size));

    return `
    <div class="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
      <div class="flex flex-wrap items-center gap-3 p-4 border-b border-slate-100">
       <input id="search-input" value="${ui.search || ''}" placeholder="Поиск..." class="w-full max-w-xs md:max-w-sm lg:max-w-md rounded-lg border-slate-300 text-sm" />
        ${config.filters
        .map(
            (filter) => `<select data-filter-key="${filter.key}" class="w-full md:w-auto min-w-[220px] rounded-lg border-slate-300 text-sm">
                <option value="">${filter.label}: все</option>
                ${filter.options(refs, data)
                .map((opt) => `<option ${String(ui.filters[filter.key] || '') === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`)
                .join('')}
              </select>`,
        )
        .join('')}
        <button id="export-csv" class="ml-auto px-3 py-2 rounded-lg border border-slate-300 text-sm hover:bg-slate-100">CSV экспорт</button>
        ${canManage ? `<button id="bulk-delete" class="px-3 py-2 rounded-lg bg-rose-100 text-rose-700 text-sm hover:bg-rose-200">Удалить выбранные (${ui.selectedIds.size})</button>` : ''}
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full">
          <thead class="bg-slate-50">
            <tr>
              ${canManage ? `<th class="px-3 py-3 text-left"><input id="select-all" type="checkbox" ${hasRows && rows.every((r) => ui.selectedIds.has(r.id)) ? 'checked' : ''}/></th>` : ''}
              ${headers}
              ${canManage ? '<th class="px-3 py-3 text-right text-xs uppercase text-slate-500">Действия</th>' : ''}
            </tr>
          </thead>
          <tbody>${body}</tbody>
        </table>
      </div>
      <div class="flex items-center justify-between p-4 border-t border-slate-100 text-sm">
        <span>Всего: ${meta.totalElements || 0}</span>
        <div class="flex items-center gap-2">
          <button data-page-action="prev" class="px-3 py-1 rounded border border-slate-300 disabled:opacity-50" ${meta.page <= 0 ? 'disabled' : ''}>Назад</button>
          <span>Стр. ${meta.page + 1} / ${pages}</span>
          <button data-page-action="next" class="px-3 py-1 rounded border border-slate-300 disabled:opacity-50" ${meta.page + 1 >= pages ? 'disabled' : ''}>Вперёд</button>
        </div>
      </div>
    </div>
  `;
}