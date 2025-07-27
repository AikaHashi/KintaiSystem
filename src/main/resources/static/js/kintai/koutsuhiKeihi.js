'use strict';

document.addEventListener('DOMContentLoaded', () => {
  // 手段の選択肢（必要に応じて外に出して共通化してもOK）
  const methodsKoutsuhi = [
    "電車(片道)", "電車(往復)", "電車(定期券)",
    "バス(片道)", "バス(往復)", "バス(定期券)",
    "タクシー", "航空機", "その他"
  ];
  const methodsKeihi = [...methodsKoutsuhi];

  // index
  let koutsuhiIndex = 0;
  let keihiIndex = 0;

  // セレクトボックスのオプション作成
  function createOptions(options, selected) {
    return options.map(opt => {
      return `<option value="${opt}" ${opt === selected ? 'selected' : ''}>${opt}</option>`;
    }).join('');
  }

 // 行追加関数
window.addRow = function(tableBodyId, prefix, index, methods, data = {}) {
  const isExisting = data && (data.koutsuhiId !== undefined || data.keihiId !== undefined);
  const tbody = document.getElementById(tableBodyId);
  const tr = document.createElement("tr");

  // ここで hiddenInput を正しく生成する
  let hiddenInput = '';
  console.log("data",data);
  if (data.hasOwnProperty('keihiId') && data.keihiId !== null && data.keihiId !== undefined) {
    hiddenInput = `<input type="hidden" name="${prefix}[${index}].keihiId" value="${data.keihiId}">`;
  } else if (data.hasOwnProperty('koutsuhiId') && data.koutsuhiId !== null && data.koutsuhiId !== undefined) {
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
      <button type="button" class="remove-row">削除</button>
    </td>
  `;

  tbody.appendChild(tr);

  if (isExisting) {
    setTimeout(() => {
      tr.querySelectorAll('input, select').forEach(el => {
        if (!el.name.endsWith('Id')) {
          el.disabled = true;
        }
      });
    }, 0);
  }
}

// 名前属性インデックス振り直し
window.refreshIndexes = function(tbodyId, prefix) {
  const rows = document.querySelectorAll(`#${tbodyId} tr`);
  rows.forEach((row, i) => {
    row.querySelectorAll('input, select').forEach(elm => {
      const name = elm.getAttribute('name');
      if (name) {
        elm.setAttribute('name', name.replace(new RegExp(`${prefix}\\[\\d+\\]`), `${prefix}[${i}]`));
      }
    });
  });
}

  // 行削除・編集イベント委譲
  document.addEventListener('click', function(e) {
    const target = e.target;
    if (target.classList.contains('remove-row')) {
      const tr = target.closest('tr');
      if (!tr) return;
      const tbody = tr.parentNode;
      tr.remove();

      if (tbody.id === 'koutsuhiTableBody') {
        refreshIndexes('koutsuhiTableBody', 'koutsuhiList');
        koutsuhiIndex = tbody.querySelectorAll('tr').length;
        if (koutsuhiIndex === 0) {
          addRow('koutsuhiTableBody', 'koutsuhiList', 0, methodsKoutsuhi);
          koutsuhiIndex = 1;
        }
      } else if (tbody.id === 'keihiTableBody') {
        refreshIndexes('keihiTableBody', 'keihiList');
        keihiIndex = tbody.querySelectorAll('tr').length;
        if (keihiIndex === 0) {
          addRow('keihiTableBody', 'keihiList', 0, methodsKeihi);
          keihiIndex = 1;
        }
      }
    } else if (target.classList.contains('edit-row')) {
      // 編集ボタン押下：該当行の入力欄disabled解除、ボタンを取消に変更
      const tr = target.closest('tr');
      if (!tr) return;
      const inputs = tr.querySelectorAll('input, select');
      const isEditing = target.textContent === '取消';

      if (isEditing) {
        inputs.forEach(input => input.disabled = true);
        target.textContent = '編集';
      } else {
        inputs.forEach(input => input.disabled = false);
        target.textContent = '取消';
      }
    }
  });
});
