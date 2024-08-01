package com.DataSoft.DataShift.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class DeletionController {
    @DeleteMapping("/delete-directory")
    public ResponseEntity<String> deleteDirectory() {
        String relativePath = ".\\dataset\\Branch-wise Data Segmentation";
        File directory = new File(relativePath);

        if (directory.exists() && directory.isDirectory()) {
            deleteDirectoryRecursively(directory);
            return new ResponseEntity<>("Directory deleted successfully.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Directory does not exist.", HttpStatus.NOT_FOUND);
        }
    }

    private void deleteDirectoryRecursively(File file) {
        File[] allContents = file.listFiles();
        if (allContents != null) {
            for (File f : allContents) {
                deleteDirectoryRecursively(f);
            }
        }
        file.delete();
    }
}
