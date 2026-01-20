package com.example.keeplearning.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.keeplearning.entity.FileShare;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.FileShareRepository;
import com.example.keeplearning.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("share")
public class FileShareController {
    
    private final FileShareRepository fileShareRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.shared-path}")
    private String uploadDirectory;

    public FileShareController(FileShareRepository fileShareRepository, UserRepository userRepository) {
        this.fileShareRepository = fileShareRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public String showShared(@AuthenticationPrincipal User me, Model model) {

        List<FileShare> shared = fileShareRepository.findAllBySenderOrderByDestinationDescSentAtDesc(me);
        List<FileShare> sharedWithMe = fileShareRepository.findAllByDestinationOrderBySenderDescSentAtDesc(me);

        model.addAttribute("shared", shared);
        model.addAttribute("sharedWithMe", sharedWithMe);

        return "share/list";
    }

    @GetMapping("{userId}")
    public String showShare(
        @PathVariable Long userId,
        @AuthenticationPrincipal User me,
        Model model
    ) {
        User user = userRepository.findById(userId).orElseThrow();
        model.addAttribute("user", user);
        return "share/upload";
    }

    @PostMapping("{userId}")
    public String shareFile(
        @PathVariable Long userId,
        @RequestParam("file") MultipartFile file,
        @RequestParam("comment") String comment,
        @AuthenticationPrincipal User me,
        Model model
    ) throws IOException {
        User user = userRepository.findById(userId).orElseThrow();

        Path uploadDir = Paths.get(uploadDirectory);
        Path destDir = uploadDir.resolve(me.getId().toString());
        Files.createDirectories(destDir);

        Path destination = destDir.resolve(UUID.randomUUID() + "_" + file.getOriginalFilename());
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        System.out.println(file.getOriginalFilename() + " -> " + destination + " [Exists: " + Files.exists(destination) + "]");

        var share = FileShare.create(me, user, uploadDir.relativize(destination).toString(), file.getOriginalFilename(), comment);
        fileShareRepository.save(share);
        
        return "redirect:/user/view/" + user.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(
        @PathVariable Long id,
        @AuthenticationPrincipal User me
    ) throws IOException {
        FileShare share = fileShareRepository.findById(id).orElseThrow();
        
        Path uploadDir = Paths.get(uploadDirectory + "/" + me.getId());
        boolean deleted = Files.deleteIfExists(uploadDir.resolve(share.getFilePath()));
        System.out.println(share.getFilePath() + " deleted (" + deleted + ")");

        fileShareRepository.delete(share);
        return "redirect:/share/list";
    }

    @GetMapping("/download/{id}")
    public void downloadFile(
        @PathVariable Long id,
        @AuthenticationPrincipal User me,
        Model model,
        HttpServletResponse response
    ) throws IOException {
        FileShare share = fileShareRepository.findById(id).orElseThrow();

        if (!share.getSender().equalsId(me) && !share.getDestination().equalsId(me)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Path filePath = Paths.get(uploadDirectory).resolve(share.getFilePath());
        File file = filePath.toFile();

        if (!file.exists()) {
            System.err.println("File not found: '" + file.getPath() + "' [Exists: " + Files.exists(filePath) + "]");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(Files.probeContentType(filePath));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.setContentLengthLong(file.length());

        try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
            out.flush();
        }
    }

}
