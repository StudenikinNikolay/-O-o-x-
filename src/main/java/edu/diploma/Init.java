package edu.diploma;

import edu.diploma.model.File;
import edu.diploma.model.FileContent;
import edu.diploma.model.User;
import edu.diploma.repository.FileContentRepository;
import edu.diploma.repository.FileRepository;
import edu.diploma.repository.UserRepository;
import jakarta.servlet.ServletContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ResourceUtils;

import java.nio.file.Files;
import java.time.*;

@Configuration
public class Init {

    @Bean
    public CommandLineRunner initDatabase(
            BCryptPasswordEncoder passwordEncoder,
            UserRepository userRepository,
            FileRepository fileRepository,
            FileContentRepository fileContentRepository,
            ServletContext ctx
    ) {
        return args -> {
            userRepository.save(new User("test", passwordEncoder.encode("test")));
            userRepository.save(new User("user2@mail.edu", passwordEncoder.encode("234")));

            ZonedDateTime now = LocalDate.now().atTime(LocalTime.now()).atZone(ZoneId.systemDefault());

            File file1 = fileRepository.save(new File(
                    "Lorem Ipsum.txt",
                    "text/plain",
                    now.minusDays(10).toInstant().toEpochMilli(),
                    now.minusDays(10).toInstant().toEpochMilli(),
                    653L
            ));
            fileContentRepository.save(new FileContent(
                    file1,
                    Files.readAllBytes(
                            ResourceUtils.getFile("classpath:demo/Lorem Ipsum.txt").toPath()
                    )
            ));

            File file2 = fileRepository.save(new File(
                    "Празднование.avif",
                    "image/avif",
                    now.minusDays(7).toInstant().toEpochMilli(),
                    now.minusDays(7).toInstant().toEpochMilli(),
                    6310L
            ));
            fileContentRepository.save(new FileContent(
                    file2,
                    Files.readAllBytes(
                            ResourceUtils.getFile("classpath:demo/milestone_celebration.avif").toPath()
                    )
            ));

            File file3 = fileRepository.save(new File(
                    "Рингтон.mp3",
                    "audio/mpeg",
                    now.minusDays(6).toInstant().toEpochMilli(),
                    now.minusDays(6).toInstant().toEpochMilli(),
                    438271L
            ));
            fileContentRepository.save(new FileContent(
                    file3,
                    Files.readAllBytes(
                            ResourceUtils.getFile("classpath:demo/incoming_ringtone.mp3").toPath()
                    )
            ));

            File file4 = fileRepository.save(new File(
                    "Облако.jpg",
                    "image/jpeg",
                    now.minusDays(5).toInstant().toEpochMilli(),
                    now.minusDays(5).toInstant().toEpochMilli(),
                    220603L
            ));
            fileContentRepository.save(new FileContent(
                    file4,
                    Files.readAllBytes(
                            ResourceUtils.getFile("classpath:demo/Облако.jpg").toPath()
                    )
            ));
        };
    }
}
