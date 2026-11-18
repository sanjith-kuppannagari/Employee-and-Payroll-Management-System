const { parseBody } = require('./_store');

module.exports = (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method !== 'POST') return res.status(405).json({ success: false, message: 'Method not allowed', data: null });

  const { username, password } = parseBody(req);
  const expectedUser = process.env.ADMIN_USERNAME || 'admin';
  const expectedPass = process.env.ADMIN_PASSWORD || 'admin123';

  if (username === expectedUser && password === expectedPass) {
    return res.status(200).json({ success: true, message: 'Login successful', data: 'ADMIN_TOKEN' });
  }
  return res.status(401).json({ success: false, message: 'Invalid credentials', data: null });
};
