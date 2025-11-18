/**
 * Script de seed para RitmoFit.
 * Limpia las colecciones principales e inserta usuarios, clases,
 * reservas de ejemplo y objetivos personales listos para usar.
 *
 * Ejecutar con:
 *    node seed_database.js
 *
 * Usa la variable MONGODB_URI si se desea apuntar a otra base.
 */
const mongoose = require('mongoose');
const bcrypt = require('bcrypt');

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/ritmofit';

// === Esquemas mínimos (copia reducida de server.js) ===
const userSchema = new mongoose.Schema({
    email: { type: String, required: true, unique: true },
    password: { type: String },
    otp: { type: String },
    otpExpires: { type: Date },
    isVerified: { type: Boolean, default: false },
    name: { type: String },
    lastName: { type: String },
    memberId: { type: String, unique: true, sparse: true },
    birthDate: { type: Date },
    phoneNumber: { type: String },
    address: { type: String },
    photo: { type: String },
    role: { type: String, enum: ['user', 'admin'], default: 'user' }
}, { collection: 'users' });
const User = mongoose.model('User', userSchema);

const gymClassSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String },
    maxCapacity: { type: Number, required: true },
    currentCapacity: { type: Number, default: 0 },
    discipline: { type: String },
    classDate: { type: Date },
    schedule: {
        day: { type: String, required: true },
        startTime: { type: String, required: true },
        endTime: { type: String, required: true }
    },
    location: { name: { type: String, required: true } },
    professor: { type: String },
    duration: { type: Number }
}, { collection: 'gym_classes' });
const GymClass = mongoose.model('GymClass', gymClassSchema);

const reservationSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    classId: { type: mongoose.Schema.Types.ObjectId, ref: 'GymClass', required: true },
    reservationDate: { type: Date, default: Date.now },
    classDate: { type: Date, required: true },
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

// === Helpers ===
const now = new Date();
const daysToMs = (days) => days * 24 * 60 * 60 * 1000;
function dateFromNow(daysFromNow, time = '18:00') {
    const [hours, minutes] = time.split(':').map(Number);
    const d = new Date(now.getTime() + daysToMs(daysFromNow));
    d.setHours(hours || 0, minutes || 0, 0, 0);
    return d;
}

async function seed() {
    await mongoose.connect(MONGODB_URI, {
        useNewUrlParser: true,
        useUnifiedTopology: true
    });
    console.log(`Conectado a ${MONGODB_URI}`);

    await Promise.all([
        User.deleteMany({}),
        GymClass.deleteMany({}),
        Reservation.deleteMany({}),
        Goal.deleteMany({})
    ]);
    console.log('Colecciones limpiadas.');

    const passwordHash = await bcrypt.hash('RitmoFit123', 10);
    const [userHoracio, userLucia] = await User.insertMany([
        {
            email: 'horacio@example.com',
            password: passwordHash,
            isVerified: true,
            name: 'Horacio',
            lastName: 'Gimenez',
            memberId: 'RF0001',
            phoneNumber: '+54 11 5555-1111',
            role: 'user'
        },
        {
            email: 'lucia@example.com',
            password: passwordHash,
            isVerified: true,
            name: 'Lucia',
            lastName: 'Suarez',
            memberId: 'RF0002',
            phoneNumber: '+54 11 5555-2222',
            role: 'user'
        }
    ]);
    console.log('Usuarios creados.');

    const classBlueprints = [
        {
            name: 'Funcional Palermo 18hs',
            description: 'Circuito intenso con trabajo funcional.',
            maxCapacity: 20,
            discipline: 'Funcional',
            schedule: { day: 'lunes', startTime: '18:00', endTime: '19:00' },
            location: { name: 'Palermo' },
            professor: 'Laura Pérez',
            duration: 60,
            classDate: dateFromNow(2, '18:00')
        },
        {
            name: 'Yoga Flow',
            description: 'Secuencias de movilidad y respiración.',
            maxCapacity: 18,
            discipline: 'Yoga',
            schedule: { day: 'martes', startTime: '07:30', endTime: '08:30' },
            location: { name: 'Belgrano' },
            professor: 'Mariana Díaz',
            duration: 60,
            classDate: dateFromNow(3, '07:30')
        },
        {
            name: 'Cycling Night',
            description: 'Sesión de spinning con foco aeróbico.',
            maxCapacity: 22,
            discipline: 'Spinning',
            schedule: { day: 'miércoles', startTime: '20:00', endTime: '21:00' },
            location: { name: 'Microcentro' },
            professor: 'Diego López',
            duration: 60,
            classDate: dateFromNow(4, '20:00')
        },
        {
            name: 'HIIT Mediodía',
            description: 'Intervalos de alta intensidad.',
            maxCapacity: 16,
            discipline: 'HIIT',
            schedule: { day: 'jueves', startTime: '12:30', endTime: '13:15' },
            location: { name: 'Puerto Madero' },
            professor: 'Sofía Ramos',
            duration: 45,
            classDate: dateFromNow(5, '12:30')
        },
        {
            name: 'Yoga Sunset',
            description: 'Clase relajante para finalizar el día.',
            maxCapacity: 18,
            discipline: 'Yoga',
            schedule: { day: 'viernes', startTime: '19:30', endTime: '20:30' },
            location: { name: 'Palermo' },
            professor: 'Julia Dominguez',
            duration: 60,
            classDate: dateFromNow(6, '19:30')
        }
    ];
    const classes = await GymClass.insertMany(classBlueprints);
    console.log('Clases creadas:', classes.length);

    const reservationsData = [
        // Historial Horacio (para objetivos mensuales)
        {
            userId: userHoracio._id,
            classId: classes[0]._id,
            classDate: dateFromNow(-15, '18:00'),
            status: 'attended'
        },
        {
            userId: userHoracio._id,
            classId: classes[1]._id,
            classDate: dateFromNow(-12, '07:30'),
            status: 'attended'
        },
        {
            userId: userHoracio._id,
            classId: classes[2]._id,
            classDate: dateFromNow(-10, '20:00'),
            status: 'attended'
        },
        {
            userId: userHoracio._id,
            classId: classes[3]._id,
            classDate: dateFromNow(-8, '12:30'),
            status: 'attended'
        },
        {
            userId: userHoracio._id,
            classId: classes[1]._id,
            classDate: dateFromNow(-6, '07:30'),
            status: 'attended'
        },
        {
            userId: userHoracio._id,
            classId: classes[4]._id,
            classDate: dateFromNow(-4, '19:30'),
            status: 'attended'
        },
        {
            userId: userHoracio._id,
            classId: classes[0]._id,
            classDate: dateFromNow(1, '18:00'),
            status: 'active'
        },
        // Reservas Lucia
        {
            userId: userLucia._id,
            classId: classes[2]._id,
            classDate: dateFromNow(-5, '20:00'),
            status: 'attended'
        },
        {
            userId: userLucia._id,
            classId: classes[1]._id,
            classDate: dateFromNow(-3, '07:30'),
            status: 'cancelled'
        },
        {
            userId: userLucia._id,
            classId: classes[3]._id,
            classDate: dateFromNow(2, '12:30'),
            status: 'active'
        }
    ];
    await Reservation.insertMany(reservationsData);
    console.log('Reservas creadas:', reservationsData.length);

    await Goal.insertMany([
        {
            userId: userHoracio._id,
            title: 'Asistir 8 veces este mes',
            targetCount: 8,
            periodType: 'MONTH',
            discipline: null,
            reminderThreshold: 2
        },
        {
            userId: userHoracio._id,
            title: 'Tomar 3 clases de Yoga esta semana',
            targetCount: 3,
            periodType: 'WEEK',
            discipline: 'Yoga',
            reminderThreshold: 1
        },
        {
            userId: userLucia._id,
            title: 'HIIT dos veces por semana',
            targetCount: 2,
            periodType: 'WEEK',
            discipline: 'HIIT',
            reminderThreshold: 1
        }
    ]);
    console.log('Objetivos creados.');

    console.log('Seed completo. Credenciales demo: horacio@example.com / RitmoFit123');
    await mongoose.disconnect();
}

seed()
    .then(() => process.exit(0))
    .catch(err => {
        console.error('Error durante el seed:', err);
        process.exit(1);
    });
