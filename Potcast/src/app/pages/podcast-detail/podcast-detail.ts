import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Podcast } from '../../models/podcast.model';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PodcastService } from '../../services/podcast';

@Component({
  selector: 'app-podcast-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './podcast-detail.html',
  styleUrl: './podcast-detail.css'
})
export class PodcastDetail implements OnInit, OnDestroy {

  podcast: Podcast | null = null;
  loading = true;

  audio = new Audio();
  isPlaying = false;
  isSubscribed = false;
  playbackRecorded = false;

  constructor(
    private podcastService: PodcastService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const podcastId = params.get('id') || '';
      this.loading = true;
      this.playbackRecorded = false;

      this.stopAudio();

      this.podcastService.getPodcast(podcastId).subscribe({
        next: (data) => {
          this.podcast = data;
          this.loading = false;
          if (this.podcast.audioUrl) {
            this.audio.src = this.podcast.audioUrl;
            this.audio.load();
          }
        },
        error: (err) => {
          console.error('Error cargando podcast', err);
          this.loading = false;
        }
      });
    });

    this.audio.onended = () => {
      this.isPlaying = false;
    };
  }

  togglePlay(): void {
    if (!this.podcast) return;

    if (this.isPlaying) {
      this.audio.pause();
    } else {
      // Registrar reproducción solo la primera vez que se presiona play
      if (!this.playbackRecorded) {
        this.podcastService.recordPlayback(this.podcast.id).subscribe({
          next: (response) => {
            console.log('Reproducción registrada:', response);
          },
          error: (err) => {
            console.error('Error registrando reproducción:', err);
          }
        });
        this.playbackRecorded = true;
      }

      this.audio.play().catch(error => console.error("Error al reproducir:", error));
    }
    this.isPlaying = !this.isPlaying;
  }

  toggleSubscribe(): void {
    this.isSubscribed = !this.isSubscribed;
    if (this.isSubscribed) {
        alert("¡Te has suscrito a este podcast!");
    }
  }

  stopAudio() {
    this.audio.pause();
    this.audio.currentTime = 0;
    this.isPlaying = false;
  }

  ngOnDestroy(): void {
    this.stopAudio();
  }

  getFlagEmoji(country?: string): string {
    if (!country) return '';
    const code = country.trim().toUpperCase();
    if (code.length !== 2) return '';
    const A = 0x1f1e6;
    const offset = 0x41;
    return String.fromCodePoint(
      A + code.charCodeAt(0) - offset,
      A + code.charCodeAt(1) - offset
    );
  }
}
