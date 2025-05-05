# BookLoginApp 📚

BookLoginApp es una aplicación de biblioteca personal que permite a los usuarios buscar libros, guardar favoritos, gestionar notas y mantener un historial de búsqueda para facilitar la exploración de nuevas lecturas. La aplicación utiliza tecnología moderna como Firebase para autenticación y almacenamiento de datos.

## Características principales 🚀

- **Buscar libros:** Busca libros a través de la API de Open Library.
- **Favoritos:** Guarda tus libros favoritos para acceder a ellos rápidamente.
- **Notas personales:** Agrega notas a tus libros favoritos para recordar lo que aprendiste o lo que más te gustó.
- **Historial de búsqueda:** Mantén un registro de tus búsquedas recientes y accede a ellas fácilmente.
- **Estado de lectura:** Guarda el estado de lectura de un libro (No iniciado, Leyendo, Terminado).
- **Autenticación:** Inicio de sesión y cierre de sesión mediante Firebase Authentication.

## Capturas de pantalla 📸

![image](https://github.com/user-attachments/assets/3246e320-a588-4022-afaa-4b2d28407b54)

![image](https://github.com/user-attachments/assets/95c806cb-4d22-4da8-a890-bdee15ca28a2)

![image](https://github.com/user-attachments/assets/e06406bd-85f5-4fb0-a138-365716de5d58)

![image](https://github.com/user-attachments/assets/086eabde-8da2-4782-af5e-d8cebfca97ff)




## Tecnologías utilizadas 🛠️

- **Kotlin**: Lenguaje principal para el desarrollo de la aplicación.
- **Jetpack Compose**: Framework de UI moderno de Android.
- **Firebase**: Utilizado para autenticación y almacenamiento de datos en tiempo real.
- **Open Library API**: Fuente para obtener datos de libros.
- **Coroutines y Flow**: Para manejar la programación asíncrona y los estados reactivos.

## Arquitectura 🏗️

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)** para mantener una separación clara de responsabilidades:

1. **Model:** Contiene las clases de datos y la lógica de negocio (como `BooksRepository`).
2. **ViewModel:** Maneja la lógica de la UI y expone estados reactivos (como `BooksViewModel`).
3. **View:** Implementada con Jetpack Compose para manejar la interfaz de usuario.

## Instalación y configuración 🔧

1. Clona este repositorio:
   ```bash
   git clone https://github.com/DenovanMonroy/BookLoginApp.git
   cd BookLoginApp
   ```

2. Configura Firebase:
   - Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
   - Descarga el archivo `google-services.json` y colócalo en el directorio `app/` de tu proyecto.
   - Habilita la autenticación por correo electrónico y la base de datos en tiempo real.

3. Ejecuta la aplicación:
   - Abre el proyecto en Android Studio.
   - Conéctalo a un emulador o dispositivo físico.
   - Haz clic en el botón **Run**.

## Estructura del proyecto 📂

```plaintext
📂 app/
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/com/example/bookloginapp/
│   │   │   ├── 📂 model/       # Modelos de datos (Book, SearchHistory, etc.)
│   │   │   ├── 📂 repository/  # Lógica de negocio y acceso a datos (BooksRepository)
│   │   │   ├── 📂 ui/          # Pantallas y componentes de la UI (HomeScreen, SearchScreen, etc.)
│   │   │   ├── 📂 viewmodel/   # Lógica de la UI y estados reactivos (BooksViewModel)
│   │   ├── 📂 res/             # Recursos como imágenes, estilos y layouts.
│   │   └── AndroidManifest.xml
└── build.gradle
```

## API utilizada 🌐

La aplicación utiliza la **[Open Library API](https://openlibrary.org/developers/api)** para buscar información sobre libros.

Ejemplo de endpoint:
```plaintext
https://openlibrary.org/search.json?q={query}&limit=20
```

## Funcionalidades futuras 🛠️

- Integración con un lector de libros.
- Soporte para múltiples idiomas.
- Exportar notas y favoritos en un archivo PDF.

## Contribuciones 🤝

¡Contribuciones son bienvenidas! Si deseas colaborar:
1. Haz un fork de este repositorio.
2. Crea una rama nueva para tus cambios:
   ```bash
   git checkout -b feature/nueva-funcionalidad
   ```
3. Realiza tus cambios y súbelos a tu repositorio:
   ```bash
   git add .
   git commit -m "Añadida nueva funcionalidad"
   git push origin feature/nueva-funcionalidad
   ```
4. Crea un Pull Request.

