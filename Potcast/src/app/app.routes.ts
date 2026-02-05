import { Routes } from '@angular/router';
import { PodcastDetail } from './pages/podcast-detail/podcast-detail';
import { CreatePodcastComponent } from './pages/create-podcast/create-podcast';
import { Dashboard } from './pages/dashboard/dashboard';
import { Locutores } from './pages/locutores/locutores';
import { PodcastDBComponent } from './pages/podcast-db/podcast-db';
import { Paises } from './pages/paises/paises';
import { Usuarios } from './pages/usuarios/usuarios';
// 👇 Importamos las clases con su nombre real (DashboardComponent)
// 👇 Y desde el archivo correcto (dashboard.ts, sin .component)

export const routes: Routes = [
    { path: '', component: Dashboard },
    { path: 'podcast/:id', component: PodcastDetail },
    // ruta para entrar en el botón de crar nuevo podcast
    { path: 'create', component: CreatePodcastComponent },
    { path: 'locutores', component: Locutores},
    { path: 'podcast-db', component: PodcastDBComponent },
    { path: 'paises', component: Paises },
    { path: 'usuarios', component: Usuarios },
    { path: '**', redirectTo: '' }
];