import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router'; // <--- ¿Tienes esto?

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet], // <--- ¿Y lo agregaste aquí?
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  title = 'potcast-front';
}