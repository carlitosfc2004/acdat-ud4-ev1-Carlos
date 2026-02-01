package org.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppBiblioteca {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> colLibros;
    private static MongoCollection<Document> colPrestamos;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        // Desactivar logs excesivos de MongoDB
        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);

        conectarDB();
        insertarDatosSemilla();
        mostrarMenu();
    }

    private static void conectarDB() {
        // SUSTITUYE ESTA LÍNEA con la que copiaste de Atlas:
        String uri = "mongodb+srv://carlosfc2004:150804sfc@carlos.uiniqzs.mongodb.net/";

        try {
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("gestion_biblioteca");

            // Esta línea es vital para confirmar que la nube responde antes de seguir
            database.runCommand(new Document("ping", 1));

            colLibros = database.getCollection("libros");
            colPrestamos = database.getCollection("prestamos");
            System.out.println(">>> Conectado exitosamente a MongoDB Atlas!");
        } catch (Exception e) {
            System.err.println("Error de conexión: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void insertarDatosSemilla() {
        // Limpiamos para no duplicar en cada ejecución de prueba
        colLibros.drop();
        colPrestamos.drop();

        // Inserción en Colección Libros (Campos: Texto, Entero, Booleano, Lista)
        Document l1 = new Document("id_libro", 101)
                .append("titulo", "El Quijote")
                .append("autor", "Cervantes")
                .append("disponible", true)
                .append("categorias", Arrays.asList("Clásico", "Aventura"));

        Document l2 = new Document("id_libro", 102)
                .append("titulo", "Clean Code")
                .append("autor", "Robert C. Martin")
                .append("disponible", false)
                .append("categorias", Arrays.asList("Software", "Técnico"));

        colLibros.insertMany(Arrays.asList(l1, l2));

        // Inserción en Colección Préstamos (Relacionada por id_libro)
        Document p1 = new Document("id_prestamo", 5001)
                .append("id_libro", 102)
                .append("usuario", "Juan Pérez")
                .append("dias_prestamo", 15);

        colPrestamos.insertOne(p1);
        System.out.println(">>> Datos iniciales cargados con éxito.");
    }

    private static void mostrarMenu() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n--- MENÚ GESTIÓN BIBLIOTECA ---");
            System.out.println("1. Ver todos los libros");
            System.out.println("2. Buscar libro por título");
            System.out.println("3. Ver préstamos con detalle de libro (Relación)");
            System.out.println("4. Actualizar disponibilidad (Opcional)");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();
            sc.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1 -> listarLibros();
                case 2 -> buscarLibro();
                case 3 -> listarPrestamosRelacionados();
                case 4 -> actualizarLibro();
                case 0 -> System.out.println("Saliendo...");
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private static void listarLibros() {
        System.out.println("\n=== LISTADO DE LIBROS ===");
        for (Document doc : colLibros.find()) {
            imprimirLibroEstructurado(doc);
        }
    }

    private static void buscarLibro() {
        System.out.print("Introduce el título a buscar: ");
        String titulo = sc.nextLine();
        Document libro = colLibros.find(Filters.eq("titulo", titulo)).first();

        if (libro != null) {
            imprimirLibroEstructurado(libro);
        } else {
            System.out.println("No se encontró ningún libro con ese título.");
        }
    }

    private static void listarPrestamosRelacionados() {
        System.out.println("\n=== LISTADO DE PRÉSTAMOS ACTIVOS ===");
        for (Document prestamo : colPrestamos.find()) {
            int idLibro = prestamo.getInteger("id_libro");
            // Buscamos la relación en la otra colección
            Document libro = colLibros.find(Filters.eq("id_libro", idLibro)).first();

            System.out.println("------------------------------------");
            System.out.println("ID PRÉSTAMO: " + prestamo.getInteger("id_prestamo"));
            System.out.println("USUARIO    : " + prestamo.getString("usuario"));
            if (libro != null) {
                System.out.println("LIBRO      : " + libro.getString("titulo") + " (" + libro.getString("autor") + ")");
            }
            System.out.println("DURACIÓN   : " + prestamo.getInteger("dias_prestamo") + " días");
        }
    }

    private static void actualizarLibro() {
        System.out.print("ID del libro a modificar disponibilidad: ");
        int id = sc.nextInt();
        System.out.print("¿Está disponible? (true/false): ");
        boolean disp = sc.nextBoolean();

        colLibros.updateOne(Filters.eq("id_libro", id), new Document("$set", new Document("disponible", disp)));
        System.out.println("Libro actualizado correctamente.");
    }

    private static void imprimirLibroEstructurado(Document doc) {
        System.out.println("------------------------------------");
        System.out.println("ID      : " + doc.getInteger("id_libro"));
        System.out.println("TÍTULO  : " + doc.getString("titulo"));
        System.out.println("AUTOR   : " + doc.getString("autor"));
        System.out.println("ESTADO  : " + (doc.getBoolean("disponible") ? "Disponible" : "Prestado"));
        System.out.println("TAGS    : " + doc.getList("categorias", String.class));
    }
}