package edu.diploma;

import edu.diploma.auth.JwtHelper;
import edu.diploma.model.*;
import edu.diploma.repository.FileContentRepository;
import edu.diploma.repository.FileRepository;
import org.javatuples.Pair;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;


@RestController
public class FileController {

    private final ResponseEntity<?> response400 = ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new AppError(HttpStatus.BAD_REQUEST.value(), "Error input data"));

    private final ResponseEntity<?> response500 = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server Error"));

    private final AuthenticationManager authManager;
    private final JwtHelper jwtHelper;
    private final FileRepository fileRepository;
    private final FileContentRepository fileContentRepository;

    public FileController(
            AuthenticationManager authManager,
            JwtHelper jwtHelper,
            FileRepository fileRepository,
            FileContentRepository fileContentRepository
    ) {
        this.authManager = authManager;
        this.jwtHelper = jwtHelper;
        this.fileRepository = fileRepository;
        this.fileContentRepository = fileContentRepository;
    }


    @PutMapping("/file")
    public ResponseEntity<?> putFile(
            @RequestParam("filename") String filename,
            @RequestBody NewFilename newFilename
    ) {
        return Optional.ofNullable(filename).filter(
                fileName -> !fileName.trim().isEmpty()
        ).flatMap(
                fileName -> Optional.ofNullable(newFilename).map(
                        theNewFilename -> Pair.with(fileName,theNewFilename)
                )
        ).flatMap(
                fileName_theNewFilename -> fileRepository.findOne(Example.of(
                        new File(fileName_theNewFilename.getValue0()),
                        ExampleMatcher.matching()
                                .withMatcher("name", m -> m.exact())
                                .withIgnorePaths("contentType", "createdAt", "editedAt", "size")
                ))
        ).map(
                file -> {
                    file.setName(newFilename.getFilename());
                    file.setEditedAt(
                            LocalDate.now()
                                    .atTime(LocalTime.now()).atZone(ZoneId.systemDefault())
                                    .toInstant().toEpochMilli()
                    );
                    return file;
                }
        ).map(file -> {
            try {
                fileRepository.save(file);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return response500;
            }
        }).orElseGet(
                () -> response400
        );
    }


    @GetMapping("/file")
    public ResponseEntity<?> getFile(@RequestParam("filename") String filename) {
        return Optional.ofNullable(filename).filter(
                fileName -> !fileName.trim().isEmpty()
        ).flatMap(
                fileName -> fileRepository.findOne(Example.of(
                        new File(fileName),
                        ExampleMatcher.matching()
                                .withMatcher("name", m -> m.exact())
                                .withIgnorePaths("contentType", "createdAt", "editedAt", "size")
                ))
        ).flatMap(
                file -> fileContentRepository.findOne(Example.of(new FileContent(file)))
        ).map(content -> {
            try (InputStream in = new ByteArrayInputStream(content.getContent())) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE,content.getFile().getContentType())
                        .body(new InputStreamResource(in));
            } catch (Exception e) {
                return response500;
            }
        }).orElseGet(
                () -> response400
        );
    }


    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestParam("filename") String filename) {

        return Optional.ofNullable(filename).filter(
                fileName -> !fileName.trim().isEmpty()
        ).map(
            fileName -> fileRepository.findAll(Example.of(
                    new File(fileName),
                    ExampleMatcher.matching().withMatcher("name", m -> m.caseSensitive())
                            .withIgnorePaths("contentType", "createdAt", "editedAt", "size")
                    ))
        ).map(files -> {
            try {
                fileRepository.deleteAll(files);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return response500;
            }
        }).orElseGet(
                () -> response400
        );

    }


    @PostMapping("/file")
    public ResponseEntity<?> postFile(
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile content
    ) {

        if (Objects.isNull(filename) || filename.trim().isEmpty() || Objects.isNull(content)) {
            return response400;
        }

        try {
            long now = LocalDate.now().atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            File file = fileRepository.save(
                    new File(
                            filename,
                            content.getContentType(),
                            now, now,
                            content.getSize()
                    )
            );
            fileContentRepository.save(new FileContent(file, content.getBytes()));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return response400;
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFiles(
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return Optional.ofNullable(limit).map(
                l -> PageRequest.of(0, l, Sort.by(Sort.Direction.ASC, "name"))
        ).map( pageRequest -> {
            try {
                return ResponseEntity.ok().body(
                            fileRepository.findAll(pageRequest).stream().toList()
                        );
            } catch (Exception e) {
                return response500;
            }
        }).orElseGet(() -> {
            try {
                return ResponseEntity.ok().body(fileRepository.findAll());
            } catch (Exception e) {
                return response500;
            }
        });
    }

}