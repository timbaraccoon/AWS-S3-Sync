package com.s3sync.app.cloudsyncservice;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.s3sync.app.dao.FileInfoRepository;
import com.s3sync.app.entity.FileInfo;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Service
public class CloudToDBSynchronizerServiceImpl implements CloudToDBSynchronizerService {

    private final FileInfoRepository repository;
    private final AmazonS3 s3client;

    // TODO change threshold
    private static final long REFRESH_THRESHOLD = TimeUnit.MINUTES.toMillis(1);
    private static final String bucketName = "cloudaware-test";


    @Autowired
    public CloudToDBSynchronizerServiceImpl(FileInfoRepository repository) {
        this.repository = repository;
        this.s3client = createS3Client();
    }

    private AmazonS3 createS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAZVYIBMMKMH74L2H6",
                "2MmPxx8HSX/UaAVZgJ49apprE2sBf/WMbCZc8z+c"
        );

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    @Override
    @PostConstruct
    public void harmonizeDatabase() {
        Runnable task = () -> { saveFromCloudToDB();
            try {
                Thread.sleep(REFRESH_THRESHOLD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }; // this part for first DB filling cuz it's created in memory from scratch with every launch
//        Runnable task = this::saveFromCloudToDB;
        new Thread(task).start();

//        try {
//            Thread.sleep(REFRESH_THRESHOLD);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private void someSketch() {




    }


    private void saveFromCloudToDB() {
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest().withBucketName(bucketName);

        ObjectListing os;
        int size = 0;

        HashSet<FileInfo> repoSet = new HashSet<>(repository.findAll());
        HashSet<FileInfo> cloudTargetSet = new HashSet<>();

        // TODO если нет в ДБ - удалить, если есть совпадение по имени но другое

        // help avoid limited (1000 max) bucket objects upload bug
        do {
            os = s3client.listObjects(listObjectsRequest);

            size += os.getObjectSummaries().size();


            for (S3ObjectSummary objectSummary : os.getObjectSummaries()) {
                FileInfo fileInfo = createFileInfoFromObjectSummary(objectSummary);
                cloudTargetSet.add(fileInfo);

                if (!(repoSet.contains(fileInfo))) {
                    repository.save(fileInfo);
                }
            }
            listObjectsRequest.setMarker(os.getNextMarker());

        } while (os.isTruncated());

        System.out.println("\nконтроль");
        System.out.println(size + " <==== кол-во элементов");

    }

    private FileInfo createFileInfoFromObjectSummary(S3ObjectSummary objectSummary) {
        FileInfo fileInfo = new FileInfo();

        fileInfo.setName(objectSummary.getKey());
        fileInfo.setType(FilenameUtils.getExtension(objectSummary.getKey()));
        fileInfo.setLastModified(convertToLocalDateTime(objectSummary.getLastModified()));
        fileInfo.setSize(objectSummary.getSize());
        fileInfo.setStorageClass(objectSummary.getStorageClass());

        return fileInfo;
    }

    private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return new java.sql.Timestamp(
                dateToConvert.getTime()).toLocalDateTime();
    }


}
