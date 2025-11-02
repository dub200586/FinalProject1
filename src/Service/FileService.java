package Service;

import Exception.FileProcessingException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileService {
    private final String fileExtension;

    public FileService(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public List<File> findTextFiles(String directoryPath) throws FileProcessingException {
        Path directory = Paths.get(directoryPath);

        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new FileProcessingException("Директория не найдена: " + directoryPath);
        }

        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(fileExtension))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка чтения директории: " + directoryPath, e);
        }
    }

    public String readFileContent(File file) throws FileProcessingException {
        try {
            return Files.readString(file.toPath()).trim();
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка чтения файла: " + file.getName(), e);
        }
    }

    public void saveToFile(String content, String filePath) throws FileProcessingException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка сохранения файла: " + filePath, e);
        }
    }

    public boolean isFileValid(File file) {
        return file.exists() && file.isFile() && file.length() > 0;
    }
}
