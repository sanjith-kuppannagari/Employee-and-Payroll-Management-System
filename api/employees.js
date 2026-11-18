const { computeEmployee, loadEmployees, saveEmployeesCookie, parseBody } = require('./_store');

module.exports = (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  if (req.method === 'OPTIONS') return res.status(204).end();

  const employees = loadEmployees(req);

  if (req.method === 'GET') {
    if (req.query && req.query.id) {
      const emp = employees.find(e => e.id === Number(req.query.id));
      if (!emp) return res.status(400).json({ success: false, message: `Employee ID ${req.query.id} not found.`, data: null });
      return res.status(200).json({ success: true, message: 'Fetched', data: emp });
    }
    return res.status(200).json({ success: true, message: 'Employees loaded', data: employees });
  }

  if (req.method === 'POST' || req.method === 'PUT') {
    const body = parseBody(req);
    const id = Number(body.id || 0);
    const basePay = Number(body.basePay || 0);
    const extra = Number(body.extra || 0);

    if (basePay <= 0 || extra < 0) {
      return res.status(400).json({
        success: false,
        message: 'Invalid salary: Base Pay must be > 0 and Bonus/Hours >= 0.',
        data: null
      });
    }

    let saved;
    if (id > 0) {
      const idx = employees.findIndex(e => e.id === id);
      if (idx === -1) return res.status(400).json({ success: false, message: `Employee ID ${id} not found.`, data: null });
      saved = computeEmployee({ ...body, id });
      employees[idx] = saved;
    } else {
      const nextId = employees.reduce((max, e) => Math.max(max, e.id), 0) + 1;
      saved = computeEmployee({ ...body, id: nextId });
      employees.push(saved);
    }

    saveEmployeesCookie(res, employees);
    return res.status(200).json({ success: true, message: 'Employee saved', data: saved });
  }

  if (req.method === 'DELETE') {
    const body = parseBody(req);
    const id = Number((req.query && req.query.id) || body.id || 0);
    const idx = employees.findIndex(e => e.id === id);
    if (idx === -1) return res.status(400).json({ success: false, message: `Employee ID ${id} not found.`, data: null });
    employees.splice(idx, 1);
    saveEmployeesCookie(res, employees);
    return res.status(200).json({ success: true, message: 'Employee deleted', data: null });
  }

  return res.status(404).json({ success: false, message: 'Endpoint not found', data: null });
};
