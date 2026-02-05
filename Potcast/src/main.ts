import { bootstrapApplication } from '@angular/platform-browser';
import 'zone.js'; // <--- ¡AGREGA ESTA LÍNEA AQUÍ AL PRINCIPIO! 🚨
import { appConfig } from './app/app.config';
// 👇 Fíjate que aquí importamos desde './app/app' (sin .component)
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));