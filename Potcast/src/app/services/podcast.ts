import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Podcast } from '../models/podcast.model';

@Injectable({
  providedIn: 'root'
})
export class PodcastService {
  private apiUrl = 'http://localhost:8080/api/podcasts';

  constructor(private http: HttpClient) { }

  // 1. Obtener UN podcast
  getPodcast(id: string): Observable<Podcast> {
    return this.http.get<Podcast>(`${this.apiUrl}/${id}`);
  }

  // 2. Obtener TODOS (Para la lista lateral)
  getAllPodcasts(): Observable<Podcast[]> {
    return this.http.get<Podcast[]>(this.apiUrl);
  }

  // 3. Obtener Top Podcasts (Más reproducidos)
  getTopPodcasts(): Observable<Podcast[]> {
    return this.http.get<Podcast[]>(`${this.apiUrl}/reportes/top-views`);
  }

  // 4. Eliminar podcast
  deletePodcast(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // 5. Crear podcast
  createPodcast(podcast: Podcast): Observable<Podcast> {
    return this.http.post<Podcast>(this.apiUrl, podcast);
  }

  // 5.1 Actualizar podcast
  updatePodcast(id: string, podcast: Podcast): Observable<Podcast> {
    return this.http.put<Podcast>(`${this.apiUrl}/${id}`, podcast);
  }

  // 6. Registrar reproducción
  recordPlayback(id: string): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/${id}/play`, {});
  }

  // 7. Obtener invitados frecuentes
  getTopGuests(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/reportes/top-invitados`);
  }

  // 8. Obtener locutores por país
  getLocutoresByCountry(country: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/reportes/pais/${country}`);
  }

  // 9. Ordenar por FECHA ✅ NUEVO
  getPodcastsByDate(orden: string = 'desc'): Observable<Podcast[]> {
    return this.http.get<Podcast[]>(`${this.apiUrl}/reportes/ordenado-por-fecha?orden=${orden}`);
  }

  // 10. Ordenar por REPRODUCCIONES ✅ NUEVO
  getPodcastsByViews(orden: string = 'desc'): Observable<Podcast[]> {
    return this.http.get<Podcast[]>(`${this.apiUrl}/reportes/ordenado-por-reproducciones?orden=${orden}`);
  }
}
