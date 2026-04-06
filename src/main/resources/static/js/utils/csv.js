function escapeCell(cell) {
  if (cell === null || cell === undefined) return '';
  const raw = String(cell);
  if (raw.includes(',') || raw.includes('"') || raw.includes('\n')) {
    return `"${raw.replaceAll('"', '""')}"`;
  }
  return raw;
}

export function exportToCsv(filename, headers, rows) {
  const content = [
    headers.map(escapeCell).join(','),
    ...rows.map((row) => row.map(escapeCell).join(',')),
  ].join('\n');

  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.append(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}