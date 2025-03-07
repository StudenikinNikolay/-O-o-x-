package edu.diploma;

import edu.diploma.auth.JwtHelper;
import edu.diploma.model.AppError;
import edu.diploma.model.File;
import edu.diploma.model.FileContent;
import edu.diploma.model.NewFilename;
import edu.diploma.repository.FileContentRepository;
import edu.diploma.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class FileControllerTest {

    private final List<File> files = List.of(
            new File("File One", "text/plain", 123,124,123),
            new File("File Two", "text/html", 234,245,234),
            new File("File Three", "image/jpeg", 345,456,236),
            new File("File Four", "audio/mpeg", 456,567,345)
    );

    private AuthenticationManager authManager;
    private JwtHelper jwtHelper;
    private FileRepository fileRepository;
    private FileContentRepository fileContentRepository;
    private FileController fileController;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testPutFile400WhenNewFilenameNull() {

        final String filename = "A File.txt";
        final NewFilename newFilename = null;

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.putFile(filename, newFilename);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPutFile400WhenFilenameNull() {

        final String filename = null;
        final NewFilename newFilename = new NewFilename("A New File.txt");

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.putFile(filename, newFilename);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPutFile500WhenSave() {

        final String filename = "A File.txt";
        final NewFilename newFilename = new NewFilename("A New File.txt");
        final File file = new File(filename);

        fileRepository = mock(FileRepository.class);
        when(fileRepository.findOne(any(Example.class))).thenReturn(Optional.of(file));
        when(fileRepository.save(any(File.class))).thenThrow(new RuntimeException("DB Error"));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.putFile(filename, newFilename);

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testPutFileOk() {

        final String filename = "A File.txt";
        final NewFilename newFilename = new NewFilename("A New File.txt");
        final File file = new File(filename);

        fileRepository = mock(FileRepository.class);
        when(fileRepository.findOne(any(Example.class))).thenReturn(Optional.of(file));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.putFile(filename, newFilename);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(file.getName(), is(newFilename.getFilename()));
    }

    @Test
    public void testGetFile400WhenFilenameNull() throws Exception {

        final String filename = null;

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFile(filename);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetFile500WhenReadingContentErrors() throws Exception {

        final String filename = "A File.txt";
        final String contents = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        final File file = new File(filename);

        fileRepository = mock(FileRepository.class);
        when(fileRepository.findOne(any(Example.class))).thenReturn(Optional.of(file));

        FileContent fileContent = mock(FileContent.class);
        when(fileContent.getFile()).thenReturn(file);
        when(fileContent.getContent()).thenThrow(new RuntimeException("Error reading file contents"));

        fileContentRepository = mock(FileContentRepository.class);
        when(fileContentRepository.findOne(any(Example.class))).thenReturn(Optional.of(fileContent));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFile(filename);

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testGetFileOk() throws Exception {

        final String filename = "A File.txt";
        final String contents = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        final File file = new File(filename);

        fileRepository = mock(FileRepository.class);
        when(fileRepository.findOne(any(Example.class))).thenReturn(Optional.of(file));

        FileContent fileContent = new FileContent(file, contents.getBytes(StandardCharsets.UTF_8));

        fileContentRepository = mock(FileContentRepository.class);
        when(fileContentRepository.findOne(any(Example.class))).thenReturn(Optional.of(fileContent));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFile(filename);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(
                ((InputStreamResource) response.getBody()).getInputStream().readAllBytes(),
                is(fileContent.getContent())
        );
    }

    @Test
    public void testDeleteFile400WhenFilenameNull() {

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        final String filename = null;

        ResponseEntity<?> response = fileController.deleteFile(filename);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testDeleteFile500WhenDbErrors() {
        fileRepository = mock(FileRepository.class);

        when(fileRepository.findAll(any(Example.class))).thenReturn(List.of(files.get(0)));
        doThrow(new RuntimeException("DB Error")).when(fileRepository).deleteAll(any(List.class));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.deleteFile(files.get(0).getName());

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testDeleteFileOk() {
        fileRepository = mock(FileRepository.class);

        when(fileRepository.findAll(any(Example.class))).thenReturn(List.of(files.get(0)));
        doNothing().when(fileRepository).deleteAll(any(List.class));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.deleteFile(files.get(0).getName());

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void testPostFile400WhenDbExceptionOnSaveContent() throws Exception {

        MultipartFile content = mock(MultipartFile.class);
        when(content.getContentType()).thenReturn(files.get(0).getContentType());
        when(content.getSize()).thenReturn(files.get(0).getSize());
        when(content.getBytes()).thenReturn(new byte[0]);

        fileRepository = mock(FileRepository.class);
        when(fileRepository.save(ArgumentMatchers.any(File.class))).thenReturn(files.get(0));

        FileContent fileContent = new FileContent(files.get(0), content.getBytes());

        fileContentRepository = mock(FileContentRepository.class);
        when(fileContentRepository.save(fileContent))
                .thenThrow(new RuntimeException("DB Error"));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.postFile(files.get(0).getName(), content);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPostFile400WhenDbExceptionOnSave() throws Exception {

        MultipartFile content = mock(MultipartFile.class);
        when(content.getContentType()).thenReturn(files.get(0).getContentType());
        when(content.getSize()).thenReturn(files.get(0).getSize());
        when(content.getBytes()).thenReturn(new byte[0]);

        fileRepository = mock(FileRepository.class);
        when(fileRepository.save(ArgumentMatchers.any(File.class)))
                .thenThrow(new RuntimeException("DB Error"));

        FileContent fileContent = new FileContent(files.get(0), content.getBytes());

        fileContentRepository = mock(FileContentRepository.class);
        when(fileContentRepository.save(fileContent)).thenReturn(fileContent);

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.postFile(files.get(0).getName(), content);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPostFile400WhenNullContent() throws Exception {

        final String filename = "File One";
        final MultipartFile content = null;

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.postFile(filename, content);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPostFile400WhenBlankFilename() throws Exception {

        MultipartFile content = mock(MultipartFile.class);

        final String filename = "   \t\n     ";

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.postFile(filename, content);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPostFile400WhenEmptyFilename() throws Exception {

        MultipartFile content = mock(MultipartFile.class);

        final String filename = "";

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.postFile(filename, content);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPostFile400WhenNullFilename() throws Exception {

        MultipartFile content = mock(MultipartFile.class);

        final String filename = null;

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.postFile(filename, content);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testPostFileOk() throws Exception {

        MultipartFile content = mock(MultipartFile.class);
        when(content.getContentType()).thenReturn(files.get(0).getContentType());
        when(content.getSize()).thenReturn(files.get(0).getSize());
        when(content.getBytes()).thenReturn(new byte[0]);

        fileRepository = mock(FileRepository.class);
        when(fileRepository.save(ArgumentMatchers.any(File.class))).thenReturn(files.get(0));

        FileContent fileContent = new FileContent(files.get(0), content.getBytes());

        fileContentRepository = mock(FileContentRepository.class);
        when(fileContentRepository.save(fileContent)).thenReturn(fileContent);

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.postFile(files.get(0).getName(), content);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void testGetFiles500WithoutLimitOnRepositoryException() {

        final Integer limit = null;

        fileRepository = mock(FileRepository.class);
        when(fileRepository.findAll()).thenThrow(new RuntimeException("Some DB Error"));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFiles(limit);

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(((AppError) response.getBody()).getCode(), is(500));
        assertThat(((AppError) response.getBody()).getMessage(), is("Server Error"));
    }

    @Test
    public void testGetFiles500WithLimitOnRepositoryException() {

        Integer limit = 3;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "name"));

        fileRepository = mock(FileRepository.class);
        when(fileRepository.findAll(pageRequest)).thenThrow(new RuntimeException("Some DB Error"));

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFiles(limit);

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(((AppError) response.getBody()).getCode(), is(500));
        assertThat(((AppError) response.getBody()).getMessage(), is("Server Error"));
    }

    @Test
    public void testGetFilesOkWithNoLimit() {

        Integer limit = null;


        fileRepository = mock(FileRepository.class);
        when(fileRepository.findAll()).thenReturn(files);

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFiles(limit);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(((List<File>) response.getBody()).size(), is(files.size()));
    }

    @Test
    public void testGetFilesOkWithGreaterLimit() {

        Integer limit = 3 + 100;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "name"));

        Page<File> page = mock(Page.class);
        when(page.stream()).thenReturn(files.stream());


        fileRepository = mock(FileRepository.class);
        when(fileRepository.findAll(pageRequest)).thenReturn(page);

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFiles(limit);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(((List<File>) response.getBody()).size(), is(files.size()));
    }

    @Test
    public void testGetFilesOkWithDefaultLimit() {

        Integer limit = 3;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "name"));

        Page<File> page = mock(Page.class);
        when(page.stream()).thenReturn(files.subList(0,limit).stream());


        fileRepository = mock(FileRepository.class);
        when(fileRepository.findAll(pageRequest)).thenReturn(page);

        fileController = new FileController(
                authManager, jwtHelper, fileRepository, fileContentRepository
        );

        ResponseEntity<?> response = fileController.getFiles(limit);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(((List<File>) response.getBody()).size(), is(limit));
    }
}
