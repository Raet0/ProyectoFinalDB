import { Component, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { Podcast } from '../../models/podcast.model';
import { PodcastService } from '../../services/podcast';
import { HttpClient, HttpClientModule } from '@angular/common/http';

type Locutor = {
  id?: string;
  nickname: string;
  mail: string;
  pais: string;
  fotografiaUrl?: string | null;
};

@Component({
  selector: 'app-create-podcast',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, HttpClientModule],
  templateUrl: './create-podcast.html'
})
export class CreatePodcastComponent implements OnInit {
  @ViewChild('podcastForm') podcastForm!: NgForm;

  // Mismas categorías del DataSeeder
  categorias = [
    'Tecnología', 'Negocios', 'Salud', 'Educación', 'Entretenimiento',
    'Ciencia', 'Música', 'Marketing', 'Deportes', 'Viajes'
  ];

  // Mismos países del DataSeeder
  paises = [
    { codigo: 'Colombia', bandera: '🇨🇴' },
    { codigo: 'España', bandera: '🇪🇸' },
    { codigo: 'México', bandera: '🇲🇽' },
    { codigo: 'Ecuador', bandera: '🇪🇨' },
    { codigo: 'USA', bandera: '🇺🇸' },
    { codigo: 'Brasil', bandera: '🇧🇷' },
    { codigo: 'Chile', bandera: '🇨🇱' },
    { codigo: 'Perú', bandera: '🇵🇪' },
    { codigo: 'Argentina', bandera: '🇦🇷' },
    { codigo: 'Venezuela', bandera: '🇻🇪' }
  ];

  apiBase = 'http://localhost:8080';
  locutores: Locutor[] = [];
  selectedLocutorId = '';
  locutorMode: 'existing' | 'manual' = 'existing';
  manualLocutorName = '';

  newPodcast: Podcast = {
    id: crypto.randomUUID(),
    temaGeneral: '',
    temaDia: '',
    categoria: 'Tecnología',
    fecha: new Date().toISOString().split('T')[0],
    audioUrl: '',
    locutorPrincipal: {
      nickname: '',
      pais: '',
      fotografiaUrl: '',
      mail: ''
    },
    invitados: []
  };

  isSubmitting = false;
  submitSuccess = false;
  submitError = '';

  constructor(
    private podcastService: PodcastService,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadLocutores();
  }

  loadLocutores() {
    this.http.get<Locutor[]>(`${this.apiBase}/locutores`).subscribe({
      next: (data) => {
        this.locutores = data ?? [];
        if (this.locutores.length > 0 && !this.selectedLocutorId) {
          this.selectedLocutorId = this.locutores[0].id ?? '';
          this.setLocutorFromSelection();
        }
      },
      error: (err) => console.error('Error cargando locutores', err),
    });
  }

  setLocutorFromSelection() {
    const selected = this.locutores.find(l => l.id === this.selectedLocutorId);
    if (!selected) {
      this.newPodcast.locutorPrincipal = { nickname: '', pais: '', fotografiaUrl: '', mail: '' };
      return;
    }

    this.newPodcast.locutorPrincipal = {
      nickname: selected.nickname,
      pais: selected.pais,
      fotografiaUrl: selected.fotografiaUrl ?? '',
      mail: selected.mail
    };
  }

  setManualLocutor() {
    const name = this.manualLocutorName.trim();
    this.newPodcast.locutorPrincipal = {
      nickname: name,
      pais: 'Desconocido',
      fotografiaUrl: '',
      mail: 'desconocido@podcast.local'
    };
  }

  resetForm() {
    this.newPodcast = {
      id: crypto.randomUUID(),
      temaGeneral: '',
      temaDia: '',
      categoria: 'Tecnología',
      fecha: new Date().toISOString().split('T')[0],
      audioUrl: '',
      locutorPrincipal: {
        nickname: '',
        pais: 'Desconocido',
        fotografiaUrl: '',
        mail: 'desconocido@podcast.local'
      },
      invitados: []
    };
    this.selectedLocutorId = '';
    this.manualLocutorName = '';
    this.locutorMode = 'existing';
    if (this.podcastForm) {
      this.podcastForm.resetForm();
    }
    this.submitError = '';
  }

  goHome() {
    this.router.navigate(['/']);
  }

  onSubmit() {
    this.submitError = '';
    this.submitSuccess = false;

    if (this.locutorMode === 'manual') {
      this.setManualLocutor();
    } else {
      this.setLocutorFromSelection();
    }

    if (!this.newPodcast.temaDia || !this.newPodcast.locutorPrincipal.nickname || !this.newPodcast.audioUrl) {
      this.submitError = 'Completa los campos obligatorios antes de continuar.';
      return;
    }

    this.isSubmitting = true;

    this.podcastService.createPodcast(this.newPodcast).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.submitSuccess = true;
        this.resetForm();
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 2000);
      },
      error: (err) => {
        console.error(err);
        this.isSubmitting = false;
        this.submitError = 'No se pudo crear el podcast. Intenta de nuevo.';
      }
    });
  }
}
