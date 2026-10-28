# Política de Privacidad de OutFix

**Última actualización:** 28 de octubre de 2026

En **OutFix**, nos comprometemos a proteger la privacidad y los datos personales de nuestros usuarios. Esta Política de Privacidad describe cómo recopilamos, usamos, almacenamos y protegemos tu información al utilizar la aplicación móvil OutFix.

---

### 1. Información que recopilamos

1. **Información de la Cuenta:**
   - Nombre de usuario y correo electrónico cuando decides registrarte o iniciar sesión para sincronizar tus datos.
2. **Fotografías y Prendas de Ropa:**
   - Las imágenes que capturas o seleccionas se procesan para recortar, clasificar y organizar tu catálogo de ropa en tu armario virtual.
3. **Datos de Uso y Preferencias:**
   - Planificaciones semanales de atuendos, registro de ropa prestada y preferencias de apariencia (como intensidad de efectos visuales).

---

### 2. Uso de Permisos del Dispositivo

- **Cámara (`android.permission.CAMERA`):**
  Utilizada exclusivamente para permitirte tomar fotografías de tus prendas de ropa dentro de la app para su digitalización y análisis con Machine Learning (ML Kit) en el dispositivo.
- **Acceso a la Galería:**
  Utilizamos el selector estándar del sistema Android (Photo Picker). OutFix **no** solicita permisos de lectura amplia de almacenamiento ni accede a tus fotos privadas no seleccionadas.
- **Notificaciones (`android.permission.POST_NOTIFICATIONS`):**
  Utilizadas para recordatorios programados (como devolución de prendas prestadas o recordatorio diario de atuendo), previa autorización del usuario.
- **Internet (`android.permission.INTERNET`):**
  Utilizado para sincronizar tus prendas y cuenta con tu base de datos cuando hay conexión disponible.

---

### 3. Almacenamiento y Seguridad de los Datos

- **Arquitectura Local-First:** Tus prendas, fotos y combinaciones se guardan principalmente de manera local en tu dispositivo mediante SQLite cifrado/Room.
- **Sincronización:** Cuando existe conexión a internet, los datos se transmiten mediante protocolos seguros cifrados en tránsito (HTTPS/TLS).
- **No venta de datos:** **Nunca vendemos, alquilamos ni compartimos tus datos personales o imágenes con terceros.**

---

### 4. Eliminación de Cuenta y Datos (Google Play Compliance)

Respetamos tu derecho al olvido y a la eliminación de tus datos:
- **Desde la aplicación:** Puedes eliminar tu cuenta y todos tus datos personales asociados en cualquier momento navegando a **Perfil > Eliminar cuenta**. Esta acción eliminará permanentemente tu guardarropa, combinaciones, historial y sesión.
- **Solicitud externa:** También puedes solicitar la eliminación manual de tus datos enviando un correo a soporte con el asunto "Solicitud de Eliminación de Cuenta".

---

### 5. Contacto

Si tienes dudas o inquietudes sobre esta Política de Privacidad, puedes contactar al equipo de desarrollo a través del repositorio oficial de la aplicación.
