package com.example.potcast_back;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.potcast_back.dtos.PodcastDTO;
import com.example.potcast_back.model.Locutor;
import com.example.potcast_back.service.PodcastService;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PodcastService podcastService;

    private static final String[] CATEGORIAS = {
        "Tecnología", "Negocios", "Salud", "Educación", "Entretenimiento",
        "Ciencia", "Música", "Marketing", "Deportes", "Viajes"
    };

    private static final String[] HOSTS_NOMBRES = {
        "Juan Pérez", "Ana Martínez", "Carlos López", "María García", "Pedro Rodríguez",
        "Dr. Roberto Sánchez", "Laura González", "David Fernández", "Dr. Lucas Medina",
        "Sofía Torres", "Andrés Silva", "Elena Costa", "Alejandro Ruiz", "Valentina Paredes",
        "Daniela Vargas", "Javier Montoya", "Paulina Díaz", "Dr. Fernando Herrera",
        "Isabel Romero", "Marco Silva", "Camila Santos", "Ricardo Peña", "Natalia Ruiz",
        "Felipe Gómez", "Cristina López", "Antonio Vega", "Marta Fernández", "Óscar Martín",
        "Gabriela Soto", "Mateo Ruiz", "Sandra Moreno", "Lucas Hernández", "Patricia Domínguez",
        "Sergio Jiménez", "Andrea Castillo", "Miguel Ángel Torres", "Rosa María Díaz",
        "Julio César Romero", "Valentina Silva", "Raúl Medina", "Esperanza González"
    };

    private static final String[] HOSTS_PAISES = {
        "Colombia", "España", "México", "Ecuador", "USA",
        "Brasil", "Chile", "Perú", "Argentina", "Venezuela"
    };

    private static final String[][] TEMAS = {
        // Tecnología
        {"Tendencias Tecnológicas", "El futuro de la Inteligencia Artificial"},
        {"Tecnología", "Cloud Computing y el futuro de la infraestructura"},
        {"Tecnología", "Blockchain: más allá de las criptomonedas"},
        {"Tecnología", "5G y la transformación digital"},
        {"Tecnología", "Realidad virtual y aumentada en 2026"},
        {"Tecnología", "Ciberseguridad: protegiendo nuestros datos"},
        {"Tecnología", "IoT y los dispositivos conectados"},
        {"Tecnología", "Machine Learning en la vida cotidiana"},
        {"Tecnología", "Desarrollo web moderno con JavaScript"},
        {"Tecnología", "APIs y microservicios en arquitectura moderna"},

        // Negocios
        {"Emprendimiento Digital", "Cómo iniciar un startup exitoso"},
        {"Negocios", "Finanzas personales para emprendedores"},
        {"Negocios", "Estrategias de marketing efectivas"},
        {"Negocios", "Gestión de recursos humanos en pequeñas empresas"},
        {"Negocios", "Cómo escalar tu negocio"},
        {"Negocios", "Inversión en startups: guía para inversionistas"},
        {"Negocios", "E-commerce: claves para vender online"},
        {"Negocios", "Networking empresarial en la era digital"},
        {"Negocios", "Negociación comercial efectiva"},
        {"Negocios", "Marca personal para profesionales"},

        // Salud
        {"Bienestar y Salud", "Hábitos saludables para el nuevo año"},
        {"Salud", "Nutrición y dieta equilibrada"},
        {"Salud", "Ejercicio físico: rutinas prácticas"},
        {"Salud", "Salud mental y meditación"},
        {"Salud", "Sueño: la importancia del descanso"},
        {"Salud", "Prevención de enfermedades crónicas"},
        {"Salud", "Medicina alternativa y tradicional"},
        {"Salud", "Estrés y ansiedad en el trabajo"},
        {"Salud", "Nutrición deportiva para atletas"},
        {"Salud", "Biohacking para mejorar tu vida"},

        // Educación
        {"Educación Online", "Transformación digital en la educación"},
        {"Educación", "Métodos de aprendizaje efectivos"},
        {"Educación", "Educación superior en tiempos de cambio"},
        {"Educación", "Formación profesional para el futuro"},
        {"Educación", "Inteligencia emocional en la educación"},
        {"Educación", "Tecnología educativa y plataformas e-learning"},
        {"Educación", "Carreras STEM: oportunidades laborales"},
        {"Educación", "Liderazgo y habilidades directivas"},
        {"Educación", "Idiomas: técnicas de aprendizaje"},
        {"Educación", "Pensamiento crítico y resolución de problemas"},

        // Entretenimiento
        {"Entretenimiento", "Series y películas del momento"},
        {"Entretenimiento", "Industria del cine en 2026"},
        {"Entretenimiento", "Videojuegos: el futuro del entretenimiento"},
        {"Entretenimiento", "Podcasts: cómo crear contenido viral"},
        {"Entretenimiento", "Realidad show: fenómeno global"},
        {"Entretenimiento", "Literatura y bestsellers internacionales"},
        {"Entretenimiento", "Influencers y creadores de contenido"},
        {"Entretenimiento", "Anime y manga en occidente"},
        {"Entretenimiento", "Stand-up comedy y humoristas modernos"},
        {"Entretenimiento", "Redes sociales y tendencias virales"},

        // Ciencia
        {"Ciencia y Tecnología", "Descubrimientos científicos de la semana"},
        {"Ciencia", "Astrofísica y exploración del espacio"},
        {"Ciencia", "Biología molecular y genética"},
        {"Ciencia", "Física cuántica para principiantes"},
        {"Ciencia", "Cambio climático y sostenibilidad"},
        {"Ciencia", "Neurociencia y el cerebro humano"},
        {"Ciencia", "Energías renovables del futuro"},
        {"Ciencia", "Paleontología y fósiles antiguos"},
        {"Ciencia", "Microbiología y enfermedades infecciosas"},
        {"Ciencia", "Investigación científica de vanguardia"},

        // Música
        {"Industria Musical", "El streaming y el futuro de la música"},
        {"Música", "Producción musical con tecnología"},
        {"Música", "Géneros musicales emergentes"},
        {"Música", "Historia del rock and roll"},
        {"Música", "Hip-hop: de los guetos al éxito mundial"},
        {"Música", "Artistas latinos que conquistan el mundo"},
        {"Música", "Festivales de música imprescindibles"},
        {"Música", "Instrumentos musicales y técnicas"},
        {"Música", "DJ y cultura electrónica"},
        {"Música", "Academia de música para principiantes"},

        // Marketing
        {"Marketing Digital", "Estrategias de redes sociales en 2026"},
        {"Marketing", "SEO y posicionamiento en Google"},
        {"Marketing", "Email marketing efectivo"},
        {"Marketing", "Publicidad digital y Google Ads"},
        {"Marketing", "Analítica web y métricas importantes"},
        {"Marketing", "Content marketing que convierte"},
        {"Marketing", "Inbound marketing y atracción de clientes"},
        {"Marketing", "Branding y construcción de marca"},
        {"Marketing", "Growth hacking para startups"},
        {"Marketing", "CRM y gestión de clientes"},

        // Deportes
        {"Deportes y Fitness", "Entrenamiento para atletas amateur"},
        {"Deportes", "Fútbol: análisis de equipos y jugadores"},
        {"Deportes", "Baloncesto NBA: noticias y análisis"},
        {"Deportes", "Tenis: torneos y campeones mundiales"},
        {"Deportes", "Boxeo y artes marciales mixtas"},
        {"Deportes", "Atletismo y carreras de distancia"},
        {"Deportes", "Natación: técnicas y entrenamientos"},
        {"Deportes", "Ciclismo: competiciones y records"},
        {"Deportes", "Golf y deportes de precisión"},
        {"Deportes", "Yoga y pilates para la flexibilidad"}
    };

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🌱 Iniciando carga de 100 podcasts de prueba...");
        cargarPodcastsPrueba();
        System.out.println("✅ 100 Podcasts cargados exitosamente!");
    }

    private void cargarPodcastsPrueba() {
        Random random = new Random();
        LocalDate fecha = LocalDate.of(2026, 2, 4);
        List<PodcastDTO> podcasts = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            String id = "podcast-" + i;
            String[] tema = TEMAS[random.nextInt(TEMAS.length)];
            String temaGeneral = tema[0];
            String temaDia = tema[1];
            String categoria = CATEGORIAS[random.nextInt(CATEGORIAS.length)];
            String fechaStr = fecha.minusDays(random.nextInt(60)).format(DateTimeFormatter.ISO_DATE);
            String audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + (random.nextInt(11) + 1) + ".mp3";

            String hostNombre = HOSTS_NOMBRES[random.nextInt(HOSTS_NOMBRES.length)];
            String hostEmail = hostNombre.toLowerCase().replaceAll(" ", ".") + "@podcast.com";
            String hostPais = HOSTS_PAISES[random.nextInt(HOSTS_PAISES.length)];
            String hostFoto = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + hostNombre.replaceAll(" ", "+") + "&scale=80";

            // Locutor hostPrincipal = new Locutor(hostNombre, hostEmail, hostPais, hostFoto);
            Locutor hostPrincipal = new Locutor(null, hostNombre, hostEmail, hostPais, hostFoto);

            // Invitados aleatorios (0-3)
            List<Locutor> invitados = new ArrayList<>();
            int numInvitados = random.nextInt(4);
            for (int j = 0; j < numInvitados; j++) {
                String invNombre = HOSTS_NOMBRES[random.nextInt(HOSTS_NOMBRES.length)];
                String invEmail = invNombre.toLowerCase().replaceAll(" ", ".") + "@podcast.com";
                String invPais = HOSTS_PAISES[random.nextInt(HOSTS_PAISES.length)];
                String invFoto = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + invNombre.replaceAll(" ", "+") + "&scale=80";
                // invitados.add(new Locutor(invNombre, invEmail, invPais, invFoto));
                invitados.add(new Locutor(null, invNombre, invEmail, invPais, invFoto));
            }

            PodcastDTO podcast = new PodcastDTO(
                id,
                temaGeneral,
                temaDia,
                categoria,
                fechaStr,
                audioUrl,
                hostPrincipal,
                invitados
            );

            podcasts.add(podcast);
        }

        // Guardar todos los podcasts
        for (PodcastDTO podcast : podcasts) {
            try {
                podcastService.crearPodcast(podcast);
                System.out.println("✓ Podcast " + podcast.getId() + " creado: " + podcast.getTemaDia());
            } catch (Exception e) {
                System.err.println("✗ Error al crear podcast: " + podcast.getId());
            }
        }

        // Registrar reproducciones aleatorias
        try {
            for (int i = 1; i <= 100; i++) {
                int reproducciones = random.nextInt(100) + 5;
                for (int j = 0; j < reproducciones; j++) {
                    podcastService.registrarReproduccion("podcast-" + i);
                }
            }
            System.out.println("✓ Reproducciones aleatorias registradas para todos los podcasts");
        } catch (Exception e) {
            System.err.println("✗ Error al registrar reproducciones");
            e.printStackTrace();
        }
    }
}