const { loadEmployees, parseBody } = require('../_store');

module.exports = (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method !== 'POST') return res.status(405).json({ success: false, message: 'Method not allowed', data: null });

  const { id } = parseBody(req);
  const employees = loadEmployees(req);
  const emp = employees.find(e => e.id === Number(id));
  if (!emp) {
    return res.status(400).json({ success: false, message: `Employee ID ${id} not found.`, data: null });
  }

  const extraLabel = emp.type === 'PART_TIME' ? 'Hours Worked   ' : 'Performance Bonus';
  const receipt =
    '================================================\n' +
    '          TechCorp Payroll Systems\n' +
    '               OFFICIAL PAYSLIP RECEIPT         \n' +
    '================================================\n' +
    `Employee ID    : EMP-${emp.id}\n` +
    `Full Name      : ${emp.name}\n` +
    `Email          : ${emp.email}\n` +
    `Department     : ${emp.department} (${emp.role})\n` +
    `Employment Type: ${emp.type}\n` +
    '------------------------------------------------\n' +
    `Base Rate/Pay  : Rs. ${emp.basePay.toFixed(2)}\n` +
    `${extraLabel}: ${emp.extra.toFixed(2)}\n` +
    'Tax Rate       : 10%\n' +
    '------------------------------------------------\n' +
    `NET SALARY     : Rs. ${emp.netSalary.toFixed(2)}\n` +
    '================================================\n';

  return res.status(200).json({
    success: true,
    message: 'Payslip exported successfully',
    data: { receipt, file: `payslips/payslip_EMP_${emp.id}.txt`, employee: emp }
  });
};
