import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

type Visitante = {
  id: number;
  nombre: string;
  numeroVisitas: number;
  ultimaVisita: Date;
  duracionPromedio: number; // en minutos
};

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule, CommonModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
  standalone: true,
})
export class Usuarios implements OnInit {
  visitantes: Visitante[] = [];
  searchName = '';
  filteredVisitantes: Visitante[] = [];
  currentPage = 1;
  pageSize = 10;
  totalVisitas = 0;
  visitantesUnicos = 0;

  constructor() {
    this.initializeVisitors();
  }

  ngOnInit() {
    // Simular que la aplicación genera visitantes automáticamente
    this.generateRandomVisitors();
    this.calculateStats();
    this.applyFilters();
  }

  initializeVisitors() {
    // Cargar visitantes del localStorage si existen
    const stored = localStorage.getItem('visitantes');
    if (stored) {
      this.visitantes = JSON.parse(stored).map((v: any) => ({
        ...v,
        ultimaVisita: new Date(v.ultimaVisita),
      }));
    }
  }

  generateRandomVisitors() {
    // Si no hay visitantes, crear algunos de ejemplo
    if (this.visitantes.length === 0) {
      for (let i = 1; i <= 15; i++) {
        const diasAtras = Math.floor(Math.random() * 30);
        const ultimaVisita = new Date();
        ultimaVisita.setDate(ultimaVisita.getDate() - diasAtras);

        this.visitantes.push({
          id: i,
          nombre: `Desconocido ${i}`,
          numeroVisitas: Math.floor(Math.random() * 50) + 1,
          ultimaVisita: ultimaVisita,
          duracionPromedio: Math.floor(Math.random() * 120) + 5,
        });
      }
    }

    // Registrar un nuevo visitante cada vez que la página se carga (simulado)
    const nuevoVisitante = this.visitantes.find(v => v.nombre === `Desconocido ${this.visitantes.length + 1}`);
    if (!nuevoVisitante && Math.random() > 0.5) {
      // 50% de probabilidad de agregar un nuevo visitante en cada carga
      const id = Math.max(...this.visitantes.map(v => v.id)) + 1;
      this.visitantes.push({
        id,
        nombre: `Desconocido ${id}`,
        numeroVisitas: 1,
        ultimaVisita: new Date(),
        duracionPromedio: Math.floor(Math.random() * 60) + 5,
      });
    }

    // Guardar en localStorage
    localStorage.setItem('visitantes', JSON.stringify(this.visitantes));
  }

  calculateStats() {
    this.visitantesUnicos = this.visitantes.length;
    this.totalVisitas = this.visitantes.reduce((sum, v) => sum + v.numeroVisitas, 0);
  }

  applyFilters() {
    this.filteredVisitantes = this.visitantes.filter(v =>
      v.nombre.toLowerCase().includes(this.searchName.toLowerCase())
    );

    // Ordenar por última visita (más recientes primero)
    this.filteredVisitantes.sort((a, b) => b.ultimaVisita.getTime() - a.ultimaVisita.getTime());
    this.currentPage = 1;
  }

  clearVisitors() {
    if (confirm('¿Estás seguro de que deseas limpiar el historial de visitantes?')) {
      this.visitantes = [];
      this.searchName = '';
      localStorage.removeItem('visitantes');
      this.calculateStats();
      this.applyFilters();
    }
  }

  get totalPages() {
    return Math.max(1, Math.ceil(this.filteredVisitantes.length / this.pageSize));
  }

  get pagedVisitantes(): Visitante[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredVisitantes.slice(start, start + this.pageSize);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
  }

  formatDate(date: Date): string {
    try {
      const now = new Date();
      const diff = now.getTime() - date.getTime();
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));

      if (days === 0) {
        return 'Hoy';
      } else if (days === 1) {
        return 'Ayer';
      } else if (days < 7) {
        return `hace ${days} días`;
      } else {
        return date.toLocaleDateString('es-ES');
      }
    } catch {
      return 'N/A';
    }
  }

  getVisitorStatus(ultimaVisita: Date): string {
    const now = new Date();
    const diff = now.getTime() - ultimaVisita.getTime();
    const horasAtras = diff / (1000 * 60 * 60);

    if (horasAtras < 1) {
      return 'En línea';
    } else if (horasAtras < 24) {
      return 'Activo hoy';
    } else {
      return 'Inactivo';
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'En línea':
        return 'bg-green-600/20 text-green-400';
      case 'Activo hoy':
        return 'bg-yellow-600/20 text-yellow-400';
      default:
        return 'bg-gray-600/20 text-gray-400';
    }
  }
}
