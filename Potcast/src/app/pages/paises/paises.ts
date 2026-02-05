import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PodcastService } from '../../services/podcast';
import { Podcast } from '../../models/podcast.model';

type Pais = {
  nombre: string;
  podcasts: number;
  locutores: number;
};

@Component({
  selector: 'app-paises',
  imports: [FormsModule, CommonModule],
  templateUrl: './paises.html',
  styleUrl: './paises.css',
  standalone: true,
})
export class Paises implements OnInit {
  paises: Pais[] = [];
  filteredPaises: Pais[] = [];
  currentPage = 1;
  pageSize = 10;
  searchName = '';
  newCountryName = '';
  podcasts: Podcast[] = [];

  constructor(private podcastService: PodcastService) {}

  ngOnInit() {
    this.loadPaises();
  }

  loadPaises() {
    this.podcastService.getAllPodcasts().subscribe({
      next: (data) => {
        this.podcasts = data || [];
        this.extractPaises();
        this.applyFilters();
      },
      error: (err) => console.error('Error cargando podcasts', err),
    });
  }

  extractPaises() {
    const paisesMap = new Map<string, { podcasts: number; locutores: Set<string> }>();

    this.podcasts.forEach(p => {
      if (p.locutorPrincipal?.pais) {
        const pais = p.locutorPrincipal.pais;
        if (!paisesMap.has(pais)) {
          paisesMap.set(pais, { podcasts: 0, locutores: new Set() });
        }
        const entry = paisesMap.get(pais)!;
        entry.podcasts++;
        if (p.locutorPrincipal.nickname) {
          entry.locutores.add(p.locutorPrincipal.nickname);
        }
      }

      // Agregar invitados
      if (p.invitados) {
        p.invitados.forEach(invitado => {
          if (invitado.pais) {
            if (!paisesMap.has(invitado.pais)) {
              paisesMap.set(invitado.pais, { podcasts: 0, locutores: new Set() });
            }
            const entry = paisesMap.get(invitado.pais)!;
            if (invitado.nickname) {
              entry.locutores.add(invitado.nickname);
            }
          }
        });
      }
    });

    this.paises = Array.from(paisesMap.entries()).map(([nombre, data]) => ({
      nombre,
      podcasts: data.podcasts,
      locutores: data.locutores.size,
    }));

    this.paises.sort((a, b) => b.podcasts - a.podcasts);
  }

  applyFilters() {
    this.filteredPaises = this.paises.filter(p =>
      p.nombre.toLowerCase().includes(this.searchName.toLowerCase())
    );
    this.currentPage = 1;
  }

  addCountry() {
    if (!this.newCountryName.trim()) {
      alert('Por favor ingresa un nombre de país');
      return;
    }

    const exists = this.paises.some(p => p.nombre.toLowerCase() === this.newCountryName.toLowerCase());
    if (exists) {
      alert('Este país ya existe');
      return;
    }

    this.paises.push({
      nombre: this.newCountryName,
      podcasts: 0,
      locutores: 0,
    });

    this.paises.sort((a, b) => b.podcasts - a.podcasts);
    this.newCountryName = '';
    this.applyFilters();
  }

  deleteCountry(nombre: string) {
    if (confirm(`¿Deseas eliminar "${nombre}"?`)) {
      this.paises = this.paises.filter(p => p.nombre !== nombre);
      this.applyFilters();
    }
  }

  clearSearch() {
    this.searchName = '';
    this.applyFilters();
  }

  get totalPages() {
    return Math.max(1, Math.ceil(this.filteredPaises.length / this.pageSize));
  }

  get pagedPaises(): Pais[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPaises.slice(start, start + this.pageSize);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
  }
}
