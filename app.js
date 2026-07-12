/**
 * IoT Patient Health Monitoring System — Patient Mobile App
 * Core Application Logic & State Management (MD3 Edition)
 */

document.addEventListener('DOMContentLoaded', () => {
  
  // ==========================================================================
  // STATE MANAGEMENT
  // ==========================================================================
  const state = {
    user: {
      name: 'Alexander Pierce',
      id: 'PATIENT-101',
      email: 'alexander.pierce@healthmail.com'
    },
    currentTab: 'dashboard-view',
    vitalCondition: 'normal', // 'normal' | 'warning' | 'critical'
    currentVitals: {
      heartRate: 78,
      bpSys: 120,
      bpDia: 80,
      temperature: 36.8,
      spo2: 98,
      timestamp: new Date()
    },
    history: [], // Stores chronological vital logs
    alerts: [],  // System alert logs
    charts: {
      heartRate: null,
      bloodPressure: null,
      temperature: null,
      spo2: null
    },
    activeRange: 'today' // 'today' | 'week' | 'month'
  };



  // Battery Level & Display Settings
  let deviceBattery = 85;
  let showBatteryHeader = true;

  function updateBatteryUI(percent) {
    const valSpan = document.getElementById('device-battery-val');
    const fillBar = document.getElementById('battery-fill-bar');
    if (valSpan) valSpan.textContent = `${percent}%`;
    if (fillBar) {
      fillBar.style.height = `${percent}%`;
      fillBar.classList.remove('low', 'medium');
      if (percent <= 20) {
        fillBar.classList.add('low');
      } else if (percent <= 50) {
        fillBar.classList.add('medium');
      }
    }
  }

  // Bind Battery Visibility Toggle in Settings
  const batteryToggle = document.getElementById('toggle-battery-show');
  const batteryHeaderContainer = document.getElementById('header-device-battery');
  
  if (batteryToggle && batteryHeaderContainer) {
    batteryToggle.addEventListener('change', (e) => {
      showBatteryHeader = e.target.checked;
      batteryHeaderContainer.style.display = showBatteryHeader ? 'flex' : 'none';
    });
  }

  // Initialize initial battery display
  updateBatteryUI(deviceBattery);

  // ==========================================================================
  // VIEW NAVIGATION & ROUTING
  // ==========================================================================
  function showView(viewId) {
    document.querySelectorAll('.view').forEach(view => {
      view.classList.remove('active');
    });
    const targetView = document.getElementById(viewId);
    if (targetView) {
      targetView.classList.add('active');
    }
  }

  function showSubView(subViewId) {
    document.querySelectorAll('.sub-view').forEach(view => {
      view.classList.remove('active');
    });
    const targetSubView = document.getElementById(subViewId);
    if (targetSubView) {
      targetSubView.classList.add('active');
    }

    // Toggle navigation tab active classes
    document.querySelectorAll('.nav-item-btn').forEach(item => {
      if (item.getAttribute('data-tab') === subViewId) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });

    state.currentTab = subViewId;
    
    // Perform view-specific initializations
    if (subViewId === 'report-view') {
      const activeReportPane = document.querySelector('.report-pane.active').id;
      if (activeReportPane === 'pane-trends') {
        setTimeout(initializeOrUpdateCharts, 50); // Small timeout to ensure canvas is visible
      }
    } else if (subViewId === 'activity-view') {
      setTimeout(() => {
        renderActivityCalendar();
        updateActivityRings();
        initActivityChart();
      }, 50);
    }
  }

  // Bind Bottom Navigation Tabs
  document.querySelectorAll('.nav-item-btn').forEach(button => {
    button.addEventListener('click', () => {
      const tabTarget = button.getAttribute('data-tab');
      showSubView(tabTarget);
    });
  });

  // Bind Report Pane Tabs
  const tabTrendsBtn = document.getElementById('tab-trends');
  const tabAlertsBtn = document.getElementById('tab-alerts');
  const paneTrends = document.getElementById('pane-trends');
  const paneAlerts = document.getElementById('pane-alerts');

  tabTrendsBtn.addEventListener('click', () => {
    tabTrendsBtn.classList.add('active');
    tabAlertsBtn.classList.remove('active');
    paneTrends.classList.add('active');
    paneAlerts.classList.remove('active');
    setTimeout(initializeOrUpdateCharts, 50);
  });

  tabAlertsBtn.addEventListener('click', () => {
    tabAlertsBtn.classList.add('active');
    tabTrendsBtn.classList.remove('active');
    paneAlerts.classList.add('active');
    paneTrends.classList.remove('active');
    renderAlertsList();
  });

  // Splash Screen Transition
  setTimeout(() => {
    showView('main-app');
    showSubView('dashboard-view');
    
    // Initialize background systems
    initializeSimulator();
    seedHistoricalData();
    updateVitals();
  }, 2500);

  // ==========================================================================
  // REAL-TIME VITAL SIMULATOR (IoT Sensor Readings)
  // ==========================================================================
  
  // Vital Ranges Definition
  const VitalRanges = {
    heartRate: {
      normal: { min: 60, max: 95 },
      warning: { min: 96, max: 119 },
      critical: { min: 120, max: 150 }
    },
    bpSys: {
      normal: { min: 90, max: 119 },
      warning: { min: 120, max: 139 },
      critical: { min: 140, max: 180 }
    },
    bpDia: {
      normal: { min: 60, max: 79 },
      warning: { min: 80, max: 89 },
      critical: { min: 90, max: 110 }
    },
    temperature: {
      normal: { min: 36.2, max: 37.2 },
      warning: { min: 37.3, max: 38.2 },
      critical: { min: 38.3, max: 40.5 }
    },
    spo2: {
      normal: { min: 95, max: 100 },
      warning: { min: 90, max: 94 },
      critical: { min: 70, max: 89 }
    }
  };

  const vitalSimulator = {
    setCondition: (cond) => {
      state.vitalCondition = cond;
      document.querySelectorAll('.demo-btn-group .btn').forEach(btn => btn.classList.remove('active-simulator-btn'));
      const activeBtn = document.querySelector(`.btn-demo-${cond}`);
      if (activeBtn) activeBtn.classList.add('active-simulator-btn');
      updateVitals();
    },

    generate: (condition) => {
      const getRandVal = (min, max, isFloat = false) => {
        const val = Math.random() * (max - min) + min;
        return isFloat ? Math.round(val * 10) / 10 : Math.round(val);
      };

      if (condition === 'warning') {
        const isLow = Math.random() > 0.5;
        return {
          heartRate: isLow ? getRandVal(50, 58) : getRandVal(96, 115),
          bpSys: getRandVal(122, 138),
          bpDia: getRandVal(81, 88),
          temperature: getRandVal(37.4, 38.1, true),
          spo2: getRandVal(91, 94),
          timestamp: new Date()
        };
      } else if (condition === 'critical') {
        return {
          heartRate: getRandVal(121, 142),
          bpSys: getRandVal(141, 165),
          bpDia: getRandVal(91, 102),
          temperature: getRandVal(38.4, 39.7, true),
          spo2: getRandVal(82, 89),
          timestamp: new Date()
        };
      } else {
        // Normal state
        return {
          heartRate: getRandVal(65, 84),
          bpSys: getRandVal(110, 118),
          bpDia: getRandVal(70, 78),
          temperature: getRandVal(36.4, 37.1, true),
          spo2: getRandVal(96, 99),
          timestamp: new Date()
        };
      }
    }
  };

  // Expose simulator globally so HTML onclick controls work
  window.vitalSimulator = vitalSimulator;

  let simulationInterval = null;

  function initializeSimulator() {
    if (simulationInterval) clearInterval(simulationInterval);

    const activeBtn = document.querySelector(`.btn-demo-${state.vitalCondition}`);
    if (activeBtn) activeBtn.classList.add('active-simulator-btn');

    // Start background IoT Polling (Simulated every 5 seconds)
    simulationInterval = setInterval(() => {
      updateVitals();
    }, 5000);
  }

  function updateVitals() {
    const rawReadings = vitalSimulator.generate(state.vitalCondition);
    state.currentVitals = rawReadings;

    // Slowly fluctuate battery level (simulate IoT battery depletion over time)
    if (Math.random() > 0.85 && deviceBattery > 10) {
      deviceBattery -= 1;
      updateBatteryUI(deviceBattery);
    }

    // Push into the front of our history list log
    state.history.unshift({
      heartRate: rawReadings.heartRate,
      bpSys: rawReadings.bpSys,
      bpDia: rawReadings.bpDia,
      temperature: rawReadings.temperature,
      spo2: rawReadings.spo2,
      timestamp: rawReadings.timestamp
    });

    // Keep history log within reasonable size
    if (state.history.length > 100) {
      state.history.pop();
    }

    // Check bounds and generate alerts
    evaluateThresholds(rawReadings);

    // Repaint Dashboard cards
    updateDashboardUI(rawReadings);
  }

  function updateDashboardUI(vitals) {
    const hrVal = document.getElementById('val-heart-rate');
    const bpVal = document.getElementById('val-blood-pressure');
    const tempVal = document.getElementById('val-temp');
    const spo2Val = document.getElementById('val-spo2');
    
    const hrTs = document.getElementById('ts-heart-rate');
    const bpTs = document.getElementById('ts-bp');
    const tempTs = document.getElementById('ts-temp');
    const spo2Ts = document.getElementById('ts-spo2');

    hrVal.textContent = vitals.heartRate;
    bpVal.textContent = `${vitals.bpSys}/${vitals.bpDia}`;
    tempVal.textContent = vitals.temperature.toFixed(1);
    spo2Val.textContent = vitals.spo2;

    // Update relative sync timestamp
    const now = vitals.timestamp;
    const pad = (n) => n < 10 ? '0' + n : n;
    const formattedTime = `Synced: ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
    
    hrTs.textContent = formattedTime;
    bpTs.textContent = formattedTime;
    tempTs.textContent = formattedTime;
    spo2Ts.textContent = formattedTime;

    // Adjust Heart Beat Animation Speed based on current pulse rate
    const heartIcon = document.querySelector('.heart-icon');
    heartIcon.classList.remove('beating');
    void heartIcon.offsetWidth; // Trigger reflow to restart animation
    heartIcon.classList.add('beating');
    
    let pulseDuration = '1.0s';
    if (vitals.heartRate > 120) pulseDuration = '0.4s';
    else if (vitals.heartRate > 90) pulseDuration = '0.6s';
    else if (vitals.heartRate < 55) pulseDuration = '1.4s';
    heartIcon.style.animationDuration = pulseDuration;

    // Evaluate CSS Classes & Badges for each card
    setCardStatus('card-heart-rate', 'badge-heart-rate', vitals.heartRate, VitalRanges.heartRate);
    
    // Evaluate Blood Pressure status (if either Systolic or Diastolic is out of range)
    setBloodPressureCardStatus(vitals.bpSys, vitals.bpDia);
    
    // Evaluate Temperature status
    setCardStatus('card-temp', 'badge-temp', vitals.temperature, VitalRanges.temperature);

    // Evaluate SpO2 status
    setCardStatus('card-spo2', 'badge-spo2', vitals.spo2, VitalRanges.spo2);

    // Draw mini sparklines inside cards
    drawSparklines();
  }

  function setCardStatus(cardId, badgeId, value, range) {
    const card = document.getElementById(cardId);
    const badge = document.getElementById(badgeId);
    
    card.className = 'vital-card ' + cardId.split('-')[1] + '-card'; // reset classes
    badge.className = 'status-badge';

    if (value >= range.normal.min && value <= range.normal.max) {
      card.classList.add('normal-state');
      badge.textContent = 'Normal';
      badge.classList.add('badge-normal');
    } else if (value >= range.warning.min && value <= range.warning.max) {
      card.classList.add('warning-state');
      badge.textContent = 'Warning';
      badge.classList.add('badge-warning');
    } else {
      card.classList.add('critical-state');
      badge.textContent = 'Critical';
      badge.classList.add('badge-critical');
    }
  }

  function setBloodPressureCardStatus(sys, dia) {
    const card = document.getElementById('card-bp');
    const badge = document.getElementById('badge-bp');
    
    card.className = 'vital-card bp-card'; // reset classes
    badge.className = 'status-badge';

    // Determine status for systolic and diastolic independently, pick worst
    let sysStatus = 'normal';
    if (sys >= VitalRanges.bpSys.critical.min) sysStatus = 'critical';
    else if (sys >= VitalRanges.bpSys.warning.min) sysStatus = 'warning';

    let diaStatus = 'normal';
    if (dia >= VitalRanges.bpDia.critical.min) diaStatus = 'critical';
    else if (dia >= VitalRanges.bpDia.warning.min) diaStatus = 'warning';

    if (sysStatus === 'critical' || diaStatus === 'critical') {
      card.classList.add('critical-state');
      badge.textContent = 'Critical';
      badge.classList.add('badge-critical');
    } else if (sysStatus === 'warning' || diaStatus === 'warning') {
      card.classList.add('warning-state');
      badge.textContent = 'Warning';
      badge.classList.add('badge-warning');
    } else {
      card.classList.add('normal-state');
      badge.textContent = 'Normal';
      badge.classList.add('badge-normal');
    }
  }

  // Draw mini sparklines inside cards using historical state
  function drawSparklines() {
    const last8Logs = [...state.history].slice(0, 8).reverse();
    if (last8Logs.length < 2) return;

    // Draw BP Sparkline (Systolic trend)
    const bpData = last8Logs.map(log => log.bpSys);
    drawSparklineCanvas('sparkline-bp', bpData, '#3498db');

    // Draw Temp Sparkline
    const tempData = last8Logs.map(log => log.temperature);
    drawSparklineCanvas('sparkline-temp', tempData, '#e67e22');

    // Draw SpO2 Sparkline
    const spo2Data = last8Logs.map(log => log.spo2);
    drawSparklineCanvas('sparkline-spo2', spo2Data, '#06b6d4');
  }

  function drawSparklineCanvas(canvasId, data, color) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    
    const dpr = window.devicePixelRatio || 1;
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    ctx.beginPath();
    ctx.strokeStyle = color;
    ctx.lineWidth = 1.8;
    ctx.lineJoin = 'round';
    ctx.lineCap = 'round';
    
    const min = Math.min(...data);
    const max = Math.max(...data);
    const range = max - min || 1;
    
    const xStep = canvas.width / (data.length - 1);
    
    data.forEach((val, idx) => {
      const x = idx * xStep;
      const y = canvas.height - ((val - min) / range) * (canvas.height - 4) - 2;
      if (idx === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.stroke();
  }

  // ==========================================================================
  // THRESHOLD EVALUATION & ALERTS SYSTEM
  // ==========================================================================
  function evaluateThresholds(vitals) {
    const timeStr = vitals.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

    // 1. Evaluate Heart Rate
    if (vitals.heartRate < VitalRanges.heartRate.normal.min) {
      addAlert('Heart Rate', `Low Heart Rate: ${vitals.heartRate} BPM`, 'Critical', timeStr);
    } else if (vitals.heartRate >= VitalRanges.heartRate.critical.min) {
      addAlert('Heart Rate', `High Heart Rate (Tachycardia): ${vitals.heartRate} BPM`, 'Critical', timeStr);
    } else if (vitals.heartRate >= VitalRanges.heartRate.warning.min) {
      addAlert('Heart Rate', `Elevated Heart Rate: ${vitals.heartRate} BPM`, 'Warning', timeStr);
    }

    // 2. Evaluate Blood Pressure
    if (vitals.bpSys >= VitalRanges.bpSys.critical.min || vitals.bpDia >= VitalRanges.bpDia.critical.min) {
      addAlert('Blood Pressure', `Critical High Blood Pressure: ${vitals.bpSys}/${vitals.bpDia} mmHg`, 'Critical', timeStr);
    } else if (vitals.bpSys >= VitalRanges.bpSys.warning.min || vitals.bpDia >= VitalRanges.bpDia.warning.min) {
      addAlert('Blood Pressure', `Elevated Blood Pressure: ${vitals.bpSys}/${vitals.bpDia} mmHg`, 'Warning', timeStr);
    }

    // 3. Evaluate Temperature
    if (vitals.temperature >= VitalRanges.temperature.critical.min) {
      addAlert('Body Temperature', `High Temperature (Fever) detected: ${vitals.temperature.toFixed(1)}°C`, 'Critical', timeStr);
    } else if (vitals.temperature >= VitalRanges.temperature.warning.min) {
      addAlert('Body Temperature', `Slight fever temperature: ${vitals.temperature.toFixed(1)}°C`, 'Warning', timeStr);
    }

    // 4. Evaluate SpO2
    if (vitals.spo2 < VitalRanges.spo2.normal.min) {
      if (vitals.spo2 <= VitalRanges.spo2.critical.max) {
        addAlert('Blood Oxygen', `Critical Low Oxygen (Hypoxia): ${vitals.spo2}%`, 'Critical', timeStr);
      } else {
        addAlert('Blood Oxygen', `Low Oxygen Level: ${vitals.spo2}%`, 'Warning', timeStr);
      }
    }
  }

  function addAlert(param, message, severity, timestamp) {
    // Avoid double alerts of exact same issue within consecutive ticks to prevent spam
    const lastAlert = state.alerts[0];
    if (lastAlert && lastAlert.param === param && lastAlert.severity === severity && (Date.now() - lastAlert.timeRaw < 15000)) {
      return; // Skip duplicate notification within 15 seconds
    }

    const newAlert = {
      id: 'alert-' + Date.now(),
      param,
      message,
      severity,
      timestamp,
      timeRaw: Date.now()
    };

    state.alerts.unshift(newAlert);
    
    // Cap alerts list at 30 entries
    if (state.alerts.length > 30) {
      state.alerts.pop();
    }

    // Update notification icon badge in bottom nav
    updateAlertBadgeCount();

    // Render alert view container if active
    if (state.currentTab === 'report-view' && document.getElementById('tab-alerts').classList.contains('active')) {
      renderAlertsList();
    }
  }

  function updateAlertBadgeCount() {
    const navBadge = document.getElementById('nav-report-badge');
    const tabBadge = document.getElementById('report-badge-count');
    const alertCount = state.alerts.length;

    if (alertCount > 0) {
      navBadge.textContent = alertCount;
      navBadge.style.display = 'flex';
      
      tabBadge.textContent = alertCount;
      tabBadge.style.display = 'inline-block';
    } else {
      navBadge.style.display = 'none';
      tabBadge.style.display = 'none';
    }
  }

  function renderAlertsList() {
    const listContainer = document.getElementById('alerts-log-list');
    const emptyState = document.getElementById('alerts-empty-state');
    
    // Clear dynamic cards (except empty state placeholder)
    const alertCards = listContainer.querySelectorAll('.alert-item-card');
    alertCards.forEach(c => c.remove());

    if (state.alerts.length === 0) {
      emptyState.style.display = 'flex';
      return;
    }

    emptyState.style.display = 'none';

    state.alerts.forEach(alert => {
      const card = document.createElement('div');
      card.className = `alert-item-card severity-${alert.severity.toLowerCase()}`;
      
      const icon = alert.severity === 'Critical' ? 'emergency_home' : 'warning';
      
      card.innerHTML = `
        <div class="alert-item-icon-box">
          <span class="material-symbols-outlined">${icon}</span>
        </div>
        <div class="alert-item-content">
          <div class="alert-item-title-row">
            <h6>${alert.param}</h6>
            <span class="alert-item-badge">${alert.severity}</span>
          </div>
          <p class="alert-item-desc">${alert.message}</p>
          <span class="alert-item-time">${alert.timestamp}</span>
        </div>
      `;
      
      listContainer.appendChild(card);
    });
  }

  // Clear Alerts Handler
  document.getElementById('clear-all-alerts').addEventListener('click', () => {
    state.alerts = [];
    updateAlertBadgeCount();
    renderAlertsList();
  });

  // ==========================================================================
  // SEED & MANAGE HISTORICAL TRENDS DATA
  // ==========================================================================
  
  function seedHistoricalData() {
    state.history = [];
    const now = new Date();

    const genSeedReading = (indexHoursAgo) => {
      const time = new Date(now.getTime() - indexHoursAgo * 60 * 60 * 1000);
      
      // Inject some mock warning fluctuations in past records for visual interest
      const isSpike = (indexHoursAgo === 4 || indexHoursAgo === 11);
      if (isSpike) {
        return {
          heartRate: getRandInt(96, 108),
          bpSys: getRandInt(131, 142),
          bpDia: getRandInt(84, 91),
          temperature: getRandFloat(37.4, 38.0),
          spo2: getRandInt(91, 94),
          timestamp: time
        };
      }

      return {
        heartRate: getRandInt(68, 86),
        bpSys: getRandInt(112, 119),
        bpDia: getRandInt(72, 79),
        temperature: getRandFloat(36.4, 37.0),
        spo2: getRandInt(96, 99),
        timestamp: time
      };
    };

    function getRandInt(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
    function getRandFloat(min, max) { return Math.round((Math.random() * (max - min) + min) * 10) / 10; }

    // Seed past 24 hourly logs
    for (let i = 24; i >= 0; i--) {
      state.history.push(genSeedReading(i));
    }
  }

  // Bind History Filters (Today, Week, Month)
  document.querySelectorAll('#pane-trends .range-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#pane-trends .range-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.activeRange = btn.getAttribute('data-range');
      initializeOrUpdateCharts();
    });
  });

  // ==========================================================================
  // CHART.JS TREND GRAPH INTEGRATION
  // ==========================================================================
  
  function getChartLabelsAndData() {
    const now = new Date();
    const dataPoints = {
      today: 8,
      week: 7,
      month: 10
    }[state.activeRange];

    const labels = [];
    const hrData = [];
    const bpSysData = [];
    const bpDiaData = [];
    const tempData = [];
    const spo2Data = [];

    if (state.activeRange === 'today') {
      const logs = [...state.history].slice(0, dataPoints).reverse();
      logs.forEach(log => {
        labels.push(log.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
        hrData.push(log.heartRate);
        bpSysData.push(log.bpSys);
        bpDiaData.push(log.bpDia);
        tempData.push(log.temperature);
        spo2Data.push(log.spo2 || 98);
      });
    } else if (state.activeRange === 'week') {
      const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
      for (let i = 6; i >= 0; i--) {
        const d = new Date(now.getTime() - i * 24 * 60 * 60 * 1000);
        labels.push(days[d.getDay()]);
        
        hrData.push(Math.round(72 + Math.random() * 8));
        bpSysData.push(Math.round(114 + Math.random() * 10));
        bpDiaData.push(Math.round(74 + Math.random() * 6));
        tempData.push(Number((36.5 + Math.random() * 0.4).toFixed(1)));
        spo2Data.push(Math.round(96 + Math.random() * 3));
      }
    } else {
      for (let i = 9; i >= 0; i--) {
        const d = new Date(now.getTime() - i * 3 * 24 * 60 * 60 * 1000);
        labels.push(`${d.getMonth() + 1}/${d.getDate()}`);
        
        hrData.push(Math.round(73 + Math.random() * 6));
        bpSysData.push(Math.round(115 + Math.random() * 8));
        bpDiaData.push(Math.round(75 + Math.random() * 5));
        tempData.push(Number((36.6 + Math.random() * 0.3).toFixed(1)));
        spo2Data.push(Math.round(96 + Math.random() * 3));
      }
    }

    return { labels, hrData, bpSysData, bpDiaData, tempData, spo2Data };
  }

  function initializeOrUpdateCharts() {
    const { labels, hrData, bpSysData, bpDiaData, tempData, spo2Data } = getChartLabelsAndData();

    // Chart Configuration Builder Helper
    const getChartConfig = (label, data, colorHex, minVal, maxVal, unit) => {
      return {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: label,
            data: data,
            borderColor: colorHex,
            backgroundColor: colorHex + '12',
            borderWidth: 2.2,
            fill: true,
            tension: 0.35,
            pointRadius: 2.5,
            pointBackgroundColor: colorHex,
            pointBorderColor: '#ffffff',
            pointBorderWidth: 1.2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: {
              backgroundColor: '#1e293b',
              padding: 9,
              bodyFont: { family: 'Inter', size: 11 },
              titleFont: { family: 'Inter', size: 10, weight: 'bold' },
              cornerRadius: 8,
              callbacks: {
                label: (context) => ` ${context.parsed.y} ${unit}`
              }
            }
          },
          scales: {
            y: {
              min: minVal,
              max: maxVal,
              grid: { color: '#f1f5f9' },
              ticks: {
                color: '#64748b',
                font: { family: 'Inter', size: 9 }
              }
            },
            x: {
              grid: { display: false },
              ticks: {
                color: '#64748b',
                font: { family: 'Inter', size: 9 }
              }
            }
          }
        }
      };
    };

    // Render / Update Heart Rate Chart
    if (state.charts.heartRate) {
      state.charts.heartRate.data.labels = labels;
      state.charts.heartRate.data.datasets[0].data = hrData;
      state.charts.heartRate.update();
    } else {
      const ctx = document.getElementById('chart-heart-rate').getContext('2d');
      state.charts.heartRate = new Chart(ctx, getChartConfig('Heart Rate', hrData, '#e74c3c', 50, 140, 'BPM'));
    }

    // Render / Update Blood Pressure Chart (Double line graph: Systolic & Diastolic)
    if (state.charts.bloodPressure) {
      state.charts.bloodPressure.data.labels = labels;
      state.charts.bloodPressure.data.datasets[0].data = bpSysData;
      state.charts.bloodPressure.data.datasets[1].data = bpDiaData;
      state.charts.bloodPressure.update();
    } else {
      const ctx = document.getElementById('chart-blood-pressure').getContext('2d');
      state.charts.bloodPressure = new Chart(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [
            {
              label: 'Systolic',
              data: bpSysData,
              borderColor: '#0d9488', // Teal
              backgroundColor: 'transparent',
              borderWidth: 2,
              tension: 0.35,
              pointRadius: 2.5,
              pointBackgroundColor: '#0d9488',
              pointBorderColor: '#ffffff'
            },
            {
              label: 'Diastolic',
              data: bpDiaData,
              borderColor: '#3498db', // Blue
              backgroundColor: 'transparent',
              borderWidth: 2,
              tension: 0.35,
              pointRadius: 2.5,
              pointBackgroundColor: '#3498db',
              pointBorderColor: '#ffffff'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              position: 'top',
              labels: {
                boxWidth: 10,
                font: { family: 'Inter', size: 9, weight: 'bold' },
                color: '#64748b'
              }
            },
            tooltip: {
              backgroundColor: '#1e293b',
              padding: 9,
              bodyFont: { family: 'Inter', size: 11 },
              titleFont: { family: 'Inter', size: 10 },
              cornerRadius: 8,
              callbacks: {
                label: (context) => ` ${context.dataset.label}: ${context.parsed.y} mmHg`
              }
            }
          },
          scales: {
            y: {
              min: 50,
              max: 180,
              grid: { color: '#f1f5f9' },
              ticks: {
                color: '#64748b',
                font: { family: 'Inter', size: 9 }
              }
            },
            x: {
              grid: { display: false },
              ticks: {
                color: '#64748b',
                font: { family: 'Inter', size: 9 }
              }
            }
          }
        }
      });
    }

    // Render / Update Temp Chart
    if (state.charts.temperature) {
      state.charts.temperature.data.labels = labels;
      state.charts.temperature.data.datasets[0].data = tempData;
      state.charts.temperature.update();
    } else {
      const ctx = document.getElementById('chart-temp-trend').getContext('2d');
      state.charts.temperature = new Chart(ctx, getChartConfig('Temperature', tempData, '#e67e22', 35, 41, '°C'));
    }

    // Render / Update SpO2 Chart
    if (state.charts.spo2) {
      state.charts.spo2.data.labels = labels;
      state.charts.spo2.data.datasets[0].data = spo2Data;
      state.charts.spo2.update();
    } else {
      const ctx = document.getElementById('chart-spo2-trend').getContext('2d');
      state.charts.spo2 = new Chart(ctx, getChartConfig('Blood Oxygen', spo2Data, '#06b6d4', 80, 100, '%'));
    }
  }

  // ==========================================================================
  // DAILY ACTIVITY SCREEN LOGIC
  // ==========================================================================

  const activityState = {
    steps: 8105,
    stepsGoal: 6000,
    time: 85,
    timeGoal: 90,
    calories: 299,
    caloriesGoal: 500,
    chartInstance: null
  };

  function renderActivityCalendar() {
    const calendarEl = document.getElementById('activity-calendar');
    if (!calendarEl) return;
    calendarEl.innerHTML = '';
    
    const days = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
    const now = new Date();
    const todayIndex = now.getDay();
    const todayDate = now.getDate();
    
    for (let i = 0; i < 7; i++) {
      // Calculate date for the day of this week
      const dateOffset = i - todayIndex;
      const d = new Date(now.getTime() + (dateOffset * 24 * 60 * 60 * 1000));
      const dateNum = d.getDate();
      
      const isActive = i === todayIndex;
      
      // Mock random progress for previous days, empty for future
      let pSteps = i < todayIndex ? (0.6 + Math.random() * 0.4) * 100 : (isActive ? (activityState.steps / activityState.stepsGoal) * 100 : 0);
      let pTime = i < todayIndex ? (0.5 + Math.random() * 0.5) * 100 : (isActive ? (activityState.time / activityState.timeGoal) * 100 : 0);
      let pCal = i < todayIndex ? (0.4 + Math.random() * 0.6) * 100 : (isActive ? (activityState.calories / activityState.caloriesGoal) * 100 : 0);

      // SVG path length calculations
      const rOuter = 10;
      const cOuter = 2 * Math.PI * rOuter;
      const oSteps = cOuter - (pSteps / 100) * cOuter;
      
      const rMid = 7;
      const cMid = 2 * Math.PI * rMid;
      const oTime = cMid - (pTime / 100) * cMid;
      
      const rInner = 4;
      const cInner = 2 * Math.PI * rInner;
      const oCal = cInner - (pCal / 100) * cInner;

      const dayHtml = `
        <div class="cal-day ${isActive ? 'active' : ''}">
          <span class="day-label">${days[i]}</span>
          <svg class="cal-ring-mini" viewBox="0 0 28 28">
            <circle cx="14" cy="14" r="10" stroke="#2ecc7133"></circle>
            <circle cx="14" cy="14" r="10" stroke="#2ecc71" stroke-dasharray="${cOuter}" stroke-dashoffset="${oSteps}"></circle>
            <circle cx="14" cy="14" r="7" stroke="#3498db33"></circle>
            <circle cx="14" cy="14" r="7" stroke="#3498db" stroke-dasharray="${cMid}" stroke-dashoffset="${oTime}"></circle>
            <circle cx="14" cy="14" r="4" stroke="#ff475733"></circle>
            <circle cx="14" cy="14" r="4" stroke="#ff4757" stroke-dasharray="${cInner}" stroke-dashoffset="${oCal}"></circle>
          </svg>
          <span class="date-label">${isActive ? (d.getMonth()+1)+'/'+dateNum : dateNum}</span>
        </div>
      `;
      calendarEl.insertAdjacentHTML('beforeend', dayHtml);
    }
  }

  function updateActivityRings() {
    // Math logic for SVG dashoffset
    // c = 2 * PI * r
    const cSteps = 2 * Math.PI * 80;
    const cTime = 2 * Math.PI * 60;
    const cCal = 2 * Math.PI * 40;

    const pSteps = Math.min(100, (activityState.steps / activityState.stepsGoal) * 100);
    const pTime = Math.min(100, (activityState.time / activityState.timeGoal) * 100);
    const pCal = Math.min(100, (activityState.calories / activityState.caloriesGoal) * 100);

    const pathSteps = document.getElementById('ring-steps-path');
    const pathTime = document.getElementById('ring-time-path');
    const pathCal = document.getElementById('ring-cal-path');

    if (pathSteps) {
      pathSteps.style.strokeDasharray = cSteps;
      pathSteps.style.strokeDashoffset = cSteps; // Start empty
      setTimeout(() => { pathSteps.style.strokeDashoffset = cSteps - (pSteps / 100) * cSteps; }, 100);
    }
    
    if (pathTime) {
      pathTime.style.strokeDasharray = cTime;
      pathTime.style.strokeDashoffset = cTime;
      setTimeout(() => { pathTime.style.strokeDashoffset = cTime - (pTime / 100) * cTime; }, 200);
    }
    
    if (pathCal) {
      pathCal.style.strokeDasharray = cCal;
      pathCal.style.strokeDashoffset = cCal;
      setTimeout(() => { pathCal.style.strokeDashoffset = cCal - (pCal / 100) * cCal; }, 300);
    }
  }

  function initActivityChart() {
    const ctx = document.getElementById('chart-activity-steps');
    if (!ctx) return;
    
    if (activityState.chartInstance) {
      activityState.chartInstance.update();
      return;
    }

    const labels = ['12 am', '6 am', '12 pm', '6 pm', '(h)'];
    // Mock 24 hour data, spikes around 8am, 12pm, 5pm
    const data = [0, 0, 0, 0, 0, 0, 300, 2500, 1200, 400, 200, 1500, 1800, 2200, 2100, 500, 800, 100, 50, 200, 300, 0, 0, 0];
    
    // To match the 5 label grid on x-axis visually, we just let chart.js scale it
    // The design has simple green bars with rounded caps
    activityState.chartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: data.map((_, i) => i), // 0 to 23
        datasets: [{
          data: data,
          backgroundColor: '#2ecc71',
          borderRadius: 4,
          barPercentage: 0.6,
          categoryPercentage: 0.8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1e293b',
            padding: 8,
            bodyFont: { family: 'Inter', size: 11 },
            callbacks: {
              title: (context) => `${context[0].label}:00`,
              label: (context) => `${context.parsed.y} steps`
            }
          }
        },
        scales: {
          y: {
            display: false,
            beginAtZero: true
          },
          x: {
            grid: {
              display: true,
              color: '#e2e8f0',
              drawBorder: false,
              tickLength: 5
            },
            ticks: {
              color: '#94a3b8',
              font: { family: 'Inter', size: 10 },
              maxRotation: 0,
              callback: function(val, index) {
                // Return labels at specific intervals
                if (index === 0) return '12 am';
                if (index === 6) return '6 am';
                if (index === 12) return '12 pm';
                if (index === 18) return '6 pm';
                if (index === 23) return '(h)';
                return '';
              }
            }
          }
        }
      }
    });
  }

  // ==========================================================================
  // BLUETOOTH & WIFI SETUP MODAL LOGIC
  // ==========================================================================
  
  let bluetoothDevice = null;
  const btnManageDevices = document.getElementById('btn-manage-devices');
  const setupModal = document.getElementById('device-setup-modal');
  const closeSetupModal = document.getElementById('close-setup-modal');
  const btnConnectBt = document.getElementById('btn-connect-bt');
  const btStatusText = document.getElementById('bt-status-text');
  const setupStep1 = document.getElementById('setup-step-1');
  const setupStep2 = document.getElementById('setup-step-2');
  const headerBtStatus = document.getElementById('header-bt-status');
  
  const btnProvisionWifi = document.getElementById('btn-provision-wifi');
  const wifiSsidInput = document.getElementById('wifi-ssid');
  const wifiPassInput = document.getElementById('wifi-pass');
  const provisionStatus = document.getElementById('provision-status');

  // Open Modal
  if (btnManageDevices) {
    btnManageDevices.addEventListener('click', () => {
      setupModal.classList.add('active');
    });
  }

  // Close Modal
  if (closeSetupModal) {
    closeSetupModal.addEventListener('click', () => {
      setupModal.classList.remove('active');
    });
  }

  // Handle Web Bluetooth Connection
  if (btnConnectBt) {
    btnConnectBt.addEventListener('click', async () => {
      try {
        btStatusText.textContent = 'Requesting Bluetooth Device...';
        
        // Use real Web Bluetooth API
        bluetoothDevice = await navigator.bluetooth.requestDevice({
          acceptAllDevices: true
          // In a real app, you would filter by service UUID:
          // filters: [{ services: ['heart_rate'] }]
        });
        
        btStatusText.textContent = 'Connecting...';
        
        // Connect to GATT Server
        const server = await bluetoothDevice.gatt.connect();
        
        // UI Updates for Connection Success
        btStatusText.innerHTML = `Connected to: <strong>${bluetoothDevice.name || 'IoT Device'}</strong>`;
        btStatusText.classList.add('connected');
        btnConnectBt.textContent = 'Connected';
        btnConnectBt.disabled = true;
        
        // Update global header status
        if (headerBtStatus) headerBtStatus.classList.add('connected');

        // Transition to Step 2 (Wi-Fi)
        setTimeout(() => {
          setupStep1.classList.add('hidden');
          setupStep2.classList.remove('hidden');
        }, 1500);

        // Handle disconnects
        bluetoothDevice.addEventListener('gattserverdisconnected', onDisconnected);
        
      } catch (error) {
        console.warn('Bluetooth connection failed or cancelled:', error);
        btStatusText.textContent = 'Connection failed or cancelled.';
        btStatusText.classList.remove('connected');
      }
    });
  }

  function onDisconnected() {
    btStatusText.textContent = 'Device disconnected.';
    btStatusText.classList.remove('connected');
    btnConnectBt.textContent = 'Connect Bluetooth';
    btnConnectBt.disabled = false;
    bluetoothDevice = null;
    if (headerBtStatus) headerBtStatus.classList.remove('connected');
    
    // Reset to Step 1
    setupStep1.classList.remove('hidden');
    setupStep2.classList.add('hidden');
  }

  // Handle Wi-Fi Provisioning
  if (btnProvisionWifi) {
    btnProvisionWifi.addEventListener('click', () => {
      const ssid = wifiSsidInput.value.trim();
      const pass = wifiPassInput.value.trim();
      
      if (!ssid) {
        provisionStatus.textContent = 'Please enter a Network Name (SSID).';
        provisionStatus.className = 'provision-status error';
        return;
      }
      
      // Simulate sending credentials to IoT device via GATT
      provisionStatus.textContent = 'Sending credentials to device...';
      provisionStatus.className = 'provision-status';
      btnProvisionWifi.disabled = true;
      
      setTimeout(() => {
        provisionStatus.innerHTML = `Successfully provisioned device to <strong>${ssid}</strong>`;
        provisionStatus.className = 'provision-status success';
        btnProvisionWifi.textContent = 'Provisioned';
        
        setTimeout(() => {
          setupModal.classList.remove('active');
          // Reset form for next time
          setTimeout(() => {
            wifiSsidInput.value = '';
            wifiPassInput.value = '';
            provisionStatus.textContent = '';
            btnProvisionWifi.textContent = 'Provision Device';
            btnProvisionWifi.disabled = false;
          }, 500);
        }, 2500);
        
      }, 2000);
    });
  }

});
