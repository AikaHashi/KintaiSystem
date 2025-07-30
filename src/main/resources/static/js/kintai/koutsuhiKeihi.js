'use strict';

document.addEventListener('DOMContentLoaded', () => {
  const methodsKoutsuhi = [
    "電車(片道)", "電車(往復)", "電車(定期券)",
    "バス(片道)", "バス(往復)", "バス(定期券)",
    "タクシー", "航空機", "その他"
  ];
  const methodsKeihi = [...methodsKoutsuhi];

  let koutsuhiIndex = 0;
  let keihiIndex = 0;

  const deletedKoutsuhiIds = [];
  const deletedKeihiIds = [];

  function createOptions(options, selected) {
    return options.map(opt =>
      `<option value="${opt}" ${opt === selected ? 'selected' : ''}>${opt}</option>`
    ).join('');
  }

  window.addRow = function (tableBodyId, prefix, index, methods, data = {}) {
    const tbody = document.getElementById(tableBodyId);
    const tr = document.createElement("tr");
    const isExisting = data && (data.koutsuhiId || data.keihiId);

    let hiddenInput = '';
    if (data.keihiId != null) {
      hiddenInput = `<input type="hidden" name="${prefix}[${index}].keihiId" value="${data.keihiId}">`;
    } else if (data.koutsuhiId != null) {
      hiddenInput = `<input type="hidden" name="${prefix}[${index}].koutsuhiId" value="${data.koutsuhiId}">`;
    }

    tr.innerHTML = `
      ${hiddenInput}
      <td><input type="date" name="${prefix}[${index}].date" value="${data.date || ''}" required></td>
      <td><select name="${prefix}[${index}].method">${createOptions(methods, data.method || '')}</select></td>
      <td><input type="text" name="${prefix}[${index}].departure" value="${data.departure || ''}" required></td>
      <td><input type="text" name="${prefix}[${index}].arrival" value="${data.arrival || ''}" required></td>
      <td><input type="text" name="${prefix}[${index}].via" value="${data.via || ''}"></td>
      <td><input type="number" name="${prefix}[${index}].amount" value="${data.amount || ''}" required>円</td>
      <td><input type="text" name="${prefix}[${index}].note" value="${data.note || ''}"></td>
      <td>
        <button type="button" class="edit-row">編集</button>
        <button type="button" class="remove-row" data-id="${data.keihiId || data.koutsuhiId || ''}" data-type="${tableBodyId}">削除</button>
      </td>
    `;

    tbody.appendChild(tr);

    if (isExisting) {
      setTimeout(() => {
        tr.querySelectorAll('input, select').forEach(el => {
          if (!el.name.endsWith('Id')) el.disabled = true;
        });
      }, 0);
    }
  };

  window.refreshIndexes = function (tbodyId, prefix) {
    const rows = document.querySelectorAll(`#${tbodyId} tr`);
    rows.forEach((row, i) => {
      row.querySelectorAll('input, select').forEach(el => {
        const name = el.getAttribute('name');
        if (name) {
          el.setAttribute('name', name.replace(new RegExp(`${prefix}\\[\\d+\\]`), `${prefix}[${i}]`));
        }
      });
    });
  };

  // 削除ボタン処理
  document.addEventListener('click', function (e) {
    const target = e.target;
    if (!target.classList.contains('remove-row')) return;

    const tr = target.closest('tr');
    const tbody = tr.closest('tbody');
    const dataId = target.getAttribute('data-id');
    const dataType = target.getAttribute('data-type');

    if (confirm('この行を削除しますか？')) {
      // 削除IDをhiddenに追記
      if (dataId) {
        if (dataType === 'koutsuhiTableBody') {
          deletedKoutsuhiIds.push(dataId);
          const hidden = document.getElementById('deletedKoutsuhiIds');
          hidden.value = deletedKoutsuhiIds.join(',');
        } else if (dataType === 'keihiTableBody') {
          deletedKeihiIds.push(dataId);
          const hidden = document.getElementById('deletedKeihiIds');
          hidden.value = deletedKeihiIds.join(',');
        }
      }

      // 行削除 & 再採番
      tr.remove();

      if (dataType === 'koutsuhiTableBody') {
        refreshIndexes('koutsuhiTableBody', 'koutsuhiList');
        koutsuhiIndex = document.querySelectorAll('#koutsuhiTableBody tr').length;
        if (koutsuhiIndex === 0) {
          addRow('koutsuhiTableBody', 'koutsuhiList', 0, methodsKoutsuhi);
          koutsuhiIndex = 1;
        }
      } else {
        refreshIndexes('keihiTableBody', 'keihiList');
        keihiIndex = document.querySelectorAll('#keihiTableBody tr').length;
        if (keihiIndex === 0) {
          addRow('keihiTableBody', 'keihiList', 0, methodsKeihi);
          keihiIndex = 1;
        }
      }
    }
  });

  // 編集ボタン処理
  document.addEventListener('click', function (e) {
    if (!e.target.classList.contains('edit-row')) return;
    const tr = e.target.closest('tr');
    const inputs = tr.querySelectorAll('input, select');
    const isEditing = e.target.textContent === '取消';
    inputs.forEach(el => el.disabled = isEditing);
    e.target.textContent = isEditing ? '編集' : '取消';
  });

  // submit 時に hidden に削除IDを再セット（念のため）
  document.querySelector('form').addEventListener('submit', function () {
    document.getElementById('deletedKoutsuhiIds').value = deletedKoutsuhiIds.join(',');
    document.getElementById('deletedKeihiIds').value = deletedKeihiIds.join(',');
  });
});