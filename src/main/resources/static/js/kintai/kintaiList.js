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

// ▼ 勤怠データ生成
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

    const plannedWorkMin = (dto.plannedWorkStartTime && dto.plannedWorkEndTime)
      ? timeStrToMinutes(dto.plannedWorkEndTime) - timeStrToMinutes(dto.plannedWorkStartTime)
      : 0;

    const plannedBreakMin = (dto.plannedBreakStartTime && dto.plannedBreakEndTime)
      ? timeStrToMinutes(dto.plannedBreakEndTime) - timeStrToMinutes(dto.plannedBreakStartTime)
      : 0;

    const scheduledMin = plannedWorkMin - plannedBreakMin;

    const actualWorkMin = (dto.actualWorkStartTime && dto.actualWorkEndTime)
      ? timeStrToMinutes(dto.actualWorkEndTime) - timeStrToMinutes(dto.actualWorkStartTime)
      : 0;

    const actualBreakMin = (dto.actualBreakStartTime && dto.actualBreakEndTime)
      ? timeStrToMinutes(dto.actualBreakEndTime) - timeStrToMinutes(dto.actualBreakStartTime)
      : 0;

    const actualMin = actualWorkMin - actualBreakMin;

    let stateDeductMin = 0;
    if (dto.kintaiStatus === 'paid_leave_half') {
      stateDeductMin = scheduledMin / 2;
    } else if (dto.kintaiStatus && dto.kintaiStatus !== 'nothing') {
      stateDeductMin = scheduledMin;
    }

    // 出勤・休暇日数集計（省略：既存コードそのまま）

    switch (dto.kintaiStatus) {
      case 'paid_leave_full':
        counts.paidLeave += 1;
        break;
      case 'paid_leave_half':
        counts.paidLeave += 0.5;
        break;
      case 'substitute_leave':
      case 'compensatory_leave':
      case 'work_related_illness':
      case 'maternity_leave':
      case 'childcare_leave':
      case 'other_leave': {
        if (scheduledMin > 0) {
          const ratio = actualMin / scheduledMin;
          let value = 0;
          if (ratio === 0) {
            value = 1;
          } else if (ratio > 0 && ratio <= 0.5) {
            value = 0.5;
          } else {
            value = 1;
          }
          switch (dto.kintaiStatus) {
            case 'substitute_leave':
              counts.substituteLeave += value;
              break;
            case 'compensatory_leave':
              counts.compensatoryLeave += value;
              break;
            case 'work_related_illness':
              counts.sickLeave += value;
              break;
            case 'maternity_leave':
              counts.maternityLeave += value;
              break;
            case 'childcare_leave':
              counts.childcareLeave += value;
              break;
            case 'other_leave':
              counts.otherLeave += value;
              break;
          }
        }
        break;
      }
      case 'bereavement_leave':
        counts.bereavementLeave += 1;
        break;
    }

    // ▼ 時間集計ロジック

    // actualHours の計算
    let adjustedActualMin;
    if (dto.kintaiStatus === 'paid_leave_full') {
      adjustedActualMin = 0;
    } else if (dto.kintaiStatus === 'paid_leave_half') {
      adjustedActualMin = actualMin / 2;
    } else if (dto.kintaiStatus && dto.kintaiStatus !== 'nothing') {
      if (scheduledMin > 0 && actualMin >= 0 && actualMin <= scheduledMin / 2) {
        adjustedActualMin = actualMin / 2;
      } else {
        adjustedActualMin = actualMin;
      }
    } else {
      adjustedActualMin = actualMin;
    }

    // breakTime の計算
    const adjustedBreakMin = dto.kintaiStatus && dto.kintaiStatus !== 'nothing' ? 0 : actualBreakMin;

    // overtime の計算
    const adjustedOvertimeMin = dto.kintaiStatus && dto.kintaiStatus !== 'nothing' ? 0 : Math.max(0, actualMin - scheduledMin);

   // holidayHours の計算（分単位で計算し、最後に時間に直して加算）
let holidayMinutes = 0;

if (dto.kintaiStatus === 'paid_leave_full') {
  holidayMinutes = actualMin;

} else if (dto.kintaiStatus === 'paid_leave_half') {
  holidayMinutes = actualMin / 2;

} else if (dto.kintaiStatus && dto.kintaiStatus !== 'nothing') {
  if (scheduledMin > 0 && actualMin >= 0 && actualMin <= scheduledMin / 2) {
    holidayMinutes = actualMin / 2;
  } else {
    holidayMinutes = actualMin;
  }
}
// それ以外（nothing や undefined）は 0 のまま

    const overtimeMin = Math.max(0, actualMin - scheduledMin);
    const deductionMin = Math.max(0, scheduledMin - actualMin - stateDeductMin);

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
      scheduledWorkHours: minutesToTimeStr(scheduledMin),
      actualWorkHours: minutesToTimeStr(actualMin),
      overtimeHours: minutesToTimeStr(overtimeMin),
      deductionTime: minutesToTimeStr(deductionMin),
      kintaiStatus: dto.kintaiStatus || 'nothing',
      kintaiComment: dto.kintaiComment || ''
    };

    data.push(entry);

    // 集計への加算
    counts.scheduledHours += scheduledMin / 60;
    counts.actualHours += adjustedActualMin / 60;
    counts.breakTime += adjustedBreakMin / 60;
    counts.overtime += adjustedOvertimeMin / 60;
    counts.deductions += deductionMin / 60;
    counts.holidayHours += holidayMinutes / 60;
  }

 // 実働がある 'nothing' ステータスを出勤扱いに含める
let statusNothingAttendanceCount = 0;

data.forEach(entry => {
  if (entry.kintaiStatus === 'nothing') {
    const actualMinutes = timeStrToMinutes(entry.actualWorkHours);
    if (actualMinutes > 0) {
      statusNothingAttendanceCount++;
    }
  }
});

const totalAbsent =
  counts.paidLeave +
  counts.substituteLeave +
  counts.compensatoryLeave +
  counts.sickLeave +
  counts.maternityLeave +
  counts.childcareLeave +
  counts.otherLeave;

const statusNothingCount = data.filter(entry => entry.kintaiStatus === 'nothing').length;

// 出勤日数：実働がある 'nothing' は attendance に加算、それ以外は欠勤扱い
counts.absent = totalAbsent;
counts.attendance = daysInMonth - totalAbsent - (statusNothingCount - statusNothingAttendanceCount);


  // 結果反映
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
  console.log("entryの中身:", entry);
});
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
    table.appendChild(thead);  // ← これが必要
table.appendChild(tbody);  // ← これも必要
 document.getElementById('tableContainer').appendChild(table); // 必ずDOMに追加
  });

//  table.appendChild(thead);
//  table.appendChild(tbody);

//  const container = document.getElementById('tableContainer');
//  container.innerHTML = '';
//  container.appendChild(table);
}



const editBtn = document.getElementById('editBtn');
const saveBtn = document.getElementById('saveBtn');
const cancelBtn = document.getElementById('cancelBtn');

let originalData = [];

// 編集ボタン押下時の処理（originalDataを行単位の配列で保存）
editBtn.addEventListener('click', () => {
  const trList = document.querySelectorAll('#kintaiTable tbody tr');

  originalData = Array.from(trList).map(tr => {
    const inputs = tr.querySelectorAll('input, select');
    return Array.from(inputs).map(input => input.value);
  });

  console.log('originalData保存:', originalData);

  trList.forEach(tr => {
    const inputs = tr.querySelectorAll('input, select');
    inputs.forEach(input => {
      if (!input.readOnly) input.disabled = false;
    });

    // もしステータス選択のイベントリスナーが必要ならここに
  });

  editBtn.style.display = 'none';
  saveBtn.style.display = 'inline';
  cancelBtn.style.display = 'inline';
});

// 編集開始時
editBtn.addEventListener('click', () => {
  const trList = document.querySelectorAll('#kintaiTable tbody tr');

  // 2次元配列で保存
  originalData = Array.from(trList).map(tr => {
    const inputs = tr.querySelectorAll('input, select');
    return Array.from(inputs).map(input => input.value);
  });

  // 全ての入力欄を編集可能にする
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

// キャンセル時
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
});
//saveBtn.addEventListener('click', () => {
//	const inputs = document.querySelectorAll('input');
//	inputs.forEach(input => {
//		if (!input.readOnly) input.disabled = true;
//	});
//
//	// ここで保存処理（API送信など）を行ってもよい
//
//	editBtn.style.display = 'inline';
//	saveBtn.style.display = 'none';
//	cancelBtn.style.display = 'none';
//});


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

//// ▼ 休暇種別ごとの集計を行う関数
//function calculateLeaveSummary() {
//  // 勤怠フォーム要素取得（例として1行フォームの要素を想定。複数行の場合はループなど必要）
//  const form = document.getElementById('kintai-form');
//  if (!form) return;
//
//  // 状態と時間（所定・実働）取得
//  const status = form.elements['kintaiStatus']?.value;
//  const scheduledStr = form.elements['scheduledWorkHours']?.value;
//  const actualStr = form.elements['actualWorkHours']?.value;
//  const scheduled = scheduledStr ? timeStrToMinutes(scheduledStr) : 0;
//  const actual = actualStr ? timeStrToMinutes(actualStr) : 0;
//
//  // 休暇ラベル対応表
//  const summaryMap = {
//    paid_leave_full: '有給休暇',
//    paid_leave_half: '有給休暇',
//    substitute_leave: '振替休暇',
//    compensatory_leave: '代休',
//    work_related_illness: '業務上の疾病による休業',
//    maternity_leave: '産前産後休業',
//    childcare_leave: '育児休業',
//    other_leave: 'その他休業'
//  };

  // グローバル集計オブジェクトがなければ初期化
//  if (!window.leaveSummary) window.leaveSummary = {};

//  let val = 0;
//  if (status === 'paid_leave_full') {
//    val = 1;
//  } else if (status === 'paid_leave_half') {
//    val = 0.5;
//  } else if (summaryMap[status]) {
//    // 所定時間が0以外の場合は実働/所定の割合で0.5 or 1を判定
//    if (scheduled > 0 && actual > 0 && actual / scheduled <= 0.5) {
//      val = 0.5;
//    } else {
//      val = 1;
//    }
//  }
//
//  if (val > 0) {
//    const label = summaryMap[status];
//    window.leaveSummary[label] = (window.leaveSummary[label] || 0) + val;
//  }
//
//  // 集計結果を画面に反映する関数を呼ぶ（必要に応じて実装）
//  renderLeaveSummary(window.leaveSummary);
//}

// ▼ 集計結果表示用（例）
function renderLeaveSummary(summary) {
  // 例：有給休暇の表示要素がある場合
  const paidLeaveElem = document.getElementById('summaryPaidLeave');
  if (paidLeaveElem) {
    paidLeaveElem.textContent = summary['有給休暇'] ? summary['有給休暇'].toFixed(1) : '0.0';
  }
  // 他の休暇も同様に必要あれば対応
}


function saveKintaiData() {
  const userId = targetUserId;
  const rows = document.querySelectorAll('#kintaiTable tbody tr');
  const newKintaiList = [];

  rows.forEach(row => {
    const cells = row.querySelectorAll('td');
    const inputs = row.querySelectorAll('input, select');
    if (inputs.length < 14) return;

    // 入力項目（index 0～13）だけ比較
    const isModified = [...inputs].slice(0, 14).some(inp => {
      const original = (inp.getAttribute('data-original') || '').trim();
      const current = (inp.value || '').trim();

      // "00:00"と""を等価として扱う
      return (original === '' && current === '00:00') || (current === '' && original === '00:00')
        ? false
        : original !== current;
    });

    if (!isModified) return;

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
      scheduledWorkHours: timeToDecimalString(inputs[8].value),
      actualWorkHours: timeToDecimalString(inputs[9].value),
      overtimeHours: timeToDecimalString(inputs[10].value),
      deductionTime: timeToDecimalString(inputs[11].value),
      kintaiStatus: inputs[12].value,
      kintaiComment: inputs[13].value,
      updatedBy: sessionUserId || targetUserId
    };

    newKintaiList.push(entry);
  });

  if (newKintaiList.length === 0) {
    alert('変更された行がありません');
    setOriginalValues()
    return;
  }

  fetch('/kintai/api/save-list/' + encodeURIComponent(userId), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(newKintaiList)
  })
    .then(async res => {
      const contentType = res.headers.get("content-type");
      const text = await res.text();
      if (!res.ok) throw new Error(`HTTP error ${res.status}: ${text}`);
      if (contentType && contentType.includes("application/json")) {
        return JSON.parse(text);
      } else {
        throw new Error("サーバーからJSON形式でレスポンスが返されませんでした");
      }
    })
    .then(json => {
      alert('保存成功！');
      console.log('保存されたデータ:', json);
      
    })
    .catch(err => {
      alert('保存失敗: ' + err.message);
      console.error('保存失敗:', err);
      
    });
setOriginalValues()
  console.log('保存する勤怠データ:', newKintaiList);
  kintaiListJson = newKintaiList;

  document.querySelectorAll('input, select').forEach(el => el.disabled = true);
  //document.getElementById('saveBtn').textContent = '編集';
  isEditing = false;
}

// ▼ 月移動イベントと初期表示
document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('prevMonth').addEventListener('click', () => {
    currentDate.setMonth(currentDate.getMonth() - 1);
    refreshTable();
    setTimeout(setOriginalValues, 100);
  });

  document.getElementById('nextMonth').addEventListener('click', () => {
    currentDate.setMonth(currentDate.getMonth() + 1);
    refreshTable();
    setTimeout(setOriginalValues, 100);
  });

  refreshTable();
  setTimeout(setOriginalValues, 100);

  document.getElementById('saveBtn').addEventListener('click', () => {
    console.log('保存ボタンが押されました');
    try {
      saveKintaiData();
    } catch (e) {
      console.error('保存中にエラー発生:', e);
    }
  });
});