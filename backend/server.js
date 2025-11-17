const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const nodemailer = require('nodemailer');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken'); 
// const { v4: uuidv4 } = require('uuid');
// No se usa, se puede remover

const app = express();
const port = 3000;
// 🔑 CLAVE SECRETA PARA FIRMAR LOS TOKENS JWT
// ¡IMPORTANTE! En producción, esto debe estar en una variable de entorno.
const JWT_SECRET = process.env.JWT_SECRET || 'tu_clave_secreta_super_segura_aqui'; 

app.use(express.json());
app.use(cors());

// Conexión a MongoDB
mongoose.connect('mongodb://localhost:27017/ritmofit', {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    // useFindAndModify: false, // Descomentar si usas versiones anteriores a Mongoose 6
    // useCreateIndex: true, // Descomentar si usas versiones anteriores a Mongoose 6
})
.then(() => console.log('Conectado a MongoDB'))
.catch(err => console.error('Error al conectar a MongoDB', err));
// =========================================================================
// ESQUEMAS Y MODELOS (SE MANTIENEN IGUAL)
// =========================================================================

const userSchema = new mongoose.Schema({
    email: { type: String, required: true, unique: true },
    password: { type: String, required: false },
    otp: { type: String, required: false },
    otpExpires: { type: Date, required: false },
    isVerified: { type: Boolean, default: false },
    name: { type: String },
    lastName: { type: String },
    // memberId: Se hará único con sparse: true para permitir nulos
    memberId: { 
type: String, unique: true, sparse: true },
    birthDate: { type: Date },
    phoneNumber: { type: String },
    address: { type: String },
    photo: { type: String },
    // 💡 AÑADIDO: Campo para roles (ej: 'user', 'admin')
    role: { type: String, enum: ['user', 'admin'], default: 'user' }
}, { collection: 'users' });
const User = mongoose.model('User', userSchema);

const gymClassSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String },
    maxCapacity: { type: Number, required: true },
    currentCapacity: { type: Number, default: 0 },
    // 💡 Añadir 'discipline' como alias de 'name' o como campo separado si es necesario
    discipline: { type: String }, 
    classDate: { type: Date }, // Se añade al modelo para la persistencia de clases únicas
    schedule: {
  
      day: { type: String, required: true },
        startTime: { type: String, required: true },
        endTime: { type: String, required: true }
    },
    location: {
        name: { type: String, required: true }
    },
    professor: { type: String },
    duration: { type: Number }
}, { collection: 'gym_classes' });
const GymClass = mongoose.model('GymClass', gymClassSchema);

const reservationSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    classId: { type: mongoose.Schema.Types.ObjectId, ref: 'GymClass', required: true },
    reservationDate: { type: Date, default: Date.now },
    classDate: { type: Date, required: true }, // Fecha y hora de inicio de la clase reservada
    status: { type: String, enum: ['active', 'cancelled', 'attended', 'expired'], default: 'active' }
}, { collection: 'reservations' });
const Reservation = mongoose.model('Reservation', reservationSchema);

const goalSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    title: { type: String, required: true },
    targetCount: { type: Number, required: true },
    periodType: { type: String, enum: ['WEEK', 'MONTH'], required: true },
    discipline: { type: String, default: null },
    reminderThreshold: { type: Number, default: 1 }
}, { collection: 'goals', timestamps: true });
const Goal = mongoose.model('Goal', goalSchema);

const counterSchema = new mongoose.Schema({
    _id: { type: String, required: true },
    seq: { type: Number, default: 0 }
}, { collection: 'counters' });
const Counter = mongoose.model('Counter', counterSchema);

// =========================================================================
// MIDDLEWARE DE AUTENTICACIÓN Y AUTORIZACIÓN (JWT)
// =========================================================================

/**
 * Middleware para verificar el token JWT en la cabecera "Authorization".
*/
const auth = (req, res, next) => {
    try {
        // Extrae el token, eliminando "Bearer "
        const token = req.header('Authorization')?.replace('Bearer ', '');
if (!token) {
            return res.status(401).json({ error: 'Authentication required. No token provided.' });
}
        // Verifica el token usando la clave secreta
        const decoded = jwt.verify(token, JWT_SECRET);
req.user = decoded; // Adjunta el payload (userId, email, role) al request
        next();
} catch (e) {
        // Token inválido o expirado
        res.status(401).json({ error: 'Invalid or expired token. Please re-authenticate.' });
}
};

/**
 * Middleware para restringir el acceso solo a administradores.
 * Debe usarse DESPUÉS del middleware 'auth'.
*/
const adminAuth = (req, res, next) => {
    // Asume que el payload de JWT incluye 'role'
    if (req.user && req.user.role === 'admin') {
        next();
} else {
        res.status(403).json({ error: 'Authorization required. Not an administrator.' });
    }
};
// =========================================================================
// HELPERS GENERALES (SE MANTIENEN IGUAL)
// =========================================================================

// Configuración de Nodemailer
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'uadepruebas@gmail.com',
        pass: 'gbdn gquc glch olbv' 
    }
});
function generateOTP() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

async function getNextSequenceValue(sequenceName) {
    const counter = await Counter.findByIdAndUpdate(
        { _id: sequenceName },
        { $inc: { seq: 1 } },
        { new: true, upsert: true } 
    );
return counter.seq.toString().padStart(4, '0');
}

function calculateNextClassDate(dayOfWeek, startTime, baseDate) {
  const normalizedDay = dayOfWeek.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
  const daysMap = { 'domingo':0,'lunes':1,'martes':2,'miercoles':3,'jueves':4,'viernes':5,'sabado':6 };
  const targetDow = daysMap[normalizedDay] ?? null;
  if (targetDow === null) { console.warn(`Día no reconocido: ${dayOfWeek}`); return null; }

  const today = baseDate ? new Date(baseDate) : new Date();
  let classDate = new Date(today);
  const todayDow = today.getDay();
  let delta = (targetDow - todayDow);
  if (delta < 0) delta += 7;
  classDate.setDate(classDate.getDate() + delta);
  if (delta === 0) {
    const [h,m] = (startTime || '00:00').split(':').map(Number);
    if (h < today.getHours() || (h === today.getHours() && m <= today.getMinutes())) {
      classDate.setDate(classDate.getDate() + 7);
    }
  }
  classDate.setHours(0,0,0,0);
  return classDate;
}


// Helper para parsear la hora y combinarla con una fecha base
function parseTimeToDate(baseDate, timeStr) {
    const [h,m] = (timeStr || '00:00').split(':').map(Number);
const d = new Date(baseDate);
    d.setHours(h||0, m||0, 0, 0);
    return d;
}

// Helper para convertir el día de la semana a español y normalizar
function parseClientDate(s) {
  if (!s) return null;
  let y, m, d;
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) { // YYYY-MM-DD
    [y, m, d] = s.split('-').map(Number);
  } else {
    const mm = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(s); // DD/MM/YYYY
    if (!mm) return null;
    d = +mm[1]; m = +mm[2]; y = +mm[3];
  }
  // Crear fecha en MEDIODÍA LOCAL para evitar saltos UTC/DST
  return new Date(y, m - 1, d, 12, 0, 0, 0);
}

function getSpanishDay(dateString) {
  const d = parseClientDate(dateString);
  if (!d) return null;
  const days = ['domingo','lunes','martes','miercoles','jueves','viernes','sabado'];
  return days[d.getDay()];
}

function normalizeDiscipline(value) {
    return value ? value.trim().toLowerCase() : null;
}

function getPeriodRange(periodType = 'MONTH', referenceDate = new Date()) {
    const start = new Date(referenceDate);
    const end = new Date(referenceDate);

    if (periodType === 'WEEK') {
        const currentDay = start.getDay(); // 0 domingo ... 6 sabado
        const diffToMonday = (currentDay === 0 ? -6 : 1) - currentDay;
        start.setDate(start.getDate() + diffToMonday);
        start.setHours(0, 0, 0, 0);

        end.setTime(start.getTime());
        end.setDate(start.getDate() + 6);
        end.setHours(23, 59, 59, 999);
    } else {
        start.setDate(1);
        start.setHours(0, 0, 0, 0);

        end.setMonth(end.getMonth() + 1);
        end.setDate(0); // ultimo dia del mes anterior
        end.setHours(23, 59, 59, 999);
    }

    return { start, end };
}

function formatPeriodLabel(periodType, start, end) {
    const months = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre'];
    if (periodType === 'WEEK') {
        const formatDate = (date) => {
            return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}`;
        };
        return `Semana ${formatDate(start)} - ${formatDate(end)}`;
    }
    const monthLabel = months[start.getMonth()] || '';
    return `${monthLabel.charAt(0).toUpperCase() + monthLabel.slice(1)} ${start.getFullYear()}`;
}

async function buildGoalPayload(goal, userId) {
    const { start, end } = getPeriodRange(goal.periodType);
    const baseQuery = {
        userId,
        status: 'attended',
        classDate: { $gte: start, $lte: end }
    };

    const reservations = await Reservation.find(baseQuery).populate('classId');
    let filteredReservations = reservations;

    if (goal.discipline) {
        const goalDiscipline = normalizeDiscipline(goal.discipline);
        filteredReservations = reservations.filter(r => {
            if (!r.classId) return false;
            const discipline = normalizeDiscipline(r.classId.discipline || r.classId.name);
            return discipline === goalDiscipline;
        });
    }

    const progress = filteredReservations.length;
    const remaining = Math.max(goal.targetCount - progress, 0);
    const reminderThreshold = goal.reminderThreshold ?? 1;
    let status = 'IN_PROGRESS';
    if (remaining === 0) {
        status = 'COMPLETED';
    } else if (remaining <= reminderThreshold) {
        status = 'NEAR_COMPLETION';
    }

    const progressPercentage = goal.targetCount > 0 ? Math.min(progress / goal.targetCount, 1) : 0;

    return {
        id: goal._id.toString(),
        userId: goal.userId.toString(),
        title: goal.title,
        targetCount: goal.targetCount,
        periodType: goal.periodType,
        discipline: goal.discipline,
        currentProgress: progress,
        remaining,
        status,
        progressPercentage,
        periodLabel: formatPeriodLabel(goal.periodType, start, end),
        reminderThreshold,
        updatedAt: goal.updatedAt
    };
}


// Rutas base
app.get('/', (req, res) => {
    res.send('API de RitmoFit en funcionamiento!');
});
// =========================================================================
// 🚀 RUTAS DE AUTENTICACIÓN (MANTIENEN SU LÓGICA)
// =========================================================================

// 1. Registro: /api/auth/register-send-otp
app.post('/api/auth/register-send-otp', async (req, res) => {
    const email = req.body.email ? req.body.email.trim() : null;
    const password = req.body.password ? req.body.password.trim() : null;
    
    try {
        let user = await User.findOne({ email });
        if (user && user.isVerified) {
            return res.status(400).json({ message: 'El usuario ya existe y está verificado.' });
    
    }

        const hashedPassword = await bcrypt.hash(password, 10);
        const otpCode = generateOTP();

        if (!user) {
            const newMemberId = await getNextSequenceValue('memberId'); 
            user = new User({ 
                email, 
            
    password: hashedPassword, 
                otp: otpCode, 
                otpExpires: new Date(Date.now() + 10 * 60000), 
                isVerified: false,
                memberId: newMemberId,
                role: 'user' //Asignar rol por defecto
            });
} else {
            user.password = hashedPassword;
            user.otp = otpCode;
user.otpExpires = new Date(Date.now() + 10 * 60000); 
            user.isVerified = false;
}

        await user.save();
const mailOptions = {
            from: 'uadepruebas@gmail.com',
            to: email,
            subject: 'Código de Verificación de Registro para RitmoFit',
            text: `Tu código de verificación para completar tu registro es: ${otpCode}`
        };
transporter.sendMail(mailOptions, (error, info) => {
            if (error) {
                console.error(error);
                return res.status(500).json({ message: 'Error al enviar el correo de registro' });
            }
            res.status(200).json({ message: 'Código OTP enviado al correo electrónico para registro.' });
      
  });
    } catch (error) {
        if (error.code === 11000) { 
            return res.status(409).json({ message: 'El correo electrónico ya está registrado.' });
}
        res.status(500).json({ message: 'Error en el servidor durante el registro', error });
    }
});
// 2. Login Directo: /api/auth/login
app.post('/api/auth/login', async (req, res) => {
    const email = req.body.email ? req.body.email.trim() : null;
    const password = req.body.password ? req.body.password.trim() : null;

    try {
        // 1. Buscar usuario
        const user = await User.findOne({ email });

        if (!user) {
            return res.status(401).json({ message: 'Credenciales incorrectas.' });
        }

        // 2. Comparar contraseña (usando bcrypt)
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(401).json({ message: 'Credenciales incorrectas.' });
        }

        // 3. Opcional: Verificar si el usuario está verificado (solo si usas el OTP para el registro)
        // if (!user.isVerified) {
        //     return res.status(401).json({ message: 'Cuenta no verificada. Por favor, verifica tu email.' });
        // }

        // 4. Generar Token JWT
        const token = jwt.sign(
            { userId: user._id, email: user.email, role: user.role }, 
            JWT_SECRET, 
            { expiresIn: '1h' } // Ajusta el tiempo de expiración según tu política
        );

        // 5. Preparar objeto de usuario (evitar devolver el hash de la contraseña)
        const userObject = user.toObject();
        // Eliminar el hash de la contraseña, OTP y datos sensibles
        delete userObject.password; 
        delete userObject.otp;
        delete userObject.otpExpires;
        
        // 6. Enviar respuesta de éxito
        res.status(200).json({ 
            message: 'Sesión iniciada exitosamente.', 
            token: token, 
            user: {
                id: userObject._id,
                name: userObject.name || null, 
                email: userObject.email,
                lastName: userObject.lastName || null,
                memberId: userObject.memberId || null,
                birthDate: userObject.birthDate || null,
                phoneNumber: userObject.phoneNumber || null,
                address: userObject.address || null,
                profilePhotoUrl: userObject.photo || null,
                role: userObject.role 
            }
        });

    } catch (error) {
        console.error('Error en el servidor durante el login:', error);
        res.status(500).json({ message: 'Error en el servidor al iniciar sesión', error });
    }
});


// 4. Solicitud de Contraseña: /api/auth/request-password-reset
app.post('/api/auth/request-password-reset', async (req, res) => {
    const email = req.body.email ? req.body.email.trim() : null;
    
    try {
        const user = await User.findOne({ email });
        if (!user) {
            return res.status(404).json({ message: 'Usuario no encontrado.' });
        }
        
        user.otp = generateOTP();
 
       user.otpExpires = new Date(Date.now() + 10 * 60000); 
        await user.save();

        const mailOptions = {
            from: 'uadepruebas@gmail.com',
            to: email,
            subject: 'Código de Recuperación de Contraseña para RitmoFit',
            text: `Tu código de verificación para restablecer tu 
contraseña es: ${user.otp}`
        };

        transporter.sendMail(mailOptions, (error, info) => {
            if (error) {
                console.error(error);
                return res.status(500).json({ message: 'Error al enviar el correo de recuperación' });
}
            res.status(200).json({ message: 'Código OTP de recuperación enviado al correo electrónico.' });
});
    } catch (error) {
        res.status(500).json({ message: 'Error en el servidor durante la solicitud de recuperación', error });
}
});

// NUEVA RUTA: Solo verifica si el OTP de recuperación es válido
app.post('/api/auth/verify-reset-otp', async (req, res) => {
    const { email, otp } = req.body;
    
    // Busca y verifica el OTP como lo hicimos en la solución anterior
    const user = await User.findOne({ email });

    if (!user || user.otp !== otp || user.otpExpires < Date.now()) {
        return res.status(400).json({ message: 'Código OTP incorrecto o ha expirado.' });
    }

    // Si es válido, avisa al cliente que puede proceder
    res.status(200).json({ message: 'Verificación de OTP exitosa. Procede a cambiar la contraseña.' });
});

// 5. Restablecer Contraseña: /api/auth/reset-password
app.post('/api/auth/reset-password', async (req, res) => {
    // 🔑 Mantenemos la extracción de datos
    const email = req.body.email ? req.body.email.trim() : null;
    const otp = req.body.otp ? req.body.otp.trim() : null;
    const newPassword = req.body.newPassword ? req.body.newPassword.trim() : null;

    try {
        // 1. Buscamos el usuario SOLO por email (asumiendo que es único)
        const user = await User.findOne({ email });

        // Verificación 1: Usuario encontrado
        if (!user) {
            // Usamos el mensaje genérico de error que tenías para evitar dar pistas.
            return res.status(400).json({ message: 'Código OTP, correo electrónico incorrecto, o ha expirado.' });
        }

        // Verificación 2: El OTP es incorrecto O ha expirado
        if (user.otp !== otp || user.otpExpires < Date.now()) {
            return res.status(400).json({ message: 'Código OTP, correo electrónico incorrecto, o ha expirado.' });
        }
        
        // Si llegamos aquí, el OTP es correcto y no ha expirado.
        
        // 2. Hash de la nueva contraseña
        // Es mejor generar el salt en lugar de usar un número fijo (como 10)
        const saltRounds = 10;
        const hashedPassword = await bcrypt.hash(newPassword, saltRounds);
        
        // 3. Aplicar cambios y guardar
        user.password = hashedPassword;
        user.otp = null; // Limpiar OTP para que no se pueda reutilizar
        user.otpExpires = null; // Limpiar la fecha de expiración
        await user.save();
        
        res.status(200).json({ message: 'Contraseña restablecida exitosamente.' });
    } catch (error) {
        console.error('Error en el servidor al restablecer contraseña:', error);
        res.status(500).json({ message: 'Error en el servidor al restablecer contraseña', error });
    }
});
// =========================================================================
// 🔒 RUTAS DE PERFIL (PROTEGIDAS)
// =========================================================================

// Ruta para obtener el perfil del usuario por su ID
app.get('/api/profile/:userId', auth, async (req, res) => {
    const { userId } = req.params;
    // 🔑 Opcional: Verificar que el token pertenezca al userId solicitado
    if (req.user.userId !== userId) {
        return res.status(403).json({ message: 'Acceso denegado: Token no corresponde al usuario solicitado.' });
    }
    
    try {
        const user = await User.findById(userId);
  
      if (user) {
            const userProfile = {
                id: user._id,
                email: user.email,
                name: user.name || null,
                lastName: user.lastName || null,
     
           memberId: user.memberId || null,
                birthDate: user.birthDate || null,
                phoneNumber: user.phoneNumber ||
null,
                address: user.address ||
null,
                profilePhotoUrl: user.photo ||
null,
                isVerified: user.isVerified,
                role: user.role
            };
res.status(200).json(userProfile);
        } else {
            res.status(404).json({ message: 'Usuario no encontrado' });
}
    } catch (error) {
        res.status(500).json({ message: 'Error al obtener el perfil', error });
}
});

// Ruta para actualizar los datos del perfil del usuario por su ID
app.put('/api/profile/:userId', auth, async (req, res) => {
    const { userId } = req.params;
    const { name, lastName, birthDate, phoneNumber, address, photo } = req.body;
    // 🔑 Opcional: Verificar que el token pertenezca al userId solicitado
    if (req.user.userId !== userId) {
        return res.status(403).json({ message: 'Acceso denegado: Token no corresponde al usuario solicitado.' });
    }
    
    try {
    
    const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({ message: 'Usuario no encontrado.' });
        }

        user.name = name ?? user.name;
        user.lastName = lastName ?? user.lastName;
        user.birthDate = birthDate ?? user.birthDate;
        user.phoneNumber = phoneNumber ?? user.phoneNumber;
        
user.address = address ?? user.address;
        user.photo = photo ?? user.photo;

        await user.save();

        const updatedProfile = {
            id: user._id,
            email: user.email,
            name: user.name ||
null,
            lastName: user.lastName ||
null,
            memberId: user.memberId ||
null,
            birthDate: user.birthDate ||
null,
            phoneNumber: user.phoneNumber ||
null,
            address: user.address ||
null,
            profilePhotoUrl: user.photo ||
null,
            isVerified: user.isVerified,
            role: user.role
        };
res.status(200).json(updatedProfile); 
    } catch (error) {
        res.status(500).json({ message: 'Error al actualizar el perfil.', error: error.message });
}
});


// =========================================================================
// 🔒 RUTAS DE CLASES (PROTEGIDAS)
// =========================================================================

// Ruta para obtener el listado de clases con filtros
app.get('/api/classes', auth, async (req, res) => {
    try {
        const { location, discipline, date } = req.query;
        let filter = {};

        if (location) {
            filter['location.name'] = location;
        }

        // Si se filtra por disciplina, usamos el campo 'name' del modelo GymClass
        if (discipline) {
            filter.name = { $regex: new RegExp(discipline, 'i') };
        }

        let classes = await GymClass.find(filter).lean();

        let targetDayOfWeek = null;
        let baseDate = null;
        if (date) {
          baseDate = parseClientDate(date);
          targetDayOfWeek = getSpanishDay(date);
          if (!baseDate || !targetDayOfWeek) {
            return res.status(400).json({ code:'INVALID_DATE', hint:'Use YYYY-MM-DD o DD/MM/YYYY' });
          }
        }

        classes = classes.map(cls => {
            const calculatedDate = calculateNextClassDate(cls.schedule.day, cls.schedule.startTime, baseDate);
return {
                id: cls._id.toString(), 
                
                // 🔑 CRÍTICO 2: Aseguramos la existencia de campos esperados por Kotlin
                name: cls.name,
                description: cls.description ||
null,
                maxCapacity: cls.maxCapacity,
                currentCapacity: cls.currentCapacity,
                schedule: cls.schedule,
                location: cls.location,
                discipline: cls.discipline ||
cls.name, // Usar discipline si existe, sino name
                professor: cls.professor ||
null,  
                duration: cls.duration ||
null,    
                
                // Agregamos la fecha real de la próxima clase en formato YYYY-MM-DD
                classDate: calculatedDate ?
calculatedDate.toISOString().split('T')[0] : null, 
            };
}).filter(cls => {
            // Aplicar el filtro por día de la semana si existe
            if (targetDayOfWeek) {
                const classDay = cls.schedule.day.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
                return classDay === targetDayOfWeek;
            }
           
 return true;
        });

        res.status(200).json(classes);
} catch (error) {
        res.status(500).json({ message: 'Error al obtener las clases filtradas', error });
}
});

// Ruta para obtener los detalles de una clase específica 
app.get('/api/classes/:classId', auth, async (req, res) => {
    const { classId } = req.params;
    try {
        const gymClass = await GymClass.findById(classId).lean();
        if (gymClass) {
            // ✅ Asegurar que la respuesta tenga los campos esperados por Kotlin
            const calculatedDate = calculateNextClassDate(gymClass.schedule.day, gymClass.schedule.startTime);
        
    const formattedClass = {
                id: gymClass._id.toString(),
                ...gymClass,
                discipline: gymClass.discipline || gymClass.name,
                professor: gymClass.professor || null,
                duration: gymClass.duration || null,
   
             classDate: calculatedDate ? calculatedDate.toISOString().split('T')[0] : null,
            };
            res.status(200).json(formattedClass);
        } else {
            res.status(404).json({ message: 'Clase no encontrada' });
        }
    } catch (error) {
        res.status(500).json({ message: 'Error al obtener los detalles de la clase', error });
    }
});

// Ruta para obtener las sedes y disciplinas disponibles para los filtros 
app.get('/api/filters', auth, async (req, res) => {
    try {
        const locations = await GymClass.distinct('location.name');
        // Usamos 'discipline' si lo tienes, sino 'name'
        const disciplines = await GymClass.distinct('discipline') || await GymClass.distinct('name');
        res.status(200).json({ locations, disciplines });
    } catch (error) {
        res.status(500).json({ message: 'Error al obtener los filtros', error });
    }
});


// 🚨 RUTAS DE ADMINISTRACIÓN DE CLASES (NUEVAS - PROTEGIDAS POR adminAuth) 🚨

// Ruta para crear una nueva clase (Solo Admin)
app.post('/api/classes', auth, adminAuth, async (req, res) => {
    try {
        const newClass = new GymClass(req.body);
        await newClass.save();
        res.status(201).json(newClass);
    } catch (error) {
        console.error('Error al crear la clase:', error);
        res.status(500).json({ message: 
'Error al crear la clase', error: error.message });
    }
});
// Ruta para actualizar una clase existente (Solo Admin)
app.put('/api/classes/:classId', auth, adminAuth, async (req, res) => {
    const { classId } = req.params;
    try {
        const updatedClass = await GymClass.findByIdAndUpdate(classId, req.body, { new: true, runValidators: true });
        
        if (!updatedClass) {
            return res.status(404).json({ message: 'Clase no encontrada.' });
        }
        
res.status(200).json(updatedClass);
    } catch (error) {
        console.error('Error al actualizar la clase:', error);
        res.status(500).json({ message: 'Error al actualizar la clase', error: error.message });
    }
});
// Ruta para eliminar una clase (Solo Admin)
app.delete('/api/classes/:classId', auth, adminAuth, async (req, res) => {
    const { classId } = req.params;
    try {
        const result = await GymClass.findByIdAndDelete(classId);
        
        if (!result) {
            return res.status(404).json({ message: 'Clase no encontrada.' });
        }

        // 💡 Opcional: Cancelar todas las reservas asociadas a esta clase
        await Reservation.updateMany({ classId: classId, status: 'active' }, { status: 'cancelled' });

        res.status(200).json({ message: 'Clase eliminada exitosamente y reservas canceladas.' });
    } catch (error) {
        console.error('Error al eliminar la clase:', error);
        res.status(500).json({ message: 'Error al eliminar la clase', error: error.message });
    }
});
// =========================================================================
// 🔒 RUTAS DE RESERVAS (PROTEGIDAS)
// =========================================================================

// Ruta para obtener las reservas ACTIVAS de un usuario (Mis Reservas)
app.get('/api/reservations/:userId', auth, async (req, res) => {
    const { userId } = req.params;
    // 🔑 Verificar que el token pertenezca al userId solicitado
    if (req.user.userId !== userId) {
        return res.status(403).json({ message: 'Acceso denegado.' });
    }

    try {
        let reservations = await Reservation.find({ userId, status: 'active' }).populate('classId');
        
        const now = new Date();
        const promises = [];

        // Lógica de expiración (solo para las activas)
        for (const r of reservations) {
            // Asegúrate de que la clase y el horario existan
            if (r.status === 'active' && r.classId && r.classDate) {
                // Si la hora de fin no existe, usamos la de inicio para el cálculo
                const endDateTime = parseTimeToDate(r.classDate, r.classId.schedule.endTime || r.classId.schedule.startTime);

                if (endDateTime < now) {
                    r.status = 'expired';
                    promises.push(r.save());
                    
                    // Liberar el cupo
                    promises.push(GymClass.findByIdAndUpdate(
                        r.classId._id,
                        { $inc: { currentCapacity: -1 } },
                        { new: true }
                    ));
                }
            }
        }
        await Promise.all(promises);

        // Volver a obtener la lista solo con activas, excluyendo las que expiraron en este ciclo
        // 🔑 CLAVE: Usar .lean() para mapear los campos a formato Kotlin.
        const rawReservations = await Reservation.find({ userId, status: 'active' })
            .populate('classId')
            .lean();
        const formattedReservations = rawReservations.map(r => {
            const classData = r.classId;

            // Mapeo del objeto GymClass anidado a formato Kotlin (id en lugar de _id)
            const formattedClass = classData ? {
                id: classData._id.toString(),
                // ✅ CORRECCIÓN 1: Aseguramos que los campos de STRING obligatorios no sean null
                name: classData.name || "",
                // ✅ CORRECCIÓN 2: 'description' debe ser un string no nulo
                description: classData.description || "",
                maxCapacity: classData.maxCapacity,
                currentCapacity: classData.currentCapacity,
                schedule: classData.schedule,
                location: classData.location,
                // ✅ CORRECCIÓN 3: 'discipline' debe ser un string no nulo
                discipline: classData.discipline || classData.name || "",
                professor: classData.professor || null,
                duration: classData.duration || null,
                // ✅ CORRECCIÓN 4: 'classDate' debe ser un string (o null si el modelo Kotlin lo permite, 
                // pero si da error de 'null' literal, usamos cadena vacía)
                classDate: classData.classDate ? new Date(classData.classDate).toISOString().split('T')[0] : "",
            } : null;

            return {
                id: r._id.toString(), // Mapea _id de la reserva a id
                userId: r.userId.toString(),
                classId: formattedClass, // Objeto GymClass completo mapeado (o null)
                reservationDate: r.reservationDate.toISOString(),
                classDate: r.classDate.toISOString(), // La fecha real de la clase (con hora)
                status: r.status,
            };
        });

        res.status(200).json(formattedReservations); // Responde con el formato corregido
    } catch (error) {
        res.status(500).json({ message: 'Error al obtener las reservas', error });
    }
});

// Ruta para crear una nueva reserva
app.post('/api/reservations', auth, async (req, res) => {
    let { userId, classId, gymClassId } = req.body;
    
    // 🔑 Verificar que el token pertenezca al userId solicitado
    if (req.user.userId !== userId) {
        return res.status(403).json({ message: 'Acceso denegado.' });
    }
    
    try {
        classId = classId || gymClassId;
        if (!userId || !classId) {
 
           return res.status(400).json({ message: 'userId y classId son requeridos.' });
        }
        const gymClass = await GymClass.findById(classId);
        if (!gymClass) {
            return res.status(404).json({ message: 'Clase no encontrada.' });
        }

        if (gymClass.currentCapacity >= gymClass.maxCapacity) {
            return 
res.status(400).json({ message: 'No hay cupo disponible para esta clase.' });
        }

        // CALCULAR FECHA REAL DE LA CLASE (Fecha y hora de inicio)
        const dateBase = calculateNextClassDate(gymClass.schedule.day, gymClass.schedule.startTime);
if (!dateBase) {
             return res.status(400).json({ message: 'Día de la semana de la clase no válido.' });
}
        
        // 🔑 CRÍTICO: Usamos el resultado de calculateNextClassDate (sin hora) y le agregamos la hora
        const startDateTime = parseTimeToDate(dateBase, gymClass.schedule.startTime);
const endDateTime = parseTimeToDate(dateBase, gymClass.schedule.endTime || gymClass.schedule.startTime);

        // Validación: no permitir reservar en el pasado
        if (endDateTime < new Date()) {
            return res.status(400).json({ message: 'La clase ya ocurrió. No es posible reservar.' });
}

        // Validación: evitar doble reserva del mismo turno (por fecha real y hora de inicio)
        const existingSame = await Reservation.findOne({ 
            userId, 
            classId, 
            classDate: startDateTime, // Se compara con el valor exacto guardado
            status: { $in: ['active'] } 
 
       });
        if (existingSame) {
            return res.status(400).json({ message: 'Ya tienes una reserva activa para esta clase.' });
}

        // Validación: evitar solapamiento con otras reservas activas (comparando start/end times del mismo día de la semana)
        const userActive = await Reservation.find({ userId, status: 'active' }).populate('classId');
const overlaps = userActive.some(r => {
            const c = r.classId;
            if (!c) return false;
            
            // 🔑 Comparar el día de la semana
            const classDay = c.schedule.day.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
            const newClassDay = gymClass.schedule.day.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");

  
          // ⚠️ La validación de solapamiento por hora solo tiene sentido si es el mismo día
            if (classDay !== newClassDay) return false;
            
            // 🔑 Usamos la fecha real de la clase que estamos reservando (startDateTime) como base para comparar los horarios
            const rStart = parseTimeToDate(startDateTime, 
c.schedule.startTime);
            const rEnd = parseTimeToDate(startDateTime, c.schedule.endTime || c.schedule.startTime);
            
            // Lógica de solapamiento: [A, B) y [C, D) se solapan si A < D y C < B
            return (startDateTime < rEnd && endDateTime > rStart);
});
        if (overlaps) {
            return res.status(400).json({ message: 'Tienes otra reserva activa que se superpone en el mismo horario.' });
}

        // Usamos la fecha y hora de inicio real de la clase en la nueva reserva
        const newReservation = new Reservation({ 
            userId, 
            classId, 
            classDate: startDateTime, 
            reservationDate: new Date()
        });
await newReservation.save();

        // Actualizar cupo en GymClass 
        gymClass.currentCapacity = (gymClass.currentCapacity || 0) + 1;
await gymClass.save();

        // reemplaza la línea: res.status(201).json(newReservation);
         const populated = await Reservation.findById(newReservation._id).populate('classId').lean();

         const c = populated.classId;
         const classDto = c ? {
           id: c._id.toString(),
           name: c.name || "",
           description: c.description || "",
           maxCapacity: c.maxCapacity,
           currentCapacity: c.currentCapacity,     // ya actualizado
           schedule: c.schedule,
           location: c.location,
           discipline: c.discipline || c.name || "",
           professor: c.professor || null,
           duration: c.duration || null,
           classDate: c.classDate ? new Date(c.classDate).toISOString().split('T')[0] : ""
         } : null;

         const resDto = {
           id: populated._id.toString(),
           userId: populated.userId.toString(),
           classId: classDto,                       // <- objeto, no string
           reservationDate: populated.reservationDate.toISOString(),
           classDate: populated.classDate.toISOString(),
           status: populated.status
         };

         return res.status(201).json(resDto);

    } catch (error) {
        console.error('Error al crear la reserva:', error);
res.status(500).json({ message: 'Error al crear la reserva', error: error.message });
    }
});
// Ruta para cancelar una reserva 
app.post('/api/reservations/cancel/:reservationId', auth, async (req, res) => {
    const { reservationId } = req.params;
    try {
        const reservation = await Reservation.findById(reservationId);
        if (!reservation) {
            return res.status(404).json({ message: 'Reserva no encontrada.' });
        }
        
        // 🔑 Verificar que el token pertenezca al usuario de la reserva
        if (req.user.userId !== reservation.userId.toString()) {
            return res.status(403).json({ message: 'Acceso denegado: No tienes permiso para cancelar esta reserva.' });
        }

        if (reservation.status !== 'active') {
            return res.status(400).json({ message: 'Solo se pueden cancelar reservas activas.' });
        }
        
      
  // 🔑 CRÍTICO: Validar que la cancelación sea antes de que termine la clase
        const gymClass = await GymClass.findById(reservation.classId);
        if (gymClass && reservation.classDate) {
             const endDateTime = parseTimeToDate(reservation.classDate, gymClass.schedule.endTime ||
gymClass.schedule.startTime);
             if (endDateTime < new Date()) {
                 // Esta situación debería ser manejada por la lógica de expiración en GET /api/reservations/:userId
                 // Pero como fallback, evitamos cancelaciones de clases ya terminadas
                 return res.status(400).json({ message: 'No se puede cancelar una clase que ya ha finalizado.' });
}
        }


        reservation.status = 'cancelled';
await reservation.save();

        // Liberar cupo
        if (gymClass && (gymClass.currentCapacity||0) > 0) {
            gymClass.currentCapacity -= 1;
await gymClass.save();
        }

        res.status(200).json({ message: 'Reserva cancelada exitosamente' });
} catch (error) {
        res.status(500).json({ message: 'Error al cancelar la reserva', error });
}
});

// =========================================================================
// 🔒 RUTAS DE HISTORIAL (PROTEGIDAS)
// =========================================================================

// =========================================================================
// RUTAS DE OBJETIVOS PERSONALES (PROTEGIDAS)
// =========================================================================

const ALLOWED_PERIODS = ['WEEK', 'MONTH'];

function validateGoalRequest(payload = {}) {
    const errors = [];
    if (!payload.title || !payload.title.trim()) {
        errors.push('El nombre del objetivo es obligatorio.');
    }
    const target = Number(payload.targetCount);
    if (!Number.isFinite(target) || target <= 0) {
        errors.push('El objetivo debe tener una meta numérica mayor a 0.');
    }
    if (!payload.periodType || !ALLOWED_PERIODS.includes(String(payload.periodType).toUpperCase())) {
        errors.push('El tipo de periodo es inválido. Usa WEEK o MONTH.');
    }
    return { errors, target };
}

app.get('/api/goals/:userId', auth, async (req, res) => {
    const { userId } = req.params;
    if (req.user.userId !== userId) {
        return res.status(403).json({ message: 'No tienes permisos para ver estos objetivos.' });
    }

    try {
        const goals = await Goal.find({ userId }).sort({ createdAt: -1 });
        const payload = await Promise.all(goals.map(goal => buildGoalPayload(goal, userId)));
        res.json(payload);
    } catch (error) {
        console.error('Error al obtener objetivos:', error);
        res.status(500).json({ message: 'No se pudieron cargar los objetivos', error });
    }
});

app.post('/api/goals', auth, async (req, res) => {
    const { errors, target } = validateGoalRequest(req.body);
    if (errors.length) {
        return res.status(400).json({ message: errors.join(' ') });
    }

    try {
        const newGoal = await Goal.create({
            userId: req.user.userId,
            title: req.body.title.trim(),
            targetCount: target,
            periodType: String(req.body.periodType).toUpperCase(),
            discipline: req.body.discipline ? req.body.discipline.trim() : null,
            reminderThreshold: req.body.reminderThreshold ?? 1
        });

        const payload = await buildGoalPayload(newGoal, req.user.userId);
        res.status(201).json(payload);
    } catch (error) {
        console.error('Error al crear objetivo:', error);
        res.status(500).json({ message: 'No se pudo crear el objetivo', error });
    }
});

app.put('/api/goals/:goalId', auth, async (req, res) => {
    const { goalId } = req.params;
    const { errors, target } = validateGoalRequest(req.body);
    if (errors.length) {
        return res.status(400).json({ message: errors.join(' ') });
    }

    try {
        const goal = await Goal.findById(goalId);
        if (!goal) {
            return res.status(404).json({ message: 'Objetivo no encontrado.' });
        }

        if (goal.userId.toString() !== req.user.userId) {
            return res.status(403).json({ message: 'No está autorizado a editar este objetivo.' });
        }

        goal.title = req.body.title.trim();
        goal.targetCount = target;
        goal.periodType = String(req.body.periodType).toUpperCase();
        goal.discipline = req.body.discipline ? req.body.discipline.trim() : null;
        goal.reminderThreshold = req.body.reminderThreshold ?? goal.reminderThreshold;
        await goal.save();

        const payload = await buildGoalPayload(goal, req.user.userId);
        res.json(payload);
    } catch (error) {
        console.error('Error al actualizar objetivo:', error);
        res.status(500).json({ message: 'No se pudo actualizar el objetivo', error });
    }
});

app.delete('/api/goals/:goalId', auth, async (req, res) => {
    const { goalId } = req.params;

    try {
        const goal = await Goal.findById(goalId);
        if (!goal) {
            return res.status(404).json({ message: 'Objetivo no encontrado.' });
        }

        if (goal.userId.toString() !== req.user.userId) {
            return res.status(403).json({ message: 'No está autorizado a eliminar este objetivo.' });
        }

        await goal.deleteOne();
        res.json({ message: 'Objetivo eliminado.' });
    } catch (error) {
        console.error('Error al eliminar objetivo:', error);
        res.status(500).json({ message: 'No se pudo eliminar el objetivo', error });
    }
});

// Ruta para obtener el historial de asistencias de un usuario
app.get('/api/history/:userId', auth, async (req, res) => {
    const { userId } = req.params;
    const { startDate, endDate } = req.query; 

    // 🔑 Verificar que el token pertenezca al userId solicitado
    if (req.user.userId !== userId) {
        return res.status(403).json({ message: 'Acceso denegado.' });
    }

    try {
        let findCriteria = { 
 
           userId, 
            status: { $in: ['attended', 'cancelled', 'expired'] } 
        };

        // Aplicamos el filtro de fecha real de la clase (classDate)
        if (startDate || endDate) {
            findCriteria.classDate = {};

            if (startDate) {
   
             // Inicio del día
                findCriteria.classDate.$gte = new Date(new Date(startDate).setHours(0, 0, 0, 0));
}

            if (endDate) {
                // Fin del día
                findCriteria.classDate.$lte = new Date(new Date(endDate).setHours(23, 59, 59, 999));
}
        }

        const history = await Reservation.find(findCriteria)
            .populate('classId')
            .sort({ classDate: -1 });
            
        // 🔑 Mapear la respuesta para el historial (similar a Mis Reservas)
        const formattedHistory = history.map(r => {
            const classData = r.classId;
            // Solo devolvemos la clase si existe (puede ser null si la clase fue borrada)
            const formattedClass = classData ? {
                id: classData._id.toString(),
                name: classData.name,
                discipline: classData.discipline || classData.name,
                // ...otros campos mínimos necesarios para el historial
            } : null;

            return {
                id: r._id.toString(), // Mapea _id de la reserva a id
                userId: r.userId.toString(),
                classId: formattedClass, // Objeto GymClass mapeado (o null)
                reservationDate: r.reservationDate.toISOString(),
                classDate: r.classDate.toISOString(), // La fecha real de la clase (con hora)
                status: r.status,
            };
        });

res.status(200).json(formattedHistory); // Responde con el formato corregido
    } catch (error) {
        console.error('Error al obtener el historial:', error);
res.status(500).json({ message: 'Error al obtener el historial de asistencias', error });
    }
});
// Iniciar el servidor
app.listen(port, () => {
    console.log(`Servidor escuchando en http://localhost:${port}`);
});
