package com.diplom.fileserver.entity.repository;


import com.diplom.fileserver.entity.FileDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDescriptionRepository extends JpaRepository<FileDescription, String> {
}
