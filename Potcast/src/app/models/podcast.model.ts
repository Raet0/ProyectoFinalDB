export interface Locutor {
  nickname: string;
  mail: string;
  pais: string;
  fotografiaUrl: string;
}

export interface Podcast {
  id: string;
  temaGeneral: string;
  temaDia: string;
  categoria: string;
  fecha: string;
  audioUrl: string;
  locutorPrincipal: Locutor;
  invitados: Locutor[];
  vistas?: number;
}
