package org.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.util.Arrays;
import java.util.Scanner;

public class GestionTienda {
    // SUSTITUYE ESTO POR TU CADENA DE CONEXIÓN REAL
    private static final String CONNECTION_STRING = "mongodb+srv://carlosfc2004:150804.sfC@carlos.uiniqzs.mongodb.net/";

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase("TiendaInformatica");

            System.out.println("Conectado con éxito a Atlas.");

            // 1. Inserción inicial (Punto 2 del enunciado)
            inicializarDatos();

            // 2. Menú principal (Punto 3 del enunciado)
            menu();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mongoClient.close();
        }
    }

    private static void inicializarDatos() {
        MongoCollection<Document> clientes = database.getCollection("clientes");
        if (clientes.countDocuments() == 0) {
            // Ejemplo de documento con String, Boolean y Listas
            Document cliente1 = new Document("dni", "12345678A")
                    .append("nombre", "Carlos Perez")
                    .append("esPremium", true)
                    .append("etiquetas", Arrays.asList("gaming", "teclados"));

            clientes.insertOne(cliente1);

            // Segunda colección relacionada lógicamente por el DNI
            MongoCollection<Document> pedidos = database.getCollection("pedidos");
            Document pedido1 = new Document("id_pedido", 5001)
                    .append("dni_cliente", "12345678A")
                    .append("total", 150.50)
                    .append("productos", Arrays.asList("Teclado Mecánico", "Alfombrilla"));

            pedidos.insertOne(pedido1);
            System.out.println("Datos de prueba insertados.");
        }
    }

    private static void menu() {
        int opcion = 0;
        while (opcion != 4) {
            System.out.println("\n--- SISTEMA DE GESTIÓN MONGODB ---");
            System.out.println("1. Listar Clientes");
            System.out.println("2. Buscar Pedidos por DNI (Filtro)");
            System.out.println("3. Actualizar Cliente (Opcional/Ampliación)");
            System.out.println("4. Salir");
            System.out.print("Elige: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> listarClientes();
                case 2 -> buscarPedidos();
                case 3 -> actualizarCliente();
                case 4 -> System.out.println("Desconectando...");
            }
        }
    }

    private static void listarClientes() {
        System.out.println("\n--- LISTADO DE CLIENTES ---");
        for (Document doc : database.getCollection("clientes").find()) {
            // Impresión estructurada (Punto 4 del enunciado)
            System.out.println("CLIENTE: " + doc.getString("nombre") +
                    " | DNI: " + doc.getString("dni") +
                    " | PREMIUM: " + doc.getBoolean("esPremium"));
        }
    }

    private static void buscarPedidos() {
        System.out.print("Introduce el DNI para ver sus pedidos: ");
        String dni = sc.nextLine();
        MongoCollection<Document> pedidos = database.getCollection("pedidos");

        // Uso de filtros
        for (Document doc : pedidos.find(Filters.eq("dni_cliente", dni))) {
            System.out.println("🛒 PEDIDO #" + doc.getInteger("id_pedido") +
                    " | TOTAL: " + doc.getDouble("total") + "€");
        }
    }

    private static void actualizarCliente() {
        System.out.print("DNI del cliente a mejorar a Premium: ");
        String dni = sc.nextLine();
        database.getCollection("clientes").updateOne(
                Filters.eq("dni", dni),
                Updates.set("esPremium", true)
        );
        System.out.println("Cliente actualizado.");
    }
}