'use strict';

document.addEventListener('DOMContentLoaded', () => {
<<<<<<< HEAD
	const targetUserId = window.targetUserId || 'defaultUser';
	const methodsKoutsuhi = [
		"電車(片道)", "電車(往復)", "電車(定期券)",
		"バス(片道)", "バス(往復)", "バス(定期券)",
		"タクシー", "航空機", "その他"
	];
	const methodsKeihi = [...methodsKoutsuhi];

	let koutsuhiIndex = 0;
	let keihiIndex = 0;
	let currentDate = new Date();

 if (statusKoutsuhi) {
    document.getElementById("statusSpanKoutsuhi").textContent = statusKoutsuhi;
  }
  if (statusKeihi) {
    document.getElementById("statusSpanKeihi").textContent = statusKeihi;
  }

// ---------------- 初期表示データ取得 ----------------
function loadInitialData() {
    const targetUserId = window.targetUserId || 'gummy.sk3@gmail.com';
    const yearMonth = currentDate.toISOString().slice(0, 7);
fetch(`/kintai/koutsuhiKeihiSafe/${encodeURIComponent(targetUserId)}?yearMonth=${yearMonth}`, {
    cache: 'no-store',
    credentials: 'same-origin'
})
    .then(res => res.json())
    .then(data => {
        console.log('初期表示データ:', data);
        window.kintaiListJson = window.kintaiListJson || {};
        window.kintaiListJson.koutsuhiByMonth = window.kintaiListJson.koutsuhiByMonth || {};
        window.kintaiListJson.keihiByMonth = window.kintaiListJson.keihiByMonth || {};

        // 月ごとのデータを格納
        window.kintaiListJson.koutsuhiByMonth[yearMonth] = data.koutsuhi || [];
        window.kintaiListJson.keihiByMonth[yearMonth] = data.keihi || [];

        // 表示用にセット
        window.kintaiListJson.koutsuhi = window.kintaiListJson.koutsuhiByMonth[yearMonth];
        window.kintaiListJson.keihi = window.kintaiListJson.keihiByMonth[yearMonth];

        // ステータスも保存
        window.kintaiListJson.statusKoutsuhi = data.statusKoutsuhi || "";
        window.kintaiListJson.statusKeihi = data.statusKeihi || "";
        window.kintaiListJson.userRole = data.userRole || "";

        // 表示更新（テーブル＋ボタン）
        refreshTable();
    })
    .catch(err => console.error("初期表示データ取得エラー:", err));
}


	function createOptions(options, selected) {
		return options.map(opt =>
			`<option value="${opt}" ${opt === selected ? 'selected' : ''}>${opt}</option>`
		).join('');
	}

	// ---------------- 行追加 ----------------
	window.addRow = function(tableBodyId, prefix, index, methods, data = {}) {
		const tbody = document.getElementById(tableBodyId);
		const tr = document.createElement("tr");
		const isExisting = data && (data.koutsuhiId || data.keihiId);

		let hiddenInput = '';
		if (data.keihiId != null) hiddenInput = `<input type="hidden" name="${prefix}[${index}].keihiId" value="${data.keihiId}">`;
		else if (data.koutsuhiId != null) hiddenInput = `<input type="hidden" name="${prefix}[${index}].koutsuhiId" value="${data.koutsuhiId}">`;

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

		// ★既存データは編集不可
		if (isExisting) {
			tr.querySelectorAll('input, select').forEach(el => {
				if (!el.name.endsWith('Id')) el.disabled = true;
			});
		}

		// ★編集ボタン押下で編集可能
		tr.querySelector('.edit-row').addEventListener('click', () => {
			tr.querySelectorAll('input, select').forEach(el => el.disabled = false);
		});
	};

	// ---------------- インデックス更新 ----------------
	window.refreshIndexes = function(tbodyId, prefix) {
		const rows = document.querySelectorAll(`#${tbodyId} tr`);
		rows.forEach((row, i) => {
			row.querySelectorAll('input, select').forEach(el => {
				const name = el.getAttribute('name');
				if (name) el.setAttribute('name', name.replace(new RegExp(`${prefix}\\[\\d+\\]`), `${prefix}[${i}]`));
			});
		});
	};

	// ---------------- 月表示更新 ----------------
	function updateMonthLabel() {
		const monthLabel = document.getElementById('currentMonthLabel');
		if (monthLabel) {
			monthLabel.textContent = `${currentDate.getFullYear()}年${currentDate.getMonth() + 1}月`;
		}
	}

	document.getElementById('prevMonth').addEventListener('click', () => {
		currentDate.setMonth(currentDate.getMonth() - 1);
		loadInitialData();
	});

	document.getElementById('nextMonth').addEventListener('click', () => {
		currentDate.setMonth(currentDate.getMonth() + 1);
		loadInitialData();
	});

	document.getElementById('add-koutsuhi-row').addEventListener('click', () => {
		addRow('koutsuhiTableBody', 'koutsuhi', koutsuhiIndex++, methodsKoutsuhi);
	});

	document.getElementById('add-keihi-row').addEventListener('click', () => {
		addRow('keihiTableBody', 'keihi', keihiIndex++, methodsKeihi);
	});

	// ---------------- 行削除 ----------------
	function bindRemoveButtons(tbody, deletedIdsInput) {
		tbody.addEventListener('click', (e) => {
			if (e.target.classList.contains('remove-row')) {
				const tr = e.target.closest('tr');
				const id = e.target.dataset.id;
				if (id) {
					const input = document.getElementById(deletedIdsInput);
					if (input.value) input.value += `,${id}`;
					else input.value = id;
				}
				tr.remove();
				refreshIndexes(tbody.id, tbody.id === 'koutsuhiTableBody' ? 'koutsuhi' : 'keihi');
			}
		});
	}

	bindRemoveButtons(document.getElementById('koutsuhiTableBody'), 'deletedKoutsuhiIds');
	bindRemoveButtons(document.getElementById('keihiTableBody'), 'deletedKeihiIds');

// ---------------- 表示更新 ----------------
function refreshTable() {
    updateMonthLabel();

    // 交通費テーブル更新
    const koutsuhiTbody = document.getElementById('koutsuhiTableBody');
    koutsuhiTbody.innerHTML = '';
    if (window.kintaiListJson.koutsuhi.length > 0) {
        window.kintaiListJson.koutsuhi.forEach((data, i) => {
            addRow('koutsuhiTableBody', 'koutsuhi', i, methodsKoutsuhi, data);
        });
        koutsuhiIndex = window.kintaiListJson.koutsuhi.length;
    } else {
        addRow('koutsuhiTableBody', 'koutsuhi', 0, methodsKoutsuhi);
        koutsuhiIndex = 1;
    }

    // 経費テーブル更新
    const keihiTbody = document.getElementById('keihiTableBody');
    keihiTbody.innerHTML = '';
    if (window.kintaiListJson.keihi.length > 0) {
        window.kintaiListJson.keihi.forEach((data, i) => {
            addRow('keihiTableBody', 'keihi', i, methodsKeihi, data);
        });
        keihiIndex = window.kintaiListJson.keihi.length;
    } else {
        addRow('keihiTableBody', 'keihi', 0, methodsKeihi);
        keihiIndex = 1;
    }

    // ★ API は呼ばず、取得済みデータでステータス反映
    const userRole = window.kintaiListJson.userRole;
    updateApplicationButtons(window.kintaiListJson.statusKoutsuhi || "", "Koutsuhi", userRole);
    updateApplicationButtons(window.kintaiListJson.statusKeihi || "", "Keihi", userRole);
}

// ★最初は refreshTable() を呼ばず、データ取得後に呼ぶ
loadInitialData();

	// ---------------- 保存（交通費） ----------------
	document.getElementById('btn-save-koutsuhi').addEventListener('click', () => {
		const form = document.getElementById('koutsuhi-form');
		const formData = new FormData(form);

		fetch('/kintai/keihi/saveKoutsuhi', { method: 'POST', body: formData })
			.then(res => res.text())
			.then(text => {
				let data;
				try { data = JSON.parse(text); }
				catch (e) {
					console.warn("交通費保存時のJSON解析失敗:", text);
					alert("交通費データの保存に失敗しました。");
					return;
				}

				if (!window.kintaiListJson) window.kintaiListJson = {};
				if (!window.kintaiListJson.koutsuhiByMonth) window.kintaiListJson.koutsuhiByMonth = {};

				let firstDateInput = form.querySelector('input[name^="koutsuhi"][type="date"]');
				let savedYearMonth = firstDateInput && firstDateInput.value
					? firstDateInput.value.slice(0, 7)
					: new Date().toISOString().slice(0, 7);

				window.kintaiListJson.koutsuhiByMonth[savedYearMonth] = data.koutsuhi || [];
				const displayYearMonth = currentDate.toISOString().slice(0, 7);
				window.kintaiListJson.koutsuhi = window.kintaiListJson.koutsuhiByMonth[displayYearMonth] || [];

				refreshTable();
				alert("交通費データを保存しました。");
			})
			.catch(err => {
				console.error(err);
				alert("交通費データの保存に失敗しました。");
			});
	});

	// ---------------- 保存（経費） ----------------
	document.getElementById('btn-save-keihi').addEventListener('click', () => {
		const form = document.getElementById('keihi-form');
		const formData = new FormData(form);

		fetch('/kintai/keihi/saveKeihi', { method: 'POST', body: formData })
			.then(res => res.text())
			.then(text => {
				let data;
				try { data = JSON.parse(text); }
				catch (e) {
					console.warn("経費保存時のJSON解析失敗:", text);
					alert("経費データの保存に失敗しました。");
					return;
				}

				if (!window.kintaiListJson) window.kintaiListJson = {};
				if (!window.kintaiListJson.keihiByMonth) window.kintaiListJson.keihiByMonth = {};

				let firstDateInput = form.querySelector('input[name^="keihi"][type="date"]');
				let savedYearMonth = firstDateInput && firstDateInput.value
					? firstDateInput.value.slice(0, 7)
					: new Date().toISOString().slice(0, 7);

				window.kintaiListJson.keihiByMonth[savedYearMonth] = data.keihi || [];
				const displayYearMonth = currentDate.toISOString().slice(0, 7);
				window.kintaiListJson.keihi = window.kintaiListJson.keihiByMonth[displayYearMonth] || [];

				refreshTable();
				alert("経費データを保存しました。");
			})
			.catch(err => {
				console.error(err);
				alert("経費データの保存に失敗しました。");
			});
	});

	// ---------------- 申請・承認ボタン更新 ----------------
// ---------------- 申請・承認ボタン更新 ----------------
function updateApplicationButtons(status, type, role = "") {
    const applyBtn = document.getElementById(`apply${type}Btn`);
    const reapplyBtn = document.getElementById(`reapply${type}Btn`);
    const approveBtn = document.getElementById(`approve${type}Btn`);
    const rejectBtn = document.getElementById(`reject${type}Btn`);
    const statusSpan = document.getElementById(`statusSpan${type}`);

    status = status ?? "";

    // role によるボタン表示
    if (role === "ROLE_ADMIN") {
        if (applyBtn) applyBtn.style.display = "none";
        if (reapplyBtn) reapplyBtn.style.display = "none";
        if (approveBtn) approveBtn.style.display = "inline-block";
        if (rejectBtn) rejectBtn.style.display = "inline-block";
    } else {
        if (applyBtn) applyBtn.style.display = "inline-block";
        if (reapplyBtn) reapplyBtn.style.display = "inline-block";
        if (approveBtn) approveBtn.style.display = "none";
        if (rejectBtn) rejectBtn.style.display = "none";
    }

    // ボタン活性／非活性
    switch (status) {
        case "":
            if (applyBtn) applyBtn.disabled = false;
            if (reapplyBtn) reapplyBtn.disabled = true;
            if (approveBtn) approveBtn.disabled = false;
            if (rejectBtn) rejectBtn.disabled = false;
            break;
        case "APPLYING":
            if (applyBtn) applyBtn.disabled = true;
            if (reapplyBtn) reapplyBtn.disabled = true;
            if (approveBtn) approveBtn.disabled = false;
            if (rejectBtn) rejectBtn.disabled = false;
            break;
        case "REJECTED":
            if (applyBtn) applyBtn.disabled = true;
            if (reapplyBtn) reapplyBtn.disabled = false;
            if (approveBtn) approveBtn.disabled = true;
            if (rejectBtn) rejectBtn.disabled = true;
            break;
        case "APPROVED":
            if (applyBtn) applyBtn.disabled = true;
            if (reapplyBtn) reapplyBtn.disabled = true;
            if (approveBtn) approveBtn.disabled = true;
            if (rejectBtn) rejectBtn.disabled = true;
            break;
        default:
            if (applyBtn) applyBtn.disabled = false;
            if (reapplyBtn) reapplyBtn.disabled = true;
            if (approveBtn) approveBtn.disabled = false;
            if (rejectBtn) rejectBtn.disabled = false;
    }

    if (statusSpan) {
        statusSpan.textContent = ({
            "APPLYING": '申請中',
            "REJECTED": '差戻中',
            "APPROVED": '承認済'
        })[status] || '未申請';
    }
}
function fetchStatusAndUpdate() {
    const yearMonth = currentDate.toISOString().slice(0, 7);
    fetch(`/kintai/koutsuhiKeihiSafe/${targetUserId}?yearMonth=${yearMonth}`, {
        cache: 'no-store',
        credentials: 'same-origin'
    })
    .then(res => res.json())
    .then(data => {

        const userRole = data.userRole; // ここで取得
        console.log(statusKoutsuhi)
        console.log("statusKoutsuhi:", data.statusKoutsuhi);
        console.log("statusKeihi:", data.statusKeihi);

        // 交通費ボタン・ステータス更新
        updateApplicationButtons(data.statusKoutsuhi || "", "Koutsuhi", userRole);

        // 経費ボタン・ステータス更新
        updateApplicationButtons(data.statusKeihi || "", "Keihi", userRole);

    })
    .catch(err => console.error("fetch エラー:", err));
}

// ---------------- 申請・承認ボタン共通 ----------------
function handleAction(btnId, commentPrompt, controllerPath) {
    const btn = document.getElementById(btnId);
    if (!btn) return;

    btn.addEventListener('click', () => {
        if (!confirm("本当に送信しますか？")) return;

        const comment = commentPrompt ? prompt(commentPrompt) || "" : "";
        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfToken = csrfTokenMeta ? csrfTokenMeta.content : '';
        const params = new URLSearchParams({ comment, userId: window.targetUserId });

        // ★必ず url を初期化（ここを消しちゃダメ）
        let url = controllerPath;

        // ★ボタン押下直後に仮ステータスを反映
        let tempStatus = "";
        if (controllerPath.includes("/apply")) tempStatus = "APPLYING";
        else if (controllerPath.includes("/approve")) tempStatus = "APPROVED";
        else if (controllerPath.includes("/reject")) tempStatus = "REJECTED";

        const updateTemp = (type) => {
            updateApplicationButtons(tempStatus, type);
            const statusSpan = document.getElementById(`statusSpan${type}`);
            if (statusSpan) {
                statusSpan.textContent = ({
                    "APPLYING": '申請中',
                    "REJECTED": '差戻中',
                    "APPROVED": '承認済'
                })[tempStatus] || '未申請';
            }
        };

        if (btnId.includes("Koutsuhi")) updateTemp("Koutsuhi");
        if (btnId.includes("Keihi")) updateTemp("Keihi");

        // ---------------- サーバー送信 ----------------
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': csrfToken
            },
            body: params,
            credentials: 'same-origin'
        })
        .then(res => {
            if (!res.ok) throw new Error("HTTPエラー " + res.status);
            return res.json();
        })
        .then(data => {
            // サーバー返却の正確なステータスで上書き
            fetchStatusAndUpdate();
        })
        .catch(err => console.error("fetch 例外:", err));
    });
}
// ---------------- ボタン登録 ----------------
handleAction("applyKoutsuhiBtn", "交通費を申請しますか？", "/kintai/koutsuhi/apply");
handleAction("reapplyKoutsuhiBtn", "交通費を再申請しますか？", "/kintai/koutsuhi/apply");
handleAction("approveKoutsuhiBtn", "交通費を承認しますか？", `/kintai/koutsuhi/approve/${window.targetUserId}`);
handleAction("rejectKoutsuhiBtn", "交通費を差戻しますか？", `/kintai/koutsuhi/reject/${window.targetUserId}`);

handleAction("applyKeihiBtn", "経費を申請しますか？", `/kintai/keihi/apply/${encodeURIComponent(window.targetUserId)}`);
handleAction("reapplyKeihiBtn", "経費を再申請しますか？", `/kintai/keihi/apply/${encodeURIComponent(window.targetUserId)}`);
handleAction("approveKeihiBtn", "経費を承認しますか？", `/kintai/keihi/approve/${encodeURIComponent(window.targetUserId)}`);
handleAction("rejectKeihiBtn", "経費を差戻しますか？", `/kintai/keihi/reject/${encodeURIComponent(window.targetUserId)}`);

=======
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
>>>>>>> b1ef8f0c1f78745eaf83d00a3989b8a9bcb2d280
});