const express = require('express');
const cors = require('cors');
const Database = require('better-sqlite3');
const path = require('path');

// ─── App Setup ───────────────────────────────────────────────────────────────
const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(express.json());

// ─── Database Setup ──────────────────────────────────────────────────────────
const dbPath = path.join(__dirname, 'database.db');
const db = new Database(dbPath);

// Enable WAL mode for better concurrent performance
db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

// ─── Table Creation ──────────────────────────────────────────────────────────
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    name       TEXT    NOT NULL,
    email      TEXT    NOT NULL UNIQUE,
    phone      TEXT,
    created_at TEXT    DEFAULT (datetime('now')),
    updated_at TEXT    DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS products (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    description TEXT,
    price       REAL    NOT NULL DEFAULT 0,
    stock       INTEGER NOT NULL DEFAULT 0,
    created_at  TEXT    DEFAULT (datetime('now')),
    updated_at  TEXT    DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS orders (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    product_id  INTEGER NOT NULL,
    quantity    INTEGER NOT NULL DEFAULT 1,
    total_price REAL    NOT NULL DEFAULT 0,
    status      TEXT    NOT NULL DEFAULT 'pending',
    created_at  TEXT    DEFAULT (datetime('now')),
    updated_at  TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS patients (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    name               TEXT    NOT NULL,
    dob                TEXT,
    gender             TEXT,
    phone              TEXT,
    email              TEXT    UNIQUE,
    address            TEXT,
    doctor_name        TEXT,
    doctor_specialty   TEXT,
    doctor_phone       TEXT,
    hospital_name      TEXT,
    hospital_address   TEXT,
    threshold_hr_min   INTEGER DEFAULT 50,
    threshold_hr_max   INTEGER DEFAULT 120,
    threshold_spo2_min REAL    DEFAULT 92,
    threshold_temp_max REAL    DEFAULT 38.0,
    created_at         TEXT    DEFAULT (datetime('now')),
    updated_at         TEXT    DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS emergency_contacts (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    name       TEXT    NOT NULL,
    relation   TEXT,
    phone      TEXT    NOT NULL,
    created_at TEXT    DEFAULT (datetime('now')),
    updated_at TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS medications (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    name       TEXT    NOT NULL,
    dosage     TEXT    NOT NULL,
    frequency  TEXT    NOT NULL,
    created_at TEXT    DEFAULT (datetime('now')),
    updated_at TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS allergies (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    allergen   TEXT    NOT NULL,
    severity   TEXT    NOT NULL,
    created_at TEXT    DEFAULT (datetime('now')),
    updated_at TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS conditions (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id     INTEGER NOT NULL,
    name           TEXT    NOT NULL,
    diagnosed_date TEXT,
    severity       TEXT,
    created_at     TEXT    DEFAULT (datetime('now')),
    updated_at     TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS telemetry_readings (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id  INTEGER NOT NULL,
    heart_rate  REAL    NOT NULL,
    spo2        REAL    NOT NULL,
    temperature REAL    NOT NULL,
    timestamp   TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS accounts (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    username          TEXT    NOT NULL UNIQUE,
    role              TEXT    NOT NULL, -- 'Clinician', 'Guardian', 'Patient'
    email             TEXT    NOT NULL,
    linked_patient_id INTEGER,
    created_at        TEXT    DEFAULT (datetime('now')),
    updated_at        TEXT    DEFAULT (datetime('now')),
    FOREIGN KEY (linked_patient_id) REFERENCES patients(id) ON DELETE SET NULL
  );
`);

console.log('✔  Database initialized — tables ready.');

// ─── Database Seeding ────────────────────────────────────────────────────────
const seedIfEmpty = () => {
  const patientCount = db.prepare('SELECT COUNT(*) AS count FROM patients').get().count;
  if (patientCount > 0) {
    console.log('ℹ  Database already seeded.');
    return;
  }

  console.log('🌱 Seeding demo database with patient John Doe...');

  const insertTransaction = db.transaction(() => {
    // 1. Seed Demo Patient (John Doe)
    const insertPatient = db.prepare(`
      INSERT INTO patients (
        name, dob, gender, phone, email, address,
        doctor_name, doctor_specialty, doctor_phone,
        hospital_name, hospital_address,
        threshold_hr_min, threshold_hr_max, threshold_spo2_min, threshold_temp_max
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);
    const pInfo = insertPatient.run(
      'John Doe', '1985-04-12', 'Male', '555-0199', 'john.doe@example.com', '123 Elm St, Springfield',
      'Dr. Robert Chen', 'Cardiology', '555-0120',
      'Springfield Medical Center', '100 Medical Plaza, Springfield',
      50, 120, 92, 38.0
    );
    const patientId = pInfo.lastInsertRowid;

    // 2. Seed Emergency Contacts
    const insertContact = db.prepare(`
      INSERT INTO emergency_contacts (patient_id, name, relation, phone)
      VALUES (?, ?, ?, ?)
    `);
    insertContact.run(patientId, 'Jane Doe', 'Spouse', '555-0198');
    insertContact.run(patientId, 'Mary Doe', 'Mother', '555-0155');

    // 3. Seed Medications
    const insertMed = db.prepare(`
      INSERT INTO medications (patient_id, name, dosage, frequency)
      VALUES (?, ?, ?, ?)
    `);
    insertMed.run(patientId, 'Lisinopril', '10mg', 'Once daily');
    insertMed.run(patientId, 'Atorvastatin', '20mg', 'Once daily at bedtime');

    // 4. Seed Allergies
    const insertAllergy = db.prepare(`
      INSERT INTO allergies (patient_id, allergen, severity)
      VALUES (?, ?, ?)
    `);
    insertAllergy.run(patientId, 'Penicillin', 'High');
    insertAllergy.run(patientId, 'Peanuts', 'Moderate');

    // 5. Seed Conditions
    const insertCondition = db.prepare(`
      INSERT INTO conditions (patient_id, name, diagnosed_date, severity)
      VALUES (?, ?, ?, ?)
    `);
    insertCondition.run(patientId, 'Hypertension', '2021-03-15', 'Mild');
    insertCondition.run(patientId, 'Hyperlipidemia', '2022-08-20', 'Mild');

    // 6. Seed 100 Telemetry Readings
    const insertTelemetry = db.prepare(`
      INSERT INTO telemetry_readings (patient_id, heart_rate, spo2, temperature, timestamp)
      VALUES (?, ?, ?, ?, ?)
    `);
    
    const now = new Date();
    for (let i = 99; i >= 0; i--) {
      const time = new Date(now.getTime() - i * 5 * 60 * 1000);
      const isoString = time.toISOString().replace('T', ' ').substring(0, 19);

      // Realistic vital signs:
      // Heart Rate: 68 - 85 bpm, occasionally fluctuates
      // SpO2: 94% - 99%
      // Temperature: 36.6°C - 37.4°C
      const hr = Math.round(72 + Math.sin(i / 5) * 8 + (Math.random() - 0.5) * 6);
      const spo2 = Math.min(100, Math.round(97 + Math.cos(i / 10) * 1.5 + (Math.random() - 0.5) * 1));
      const temp = (36.8 + Math.sin(i / 12) * 0.3 + (Math.random() - 0.5) * 0.2).toFixed(1);

      insertTelemetry.run(patientId, hr, spo2, parseFloat(temp), isoString);
    }

    // 7. Seed Demo Accounts
    const insertAccount = db.prepare(`
      INSERT INTO accounts (username, role, email, linked_patient_id)
      VALUES (?, ?, ?, ?)
    `);
    insertAccount.run('clinician_demo', 'Clinician', 'clinician@medsentry.io', null);
    insertAccount.run('guardian_demo', 'Guardian', 'guardian@medsentry.io', patientId);
    insertAccount.run('patient_demo', 'Patient', 'john.doe@example.com', patientId);
  });

  insertTransaction();
  console.log('✅ Seeding complete.');
};

seedIfEmpty();

// ─── Helpers ─────────────────────────────────────────────────────────────────
function success(res, data, status = 200) {
  return res.status(status).json({ success: true, data });
}

function fail(res, message, status = 400) {
  return res.status(status).json({ success: false, error: message });
}

// ─── Health Check & API Metadata ─────────────────────────────────────────────
app.get('/api', (_req, res) => {
  success(res, {
    message: 'MedSentry Backend Health Monitoring API is running',
    endpoints: {
      users:              '/api/users',
      products:           '/api/products',
      orders:             '/api/orders',
      patients:           '/api/patients',
      emergencyContacts:  '/api/emergency-contacts',
      medications:        '/api/medications',
      allergies:          '/api/allergies',
      conditions:         '/api/conditions',
      telemetryReadings:  '/api/telemetry-readings',
      accounts:           '/api/accounts'
    },
  });
});

// ═════════════════════════════════════════════════════════════════════════════
//  PATIENTS CRUD
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/patients', (_req, res) => {
  const rows = db.prepare('SELECT * FROM patients ORDER BY id DESC').all();
  success(res, rows);
});

// Get patient complete dashboard (patient + all sub-tables)
app.get('/api/patients/:id/dashboard', (req, res) => {
  const patient = db.prepare('SELECT * FROM patients WHERE id = ?').get(req.params.id);
  if (!patient) return fail(res, 'Patient not found', 404);

  const emergencyContacts = db.prepare('SELECT * FROM emergency_contacts WHERE patient_id = ?').all(req.params.id);
  const medications = db.prepare('SELECT * FROM medications WHERE patient_id = ?').all(req.params.id);
  const allergies = db.prepare('SELECT * FROM allergies WHERE patient_id = ?').all(req.params.id);
  const conditions = db.prepare('SELECT * FROM conditions WHERE patient_id = ?').all(req.params.id);
  const telemetry = db.prepare('SELECT * FROM telemetry_readings WHERE patient_id = ? ORDER BY id DESC LIMIT 100').all(req.params.id);

  success(res, {
    patient,
    emergencyContacts,
    medications,
    allergies,
    conditions,
    telemetry
  });
});

app.get('/api/patients/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM patients WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Patient not found', 404);
  success(res, row);
});

app.post('/api/patients', (req, res) => {
  const {
    name, dob, gender, phone, email, address,
    doctor_name, doctor_specialty, doctor_phone,
    hospital_name, hospital_address,
    threshold_hr_min, threshold_hr_max, threshold_spo2_min, threshold_temp_max
  } = req.body;

  if (!name) return fail(res, 'name is required');

  try {
    const stmt = db.prepare(`
      INSERT INTO patients (
        name, dob, gender, phone, email, address,
        doctor_name, doctor_specialty, doctor_phone,
        hospital_name, hospital_address,
        threshold_hr_min, threshold_hr_max, threshold_spo2_min, threshold_temp_max
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);
    const info = stmt.run(
      name, dob || null, gender || null, phone || null, email || null, address || null,
      doctor_name || null, doctor_specialty || null, doctor_phone || null,
      hospital_name || null, hospital_address || null,
      threshold_hr_min ?? 50, threshold_hr_max ?? 120, threshold_spo2_min ?? 92.0, threshold_temp_max ?? 38.0
    );
    const row = db.prepare('SELECT * FROM patients WHERE id = ?').get(info.lastInsertRowid);
    success(res, row, 201);
  } catch (err) {
    if (err.message.includes('UNIQUE')) return fail(res, 'Email already exists', 409);
    fail(res, err.message, 500);
  }
});

app.put('/api/patients/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM patients WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Patient not found', 404);

  const fields = [
    'name', 'dob', 'gender', 'phone', 'email', 'address',
    'doctor_name', 'doctor_specialty', 'doctor_phone',
    'hospital_name', 'hospital_address',
    'threshold_hr_min', 'threshold_hr_max', 'threshold_spo2_min', 'threshold_temp_max'
  ];

  const updates = {};
  fields.forEach(field => {
    updates[field] = req.body[field] !== undefined ? req.body[field] : existing[field];
  });

  try {
    db.prepare(`
      UPDATE patients
      SET
        name = ?, dob = ?, gender = ?, phone = ?, email = ?, address = ?,
        doctor_name = ?, doctor_specialty = ?, doctor_phone = ?,
        hospital_name = ?, hospital_address = ?,
        threshold_hr_min = ?, threshold_hr_max = ?, threshold_spo2_min = ?, threshold_temp_max = ?,
        updated_at = datetime('now')
      WHERE id = ?
    `).run(
      updates.name, updates.dob, updates.gender, updates.phone, updates.email, updates.address,
      updates.doctor_name, updates.doctor_specialty, updates.doctor_phone,
      updates.hospital_name, updates.hospital_address,
      updates.threshold_hr_min, updates.threshold_hr_max, updates.threshold_spo2_min, updates.threshold_temp_max,
      req.params.id
    );
    const updated = db.prepare('SELECT * FROM patients WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    if (err.message.includes('UNIQUE')) return fail(res, 'Email already exists', 409);
    fail(res, err.message, 500);
  }
});

app.delete('/api/patients/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM patients WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Patient not found', 404);
  db.prepare('DELETE FROM patients WHERE id = ?').run(req.params.id);
  success(res, { message: 'Patient deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  EMERGENCY CONTACTS CRUD
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/emergency-contacts', (_req, res) => {
  const rows = db.prepare('SELECT * FROM emergency_contacts ORDER BY id DESC').all();
  success(res, rows);
});

app.get('/api/emergency-contacts/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Contact not found', 404);
  success(res, row);
});

app.post('/api/emergency-contacts', (req, res) => {
  const { patient_id, name, relation, phone } = req.body;
  if (!patient_id || !name || !phone) return fail(res, 'patient_id, name, and phone are required');

  if (!db.prepare('SELECT id FROM patients WHERE id = ?').get(patient_id)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    const stmt = db.prepare(`
      INSERT INTO emergency_contacts (patient_id, name, relation, phone)
      VALUES (?, ?, ?, ?)
    `);
    const info = stmt.run(patient_id, name, relation || null, phone);
    const row = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(info.lastInsertRowid);
    success(res, row, 201);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.put('/api/emergency-contacts/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Contact not found', 404);

  const { patient_id, name, relation, phone } = req.body;
  const newPatientId = patient_id ?? existing.patient_id;

  if (patient_id && !db.prepare('SELECT id FROM patients WHERE id = ?').get(newPatientId)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    db.prepare(`
      UPDATE emergency_contacts
      SET patient_id = ?, name = ?, relation = ?, phone = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      newPatientId,
      name ?? existing.name,
      relation ?? existing.relation,
      phone ?? existing.phone,
      req.params.id
    );
    const updated = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.delete('/api/emergency-contacts/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM emergency_contacts WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Contact not found', 404);
  db.prepare('DELETE FROM emergency_contacts WHERE id = ?').run(req.params.id);
  success(res, { message: 'Contact deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  MEDICATIONS CRUD
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/medications', (_req, res) => {
  const rows = db.prepare('SELECT * FROM medications ORDER BY id DESC').all();
  success(res, rows);
});

app.get('/api/medications/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM medications WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Medication not found', 404);
  success(res, row);
});

app.post('/api/medications', (req, res) => {
  const { patient_id, name, dosage, frequency } = req.body;
  if (!patient_id || !name || !dosage || !frequency) {
    return fail(res, 'patient_id, name, dosage, and frequency are required');
  }

  if (!db.prepare('SELECT id FROM patients WHERE id = ?').get(patient_id)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    const stmt = db.prepare(`
      INSERT INTO medications (patient_id, name, dosage, frequency)
      VALUES (?, ?, ?, ?)
    `);
    const info = stmt.run(patient_id, name, dosage, frequency);
    const row = db.prepare('SELECT * FROM medications WHERE id = ?').get(info.lastInsertRowid);
    success(res, row, 201);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.put('/api/medications/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM medications WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Medication not found', 404);

  const { patient_id, name, dosage, frequency } = req.body;
  const newPatientId = patient_id ?? existing.patient_id;

  if (patient_id && !db.prepare('SELECT id FROM patients WHERE id = ?').get(newPatientId)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    db.prepare(`
      UPDATE medications
      SET patient_id = ?, name = ?, dosage = ?, frequency = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      newPatientId,
      name ?? existing.name,
      dosage ?? existing.dosage,
      frequency ?? existing.frequency,
      req.params.id
    );
    const updated = db.prepare('SELECT * FROM medications WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.delete('/api/medications/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM medications WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Medication not found', 404);
  db.prepare('DELETE FROM medications WHERE id = ?').run(req.params.id);
  success(res, { message: 'Medication deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  ALLERGIES CRUD
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/allergies', (_req, res) => {
  const rows = db.prepare('SELECT * FROM allergies ORDER BY id DESC').all();
  success(res, rows);
});

app.get('/api/allergies/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM allergies WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Allergy not found', 404);
  success(res, row);
});

app.post('/api/allergies', (req, res) => {
  const { patient_id, allergen, severity } = req.body;
  if (!patient_id || !allergen || !severity) return fail(res, 'patient_id, allergen, and severity are required');

  if (!db.prepare('SELECT id FROM patients WHERE id = ?').get(patient_id)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    const stmt = db.prepare(`
      INSERT INTO allergies (patient_id, allergen, severity)
      VALUES (?, ?, ?)
    `);
    const info = stmt.run(patient_id, allergen, severity);
    const row = db.prepare('SELECT * FROM allergies WHERE id = ?').get(info.lastInsertRowid);
    success(res, row, 201);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.put('/api/allergies/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM allergies WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Allergy not found', 404);

  const { patient_id, allergen, severity } = req.body;
  const newPatientId = patient_id ?? existing.patient_id;

  if (patient_id && !db.prepare('SELECT id FROM patients WHERE id = ?').get(newPatientId)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    db.prepare(`
      UPDATE allergies
      SET patient_id = ?, allergen = ?, severity = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      newPatientId,
      allergen ?? existing.allergen,
      severity ?? existing.severity,
      req.params.id
    );
    const updated = db.prepare('SELECT * FROM allergies WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.delete('/api/allergies/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM allergies WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Allergy not found', 404);
  db.prepare('DELETE FROM allergies WHERE id = ?').run(req.params.id);
  success(res, { message: 'Allergy deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  CONDITIONS CRUD
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/conditions', (_req, res) => {
  const rows = db.prepare('SELECT * FROM conditions ORDER BY id DESC').all();
  success(res, rows);
});

app.get('/api/conditions/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM conditions WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Condition not found', 404);
  success(res, row);
});

app.post('/api/conditions', (req, res) => {
  const { patient_id, name, diagnosed_date, severity } = req.body;
  if (!patient_id || !name) return fail(res, 'patient_id and name are required');

  if (!db.prepare('SELECT id FROM patients WHERE id = ?').get(patient_id)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    const stmt = db.prepare(`
      INSERT INTO conditions (patient_id, name, diagnosed_date, severity)
      VALUES (?, ?, ?, ?)
    `);
    const info = stmt.run(patient_id, name, diagnosed_date || null, severity || null);
    const row = db.prepare('SELECT * FROM conditions WHERE id = ?').get(info.lastInsertRowid);
    success(res, row, 201);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.put('/api/conditions/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM conditions WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Condition not found', 404);

  const { patient_id, name, diagnosed_date, severity } = req.body;
  const newPatientId = patient_id ?? existing.patient_id;

  if (patient_id && !db.prepare('SELECT id FROM patients WHERE id = ?').get(newPatientId)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    db.prepare(`
      UPDATE conditions
      SET patient_id = ?, name = ?, diagnosed_date = ?, severity = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      newPatientId,
      name ?? existing.name,
      diagnosed_date ?? existing.diagnosed_date,
      severity ?? existing.severity,
      req.params.id
    );
    const updated = db.prepare('SELECT * FROM conditions WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.delete('/api/conditions/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM conditions WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Condition not found', 404);
  db.prepare('DELETE FROM conditions WHERE id = ?').run(req.params.id);
  success(res, { message: 'Condition deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  TELEMETRY READINGS CRUD
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/telemetry-readings', (req, res) => {
  const limit = req.query.limit ? parseInt(req.query.limit) : 100;
  const rows = db.prepare('SELECT * FROM telemetry_readings ORDER BY id DESC LIMIT ?').all(limit);
  success(res, rows);
});

app.get('/api/telemetry-readings/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM telemetry_readings WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Reading not found', 404);
  success(res, row);
});

app.post('/api/telemetry-readings', (req, res) => {
  const { patient_id, heart_rate, spo2, temperature, timestamp } = req.body;
  if (!patient_id || heart_rate == null || spo2 == null || temperature == null) {
    return fail(res, 'patient_id, heart_rate, spo2, and temperature are required');
  }

  if (!db.prepare('SELECT id FROM patients WHERE id = ?').get(patient_id)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    const stmt = db.prepare(`
      INSERT INTO telemetry_readings (patient_id, heart_rate, spo2, temperature, timestamp)
      VALUES (?, ?, ?, ?, COALESCE(?, datetime('now')))
    `);
    const info = stmt.run(patient_id, heart_rate, spo2, temperature, timestamp || null);
    const row = db.prepare('SELECT * FROM telemetry_readings WHERE id = ?').get(info.lastInsertRowid);
    success(res, row, 201);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.put('/api/telemetry-readings/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM telemetry_readings WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Reading not found', 404);

  const { patient_id, heart_rate, spo2, temperature, timestamp } = req.body;
  const newPatientId = patient_id ?? existing.patient_id;

  if (patient_id && !db.prepare('SELECT id FROM patients WHERE id = ?').get(newPatientId)) {
    return fail(res, 'Patient not found', 404);
  }

  try {
    db.prepare(`
      UPDATE telemetry_readings
      SET patient_id = ?, heart_rate = ?, spo2 = ?, temperature = ?, timestamp = ?
      WHERE id = ?
    `).run(
      newPatientId,
      heart_rate ?? existing.heart_rate,
      spo2 ?? existing.spo2,
      temperature ?? existing.temperature,
      timestamp ?? existing.timestamp,
      req.params.id
    );
    const updated = db.prepare('SELECT * FROM telemetry_readings WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.delete('/api/telemetry-readings/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM telemetry_readings WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Reading not found', 404);
  db.prepare('DELETE FROM telemetry_readings WHERE id = ?').run(req.params.id);
  success(res, { message: 'Reading deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  DEMO ACCOUNTS CRUD
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/accounts', (_req, res) => {
  const rows = db.prepare('SELECT * FROM accounts ORDER BY id DESC').all();
  success(res, rows);
});

app.get('/api/accounts/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM accounts WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Account not found', 404);
  success(res, row);
});

app.post('/api/accounts', (req, res) => {
  const { username, role, email, linked_patient_id } = req.body;
  if (!username || !role || !email) return fail(res, 'username, role, and email are required');

  if (linked_patient_id && !db.prepare('SELECT id FROM patients WHERE id = ?').get(linked_patient_id)) {
    return fail(res, 'Linked Patient not found', 404);
  }

  try {
    const stmt = db.prepare(`
      INSERT INTO accounts (username, role, email, linked_patient_id)
      VALUES (?, ?, ?, ?)
    `);
    const info = stmt.run(username, role, email, linked_patient_id || null);
    const row = db.prepare('SELECT * FROM accounts WHERE id = ?').get(info.lastInsertRowid);
    success(res, row, 201);
  } catch (err) {
    if (err.message.includes('UNIQUE')) return fail(res, 'Username already exists', 409);
    fail(res, err.message, 500);
  }
});

app.put('/api/accounts/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM accounts WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Account not found', 404);

  const { username, role, email, linked_patient_id } = req.body;
  const newPatientId = linked_patient_id !== undefined ? linked_patient_id : existing.linked_patient_id;

  if (newPatientId && !db.prepare('SELECT id FROM patients WHERE id = ?').get(newPatientId)) {
    return fail(res, 'Linked Patient not found', 404);
  }

  try {
    db.prepare(`
      UPDATE accounts
      SET username = ?, role = ?, email = ?, linked_patient_id = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      username ?? existing.username,
      role ?? existing.role,
      email ?? existing.email,
      newPatientId,
      req.params.id
    );
    const updated = db.prepare('SELECT * FROM accounts WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    if (err.message.includes('UNIQUE')) return fail(res, 'Username already exists', 409);
    fail(res, err.message, 500);
  }
});

app.delete('/api/accounts/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM accounts WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Account not found', 404);
  db.prepare('DELETE FROM accounts WHERE id = ?').run(req.params.id);
  success(res, { message: 'Account deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  ORIGINAL USERS CRUD (Backward Compatibility)
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/users', (_req, res) => {
  const rows = db.prepare('SELECT * FROM users ORDER BY id DESC').all();
  success(res, rows);
});

app.get('/api/users/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'User not found', 404);
  success(res, row);
});

app.post('/api/users', (req, res) => {
  const { name, email, phone } = req.body;
  if (!name || !email) return fail(res, 'name and email are required');

  try {
    const stmt = db.prepare('INSERT INTO users (name, email, phone) VALUES (?, ?, ?)');
    const info = stmt.run(name, email, phone || null);
    const user = db.prepare('SELECT * FROM users WHERE id = ?').get(info.lastInsertRowid);
    success(res, user, 201);
  } catch (err) {
    if (err.message.includes('UNIQUE')) return fail(res, 'Email already exists', 409);
    fail(res, err.message, 500);
  }
});

app.put('/api/users/:id', (req, res) => {
  const { name, email, phone } = req.body;
  const existing = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'User not found', 404);

  try {
    db.prepare(`
      UPDATE users SET name = ?, email = ?, phone = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      name  ?? existing.name,
      email ?? existing.email,
      phone ?? existing.phone,
      req.params.id,
    );
    const updated = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    if (err.message.includes('UNIQUE')) return fail(res, 'Email already exists', 409);
    fail(res, err.message, 500);
  }
});

app.delete('/api/users/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'User not found', 404);
  db.prepare('DELETE FROM users WHERE id = ?').run(req.params.id);
  success(res, { message: 'User deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  ORIGINAL PRODUCTS CRUD (Backward Compatibility)
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/products', (_req, res) => {
  const rows = db.prepare('SELECT * FROM products ORDER BY id DESC').all();
  success(res, rows);
});

app.get('/api/products/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
  if (!row) return fail(res, 'Product not found', 404);
  success(res, row);
});

app.post('/api/products', (req, res) => {
  const { name, description, price, stock } = req.body;
  if (!name || price == null) return fail(res, 'name and price are required');

  try {
    const stmt = db.prepare(
      'INSERT INTO products (name, description, price, stock) VALUES (?, ?, ?, ?)'
    );
    const info = stmt.run(name, description || null, price, stock ?? 0);
    const product = db.prepare('SELECT * FROM products WHERE id = ?').get(info.lastInsertRowid);
    success(res, product, 201);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.put('/api/products/:id', (req, res) => {
  const { name, description, price, stock } = req.body;
  const existing = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Product not found', 404);

  try {
    db.prepare(`
      UPDATE products SET name = ?, description = ?, price = ?, stock = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      name        ?? existing.name,
      description ?? existing.description,
      price       ?? existing.price,
      stock       ?? existing.stock,
      req.params.id,
    );
    const updated = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.delete('/api/products/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Product not found', 404);
  db.prepare('DELETE FROM products WHERE id = ?').run(req.params.id);
  success(res, { message: 'Product deleted', id: Number(req.params.id) });
});

// ═════════════════════════════════════════════════════════════════════════════
//  ORIGINAL ORDERS CRUD (Backward Compatibility)
// ═════════════════════════════════════════════════════════════════════════════
app.get('/api/orders', (_req, res) => {
  const rows = db.prepare(`
    SELECT
      o.*,
      u.name  AS user_name,
      u.email AS user_email,
      p.name  AS product_name
    FROM orders o
    LEFT JOIN users    u ON o.user_id    = u.id
    LEFT JOIN products p ON o.product_id = p.id
    ORDER BY o.id DESC
  `).all();
  success(res, rows);
});

app.get('/api/orders/:id', (req, res) => {
  const row = db.prepare(`
    SELECT
      o.*,
      u.name  AS user_name,
      u.email AS user_email,
      p.name  AS product_name
    FROM orders o
    LEFT JOIN users    u ON o.user_id    = u.id
    LEFT JOIN products p ON o.product_id = p.id
    WHERE o.id = ?
  `).get(req.params.id);
  if (!row) return fail(res, 'Order not found', 404);
  success(res, row);
});

app.post('/api/orders', (req, res) => {
  const { user_id, product_id, quantity, total_price, status } = req.body;
  if (!user_id || !product_id) return fail(res, 'user_id and product_id are required');

  if (!db.prepare('SELECT id FROM users WHERE id = ?').get(user_id)) {
    return fail(res, 'User not found', 404);
  }
  if (!db.prepare('SELECT id FROM products WHERE id = ?').get(product_id)) {
    return fail(res, 'Product not found', 404);
  }

  try {
    const stmt = db.prepare(
      'INSERT INTO orders (user_id, product_id, quantity, total_price, status) VALUES (?, ?, ?, ?, ?)'
    );
    const info = stmt.run(user_id, product_id, quantity ?? 1, total_price ?? 0, status ?? 'pending');
    const order = db.prepare('SELECT * FROM orders WHERE id = ?').get(info.lastInsertRowid);
    success(res, order, 201);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.put('/api/orders/:id', (req, res) => {
  const { user_id, product_id, quantity, total_price, status } = req.body;
  const existing = db.prepare('SELECT * FROM orders WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Order not found', 404);

  const newUserId    = user_id    ?? existing.user_id;
  const newProductId = product_id ?? existing.product_id;
  if (user_id && !db.prepare('SELECT id FROM users WHERE id = ?').get(newUserId)) {
    return fail(res, 'User not found', 404);
  }
  if (product_id && !db.prepare('SELECT id FROM products WHERE id = ?').get(newProductId)) {
    return fail(res, 'Product not found', 404);
  }

  try {
    db.prepare(`
      UPDATE orders
      SET user_id = ?, product_id = ?, quantity = ?, total_price = ?, status = ?, updated_at = datetime('now')
      WHERE id = ?
    `).run(
      newUserId,
      newProductId,
      quantity    ?? existing.quantity,
      total_price ?? existing.total_price,
      status      ?? existing.status,
      req.params.id,
    );
    const updated = db.prepare('SELECT * FROM orders WHERE id = ?').get(req.params.id);
    success(res, updated);
  } catch (err) {
    fail(res, err.message, 500);
  }
});

app.delete('/api/orders/:id', (req, res) => {
  const existing = db.prepare('SELECT * FROM orders WHERE id = ?').get(req.params.id);
  if (!existing) return fail(res, 'Order not found', 404);
  db.prepare('DELETE FROM orders WHERE id = ?').run(req.params.id);
  success(res, { message: 'Order deleted', id: Number(req.params.id) });
});

// ─── Start Server ────────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`🚀  MedSentry API running at http://localhost:${PORT}`);
  console.log(`   Base endpoint:  http://localhost:${PORT}/api`);
});
