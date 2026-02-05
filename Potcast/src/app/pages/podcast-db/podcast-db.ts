import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PodcastService } from '../../services/podcast';
import { Podcast } from '../../models/podcast.model';

@Component({
  selector: 'app-podcast-db',
  imports: [FormsModule, CommonModule],
  templateUrl: './podcast-db.html',
  styleUrl: './podcast-db.css',
  standalone: true,
})
export class PodcastDBComponent implements OnInit {
  podcasts: Podcast[] = [];
  filteredPodcasts: Podcast[] = [];
  currentPage = 1;
  pageSize = 10;
  editingId: string | null = null;
  editingPodcast: Podcast | null = null;

  searchName = '';
  searchCountry = '';
  searchHost = '';
  searchTheme = '';

  countries: string[] = [];
  hosts: string[] = [];
  themes: string[] = [];

  constructor(private podcastService: PodcastService) {}

  ngOnInit() {
    this.loadPodcasts();
  }

  loadPodcasts() {
    this.podcastService.getAllPodcasts().subscribe({
      next: (data) => {
        this.podcasts = data || [];
        this.extractFilters();
        this.applyFilters();
      },
      error: (err) => console.error('Error cargando podcasts', err),
    });
  }

  extractFilters() {
    this.countries = [...new Set(this.podcasts.map(p => p.locutorPrincipal?.pais).filter(Boolean))] as string[];
    this.hosts = [...new Set(this.podcasts.map(p => p.locutorPrincipal?.nickname).filter(Boolean))] as string[];
    this.themes = [...new Set(this.podcasts.map(p => p.temaDia).filter(Boolean))] as string[];
  }

  applyFilters() {
    this.filteredPodcasts = this.podcasts.filter(p => {
      const matchName = this.searchName === '' || (p.temaGeneral?.toLowerCase().includes(this.searchName.toLowerCase()));
      const matchCountry = this.searchCountry === '' || p.locutorPrincipal?.pais === this.searchCountry;
      const matchHost = this.searchHost === '' || p.locutorPrincipal?.nickname === this.searchHost;
      const matchTheme = this.searchTheme === '' || p.temaDia === this.searchTheme;
      
      return matchName && matchCountry && matchHost && matchTheme;
    });
    this.currentPage = 1;
  }

  clearFilters() {
    this.searchName = '';
    this.searchCountry = '';
    this.searchHost = '';
    this.searchTheme = '';
    this.applyFilters();
  }

  startEdit(podcast: Podcast) {
    this.editingId = podcast.id || null;
    this.editingPodcast = JSON.parse(JSON.stringify(podcast));
    if (this.editingPodcast && !this.editingPodcast.locutorPrincipal) {
      this.editingPodcast.locutorPrincipal = { nickname: '', pais: '', mail: '', fotografiaUrl: '' };
    }
  }

  cancelEdit() {
    this.editingId = null;
    this.editingPodcast = null;
  }

  saveEdit() {
    if (!this.editingPodcast || !this.editingId) return;
    this.podcastService.updatePodcast(this.editingId, this.editingPodcast).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadPodcasts();
      },
      error: (err) => console.error('Error actualizando podcast', err),
    });
  }

  deletePodcast(id: string | undefined) {
    if (!id) return;
    if (!confirm('¿Deseas eliminar este podcast?')) return;
    this.podcastService.deletePodcast(id).subscribe({
      next: () => this.loadPodcasts(),
      error: (err) => console.error('Error eliminando podcast', err),
    });
  }

  get totalPages() {
    return Math.max(1, Math.ceil(this.filteredPodcasts.length / this.pageSize));
  }

  get pagedPodcasts(): Podcast[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPodcasts.slice(start, start + this.pageSize);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
  }

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    try {
      return new Date(date).toLocaleDateString('es-ES');
    } catch {
      return date;
    }
  }
}
