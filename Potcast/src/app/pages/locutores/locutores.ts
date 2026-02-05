import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

type LocutorForm = {
  nickname: string;
};

type Locutor = {
  id?: string;
  nickname: string;
  mail: string;
  pais: string;
  fotografiaUrl?: string | null;
};

@Component({
  selector: 'app-locutores',
  imports: [FormsModule, HttpClientModule, CommonModule],
  templateUrl: './locutores.html',
  styleUrl: './locutores.css',
  standalone: true,
})
export class Locutores implements OnInit {
  apiBase = 'http://localhost:8080';

  form: LocutorForm = {
    nickname: '',
  };
  selectedFile: File | null = null;
  searchNickname = '';
  locutores: Locutor[] = [];
  currentPage = 1;
  pageSize = 10;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadAll();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  createLocutor() {
    if (!this.form.nickname) {
      alert('Por favor ingresa un nombre');
      return;
    }

    const fd = new FormData();
    fd.append('nickname', this.form.nickname);
    if (this.selectedFile) fd.append('foto', this.selectedFile);

    this.http.post<Locutor>(`${this.apiBase}/locutores`, fd).subscribe({
      next: (created) => {
        console.log('✓ Locutor creado:', created);
        this.form = { nickname: '' };
        this.selectedFile = null;
        this.loadAll();
      },
      error: (err) => console.error('✗ Error al crear locutor', err),
    });
  }

  loadAll() {
    this.http.get<Locutor[]>(`${this.apiBase}/locutores`).subscribe({
      next: (data) => {
        this.locutores = data ?? [];
        this.currentPage = 1;
      },
      error: (err) => console.error('✗ Error al listar locutores', err),
    });
  }

  searchLocutor() {
    const q = this.searchNickname.trim();
    if (!q) {
      this.loadAll();
      return;
    }

    this.http.get<Locutor[]>(`${this.apiBase}/locutores?nickname=${encodeURIComponent(q)}`).subscribe({
      next: (data) => {
        this.locutores = data ?? [];
        this.currentPage = 1;
      },
      error: (err) => console.error('✗ Error al buscar locutor', err),
    });
  }

  get totalPages() {
    return Math.max(1, Math.ceil(this.locutores.length / this.pageSize));
  }

  get pagedLocutores(): Locutor[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.locutores.slice(start, start + this.pageSize);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
  }
}