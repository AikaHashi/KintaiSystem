'use strict';

const KintaiApp = (function () {
  function init() {
    document.addEventListener('DOMContentLoaded', function () {
      initCalendar();
      initSearchButton();
    });
  }

  function initSearchButton() {
    const searchBtn = document.getElementById('btn-search');
    if (searchBtn) {
      searchBtn.addEventListener('click', search);
    }
  }

  function initCalendar() {
  const calendarEl = document.getElementById('kintai-calendar');
  if (!calendarEl) return;

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    locale: 'ja',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth'
    },

    // ★これがカレンダーにDB反映する本体
  events: function (fetchInfo, successCallback, failureCallback) {

  fetch(`/kintai/events?start=${fetchInfo.startStr}&end=${fetchInfo.endStr}`)
    .then(res => res.json())
    .then(data => successCallback(data))
    .catch(err => failureCallback(err));
},
    dateClick: function (info) {
      loadKintaiTable(info.dateStr);
    }
  });

  calendar.render();

  const today = new Date();
  const todayStr = today.toISOString().split('T')[0];
  loadKintaiTable(todayStr);
}

  function loadKintaiTable(dateStr) {
    const container = document.getElementById('kintai-table-container');
    if (!container) return;

    const loginUserName = document.body.dataset.username || '不明なユーザー';
    const loginUserId = document.body.dataset.userid || '';

    const tableHtml = `
      <form id="kintai-form">
        <h3>${dateStr} の勤怠</h3>
        <button type="button" id="btn-save">保存</button>
        <table border="1" style="width: 100%;">
          <thead>
            <tr>
              <th></th><th colspan="2">予定</th><th colspan="8">実績</th>
            </tr>
            <tr>
              <th>社員名</th><th>勤務時間</th><th>休憩時間</th>
              <th>勤務時間</th><th>休憩時間</th>
              <th>所定時間</th><th>実働時間</th>
              <th>時間外</th><th>控除</th><th>状態</th><th>特記事項</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span id="userName"></span></td>
              <td><input type="time" name="plannedWorkStartTime" />～<input type="time" name="plannedWorkEndTime" /></td>
              <td><input type="time" name="plannedBreakStartTime" />～<input type="time" name="plannedBreakEndTime" /></td>
              <td><input type="time" name="actualWorkStartTime" />～<input type="time" name="actualWorkEndTime" /></td>
              <td><input type="time" name="actualBreakStartTime" />～<input type="time" name="actualBreakEndTime" /></td>
              <td><input type="text" name="scheduledWorkHours" readonly /></td>
              <td><input type="text" name="actualWorkHours" readonly /></td>
              <td><input type="text" name="overtimeHours" readonly /></td>
              <td><input type="text" name="deductionTime" readonly /></td>
              <td>
                <select name="kintaiStatus" id="kintai-status">
                  <option value="nothing">なし</option>
                  <option value="paid_leave_full">有給休暇(全休)</option>
                  <option value="paid_leave_half">有給休暇(半休)</option>
                  <option value="substitute_leave">振替休暇</option>
                  <option value="compensatory_leave">代休</option>
                  <option value="work_related_illness">業務上の疾病による休業</option>
                  <option value="maternity_leave">産前産後休業</option>
                  <option value="childcare_leave">育児休業</option>
                  <option value="other_leave">その他休業</option>
                </select>
              </td>
              <td><input type="text" name="kintaiComment" /></td>
            </tr>
          </tbody>
        </table>
        <input type="hidden" name="userId" value="${loginUserId}" id="formUserId" />
        <input type="hidden" name="userName" id="formUserName" value="${loginUserName}" />
        <input type="hidden" name="workDate" value="${dateStr}" />
      </form>
    `;

    container.innerHTML = tableHtml;

    document.getElementById('userName').textContent = loginUserName;

    setupStatusBehavior();
    setupTimeAutoCalculation();

    const saveBtn = document.getElementById('btn-save');
    if (saveBtn) {
      saveBtn.addEventListener('click', async function () {
        const form = document.getElementById('kintai-form');
        if (!form) return;
        if (!validateForm(form)) return;

        const formData = new FormData(form);
        const jsonData = {};
        formData.forEach((v, k) => jsonData[k] = v);

        try {
          const res = await fetch('/kintai/api/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(jsonData)
          });
          if (!res.ok) throw new Error('サーバーエラー');
          const result = await res.json();
          alert(result.message || '保存しました');
        } catch (err) {
          alert('保存に失敗しました: ' + err.message);
        }
      });
    }
    fetch(`/kintai/api/data?date=${dateStr}`)
  .then(res => {
    if (!res.ok) throw new Error('取得失敗');
    return res.json();
  })
  .then(data => {
    if (!data) return;

    const form = document.getElementById('kintai-form');
    if (!form) return;

    const setTime = (name, value) => {
      if (value && value.length >= 5) form.elements[name].value = value.substring(0, 5);
    };

    setTime('plannedWorkStartTime', data.plannedWorkStartTime);
    setTime('plannedWorkEndTime', data.plannedWorkEndTime);
    setTime('plannedBreakStartTime', data.plannedBreakStartTime);
    setTime('plannedBreakEndTime', data.plannedBreakEndTime);
    setTime('actualWorkStartTime', data.actualWorkStartTime);
    setTime('actualWorkEndTime', data.actualWorkEndTime);
    setTime('actualBreakStartTime', data.actualBreakStartTime);
    setTime('actualBreakEndTime', data.actualBreakEndTime);

    form.elements['kintaiStatus'].value = data.kintaiStatus || 'nothing';
    form.elements['kintaiComment'].value = data.kintaiComment || '';

    // 計算系再反映
    updateCalculatedFields();
  })
  .catch(err => {
    console.warn('勤怠データ取得エラー: ', err);
  });
  }

  function validateForm(form) {
    const start = form.elements['actualWorkStartTime'].value;
    const end = form.elements['actualWorkEndTime'].value;

    if (start && end && start > end) {
      alert('開始時間は終了時間より前にしてください');
      return false;
    }

    return true;
  }

  function search() {
    alert('検索機能は未実装です');
  }

 function setupStatusBehavior() {
  const status = document.getElementById('kintai-status');
  status.addEventListener('change', function () {
    const form = document.getElementById('kintai-form');
    if (!form) return;

    // フォーム内の全ての input[type="time"], input[type="text"], select を活性化する
    const fields = form.querySelectorAll('input[type="time"], input[type="text"], select');

    fields.forEach(field => {
      // userId, userName, workDateのhiddenは変更しない
      if (['userId', 'userName', 'workDate'].includes(field.name)) return;

      field.disabled = false;  // 全部活性化
    });
    // ★ 状態変更時に計算を再実行（これが必要！）★
    updateCalculatedFields();
  });
}

  function setupTimeAutoCalculation() {
    const inputs = document.querySelectorAll('input[type="time"]');
    inputs.forEach(input => {
      input.addEventListener('change', updateCalculatedFields);
    });
  }

  function updateCalculatedFields() {
  const form = document.getElementById('kintai-form');
  if (!form) return;

  // 予定時間
  const plannedStart = form.elements['plannedWorkStartTime'].value;
  const plannedEnd = form.elements['plannedWorkEndTime'].value;
  const plannedBreakStart = form.elements['plannedBreakStartTime'].value;
  const plannedBreakEnd = form.elements['plannedBreakEndTime'].value;

  // 実績時間
  const actualStart = form.elements['actualWorkStartTime'].value;
  const actualEnd = form.elements['actualWorkEndTime'].value;
  const actualBreakStart = form.elements['actualBreakStartTime'].value;
  const actualBreakEnd = form.elements['actualBreakEndTime'].value;

  let plannedWorkMinutes = 0;
  let plannedBreakMinutes = 0;
  if (plannedStart && plannedEnd && plannedStart < plannedEnd) {
    plannedWorkMinutes = getMinutesDiff(plannedStart, plannedEnd);
  }
  if (plannedBreakStart && plannedBreakEnd && plannedBreakStart < plannedBreakEnd) {
    plannedBreakMinutes = getMinutesDiff(plannedBreakStart, plannedBreakEnd);
  }
  const scheduledMinutes = plannedWorkMinutes - plannedBreakMinutes;

  let actualWorkMinutes = 0;
  let actualBreakMinutes = 0;
  if (actualStart && actualEnd && actualStart < actualEnd) {
    actualWorkMinutes = getMinutesDiff(actualStart, actualEnd);
  }
  if (actualBreakStart && actualBreakEnd && actualBreakStart < actualBreakEnd) {
    actualBreakMinutes = getMinutesDiff(actualBreakStart, actualBreakEnd);
  }
  const actualMinutes = actualWorkMinutes - actualBreakMinutes;

  // 状態による控除対象時間
  const status = form.elements['kintaiStatus'].value;
  let statusMinutes = 0;
  if (status === 'paid_leave_half') {
    statusMinutes = scheduledMinutes / 2;
  } else if (status !== 'nothing') {
    statusMinutes = scheduledMinutes;
  }

  // 時間外 = 実働 - 所定 （マイナスなら 0）
  const overtimeMinutes = Math.max(0, actualMinutes - scheduledMinutes);

  // 控除 = 所定 - 実働 - 状態（プラスなら）マイナスなら 0
  let deductionMinutes = scheduledMinutes - actualMinutes - statusMinutes;
  deductionMinutes = Math.max(0, deductionMinutes);

  // 値のセット
  form.elements['scheduledWorkHours'].value = formatHours(scheduledMinutes);
  form.elements['actualWorkHours'].value = formatHours(actualMinutes);
  form.elements['overtimeHours'].value = formatHours(overtimeMinutes);
  form.elements['deductionTime'].value = formatHours(deductionMinutes);
  
 calculateLeaveSummary();
 
}

function calculateLeaveSummary() {
  const form = document.getElementById('kintai-form');
  if (!form) return;

  const status = form.elements['kintaiStatus'].value;
  const scheduled = getMinutes(form.elements['scheduledWorkHours'].value);
  const actual = getMinutes(form.elements['actualWorkHours'].value);

  // 休暇種別と集計項目のマッピング
  const summaryMap = {
    paid_leave_full: '有給休暇',
    paid_leave_half: '有給休暇',
    substitute_leave: '振替休暇',
    compensatory_leave: '代休',
    work_related_illness: '業務上の疾病による休業',
    maternity_leave: '産前産後休業',
    childcare_leave: '育児休業',
    other_leave: 'その他休業'
  };

  // 既存 summary を維持して加算する
  let currentSummary = window.leaveSummary || {};

  if (status === 'paid_leave_full') {
    currentSummary['有給休暇'] = (currentSummary['有給休暇'] || 0) + 1;
  } else if (status === 'paid_leave_half') {
    currentSummary['有給休暇'] = (currentSummary['有給休暇'] || 0) + 0.5;
  } else if (summaryMap[status]) {
    const label = summaryMap[status];
    let val = 1;
    if (scheduled > 0) {
      const ratio = actual / scheduled;
      if (ratio > 0 && ratio <= 0.5) {
        val = 0.5;
      }
    }
    currentSummary[label] = (currentSummary[label] || 0) + val;
  }

  // グローバルに保持（連続集計対応）
  window.leaveSummary = currentSummary;

  renderLeaveSummary(currentSummary);
}

function getMinutes(timeStr) {
  const num = parseFloat(timeStr);
  return isNaN(num) ? 0 : Math.round(num * 60);
}

  function getMinutesDiff(startTime, endTime) {
    const [sh, sm] = startTime.split(':').map(Number);
    const [eh, em] = endTime.split(':').map(Number);
    return (eh * 60 + em) - (sh * 60 + sm);
  }

  function formatHours(minutes) {
    return (minutes / 60).toFixed(2);
  }

  return { init };
})();

KintaiApp.init();
