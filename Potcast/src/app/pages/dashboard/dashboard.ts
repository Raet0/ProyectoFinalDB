import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Podcast } from '../../models/podcast.model';
import { PodcastService } from '../../services/podcast';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, OnDestroy {
  mostPlayed: Podcast | null = null;
  allPodcasts: Podcast[] = [];
  filteredPodcasts: Podcast[] = [];
  currentPageLibrary = 1;
  pageSizeLibrary = 10;

  topGuests: string[] = [];
  currentPageTopGuests = 1;
  pageSizeTopGuests = 10;
  uniqueCountries: string[] = [];
  locutoresByCountry: Map<string, string[]> = new Map();

  filterHost: string = '';
  filterCategory: string = '';
  filterCountry: string = '';
  sortOrder: string = 'newest';

  uniqueCategories: string[] = [];

  totalPodcasts = 0;
  totalPlays = 0;
  avgPlays = 0;
  latestPodcasts: Podcast[] = [];

  // ✅ NUEVAS PROPIEDADES PARA LOS REPORTES
  podcastsByDate: Podcast[] = [];
  podcastsByViews: Podcast[] = [];
  currentPageByDate = 1;
  pageSizeByDate = 10;
  currentPageByViews = 1;
  pageSizeByViews = 10;
  dateOrder: string = 'desc';
  viewsOrder: string = 'desc';

  currentPageCountries = 1;
  pageSizeCountries = 10;

  private routerSubscription?: Subscription;

  constructor(
    private podcastService: PodcastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadData();

    // Recargar cuando navegas al dashboard (/)
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      setTimeout(() => {
        if (event.url === '/' || event.url === '') {
          this.loadData();
        }
      }, 100);
    });
  }

  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  loadData() {
    this.podcastService.getTopPodcasts().subscribe({
      next: (data) => {
        if (data && data.length > 0) this.mostPlayed = data[0];
      },
      error: (e) => console.error('Error cargando top podcasts', e),
    });

    this.podcastService.getAllPodcasts().subscribe({
      next: (data) => {
        this.allPodcasts = data;

        // Estadísticas reales
        this.totalPodcasts = data.length;
        this.totalPlays = data.reduce((sum, p) => sum + (p.vistas || 0), 0);
        this.avgPlays = this.totalPodcasts ? Math.round(this.totalPlays / this.totalPodcasts) : 0;

        // Últimos añadidos reales
        this.latestPodcasts = [...data]
          .sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime())
          .slice(0, 5);

        this.uniqueCategories = [...new Set(data.map(p => p.categoria).filter(Boolean))];
        this.uniqueCountries = [...new Set(data.map(p => p.locutorPrincipal?.pais).filter(Boolean))];
        this.currentPageCountries = 1;

        this.uniqueCountries.forEach(country => {
          if (country) {
            this.podcastService.getLocutoresByCountry(country).subscribe({
              next: (locutores) => {
                this.locutoresByCountry.set(country, locutores);
              },
              error: (e) => console.error(`Error cargando locutores de ${country}`, e)
            });
          }
        });

        this.applyFilters();
        this.currentPageLibrary = 1;
      },
      error: (e) => console.error('Error cargando lista', e),
    });

    this.podcastService.getTopGuests().subscribe({
      next: (data) => {
        this.topGuests = data || [];
        this.currentPageTopGuests = 1;
      },
      error: (e) => console.error('Error cargando invitados', e),
    });

    // ✅ CARGAR REPORTES NUEVOS
    this.loadDateReport();
    this.loadViewsReport();
  }

  // ✅ MÉTODO PARA CARGAR REPORTE POR FECHA
  loadDateReport() {
    this.podcastService.getPodcastsByDate(this.dateOrder).subscribe({
      next: (data) => {
        this.podcastsByDate = data || [];
        this.currentPageByDate = 1;
      },
      error: (e) => console.error('Error cargando reportes por fecha', e),
    });
  }

  // ✅ MÉTODO PARA CARGAR REPORTE POR REPRODUCCIONES
  loadViewsReport() {
    this.podcastService.getPodcastsByViews(this.viewsOrder).subscribe({
      next: (data) => {
        this.podcastsByViews = data || [];
        this.currentPageByViews = 1;
      },
      error: (e) => console.error('Error cargando reportes por vistas', e),
    });
  }

  // ✅ CAMBIAR ORDEN DE FECHA
  changeDateOrder(newOrder: string) {
    this.dateOrder = newOrder;
    this.loadDateReport();
  }

  // ✅ CAMBIAR ORDEN DE VISTAS
  changeViewsOrder(newOrder: string) {
    this.viewsOrder = newOrder;
    this.loadViewsReport();
  }

  applyFilters() {
    let temp = [...this.allPodcasts];

    if (this.filterHost) {
      const term = this.filterHost.toLowerCase();
      temp = temp.filter(p => p.locutorPrincipal?.nickname.toLowerCase().includes(term));
    }

    if (this.filterCategory) {
      temp = temp.filter(p => p.categoria === this.filterCategory);
    }

    if (this.filterCountry) {
      temp = temp.filter(p => p.locutorPrincipal?.pais === this.filterCountry);
    }

    if (this.sortOrder === 'newest') {
      temp.sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime());
    } else if (this.sortOrder === 'oldest') {
      temp.sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime());
    } else if (this.sortOrder === 'most_played') {
      temp.sort((a, b) => (b.vistas || 0) - (a.vistas || 0));
    } else if (this.sortOrder === 'least_played') {
      temp.sort((a, b) => (a.vistas || 0) - (b.vistas || 0));
    }

    this.filteredPodcasts = temp;
    this.currentPageLibrary = 1;
  }

  deletePodcast(id: string, event: Event) {
    event.stopPropagation();
    event.preventDefault();
    if (confirm('¿Estás seguro de eliminar este podcast?')) {
      this.podcastService.deletePodcast(id).subscribe(() => {
        this.loadData();
      });
    }
  }

  getLocutoresForCountry(country: string): string[] {
    return this.locutoresByCountry.get(country) || [];
  }

  get totalPagesLibrary() {
    return Math.max(1, Math.ceil(this.filteredPodcasts.length / this.pageSizeLibrary));
  }

  get pagedLibrary(): Podcast[] {
    const start = (this.currentPageLibrary - 1) * this.pageSizeLibrary;
    return this.filteredPodcasts.slice(start, start + this.pageSizeLibrary);
  }

  goToLibraryPage(page: number) {
    if (page < 1 || page > this.totalPagesLibrary) return;
    this.currentPageLibrary = page;
  }

  get totalPagesTopGuests() {
    return Math.max(1, Math.ceil(this.topGuests.length / this.pageSizeTopGuests));
  }

  get pagedTopGuests(): string[] {
    const start = (this.currentPageTopGuests - 1) * this.pageSizeTopGuests;
    return this.topGuests.slice(start, start + this.pageSizeTopGuests);
  }

  goToTopGuestsPage(page: number) {
    if (page < 1 || page > this.totalPagesTopGuests) return;
    this.currentPageTopGuests = page;
  }

  get totalPagesByDate() {
    return Math.max(1, Math.ceil(this.podcastsByDate.length / this.pageSizeByDate));
  }

  get pagedByDate(): Podcast[] {
    const start = (this.currentPageByDate - 1) * this.pageSizeByDate;
    return this.podcastsByDate.slice(start, start + this.pageSizeByDate);
  }

  goToByDatePage(page: number) {
    if (page < 1 || page > this.totalPagesByDate) return;
    this.currentPageByDate = page;
  }

  get totalPagesByViews() {
    return Math.max(1, Math.ceil(this.podcastsByViews.length / this.pageSizeByViews));
  }

  get pagedByViews(): Podcast[] {
    const start = (this.currentPageByViews - 1) * this.pageSizeByViews;
    return this.podcastsByViews.slice(start, start + this.pageSizeByViews);
  }

  goToByViewsPage(page: number) {
    if (page < 1 || page > this.totalPagesByViews) return;
    this.currentPageByViews = page;
  }

  get totalPagesCountries() {
    return Math.max(1, Math.ceil(this.uniqueCountries.length / this.pageSizeCountries));
  }

  get pagedCountries(): string[] {
    const start = (this.currentPageCountries - 1) * this.pageSizeCountries;
    return this.uniqueCountries.slice(start, start + this.pageSizeCountries);
  }

  goToCountriesPage(page: number) {
    if (page < 1 || page > this.totalPagesCountries) return;
    this.currentPageCountries = page;
  }
}
