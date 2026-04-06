let container;

function ensureContainer() {
    if (!container) {
        container = document.createElement('div');
        container.className = 'fixed top-4 right-4 z-50 space-y-2';
        document.body.appendChild(container);
    }
}

export function notify(message, type = 'success') {
    ensureContainer();

    const item = document.createElement('div');
    const colorMap = {
        success: 'bg-emerald-600',
        error: 'bg-rose-600',
        info: 'bg-sky-600',
    };

    item.className = `${colorMap[type] || colorMap.info} text-white px-4 py-3 rounded-lg shadow-lg text-sm min-w-64`;
    item.textContent = message;
    container.appendChild(item);

    setTimeout(() => {
        item.classList.add('opacity-0', 'transition');
        setTimeout(() => item.remove(), 250);
    }, 2600);
}

export function confirmModal({ title, description, onConfirm }) {
    const overlay = document.createElement('div');
    overlay.className = 'fixed inset-0 bg-black/40 z-40 flex items-center justify-center p-4';

    overlay.innerHTML = `
    <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
      <h3 class="text-lg font-semibold text-slate-900 mb-2">${title}</h3>
      <p class="text-sm text-slate-600 mb-6">${description}</p>
      <div class="flex justify-end gap-3">
        <button data-close class="px-4 py-2 rounded-lg border border-slate-300 hover:bg-slate-100">Отмена</button>
        <button data-confirm class="px-4 py-2 rounded-lg bg-rose-600 text-white hover:bg-rose-700">Удалить</button>
      </div>
    </div>
  `;

    overlay.querySelector('[data-close]').addEventListener('click', () => overlay.remove());
    overlay.querySelector('[data-confirm]').addEventListener('click', async () => {
        await onConfirm();
        overlay.remove();
    });
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) overlay.remove();
    });

    document.body.appendChild(overlay);
}