package de.taulinger.ccryptjcli;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.spec.InvalidKeySpecException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jabberwocky.ccryptj.CCrypt;

/**
 *
 * @author taulinger
 */
public class App {

    private final static Logger Log = LoggerFactory.getLogger(App.class);

    public void run(String... args) {

        try {
            final Options options = new Options();
            final OptionGroup group0 = new OptionGroup();
            group0.addOption(new Option("d", "decrypt", true, "resource to decrypt (file or http)"));
            group0.addOption(new Option("e", "encrypt", true, "resource to encrypt (file)"));
            group0.addOption(new Option("h", "help", false, "print the help"));
            options.addOptionGroup(group0);
            options.addOption(Option.builder("f").longOpt("force").hasArg(false).optionalArg(true).desc("overwrite existing file").build());

            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(options, args);

            if (cmd.getOptions().length > 0) {
                final boolean force = cmd.hasOption("f");
                if (cmd.hasOption("d")) {
                    final String resource = cmd.getOptionValue("d", "").trim();
                    final Path file;
                    if (resource.startsWith("http")) {
                        file = downloadResource(resource);
                    } else {
                        file = Paths.get(resource);
                    }
                    decrypt(file, force);

                } else if (cmd.hasOption("e")) {
                    final Path file = Path.of(cmd.getOptionValue("e"));
                    this.encrypt(file, force);
                } else if (cmd.hasOption("h")) {
                    printHelp(options);
                }

            } else {
                printHelp(options);
            }
        } catch (InvalidKeySpecException | IOException | ParseException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String... args) {
        new App().run(args);
    }

    public void printHelp(Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar ccrypt-j-cli", options, true);
    }

    public void encrypt(Path path, boolean force) throws URISyntaxException, IOException, InvalidKeySpecException {

        if (!Files.exists(path)) {
            throw new RuntimeException("File " + path.toString() + " does not exists");
        }

        final Path encryptedFile = path
                .getParent()
                .resolve(
                        path
                                .getFileName()
                                .toString() + ".cpt");

        if (!force && Files.exists(encryptedFile)) {
            throw new RuntimeException("File " + encryptedFile + " already exists");
        }

        final char[] secret0 = readPassword("Please enter password:");
        final char[] secret1 = readPassword("Please enter password again:");
        if (new String(secret0).compareTo(new String(secret1)) == 0) {
            final CCrypt crypto = new CCrypt(secret0);
            Log.debug("Encrypting: " + path);
            crypto.encrypt(path.toFile());

        } else {
            throw new RuntimeException("Passwords do not match");
        }

    }

    public void decrypt(Path path, boolean force) throws URISyntaxException, IOException, InvalidKeySpecException {

        if (!Files.exists(path)) {
            throw new RuntimeException("File " + path.toString() + " does not exists");
        }

        final Path decryptedFile = path
                .getParent()
                .resolve(
                        path
                                .getFileName()
                                .toString()
                                .split("\\.cpt")[0]);

        if (!force && Files.exists(decryptedFile)) {
            throw new RuntimeException("File " + decryptedFile + " already exists");
        }

        final char[] secret = readPassword("Please enter password:");

        Log.debug("Decrypting: " + path);

        final CCrypt crypto = new CCrypt(new String(secret));
        crypto.decrypt(path.toFile(), true);
    }

    char[] readPassword(String message) {
        return System.console().readPassword(message);
    }

    Path downloadResource(String resource) throws IOException, URISyntaxException {

        final URI uri = new URI(resource);

        final String resourceName = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
        final Path target = getCurrentFolder().resolve(resourceName);

        final Client client = ClientBuilder.newClient();
        final Response response = client.target(resource).request().get();
        if (response.getStatus() == 200) {

            try (InputStream in = response.readEntity(InputStream.class)) {

                Log.debug("Downloading to: " + target);

                Files.copy(
                        in,
                        target,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } else {
            throw new RuntimeException(resource + " could not be fetched: " + response.getStatus());
        }
    }

    Path getCurrentFolder() throws URISyntaxException {
        return Path
                .of(App.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParent();
    }
}
