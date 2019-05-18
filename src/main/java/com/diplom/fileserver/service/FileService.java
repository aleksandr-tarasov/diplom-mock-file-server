package com.diplom.fileserver.service;

import com.diplom.fileserver.dto.ShortVideoDescription;
import com.diplom.fileserver.dto.VideoMetadataDto;
import com.diplom.fileserver.entity.FileDescription;
import com.diplom.fileserver.entity.repository.FileDescriptionRepository;
import com.diplom.fileserver.exception.FileServerException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Math.min;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final int CONTENT_LENGTH = 1024 * 1024;
    private static final String FILESYSTEM_URL = "/home/alexandr/diplom/file-server/src/main/resources/videos";
    private static final Pattern PATTERN = Pattern.compile(".*\\.(.*)");

    private final FileDescriptionRepository fileDescriptionRepository;

    public ResponseEntity<ResourceRegion> getFilePart(String id, HttpRange range) throws IOException {
        FileDescription fileDescription = fileDescriptionRepository.findById(id)
                .orElseThrow(() -> new FileServerException("could not find file by id"));
        String fileUrl = String.format("file:%s/%s", FILESYSTEM_URL, fileDescription.getUrl());
        var file = new UrlResource(fileUrl);
        var region = getRegion(file, range);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory
                        .getMediaType(file)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }

    private ResourceRegion getRegion(Resource file, HttpRange range) throws IOException {
        var contentLength = file.contentLength();
        if (range != null) {
            var start = range.getRangeStart(contentLength);
            var end = range.getRangeEnd(contentLength);
            var rangeLength = min(CONTENT_LENGTH, end - start + 1);
            return new ResourceRegion(file, start, rangeLength);
        } else {
            var rangeLength = min(CONTENT_LENGTH, contentLength);
            return new ResourceRegion(file, 0, rangeLength);
        }
    }

    public List<ShortVideoDescription> listAllVideos() {
        return fileDescriptionRepository.findAll()
                .stream()
                .map(file ->
                        ShortVideoDescription.builder()
                                .id(file.getId())
                                .name(file.getName())
                                .build()
                )
                .collect(Collectors.toList());
    }


    @Transactional
    public VideoMetadataDto getVideoMetadata(String id) {
        FileDescription video = fileDescriptionRepository.getOne(id);
        video.setViews(video.getViews() + 1);
        return VideoMetadataDto.builder()
                .name(video.getName())
                .description(video.getDescription())
                .views(video.getViews())
                .build();
    }

    @Transactional
    public void saveFile(HttpServletRequest request) throws IOException, FileUploadException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            throw new FileServerException("uploaded file should be mulitpart");
        }
        ServletFileUpload upload = new ServletFileUpload();

        FileItemIterator iter = upload.getItemIterator(request);
        while (iter.hasNext()) {
            FileItemStream item = iter.next();
            item.getFieldName();
            InputStream stream = item.openStream();
            if (!item.isFormField()) {
                String filename = item.getName();

                FileDescription video = FileDescription.builder()
                        .description("some " + filename)
                        .name(filename)
                        .build();

                fileDescriptionRepository.save(video);
                fileDescriptionRepository.flush();

                Matcher matcher = PATTERN.matcher(filename);
                matcher.find();

                String  file = String.format("%s.%s", video.getId(), matcher.group(1));
                String url = String.format("%s/%s", FILESYSTEM_URL, file);

                video.setUrl(file);

                OutputStream out = new FileOutputStream(url);
                IOUtils.copy(stream, out);
                stream.close();
                out.close();
            }
        }
    }
}
