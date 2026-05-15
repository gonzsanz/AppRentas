
CREATE TABLE IF NOT EXISTS ingreso_diario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,          -- formato YYYY-MM-DD
    num_factura INTEGER,
    total REAL NOT NULL DEFAULT 0,
    cuota_iva REAL NOT NULL DEFAULT 0,
    base_imponible REAL NOT NULL DEFAULT 0,
    cerrado INTEGER NOT NULL DEFAULT 0  -- 0 = abierto, 1 = cerrado
);

CREATE TABLE IF NOT EXISTS factura_recibida (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,
    num_factura TEXT NOT NULL,
    proveedor TEXT NOT NULL,
    nif TEXT,
    naturaleza TEXT,              -- EXISTENCIAS, FAC. LUZ, FAC. TELEFONO, REPARACION Y CONSERVACION, etc.
    base REAL NOT NULL DEFAULT 0,
    porcentaje_iva REAL NOT NULL DEFAULT 21,
    cuota_iva REAL NOT NULL DEFAULT 0,
    total REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS otro_gasto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,          -- formato YYYY-MM-DD (primer día del mes)
    mes TEXT NOT NULL,            -- ENERO, FEBRERO, etc.
    anio INTEGER NOT NULL,
    nombre TEXT NOT NULL,         -- S SOCIAL, LOCAL, COMUNIDAD, TPV
    concepto TEXT NOT NULL,       -- AUTONOMOS, PRESTAMO LOCAL, COMUNIDAD MES, TARIFA PLANA
    importe REAL NOT NULL DEFAULT 0
);