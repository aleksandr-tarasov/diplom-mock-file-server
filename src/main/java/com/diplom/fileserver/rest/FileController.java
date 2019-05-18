package com.diplom.fileserver.rest;

import com.diplom.fileserver.dto.ShortVideoDescription;
import com.diplom.fileserver.dto.VideoMetadataDto;
import com.diplom.fileserver.exception.FileServerException;
import com.diplom.fileserver.service.FileService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/file/{id}")
    public ResponseEntity<ResourceRegion> getFile(@PathVariable String id,
                                                  @RequestHeader HttpHeaders headers) throws IOException {
        return fileService.getFilePart(id, headers.getRange().get(0));
    }

    @GetMapping("/metadata/{id}")
    public VideoMetadataDto getVideoMetadataInfo(@PathVariable String id) {
        return fileService.getVideoMetadata(id);
    }

    // UI
    @GetMapping("server/listVideos")
    public List<ShortVideoDescription> listVideos() {
        return fileService.listAllVideos();
    }


    @RequestMapping(value = "server/upload", method = RequestMethod.POST)
    public void upload(HttpServletRequest request) throws IOException, FileUploadException {
        fileService.saveFile(request);
    }

}
