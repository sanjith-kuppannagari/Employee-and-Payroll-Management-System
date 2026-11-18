const DEPARTMENTS = {
  ENGINEERING: 'Engineering & R&D',
  HR: 'Human Resources',
  FINANCE: 'Finance & Accounts',
  MANAGEMENT: 'Executive Management',
  OPERATIONS: 'Operations & Support'
};

const SEED_EMPLOYEES = [
  { id: 1, name: 'Aarav Sharma', email: 'aarav@techcorp.com', role: 'ENGINEERING', type: 'FULL_TIME', basePay: 75000, extra: 12000 },
  { id: 2, name: 'Priya Nair', email: 'priya@techcorp.com', role: 'FINANCE', type: 'FULL_TIME', basePay: 68000, extra: 9500 },
  { id: 3, name: 'Rohan Verma', email: 'rohan@techcorp.com', role: 'OPERATIONS', type: 'PART_TIME', basePay: 450, extra: 90 }
];

function computeEmployee(raw) {
  const basePay = Number(raw.basePay);
  const extra = Number(raw.extra);
  const role = String(raw.role || 'ENGINEERING').toUpperCase();
  const type = String(raw.type || 'FULL_TIME').toUpperCase() === 'PART_TIME' ? 'PART_TIME' : 'FULL_TIME';
  let netSalary;
  if (type === 'PART_TIME') {
    const gross = basePay * Math.trunc(extra);
    netSalary = gross - gross * 0.05;
  } else {
    const hra = basePay * 0.20;
    const tax = (basePay + hra + extra) * 0.10;
    netSalary = basePay + hra + extra - tax;
  }
  return {
    id: Number(raw.id),
    name: String(raw.name || ''),
    email: String(raw.email || ''),
    role,
    department: DEPARTMENTS[role] || role,
    type,
    basePay: Number(basePay.toFixed(2)),
    extra: Number((type === 'PART_TIME' ? Math.trunc(extra) : extra).toFixed(2)),
    netSalary: Number(netSalary.toFixed(2))
  };
}

function loadEmployees(req) {
  const cookieHeader = req.headers.cookie || '';
  const match = cookieHeader.match(/(?:^|;\s*)payroll_employees=([^;]+)/);
  if (match) {
    try {
      const parsed = JSON.parse(Buffer.from(decodeURIComponent(match[1]), 'base64').toString('utf8'));
      if (Array.isArray(parsed)) return parsed.map(computeEmployee);
    } catch (_) {}
  }
  return SEED_EMPLOYEES.map(computeEmployee);
}

function saveEmployeesCookie(res, list) {
  const compact = list.map(({ id, name, email, role, type, basePay, extra }) => ({ id, name, email, role, type, basePay, extra }));
  const encoded = encodeURIComponent(Buffer.from(JSON.stringify(compact), 'utf8').toString('base64'));
  res.setHeader('Set-Cookie', `payroll_employees=${encoded}; Path=/; Max-Age=2592000; SameSite=Lax`);
}

function parseBody(req) {
  if (!req.body) return {};
  if (typeof req.body === 'object') return req.body;
  try { return JSON.parse(req.body); } catch (_) { return {}; }
}

module.exports = { computeEmployee, loadEmployees, saveEmployeesCookie, parseBody };
