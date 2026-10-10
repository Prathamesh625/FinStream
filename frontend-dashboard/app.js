document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const tpsCounter = document.getElementById('tps-counter');
    const tpsProgress = document.getElementById('tps-progress');
    const peakTps = document.getElementById('peak-tps');
    const p50Val = document.getElementById('p50-val');
    const p95Val = document.getElementById('p95-val');
    const p99Val = document.getElementById('p99-val');
    const totalTxns = document.getElementById('total-txns');
    const fraudCount = document.getElementById('fraud-count');
    const tableBody = document.getElementById('transaction-feed-body');
    const alertList = document.getElementById('fraud-alert-list');
    const alertBadge = document.getElementById('alert-count-badge');

    // Controls
    const tpsSlider = document.getElementById('tps-slider');
    const tpsSliderVal = document.getElementById('tps-slider-val');
    const btnStartLoad = document.getElementById('btn-start-load');
    const btnStopLoad = document.getElementById('btn-stop-load');
    const singleTxnForm = document.getElementById('single-txn-form');

    // State Variables
    let currentTPSVal = 1240;
    let maxTPSSeen = 1480;
    let sessionTotal = 148920;
    let totalFraud = 14;
    let isSimulationActive = false;
    let loadTestInterval = null;

    // Slider listener
    if (tpsSlider) {
        tpsSlider.addEventListener('input', (e) => {
            tpsSliderVal.textContent = `${parseInt(e.target.value).toLocaleString()} TPS`;
        });
    }

    // Generate Mock Transaction Feed Items
    function createTransactionRow(id, src, dest, amount, latency, status) {
        const tr = document.createElement('tr');
        const isSuccess = status === 'COMPLETED';
        const badgeClass = isSuccess ? 'badge-success' : 'badge-rose';
        
        tr.innerHTML = `
            <td class="font-mono text-cyan">${id}</td>
            <td class="font-mono">${src}</td>
            <td class="font-mono">${dest}</td>
            <td class="font-mono">$${parseFloat(amount).toFixed(2)}</td>
            <td class="font-mono text-emerald">${latency} ms</td>
            <td><span class="badge ${badgeClass}">${status}</span></td>
        `;
        return tr;
    }

    // Populate Initial Transactions
    const initialTxns = [
        { id: 'TXN-984201', src: 'ACC-104', dest: 'ACC-209', amount: 1250.00, latency: 12, status: 'COMPLETED' },
        { id: 'TXN-984202', src: 'ACC-118', dest: 'ACC-301', amount: 480.50, latency: 8, status: 'COMPLETED' },
        { id: 'TXN-984203', src: 'ACC-502', dest: 'ACC-110', amount: 55000.00, latency: 15, status: 'REJECTED_INSUFFICIENT_FUNDS' },
        { id: 'TXN-984204', src: 'ACC-205', dest: 'ACC-804', amount: 320.00, latency: 11, status: 'COMPLETED' },
        { id: 'TXN-984205', src: 'ACC-701', dest: 'ACC-102', amount: 89.99, latency: 9, status: 'COMPLETED' }
    ];

    initialTxns.forEach(tx => {
        tableBody.appendChild(createTransactionRow(tx.id, tx.src, tx.dest, tx.amount, tx.latency, tx.status));
    });

    // Populate Initial Fraud Alerts
    const initialAlerts = [
        { title: 'VELOCITY_SPIKE: High transaction rate', desc: 'ACC-104 triggered 14 txns in 5s window', time: '10:12:04' },
        { title: 'HIGH_VALUE_TRANSFER: Large transfer threshold', desc: 'ACC-502 attempted $55,000 transfer', time: '10:11:48' }
    ];

    function renderAlerts() {
        alertList.innerHTML = '';
        initialAlerts.forEach(al => {
            const div = document.createElement('div');
            div.className = 'alert-item';
            div.innerHTML = `
                <div class="alert-info">
                    <h4>${al.title}</h4>
                    <p>${al.desc}</p>
                </div>
                <div class="alert-time">${al.time}</div>
            `;
            alertList.appendChild(div);
        });
        alertBadge.textContent = `${initialAlerts.length} Active Alerts`;
    }
    renderAlerts();

    // Live Metrics Poller / Generator
    function updateMetrics() {
        let targetTPS = isSimulationActive ? parseInt(tpsSlider.value) : (1100 + Math.floor(Math.random() * 250));
        currentTPSVal = Math.round(currentTPSVal * 0.4 + targetTPS * 0.6);
        
        if (currentTPSVal > maxTPSSeen) maxTPSSeen = currentTPSVal;
        sessionTotal += Math.round(currentTPSVal);

        tpsCounter.textContent = currentTPSVal.toLocaleString();
        peakTps.textContent = `Peak: ${maxTPSSeen.toLocaleString()} TPS`;
        totalTxns.textContent = sessionTotal.toLocaleString();
        fraudCount.textContent = totalFraud;

        const pct = Math.min(100, Math.round((currentTPSVal / 2000) * 100));
        tpsProgress.style.width = `${pct}%`;

        // Dynamic latencies under 200ms
        const p50 = Math.floor(8 + Math.random() * 8);
        const p95 = Math.floor(35 + Math.random() * 15);
        const p99 = Math.floor(105 + Math.random() * 30);

        p50Val.textContent = `${p50} ms`;
        p95Val.textContent = `${p95} ms`;
        p99Val.textContent = `${p99} ms`;

        // Stream fake live rows
        if (Math.random() > 0.3) {
            const randSrc = `ACC-${Math.floor(100 + Math.random() * 900)}`;
            const randDest = `ACC-${Math.floor(100 + Math.random() * 900)}`;
            const randAmt = (10 + Math.random() * 450).toFixed(2);
            const randLat = Math.floor(5 + Math.random() * 20);
            const id = `TXN-${Math.floor(100000 + Math.random() * 900000)}`;

            const row = createTransactionRow(id, randSrc, randDest, randAmt, randLat, 'COMPLETED');
            tableBody.insertBefore(row, tableBody.firstChild);
            if (tableBody.children.length > 20) {
                tableBody.removeChild(tableBody.lastChild);
            }
        }
    }

    setInterval(updateMetrics, 1000);

    // Fetch Backend Analytics API if available
    async function pollBackendMetrics() {
        try {
            const res = await fetch('http://localhost:8085/api/v1/analytics/dashboard');
            if (res.ok) {
                const data = await res.json();
                if (data.totalTransactions) {
                    sessionTotal = data.totalTransactions;
                    if (data.currentTPS) currentTPSVal = data.currentTPS;
                    if (data.fraudAlerts) totalFraud = data.fraudAlerts;
                }
            }
        } catch (e) {
            // Standalone client fallback mode
        }
    }
    setInterval(pollBackendMetrics, 2000);

    // Load Simulator Buttons
    btnStartLoad.addEventListener('click', () => {
        isSimulationActive = true;
        btnStartLoad.disabled = true;
        btnStopLoad.disabled = false;

        fetch('http://localhost:8085/api/v1/analytics/load-test/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ tps: parseInt(tpsSlider.value), duration: parseInt(document.getElementById('test-duration').value) })
        }).catch(() => {});

        setTimeout(() => {
            stopSimulation();
        }, parseInt(document.getElementById('test-duration').value) * 1000);
    });

    btnStopLoad.addEventListener('click', () => {
        stopSimulation();
    });

    function stopSimulation() {
        isSimulationActive = false;
        btnStartLoad.disabled = false;
        btnStopLoad.disabled = true;

        fetch('http://localhost:8085/api/v1/analytics/load-test/stop', { method: 'POST' }).catch(() => {});
    }

    // Single Form Submit
    singleTxnForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const src = document.getElementById('input-src').value;
        const dest = document.getElementById('input-dest').value;
        const amt = document.getElementById('input-amount').value;

        const id = `TXN-DIRECT-${Math.floor(100000 + Math.random() * 900000)}`;
        const row = createTransactionRow(id, src, dest, amt, Math.floor(6 + Math.random() * 10), 'COMPLETED');
        tableBody.insertBefore(row, tableBody.firstChild);

        fetch('http://localhost:8082/api/v1/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                idempotencyKey: `KEY-${Date.now()}`,
                sourceAccountId: src,
                destinationAccountId: dest,
                amount: parseFloat(amt),
                currency: 'USD',
                type: 'TRANSFER',
                referenceNote: 'Manual web dashboard entry'
            })
        }).catch(() => {});
    });
});
