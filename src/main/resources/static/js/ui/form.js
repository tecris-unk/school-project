import { classLabel, fullName } from '../utils/helpers.js';
import {entityLabelWithIcon} from './entityMeta.js';

function getRefLabel(field, item) {
    if (field.ref === 'classes') return classLabel(item);
    if (field.ref === 'teachers' || field.ref === 'students') return fullName(item);
    if (field.ref === 'subjects') return item.name;
    return item.id;
}

function optionsForRef(field, refs, data, value) {
    const source = refs[field.ref] || data[field.ref] || [];
    return source
        .map((item) => `<option ${String(value) === String(item.id) ? 'selected' : ''} value="${item.id}">${getRefLabel(field, item)}</option>`)
        .join('');
}

export function validateForm(entity, values) {
    const errors = {};

    if (['students', 'teachers'].includes(entity)) {
        if (!values.firstName?.trim()) errors.firstName = 'Поле обязательно';
        if (!values.lastName?.trim()) errors.lastName = 'Поле обязательно';
        if (!/^\S+@\S+\.\S+$/.test(values.email || '')) errors.email = 'Укажите корректный email';
    }

    if (entity === 'students' && !['MALE', 'FEMALE'].includes(values.gender)) {
        errors.gender = 'Укажите пол ученика';
    }

    if (entity === 'classes') {
        const grade = Number(values.grade);
        if (!Number.isInteger(grade) || grade < 1 || grade > 11) errors.grade = 'Диапазон: от 1 до 11';
        if (!(values.letter || '').trim()) errors.letter = 'Поле обязательно';
    }

    if (entity === 'subjects' && !(values.name || '').trim()) {
        errors.name = 'Название предмета обязательно';
    }

    if (entity === 'grades') {
        const score = Number(values.score);
        if (!Number.isInteger(score) || score < 2 || score > 10) errors.score = 'Оценка от 2 до 10';
        if (!values.date) errors.date = 'Дата обязательна';
        if (!values.studentId) errors.studentId = 'Выберите ученика';
        if (!values.subjectId) errors.subjectId = 'Выберите предмет';
    }

    return errors;
}

function renderField(field, value, refs, data, error) {
    const baseInputClass = `mt-1 w-full rounded-xl border px-3 py-2 text-sm ${error ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-200' : 'border-slate-300'}`;

    if (field.type === 'select') {
        return `
      <label class="block text-sm font-medium text-slate-700">
        ${field.label}
        <select name="${field.key}" class="${baseInputClass}" ${field.required ? 'required' : ''}>
          <option value="">Выберите...</option>
          ${field.options
            .map((opt) => `<option ${String(value) === String(opt.value) ? 'selected' : ''} value="${opt.value}">${opt.label}</option>`)
            .join('')}
        </select>
        ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
      </label>`;
    }

    if (field.type === 'ref') {
        return `
      <label class="block text-sm font-medium text-slate-700">
        ${field.label}
        <select name="${field.key}" class="${baseInputClass}" ${field.required ? 'required' : ''}>
          <option value="">${field.allowEmpty ? 'Не выбрано' : 'Выберите...'}</option>
          ${optionsForRef(field, refs, data, value)}
        </select>
        ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
      </label>`;
    }

    if (field.type === 'textarea') {
        return `
      <label class="block text-sm font-medium text-slate-700">
        ${field.label}
        <textarea name="${field.key}" rows="3" class="${baseInputClass}">${value || ''}</textarea>
        ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
      </label>`;
    }

    return `
    <label class="block text-sm font-medium text-slate-700">
      ${field.label}
      <input type="${field.type || 'text'}" name="${field.key}" value="${value || ''}" class="${baseInputClass}" ${field.required ? 'required' : ''} />
      ${error ? `<p class="mt-1 text-xs text-rose-600">${error}</p>` : ''}
    </label>`;
}

export function renderEntityForm(config, refs, data, row = null, entityKey = null, formErrors = {}) {
    const fieldHtml = config.fields
        .map((field) => renderField(field, row?.[field.key] ?? field.defaultValue ?? '', refs, data, formErrors[field.key]))
        .join('');

    return `
  <form id="entity-form" class="space-y-4">
      <div>
        <h3 class="font-semibold text-slate-900 text-lg">${row ? 'Редактирование' : 'Создание'}: ${entityKey ? entityLabelWithIcon(entityKey, config.title) : config.title}</h3>
      </div>
      <div class="space-y-4">${fieldHtml}</div>
      <div class="flex gap-2 pt-2">
        <button class="btn-primary" type="submit">${row ? 'Сохранить' : 'Создать'}</button>
        <button class="btn-secondary" type="button" data-cancel-edit>Отмена</button>
      </div>
   </form>`;
}

export function renderDrawer({title, subtitle = '', body, open}) {
    return `
    <div class="fixed inset-0 z-40 ${open ? '' : 'pointer-events-none'}" data-drawer-root>
      <div class="absolute inset-0 bg-slate-900/40 transition-opacity ${open ? 'opacity-100' : 'opacity-0'}" data-drawer-close></div>
      <aside class="absolute right-0 top-0 h-full w-full max-w-xl bg-white border-l border-slate-200 shadow-2xl transition-transform duration-200 ${open ? 'translate-x-0' : 'translate-x-full'}">
        <div class="p-6 border-b border-slate-200 flex items-start justify-between gap-4">
          <div>
            <h2 class="text-xl font-semibold text-slate-900">${title}</h2>
            ${subtitle ? `<p class="text-sm text-slate-500 mt-1">${subtitle}</p>` : ''}
          </div>
          <button class="btn-secondary" type="button" data-drawer-close>Закрыть</button>
        </div>
        <div class="p-6 overflow-y-auto h-[calc(100%-89px)]">${body}</div>
      </aside>
    </div>`;
}

export function formDataToObject(form) {
    const data = Object.fromEntries(new FormData(form).entries());
    Object.keys(data).forEach((key) => {
        if (data[key] === '') data[key] = null;
    });
    return data;
}