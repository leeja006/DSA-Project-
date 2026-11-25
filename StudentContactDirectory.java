import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Student Contact Directory
 * PS No. 24
 *
 * Features:
 * 1. Add contact
 * 2. Display all contacts
 * 3. Search by name or phone number
 * 4. Delete contact
 * 5. Update contact
 * 6. Exit
 *
 * Data is stored in contacts.txt in the program's current working directory.
 */
public class StudentContactDirectory {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<Contact> contacts = new ArrayList<>();
    private static final HashMap<String, Contact> phoneIndex = new HashMap<>();
    private static final Path DATA_FILE = Paths.get("contacts.txt");

    // Contact model
    private static class Contact {
        private String name;
        private String phone;
        private String email;

        Contact(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }
    }

    public static void main(String[] args) {
        loadContacts();

        System.out.println("========================================");
        System.out.println("       STUDENT CONTACT DIRECTORY");
        System.out.println("========================================");
        System.out.println("Loaded " + contacts.size() + " contact(s).");

        boolean running = true;

        while (running) {
            showMenu();
            int choice = readMenuChoice();

            switch (choice) {
                case 1:
                    addContact();
                    break;
                case 2:
                    displayContacts();
                    break;
                case 3:
                    searchContact();
                    break;
                case 4:
                    deleteContact();
                    break;
                case 5:
                    updateContact();
                    break;
                case 6:
                    saveContacts();
                    System.out.println("\nContacts saved successfully.");
                    System.out.println("Thank you for using Student Contact Directory!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a number from 1 to 6.");
            }
        }

        scanner.close();
    }

    private static void showMenu() {
        System.out.println("\n--------------- MAIN MENU ---------------");
        System.out.println("1. Add Contact");
        System.out.println("2. Display All Contacts");
        System.out.println("3. Search Contact");
        System.out.println("4. Delete Contact");
        System.out.println("5. Update Contact");
        System.out.println("6. Exit");
        System.out.println("-----------------------------------------");
        System.out.print("Enter your choice: ");
    }

    private static int readMenuChoice() {
        String input = scanner.nextLine().trim();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // 1. Add a new contact
    private static void addContact() {
        System.out.println("\n----------- ADD CONTACT -----------");

        String name = readRequiredText("Enter name: ");
        String phone = readPhone("Enter 10-digit phone number: ");

        if (phoneIndex.containsKey(phone)) {
            System.out.println("A contact with this phone number already exists.");
            return;
        }

        String email = readRequiredText("Enter email: ");

        Contact contact = new Contact(name, phone, email);
        contacts.add(contact);
        phoneIndex.put(phone, contact);

        saveContacts();
        System.out.println("Contact added successfully.");
        printContact(contact);
    }

    // 2. Display all contacts
    private static void displayContacts() {
        System.out.println("\n----------- CONTACT DIRECTORY -----------");

        if (contacts.isEmpty()) {
            System.out.println("Directory is now empty.");
            return;
        }

        System.out.printf("%-5s %-22s %-15s %-30s%n",
                "No.", "Name", "Phone", "Email");
        System.out.println("--------------------------------------------------------------------------");

        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%-5d %-22s %-15s %-30s%n",
                    i + 1, c.name, c.phone, c.email);
        }

        System.out.println("\nTotal contacts: " + contacts.size());
    }

    // 3. Search by name or phone number
    private static void searchContact() {
        System.out.println("\n----------- SEARCH CONTACT -----------");
        System.out.println("1. Search by name");
        System.out.println("2. Search by phone number");
        System.out.print("Choose search method: ");

        int choice = readMenuChoice();

        if (choice == 1) {
            System.out.print("Enter name to search: ");
            String query = scanner.nextLine().trim();

            if (query.isEmpty()) {
                System.out.println("Search name cannot be empty.");
                return;
            }

            boolean found = false;
            for (Contact c : contacts) {
                if (c.name.toLowerCase().contains(query.toLowerCase())) {
                    printContact(c);
                    found = true;
                }
            }

            if (!found) {
                System.out.println("No contact found with that name.");
            }

        } else if (choice == 2) {
            System.out.print("Enter phone number to search: ");
            String phone = scanner.nextLine().trim();

            Contact contact = phoneIndex.get(phone);
            if (contact != null) {
                printContact(contact);
            } else {
                System.out.println("No contact found with that phone number.");
            }

        } else {
            System.out.println("Invalid search option.");
        }
    }

    // 4. Delete a contact using its phone number
    private static void deleteContact() {
        System.out.println("\n----------- DELETE CONTACT -----------");

        if (contacts.isEmpty()) {
            System.out.println("Directory is already empty.");
            return;
        }

        System.out.print("Enter the phone number of the contact to delete: ");
        String phone = scanner.nextLine().trim();

        Contact contact = phoneIndex.get(phone);

        if (contact == null) {
            System.out.println("Contact not found.");
            return;
        }

        contacts.remove(contact);
        phoneIndex.remove(phone);

        saveContacts();
        System.out.println("Contact deleted.");
        System.out.println("Directory now contains " + contacts.size() + " contact(s).");
    }

    // 5. Update an existing contact
    private static void updateContact() {
        System.out.println("\n----------- UPDATE CONTACT -----------");

        if (contacts.isEmpty()) {
            System.out.println("No contacts available to update.");
            return;
        }

        System.out.print("Enter the current phone number of the contact: ");
        String oldPhone = scanner.nextLine().trim();

        Contact contact = phoneIndex.get(oldPhone);

        if (contact == null) {
            System.out.println("Contact not found.");
            return;
        }

        System.out.println("\nCurrent contact details:");
        printContact(contact);

        System.out.println("\nPress Enter without typing anything to keep the current value.");

        System.out.print("New name [" + contact.name + "]: ");
        String newName = scanner.nextLine().trim();

        System.out.print("New phone [" + contact.phone + "]: ");
        String newPhone = scanner.nextLine().trim();

        System.out.print("New email [" + contact.email + "]: ");
        String newEmail = scanner.nextLine().trim();

        // Validate everything before changing the stored contact.
        if (!newPhone.isEmpty() && !isValidPhone(newPhone)) {
            System.out.println("Invalid phone number. It must contain exactly 10 digits.");
            return;
        }

        if (!newPhone.isEmpty()) {
            Contact existing = phoneIndex.get(newPhone);
            if (existing != null && existing != contact) {
                System.out.println("Another contact already uses this phone number.");
                return;
            }
        }

        if (!newName.isEmpty()) {
            contact.name = newName;
        }

        if (!newPhone.isEmpty() && !newPhone.equals(oldPhone)) {
            phoneIndex.remove(oldPhone);
            contact.phone = newPhone;
            phoneIndex.put(newPhone, contact);
        }

        if (!newEmail.isEmpty()) {
            contact.email = newEmail;
        }

        saveContacts();
        System.out.println("Contact updated successfully.");
        printContact(contact);
    }

    // Read a non-empty text field
    private static String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field cannot be empty. Please try again.");
        }
    }

    // Read and validate a 10-digit phone number
    private static String readPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String phone = scanner.nextLine().trim();

            if (!isValidPhone(phone)) {
                System.out.println("Invalid phone number. Enter exactly 10 digits.");
                continue;
            }

            return phone;
        }
    }

    private static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[0-9]{10}");
    }

    // Print one contact's details
    private static void printContact(Contact c) {
        System.out.println("-----------------------------------------");
        System.out.println("Name  : " + c.name);
        System.out.println("Phone : " + c.phone);
        System.out.println("Email : " + c.email);
        System.out.println("-----------------------------------------");
    }

    // Load saved contacts when the program starts
    private static void loadContacts() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(DATA_FILE, StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length != 3) {
                        continue;
                    }

                    String name = decode(parts[0]);
                    String phone = decode(parts[1]);
                    String email = decode(parts[2]);

                    if (name.isEmpty() || email.isEmpty() || !isValidPhone(phone)
                            || phoneIndex.containsKey(phone)) {
                        continue;
                    }

                    Contact contact = new Contact(name, phone, email);
                    contacts.add(contact);
                    phoneIndex.put(phone, contact);

                } catch (IllegalArgumentException e) {
                    // Ignore a malformed record and continue loading the remaining file.
                }
            }

        } catch (IOException e) {
            System.out.println("Warning: Could not read contacts.txt: " + e.getMessage());
        }
    }

    // Save all contacts whenever the directory changes
    private static void saveContacts() {
        try (BufferedWriter writer = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8)) {
            for (Contact c : contacts) {
                writer.write(encode(c.name) + "|" + encode(c.phone) + "|" + encode(c.email));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving contacts: " + e.getMessage());
        }
    }

    // Base64 encoding keeps special characters from breaking the file format.
    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        byte[] decoded = Base64.getDecoder().decode(value);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
