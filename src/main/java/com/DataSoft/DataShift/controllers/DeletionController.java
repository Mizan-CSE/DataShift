package com.DataSoft.DataShift.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class DeletionController {
    @DeleteMapping("/delete-directory")
    public ResponseEntity<String> deleteRootDirectory() {
        String rootPath = ".\\dataset\\Branch-wise Data Segmentation";
        File rootDirectory = new File(rootPath);

        if (rootDirectory.exists() && rootDirectory.isDirectory()) {
            File[] subDirectories = rootDirectory.listFiles(File::isDirectory);

            if (subDirectories != null) {
                for (File directory : subDirectories) {
                    deleteDirectoryRecursively(directory);
                }
            }
            deleteDirectoryRecursively(rootDirectory); // Delete the root directory itself

            return new ResponseEntity<>("Root directory and its contents deleted successfully.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Root directory does not exist.", HttpStatus.NOT_FOUND);
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