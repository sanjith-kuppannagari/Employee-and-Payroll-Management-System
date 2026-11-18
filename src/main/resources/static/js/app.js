const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080' : '';

function showAlert(id, msg, isError = false) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = msg;
    el.className = 'alert ' + (isError ? 'alert-error' : 'alert-success');
    el.style.display = 'block';
    setTimeout(() => { el.style.display = 'none'; }, 4500);
}

// 1. Login Handler
async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    try {
        const res = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const result = await res.json();
        if (result.success) {
            localStorage.setItem('payroll_token', result.data);
            localStorage.setItem('payroll_user', username);
            window.location.href = '/dashboard.html';
        } else {
            showAlert('loginAlert', result.message || 'Invalid credentials', true);
        }
    } catch (err) {
        showAlert('loginAlert', 'Server error: Ensure Java HttpServer is running on port 8080.', true);
    }
}

function logout() {
    localStorage.removeItem('payroll_token');
    window.location.href = '/login.html';
}

// 2. Dashboard CRUD Operations
async function loadEmployees() {
    const tbody = document.getElementById('employeeTableBody');
    if (!tbody) return;
    try {
        const res = await fetch(`${API_BASE}/api/employees`);
        const result = await res.json();
        const list = result.data || [];
        tbody.innerHTML = '';

        let totalPayroll = 0;
        let ftCount = 0;

        list.forEach(emp => {
            totalPayroll += emp.netSalary;
            if (emp.type === 'FULL_TIME') ftCount++;
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>EMP-${emp.id}</strong></td>
                <td>${emp.name}<br><small style="color:#64748b">${emp.email}</small></td>
                <td>${emp.role}</td>
                <td><span class="badge ${emp.type === 'FULL_TIME' ? 'badge-ft' : 'badge-pt'}">${emp.type}</span></td>
                <td>Rs. ${emp.basePay.toFixed(2)}</td>
                <td>${emp.type === 'FULL_TIME' ? 'Rs. ' + emp.extra.toFixed(2) : emp.extra + ' hrs'}</td>
                <td><strong>Rs. ${emp.netSalary.toFixed(2)}</strong></td>
                <td>
                    <button class="btn btn-xs" onclick='editEmployee(${JSON.stringify(emp)})'>Edit</button>
                    <button class="btn btn-success btn-xs" onclick="openPayslip(${emp.id})">Payslip</button>
                    <button class="btn btn-danger btn-xs" onclick="deleteEmployee(${emp.id})">Delete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        document.getElementById('statTotalEmp').textContent = list.length;
        document.getElementById('statFullTime').textContent = ftCount;
        document.getElementById('statPartTime').textContent = list.length - ftCount;
        document.getElementById('statTotalPayroll').textContent = 'Rs. ' + totalPayroll.toFixed(2);
    } catch (err) {
        showAlert('dashAlert', 'Failed to fetch employees from server.', true);
    }
}

function toggleTypeFields() {
    const type = document.getElementById('empType').value;
    document.getElementById('basePayLabel').textContent = type === 'PART_TIME' ? 'Hourly Rate (Rs.)' : 'Monthly Base Salary (Rs.)';
    document.getElementById('extraLabel').textContent = type === 'PART_TIME' ? 'Hours Worked' : 'Performance Bonus (Rs.)';
}

async function saveEmployee(e) {
    e.preventDefault();
    const payload = {
        id: document.getElementById('empId').value || '0',
        name: document.getElementById('empName').value.trim(),
        email: document.getElementById('empEmail').value.trim(),
        role: document.getElementById('empRole').value,
        type: document.getElementById('empType').value,
        basePay: document.getElementById('empBasePay').value,
        extra: document.getElementById('empExtra').value
    };
    try {
        const res = await fetch(`${API_BASE}/api/employees`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await res.json();
        if (result.success) {
            showAlert('dashAlert', result.message, false);
            resetForm();
            loadEmployees();
        } else {
            showAlert('dashAlert', result.message, true);
        }
    } catch (err) {
        showAlert('dashAlert', 'Error saving employee record.', true);
    }
}

function editEmployee(emp) {
    document.getElementById('formTitle').textContent = 'Update Employee #' + emp.id;
    document.getElementById('empId').value = emp.id;
    document.getElementById('empName').value = emp.name;
    document.getElementById('empEmail').value = emp.email;
    document.getElementById('empRole').value = emp.role;
    document.getElementById('empType').value = emp.type;
    toggleTypeFields();
    document.getElementById('empBasePay').value = emp.basePay;
    document.getElementById('empExtra').value = emp.extra;
}

function resetForm() {
    document.getElementById('formTitle').textContent = 'Add New Employee';
    document.getElementById('employeeForm').reset();
    document.getElementById('empId').value = '0';
    toggleTypeFields();
}

async function deleteEmployee(id) {
    if (!confirm(`Delete Employee EMP-${id}?`)) return;
    const res = await fetch(`${API_BASE}/api/employees?id=${id}`, { method: 'DELETE' });
    const result = await res.json();
    showAlert('dashAlert', result.message, !result.success);
    loadEmployees();
}

function openPayslip(id) {
    window.location.href = `/payslip.html?id=${id}`;
}

// 3. Payslip Export & Viewer
async function initPayslipPage() {
    const select = document.getElementById('payslipEmpSelect');
    if (!select) return;
    const res = await fetch(`${API_BASE}/api/employees`);
    const result = await res.json();
    const list = result.data || [];
    select.innerHTML = list.map(e => `<option value="${e.id}">EMP-${e.id} - ${e.name} (${e.type})</option>`).join('');

    const params = new URLSearchParams(window.location.search);
    if (params.get('id')) {
        select.value = params.get('id');
    }
    if (select.value) generateAndExportPayslip();
}

async function generateAndExportPayslip() {
    const id = document.getElementById('payslipEmpSelect').value;
    if (!id) return;
    try {
        const res = await fetch(`${API_BASE}/api/payroll/export`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id })
        });
        const result = await res.json();
        if (result.success) {
            document.getElementById('receiptOutput').textContent = result.data.receipt;
            document.getElementById('savedFilePath').textContent = `Saved on Server Disk: ${result.data.file}`;
            showAlert('payslipAlert', result.message, false);
        } else {
            showAlert('payslipAlert', result.message, true);
        }
    } catch (err) {
        showAlert('payslipAlert', 'Error generating payslip.', true);
    }
}

function downloadReceiptTxt() {
    const content = document.getElementById('receiptOutput').textContent;
    const id = document.getElementById('payslipEmpSelect').value || 'receipt';
    const blob = new Blob([content], { type: 'text/plain' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `payslip_EMP_${id}.txt`;
    a.click();
}
