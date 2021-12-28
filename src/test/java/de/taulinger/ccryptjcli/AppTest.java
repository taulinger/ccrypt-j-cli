package de.taulinger.ccryptjcli;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.spec.InvalidKeySpecException;
import org.apache.commons.cli.Options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author taulinger
 */
@ExtendWith({ MockitoExtension.class, MockServerExtension.class })
public class AppTest {

	@Spy
	private App app;

	@Test
	public void cliShouldCallPrintHelpMethod() {

		doNothing().when(app).printHelp(any(Options.class));

		app.run("");
		app.run("-h");
		app.run("--help");

		verify(app, times(3)).printHelp(any(Options.class));
	}

	@Test
	public void cliShouldCallDecryptMethod() throws URISyntaxException, IOException, InvalidKeySpecException {

		var file = "/foo/bar.cpt";
		doNothing().when(app).decrypt(eq(Path.of(file)), any(Boolean.class));

		app.run("-d", file);
		app.run("--decrypt", "/foo/bar.cpt");
		app.run("-d", "/foo/bar.cpt", "-f");
		app.run("--decrypt", "/foo/bar.cpt", "-f");

		verify(app, times(2)).decrypt(Path.of(file), false);
		verify(app, times(2)).decrypt(Path.of(file), true);
	}

	@Test
	public void cliShouldCallEncryptMethod() throws URISyntaxException, IOException, InvalidKeySpecException {

		doNothing().when(app).encrypt(any(Path.class), any(Boolean.class));

		app.run("-e", "/foo/bar.some");
		app.run("--encrypt", "/foo/bar.some");
		app.run("-e", "/foo/bar.some", "-f");
		app.run("--encrypt", "/foo/bar.some", "-f");

		verify(app, times(2)).encrypt(Path.of("/foo/bar.some"), false);
		verify(app, times(2)).encrypt(Path.of("/foo/bar.some"), true);
	}

	@Test
	public void shouldDecryptFile(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var filename = "file.txt.cpt";
		doReturn("secret".toCharArray()).when(app).readPassword(eq("Please enter password:"));
		var testFile = Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(filename);
		Files.copy(testFile, tempDir.resolve(filename));

		app.decrypt(tempDir.resolve(filename), false);

		assertEquals("42", Files.readString(tempDir.resolve("file.txt")));
	}

	@Test
	public void decryptShouldThrowWhenFileDoesNotExists(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var filename = "file.txt.cpt";

		var thrown = assertThrows(RuntimeException.class, () -> {
			app.decrypt(tempDir.resolve(filename), false);
		});

		assertTrue(thrown.getMessage().contains("File " + tempDir.resolve(filename) + " does not exists"));
	}

	@Test
	public void shouldNotDecryptWhenTargetFileAlreadyExists(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var encryptedFilename = "file.txt.cpt";
		var decryptedFilename = "file.txt";
		Files.copy(Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(encryptedFilename),
				tempDir.resolve(encryptedFilename));
		Files.createFile(tempDir.resolve(decryptedFilename));

		var thrown = assertThrows(RuntimeException.class, () -> {
			app.decrypt(tempDir.resolve(encryptedFilename), false);
		}, "File " + decryptedFilename + " already exists");

		assertTrue(thrown.getMessage().contains("File " + tempDir.resolve(decryptedFilename) + " already exists"));
	}

	@Test
	public void shouldDecryptWhenTargetFileAlreadyExists(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var encryptedFilename = "file.txt.cpt";
		var decryptedFilename = "file.txt";
		Files.copy(Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(encryptedFilename),
				tempDir.resolve(encryptedFilename));
		Files.createFile(tempDir.resolve(decryptedFilename));
		doReturn("secret".toCharArray()).when(app).readPassword(eq("Please enter password:"));

		app.decrypt(tempDir.resolve(encryptedFilename), true);

		assertEquals("42", Files.readString(tempDir.resolve("file.txt")));
	}

	@Test
	public void shouldDownloadHttpResource(@TempDir Path tempDir, ClientAndServer client)
			throws URISyntaxException, IOException, InvalidKeySpecException {
		var filename = "file.txt.cpt";
		var testFile = Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(filename);
		var testResource = "http://localhost:" + client.getPort() + "/" + filename;
		client.when(request().withMethod("GET").withPath("/" + filename), exactly(1))
				.respond(response().withBody(Files.readAllBytes(testFile)));
		doReturn(tempDir).when(app).getCurrentFolder();

		var file = app.downloadResource(testResource);

		assertEquals(file, tempDir.resolve(filename));
	}

	@Test
	public void downloadShouldThrowAnExceptionWhenNotFound(@TempDir Path tempDir, ClientAndServer client)
			throws URISyntaxException, IOException, InvalidKeySpecException {
		var filename = "file.txt.cpt";
		var testResource = "http://localhost:" + client.getPort() + "/" + filename;
		client.when(request().withMethod("GET").withPath("/file.txt.cpt"), exactly(1))
				.respond(response().withStatusCode(404));
		doReturn(tempDir).when(app).getCurrentFolder();

		var thrown = assertThrows(RuntimeException.class, () -> {
			app.downloadResource(testResource);
		});

		assertTrue(thrown.getMessage().contains(testResource + " could not be fetched: 404"));
	}

	@Test
	public void shouldEncryptFile(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var filename = "file.txt";
		doReturn("secret".toCharArray()).when(app).readPassword(startsWith("Please enter password"));
		var testFile = Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(filename);
		Files.copy(testFile, tempDir.resolve(filename));

		app.encrypt(tempDir.resolve(filename), false);

		assertTrue(Files.exists(tempDir.resolve("file.txt.cpt")));
	}

	@Test
	public void encryptShouldThrowWhenFileDoesNotExists(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var filename = "file.txt";

		var thrown = assertThrows(RuntimeException.class, () -> {
			app.encrypt(tempDir.resolve(filename), false);
		});

		assertTrue(thrown.getMessage().contains("File " + tempDir.resolve(filename) + " does not exists"));
	}

	@Test
	public void shouldNotEncryptWhenTargetFileAlreadyExists(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var encryptedFilename = "file.txt.cpt";
		var decryptedFilename = "file.txt";
		Files.copy(Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(decryptedFilename),
				tempDir.resolve(decryptedFilename));
		Files.createFile(tempDir.resolve(encryptedFilename));

		var thrown = assertThrows(RuntimeException.class, () -> {
			app.encrypt(tempDir.resolve(decryptedFilename), false);
		});

		assertTrue(thrown.getMessage().contains("File " + tempDir.resolve(encryptedFilename) + " already exists"));
	}

	@Test
	public void shouldEncryptWhenTargetFileAlreadyExists(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var encryptedFilename = "file.txt.cpt";
		var decryptedFilename = "file.txt";
		Files.copy(Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(decryptedFilename),
				tempDir.resolve(decryptedFilename));
		Files.createFile(tempDir.resolve(encryptedFilename));
		doReturn("secret".toCharArray()).when(app).readPassword(startsWith("Please enter password"));

		app.encrypt(tempDir.resolve(decryptedFilename), true);

		assertTrue(Files.exists(tempDir.resolve(encryptedFilename)));
	}

	@Test
	public void shouldNotEncryptWhenSecretsDoNotMatch(@TempDir Path tempDir)
			throws URISyntaxException, IOException, InvalidKeySpecException {

		var filename = "file.txt";
		doReturn("secret".toCharArray(), "secret42".toCharArray()).when(app)
				.readPassword(startsWith("Please enter password"));
		var testFile = Path.of("", "src/test/resources/de/taulinger/ccryptjcli").resolve(filename);
		Files.copy(testFile, tempDir.resolve(filename));

		var thrown = assertThrows(RuntimeException.class, () -> {
			app.encrypt(tempDir.resolve(filename), false);
		});

		assertTrue(thrown.getMessage().contains("Passwords do not match"));
	}

}
