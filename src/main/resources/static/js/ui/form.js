import { classLabel, fullName } from '../utils/helpers.js';

function optionsForRef(field, refs, data) {
    const source = refs[field.ref] || data[field.ref] || [];
    return source.map((item) => {
        if (field.ref === 'classes') return `<option value="${item.id}">${classLabel(item)}</option>`;
        if (field.ref === 'teachers' || field.ref === 'students') return `<option value="${item.id}">${fullName(item)}</option>`;
        if (field.ref === 'subjects') return `<option value="${item.id}">${item.name}</option>`;
        return `<option value="${item.id}">${item.id}</option>`;
    });
}

export function validateForm(entity, values) {
    const errors = [];

    if (['students', 'teachers'].includes(entity)) {
        if (!values.firstName?.trim()) errors.push('Поле "Имя" обязательно');
        if (!values.lastName?.trim()) errors.push('Поле "Фамилия" обязательно');
        if (!/^\S+@\S+\.\S+$/.test(values.email || '')) errors.push('Укажите корректный email');
    }

    if (entity === 'students' && !['MALE', 'FEMALE'].includes(values.gender)) {
        errors.push('Укажите пол ученика');
    }

    if (entity === 'classes') {
        const grade = Number(values.grade);
        if (!Number.isInteger(grade) || grade < 1 || grade > 11) errors.push('Параллель должна быть от 1 до 11');
        if (!(values.letter || '').trim()) errors.push('Буква класса обязательна');
    }

    if (entity === 'subjects' && !(values.name || '').trim()) {
        errors.push('Название предмета обязательно');
    }

    if (entity === 'grades') {
        const score = Number(values.score);
        if (!Number.isInteger(score) || score < 2 || score > 10) errors.push('Оценка должна быть целым числом от 2 до 10');
        if (!values.date) errors.push('Дата обязательна');
        if (!values.studentId) errors.push('Выберите ученика');
        if (!values.subjectId) errors.push('Выберите предмет');
    }

    return errors;
}

export function renderEntityForm(config, refs, data, row = null) {
    const fieldHtml = config.fields
        .map((field) => {
            const value = row?.[field.key] ?? field.defaultValue ?? '';
            if (field.type === 'select') {
                return `
          <label class="block text-sm font-medium text-slate-700 mb-3">
            ${field.label}
            <select name="${field.key}" class="mt-1 w-full rounded-lg border-slate-300" ${field.required ? 'required' : ''}>
              <option value="">Выберите...</option>
              ${field.options
                    .map((opt) => `<option ${String(value) === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`)
                    .join('')}
            </select>
          </label>
        `;
            }
            if (field.type === 'ref') {
                return `
          <label class="block text-sm font-medium text-slate-700 mb-3">
            ${field.label}
            <select name="${field.key}" class="mt-1 w-full rounded-lg border-slate-300" ${field.required ? 'required' : ''}>
              <option value="">${field.allowEmpty ? 'Не выбрано' : 'Выберите...'}</option>
              ${optionsForRef(field, refs, data).join('')}
            </select>
          </label>
        `;
            }
            if (field.type === 'textarea') {
                return `
          <label class="block text-sm font-medium text-slate-700 mb-3">
            ${field.label}
            <textarea name="${field.key}" rows="3" class="mt-1 w-full rounded-lg border-slate-300">${value || ''}</textarea>
          </label>
        `;
            }
            return `
        <label class="block text-sm font-medium text-slate-700 mb-3">
          ${field.label}
          <input type="${field.type || 'text'}" name="${field.key}" value="${value || ''}" class="mt-1 w-full rounded-lg border-slate-300" ${field.required ? 'required' : ''} />
        </label>
      `;
        })
        .join('');

    return `
    <form id="entity-form" class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
      <h3 class="font-semibold text-slate-900 mb-4">${row ? 'Редактирование' : 'Создание'}: ${config.title}</h3>
      ${fieldHtml}
      <div class="flex gap-2 pt-2">
        <button class="px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-700" type="submit">${row ? 'Сохранить' : 'Создать'}</button>
        <button class="px-4 py-2 rounded-lg border border-slate-300 hover:bg-slate-100" type="button" data-cancel-edit>Сбросить</button>
      </div>
    </form>
  `;
}

export function formDataToObject(form) {
    const data = Object.fromEntries(new FormData(form).entries());
    Object.keys(data).forEach((key) => {
        if (data[key] === '') data[key] = null;
    });
    return data;
}