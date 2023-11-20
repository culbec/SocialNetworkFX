package ro.ubbcluj.map.socialnetworkfx.utility;

import ro.ubbcluj.map.socialnetworkfx.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class RandomUserGenerator {
    // Collection of Romanian firstnames.
    private static final ArrayList<String> FIRSTNAMES = new ArrayList<>(
            Arrays.asList("Adrian", "Alexandru", "Alin", "Andrei", "Aurel",
                    "Bogdan", "Calin", "Catalin", "Carol", "Cezar", "Claudiu", "Constantin",
                    "Ion", "Ioan", "Ionel", "Cosmin", "Dan", "Daniel", "Dumitru", "Eugen", "Eusebiu",
                    "Filip", "Flavius", "Gabriel", "Marius", "Marian", "Iulian", "Laurentiu", "Lucian",
                    "Matei", "Nicolae", "Radu", "Razvan", "Remus", "Robert", "Sebastian", "Sergiu", "Silviu",
                    "Vlad", "Vasile"));

    // Collection of Romanian lastnames.
    private static final ArrayList<String> LASTNAMES = new ArrayList<>(
            Arrays.asList("Albu", "Chiriac", "Manole", "Baciu", "Balan", "Botezatu",
                    "Bucur", "Ciobanu", "Grigorescu", "Ionescu", "Iliescu", "Iordanescu", "Lupu", "Marcu",
                    "Matei", "Mitrea", "Popovici", "Popescu", "Muresan", "Popa", "Olaroiu", "Radu", "Rosu",
                    "Ungureanu", "Bathory", "Ambrosia")
    );

    /**
     * Generates 20 users.
     *
     * @return A list of 20 users.
     */
    public static ArrayList<User> generate20Users() {
        Collections.shuffle(FIRSTNAMES);
        Collections.shuffle(LASTNAMES);

        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            int randomIndexFirstName = new Random().nextInt(FIRSTNAMES.size());
            int randomIndexLastName = new Random().nextInt(LASTNAMES.size());

            String firstName = FIRSTNAMES.get(randomIndexFirstName);
            String lastName = LASTNAMES.get(randomIndexLastName);
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@mail.com";

            users.add(new User(firstName, lastName, email));
        }

        return users;
    }
}
