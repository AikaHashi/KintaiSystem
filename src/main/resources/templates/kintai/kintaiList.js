
// ▼ ログインユーザー（社員名）
//const currentEmployee = '田中太郎';
//const targetUserName = document.getElementById('userSelect').value;

// ▼ 現在表示中の年月
let currentDate = new Date();

// ▼ 時間文字列（"HH:MM"）を分数に変換
function timeStrToMinutes(timeStr) {
	try {
		const str = String(timeStr);
		const [h, m] = str.split(':').map(Number);
		return h * 60 + m;
	} catch (e) {
		console.error("Error in timeStrToMinutes. timeStr=", timeStr, "typeof:", typeof timeStr);
		throw e;
	}
}

// ▼ 分数を時間文字列（"HH:MM"）に変換
function minutesToTimeStr(minutes) {
	const h = Math.floor(minutes / 60);
	const m = minutes % 60;
	return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`;
}

// 🔥 ★追加：Decimal → "HH:MM"
function decimalToTimeStr(decimal) {
	if (!decimal && decimal !== 0) return "00:00"; // null, undefined, '' 対策
	const totalMin = Math.round(Number(decimal) * 60);
	const h = Math.floor(totalMin / 60);
	const m = totalMin % 60;
	return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`;
}


// ▼ 勤怠データ生成（出勤・休暇日数・時間集計込み）
function generateDummyData(year, month) {
	const daysInMonth = new Date(year, month + 1, 0).getDate();
	const data = [];

	const counts = {
		attendance: 0, absent: 0, paidLeave: 0, substituteLeave: 0,
		compensatoryLeave: 0, bereavementLeave: 0, sickLeave: 0,
		maternityLeave: 0, childcareLeave: 0, otherLeave: 0,
		scheduledHours: 0, actualHours: 0, breakTime: 0,
		overtime: 0, deductions: 0, holidayHours: 0
	};

	const kintaiMap = {};
	if (Array.isArray(kintaiListJson)) {
		kintaiListJson.forEach(dto => {
			if (dto.workDate) kintaiMap[dto.workDate] = dto;
		});
	}

	for (let day = 1; day <= daysInMonth; day++) {
		const date = new Date(year, month, day, 12);
		const dateStr = date.toISOString().split('T')[0];
		const dto = kintaiMap[dateStr] || {};

		const plannedWorkMin = (dto.plannedWorkStartTimeStr && dto.plannedWorkEndTimeStr)
			? timeStrToMinutes(dto.plannedWorkEndTimeStr) - timeStrToMinutes(dto.plannedWorkStartTimeStr)
			: 0;

		const plannedBreakMin = (dto.plannedBreakStartTimeStr && dto.plannedBreakEndTimeStr)
			? timeStrToMinutes(dto.plannedBreakEndTimeStr) - timeStrToMinutes(dto.plannedBreakStartTimeStr)
			: 0;

		let scheduledMin = plannedWorkMin - plannedBreakMin;

		const actualWorkMin = (dto.actualWorkStartTimeStr && dto.actualWorkEndTimeStr)
			? timeStrToMinutes(dto.actualWorkEndTimeStr) - timeStrToMinutes(dto.actualWorkStartTimeStr)
			: 0;

		const actualBreakMin = (dto.actualBreakStartTimeStr && dto.actualBreakEndTimeStr)
			? timeStrToMinutes(dto.actualBreakEndTimeStr) - timeStrToMinutes(dto.actualBreakStartTimeStr)
			: 0;

		const actualMin = actualWorkMin - actualBreakMin;
		// ステータス別調整
		let adjustedActualMin = actualMin;
		let adjustedBreakMin = actualBreakMin;
		let adjustedOvertimeMin = Math.max(0, actualMin - scheduledMin);
		let holidayMinutes = 0;
		let deductionMin = 0;

		switch (dto.kintaiStatus) {
			case 'paid_leave_full':
			case 'substitute_leave':
			case 'compensatory_leave':
			case 'work_related_illness':
			case 'maternity_leave':
			case 'childcare_leave':
			case 'other_leave':
				adjustedActualMin = 0;
				adjustedBreakMin = 0;
				adjustedOvertimeMin = 0;
				deductionMin = 0;
				scheduledMin = 0;
				break;
			case 'paid_leave_half':
				adjustedActualMin = actualMin / 2;
				adjustedBreakMin = 0;
				adjustedOvertimeMin = 0;
				deductionMin = scheduledMin - adjustedActualMin;
				break;
			default:
				deductionMin = Math.max(0, scheduledMin - adjustedActualMin);
		}

		const entry = {
			date: dateStr,
			userName: dto.userName || targetUserName,
			updatedBy: dto.updatedBy || '',

			plannedWorkStartTime: dto.plannedWorkStartTime || '',
			plannedWorkEndTime: dto.plannedWorkEndTime || '',
			plannedBreakStartTime: dto.plannedBreakStartTime || '',
			plannedBreakEndTime: dto.plannedBreakEndTime || '',

			actualWorkStartTime: dto.actualWorkStartTime || '',
			actualWorkEndTime: dto.actualWorkEndTime || '',
			actualBreakStartTime: dto.actualBreakStartTime || '',
			actualBreakEndTime: dto.actualBreakEndTime || '',

			// 🔥 ここが本命（DBのDecimalを使う）
			scheduledWorkHours: decimalToTimeStr(dto.scheduledWorkHours),
			actualWorkHours: decimalToTimeStr(dto.actualWorkHours),
			overtimeHours: decimalToTimeStr(dto.overtimeHours),
			deductionTime: decimalToTimeStr(dto.deductionTime),

			kintaiStatus: dto.kintaiStatus || 'nothing',
			kintaiComment: dto.kintaiComment || ''
		};
		data.push(entry);

		// 🔥 ここ追加（休暇カウント）
		switch (dto.kintaiStatus) {
			case 'paid_leave_full':
				counts.paidLeave += 1;
				break;
			case 'paid_leave_half':
				counts.paidLeave += 0.5;
				break;
			case 'substitute_leave':
				counts.substituteLeave += 1;
				break;
			case 'compensatory_leave':
				counts.compensatoryLeave += 1;
				break;
			case 'work_related_illness':
				counts.sickLeave += 1;
				break;
			case 'maternity_leave':
				counts.maternityLeave += 1;
				break;
			case 'childcare_leave':
				counts.childcareLeave += 1;
				break;
			case 'other_leave':
				counts.otherLeave += 1;
				break;
		}

		// 集計加算
		counts.scheduledHours += scheduledMin / 60;
		counts.actualHours += adjustedActualMin / 60;
		counts.breakTime += adjustedBreakMin / 60;
		counts.overtime += adjustedOvertimeMin / 60;
		counts.deductions += deductionMin / 60;
		counts.holidayHours += holidayMinutes / 60;
	}

	// 出勤・欠勤
	let totalAbsent =
		counts.paidLeave +
		counts.substituteLeave +
		counts.compensatoryLeave +
		counts.sickLeave +
		counts.maternityLeave +
		counts.childcareLeave +
		counts.otherLeave +
		counts.bereavementLeave;

	let totalAttendance = 0;
	data.forEach(e => {
		if (e.kintaiStatus === 'nothing' && timeStrToMinutes(e.actualWorkHours) > 0) {
			totalAttendance += 1;
		}
	});

	counts.attendance = totalAttendance;
	counts.absent = totalAbsent;

	// 表示
	document.getElementById('attendanceDays').textContent = counts.attendance;
	document.getElementById('absentDays').textContent = counts.absent;
	document.getElementById('paidLeaveDays').textContent = counts.paidLeave;
	document.getElementById('substituteLeaveDays').textContent = counts.substituteLeave;
	document.getElementById('compensatoryLeaveDays').textContent = counts.compensatoryLeave;
	document.getElementById('bereavementLeaveDays').textContent = counts.bereavementLeave;
	document.getElementById('sickLeaveDays').textContent = counts.sickLeave;
	document.getElementById('maternityLeaveDays').textContent = counts.maternityLeave;
	document.getElementById('childcareLeaveDays').textContent = counts.childcareLeave;
	document.getElementById('otherLeaveDays').textContent = counts.otherLeave;
	document.getElementById('scheduledHours').textContent = `${counts.scheduledHours.toFixed(2)}h`;
	document.getElementById('actualHours').textContent = `${counts.actualHours.toFixed(2)}h`;
	document.getElementById('breakTime').textContent = `${counts.breakTime.toFixed(2)}h`;
	document.getElementById('overtime').textContent = `${counts.overtime.toFixed(2)}h`;
	document.getElementById('deductions').textContent = `${counts.deductions.toFixed(2)}h`;
	document.getElementById('holidayHours').textContent = `${counts.holidayHours.toFixed(2)}h`;

	return data;
}
// ▼ 勤怠テーブルの生成
function createTable(data) {
	// 古いテーブルを削除
	const existingTable = document.getElementById('kintaiTable');
	if (existingTable) {
		existingTable.remove();
	}

	const table = document.createElement('table');
	table.id = 'kintaiTable';
	const thead = document.createElement('thead');
	const tbody = document.createElement('tbody');

	thead.innerHTML = `
    <tr>
      <th rowspan="2">日付</th>
      <th rowspan="2">社員名</th>
      <th rowspan="2">更新者</th>
      <th colspan="2">予定</th>
      <th colspan="8">実績</th>
    </tr>
    <tr>
      <th>勤務時間</th><th>休憩時間</th>
      <th>勤務時間</th><th>休憩時間</th>
      <th>所定時間</th><th>実働時間</th>
      <th>時間外</th><th>控除</th><th>状態</th><th>特記事項</th>
    </tr>
  `;

	data.forEach(entry => {
		const tr = document.createElement('tr');
		tr.innerHTML = `
      <td>${entry.date}</td>
      <td>${entry.userName}</td>
      <td>${entry.updatedBy}</td>
      <td>
        <input type="time" name="plannedWorkStartTime" value="${entry.plannedWorkStartTime}" disabled/>
         ～ 
        <input type="time" name="plannedWorkEndTime" value="${entry.plannedWorkEndTime}" disabled/>
      </td>
      <td>
        <input type="time" name="plannedBreakStartTime" value="${entry.plannedBreakStartTime}" disabled/>
         ～ 
        <input type="time" name="plannedBreakEndTime" value="${entry.plannedBreakEndTime}" disabled/>
      </td>
      <td>
        <input type="time" name="actualWorkStartTime" value="${entry.actualWorkStartTime}" disabled/>
         ～ 
        <input type="time" name="actualWorkEndTime" value="${entry.actualWorkEndTime}" disabled/>
      </td>
      <td>
        <input type="time" name="actualBreakStartTime" value="${entry.actualBreakStartTime}" disabled/>
         ～ 
        <input type="time" name="actualBreakEndTime" value="${entry.actualBreakEndTime}" disabled/>
      </td>
      <td><input type="text" name="scheduledWorkHours" value="${entry.scheduledWorkHours}" readonly disabled/></td>
      <td><input type="text" name="actualWorkHours" value="${entry.actualWorkHours}" readonly disabled/></td>
      <td><input type="text" name="overtimeHours" value="${entry.overtimeHours}" readonly disabled/></td>
      <td><input type="text" name="deductionTime" value="${entry.deductionTime}" readonly disabled/></td>
      <td>
        <select name="kintaiStatus" disabled>
          <option value="nothing" ${entry.kintaiStatus === 'nothing' ? 'selected' : ''}>なし</option>
          <option value="paid_leave_full" ${entry.kintaiStatus === 'paid_leave_full' ? 'selected' : ''}>有給休暇(全休)</option>
          <option value="paid_leave_half" ${entry.kintaiStatus === 'paid_leave_half' ? 'selected' : ''}>有給休暇(半休)</option>
          <option value="substitute_leave" ${entry.kintaiStatus === 'substitute_leave' ? 'selected' : ''}>振替休暇</option>
          <option value="compensatory_leave" ${entry.kintaiStatus === 'compensatory_leave' ? 'selected' : ''}>代休</option>
          <option value="work_related_illness" ${entry.kintaiStatus === 'work_related_illness' ? 'selected' : ''}>業務上の疾病</option>
          <option value="maternity_leave" ${entry.kintaiStatus === 'maternity_leave' ? 'selected' : ''}>産前産後休業</option>
          <option value="childcare_leave" ${entry.kintaiStatus === 'childcare_leave' ? 'selected' : ''}>育児休業</option>
          <option value="other_leave" ${entry.kintaiStatus === 'other_leave' ? 'selected' : ''}>その他休業</option>
        </select>
      </td>
      <td><input type="text" name="kintaiComment" value="${entry.kintaiComment}" disabled/></td>
    `;
		tbody.appendChild(tr);
	});

	// ループ外で append
	table.appendChild(thead);
	table.appendChild(tbody);
	document.getElementById('tableContainer').appendChild(table);
}

// ボタン要素
const editBtn = document.getElementById('editBtn');
const saveBtn = document.getElementById('saveBtn');
const cancelBtn = document.getElementById('cancelBtn');

let originalData = [];

// 編集開始
// ▼ 編集開始
editBtn.addEventListener('click', () => {
	const trList = document.querySelectorAll('#kintaiTable tbody tr');

	// 元データ保持
	originalData = Array.from(trList).map(tr => {
		const inputs = tr.querySelectorAll('input, select');
		return Array.from(inputs).map(input => input.value);
	});

	// 編集可能化
	trList.forEach(tr => {
		const inputs = tr.querySelectorAll('input, select');
		inputs.forEach(input => {
			if (!input.readOnly) input.disabled = false;
		});
	});

	editBtn.style.display = 'none';
	saveBtn.style.display = 'inline';
	cancelBtn.style.display = 'inline';
});

// ▼ キャンセル
cancelBtn.addEventListener('click', () => {
	const trList = document.querySelectorAll('#kintaiTable tbody tr');

	trList.forEach((tr, rowIndex) => {
		const inputs = tr.querySelectorAll('input, select');
		inputs.forEach((input, idx) => {
			if (originalData && originalData[rowIndex] && originalData[rowIndex][idx] !== undefined) {
				input.value = originalData[rowIndex][idx];
			}
			input.disabled = true;
		});
	});

	editBtn.style.display = 'inline';
	saveBtn.style.display = 'none';
	cancelBtn.style.display = 'none';
	setOriginalValues(); // 安全のため再設定

	// 入力イベントを付与
	attachRecalcEvents();
});



// ▼ 現在の月表示更新
function updateMonthLabel() {
	const year = currentDate.getFullYear();
	const month = currentDate.getMonth() + 1;
	document.getElementById('currentMonthLabel').textContent = `${year}年${month}月`;
}

// ▼ 勤怠データを再描画
function refreshTable() {
	updateMonthLabel();
	const year = currentDate.getFullYear();
	const month = currentDate.getMonth();
	const data = generateDummyData(year, month);
	createTable(data);

	//	calculateLeaveSummary(data);  
}

// 例: "08:30" → "8.50" などの小数時間に変換
function timeToDecimalString(timeStr) {
	if (!timeStr || !timeStr.includes(':')) return "0.00";
	const [hours, minutes] = timeStr.split(':').map(Number);
	const decimal = hours + minutes / 60;
	return decimal.toFixed(2);  // 例: 8.50
}


function setOriginalValues() {
	document.querySelectorAll('#kintaiTable tbody tr').forEach(row => {
		const inputs = row.querySelectorAll('input, select');
		inputs.forEach(input => {
			input.setAttribute('data-original', input.value);
		});
	});
}



// ▼ 集計結果表示用（例）
function renderLeaveSummary(summary) {
	// 例：有給休暇の表示要素がある場合
	const paidLeaveElem = document.getElementById('paidLeaveDays');
	paidLeaveElem.textContent =
		summary['有給休暇'] !== undefined
			? summary['有給休暇'].toFixed(1)
			: '0.0';
	// 他の休暇も同様に必要あれば対応
}

function recalcRow(tr) {
	const start = tr.querySelector('input[name="actualWorkStartTime"]').value;
	const end = tr.querySelector('input[name="actualWorkEndTime"]').value;
	const breakStart = tr.querySelector('input[name="actualBreakStartTime"]').value;
	const breakEnd = tr.querySelector('input[name="actualBreakEndTime"]').value;

	const workMin = (start && end) ? timeStrToMinutes(end) - timeStrToMinutes(start) : 0;
	const breakMin = (breakStart && breakEnd) ? timeStrToMinutes(breakEnd) - timeStrToMinutes(breakStart) : 0;
	const actualMin = Math.max(0, workMin - breakMin);

	// 所定は予定時間から
	const plannedStart = tr.querySelector('input[name="plannedWorkStartTime"]').value;
	const plannedEnd = tr.querySelector('input[name="plannedWorkEndTime"]').value;
	const plannedBreakStart = tr.querySelector('input[name="plannedBreakStartTime"]').value;
	const plannedBreakEnd = tr.querySelector('input[name="plannedBreakEndTime"]').value;
	const plannedWorkMin = (plannedStart && plannedEnd) ? timeStrToMinutes(plannedEnd) - timeStrToMinutes(plannedStart) : 0;
	const plannedBreakMin = (plannedBreakStart && plannedBreakEnd) ? timeStrToMinutes(plannedBreakEnd) - timeStrToMinutes(plannedBreakStart) : 0;
	const scheduledMin = plannedWorkMin - plannedBreakMin;

	const overtimeMin = Math.max(0, actualMin - scheduledMin);

	// 結果を反映
	tr.querySelector('input[name="scheduledWorkHours"]').value = minutesToTimeStr(scheduledMin);
	tr.querySelector('input[name="actualWorkHours"]').value = minutesToTimeStr(actualMin);
	tr.querySelector('input[name="overtimeHours"]').value = minutesToTimeStr(overtimeMin);
	tr.querySelector('input[name="deductionTime"]').value =
		minutesToTimeStr(Math.max(0, scheduledMin - actualMin));
}

function attachRecalcEvents() {
	document.querySelectorAll('#kintaiTable tbody tr').forEach(tr => {
		const inputs = tr.querySelectorAll('input[type="time"]');
		inputs.forEach(input => {
			input.addEventListener('input', () => {
				setTimeout(() => recalcRow(tr), 0); // 非同期で実行
			});
		});
	});
}


function attachRecalcEvents() {
	document.querySelectorAll('#kintaiTable tbody tr').forEach(tr => {
		const inputs = tr.querySelectorAll('input[type="time"]');
		inputs.forEach(input => {
			input.addEventListener('input', () => {
				setTimeout(() => recalcRow(tr), 0); // 非同期で実行
			});
		});
	});
}


// --- DB送信用：分 → 小数時間（DECIMAL）変換 ---
function minutesToDecimalHours(min) {
	return +(min / 60).toFixed(2); // 小数点2桁で丸め
}




function saveKintaiData() {
	if (!isChanged()) {
		alert('変更された行はありません');
		return;
	}

	const userId = targetUserId;
	const rows = document.querySelectorAll('#kintaiTable tbody tr');
	let newKintaiList = [];

	let counts = {
		attendance: 0, absent: 0, paidLeave: 0, substituteLeave: 0,
		compensatoryLeave: 0, bereavementLeave: 0, sickLeave: 0,
		maternityLeave: 0, childcareLeave: 0, otherLeave: 0
	};

	rows.forEach((row) => {
		const cells = row.querySelectorAll('td');
		const inputs = row.querySelectorAll('input, select');
		if (inputs.length < 14) return;

		// --- 記入行のみ ---
		const timeAndCommentInputs = Array.from(inputs).slice(0, 12);
		const commentInput = inputs[13];
		const anyFilled = timeAndCommentInputs.some(input => {
			const v = (input.value || '').trim();
			return v !== '' && v !== '00:00' && v !== 'nothing';
		}) || (commentInput && commentInput.value && commentInput.value.trim() !== '');
		if (!anyFilled) return;

		// --- 時間計算 ---
		const plannedWorkMin = (inputs[1].value && inputs[0].value)
			? timeStrToMinutes(inputs[1].value) - timeStrToMinutes(inputs[0].value)
			: 0;

		const plannedBreakMin = (inputs[3].value && inputs[2].value)
			? timeStrToMinutes(inputs[3].value) - timeStrToMinutes(inputs[2].value)
			: 0;

		let scheduledMin = plannedWorkMin - plannedBreakMin;

		const actualWorkMin = (inputs[5].value && inputs[4].value)
			? timeStrToMinutes(inputs[5].value) - timeStrToMinutes(inputs[4].value)
			: 0;

		const actualBreakMin = (inputs[7].value && inputs[6].value)
			? timeStrToMinutes(inputs[7].value) - timeStrToMinutes(inputs[6].value)
			: 0;

		let actualMin = actualWorkMin - actualBreakMin;

		let adjustedActualMin = actualMin;
		let adjustedOvertimeMin = Math.max(0, actualMin - scheduledMin);
		let deductionMin = Math.max(0, scheduledMin - adjustedActualMin);

		switch (inputs[12].value) {
			case 'paid_leave_half':
				adjustedActualMin = actualMin / 2;
				adjustedOvertimeMin = 0;
				deductionMin = scheduledMin - adjustedActualMin;
				counts.paidLeave += 0.5;
				break;

			case 'paid_leave_full':
			case 'substitute_leave':
			case 'compensatory_leave':
			case 'bereavement_leave':
			case 'sick_leave':
			case 'maternity_leave':
			case 'childcare_leave':
			case 'other_leave':
				adjustedActualMin = 0;
				adjustedOvertimeMin = 0;
				deductionMin = 0;
				const leaveKey = inputs[12].value.replace(/_leave$/, '');
				if (counts.hasOwnProperty(leaveKey)) counts[leaveKey] += 1;
				break;

			default:
				deductionMin = Math.max(0, scheduledMin - adjustedActualMin);
				if (inputs[12].value === 'nothing' && actualMin > 0) counts.attendance += 1;
				break;
		}

		// ✅ ここが最重要：Decimalで送る
		const entry = {
			userId: userId,
			workDate: cells[0].textContent.trim(),
			userName: cells[1].textContent.trim(),

			plannedWorkStartTime: inputs[0].value,
			plannedWorkEndTime: inputs[1].value,
			plannedBreakStartTime: inputs[2].value,
			plannedBreakEndTime: inputs[3].value,

			actualWorkStartTime: inputs[4].value,
			actualWorkEndTime: inputs[5].value,
			actualBreakStartTime: inputs[6].value,
			actualBreakEndTime: inputs[7].value,

			// 🔥 ここ変更
			scheduledWorkHours: minutesToDecimalHours(scheduledMin),
			actualWorkHours: minutesToDecimalHours(adjustedActualMin),
			overtimeHours: minutesToDecimalHours(adjustedOvertimeMin),
			deductionTime: minutesToDecimalHours(deductionMin),

			kintaiStatus: inputs[12].value,
			kintaiComment: inputs[13].value,
			updatedBy: sessionUserName || targetUserName
		};

		newKintaiList.push(entry);
	});

	counts.absent = counts.paidLeave + counts.substituteLeave + counts.compensatoryLeave +
		counts.bereavementLeave + counts.sickLeave + counts.maternityLeave +
		counts.childcareLeave + counts.otherLeave;

	if (!newKintaiList || newKintaiList.length === 0) {
		alert('変更された行がありません');
		return;
	}

	const csrfMeta = document.querySelector('meta[name="_csrf"]');
	const csrfToken = csrfMeta ? csrfMeta.content : '';

	fetch('/kintai/api/save-list/' + encodeURIComponent(userId), {
		method: 'POST',
		headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken },
		body: JSON.stringify(newKintaiList)
	})
		.then(res => res.json())
		.then(json => {

			if (json.status === "error") {
				let msg = json.message || "保存に失敗しました";
				if (json.errors && json.errors.length > 0) {
					msg += "\n\n" + json.errors.join("\n");
				}
				alert(msg);
				return;
			}

			alert('保存成功！');

			refreshTable();
			setTimeout(setOriginalValues, 100);

			editBtn.style.display = 'inline';
			saveBtn.style.display = 'none';
			cancelBtn.style.display = 'none';

			document.querySelectorAll('input, select').forEach(el => el.disabled = true);
			isEditing = false;
		})
		.catch(err => {
			console.error('保存失敗:', err);
			alert('通信エラーが発生しました');
		});
}
// ▼ 月移動イベントと初期表示
document.addEventListener('DOMContentLoaded', () => {
	// 1. 月移動ボタン
	['prevMonth', 'nextMonth'].forEach(id => {
		const btn = document.getElementById(id);
		if (btn) {
			btn.addEventListener('click', () => {
				currentDate.setMonth(currentDate.getMonth() + (id === 'prevMonth' ? -1 : 1));
				refreshTable();
				setTimeout(setOriginalValues, 100);
				fetchStatusAndUpdate(); // 月変更時にステータス更新
			});
		}
	});

	// 2. 初期表示
	refreshTable();
	setTimeout(setOriginalValues, 100);

	// 3. 勤怠申請ボタン制御
	handleAction("applyBtn", "applyForm", null, "申請しますか？");
	handleAction("reapplyBtn", "applyForm", null, "再申請しますか？");
	handleAction("approveBtn", "approveForm", null, "承認しますか？");
	handleAction("rejectBtn", "rejectForm", "rejectComment", "差戻しますか？");

	// 4. ステータス取得 & ボタン更新
	fetchStatusAndUpdate();

	// 5. 保存ボタン
	document.getElementById('saveBtn').addEventListener('click', saveKintaiData);
});
// ▼ 勤怠申請ボタン制御（バックアップ＋fetch対応）
function updateApplicationButtons(status) {
	const applyBtn = document.getElementById("applyBtn");
	const reapplyBtn = document.getElementById("reapplyBtn");
	const approveBtn = document.getElementById("approveBtn");
	const rejectBtn = document.getElementById("rejectBtn");

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
}
// ▼ ステータス取得 & ボタン更新
function fetchStatusAndUpdate() {
	const yearMonth =
		currentDate.getFullYear() + "-" +
		String(currentDate.getMonth() + 1).padStart(2, "0");
	fetch(`/kintai/status?userId=${targetUserId}&yearMonth=${yearMonth}`)
		.then(res => res.json())
		.then(data => {
			console.log("取得したstatus:", data);
			updateApplicationButtons(data.status);
			const statusSpan = document.querySelector('#statusSpan');
			if (statusSpan) {
				statusSpan.textContent = ({
					APPLYING: '申請中',
					REJECTED: '差戻中',
					APPROVED: '承認済'
				})[data.status] || '未申請';
			}
		})
		.catch(e => console.error("status取得エラー:", e));
}

// ▼ 申請・承認フロー共通処理
// ▼ 申請・承認フロー共通処理
function handleAction(buttonId, formId, commentInputId, confirmMsg) {
	const btn = document.getElementById(buttonId);
	if (!btn) return;

	btn.addEventListener("click", async () => {
		if (!confirm(confirmMsg)) return;

		let comment = "";
		comment = prompt("コメントを入力してください", commentInputId ? document.getElementById(commentInputId)?.value || "" : "");
		if (comment === null) return;

		if (commentInputId) {
			const commentInput = document.getElementById(commentInputId);
			if (commentInput) commentInput.value = comment;
		}

		try {
			const form = document.getElementById(formId);
			if (!form) throw new Error("フォームが見つかりません");

			const formData = new FormData(form);
			formData.set("comment", comment);

			const params = new URLSearchParams(formData);

			const response = await fetch(form.action, {
				method: "POST",
				body: params,
			});

			if (!response.ok) throw new Error(`HTTPエラー: ${response.status}`);

			let data = {};
			try { data = await response.json(); }
			catch (e) { console.warn("JSON変換失敗:", e); }

			console.log(buttonId + " 結果:", data);

			updateApplicationButtons?.(data.status || "");
			const statusSpan = document.querySelector("#statusSpan");
			if (statusSpan) {
				statusSpan.textContent = ({
					APPLYING: '申請中',
					REJECTED: '差戻中',
					APPROVED: '承認済'
				})[data.status] || '未申請';
			}

		} catch (err) {
			console.error(buttonId + " エラー:", err);
			alert("通信に失敗しました。再度お試しください。");
		}
	});
}


// ★完全に外に置く（ここ重要）
function isChanged() {
	const trList = document.querySelectorAll('#kintaiTable tbody tr');

	for (let i = 0; i < trList.length; i++) {
		const inputs = trList[i].querySelectorAll('input, select');

		for (let j = 0; j < inputs.length; j++) {
			const currentValue = inputs[j].value;
			const originalValue = originalData[i]?.[j];

			if (currentValue !== originalValue) {
				return true;
			}
		}
	}

	return false;
}





